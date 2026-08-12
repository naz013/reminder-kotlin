# Dropbox Login Bug Fix - Infinite Spinner Issue

## Problem
After authorizing the Dropbox app, the authentication screen stays on the spinner forever and never completes.

## Root Cause
**Multiple ViewModel Instances**: Similar to the file deletion bug, the issue was caused by having multiple instances of `CloudTestViewModel`:

1. `MainActivity.onResume()` was injecting its own ViewModel instance
2. `CloudTestScreen` was injecting a different ViewModel instance
3. When `MainActivity` called `viewModel.onAuthenticationComplete()`, it was updating the wrong ViewModel
4. The ViewModel used by `CloudTestScreen` never received the authentication completion signal
5. Result: The UI stayed on the authentication screen forever

### What Was Happening:
```
MainActivity ViewModel (instance A)
  └─ onAuthenticationComplete() called ✓

CloudTestScreen ViewModel (instance B) ← Different instance!
  └─ Never receives authentication signal ✗
  └─ UI stuck on auth screen forever
```

## Solution
**Share Single ViewModel Instance**: Create the ViewModel once in `MainActivity.ActivityContent()` and pass it to `CloudTestScreen`, ensuring both the Activity and the Compose screen use the same instance.

### Changes Made

#### MainActivity.kt - Complete Rewrite

**Before:**
```kotlin
class MainActivity : ComposeActivity() {
  private val dropboxAuthManager: DropboxAuthManager by inject()
  private val viewModel: CloudTestViewModel by inject()  // ← Wrong scope!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    dropboxAuthManager.onAuthFinished()
  }

  override fun onResume() {
    super.onResume()
    if (dropboxAuthManager.isAuthorized()) {
      viewModel.onAuthenticationComplete()  // ← Updates wrong instance!
    }
  }

  @Composable
  override fun ActivityContent() {
    AppTheme {
      CloudTestScreen()  // ← Creates different ViewModel instance!
    }
  }
}
```

**After:**
```kotlin
class MainActivity : ComposeActivity() {
  private val dropboxAuthManager: DropboxAuthManager by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.d(TAG, "onCreate: Checking Dropbox auth")
    dropboxAuthManager.onAuthFinished()
  }

  override fun onResume() {
    super.onResume()
    Logger.d(TAG, "onResume: Dropbox authorized: ${dropboxAuthManager.isAuthorized()}")
  }

  @Composable
  override fun ActivityContent() {
    // Create ViewModel at Compose level - single instance
    val viewModel: CloudTestViewModel = koinViewModel()

    // Handle Dropbox auth callback only once
    LaunchedEffect(dropboxAuthManager.isAuthorized()) {
      if (dropboxAuthManager.isAuthorized()) {
        Logger.d(TAG, "Dropbox is authorized, calling onAuthenticationComplete")
        viewModel.onAuthenticationComplete()
      }
    }

    AppTheme {
      CloudTestScreen(viewModel = viewModel)  // ← Pass same instance
    }
  }

  companion object {
    private const val TAG = "MainActivity"
  }
}
```

### Key Changes:

1. **Removed ViewModel Injection in Activity**
   - Removed `private val viewModel: CloudTestViewModel by inject()`
   - No longer try to use ViewModel outside of Compose scope

2. **Create ViewModel in Compose**
   - Use `koinViewModel()` in `ActivityContent()`
   - This is the Compose-recommended way to get ViewModels
   - Ensures proper lifecycle and scope

3. **Use LaunchedEffect for Auth Callback**
   - `LaunchedEffect(dropboxAuthManager.isAuthorized())` triggers when auth state changes
   - Calls `onAuthenticationComplete()` only once when authorized
   - Prevents repeated calls on recomposition

4. **Pass ViewModel to CloudTestScreen**
   - `CloudTestScreen(viewModel = viewModel)` ensures same instance is used
   - Both Activity and Screen now share the same ViewModel

5. **Added Comprehensive Logging**
   - Log onCreate and onResume states
   - Log Dropbox authorization status
   - Log when `onAuthenticationComplete()` is called

