package pl.soulsnaps.features.auth.mvp.guard.model

import kotlinx.serialization.Serializable

/**
 * PlanType represents different subscription plans available in the app
 */
@Serializable
enum class PlanType {
    GUEST,           // Anonymous user with limited access
    FREE_USER,       // Free user with basic features and limits
    PREMIUM_USER,    // Premium user with advanced features
    ENTERPRISE_USER  // Enterprise user with unlimited access
}
