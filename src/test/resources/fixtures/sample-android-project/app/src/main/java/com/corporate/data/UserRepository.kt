package com.corporate.data

data class User(
    val id: String,
    val displayName: String
)

class UserRepository {

    private var cache: User? = null

    fun currentUser(): User {
        return cache ?: User(id = "0", displayName = "guest").also { cache = it }
    }

    fun warmUp() {
        currentUser()
    }
}
