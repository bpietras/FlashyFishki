package com.an.intelligence.flashyfishki.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PasswordUtils {
    
    private const val SALT_LENGTH = 16
    
    /**
     * Hashes a password with a random salt using SHA-256
     */
    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val passwordBytes = password.toByteArray()
        val saltBytes = Base64.getDecoder().decode(salt)
        
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(saltBytes)
        val hashedPassword = digest.digest(passwordBytes)
        
        val hashedPasswordBase64 = Base64.getEncoder().encodeToString(hashedPassword)
        return "$salt:$hashedPasswordBase64"
    }
    
    /**
     * Verifies a password against a stored hash
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false
        
        val salt = parts[0]
        val storedPasswordHash = parts[1]
        
        val saltBytes = Base64.getDecoder().decode(salt)
        val passwordBytes = password.toByteArray()
        
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(saltBytes)
        val hashedPassword = digest.digest(passwordBytes)
        
        val hashedPasswordBase64 = Base64.getEncoder().encodeToString(hashedPassword)
        return hashedPasswordBase64 == storedPasswordHash
    }
    
    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }
}
