package pl.soulsnaps.features.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import pl.soulsnaps.features.auth.mvp.guard.UserPlanManager

@Composable
fun OnboardingCompletionTracker(
    onComplete: () -> Unit
) {
    val userPlanManager = remember { UserPlanManager() }
    val hasCompletedOnboarding by userPlanManager.hasCompletedOnboarding.collectAsState(initial = false)
    val currentPlan by userPlanManager.currentPlan.collectAsState(initial = null)
    val isLoading by userPlanManager.isLoading.collectAsState(initial = true)
    
    LaunchedEffect(hasCompletedOnboarding, currentPlan, isLoading) {
        println("DEBUG: OnboardingCompletionTracker - state changed: hasCompletedOnboarding=$hasCompletedOnboarding, currentPlan=$currentPlan, isLoading=$isLoading")
        
        // Czekaj aż dane się załadują
        if (!isLoading) {
            if (hasCompletedOnboarding && currentPlan != null) {
                println("DEBUG: OnboardingCompletionTracker - onboarding completed, navigating to dashboard")
                onComplete()
            } else {
                println("DEBUG: OnboardingCompletionTracker - onboarding not completed, staying on onboarding")
            }
        } else {
            println("DEBUG: OnboardingCompletionTracker - still loading data...")
        }
    }
}
