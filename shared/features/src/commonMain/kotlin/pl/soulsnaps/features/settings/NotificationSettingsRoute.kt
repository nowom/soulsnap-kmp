package pl.soulsnaps.features.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object NotificationSettingsRoute

fun NavController.navigateToNotificationSettings(navOptions: NavOptions? = null) =
    navigate(NotificationSettingsRoute, navOptions)

fun NavGraphBuilder.notificationSettingsScreen(
    onBack: () -> Unit = {}
) {
    composable<NotificationSettingsRoute> {
        NotificationSettingsScreen(
            onBack = onBack
        )
    }
}

