# Back Press Handling - Implementation Guide

## Overview
Added comprehensive back press handling to properly navigate between screens in the Cloud Test Admin app.

## Implementation

### BackHandler in CloudTestScreen

Added `BackHandler` composable that intercepts the system back button based on the current UI state.

```kotlin
import androidx.activity.compose.BackHandler

BackHandler(enabled = uiState !is CloudTestUiState.SelectSource) {
  when (uiState) {
    is CloudTestUiState.NeedAuth -> viewModel.backToSourceSelection()
    is CloudTestUiState.FolderList -> viewModel.backToSourceSelection()
    is CloudTestUiState.FileList -> viewModel.backToFolderList()
    is CloudTestUiState.SelectSource -> { /* Never reached */ }
  }
}
```

## Navigation Flow

### Back Press Behavior by Screen

| Current Screen | Back Press Action | Destination |
|---------------|-------------------|-------------|
| **Source Selection** | System handles (exit app) | Exit app |
| **Authentication** | Intercepted | Source Selection |
| **Folder List** | Intercepted | Source Selection |
| **File List** | Intercepted | Folder List |

### Navigation Hierarchy

```
┌─────────────────────────┐
│   Source Selection      │ ← Back press exits app
│   (Google/Dropbox)      │
└───────────┬─────────────┘
            │
    ┌───────▼──────────┐
    │  Authentication  │ ← Back press → Source Selection
    └───────┬──────────┘
            │
    ┌───────▼──────────┐
    │   Folder List    │ ← Back press → Source Selection
    │  (DataTypes)     │
    └───────┬──────────┘
            │
    ┌───────▼──────────┐
    │   File List      │ ← Back press → Folder List
    │   (CloudFiles)   │
    └──────────────────┘
```

## Key Features

### 1. Conditional Back Handling
```kotlin
BackHandler(enabled = uiState !is CloudTestUiState.SelectSource) { ... }
```
- **Enabled**: On all screens except Source Selection
- **Disabled**: On Source Selection (allows system to exit app)

### 2. State-Based Navigation
Each screen state has a specific back action:
- **NeedAuth** → Returns to source selection
- **FolderList** → Returns to source selection (logs out)
- **FileList** → Returns to folder list

### 3. Comprehensive Logging
Every back press is logged:
```
D CloudTestScreen: Back press detected, current state: NeedAuth
D CloudTestScreen: Back from auth screen to source selection
D CloudTestViewModel: Navigating back to source selection
```

### 4. Consistent with UI Buttons
Back press behavior matches the back arrow buttons in TopAppBar:
- Both use the same ViewModel methods
- Same navigation logic
- Same logging

## Testing

### Test Case 1: Source Selection Screen
1. Launch app (on Source Selection)
2. Press back button
3. **Expected**: App exits

### Test Case 2: Authentication Screen
1. Select a cloud service (navigate to Auth)
2. Press back button
3. **Expected**: Returns to Source Selection
4. **Logs**:
   ```
   D CloudTestScreen: Back from auth screen to source selection
   D CloudTestViewModel: Navigating back to source selection
   ```

### Test Case 3: Folder List Screen
1. Login to cloud service (navigate to Folder List)
2. Press back button
3. **Expected**: Returns to Source Selection
4. **Logs**:
   ```
   D CloudTestScreen: Back from folder list to source selection
   D CloudTestViewModel: Navigating back to source selection
   ```

### Test Case 4: File List Screen
1. Click on a folder (navigate to File List)
2. Press back button
3. **Expected**: Returns to Folder List
4. **Logs**:
   ```
   D CloudTestScreen: Back from file list to folder list
   D CloudTestViewModel: Navigating back to folder list
   ```

### Test Case 5: Deep Navigation
1. Source Selection → Login → Folder List → File List
2. Press back button (File List → Folder List)
3. Press back button (Folder List → Source Selection)
4. Press back button (App exits)

## Code Changes

### Files Modified

#### CloudTestScreen.kt
- Added `BackHandler` import
- Added conditional `BackHandler` with state-based navigation
- Added logging for each back press action

### ViewModel Methods Used

| Method | Description |
|--------|-------------|
| `backToSourceSelection()` | Clears current source and navigates to selection |
| `backToFolderList()` | Navigates to folder list |

Both methods already have logging built in.

## Advantages

✅ **Intuitive Navigation**: Back button works as expected
✅ **Consistent UX**: Matches TopAppBar back arrows
✅ **Proper State Management**: Uses ViewModel for navigation
✅ **Exit Control**: Allows system to handle app exit
✅ **Comprehensive Logging**: Full visibility of navigation flow
✅ **Type Safe**: Uses sealed class UI states

## Edge Cases Handled

1. **On Source Selection**: System handles back press (app exits)
2. **During Loading**: Back press still works (state doesn't change during loading)
3. **After Error**: Back press works normally after error snackbar
4. **Multiple Rapid Presses**: State machine prevents issues

## Debugging

### Logcat Filter
```
tag:CloudTestScreen|CloudTestViewModel
```

### Expected Log Sequence (File List → Folder List → Exit)
```
D CloudTestScreen: Back press detected, current state: FileList
D CloudTestScreen: Back from file list to folder list
D CloudTestViewModel: Navigating back to folder list
D CloudTestScreen: Back press detected, current state: FolderList
D CloudTestScreen: Back from folder list to source selection
D CloudTestViewModel: Navigating back to source selection
[Back press on Source Selection - app exits]
```

## Future Enhancements

Potential improvements:
- Double-tap to exit confirmation on Source Selection
- Animation transitions between screens
- Save/restore navigation state on configuration change
- Custom back press animation

---

**Status**: ✅ Implemented and tested
**Files Modified**: CloudTestScreen.kt
**Date**: 2025-10-31

