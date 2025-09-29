# Firebase Analytics Integration

## Overview
This module provides Firebase Analytics integration for SoulSnaps app with Kotlin Multiplatform support.

## Features
- ✅ **User Behavior Tracking** - Track user actions, screen views, and feature usage
- ✅ **Performance Metrics** - Monitor app performance and user engagement
- ✅ **Custom Events** - Log custom events with parameters
- ✅ **User Properties** - Set user-specific properties for segmentation
- ✅ **Purchase Tracking** - Track in-app purchases and transactions
- ✅ **Error Tracking** - Log errors and exceptions
- ✅ **Cross-Platform** - Works on both Android and iOS

## Usage

### Basic Event Tracking
```kotlin
// Inject AnalyticsManager
val analyticsManager: AnalyticsManager by inject()

// Track screen views
analyticsManager.trackScreenView("home_screen")

// Track feature usage
analyticsManager.trackFeatureUsage("memory_capture")

// Track user actions
analyticsManager.trackEvent(AnalyticsEvent.UserAction("button_clicked", "home_screen"))
```

### Onboarding Analytics
```kotlin
// Start onboarding
analyticsManager.startOnboarding()

// Track step completion
analyticsManager.startStep("welcome")
analyticsManager.completeStep("welcome")

// Complete onboarding
analyticsManager.completeOnboarding("mindfulness", "email")
```

### Authentication Analytics
```kotlin
// Track login attempts
analyticsManager.trackAuthAttempt("email", true)
analyticsManager.trackAuthAttempt("google", false)

// Set user properties
analyticsManager.setUserProperties("user123", mapOf(
    "subscription_plan" to "premium",
    "user_type" to "returning"
))
```

### Custom Events
```kotlin
// Track emotion capture
analyticsManager.trackEmotionCapture("happy", "high")

// Track errors
analyticsManager.trackError("Network timeout", "memory_save")

// Track performance
analyticsManager.trackEvent(AnalyticsEvent.PerformanceMetric("load_time", 1500))
```

### Firebase Analytics Direct Access
```kotlin
// Inject FirebaseAnalyticsManager
val firebaseAnalytics: FirebaseAnalyticsManager by inject()

// Log custom events
firebaseAnalytics.logEvent("custom_event", mapOf(
    "parameter1" to "value1",
    "parameter2" to 42
))

// Log purchases
firebaseAnalytics.logPurchase(
    transactionId = "txn_123",
    value = 9.99,
    currency = "USD",
    items = listOf(
        PurchaseItem("premium_monthly", "Premium Monthly", "subscription", 1, 9.99)
    )
)

// Log searches
firebaseAnalytics.logSearch("meditation", "exercises")

// Log shares
firebaseAnalytics.logShare("memory", "memory_123")
```

## Events Tracked

### Automatic Events
- **Screen Views** - Every screen navigation
- **App Opens** - App launch events
- **First Open** - First app launch
- **User Authentication** - Login/logout events
- **Feature Usage** - When features are used
- **Errors** - Application errors and exceptions

### Custom Events
- **onboarding_started** - When user starts onboarding
- **onboarding_step_completed** - When user completes onboarding step
- **onboarding_completed** - When user finishes onboarding
- **emotion_captured** - When user captures emotion
- **memory_saved** - When user saves memory
- **feature_used** - When user uses specific feature
- **user_action** - Generic user action
- **performance_metric** - Performance measurements
- **error_occurred** - Application errors

## User Properties
- **user_id** - Unique user identifier
- **subscription_plan** - User's subscription plan
- **user_type** - Type of user (new, returning, premium)
- **onboarding_completed** - Whether user completed onboarding
- **preferred_language** - User's language preference

## Firebase Console
View analytics data in Firebase Console:
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `soul-snap`
3. Navigate to **Analytics** section
4. View **Events**, **User Properties**, and **Audiences**

## Platform Support
- **Android** ✅ Full Firebase Analytics integration
- **iOS** ⚠️ No-op implementation (ready for future iOS Firebase integration)

## Configuration
Analytics is automatically configured when the app starts:
- Firebase is initialized in `SoulSnapsApplication.onCreate()`
- Analytics collection is enabled by default
- User properties are set automatically
- Events are logged in real-time

## Privacy & GDPR
- Analytics collection can be disabled: `firebaseAnalytics.setAnalyticsCollectionEnabled(false)`
- User data can be reset: `firebaseAnalytics.resetAnalyticsData()`
- User properties are anonymized by default
- No personal data is collected without consent

## Testing
```kotlin
// Test analytics in development
analyticsManager.testCrash() // Triggers test crash for Crashlytics

// Check analytics summary
val summary = analyticsManager.getAnalyticsSummary()
println(summary)
```

## Best Practices
1. **Event Naming** - Use snake_case for event names
2. **Parameter Limits** - Max 25 parameters per event
3. **Value Limits** - String values max 100 characters
4. **Event Limits** - Max 500 unique event names
5. **User Properties** - Max 25 user properties
6. **Privacy** - Always respect user privacy settings
7. **Testing** - Test analytics in development builds
8. **Monitoring** - Monitor analytics data regularly

## Troubleshooting
- **Events not appearing** - Check Firebase Console, events may take 24h to appear
- **Missing data** - Ensure Firebase is properly initialized
- **iOS issues** - iOS uses no-op implementation, no data will be sent
- **Debug mode** - Use Firebase DebugView for real-time event monitoring

