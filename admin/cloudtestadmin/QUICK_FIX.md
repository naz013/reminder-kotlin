# Quick Fix Reference - Main Thread Deadlock

## Error
```
GoogleDriveApi: Failed to find files: Calling this from your main thread can lead to deadlock
```

## Fix Applied
Changed `CloudTestViewModel.loadFiles()` to use IO dispatcher for network calls.

## Code Change
```kotlin
// Use Dispatchers.IO for network operations
viewModelScope.launch(Dispatchers.IO) {
  // UI updates must use withContext(Dispatchers.Main)
  withContext(Dispatchers.Main) {
    _isLoading.value = true
  }

  // Network call runs on IO thread (safe)
  val files = api.findFiles(dataType.fileExtension)

  // Switch back to Main for UI update
  withContext(Dispatchers.Main) {
    _uiState.value = CloudTestUiState.FileList(dataType, files)
  }
}
```

## Imports Added
```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
```

## Result
✅ Network calls run on background thread
✅ UI updates happen on main thread
✅ No deadlock warning
✅ Smooth, responsive UI

## Test It
1. Build and run: `gradlew :cloudtestadmin:installDebug`
2. Login to Google Drive
3. Click any folder
4. **Verify**: Files load without deadlock error in logs

---
**Fixed in**: CloudTestViewModel.kt
**Status**: ✅ Complete

