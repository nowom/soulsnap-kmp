MVP (Minimum Viable Product) – SoulFlow (Final Version)
🎯 MVP Goal
To enable the user to record their emotions and moments in a multimedia journal with a map feature, receive mood-matched affirmations, and build basic emotional self-awareness.

📱 Main Tabs in MVP (Bottom Navigation Bar)
🏠 Start / Dashboard: Navigation dashboard with the affirmation of the day, emotional summary, and quick access to key functions.

📸 SoulSnaps (Journal): Timeline, emotional memories gallery, and a memories map.

🎧 Affirmations: List of audio affirmations, favorites, thematic filters, and AI-generated content.

🧠 Coach / AI Trainer (Light Version): Daily emotional quiz and basic educational tools.

🧩 Detailed MVP Features
1. 🏠 Start / Dashboard
Affirmation of the day: A short affirmation text with a playback option (AI voice).

Last SoulSnap: Thumbnail with a preview of the most recently added SoulSnap.

Emotion of the day: Simple representation of the current emotion (emoji + description).

+ Add Snap button: Quick access to the SoulSnap creation screen.

2. 📸 SoulSnaps / Journal
Adding a Snap: Ability to add a photo, a short description, select an emotion (e.g., from an emoji/text list), and an optional location (geotagging).

Snap Timeline: A chronological list of added Snaps.

Snap Gallery: A grid view of photos with associated emotion emojis.

Memories Map: An interactive map with pins for Snap locations (Mapbox SDK), allowing for a geographical review of memories. Supports basic offline map functionality.

Snap Preview: Full entry with description, photo, emotion, and location.

Edit and Delete Snap: Basic entry management functions.

Offline mode: Ability to add Snaps without internet access, with later synchronization.

AI reflection: A simple function to generate an affirmation or a short reflection based on the Snap's description (using a simple AI prompt).

3. 🎧 Affirmations
Affirmation List: Access to a predefined database of affirmations (text + AI-generated audio).

Affirmation Playback: Ability to listen to affirmations in an AI voice (ElevenLabs).

Emotion Filters: Basic filtering of affirmations by associated emotions.

User's Voice (Basic): Ability to play affirmations in the user's previously recorded voice (during onboarding).

4. 🧠 Coach / AI Trainer (Light Version)
"How do you feel today?" Quiz: A daily, short quiz, after which a personalized affirmation and a simple AI-generated reflection are provided.

Plutchik's Wheel of Emotions: A simple, static version with the ability to click on an emotion and view its basic description.

Two-three Basic Relaxation Techniques: Short text or audio instructions for simple exercises, e.g., 4-7-8 breathing, gratitude exercise.

🔧 Key Technologies in MVP (Updated)
UI: Jetpack Compose Multiplatform (for a common UI codebase on Android and iOS).

Database: SQLDelight (for local data storage on Android and iOS, and in the common module for schema and query definitions); Firebase Firestore for cloud data synchronization.

TTS (Text-to-Speech): ElevenLabs (for AI voice), native TTS APIs of each platform (for user's voice).

AI: OpenAI GPT (for generating affirmations and reflections).

Audio: ExoPlayer (Android), AVFoundation (iOS), or multiplatform audio libraries for affirmation playback.

Maps: Mapbox SDK (for implementing the memories map on Android and iOS).

💰 MVP Monetization
The MVP version will be free, serving for testing, market validation, and gathering user feedback. Premium features will be introduced in later development phases.

🗓️ Estimated MVP Timeline
Including the map in the MVP will increase complexity and potential development time. A precise estimate would require a detailed analysis of Mapbox SDK implementation in the context of Compose Multiplatform and offline mode, but it will certainly extend the initially estimated 2-4 weeks.

🧪 MVP Success Metrics
User Retention: Do users return to the app and use SoulSnaps?

Affirmation Engagement: Are voice affirmations used by users?

AI Accuracy: Are AI quizzes and reflections perceived as accurate and helpful?

Emotional Value: Does the combination of SoulSnaps and affirmations generate emotional value for the user?

Multiplatform Consistency: Is the user experience and functionality consistent on Android and iOS?

Map Usability: Do users actively use the memories map and find it a valuable feature?