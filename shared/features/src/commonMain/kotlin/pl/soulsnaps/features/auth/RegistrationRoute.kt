package pl.soulsnaps.features.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
data object RegistrationRoute

fun NavController.navigateToRegistration(navOptions: NavOptions? = null) =
    navigate(RegistrationRoute, navOptions)


fun NavGraphBuilder.registrationScreen(
    onRegistrationSuccess: () -> Unit,
) {
} 