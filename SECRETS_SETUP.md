# 🔐 Secure Secrets Management in Kotlin Multiplatform

This project implements secure secrets management using the expect/actual pattern as described in the [Medium article](https://medium.com/@mohaberabi98/managing-app-secrets-in-kotlin-multiplatform-app-374aa0f693a5).

## 📁 File Structure

```
shared/features/src/
├── commonMain/kotlin/pl/soulsnaps/config/
│   └── Secrets.kt                    # expect object
├── androidMain/kotlin/pl/soulsnaps/config/
│   └── Secrets.android.kt            # Android actual implementation
└── iosMain/kotlin/pl/soulsnaps/config/
    └── Secrets.ios.kt                # iOS actual implementation
```

## 🔧 Implementation

### 1. Common Interface (expect)

```kotlin
// shared/features/src/commonMain/kotlin/pl/soulsnaps/config/Secrets.kt
expect object Secrets {
    val SUPABASE_URL: String
    val SUPABASE_ANON_KEY: String
}
```

### 2. Android Implementation

```kotlin
// shared/features/src/androidMain/kotlin/pl/soulsnaps/config/Secrets.android.kt
actual object Secrets {
    actual val SUPABASE_URL: String = pl.soulsnaps.features.BuildConfig.SUPABASE_URL
    actual val SUPABASE_ANON_KEY: String = pl.soulsnaps.features.BuildConfig.SUPABASE_ANON_KEY
}
```

### 3. iOS Implementation

```kotlin
// shared/features/src/iosMain/kotlin/pl/soulsnaps/config/Secrets.ios.kt
actual object Secrets {
    actual val SUPABASE_URL: String = "https://your-project.supabase.co"
    actual val SUPABASE_ANON_KEY: String = "eyJ..."
}
```

### 4. Gradle Configuration

```kotlin
// shared/features/build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"${project.findProperty("SUPABASE_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${project.findProperty("SUPABASE_ANON_KEY") ?: ""}\"")
    }
    
    buildFeatures {
        buildConfig = true
    }
}
```

### 5. Usage in AuthConfig

```kotlin
// shared/features/src/commonMain/kotlin/pl/soulsnaps/config/AuthConfig.kt
object AuthConfig {
    val SUPABASE_URL: String = Secrets.SUPABASE_URL
    val SUPABASE_ANON_KEY: String = Secrets.SUPABASE_ANON_KEY
}
```

## 🔒 Security Features

### ✅ **Android Security**
- Secrets stored in `gradle.properties` (not committed to git)
- BuildConfig generates compile-time constants
- No runtime access to raw secrets

### ✅ **iOS Security**
- Secrets can be stored in Info.plist
- Environment variables support
- Build-time configuration

### ✅ **Git Security**
- `gradle.properties` is in `.gitignore`
- No secrets in source code
- Platform-specific implementations

## 🚀 Setup Instructions

### 1. Add Secrets to gradle.properties

```properties
# gradle.properties (not committed to git)
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. Ensure gradle.properties is in .gitignore

```gitignore
# .gitignore
gradle.properties
```

### 3. For iOS (Optional Enhancement)

You can enhance the iOS implementation to use environment variables:

```kotlin
// shared/features/src/iosMain/kotlin/pl/soulsnaps/config/Secrets.ios.kt
actual object Secrets {
    actual val SUPABASE_URL: String = System.getenv("SUPABASE_URL") 
        ?: "https://your-project.supabase.co"
    
    actual val SUPABASE_ANON_KEY: String = System.getenv("SUPABASE_ANON_KEY")
        ?: "eyJ..."
}
```

## 🧪 Testing

The implementation works with existing tests:

```bash
./gradlew :shared:features:testDebugUnitTest --tests "pl.soulsnaps.access.guard.AccessGuardTest"
```

## 🔄 Migration from Previous Approach

This replaces the previous hardcoded approach with a secure, platform-specific implementation that:

1. **Separates concerns** - Platform-specific secret management
2. **Improves security** - No secrets in source code
3. **Maintains compatibility** - Same API for AuthConfig
4. **Supports CI/CD** - Environment variables for production

## 📚 References

- [Medium Article: Managing App Secrets in Kotlin Multiplatform](https://medium.com/@mohaberabi98/managing-app-secrets-in-kotlin-multiplatform-app-374aa0f693a5)
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Android BuildConfig Documentation](https://developer.android.com/studio/build/gradle-tips#use-buildconfig-with-custom-fields)

