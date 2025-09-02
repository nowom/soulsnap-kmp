package pl.soulsnaps.access.model

import kotlinx.serialization.Serializable

/**
 * AppFeature represents different features available in the app
 * Each feature can be restricted based on user's plan
 */
@Serializable
enum class AppFeature {
    // Memory Management
    MEMORY_CAPTURE,        // Basic memory capture (all plans)
    MEMORY_SYNC,           // Sync between devices (Free+)
    MAP_BASIC,             // Basic map view (all plans)
    MAP_ADVANCED,          // Advanced map with filters (Premium+)
    FILTERS_ADVANCED,      // Advanced filtering and tags (Premium+)
    
    // Export & Backup
    EXPORT_PDF,            // PDF export (all plans)
    EXPORT_VIDEO,          // Video export (Premium+)
    BACKUP_CLOUD,          // Cloud backup (Premium+)
    
    // Sharing & Collaboration
    SHARE_LINK,            // Share via link (Free+)
    AUDIO_ATTACH,          // Audio to snaps (Premium+)
    
    // AI Features
    AI_GENERATE,           // AI generation (Free+ with limits)
    AI_INSIGHTS,           // AI insights and patterns (Premium+)
    
    // Advanced Features
    TEAM_COLLABORATION,    // Team features (Enterprise+)
    ANALYTICS_DASHBOARD,   // Analytics dashboard (Enterprise+)
    API_ACCESS             // API access (Enterprise+)
}

