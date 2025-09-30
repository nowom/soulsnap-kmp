package pl.soulsnaps

import dev.mokkery.MockMode
import dev.mokkery.MokkeryCompilerDefaults
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.flow.flowOf
import pl.soulsnaps.access.guard.AccessGuard
import pl.soulsnaps.domain.StartupRepository
import pl.soulsnaps.access.manager.OnboardingManager
import pl.soulsnaps.access.manager.UserPlanManager
import pl.soulsnaps.audio.AudioManager
import pl.soulsnaps.domain.interactor.GenerateAffirmationUseCase
import pl.soulsnaps.domain.interactor.SaveMemoryUseCase
import pl.soulsnaps.features.analytics.AnalyticsManager
import pl.soulsnaps.network.SupabaseAuthService

val accessGuard = AccessGuard(mock(), mock(), mock(), mock())
val audioManager = AudioManager(mock(), mock())
val generateAffirmationUseCase = GenerateAffirmationUseCase(mock(), mock())
val saveMemoryUseCase = SaveMemoryUseCase(mock(), mock(), generateAffirmationUseCase)
val analyticsManager = AnalyticsManager(mock(), mock(), mock())
val onboardingManager = OnboardingManager(mock())

val startupRepository = mock<StartupRepository>()


fun createMockUserPlanManager(): UserPlanManager =
    mock(mode = MockMode.autoUnit) {
        // property
        every { currentPlan } returns flowOf<String?>(null)

        // metody zwracające wartości
        every { getUserPlan() } returns null
        every { isOnboardingCompleted() } returns false
        every { getPlanOrDefault() } returns "GUEST"
        every { hasPlanSet() } returns false
        every { getCurrentPlan() } returns null

        // metody Unit (opcjonalnie – relaxed i tak zrobi no-op)
        every { setUserPlan(any()) } calls { /* no-op */ }
        every { resetUserPlan() } calls { /* no-op */ }
        every { setDefaultPlanIfNeeded() } calls { /* no-op */ }

        // suspend Unit (relaxed = true → no-op, ale możesz jawnie)
        everySuspend { setUserPlanAndWait(any()) } calls { /* no-op */ }
        everySuspend { waitForInitialization() } calls { /* no-op */ }
    }