# Deadlock Fix - Google Drive API

## Issue
Error in logs: `GoogleDriveApi: Failed to find files: Calling this from your main thread can lead to deadlock`

## Root Cause
The Google Drive API operations are blocking network calls that should not be executed on the main thread. The ViewModel was using `viewModelScope.launch` which defaults to `Dispatchers.Main`, causing the API calls to run on the UI thread and leading to potential deadlocks.

## Solution
Modified `CloudTestViewModel.loadFiles()` to use `Dispatchers.IO` for network operations while keeping UI state updates on the main thread.

### Changes Made

#### CloudTestViewModel.kt

**Before:**
```kotlin
viewModelScope.launch {
  _isLoading.value = true
  _errorMessage.value = null
  try {
    val files = api.findFiles(dataType.fileExtension)
    _uiState.value = CloudTestUiState.FileList(dataType, files)
  } catch (e: Exception) {
    _errorMessage.value = "Failed to load files: ${e.message}"
  } finally {
    _isLoading.value = false
  }
}
```

**After:**
```kotlin
viewModelScope.launch(Dispatchers.IO) {
  withContext(Dispatchers.Main) {
    _isLoading.value = true
    _errorMessage.value = null
  }
  try {
    val files = api.findFiles(dataType.fileExtension)
    withContext(Dispatchers.Main) {
      _uiState.value = CloudTestUiState.FileList(dataType, files)
    }
  } catch (e: Exception) {
    withContext(Dispatchers.Main) {
      _errorMessage.value = "Failed to load files: ${e.message}"
    }
  } finally {
    withContext(Dispatchers.Main) {
      _isLoading.value = false
    }
  }
}
```

**Added Imports:**
```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
```

## How It Works

1. **`viewModelScope.launch(Dispatchers.IO)`** - Starts the coroutine on the IO dispatcher (background thread pool)
2. **Network Operation** - `api.findFiles()` executes on IO thread (safe for blocking operations)
3. **`withContext(Dispatchers.Main)`** - Switches to main thread for UI state updates
4. **StateFlow Updates** - All state changes happen on the main thread (required for UI)

## Benefits

✅ **No Deadlock**: Network operations run on background threads
✅ **Thread Safety**: UI state updates still happen on main thread
✅ **Better Performance**: Doesn't block the UI thread
✅ **Proper Android Architecture**: Follows coroutines best practices

## Testing

The error should no longer appear in logs when loading files from folders. The operation will:
1. Show loading indicator (main thread)
2. Load files from cloud (IO thread)
3. Update UI with results (main thread)

## Dispatcher Usage Guide

| Dispatcher | Use Case |
|------------|----------|
| `Dispatchers.Main` | UI updates, StateFlow/LiveData changes |
| `Dispatchers.IO` | Network calls, file I/O, database operations |
| `Dispatchers.Default` | CPU-intensive work, sorting, parsing |

## Related Files Modified

- `CloudTestViewModel.kt` - Added Dispatchers.IO for file loading operation

---

**Status**: ✅ Fixed
**Date**: 2025-10-31

