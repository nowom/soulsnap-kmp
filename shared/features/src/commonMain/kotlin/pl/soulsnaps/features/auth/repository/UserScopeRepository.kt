package pl.soulsnaps.features.auth.repository

import pl.soulsnaps.features.auth.model.UserScope

/**
 * Repository interface for user scope management
 */
interface UserScopeRepository {
    
    /**
     * Get user scope by user ID
     */
    suspend fun getUserScope(userId: String): UserScope?
    
    /**
     * Create new user scope
     */
    suspend fun createUserScope(userScope: UserScope): Boolean
    
    /**
     * Update existing user scope
     */
    suspend fun updateUserScope(userScope: UserScope): Boolean
    
    /**
     * Delete user scope
     */
    suspend fun deleteUserScope(userId: String): Boolean
    
    /**
     * Check if user scope exists
     */
    suspend fun userScopeExists(userId: String): Boolean
    
    /**
     * Get all user scopes (for admin purposes)
     */
    suspend fun getAllUserScopes(): List<UserScope>
    
    /**
     * Get user scopes by role
     */
    suspend fun getUserScopesByRole(role: pl.soulsnaps.features.auth.model.UserRole): List<UserScope>
    
    /**
     * Get user scopes by subscription plan
     */
    suspend fun getUserScopesByPlan(plan: pl.soulsnaps.features.auth.model.SubscriptionPlan): List<UserScope>
    
    /**
     * Update user role
     */
    suspend fun updateUserRole(userId: String, newRole: pl.soulsnaps.features.auth.model.UserRole): Boolean
    
    /**
     * Update subscription plan
     */
    suspend fun updateSubscriptionPlan(userId: String, newPlan: pl.soulsnaps.features.auth.model.SubscriptionPlan): Boolean
    
    /**
     * Deactivate user scope
     */
    suspend fun deactivateUserScope(userId: String): Boolean
    
    /**
     * Reactivate user scope
     */
    suspend fun reactivateUserScope(userId: String): Boolean
}
