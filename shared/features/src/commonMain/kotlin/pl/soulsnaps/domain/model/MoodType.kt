package pl.soulsnaps.domain.model

enum class MoodType {
    HAPPY, SAD, EXCITED, CALM, ANXIOUS, GRATEFUL, LOVED, STRESSED;
    
    /**
     * Get the database-compatible string value
     */
    val databaseValue: String
        get() = name.lowercase()
    
    /**
     * Create MoodType from database string value
     */
    companion object {
        fun fromDatabaseValue(value: String): MoodType? {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        
        /**
         * Validate if a mood value is supported
         */
        fun isValidMood(mood: String?): Boolean {
            return mood != null && fromDatabaseValue(mood) != null
        }
    }
}