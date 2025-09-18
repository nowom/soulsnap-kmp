-- SoulSnaps Database Schema - Updated for Snap Storage
-- This file contains the SQL to set up the database tables and security policies

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS memories CASCADE;
DROP TABLE IF EXISTS user_profiles CASCADE;

-- Create memories table (updated for snap storage)
CREATE TABLE memories (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    mood_type TEXT CHECK (mood_type IN ('HAPPY', 'SAD', 'NEUTRAL', 'EXCITED', 'RELAXED')),
    photo_uri TEXT, -- Changed from photo_url to photo_uri for consistency
    audio_uri TEXT, -- Changed from audio_url to audio_uri for consistency
    image_url TEXT, -- Additional field for processed images
    location_lat DECIMAL(10, 8),
    location_lng DECIMAL(11, 8),
    location_name TEXT,
    affirmation TEXT,
    is_favorite BOOLEAN DEFAULT FALSE,
    is_synced BOOLEAN DEFAULT FALSE, -- Track sync status
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create index for faster queries
CREATE INDEX idx_memories_user_id ON memories(user_id);
CREATE INDEX idx_memories_created_at ON memories(created_at DESC);
CREATE INDEX idx_memories_is_synced ON memories(is_synced);
CREATE INDEX idx_memories_mood_type ON memories(mood_type);

-- Create user_profiles table for additional user information
CREATE TABLE user_profiles (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE UNIQUE,
    display_name TEXT,
    bio TEXT,
    avatar_url TEXT,
    preferences JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create index for user_profiles
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers to automatically update updated_at
CREATE TRIGGER update_memories_updated_at 
    BEFORE UPDATE ON memories 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_profiles_updated_at 
    BEFORE UPDATE ON user_profiles 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Enable Row Level Security (RLS)
ALTER TABLE memories ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_profiles ENABLE ROW LEVEL SECURITY;

-- Create policies for memories table
CREATE POLICY "Users can view their own memories" ON memories
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own memories" ON memories
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own memories" ON memories
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own memories" ON memories
    FOR DELETE USING (auth.uid() = user_id);

-- Create policies for user_profiles table
CREATE POLICY "Users can view their own profile" ON user_profiles
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own profile" ON user_profiles
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own profile" ON user_profiles
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own profile" ON user_profiles
    FOR DELETE USING (auth.uid() = user_id);

-- Create function to automatically create user profile on signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.user_profiles (user_id, display_name)
    VALUES (NEW.id, COALESCE(NEW.raw_user_meta_data->>'full_name', NEW.email));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Create trigger to automatically create user profile
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Grant necessary permissions
GRANT USAGE ON SCHEMA public TO anon, authenticated;
GRANT ALL ON ALL TABLES IN SCHEMA public TO anon, authenticated;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO anon, authenticated;
GRANT ALL ON ALL FUNCTIONS IN SCHEMA public TO anon, authenticated;

-- Create storage bucket for snap images
INSERT INTO storage.buckets (id, name, public) 
VALUES ('snap-images', 'snap-images', true)
ON CONFLICT (id) DO NOTHING;

-- Create storage bucket for snap audio
INSERT INTO storage.buckets (id, name, public) 
VALUES ('snap-audio', 'snap-audio', true)
ON CONFLICT (id) DO NOTHING;

-- Create storage policies for snap images
CREATE POLICY "Users can upload their own snap images" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'snap-images' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can view their own snap images" ON storage.objects
    FOR SELECT USING (
        bucket_id = 'snap-images' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can update their own snap images" ON storage.objects
    FOR UPDATE USING (
        bucket_id = 'snap-images' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can delete their own snap images" ON storage.objects
    FOR DELETE USING (
        bucket_id = 'snap-images' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

-- Create storage policies for snap audio
CREATE POLICY "Users can upload their own snap audio" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'snap-audio' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can view their own snap audio" ON storage.objects
    FOR SELECT USING (
        bucket_id = 'snap-audio' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can update their own snap audio" ON storage.objects
    FOR UPDATE USING (
        bucket_id = 'snap-audio' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can delete their own snap audio" ON storage.objects
    FOR DELETE USING (
        bucket_id = 'snap-audio' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

-- Create function to generate storage path for snap files
CREATE OR REPLACE FUNCTION generate_snap_storage_path(
    user_id UUID,
    file_type TEXT,
    file_extension TEXT
)
RETURNS TEXT AS $$
BEGIN
    RETURN user_id::text || '/' || file_type || '/' || uuid_generate_v4()::text || '.' || file_extension;
END;
$$ LANGUAGE plpgsql;

-- Create function to clean up orphaned files when memory is deleted
CREATE OR REPLACE FUNCTION cleanup_memory_files()
RETURNS TRIGGER AS $$
DECLARE
    photo_path TEXT;
    audio_path TEXT;
BEGIN
    -- Extract file paths from the deleted memory
    photo_path := OLD.photo_uri;
    audio_path := OLD.audio_uri;
    
    -- Delete photo file if it exists
    IF photo_path IS NOT NULL AND photo_path != '' THEN
        DELETE FROM storage.objects 
        WHERE bucket_id = 'snap-images' AND name = photo_path;
    END IF;
    
    -- Delete audio file if it exists
    IF audio_path IS NOT NULL AND audio_path != '' THEN
        DELETE FROM storage.objects 
        WHERE bucket_id = 'snap-audio' AND name = audio_path;
    END IF;
    
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to clean up files when memory is deleted
CREATE TRIGGER cleanup_memory_files_trigger
    AFTER DELETE ON memories
    FOR EACH ROW
    EXECUTE FUNCTION cleanup_memory_files();

-- Create view for memory statistics
CREATE VIEW memory_stats AS
SELECT 
    user_id,
    COUNT(*) as total_memories,
    COUNT(CASE WHEN is_favorite = true THEN 1 END) as favorite_memories,
    COUNT(CASE WHEN photo_uri IS NOT NULL THEN 1 END) as memories_with_photos,
    COUNT(CASE WHEN audio_uri IS NOT NULL THEN 1 END) as memories_with_audio,
    COUNT(CASE WHEN location_lat IS NOT NULL AND location_lng IS NOT NULL THEN 1 END) as memories_with_location,
    MIN(created_at) as first_memory_date,
    MAX(created_at) as last_memory_date
FROM memories
GROUP BY user_id;

-- Grant access to memory_stats view
GRANT SELECT ON memory_stats TO anon, authenticated;

-- Create indexes for better performance
CREATE INDEX idx_memories_photo_uri ON memories(photo_uri) WHERE photo_uri IS NOT NULL;
CREATE INDEX idx_memories_audio_uri ON memories(audio_uri) WHERE audio_uri IS NOT NULL;
CREATE INDEX idx_memories_location ON memories(location_lat, location_lng) WHERE location_lat IS NOT NULL AND location_lng IS NOT NULL;
CREATE INDEX idx_memories_is_favorite ON memories(is_favorite) WHERE is_favorite = true;

-- Create function to get memory by ID with proper authorization
CREATE OR REPLACE FUNCTION get_memory_by_id(memory_id UUID)
RETURNS TABLE (
    id UUID,
    user_id UUID,
    title TEXT,
    description TEXT,
    mood_type TEXT,
    photo_uri TEXT,
    audio_uri TEXT,
    image_url TEXT,
    location_lat DECIMAL(10, 8),
    location_lng DECIMAL(11, 8),
    location_name TEXT,
    affirmation TEXT,
    is_favorite BOOLEAN,
    is_synced BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        m.id,
        m.user_id,
        m.title,
        m.description,
        m.mood_type,
        m.photo_uri,
        m.audio_uri,
        m.image_url,
        m.location_lat,
        m.location_lng,
        m.location_name,
        m.affirmation,
        m.is_favorite,
        m.is_synced,
        m.created_at,
        m.updated_at
    FROM memories m
    WHERE m.id = memory_id 
    AND m.user_id = auth.uid();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant execute permission on the function
GRANT EXECUTE ON FUNCTION get_memory_by_id(UUID) TO anon, authenticated;
