package pl.soulsnaps.features.memoryhub.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.interactor.GetAllMemoriesUseCase
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.utils.getCurrentTimeMillis

class MemoryMapViewModel (
    private val getAllMemoriesUseCase: GetAllMemoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadMemoriesWithLocation()
    }

    private fun loadMemoriesWithLocation() {
        viewModelScope.launch {
            println("DEBUG: MemoryMapViewModel.loadMemoriesWithLocation() - starting to load memories")
            _uiState.update { it.copy(isLoading = true) }
            getAllMemoriesUseCase().collect { memories ->
                println("DEBUG: MemoryMapViewModel - received ${memories.size} total memories")
                memories.forEach { memory ->
                    println("DEBUG: MemoryMapViewModel - memory: id=${memory.id}, title='${memory.title}', lat=${memory.latitude}, lng=${memory.longitude}, locationName='${memory.locationName}'")
                }
                
                var withLocation = memories.filter { it.latitude != null && it.longitude != null }
                println("DEBUG: MemoryMapViewModel - filtered to ${withLocation.size} memories with coordinates")
                
                // ADD MOCK DATA FOR TESTING
                if (withLocation.isEmpty()) {
                    println("DEBUG: MemoryMapViewModel - no memories with coordinates, adding mock data for testing")
                    withLocation = createMockMemoriesWithLocation()
                }
                
                _uiState.update {
                    it.copy(
                        isLoading = false, 
                        memoriesWithLocation = withLocation
                    )
                }
            }
        }
    }
    
    private fun createMockMemoriesWithLocation(): List<Memory> {
        return listOf(
            Memory(
                id = 9001,
                title = "Kraków Old Town",
                description = "Beautiful day in Kraków's main square",
                createdAt = getCurrentTimeMillis() - 86400000, // Yesterday
                mood = pl.soulsnaps.domain.model.MoodType.HAPPY,
                photoUri = null,
                audioUri = null,
                locationName = "Kraków, Poland",
                latitude = 50.0647, // Kraków coordinates
                longitude = 19.9450,
                affirmation = "Today was amazing in Kraków!"
            ),
            Memory(
                id = 9002,
                title = "Warsaw Palace",
                description = "Visiting the Royal Castle in Warsaw",
                createdAt = getCurrentTimeMillis() - 172800000, // 2 days ago
                mood = pl.soulsnaps.domain.model.MoodType.EXCITED,
                photoUri = null,
                audioUri = null,
                locationName = "Warsaw, Poland",
                latitude = 52.2297, // Warsaw coordinates
                longitude = 21.0122,
                affirmation = "History comes alive here!"
            ),
            Memory(
                id = 9003,
                title = "Gdańsk Seaside",
                description = "Peaceful moment by the Baltic Sea",
                createdAt = getCurrentTimeMillis() - 259200000, // 3 days ago
                mood = pl.soulsnaps.domain.model.MoodType.RELAXED,
                photoUri = null,
                audioUri = null,
                locationName = "Gdańsk, Poland",
                latitude = 54.3520, // Gdańsk coordinates
                longitude = 18.6466,
                affirmation = "The sea brings me peace"
            ),
            Memory(
                id = 9004,
                title = "Zakopane Mountains",
                description = "Hiking in the Tatra Mountains",
                createdAt = getCurrentTimeMillis() - 345600000, // 4 days ago
                mood = pl.soulsnaps.domain.model.MoodType.EXCITED,
                photoUri = null,
                audioUri = null,
                locationName = "Zakopane, Poland",
                latitude = 49.2992, // Zakopane coordinates
                longitude = 19.9496,
                affirmation = "Mountains give me strength!"
            ),
            Memory(
                id = 9005,
                title = "Wrocław Market Square",
                description = "Colorful buildings and great atmosphere",
                createdAt = getCurrentTimeMillis() - 432000000, // 5 days ago
                mood = pl.soulsnaps.domain.model.MoodType.HAPPY,
                photoUri = null,
                audioUri = null,
                locationName = "Wrocław, Poland",
                latitude = 51.1079, // Wrocław coordinates
                longitude = 17.0385,
                affirmation = "Beauty is everywhere!"
            )
        )
    }
}

data class MapUiState(
    val isLoading: Boolean = false,
    val memoriesWithLocation: List<Memory> = emptyList()
)
