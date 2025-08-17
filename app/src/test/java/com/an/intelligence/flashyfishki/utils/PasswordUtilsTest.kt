package com.an.intelligence.flashyfishki.utils

import org.junit.Assert.*
import org.junit.Test

class PasswordUtilsTest {

    @Test
    fun `hashPassword should return different hashes for same password`() {
        // Given
        val password = "testPassword123"

        // When
        val hash1 = PasswordUtils.hashPassword(password)
        val hash2 = PasswordUtils.hashPassword(password)

        // Then
        assertNotEquals("Hashes should be different due to different salts", hash1, hash2)
        assertTrue("Hash should contain salt and password hash separated by colon", hash1.contains(":"))
        assertTrue("Hash should contain salt and password hash separated by colon", hash2.contains(":"))
    }

    @Test
    fun `verifyPassword should return true for correct password`() {
        // Given
        val password = "testPassword123"
        val hash = PasswordUtils.hashPassword(password)

        // When
        val isValid = PasswordUtils.verifyPassword(password, hash)

        // Then
        assertTrue("Password verification should succeed for correct password", isValid)
    }

    @Test
    fun `verifyPassword should return false for incorrect password`() {
        // Given
        val correctPassword = "testPassword123"
        val incorrectPassword = "wrongPassword"
        val hash = PasswordUtils.hashPassword(correctPassword)

        // When
        val isValid = PasswordUtils.verifyPassword(incorrectPassword, hash)

        // Then
        assertFalse("Password verification should fail for incorrect password", isValid)
    }

    @Test
    fun `verifyPassword should return false for malformed hash`() {
        // Given
        val password = "testPassword123"
        val malformedHash = "invalid_hash_format"

        // When
        val isValid = PasswordUtils.verifyPassword(password, malformedHash)

        // Then
        assertFalse("Password verification should fail for malformed hash", isValid)
    }

    @Test
    fun `verifyPassword should return false for empty hash`() {
        // Given
        val password = "testPassword123"
        val emptyHash = ""

        // When
        val isValid = PasswordUtils.verifyPassword(password, emptyHash)

        // Then
        assertFalse("Password verification should fail for empty hash", isValid)
    }

    @Test
    fun `verifyPassword should handle special characters in password`() {
        // Given
        val password = "testPassword123!@#$%^&*()"
        val hash = PasswordUtils.hashPassword(password)

        // When
        val isValid = PasswordUtils.verifyPassword(password, hash)

        // Then
        assertTrue("Password verification should work with special characters", isValid)
    }

    @Test
    fun `verifyPassword should handle unicode characters in password`() {
        // Given
        val password = "testPassword123ąęźćżółń"
        val hash = PasswordUtils.hashPassword(password)

        // When
        val isValid = PasswordUtils.verifyPassword(password, hash)

        // Then
        assertTrue("Password verification should work with unicode characters", isValid)
    }
}
