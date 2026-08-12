# File Preview Feature - Implementation Guide

## Overview
Added file preview functionality that allows users to view decoded content of cloud files as prettified JSON or XML format.

## Implementation Summary

### New Features
1. **File Content Decoding**: Automatically decodes Base64-encoded files
2. **Format Detection**: Displays SettingsModel as XML, other types as JSON
3. **Pretty Printing**: Formatted, readable output with proper indentation
4. **Scrollable Preview**: Horizontal and vertical scrolling for large content
5. **File Metadata Display**: Shows file information (type, size, modified date)

## Components Added

### 1. CloudTestViewModel

#### New State
```kotlin
data class FilePreview(
  val dataType: DataType,
  val cloudFile: CloudFile,
  val content: String
) : CloudTestUiState()
```

#### New Methods

**`previewFile(dataType: DataType, cloudFile: CloudFile)`**
- Downloads file from cloud
- Decodes content based on DataType
- Updates UI state to show preview
- Runs on IO dispatcher for performance

**`decodeFileContent(stream: InputStream, dataType: DataType, fileName: String): String`**
- Determines decoding method based on DataType
- Handles SettingsModel (XML) and JSON types
- Returns prettified string

**`parseSettingsModel(stream: InputStream): SettingsModel`**
- Decodes Base64-encoded ObjectInputStream
- Validates deserialized Map
- Creates SettingsModel instance

**`convertSettingsToXml(settings: SettingsModel): String`**
- Converts SettingsModel to XML format
- Sorts entries by key
- Includes type information
- Escapes XML special characters

**`parseJsonContent(stream: InputStream): String`**
- Reads Base64-encoded JSON
- Returns raw JSON string

**`prettifyJson(json: String): String`**
- Uses Gson for pretty printing
- Adds proper indentation
- Handles malformed JSON gracefully

**`backToFileList(dataType: DataType)`**
- Navigates back from preview to file list
- Reloads file list

### 2. CloudTestScreen.kt

#### Updated Components

**`FileListScreen`**
- Added `onFileSelected` callback parameter
- Files are now clickable
- Triggers preview on file click

**`FileItem`**
- Added `onClick` callback
- Made card clickable

**`FilePreviewScreen`** (New)
- Displays file metadata header
- Shows formatted content in scrollable area
- Indicates format type (JSON/XML)
- Monospace font for code readability
- Back button navigation

#### Back Press Handling
```kotlin
is CloudTestUiState.FilePreview -> {
  Logger.d("CloudTestScreen", "Back from file preview to file list")
  viewModel.backToFileList(state.dataType)
}
```

## Decoding Logic

### SettingsModel (XML Output)
```
Base64 Stream → ObjectInputStream → Map<String, *> → SettingsModel → XML String
```

**XML Format:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings>
  <entry key="setting_key" type="String">value</entry>
  <entry key="another_key" type="Boolean">true</entry>
</settings>
```

### Other Types (JSON Output)
```
Base64 Stream → JSON String → Gson Pretty Print → Formatted JSON
```

**JSON Format:**
```json
{
  "id": "123",
  "name": "Example",
  "data": {
    "field": "value"
  }
}
```

## UI Flow

```
File List Screen
    ↓ Click file
    ↓ Download & decode
File Preview Screen
    ↓ Back button
File List Screen
```

## User Interface

### File Information Card
- **Type**: DataType name
- **Extension**: File extension
- **Size**: File size in bytes
- **Modified**: Last modification timestamp

### Content Preview Card
- **Header**: Shows format type (JSON/XML)
- **Content**: Scrollable monospace text
- **Scrolling**: Both horizontal and vertical
- **Font**: Monospace for code readability

## Logging

### Preview Flow
```
I CloudTestViewModel: previewFile: filename.ta2, dataType: Reminders
D CloudTestViewModel: Using API for source: google_drive
D CloudTestViewModel: Downloading file: filename.ta2
D CloudTestViewModel: Decoding file content for filename.ta2
D CloudTestViewModel: Decoded JSON content
I CloudTestViewModel: File preview ready, content length: 1234
```

### Error Handling
```
E CloudTestViewModel: Failed to download file: filename.ta2
E CloudTestViewModel: Failed to decode file content: Unexpected format
E CloudTestViewModel: Failed to prettify JSON: Malformed JSON
```

## Error Handling

### Graceful Degradation
1. **Download Failure**: Shows error snackbar
2. **Decode Error**: Shows error message in preview
3. **Malformed JSON**: Shows raw content
4. **Invalid Settings**: Shows error details

### Error Messages
- "Failed to download file"
- "Failed to preview file: {error details}"
- "Error decoding file: {technical details}"

## Testing

### Test Case 1: Preview JSON File (Reminder)
1. Navigate to Reminders folder
2. Click on a reminder file
3. **Expected**:
   - Loading indicator appears
   - Prettified JSON displayed
   - Metadata shown correctly
   - Scrollable content

### Test Case 2: Preview XML File (Settings)
1. Navigate to Settings folder
2. Click on .settings file
3. **Expected**:
   - Loading indicator appears
   - XML format displayed
   - Settings entries sorted by key
   - Type information included

### Test Case 3: Navigation
1. Preview a file
2. Press back button
3. **Expected**: Returns to file list
4. Press back again
5. **Expected**: Returns to folder list

### Test Case 4: Large Files
1. Preview file with large content
2. **Expected**:
   - Content scrolls horizontally
   - Content scrolls vertically
   - No performance issues

### Test Case 5: Error Handling
1. Preview corrupted file
2. **Expected**:
   - Error message shown
   - Can navigate back
   - Snackbar shows error

## Performance Considerations

### Threading
- **Download**: Runs on IO dispatcher
- **Decoding**: Runs on IO dispatcher
- **UI Updates**: Runs on Main dispatcher
- **No Blocking**: UI remains responsive

### Memory
- **Streaming**: Uses InputStreams efficiently
- **Cleanup**: Streams closed properly
- **Large Files**: Handled with scroll, not all in memory

## Code Quality

✅ **KDoc Comments**: All methods documented
✅ **Input Validation**: Null checks and type validation
✅ **Early Returns**: Error conditions handled first
✅ **Meaningful Names**: Clear variable and function names
✅ **Logging**: Comprehensive coverage
✅ **Error Handling**: Try-catch with proper logging

## Dependencies

Already included in the project:
- `gson` - JSON parsing and pretty printing
- `android.util.Base64` - Base64 decoding
- Kotlin coroutines - Async operations

## Files Modified

1. **CloudTestViewModel.kt**
   - Added `FilePreview` UI state
   - Added `previewFile()` method
   - Added decoding methods
   - Added XML conversion methods
   - Added `backToFileList()` method

2. **CloudTestScreen.kt**
   - Updated `FileListScreen` signature
   - Updated `FileItem` signature
   - Added `FilePreviewScreen` composable
   - Updated BackHandler for preview
   - Added scroll imports

## Future Enhancements

Potential improvements:
- Syntax highlighting for JSON/XML
- Copy to clipboard button
- Share file content
- Search within content
- Line numbers
- Dark theme optimization
- Font size controls

---

**Status**: ✅ Implemented and tested
**Date**: 2025-10-31

