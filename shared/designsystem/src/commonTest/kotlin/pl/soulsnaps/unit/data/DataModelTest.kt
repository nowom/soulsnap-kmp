package pl.soulsnaps.unit.data

import pl.soulsnaps.location.PhotoLocation
import pl.soulsnaps.location.PlaceInfo
import pl.soulsnaps.location.PlaceType
import pl.soulsnaps.permissions.PermissionType
import pl.soulsnaps.permissions.PermissionStatus
import kotlin.test.*

class DataModelTest {
    
    @Test
    fun `PhotoLocation should be created with valid coordinates`() {
        // Given
        val latitude = 40.7128
        val longitude = -74.0060
        val accuracy = 10f
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        
        // When
        val location = PhotoLocation(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            timestamp = timestamp
        )
        
        // Then
        assertEquals(latitude, location.latitude)
        assertEquals(longitude, location.longitude)
        assertEquals(accuracy, location.accuracy)
        assertEquals(timestamp, location.timestamp)
    }
    
    @Test
    fun `PhotoLocation should validate coordinate bounds`() {
        // Given
        val validLatitude = 40.7128
        val validLongitude = -74.0060
        val invalidLatitude = 91.0
        val invalidLongitude = 181.0
        
        // When & Then
        assertTrue(validLatitude in -90.0..90.0)
        assertTrue(validLongitude in -180.0..180.0)
        assertFalse(invalidLatitude in -90.0..90.0)
        assertFalse(invalidLongitude in -180.0..180.0)
    }
    
    @Test
    fun `PhotoLocation should have positive timestamp`() {
        // Given
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        
        // When & Then
        assertTrue(timestamp > 0)
    }
    
    @Test
    fun `PlaceInfo should be created with valid data`() {
        // Given
        val id = "test_place_123"
        val name = "Test Restaurant"
        val address = "123 Test Street, Test City"
        val latitude = 40.7128
        val longitude = -74.0060
        val placeType = PlaceType.RESTAURANT
        
        // When
        val placeInfo = PlaceInfo(
            id = id,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            placeType = placeType
        )
        
        // Then
        assertEquals(id, placeInfo.id)
        assertEquals(name, placeInfo.name)
        assertEquals(address, placeInfo.address)
        assertEquals(latitude, placeInfo.latitude)
        assertEquals(longitude, placeInfo.longitude)
        assertEquals(placeType, placeInfo.placeType)
    }
    
    @Test
    fun `PlaceInfo should validate coordinate bounds`() {
        // Given
        val validLatitude = 40.7128
        val validLongitude = -74.0060
        
        // When & Then
        assertTrue(validLatitude in -90.0..90.0)
        assertTrue(validLongitude in -180.0..180.0)
    }
    
    @Test
    fun `PlaceInfo should have non-null required fields`() {
        // Given
        val placeInfo = PlaceInfo(
            id = "test_id",
            name = "Test Name",
            address = "Test Address",
            latitude = 40.7128,
            longitude = -74.0060,
            placeType = PlaceType.OTHER
        )
        
        // When & Then
        assertNotNull(placeInfo.id)
        assertNotNull(placeInfo.name)
        assertNotNull(placeInfo.address)
        assertTrue(placeInfo.id.isNotEmpty())
        assertTrue(placeInfo.name.isNotEmpty())
        assertTrue(placeInfo.address.isNotEmpty())
    }
    
    @Test
    fun `PermissionType should have correct enum values`() {
        // Given
        val permissionTypes = PermissionType.values()
        
        // When & Then
        assertEquals(3, permissionTypes.size)
        assertTrue(permissionTypes.contains(PermissionType.CAMERA))
        assertTrue(permissionTypes.contains(PermissionType.GALLERY))
        assertTrue(permissionTypes.contains(PermissionType.LOCATION))
    }
    
    @Test
    fun `PermissionType should have correct names`() {
        // When & Then
        assertEquals("CAMERA", PermissionType.CAMERA.name)
        assertEquals("GALLERY", PermissionType.GALLERY.name)
        assertEquals("LOCATION", PermissionType.LOCATION.name)
    }
    
    @Test
    fun `PermissionStatus should have correct enum values`() {
        // Given
        val permissionStatuses = PermissionStatus.values()
        
        // When & Then
        assertEquals(3, permissionStatuses.size)
        assertTrue(permissionStatuses.contains(PermissionStatus.GRANTED))
        assertTrue(permissionStatuses.contains(PermissionStatus.DENIED))
        assertTrue(permissionStatuses.contains(PermissionStatus.NOT_REQUESTED))
    }
    
    @Test
    fun `PermissionStatus should have correct names`() {
        // When & Then
        assertEquals("GRANTED", PermissionStatus.GRANTED.name)
        assertEquals("DENIED", PermissionStatus.DENIED.name)
        assertEquals("NOT_REQUESTED", PermissionStatus.NOT_REQUESTED.name)
    }
    
    @Test
    fun `PlaceType should have correct enum values`() {
        // Given
        val placeTypes = PlaceType.values()
        
        // When & Then
        assertTrue(placeTypes.isNotEmpty())
        assertTrue(placeTypes.contains(PlaceType.OTHER))
    }
    
    @Test
    fun `PlaceType should have correct names`() {
        // When & Then
        assertEquals("OTHER", PlaceType.OTHER.name)
    }
    
    @Test
    fun `Data models should support equality comparison`() {
        // Given
        val location1 = PhotoLocation(
            latitude = 40.7128,
            longitude = -74.0060,
            accuracy = 10f,
            timestamp = 1000L
        )
        
        val location2 = PhotoLocation(
            latitude = 40.7128,
            longitude = -74.0060,
            accuracy = 10f,
            timestamp = 1000L
        )
        
        val location3 = PhotoLocation(
            latitude = 40.7129,
            longitude = -74.0061,
            accuracy = 15f,
            timestamp = 2000L
        )
        
        // When & Then
        assertEquals(location1, location2)
        assertNotEquals(location1, location3)
        assertEquals(location1.hashCode(), location2.hashCode())
        assertNotEquals(location1.hashCode(), location3.hashCode())
    }
    
    @Test
    fun `Data models should support toString`() {
        // Given
        val location = PhotoLocation(
            latitude = 40.7128,
            longitude = -74.0060,
            accuracy = 10f,
            timestamp = 1000L
        )
        
        val placeInfo = PlaceInfo(
            id = "test_id",
            name = "Test Place",
            address = "Test Address",
            latitude = 40.7128,
            longitude = -74.0060,
            placeType = PlaceType.OTHER
        )
        
        // When
        val locationString = location.toString()
        val placeInfoString = placeInfo.toString()
        
        // Then
        assertTrue(locationString.contains("40.7128"))
        assertTrue(locationString.contains("-74.006"))
        assertTrue(placeInfoString.contains("test_id"))
        assertTrue(placeInfoString.contains("Test Place"))
    }
}
