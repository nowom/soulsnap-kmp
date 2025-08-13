# Supabase Authentication Setup Guide

This guide will help you set up real Supabase authentication in your SoulSnaps app.

## 🚀 Quick Start

### 1. Create a Supabase Project

1. Go to [supabase.com](https://supabase.com) and sign up/login
2. Click "New Project"
3. Choose your organization
4. Enter project details:
   - **Name**: `soulsnaps` (or your preferred name)
   - **Database Password**: Choose a strong password
   - **Region**: Select the region closest to your users
5. Click "Create new project"

### 2. Get Your Project Credentials

1. In your Supabase dashboard, go to **Settings** → **API**
2. Copy the following values:
   - **Project URL** (e.g., `https://your-project-id.supabase.co`)
   - **anon public** key (starts with `eyJ...`)

### 3. Update Configuration

1. Open `shared/features/src/commonMain/kotlin/pl/soulsnaps/config/AuthConfig.kt`
2. Replace the placeholder values:

```kotlin
object AuthConfig {
    const val USE_SUPABASE_AUTH = true
    
    const val SUPABASE_URL = "https://your-project-id.supabase.co" // Your actual project URL
    const val SUPABASE_ANON_KEY = "eyJ..." // Your actual anon key
    
    const val REQUIRE_EMAIL_CONFIRMATION = false
}
```

### 4. Configure Authentication Settings

1. In your Supabase dashboard, go to **Authentication** → **Settings**
2. Configure the following:

#### Email Authentication
- **Enable email confirmations**: Set to `false` for development (or `true` for production)
- **Secure email change**: Set to `true`
- **Double confirm changes**: Set to `true`

#### Site URL
- Add your app's URL (for development, you can use `http://localhost:8080`)

#### Redirect URLs
- Add your app's redirect URLs (for development, you can use `http://localhost:8080/**`)

### 5. Test the Integration

1. Build and run your app
2. Try to register a new user
3. Try to log in with the registered user
4. Try the "Continue as Guest" option

## 🔧 Advanced Configuration

### Email Confirmation

If you want to require email confirmation:

1. Set `REQUIRE_EMAIL_CONFIRMATION = true` in `AuthConfig.kt`
2. In Supabase dashboard, go to **Authentication** → **Settings**
3. Enable "Enable email confirmations"
4. Configure your email templates in **Authentication** → **Email Templates**

### Social Authentication

To add Google, Apple, or other social providers:

1. In Supabase dashboard, go to **Authentication** → **Providers**
2. Enable and configure your desired providers
3. Update the `SupabaseAuthService.kt` to handle social authentication

### Custom User Metadata

To store additional user information:

1. Update the `SupabaseAuthService.kt` to include user metadata in registration
2. The metadata will be stored in Supabase's `auth.users` table

## 🏗️ Architecture Overview

### Current Implementation

The app uses a layered architecture:

```
UI Layer (LoginScreen, RegistrationScreen)
    ↓
ViewModel Layer (LoginViewModel, RegistrationViewModel)
    ↓
Use Case Layer (SignInUseCase, RegisterUseCase)
    ↓
Repository Layer (SupabaseAuthRepository)
    ↓
Service Layer (SupabaseAuthService)
    ↓
Network Layer (Ktor HTTP Client)
    ↓
Supabase API
```

### Key Components

1. **AuthConfig.kt**: Central configuration for authentication settings
2. **SupabaseAuthService.kt**: HTTP client for communicating with Supabase
3. **SupabaseAuthRepository.kt**: Repository implementation using Supabase
4. **DataModule.kt**: Dependency injection configuration

## 🔄 Switching Between Fake and Real Auth

To switch between fake and real authentication:

1. **Use Fake Auth**: Set `USE_SUPABASE_AUTH = false` in `AuthConfig.kt`
2. **Use Real Supabase Auth**: Set `USE_SUPABASE_AUTH = true` in `AuthConfig.kt`

The app will automatically use the appropriate implementation without any code changes.

## 🚨 Security Considerations

### Production Checklist

- [ ] Use HTTPS for all API calls
- [ ] Enable email confirmation for production
- [ ] Configure proper redirect URLs
- [ ] Set up Row Level Security (RLS) policies
- [ ] Use environment variables for sensitive data
- [ ] Enable audit logging
- [ ] Configure rate limiting

### Environment Variables

For production, consider using environment variables instead of hardcoded values:

```kotlin
// In a real app, you'd load these from environment variables
const val SUPABASE_URL = System.getenv("SUPABASE_URL") ?: "https://your-project.supabase.co"
const val SUPABASE_ANON_KEY = System.getenv("SUPABASE_ANON_KEY") ?: "your-anon-key"
```

## 🐛 Troubleshooting

### Common Issues

1. **"Invalid API key" error**
   - Check that your anon key is correct
   - Ensure you're using the `anon public` key, not the `service_role` key

2. **"Email confirmation required" error**
   - Check your email confirmation settings in Supabase
   - Verify your email templates are configured

3. **Network errors**
   - Check your internet connection
   - Verify the Supabase URL is correct
   - Check if Supabase is experiencing downtime

4. **Build errors**
   - Clean and rebuild the project
   - Check that all dependencies are properly configured

### Debug Mode

To enable debug logging, you can add logging to the `SupabaseAuthService.kt`:

```kotlin
suspend fun signIn(email: String, password: String): UserSession {
    println("DEBUG: Attempting to sign in user: $email")
    // ... rest of the implementation
}
```

## 📚 Next Steps

Once basic authentication is working, consider adding:

1. **Password Reset**: Implement password reset functionality
2. **Profile Management**: Allow users to update their profiles
3. **Session Management**: Implement proper session handling
4. **Database Integration**: Connect to Supabase's PostgreSQL database
5. **Real-time Features**: Use Supabase's real-time subscriptions
6. **File Storage**: Use Supabase Storage for photos and audio

## 📞 Support

If you encounter issues:

1. Check the [Supabase Documentation](https://supabase.com/docs)
2. Visit the [Supabase Community](https://github.com/supabase/supabase/discussions)
3. Check the [Supabase Status Page](https://status.supabase.com)

## 🎉 Congratulations!

You've successfully integrated Supabase authentication into your SoulSnaps app! The authentication system is now production-ready and can scale with your app's needs.
