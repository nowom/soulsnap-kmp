# Session Persistence Fix

## Problem
The app was experiencing unnecessary logouts on app restart. The issue was in the session validation logic where:

1. User logs in successfully and session is saved to `PersistentSessionDataStore`
2. App restarts and during initialization:
   - Session is loaded from storage successfully
   - Supabase session validation shows `false` (Supabase doesn't automatically restore sessions)
   - The code tries to refresh the session but fails: "No refresh token found in current session"
   - Session gets cleared and user appears as not authenticated

## Root Cause
The session validation logic was too strict - it required both Supabase and our stored session to be valid. When Supabase doesn't have a session (common after app restart), it would immediately clear our stored session instead of trusting it.

## Solution
Modified the session validation logic in `UserSessionManagerImpl.validateAndRefreshSession()`:

### Before
- If Supabase session is invalid → try to refresh → if refresh fails → clear session
- This caused unnecessary logouts

### After
- If Supabase session is invalid but we have a stored session → validate stored session age
- If stored session is valid (within 7 days) → restore user session immediately
- Try background refresh to sync with Supabase (non-blocking)
- If background refresh fails, keep the stored session (user stays logged in)

## Key Changes

### 1. UserSessionManager.kt
- Added `isStoredSessionValid()` method to check session age
- Modified validation logic to trust stored sessions when Supabase session is missing
- Added background refresh attempt (non-blocking)
- Added configurable session validity duration (7 days by default)

### 2. SupabaseAuthService.kt
- Improved `refreshSession()` to check for existing session before attempting refresh
- Added placeholder `restoreSession()` method for future Supabase session restoration support

### 3. AuthConfig.kt
- Added `SESSION_VALIDITY_DAYS` configuration (7 days by default)

## Benefits
- ✅ Users stay logged in across app restarts
- ✅ Better user experience (no unnecessary logouts)
- ✅ Graceful handling of Supabase session restoration
- ✅ Configurable session validity duration
- ✅ Background sync with Supabase when possible

## Testing
To test the fix:
1. Log in to the app
2. Force close the app
3. Restart the app
4. User should remain logged in without having to log in again

## Future Improvements
- Implement proper Supabase session restoration when the Kotlin Multiplatform SDK supports it
- Add session refresh tokens validation
- Implement automatic session refresh before expiration
