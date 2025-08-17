package pl.soulsnaps.unit.error

import kotlin.test.*

class ErrorHandlingTest {
    
    @Test
    fun `exception creation should work correctly`() {
        // Given
        val exceptionMessage = "Test exception message"
        val exception = RuntimeException(exceptionMessage)
        
        // When
        val message = exception.message
        val type = exception::class.simpleName
        
        // Then
        assertNotNull(message, "Exception message should not be null")
        assertEquals(exceptionMessage, message, "Exception message should match")
        assertEquals("RuntimeException", type, "Exception type should be correct")
    }
    
    @Test
    fun `exception with null message should be handled correctly`() {
        // Given
        val exception = RuntimeException(null as String?)
        
        // When
        val message = exception.message
        
        // Then
        assertNull(message, "Exception message should be null")
    }
    
    @Test
    fun `exception with empty message should be handled correctly`() {
        // Given
        val exception = RuntimeException("")
        
        // When
        val message = exception.message
        
        // Then
        assertNotNull(message, "Exception message should not be null")
        assertEquals("", message, "Exception message should be empty string")
        assertTrue(message.isEmpty(), "Exception message should be empty")
    }
    
    @Test
    fun `different exception types should have correct names`() {
        // Given
        val runtimeException = RuntimeException("Runtime error")
        val illegalArgumentException = IllegalArgumentException("Invalid argument")
        val nullPointerException = NullPointerException("Null pointer")
        
        // When & Then
        assertEquals("RuntimeException", runtimeException::class.simpleName)
        assertEquals("IllegalArgumentException", illegalArgumentException::class.simpleName)
        assertEquals("NullPointerException", nullPointerException::class.simpleName)
    }
    
    @Test
    fun `exception cause should be preserved`() {
        // Given
        val cause = RuntimeException("Original cause")
        val exception = RuntimeException("Wrapper exception", cause)
        
        // When
        val retrievedCause = exception.cause
        
        // Then
        assertNotNull(retrievedCause, "Exception cause should not be null")
        assertEquals(cause, retrievedCause, "Exception cause should match original")
        assertEquals("Original cause", retrievedCause.message, "Cause message should be preserved")
    }
    
    @Test
    fun `exception without cause should return null`() {
        // Given
        val exception = RuntimeException("No cause")
        
        // When
        val cause = exception.cause
        
        // Then
        assertNull(cause, "Exception without cause should return null")
    }
    
    @Test
    fun `exception toString should contain message`() {
        // Given
        val exceptionMessage = "Test exception for toString"
        val exception = RuntimeException(exceptionMessage)
        
        // When
        val stringRepresentation = exception.toString()
        
        // Then
        assertNotNull(stringRepresentation, "String representation should not be null")
        assertTrue(stringRepresentation.contains(exceptionMessage), "String should contain exception message")
        assertTrue(stringRepresentation.contains("RuntimeException"), "String should contain exception type")
    }
    
    @Test
    fun `exception with null message toString should work`() {
        // Given
        val exception = RuntimeException(null as String?)
        
        // When
        val stringRepresentation = exception.toString()
        
        // Then
        assertNotNull(stringRepresentation, "String representation should not be null")
        assertTrue(stringRepresentation.contains("RuntimeException"), "String should contain exception type")
    }
    
    @Test
    fun `multiple exceptions should be handled independently`() {
        // Given
        val exception1 = RuntimeException("First exception")
        val exception2 = RuntimeException("Second exception")
        val exception3 = IllegalArgumentException("Third exception")
        
        // When & Then
        assertNotNull(exception1.message)
        assertNotNull(exception2.message)
        assertNotNull(exception3.message)
        
        assertEquals("First exception", exception1.message)
        assertEquals("Second exception", exception2.message)
        assertEquals("Third exception", exception3.message)
        
        assertNotEquals(exception1.message, exception2.message)
        assertNotEquals(exception2.message, exception3.message)
        assertNotEquals(exception1.message, exception3.message)
    }
    
    @Test
    fun `exception with special characters should be handled correctly`() {
        // Given
        val specialMessage = "Exception with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?"
        val exception = RuntimeException(specialMessage)
        
        // When
        val message = exception.message
        
        // Then
        assertNotNull(message, "Exception message should not be null")
        assertEquals(specialMessage, message, "Exception message should preserve special characters")
    }
    
    @Test
    fun `exception with unicode characters should be handled correctly`() {
        // Given
        val unicodeMessage = "Exception with unicode: 你好世界 🌍 🚀"
        val exception = RuntimeException(unicodeMessage)
        
        // When
        val message = exception.message
        
        // Then
        assertNotNull(message, "Exception message should not be null")
        assertEquals(unicodeMessage, message, "Exception message should preserve unicode characters")
    }
    
    @Test
    fun `exception with very long message should be handled correctly`() {
        // Given
        val longMessage = "A".repeat(10000)
        val exception = RuntimeException(longMessage)
        
        // When
        val message = exception.message
        
        // Then
        assertNotNull(message, "Exception message should not be null")
        assertEquals(longMessage, message, "Exception message should preserve long content")
        assertEquals(10000, message.length, "Exception message should have correct length")
    }
    
    @Test
    fun `exception inheritance should work correctly`() {
        // Given
        val exception = RuntimeException("Test")
        
        // When & Then
        assertTrue(exception is Exception, "RuntimeException should be instance of Exception")
        assertTrue(exception is Throwable, "RuntimeException should be instance of Throwable")
        assertTrue(exception is RuntimeException, "RuntimeException should be instance of RuntimeException")
    }
}
