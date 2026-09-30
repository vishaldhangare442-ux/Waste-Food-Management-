package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.security.MessageDigest
import java.security.SecureRandom

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val securityQuestion: String = "What was your first pet's name?",
    val securityAnswerHash: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

object SecurityUtil {
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$salt$password".toByteArray(Charsets.UTF_8)
        val digest = md.digest(combined)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val hash = hashPassword(password, salt)
        return hash == expectedHash
    }
}
