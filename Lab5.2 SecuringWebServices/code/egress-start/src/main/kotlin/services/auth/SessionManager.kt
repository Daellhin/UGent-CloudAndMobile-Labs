package be.ugent.idlab.predict.ocmt.egress.services.auth

import be.ugent.idlab.predict.ocmt.egress.services.resolve
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.util.logging.*
import kotlinx.serialization.Serializable
import java.util.*

object SessionManager {

    data object AlreadyExists: RuntimeException("Username already taken")
    data object InvalidCredentials: RuntimeException("Invalid credentials")

    @Serializable
    data class Credentials(
        val username: String,
        val password: String
    )

    @Serializable
    data class Token(
        val token: String
    )

    private val LOGGER = KtorSimpleLogger("services.Auth")

    private val properties = this::class.java
        .classLoader
        .getResourceAsStream("auth.properties")
        .use { Properties().apply { load(it) } }

    val secret = properties.resolve("jwt.secret")
    val issuer = properties.resolve("jwt.issuer")
    val audience = properties.resolve("jwt.audience")
    val expiry = properties.resolve("jwt.expiry").toLong()

    /**
     * Creates a user account using the credentials found in the body
     */
    suspend fun createAccount(credentials: Credentials): Result<Unit> {
        LOGGER.info("Incoming register attempt for `${credentials.username}`")
        return try {
            // Check if the username already exists in the database
            val userExists = UserManager.check(credentials.username, credentials.password)
            if (userExists)
                return Result.failure(AlreadyExists)

            // Create the user account in the database
            UserManager.create(credentials.username, credentials.password)
            Result.success(Unit)
        } catch (e: Exception) {
            LOGGER.error("Error creating account for `${credentials.username}`: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Creates a token result if the incoming call contains correct credentials, or a failure with matching exception
     */
    suspend fun processLogin(credentials: Credentials): Result<Token> {
        LOGGER.info("Incoming login attempt for `${credentials.username}`")
        return try {
            val isValid = UserManager.check(credentials.username, credentials.password)
            if (!isValid) {
                return Result.failure(InvalidCredentials)
            }

            val token = JWT.create()
                .withIssuer(issuer)
                .withAudience(audience)
                .withClaim("username", credentials.username)
                .withExpiresAt(Date(System.currentTimeMillis() + expiry))
                .sign(Algorithm.HMAC256(secret))

            Result.success(Token(token))
        } catch (e: Exception) {
            LOGGER.error("Error during login for `${credentials.username}`: ${e.message}")
            Result.failure(e)
        }
    }

}
