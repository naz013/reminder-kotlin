# Quick Start Guide

## Prerequisites

1. **Google Drive Setup**:
   - Ensure Google Play Services are installed on the device/emulator
   - The app uses the credentials configured in the main app's Google API Console

2. **Dropbox Setup**:
   - Ensure Dropbox API keys are configured in CloudKeysStorage
   - The app will use the Dropbox OAuth flow

## Running the App

### Using Android Studio

1. Open the project in Android Studio
2. Select the `cloudtestadmin` configuration from the run configurations dropdown
3. Click Run or press Shift+F10

### Using Gradle

```cmd
gradlew :cloudtestadmin:installDebug
```

## First Run Flow

### Testing Google Drive

1. Launch the app
2. Tap "Google Drive"
3. If not authenticated, tap "Sign in with Google"
4. Select your Google account
5. Grant necessary permissions
6. You'll see the folder list screen with all DataTypes
7. Tap any folder (e.g., "Reminders")
8. View files with .ta2 extension

### Testing Dropbox

1. From the main screen, tap "Dropbox"
2. You'll be redirected to Dropbox authorization page in browser
3. Log in and authorize the app
4. Return to the app (may need to tap the app icon)
5. Browse folders and files

## Features to Test

### Navigation
- ✅ Back button on folder list returns to source selection
- ✅ Back button on file list returns to folder list
- ✅ Logout button on folder list (logs out and returns to source selection)

### Data Display
- ✅ Source selection shows authentication status
- ✅ Folder list shows all DataType enum values
- ✅ File list shows CloudFile metadata (name, size, date)
- ✅ Empty folder shows "No files found" message

### Error Handling
- ✅ Loading indicator during file operations
- ✅ Error messages displayed in Snackbar
- ✅ Authentication failures handled gracefully

## Troubleshooting

### Google Drive "Sign in failed"
- Check Google Play Services are up to date
- Verify the Google API credentials are configured
- Check device has network connectivity

### Dropbox authentication doesn't complete
- Make sure to return to the app after authorizing
- Check Dropbox API keys are configured
- Try force-closing and reopening the app

### No files shown
- This is expected if no files have been synced yet
- Use the main reminder app to create and sync data
- Then open cloudtestadmin to view the files

## Code Structure

```
cloudtestadmin/
├── CloudTestApplication.kt    # App initialization
├── MainActivity.kt             # Main activity with Compose
├── CloudTestViewModel.kt       # State management
├── CloudTestScreen.kt          # UI components
└── KoinModule.kt              # Dependency injection
```

## Next Steps

After verifying the app works:
1. Test with real data from the main app
2. Verify file upload/download operations
3. Test with both Google Drive and Dropbox
4. Check error handling with network failures

