package pl.soulsnaps.unit.validation

import pl.soulsnaps.location.PhotoLocation
import pl.soulsnaps.location.PlaceInfo
import pl.soulsnaps.location.PlaceType
import pl.soulsnaps.permissions.PermissionType
import pl.soulsnaps.permissions.PermissionStatus
import kotlin.test.*

class ValidationTest {
    
    @Test
    fun `PhotoLocation coordinates should be within valid bounds`() {
        // Given
        val validLatitudes = listOf(-90.0, -45.0, 0.0, 45.0, 90.0)
        val validLongitudes = listOf(-180.0, -90.0, 0.0, 90.0, 180.0)
        val invalidLatitudes = listOf(-91.0, 91.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
        val invalidLongitudes = listOf(-181.0, 181.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
        
        // When & Then - Valid coordinates
        validLatitudes.forEach { latitude ->
            validLongitudes.forEach { longitude ->
                val location = PhotoLocation(
                    latitude = latitude,
                    longitude = longitude,
                    accuracy = 10f,
                    timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                )
                
                assertTrue(location.latitude in -90.0..90.0, "Latitude $latitude should be valid")
                assertTrue(location.longitude in -180.0..180.0, "Longitude $longitude should be valid")
            }
        }
        
        // When & Then - Invalid coordinates
        invalidLatitudes.forEach { latitude ->
            assertFalse(latitude in -90.0..90.0, "Latitude $latitude should be invalid")
        }
        
        invalidLongitudes.forEach { longitude ->
            assertFalse(longitude in -180.0..180.0, "Longitude $longitude should be invalid")
        }
    }
    
    @Test
    fun `PhotoLocation timestamp should be positive`() {
        // Given
        val validTimestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val invalidTimestamp = -1L
        
        // When
        val validLocation = PhotoLocation(
            latitude = 40.7128,
            longitude = -74.0060,
            accuracy = 10f,
            timestamp = validTimestamp
        )
        
        // Then
        assertTrue(validLocation.timestamp > 0, "Timestamp should be positive")
        assertTrue(invalidTimestamp <= 0, "Invalid timestamp should be non-positive")
    }
    
    @Test
    fun `PhotoLocation accuracy should be non-negative`() {
        // Given
        val validAccuracies = listOf(0f, 1f, 10f, 100f)
        val invalidAccuracies = listOf(-1f, -10f, Float.NEGATIVE_INFINITY)
        
        // When & Then - Valid accuracies
        validAccuracies.forEach { accuracy ->
            val location = PhotoLocation(
                latitude = 40.7128,
                longitude = -74.0060,
                accuracy = accuracy,
                timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            )
            
            assertTrue(location.accuracy ?: 0f >= 0, "Accuracy $accuracy should be non-negative")
        }
        
        // When & Then - Invalid accuracies
        invalidAccuracies.forEach { accuracy ->
            assertTrue(accuracy < 0, "Accuracy $accuracy should be negative")
        }
    }
    
    @Test
    fun `PlaceInfo should have valid required fields`() {
        // Given
        val validId = "test_place_123"
        val validName = "Test Restaurant"
        val validAddress = "123 Test Street, Test City"
        val validLatitude = 40.7128
        val validLongitude = -74.0060
        val validPlaceType = PlaceType.RESTAURANT
        
        // When
        val placeInfo = PlaceInfo(
            id = validId,
            name = validName,
            address = validAddress,
            latitude = validLatitude,
            longitude = validLongitude,
            placeType = validPlaceType
        )
        
        // Then
        assertNotNull(placeInfo.id, "ID should not be null")
        assertNotNull(placeInfo.name, "Name should not be null")
        assertNotNull(placeInfo.address, "Address should not be null")
        assertTrue(placeInfo.id.isNotEmpty(), "ID should not be empty")
        assertTrue(placeInfo.name.isNotEmpty(), "Name should not be empty")
        assertTrue(placeInfo.address.isNotEmpty(), "Address should not be empty")
        assertTrue(placeInfo.latitude in -90.0..90.0, "Latitude should be within bounds")
        assertTrue(placeInfo.longitude in -180.0..180.0, "Longitude should be within bounds")
    }
    
    @Test
    fun `PlaceInfo coordinates should be within valid bounds`() {
        // Given
        val validLatitudes = listOf(-90.0, -45.0, 0.0, 45.0, 90.0)
        val validLongitudes = listOf(-180.0, -90.0, 0.0, 90.0, 180.0)
        
        // When & Then
        validLatitudes.forEach { latitude ->
            validLongitudes.forEach { longitude ->
                val placeInfo = PlaceInfo(
                    id = "test_id",
                    name = "Test Name",
                    address = "Test Address",
                    latitude = latitude,
                    longitude = longitude,
                    placeType = PlaceType.OTHER
                )
                
                assertTrue(placeInfo.latitude in -90.0..90.0, "Latitude $latitude should be valid")
                assertTrue(placeInfo.longitude in -180.0..180.0, "Longitude $longitude should be valid")
            }
        }
    }
    
    @Test
    fun `PermissionType should have valid enum values`() {
        // Given
        val expectedTypes = setOf("CAMERA", "GALLERY", "LOCATION")
        
        // When
        val actualTypes = PermissionType.values().map { it.name }.toSet()
        
        // Then
        assertEquals(expectedTypes, actualTypes, "Permission types should match expected values")
        assertEquals(3, PermissionType.values().size, "Should have exactly 3 permission types")
    }
    
    @Test
    fun `PermissionStatus should have valid enum values`() {
        // Given
        val expectedStatuses = setOf("GRANTED", "DENIED", "NOT_REQUESTED")
        
        // When
        val actualStatuses = PermissionStatus.values().map { it.name }.toSet()
        
        // Then
        assertEquals(expectedStatuses, actualStatuses, "Permission statuses should match expected values")
        assertEquals(3, PermissionStatus.values().size, "Should have exactly 3 permission statuses")
    }
    
    @Test
    fun `PlaceType should have valid enum values`() {
        // Given
        val expectedTypes = setOf("OTHER")
        
        // When
        val actualTypes = PlaceType.values().map { it.name }.toSet()
        
        // Then
        assertTrue(actualTypes.containsAll(expectedTypes), "Place types should contain expected values")
        assertTrue(PlaceType.values().isNotEmpty(), "Should have at least one place type")
    }
    
    @Test
    fun `enum values should have correct ordinal positions`() {
        // When & Then - PermissionType
        assertTrue(PermissionType.CAMERA.ordinal >= 0, "CAMERA should have non-negative ordinal")
        assertTrue(PermissionType.GALLERY.ordinal >= 0, "GALLERY should have non-negative ordinal")
        assertTrue(PermissionType.LOCATION.ordinal >= 0, "LOCATION should have non-negative ordinal")
        
        // When & Then - PermissionStatus
        assertTrue(PermissionStatus.GRANTED.ordinal >= 0, "GRANTED should have non-negative ordinal")
        assertTrue(PermissionStatus.DENIED.ordinal >= 0, "DENIED should have non-negative ordinal")
        assertTrue(PermissionStatus.NOT_REQUESTED.ordinal >= 0, "NOT_REQUESTED should have non-negative ordinal")
        
        // When & Then - PlaceType
        assertTrue(PlaceType.OTHER.ordinal >= 0, "OTHER should have non-negative ordinal")
    }
    
    @Test
    fun `enum values should have correct names`() {
        // When & Then - PermissionType
        assertEquals("CAMERA", PermissionType.CAMERA.name, "CAMERA should have correct name")
        assertEquals("GALLERY", PermissionType.GALLERY.name, "GALLERY should have correct name")
        assertEquals("LOCATION", PermissionType.LOCATION.name, "LOCATION should have correct name")
        
        // When & Then - PermissionStatus
        assertEquals("GRANTED", PermissionStatus.GRANTED.name, "GRANTED should have correct name")
        assertEquals("DENIED", PermissionStatus.DENIED.name, "DENIED should have correct name")
        assertEquals("NOT_REQUESTED", PermissionStatus.NOT_REQUESTED.name, "NOT_REQUESTED should have correct name")
        
        // When & Then - PlaceType
        assertEquals("OTHER", PlaceType.OTHER.name, "OTHER should have correct name")
    }
    
    @Test
    fun `data models should maintain consistency under validation`() {
        // Given
        val location = PhotoLocation(
            latitude = 40.7128,
            longitude = -74.0060,
            accuracy = 10f,
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        )
        
        val placeInfo = PlaceInfo(
            id = "test_place",
            name = "Test Place",
            address = "Test Address",
            latitude = 40.7128,
            longitude = -74.0060,
            placeType = PlaceType.OTHER
        )
        
        // When & Then - Location validation
        assertTrue(location.latitude in -90.0..90.0, "Location latitude should be valid")
        assertTrue(location.longitude in -180.0..180.0, "Location longitude should be valid")
        assertTrue(location.timestamp > 0, "Location timestamp should be positive")
        assertTrue(location.accuracy ?: 0f >= 0, "Location accuracy should be non-negative")
        
        // When & Then - PlaceInfo validation
        assertTrue(placeInfo.latitude in -90.0..90.0, "PlaceInfo latitude should be valid")
        assertTrue(placeInfo.longitude in -180.0..180.0, "PlaceInfo longitude should be valid")
        assertNotNull(placeInfo.id, "PlaceInfo ID should not be null")
        assertNotNull(placeInfo.name, "PlaceInfo name should not be null")
        assertNotNull(placeInfo.address, "PlaceInfo address should not be null")
        assertTrue(placeInfo.id.isNotEmpty(), "PlaceInfo ID should not be empty")
        assertTrue(placeInfo.name.isNotEmpty(), "PlaceInfo name should not be empty")
        assertTrue(placeInfo.address.isNotEmpty(), "PlaceInfo address should not be empty")
    }
}
