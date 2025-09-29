# Firebase Crashlytics Integration

## Overview

This module provides a cross-platform interface for Firebase Crashlytics integration in the SoulSnaps application. It allows for crash reporting, error tracking, and user analytics across Android and iOS platforms.

## Architecture

The implementation follows the expect/actual pattern for Kotlin Multiplatform:

- **Common**: `CrashlyticsManager` interface and `CrashlyticsManagerFactory`
- **Android**: `AndroidCrashlyticsManager` using Firebase Crashlytics
- **iOS**: `IosCrashlyticsManager` (no-op implementation for now)

## Features

### Crash Reporting
- Automatic crash detection and reporting
- Custom exception logging
- User context tracking

### Error Tracking
- Non-fatal error logging
- Custom key-value pairs for debugging
- User session tracking

### Analytics Integration
- User identification
- Custom event logging
- Performance metrics

## Usage

### Basic Setup

The `CrashlyticsManager` is automatically injected via Koin DI:

```kotlin
class MyViewModel(
    private val crashlyticsManager: CrashlyticsManager
) : ViewModel() {
    
    fun handleError(error: Throwable) {
        crashlyticsManager.recordException(error)
        crashlyticsManager.log("Error occurred in MyViewModel")
    }
}
```

### Error Logging

```kotlin
try {
    // Risky operation
    riskyOperation()
} catch (e: Exception) {
    // Log to Crashlytics
    crashlyticsManager.recordException(e)
    crashlyticsManager.log("Error in risky operation: ${e.message}")
    crashlyticsManager.setCustomKey("operation_type", "risky")
    crashlyticsManager.setCustomKey("user_id", userId)
}
```

### User Tracking

```kotlin
// Set user ID for crash reports
crashlyticsManager.setUserId("user123")

// Set custom user properties
crashlyticsManager.setCustomKey("user_plan", "premium")
crashlyticsManager.setCustomKey("app_version", "1.0.0")
crashlyticsManager.setCustomKey("device_type", "android")
```

### Custom Logging

```kotlin
// Log custom messages
crashlyticsManager.log("User completed onboarding")
crashlyticsManager.log("Memory saved successfully")

// Log with context
crashlyticsManager.log("Error saving memory: ${error.message}")
```

## Configuration

### Android

1. **Firebase Project Setup**:
   - Create Firebase project
   - Add Android app to project
   - Download `google-services.json`
   - Place in `composeApp/` directory

2. **Dependencies**:
   - Firebase Crashlytics plugin
   - Google Services plugin
   - Firebase BOM for version management

3. **Initialization**:
   - Firebase is initialized in `SoulSnapsApplication`
   - Crashlytics is configured via `AndroidCrashlyticsManager`

### iOS

Currently uses no-op implementation. To enable iOS crash reporting:

1. Add Firebase iOS SDK
2. Implement `IosCrashlyticsManager` with Firebase iOS
3. Configure Firebase in iOS app

## Best Practices

### Error Handling

1. **Always log exceptions**:
   ```kotlin
   try {
       // Operation
   } catch (e: Exception) {
       crashlyticsManager.recordException(e)
       // Handle error
   }
   ```

2. **Add context**:
   ```kotlin
   crashlyticsManager.setCustomKey("screen", "CaptureMoment")
   crashlyticsManager.setCustomKey("user_action", "save_memory")
   ```

3. **Don't log sensitive data**:
   ```kotlin
   // ❌ Don't do this
   crashlyticsManager.setCustomKey("password", userPassword)
   
   // ✅ Do this instead
   crashlyticsManager.setCustomKey("has_password", userPassword.isNotEmpty())
   ```

### Performance

1. **Use async logging**:
   - Crashlytics operations are non-blocking
   - Don't wrap in try-catch blocks

2. **Limit custom keys**:
   - Too many keys can impact performance
   - Use meaningful, concise keys

### Privacy

1. **Respect user privacy**:
   - Don't log personal information
   - Use hashed or anonymized data

2. **GDPR compliance**:
   - Allow users to opt-out
   - Provide data deletion options

## Testing

### Test Crash

For development testing:

```kotlin
// This will cause a test crash
crashlyticsManager.testCrash()
```

### Debug Mode

In debug builds, Crashlytics is enabled but may not send data immediately. Check Firebase Console for crash reports.

## Monitoring

### Firebase Console

1. Go to Firebase Console
2. Select your project
3. Navigate to Crashlytics
4. View crash reports and analytics

### Key Metrics

- Crash-free users
- Crash-free sessions
- Top crashes
- User impact

## Troubleshooting

### Common Issues

1. **Crashes not appearing**:
   - Check Firebase configuration
   - Verify `google-services.json` is correct
   - Ensure app is signed for release

2. **Custom keys not showing**:
   - Keys are only visible in crash reports
   - Use `log()` for general debugging

3. **iOS not working**:
   - iOS implementation is currently no-op
   - Implement Firebase iOS SDK for full support

### Debug Logging

Enable debug logging in development:

```kotlin
crashlyticsManager.setCrashlyticsCollectionEnabled(true)
crashlyticsManager.log("Debug: Crashlytics enabled")
```

## Future Enhancements

1. **iOS Support**: Full Firebase iOS integration
2. **Custom Dashboards**: Build custom analytics dashboards
3. **A/B Testing**: Integrate with Firebase Remote Config
4. **Performance Monitoring**: Add Firebase Performance Monitoring
5. **User Feedback**: Integrate with Firebase In-App Messaging

## Security Considerations

1. **API Keys**: Never commit `google-services.json` to version control
2. **Data Privacy**: Ensure compliance with local privacy laws
3. **Access Control**: Limit Firebase Console access to authorized personnel
4. **Data Retention**: Configure appropriate data retention policies

## Support

For issues or questions:

1. Check Firebase documentation
2. Review crash reports in Firebase Console
3. Contact development team
4. Check project issues on GitHub

