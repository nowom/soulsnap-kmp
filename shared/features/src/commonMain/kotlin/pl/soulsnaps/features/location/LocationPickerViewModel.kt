package pl.soulsnaps.features.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.interactor.SearchLocationsUseCase

/**
 * ViewModel for location picker with autocomplete
 * Manages search state and coordinates with UseCase
 */
data class LocationPickerState(
    val query: String = "",
    val suggestions: List<LocationSuggestion> = emptyList(),
    val isSearching: Boolean = false,
    val isLoadingCurrentLocation: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LocationPickerIntent {
    data class UpdateQuery(val query: String) : LocationPickerIntent
    data object ClearQuery : LocationPickerIntent
    data class SelectLocation(val suggestion: LocationSuggestion) : LocationPickerIntent
    data object UseCurrentLocation : LocationPickerIntent
    data object ClearError : LocationPickerIntent
}

class LocationPickerViewModel(
    private val searchLocationsUseCase: SearchLocationsUseCase,
    private val locationService: LocationService
) : ViewModel() {
    
    private val _state = MutableStateFlow(LocationPickerState())
    val state: StateFlow<LocationPickerState> = _state.asStateFlow()
    
    private var searchJob: Job? = null
    
    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val MIN_QUERY_LENGTH = 2
    }
    
    fun handleIntent(intent: LocationPickerIntent) {
        when (intent) {
            is LocationPickerIntent.UpdateQuery -> updateQuery(intent.query)
            is LocationPickerIntent.ClearQuery -> clearQuery()
            is LocationPickerIntent.SelectLocation -> selectLocation(intent.suggestion)
            is LocationPickerIntent.UseCurrentLocation -> useCurrentLocation()
            is LocationPickerIntent.ClearError -> clearError()
        }
    }
    
    private fun updateQuery(newQuery: String) {
        println("DEBUG: LocationPickerViewModel - updating query to: '$newQuery'")
        
        _state.update { it.copy(query = newQuery, errorMessage = null) }
        
        // Cancel previous search
        searchJob?.cancel()
        
        // Start new search with debounce
        if (newQuery.length >= MIN_QUERY_LENGTH) {
            searchJob = viewModelScope.launch {
                _state.update { it.copy(isSearching = true) }
                
                try {
                    // Debounce - wait before searching
                    delay(SEARCH_DEBOUNCE_MS)
                    
                    // Perform search in background thread
                    val results = searchLocationsUseCase(newQuery)
                    
                    _state.update { 
                        it.copy(
                            suggestions = results,
                            isSearching = false,
                            errorMessage = null
                        )
                    }
                    
                } catch (e: Exception) {
                    println("ERROR: LocationPickerViewModel - search failed: ${e.message}")
                    _state.update { 
                        it.copy(
                            suggestions = emptyList(),
                            isSearching = false,
                            errorMessage = "Search failed: ${e.message}"
                        )
                    }
                }
            }
        } else {
            // Clear suggestions for short queries
            _state.update { 
                it.copy(
                    suggestions = emptyList(),
                    isSearching = false
                )
            }
        }
    }
    
    private fun clearQuery() {
        println("DEBUG: LocationPickerViewModel - clearing query")
        searchJob?.cancel()
        _state.update { 
            LocationPickerState() // Reset to initial state
        }
    }
    
    private fun selectLocation(suggestion: LocationSuggestion) {
        println("DEBUG: LocationPickerViewModel - location selected: ${suggestion.name}")
        // Note: The actual selection callback will be handled by the UI layer
        // This is just for logging/tracking purposes
    }
    
    private fun useCurrentLocation() {
        println("DEBUG: LocationPickerViewModel - getting current location via GPS")
        
        viewModelScope.launch {
            _state.update { it.copy(isLoadingCurrentLocation = true, errorMessage = null) }
            
            try {
                // Check permissions first
                if (!locationService.hasLocationPermission()) {
                    println("DEBUG: LocationPickerViewModel - requesting location permission")
                    val permissionGranted = locationService.requestLocationPermission()
                    
                    if (!permissionGranted) {
                        _state.update { 
                            it.copy(
                                isLoadingCurrentLocation = false,
                                errorMessage = "Location permission is required to use GPS."
                            )
                        }
                        return@launch
                    }
                }
                
                // Get current location
                when (val result = locationService.getCurrentLocation()) {
                    is LocationResult.Success -> {
                        val locationName = result.address ?: "Current Location"
                        println("DEBUG: LocationPickerViewModel - GPS location success: $locationName")
                        
                        _state.update { 
                            it.copy(
                                query = locationName,
                                isLoadingCurrentLocation = false,
                                suggestions = emptyList() // Clear suggestions when using GPS
                            )
                        }
                    }
                    
                    is LocationResult.Error.PermissionDenied -> {
                        _state.update { 
                            it.copy(
                                isLoadingCurrentLocation = false,
                                errorMessage = "Location permission denied. Please enable location access in settings."
                            )
                        }
                    }
                    
                    is LocationResult.Error.LocationDisabled -> {
                        _state.update { 
                            it.copy(
                                isLoadingCurrentLocation = false,
                                errorMessage = "Location services are disabled. Please enable GPS in settings."
                            )
                        }
                    }
                    
                    is LocationResult.Error.Timeout -> {
                        _state.update { 
                            it.copy(
                                isLoadingCurrentLocation = false,
                                errorMessage = "Location timeout. Please try again or check GPS signal."
                            )
                        }
                    }
                    
                    is LocationResult.Error.Unknown -> {
                        _state.update { 
                            it.copy(
                                isLoadingCurrentLocation = false,
                                errorMessage = "Unable to get location: ${result.message}"
                            )
                        }
                    }
                }
                
            } catch (e: Exception) {
                println("ERROR: LocationPickerViewModel - GPS location failed: ${e.message}")
                _state.update { 
                    it.copy(
                        isLoadingCurrentLocation = false,
                        errorMessage = "Unable to get your location. Please try again."
                    )
                }
            }
        }
    }
    
    private fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
    
    fun setInitialQuery(query: String?) {
        if (!query.isNullOrBlank()) {
            println("DEBUG: LocationPickerViewModel - setting initial query: '$query'")
            _state.update { it.copy(query = query) }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        println("DEBUG: LocationPickerViewModel - cleared")
    }
}
