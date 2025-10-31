# Quick Debug Guide - Google Drive Authentication

## Logcat Filter
```
tag:CloudTestScreen|CloudTestViewModel|GoogleDriveAuthManager|GoogleDriveApi
```

## Key Log Points

### 1. Starting Authentication
```
I CloudTestScreen: Starting Google Sign-In flow
D CloudTestScreen: Scopes: [...]
D CloudTestScreen: Launching Google Sign-In intent
```
**Problem if missing**: Button click not registered or exception in onClick

### 2. Receiving Result
```
D CloudTestScreen: Google Sign-In result received, resultCode=-1
```
**resultCode=-1**: Success (RESULT_OK)
**resultCode=0**: Cancelled
**Problem if missing**: Activity result callback not working

### 3. Processing Account
```
I CloudTestScreen: Google Sign-In successful, email=user@example.com
```
**Problem if "Email is null"**: Account has no email
**Problem if "Account is null"**: Sign-in failed or cancelled

### 4. Saving Credentials
```
D CloudTestScreen: User name saved to storage
D GoogleDriveAuthManager: Google Drive user name saved
```
**Problem if missing**: CloudKeysStorage not working

### 5. Initializing API
```
D CloudTestScreen: Google Drive API initialized
```
**Problem if missing**: GoogleDriveApi.initialize() failed

### 6. Completing Authentication
```
I CloudTestViewModel: onAuthenticationComplete called for source: GoogleDrive
D CloudTestViewModel: isAuthenticated for GoogleDrive: true
I CloudTestViewModel: Authentication successful, navigating to folder list
```
**Problem if "false"**: User name not saved correctly or doesn't match email pattern

## Common Issues

### Issue: "Authentication failed" snackbar
**Check logs for**:
1. Was result received? → Check step 2
2. Was account extracted? → Check step 3
3. Was email saved? → Check step 4
4. Was API initialized? → Check step 5
5. Is user authorized? → Check step 6

### Issue: Nothing happens after selecting account
**Check**:
- resultCode in step 2 (should be -1)
- Look for Exception logs
- Check if callback is triggered

### Issue: Folder list not showing
**Check**:
- Step 6 logs - is isAuthenticated returning true?
- Look for "Navigating to folder list" log
- Check UI state in ViewModel

## Manual Verification

### Check CloudKeysStorage
Add temporary log in GoogleDriveAuthManager:
```kotlin
override fun isAuthorized(): Boolean {
  val userName = cloudKeysStorage.getGoogleDriveUserName()
  Logger.d(TAG, "Checking auth: userName='$userName'")
  return userName.isNotEmpty() && userName.matches(".*@.*".toRegex())
}
```

### Check API Initialization
Look for this in GoogleDriveApi logs:
```
I GoogleDriveApi: Google Drive initialized: true
```

## Testing Commands

### Clear app data
```cmd
adb shell pm clear com.cray.software.justreminderpro
```

### View logs in real-time
```cmd
adb logcat -s CloudTestScreen:* CloudTestViewModel:* GoogleDriveAuthManager:* GoogleDriveApi:*
```

### Install debug build
```cmd
gradlew :cloudtestadmin:installDebug
```

## Success Indicators

1. ✅ All 6 log points appear in sequence
2. ✅ No error logs
3. ✅ UI shows folder list
4. ✅ No snackbar error message
5. ✅ Can navigate to file list

## Failure Indicators

1. ❌ "Authentication failed" snackbar
2. ❌ "Email is null" or "Account is null" in logs
3. ❌ isAuthenticated returns false in step 6
4. ❌ Missing log entries from steps 1-5
5. ❌ Exception stack traces in logs

