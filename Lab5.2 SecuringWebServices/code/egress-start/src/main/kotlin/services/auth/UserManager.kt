package be.ugent.idlab.predict.ocmt.egress.services.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import be.ugent.idlab.predict.ocmt.egress.services.resolve
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object UserManager {

    object UserTable: Table() {

        val username = varchar("username", 50)
        val hash = binary("hash", 60)

        override val primaryKey = PrimaryKey(username)

    }

    private val properties = this::class.java
        .classLoader
        .getResourceAsStream("auth.properties")
        .use { Properties().apply { load(it) } }

    private val database = Database.connect(
        url = properties.resolve("mariadb.url"),
        user = properties.resolve("mariadb.username"),
        password = properties.resolve("mariadb.password"),
        databaseConfig = DatabaseConfig(
            body = {
                defaultRepetitionAttempts = 1
            }
        )
    )
    private val hasher = BCrypt.withDefaults()
    private val verifyer = BCrypt.verifyer()

    init {
        transaction(database) {
            SchemaUtils.create(UserTable)
        }
    }

    private suspend inline fun <T> db(noinline block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(context = Dispatchers.IO, db = database, statement = block)

    /**
     * Creates a user with the given `name` and `password`. This operation may fail, in which case `false` is returned.
     * The most common cause for a failure is name collision (name already in use).
     */
    suspend fun create(name: String, password: String): Boolean {
        return try {
            db {
                UserTable.insert {
                    it[username] = name
                    it[hash] = hasher.hash(12, password.toCharArray())
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks the provided `name` and `password`, returning `true` if the credentials are correct.
     */
    suspend fun check(name: String, password: String): Boolean {
        return try {
            db {
                UserTable
                    .select(UserTable.hash)
                    .where { UserTable.username eq name }
                    .singleOrNull()
                    ?.let { record -> verifyer.verify(password.toCharArray(), record[UserTable.hash]).verified }
                    ?: false
            }
        } catch (e: Exception) {
            false
        }
    }

}
