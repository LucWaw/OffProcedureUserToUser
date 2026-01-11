package fr.lucwaw.utou.data.repository

import fr.lucwaw.utou.user.User

interface UserRepository {
    suspend fun getUsers(): List<User>
}
