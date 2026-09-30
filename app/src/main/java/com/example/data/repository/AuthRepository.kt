package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.UserDao
import com.example.data.model.SecurityUtil
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val userDao: UserDao,
    context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser.asStateFlow()

    suspend fun checkSession(): User? {
        val savedUserId = prefs.getLong("current_user_id", -1L)
        if (savedUserId != -1L) {
            val user = userDao.getUserById(savedUserId)
            _currentUser.value = user
            return user
        }
        return null
    }

    suspend fun login(email: String, password: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }

        val user = userDao.getUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("No account found with this email"))

        val isPasswordCorrect = SecurityUtil.verifyPassword(password, user.salt, user.passwordHash)
        if (!isPasswordCorrect) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }

        prefs.edit().putLong("current_user_id", user.id).apply()
        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        securityAnswer: String
    ): Result<User> {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanAnswer = securityAnswer.trim().lowercase()

        if (cleanName.length < 2) {
            return Result.failure(IllegalArgumentException("Please enter a valid name (at least 2 characters)"))
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters long"))
        }

        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists"))
        }

        val salt = SecurityUtil.generateSalt()
        val passwordHash = SecurityUtil.hashPassword(password, salt)
        val answerHash = SecurityUtil.hashPassword(cleanAnswer, salt)

        val newUser = User(
            name = cleanName,
            email = cleanEmail,
            passwordHash = passwordHash,
            salt = salt,
            securityAnswerHash = answerHash
        )

        val newId = userDao.insertUser(newUser)
        val createdUser = newUser.copy(id = newId)
        prefs.edit().putLong("current_user_id", newId).apply()
        _currentUser.value = createdUser
        return Result.success(createdUser)
    }

    suspend fun resetPassword(
        email: String,
        securityAnswer: String,
        newPassword: String
    ): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        val cleanAnswer = securityAnswer.trim().lowercase()

        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters"))
        }

        val user = userDao.getUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("No account found with this email"))

        // Verify security answer
        val answerCorrect = if (user.securityAnswerHash.isNotBlank()) {
            SecurityUtil.verifyPassword(cleanAnswer, user.salt, user.securityAnswerHash)
        } else {
            true // Allow reset if security answer wasn't set previously
        }

        if (!answerCorrect) {
            return Result.failure(IllegalArgumentException("Security verification answer is incorrect"))
        }

        val newSalt = SecurityUtil.generateSalt()
        val newHash = SecurityUtil.hashPassword(newPassword, newSalt)
        userDao.updatePassword(cleanEmail, newHash, newSalt)
        return Result.success(Unit)
    }

    fun logout() {
        prefs.edit().remove("current_user_id").apply()
        _currentUser.value = null
    }

    suspend fun quickLoginDemo(): Result<User> {
        return login("demo@wastewise.com", "Demo123!")
    }
}
