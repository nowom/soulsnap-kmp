package pl.soulsnaps.features.upgrade

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object UpgradeRoute

fun NavController.navigateToUpgrade(navOptions: NavOptions? = null) =
    navigate(UpgradeRoute, navOptions)

fun NavGraphBuilder.upgradeScreen(
    onBack: () -> Unit,
    onUpgradeToPlan: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    composable<UpgradeRoute> {
        val viewModel: UpgradeViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        UpgradeScreen(
            onBack = onBack,
            onUpgradeToPlan = { planName ->
                viewModel.handleIntent(UpgradeIntent.UpgradeToPlan(planName))
                onUpgradeToPlan(planName)
            },
            onDismiss = onDismiss,
            currentPlan = state.currentPlan,
            recommendations = state.recommendations,
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            onRetry = {
                viewModel.handleIntent(UpgradeIntent.LoadData)
            },
            onClearError = {
                viewModel.handleIntent(UpgradeIntent.ClearError)
            }
        )
    }
}

