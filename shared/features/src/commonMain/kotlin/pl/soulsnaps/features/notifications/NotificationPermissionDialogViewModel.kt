package pl.soulsnaps.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.access.storage.UserPreferencesStorage

data class NotificationPermissionDialogState(
    val isVisible: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val userHasDecided: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showSuccessMessage: Boolean = false,
    val successMessage: String? = null
)

class NotificationPermissionDialogViewModel(
    private val notificationService: NotificationService,
    private val userPreferencesStorage: UserPreferencesStorage
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationPermissionDialogState())
    val state: StateFlow<NotificationPermissionDialogState> = _state.asStateFlow()

    init {
        checkNotificationPermission()
    }

    fun checkNotificationPermission() {
        viewModelScope.launch {
            _state.update { 
                it.copy(
                    isLoading = true, 
                    errorMessage = null,
                    showSuccessMessage = false,
                    successMessage = null
                )
            }
            
            try {
                val hasPermission = notificationService.areNotificationsEnabled()
                val userHasDecided = userPreferencesStorage.isNotificationPermissionDecided()
                
                _state.update { 
                    it.copy(
                        hasNotificationPermission = hasPermission,
                        userHasDecided = userHasDecided,
                        isVisible = !hasPermission && !userHasDecided,
                        isLoading = false
                    )
                }
                
                // Pokaż komunikat sukcesu jeśli użytkownik ma uprawnienia
                if (hasPermission) {
                    _state.update { 
                        it.copy(
                            showSuccessMessage = true,
                            successMessage = "✅ Powiadomienia są włączone! Otrzymasz przypomnienia o quizie emocji."
                        )
                    }
                    
                    // Ukryj komunikat sukcesu po 3 sekundach
                    kotlinx.coroutines.delay(3000)
                    _state.update { 
                        it.copy(
                            showSuccessMessage = false,
                            successMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "❌ Nie udało się sprawdzić uprawnień. Sprawdź połączenie internetowe i spróbuj ponownie."
                    )
                }
            }
        }
    }

    fun onEnableNotifications() {
        viewModelScope.launch {
            _state.update { 
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    showSuccessMessage = false,
                    successMessage = null
                )
            }
            
            try {
                val result = notificationService.requestNotificationPermissions()
                
                when (result) {
                    PermissionResult.GRANTED -> {
                        _state.update { 
                            it.copy(
                                hasNotificationPermission = true,
                                isVisible = false,
                                userHasDecided = true,
                                isLoading = false,
                                showSuccessMessage = true,
                                successMessage = "🎉 Świetnie! Powiadomienia zostały włączone. Otrzymasz przypomnienia o codziennym quizie emocji."
                            )
                        }
                        userPreferencesStorage.saveNotificationPermissionDecided( true)
                        userPreferencesStorage.saveNotificationPermissionGranted( true)
                        
                        // Ukryj komunikat sukcesu po 4 sekundach
                        kotlinx.coroutines.delay(4000)
                        _state.update { 
                            it.copy(
                                showSuccessMessage = false,
                                successMessage = null
                            )
                        }
                    }
                    PermissionResult.DENIED -> {
                        _state.update { 
                            it.copy(
                                hasNotificationPermission = false,
                                isVisible = false,
                                userHasDecided = true,
                                isLoading = false,
                                errorMessage = "⚠️ Uprawnienia zostały odrzucone. Możesz je włączyć później w ustawieniach aplikacji."
                            )
                        }
                        userPreferencesStorage.saveNotificationPermissionDecided( true)
                        userPreferencesStorage.saveNotificationPermissionGranted( false)
                    }
                    PermissionResult.PERMANENTLY_DENIED -> {
                        _state.update { 
                            it.copy(
                                hasNotificationPermission = false,
                                isVisible = false,
                                userHasDecided = true,
                                isLoading = false,
                                errorMessage = "🚫 Uprawnienia zostały trwale odrzucone. Aby je włączyć, przejdź do ustawień systemu."
                            )
                        }
                        userPreferencesStorage.saveNotificationPermissionDecided( true)
                        userPreferencesStorage.saveNotificationPermissionGranted( false)
                    }
                    PermissionResult.ERROR -> {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "❌ Wystąpił błąd podczas prośby o uprawnienia. Spróbuj ponownie lub włącz powiadomienia ręcznie w ustawieniach."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "❌ Błąd podczas prośby o uprawnienia: ${e.message}. Sprawdź połączenie internetowe i spróbuj ponownie."
                    )
                }
            }
        }
    }

    fun onDisableNotifications() {
        viewModelScope.launch {
            _state.update { 
                it.copy(
                    isVisible = false,
                    userHasDecided = true
                )
            }
            userPreferencesStorage.saveNotificationPermissionDecided(true)
            userPreferencesStorage.saveNotificationPermissionGranted( false)
        }
    }

    fun onOpenSettings() {
        viewModelScope.launch {
            try {
                notificationService.openAppSettings()
                _state.update { 
                    it.copy(
                        isVisible = false,
                        userHasDecided = true
                    )
                }
                userPreferencesStorage.saveNotificationPermissionDecided( true)
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        errorMessage = "Błąd podczas otwierania ustawień: ${e.message}"
                    )
                }
            }
        }
    }

    fun onDismiss() {
        _state.update { it.copy(isVisible = false) }
    }

    fun clearError() {
        _state.update { 
            it.copy(
                errorMessage = null,
                showSuccessMessage = false,
                successMessage = null
            )
        }
    }
    
    fun clearSuccessMessage() {
        _state.update { 
            it.copy(
                showSuccessMessage = false,
                successMessage = null
            )
        }
    }
}
