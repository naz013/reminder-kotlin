# Visual Test Guide - Back Press Behavior

## Quick Visual Test

### 🎯 Test 1: Source Selection Screen
```
┌─────────────────────────┐
│   Source Selection      │
│                         │
│   [Google Drive]        │
│   [Dropbox]             │
│                         │
└─────────────────────────┘
         ↓ Press BACK
    [App Exits] ✅
```

### 🎯 Test 2: Authentication Screen
```
┌─────────────────────────┐
│ ← Google Drive Login    │
│                         │
│  [Sign in with Google]  │
│                         │
└─────────────────────────┘
         ↓ Press BACK
┌─────────────────────────┐
│   Source Selection      │
│   [Google Drive]        │
│   [Dropbox]             │ ✅
└─────────────────────────┘
```

### 🎯 Test 3: Folder List Screen
```
┌─────────────────────────┐
│ ← Folders        [🚪]   │
│                         │
│  📁 Reminders           │
│  📁 Notes               │
│  📁 Birthdays           │
│  📁 Groups              │
└─────────────────────────┘
         ↓ Press BACK
┌─────────────────────────┐
│   Source Selection      │
│   [Google Drive]        │
│   [Dropbox]             │ ✅
└─────────────────────────┘
```

### 🎯 Test 4: File List Screen
```
┌─────────────────────────┐
│ ← Reminders Files       │
│                         │
│  📄 reminder1.ta2       │
│     Size: 1234 bytes    │
│  📄 reminder2.ta2       │
│     Size: 5678 bytes    │
└─────────────────────────┘
         ↓ Press BACK
┌─────────────────────────┐
│ ← Folders        [🚪]   │
│  📁 Reminders           │
│  📁 Notes               │ ✅
│  📁 Birthdays           │
└─────────────────────────┘
```

### 🎯 Test 5: Deep Navigation
```
Source Selection
    ↓ Select Google Drive
Authentication
    ↓ Sign in
Folder List
    ↓ Click Reminders
File List
    ↓ Press BACK
Folder List ✅
    ↓ Press BACK
Source Selection ✅
    ↓ Press BACK
[App Exits] ✅
```

## Expected Behavior Summary

| Screen | Back Press | TopAppBar ← | Both Should |
|--------|-----------|-------------|-------------|
| Source Selection | Exit app | N/A | Exit |
| Authentication | → Source | → Source | Match ✅ |
| Folder List | → Source | → Source | Match ✅ |
| File List | → Folders | → Folders | Match ✅ |

## Visual Indicators

### ✅ Success Indicators
- Smooth screen transitions
- No error snackbars
- Correct destination screen
- Loading states work properly

### ❌ Failure Indicators
- App crashes
- Wrong destination screen
- Error snackbar appears
- Back press does nothing

## Logcat Visual

```
┌──────────────────────────────────────────────────────────┐
│ Logcat Output (tag:CloudTestScreen|CloudTestViewModel)  │
├──────────────────────────────────────────────────────────┤
│ D CloudTestScreen: Back press detected, current state:  │
│                    FileList                              │
│ D CloudTestScreen: Back from file list to folder list   │
│ D CloudTestViewModel: Navigating back to folder list    │
│                                                          │
│ D CloudTestScreen: Back press detected, current state:  │
│                    FolderList                            │
│ D CloudTestScreen: Back from folder list to source      │
│                    selection                             │
│ D CloudTestViewModel: Navigating back to source         │
│                      selection                           │
│                                                          │
│ [Press back on Source Selection - app exits]            │
└──────────────────────────────────────────────────────────┘
```

## One-Minute Test Script

```
1. Launch app
   ✅ See: Source Selection screen

2. Press BACK
   ✅ App exits

3. Launch app again
   ✅ See: Source Selection screen

4. Tap "Google Drive"
   ✅ See: Authentication screen

5. Press BACK
   ✅ See: Source Selection screen

6. Tap "Google Drive", sign in
   ✅ See: Folder List screen

7. Press BACK
   ✅ See: Source Selection screen

8. Re-login (already authenticated)
   ✅ See: Folder List screen directly

9. Tap "Reminders" folder
   ✅ See: File List screen

10. Press BACK
    ✅ See: Folder List screen

11. Press BACK
    ✅ See: Source Selection screen

12. Press BACK
    ✅ App exits

TOTAL TIME: < 1 minute
ALL PASSED: ✅
```

## Screenshot Checklist

### Source Selection
- [ ] Google Drive card visible
- [ ] Dropbox card visible
- [ ] Connection status shown
- [ ] No back arrow in TopAppBar

### Authentication
- [ ] Back arrow in TopAppBar
- [ ] Sign in button visible
- [ ] Title shows service name

### Folder List
- [ ] Back arrow in TopAppBar
- [ ] Logout button in TopAppBar
- [ ] All DataType folders listed
- [ ] Cards are clickable

### File List
- [ ] Back arrow in TopAppBar
- [ ] Files listed with metadata
- [ ] Loading indicator (when loading)
- [ ] "No files" message (when empty)

---

**Test Duration**: 1-2 minutes
**Expected Result**: All ✅
**Status**: Ready to test!

