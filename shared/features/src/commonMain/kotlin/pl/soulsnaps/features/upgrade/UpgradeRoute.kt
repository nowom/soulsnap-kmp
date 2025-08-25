package pl.soulsnaps.features.upgrade

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object UpgradeRoute

fun NavController.navigateToUpgrade(navOptions: NavOptions? = null) =
    navigate(UpgradeRoute, navOptions)

fun NavGraphBuilder.upgradeScreen(
    onBack: () -> Unit,
    onUpgradeToPlan: (String) -> Unit,
    onDismiss: () -> Unit,
    currentPlan: String,
    recommendations: List<UpgradeRecommendation> = emptyList()
) {
    composable<UpgradeRoute> {
        UpgradeScreen(
            onBack = onBack,
            onUpgradeToPlan = onUpgradeToPlan,
            onDismiss = onDismiss,
            currentPlan = currentPlan,
            recommendations = recommendations
        )
    }
}