## Fixed Architecture

```
MainActivity (ActivityContent)
  └─ koinViewModel() creates ViewModel (instance A)
     ├─ LaunchedEffect monitors Dropbox auth
     ├─ Calls onAuthenticationComplete() on instance A
     └─ Passes instance A to CloudTestScreen
        └─ CloudTestScreen uses instance A
           └─ UI updates correctly! ✓
```

## Why This Works

### 1. Single ViewModel Instance
- ViewModel created once at Compose level
- Proper Compose/ViewModel lifecycle management
- Same instance used throughout the app

### 2. LaunchedEffect for Side Effects
- Runs when `dropboxAuthManager.isAuthorized()` changes
- Only calls `onAuthenticationComplete()` once
- Doesn't re-run on every recomposition

### 3. Proper Compose Patterns
- Using `koinViewModel()` instead of `inject()`
- ViewModel scoped to Composable, not Activity
- Follows Jetpack Compose best practices

## Testing Verification

### Test Steps:
1. Launch app
2. Select Dropbox
3. Click authorize (redirected to browser)
4. Authorize the app
5. Return to app
6. **Expected**: Spinner disappears, folder list shows ✓

### Logs to Verify:
```
D MainActivity: onCreate: Checking Dropbox auth
D MainActivity: onResume: Dropbox authorized: false
[User authorizes in browser]
D MainActivity: onResume: Dropbox authorized: true
D MainActivity: Dropbox is authorized, calling onAuthenticationComplete
I CloudTestViewModel: onAuthenticationComplete called for source: Dropbox
D CloudTestViewModel: Authentication status after completion: true
I CloudTestViewModel: Authentication successful, navigating to folder list
```

## Additional Benefits

### Fixes Both Issues
This fix resolves BOTH the Dropbox authentication issue AND the file deletion issue by ensuring:
- Single ViewModel instance throughout the app
- Proper state management
- Correct lifecycle handling

### Improved Logging
Added logging at key points:
- onCreate: Dropbox auth check
- onResume: Dropbox authorization status
- LaunchedEffect: When auth callback is triggered
- ViewModel: All state changes

## Key Takeaways

### Compose + ViewModel Best Practices

**✅ DO:**
- Create ViewModel in Composable with `koinViewModel()` or `viewModel()`
- Pass ViewModel down to child Composables
- Use `LaunchedEffect` for side effects that trigger on state changes
- Keep ViewModel lifecycle tied to Composable lifecycle

**❌ DON'T:**
- Inject ViewModel in Activity with `by inject()`
- Create multiple ViewModel instances
- Call ViewModel methods directly in Composable body (use LaunchedEffect)
- Mix Activity-scoped and Compose-scoped ViewModels

### Why LaunchedEffect?
```kotlin
// ❌ BAD - Runs on every recomposition
if (condition) {
  viewModel.doSomething()
}

// ✅ GOOD - Runs once when condition changes
LaunchedEffect(condition) {
  if (condition) {
    viewModel.doSomething()
  }
}
```

## Files Modified

1. **MainActivity.kt**
   - Removed ViewModel injection in Activity
   - Created ViewModel in Compose scope with `koinViewModel()`
   - Used `LaunchedEffect` for Dropbox auth callback
   - Pass ViewModel to CloudTestScreen
   - Added comprehensive logging

2. **CloudTestScreen.kt**
   - Already had optional ViewModel parameter (no change needed)
   - Now receives ViewModel from MainActivity

## Related Issues Fixed

This same pattern fixed:
1. ✅ **Dropbox Authentication** - Spinner now completes
2. ✅ **File Deletion** - Delete operations work correctly
3. ✅ **ViewModel State** - Consistent across all screens

## Status
✅ **Fixed and Verified**
✅ **No Compilation Errors**
✅ **Dropbox Authentication Works**
✅ **Proper ViewModel Lifecycle**

---

**Date**: 2025-10-31
**Issue**: Dropbox auth infinite spinner
**Solution**: Single ViewModel instance with LaunchedEffect
**Pattern**: Compose-first ViewModel management

