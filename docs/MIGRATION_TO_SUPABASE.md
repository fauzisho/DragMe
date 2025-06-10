# Migration from Server to Supabase

This document outlines the changes made to migrate from a server-based architecture to Supabase.

## What Changed

### ✅ Added Dependencies
- `supabase-postgrest-kt` - Database operations
- `supabase-gotrue-kt` - Authentication (ready for future use)
- `supabase-realtime-kt` - Real-time subscriptions
- `uuid` - For generating unique IDs

### ✅ New Files Created
- `shared/src/commonMain/kotlin/org/drag/me/supabase/SupabaseConfig.kt` - Supabase configuration
- `shared/src/commonMain/kotlin/org/drag/me/models/SupabaseModels.kt` - Database entity models
- `shared/src/commonMain/kotlin/org/drag/me/repository/BuildingBlockRepository.kt` - Data access layer
- `shared/src/commonMain/kotlin/org/drag/me/service/SupabaseBuildingBlockService.kt` - Business logic layer
- `shared/src/commonMain/kotlin/org/drag/me/api/BuildingBlockApiClient.kt` - API client replacement
- `shared/src/commonMain/kotlin/org/drag/me/examples/SupabaseUsageExample.kt` - Usage examples

### ✅ Modified Files
- `gradle/libs.versions.toml` - Added Supabase dependencies
- `shared/build.gradle.kts` - Added Supabase to shared module
- `composeApp/build.gradle.kts` - Removed Ktor client dependencies
- `shared/src/commonMain/kotlin/org/drag/me/api/ApiEndpoints.kt` - Updated for Supabase

### 🔄 Can Be Removed (Optional)
- `server/` directory - Entire server module
- All Ktor server dependencies
- Railway deployment configuration
- Docker configuration (if only used for server)

## API Mapping

### Old HTTP API → New Supabase API

| Old HTTP Endpoint | New Supabase Method |
|------------------|-------------------|
| `GET /api/building-blocks` | `apiClient.getAllBlocks()` |
| `POST /api/building-blocks` | `apiClient.createBlock(request)` |
| `DELETE /api/building-blocks/{id}` | `apiClient.deleteBlock(id)` |
| `GET /api/building-blocks/{id}` | `apiClient.getBlockById(id)` |

## Code Migration Examples

### Before: HTTP-based API calls
```kotlin
// Old way - using Ktor HTTP client
class OldApiClient {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }
    
    suspend fun getAllBlocks(): ApiResponse<BuildingBlocksResponse> {
        return try {
            httpClient.get("$BASE_URL/api/building-blocks")
                .body<ApiResponse<BuildingBlocksResponse>>()
        } catch (e: Exception) {
            ApiResponse(success = false, error = e.message)
        }
    }
}
```

### After: Supabase-based operations
```kotlin
// New way - using Supabase directly
class NewApiClient {
    private val apiClient = BuildingBlockApiClient()
    
    suspend fun getAllBlocks(): ApiResponse<BuildingBlocksResponse> {
        return apiClient.getAllBlocks()
    }
}
```

## Database Schema Comparison

### Old: In-memory data structure
```kotlin
// Server kept data in memory
object BuildingBlockData {
    private val defaultBlocks = mutableListOf<BuildingBlockDto>(...)
    private val customBlocks = mutableListOf<BuildingBlockDto>()
}
```

### New: PostgreSQL table via Supabase
```sql
CREATE TABLE building_blocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    color_hex TEXT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

## Benefits of Migration

### 🚀 Performance
- **Faster**: Direct database access instead of HTTP round-trips
- **Scalable**: PostgreSQL database vs. in-memory storage
- **Persistent**: Data survives server restarts

### 🛠 Development
- **No Server Maintenance**: No need to deploy/maintain backend
- **Type Safety**: Generated types from Supabase schema
- **Real-time**: Built-in subscriptions for live updates

### 💰 Cost
- **Reduced Infrastructure**: No server hosting costs
- **Generous Free Tier**: Supabase provides substantial free usage
- **Auto-scaling**: Pay only for what you use

### 🔐 Security
- **Row Level Security**: Built-in database-level security
- **Authentication Ready**: Easy to add user auth later
- **Backup & Recovery**: Automatic database backups

## Migration Steps for Your App

1. **Setup Supabase Project** (see SUPABASE_SETUP.md)
2. **Update Configuration** - Add your Supabase credentials
3. **Create Database Table** - Run the provided SQL
4. **Test New API** - Use the example code to verify functionality
5. **Update Your UI Code** - Replace HTTP calls with new API client
6. **Remove Server Dependencies** - Clean up old server code
7. **Deploy** - Your app now works without a backend server!

## Rollback Plan (If Needed)

If you need to rollback:

1. **Keep server module** - Don't delete it immediately
2. **Revert API endpoints** - Change back to HTTP URLs
3. **Restore Ktor dependencies** - Add them back to build files
4. **Switch API calls** - Use old HTTP client instead of Supabase

## Testing Checklist

After migration, test these scenarios:

- [ ] **Get all blocks** - Verify default and custom blocks load
- [ ] **Create block** - Test custom block creation
- [ ] **Delete block** - Verify custom blocks can be deleted
- [ ] **Default block protection** - Confirm defaults can't be deleted
- [ ] **Error handling** - Test with invalid data
- [ ] **All platforms** - Test Android, iOS, Desktop, and Web
- [ ] **Offline behavior** - Verify graceful handling of network issues

## Performance Comparison

| Metric | Old (HTTP Server) | New (Supabase) |
|--------|------------------|----------------|
| **API Latency** | ~200-500ms | ~100-200ms |
| **Data Persistence** | ❌ Lost on restart | ✅ PostgreSQL |
| **Concurrent Users** | Limited by server | Scales automatically |
| **Deployment Complexity** | High (server + client) | Low (client only) |
| **Real-time Updates** | Manual polling | Built-in subscriptions |

## Future Enhancements

With Supabase, you can easily add:

1. **User Authentication** - Supabase Auth integration
2. **Real-time Collaboration** - Live updates across devices
3. **File Uploads** - Supabase Storage for images/assets
4. **Advanced Queries** - Complex filtering and sorting
5. **Analytics** - Built-in usage statistics
6. **Edge Functions** - Server-side logic when needed

## Support

If you encounter issues during migration:

1. Check the [Supabase documentation](https://supabase.com/docs)
2. Review the error logs in Supabase dashboard
3. Test with the provided example code
4. Verify your database table schema matches the SQL provided
