# Delete File Bug Fix - ViewModel Instance Issue

## Problem
Individual file deletion was failing with the error:
```
CloudTestViewModel: Cannot delete file - no cloud service selected
```

## Root Cause
**Multiple ViewModel Instances**: The issue was caused by creating new ViewModel instances using `koinInject()` in composables, rather than using the same instance that manages the app state.

### What Was Happening:
1. `CloudTestScreen` creates a ViewModel with `currentApi` and `currentSource` initialized
2. `FileListScreen` was calling `koinInject()` to get a NEW ViewModel instance
3. This new instance had `currentApi = null` and `currentSource = null`
4. When `deleteFile()` was called, it failed the null check immediately

## Solution
**Use Single ViewModel Instance**: Pass the ViewModel instance from the parent composable down to child composables instead of injecting new instances.

### Changes Made:

#### 1. CloudTestScreen.kt - Pass ViewModel to FileListScreen
**Before:**
```kotlin
is CloudTestUiState.FileList -> {
  FileListScreen(
    dataType = state.dataType,
    files = state.files,
    onFileSelected = { file -> viewModel.previewFile(state.dataType, file) },
    onBackPressed = { viewModel.backToFolderList() }
  )
}
```

**After:**
```kotlin
is CloudTestUiState.FileList -> {
  FileListScreen(
    viewModel = viewModel,  // ← Pass the ViewModel
    dataType = state.dataType,
    files = state.files,
    onFileSelected = { file -> viewModel.previewFile(state.dataType, file) },
    onBackPressed = { viewModel.backToFolderList() }
  )
}
```

#### 2. FileListScreen - Accept ViewModel Parameter
**Before:**
```kotlin
@Composable
fun FileListScreen(
  dataType: DataType,
  files: List<CloudFile>,
  onFileSelected: (CloudFile) -> Unit,
  onBackPressed: () -> Unit
) {
  var showDeleteAllDialog by remember { mutableStateOf(false) }
  val viewModel: CloudTestViewModel = koinInject()  // ← New instance!
```

**After:**
```kotlin
@Composable
fun FileListScreen(
  viewModel: CloudTestViewModel,  // ← Accept as parameter
  dataType: DataType,
  files: List<CloudFile>,
  onFileSelected: (CloudFile) -> Unit,
  onBackPressed: () -> Unit
) {
  var showDeleteAllDialog by remember { mutableStateOf(false) }
  // No more koinInject() here!
```

#### 3. FileItem - Use Callback
**Before:**
```kotlin
@Composable
fun FileItem(
  dataType: DataType,
  file: CloudFile,
  onFileClick: () -> Unit
) {
  val viewModel: CloudTestViewModel = koinInject()  // ← New instance!
  // ...
  onClick = { viewModel.deleteFile(file, dataType) }
```

**After:**
```kotlin
@Composable
fun FileItem(
  dataType: DataType,
  file: CloudFile,
  onFileClick: () -> Unit,
  onDeleteClick: () -> Unit  // ← Callback instead
) {
  // No ViewModel injection here!
  // ...
  onClick = { onDeleteClick() }
```

And in FileListScreen:
```kotlin
items(files) { file ->
  FileItem(
    dataType = dataType,
    file = file,
    onFileClick = { onFileSelected(file) },
    onDeleteClick = { viewModel.deleteFile(file, dataType) }  // ← Use parent's ViewModel
  )
}
```

## Why This Works

### ViewModel Lifecycle
- `CloudTestScreen` creates ONE ViewModel instance
- This instance is initialized with cloud service data
- The same instance is passed down to child composables
- All operations use the same state

### Previous Problem
```
CloudTestScreen ViewModel (instance A)
  ├─ currentSource = GoogleDrive ✓
  ├─ currentApi = GoogleDriveApi ✓

FileListScreen ViewModel (instance B) ← NEW INSTANCE!
  ├─ currentSource = null ✗
  ├─ currentApi = null ✗
  └─ deleteFile() → "Cannot delete file - no cloud service selected"
```

### Fixed Architecture
```
CloudTestScreen ViewModel (instance A)
  ├─ currentSource = GoogleDrive ✓
  ├─ currentApi = GoogleDriveApi ✓
  │
  └─ Passed to FileListScreen (same instance)
     └─ Passed to FileItem callbacks (same instance)
        └─ deleteFile() → Works! ✓
```

## Testing Verification

### Test Steps:
1. Launch app
2. Login to Google Drive or Dropbox
3. Navigate to any folder with files
4. Click delete button on a file
5. Confirm deletion
6. **Expected**: File deleted successfully ✓

### Logs to Verify:
```
I CloudTestViewModel: deleteFile: reminder1.ta2, dataType: Reminders
D CloudTestViewModel: Using API for source: google_drive  ← Not null!
D CloudTestViewModel: Deleting file: reminder1.ta2
I CloudTestViewModel: File deleted successfully: reminder1.ta2
```

## Key Takeaway

**Compose Best Practice**: When you need to share state between composables, pass the ViewModel (or state) as parameters rather than creating new instances with dependency injection. Each `koinInject()` call can create a new instance if not properly scoped.

### Rule of Thumb:
- ✅ **Create ViewModel once** at the screen level
- ✅ **Pass it down** to child composables
- ✅ **Use callbacks** from child to parent
- ❌ **Don't inject** ViewModel in nested composables
- ❌ **Don't create** multiple instances of the same ViewModel

## Files Modified
- `CloudTestScreen.kt`
  - Updated `CloudTestScreen` to pass viewModel to `FileListScreen`
  - Updated `FileListScreen` to accept viewModel parameter
  - Updated `FileItem` to use callback instead of injecting ViewModel

## Status
✅ **Fixed and Verified**
✅ **No Compilation Errors**
✅ **Delete Operations Work Correctly**

---

**Date**: 2025-10-31
**Issue**: ViewModel instance mismatch
**Solution**: Single ViewModel instance pattern

