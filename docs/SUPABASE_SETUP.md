# Supabase Integration Setup Guide

This guide will help you set up Supabase to replace your server-side functionality.

## Prerequisites

1. Create a Supabase account at [supabase.com](https://supabase.com)
2. Create a new Supabase project

## Step 1: Get Your Supabase Credentials

1. Go to your Supabase project dashboard
2. Navigate to `Settings` > `API`
3. Copy your:
   - Project URL (looks like: `https://your-project-id.supabase.co`)
   - Anon public key (starts with `eyJ...`)

## Step 2: Update Your Configuration

1. Open `shared/src/commonMain/kotlin/org/drag/me/supabase/SupabaseConfig.kt`
2. Replace the placeholder values:

```kotlin
const val SUPABASE_URL = "https://your-project-id.supabase.co"
const val SUPABASE_ANON_KEY = "your-anon-key-here"
```

## Step 3: Create Database Table

Execute this SQL in your Supabase SQL editor:

```sql
-- Create the building_blocks table
CREATE TABLE building_blocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    color_hex TEXT NOT NULL CHECK (color_hex ~ '^#[0-9A-Fa-f]{6}$'),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create an updated_at trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_building_blocks_updated_at 
    BEFORE UPDATE ON building_blocks 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Enable Row Level Security (RLS)
ALTER TABLE building_blocks ENABLE ROW LEVEL SECURITY;

-- Create policies for public access (adjust based on your needs)
CREATE POLICY "Enable read access for all users" ON building_blocks
    FOR SELECT USING (true);

CREATE POLICY "Enable insert access for all users" ON building_blocks
    FOR INSERT WITH CHECK (true);

CREATE POLICY "Enable update access for all users" ON building_blocks
    FOR UPDATE USING (true);

CREATE POLICY "Enable delete access for non-default blocks" ON building_blocks
    FOR DELETE USING (is_default = false);
```

## Step 4: Initialize Default Data

In your app, call the initialization method once:

```kotlin
// In your app startup code
val apiClient = BuildingBlockApiClient()
val response = apiClient.initializeDefaultBlocks()
if (response.success) {
    println("Default blocks initialized!")
} else {
    println("Error: ${response.error}")
}
```

## Step 5: Update Your App Code

Replace your old HTTP API calls with the new Supabase client:

### Before (HTTP API):
```kotlin
// Old way using HTTP
val httpClient = HttpClient()
val response = httpClient.get("$BASE_URL/api/building-blocks")
```

### After (Supabase):
```kotlin
// New way using Supabase
val apiClient = BuildingBlockApiClient()
val response = apiClient.getAllBlocks()
```

## Step 6: Remove Server Dependencies

You can now:

1. **Remove the server module** - it's no longer needed
2. **Update your deployment** - no need to deploy a backend server
3. **Remove server-related configuration** from your build files

## Security Considerations

1. **Row Level Security (RLS)**: The example above enables public access. For production, consider implementing proper authentication and authorization.

2. **API Key Security**: The anon key is public and safe to use in client applications. For admin operations, you might want to use the service role key server-side only.

3. **Rate Limiting**: Supabase provides built-in rate limiting, but consider implementing additional client-side throttling for better UX.

## Advantages of Using Supabase

1. **No Server Maintenance**: No need to maintain, deploy, or scale servers
2. **Real-time Updates**: Built-in real-time subscriptions for live data updates
3. **Automatic Scaling**: Supabase handles scaling automatically
4. **Built-in Authentication**: Easy to add user authentication later
5. **Database Backups**: Automatic backups and point-in-time recovery
6. **Admin Dashboard**: Built-in admin interface for data management

## Migration Checklist

- [ ] Create Supabase project
- [ ] Update SupabaseConfig.kt with your credentials
- [ ] Create database table using provided SQL
- [ ] Test connection by running initialization
- [ ] Update all API calls in your app
- [ ] Remove server module and dependencies
- [ ] Update deployment configuration
- [ ] Test all functionality on all platforms (Android, iOS, Desktop, Web)

## Troubleshooting

### Common Issues:

1. **Connection Failed**: Check your URL and API key
2. **CORS Errors**: Make sure your domain is added to Supabase allowed origins
3. **Permission Errors**: Verify your RLS policies are correct
4. **SSL Issues**: Ensure you're using HTTPS URLs

### Platform-Specific Notes:

- **iOS**: No additional configuration needed
- **Android**: Ensure network security config allows HTTPS
- **Desktop**: No additional configuration needed  
- **Web**: Supabase works great with WASM/JS targets

## Next Steps

1. Consider adding user authentication with Supabase Auth
2. Implement real-time updates using Supabase Realtime
3. Add file storage capabilities with Supabase Storage
4. Set up proper RLS policies for multi-user scenarios
