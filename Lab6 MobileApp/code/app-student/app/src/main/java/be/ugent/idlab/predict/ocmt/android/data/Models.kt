package be.ugent.idlab.predict.ocmt.android.data

import kotlinx.serialization.Serializable


@Serializable
data class Credentials(
    val username: String,
    val password: String
)

@Serializable
data class User(val token: String)
