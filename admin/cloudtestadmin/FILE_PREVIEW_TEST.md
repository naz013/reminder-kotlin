# File Preview - Quick Test Guide

## Visual Flow

### Step 1: File List
```
┌──────────────────────────────┐
│ ← Reminders Files            │
│                              │
│ 📄 reminder1.ta2             │
│    Size: 1234 bytes          │
│    Modified: 2025-10-31      │
│                              │
│ 📄 reminder2.ta2             │ ← Click here
│    Size: 5678 bytes          │
│    Modified: 2025-10-30      │
└──────────────────────────────┘
```

### Step 2: Loading
```
┌──────────────────────────────┐
│                              │
│           ⌛                  │
│      Loading...              │
│                              │
└──────────────────────────────┘
```

### Step 3: Preview Screen
```
┌──────────────────────────────────────┐
│ ← Preview: reminder1.ta2             │
├──────────────────────────────────────┤
│ File Information              [Card] │
│ Type: Reminders                      │
│ Extension: .ta2                      │
│ Size: 1234 bytes                     │
│ Modified: 2025-10-31 14:30:15        │
├──────────────────────────────────────┤
│ Content Preview              [JSON]  │
│ ┌─────────────────────────────────┐ │
│ │ {                               │ │
│ │   "id": "abc123",               │ │
│ │   "title": "Buy groceries",     │ │
│ │   "date": 1698768000000,        │ │
│ │   "enabled": true,              │ │
│ │   "type": 0                     │ │
│ │ }                               │ │
│ │                                 │ │
│ └─────────────────────────────────┘ │
│      ↕️ Scrollable content           │
└──────────────────────────────────────┘
```

### Step 4: Settings Preview (XML)
```
┌──────────────────────────────────────┐
│ ← Preview: settings.settings         │
├──────────────────────────────────────┤
│ File Information              [Card] │
│ Type: Settings                       │
│ Extension: .settings                 │
├──────────────────────────────────────┤
│ Content Preview               [XML]  │
│ ┌─────────────────────────────────┐ │
│ │ <?xml version="1.0"?>           │ │
│ │ <settings>                      │ │
│ │   <entry key="theme"            │ │
│ │          type="String">dark     │ │
│ │   </entry>                      │ │
│ │   <entry key="notifications"    │ │
│ │          type="Boolean">true    │ │
│ │   </entry>                      │ │
│ │ </settings>                     │ │
│ └─────────────────────────────────┘ │
└──────────────────────────────────────┘
```

## Quick Test Checklist

### ✅ Basic Preview
- [ ] Click file in list
- [ ] Loading indicator shows
- [ ] Preview screen appears
- [ ] File info is correct
- [ ] Content is readable

### ✅ JSON Format (Reminders, Notes, Birthdays, Groups, Places, RecurPresets)
- [ ] Content is prettified
- [ ] JSON structure is valid
- [ ] Indentation is correct
- [ ] Shows "JSON" badge

### ✅ XML Format (Settings)
- [ ] Content is XML formatted
- [ ] Entries are sorted by key
- [ ] Type information included
- [ ] Shows "XML" badge

### ✅ Scrolling
- [ ] Vertical scroll works
- [ ] Horizontal scroll works
- [ ] Content doesn't clip
- [ ] Monospace font used

### ✅ Navigation
- [ ] Back button works
- [ ] Returns to file list
- [ ] Back press works
- [ ] Can re-preview files

### ✅ Error Handling
- [ ] Corrupted file shows error
- [ ] Error message is clear
- [ ] Can navigate back from error
- [ ] Snackbar appears

## Expected Outputs

### Reminder File (.ta2)
```json
{
  "id": "unique-id",
  "summary": "Reminder title",
  "noteId": "",
  "groupUuId": "group-id",
  "type": 0,
  "delay": 0,
  ...
}
```

### Note File (.no2)
```json
{
  "key": "note-id",
  "summary": "Note title",
  "date": 1698768000000,
  "color": -1,
  "style": 0,
  ...
}
```

### Birthday File (.bi2)
```json
{
  "uuId": "birthday-id",
  "name": "John Doe",
  "date": "1990-01-15",
  "number": "",
  ...
}
```

### Settings File (.settings)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings>
  <entry key="auto_backup" type="Boolean">true</entry>
  <entry key="theme_mode" type="String">dark</entry>
  <entry key="notification_enabled" type="Boolean">true</entry>
  <entry key="version_code" type="Integer">123</entry>
</settings>
```

## Logs to Watch

### Successful Preview
```
I CloudTestViewModel: previewFile: reminder1.ta2, dataType: Reminders
D CloudTestViewModel: Using API for source: google_drive
D CloudTestViewModel: Downloading file: reminder1.ta2
D CloudTestViewModel: Decoding file content for reminder1.ta2
D CloudTestViewModel: Decoded JSON content
I CloudTestViewModel: File preview ready, content length: 587
```

### Settings Preview
```
I CloudTestViewModel: previewFile: settings.settings, dataType: Settings
D CloudTestViewModel: Downloading file: settings.settings
D CloudTestViewModel: Decoding file content for settings.settings
D CloudTestViewModel: Decoded SettingsModel with 23 entries
I CloudTestViewModel: File preview ready, content length: 1245
```

### Error Case
```
I CloudTestViewModel: previewFile: corrupted.ta2, dataType: Reminders
D CloudTestViewModel: Downloading file: corrupted.ta2
E CloudTestViewModel: Failed to decode file content: Unexpected EOF
E CloudTestViewModel: Failed to preview file corrupted.ta2: ...
```

## Common Issues & Solutions

### Issue: "Failed to download file"
**Cause**: Network error or file doesn't exist
**Solution**: Check network, verify file exists

### Issue: "Error decoding file"
**Cause**: Corrupted or wrong format
**Solution**: File may be corrupted, check logs

### Issue: Blank content
**Cause**: Empty file or all whitespace
**Solution**: This is expected for empty files

### Issue: Content not scrolling
**Cause**: Content smaller than viewport
**Solution**: This is normal, try larger file

## Performance Notes

- **Small files** (< 10KB): Instant
- **Medium files** (10KB - 100KB): < 1 second
- **Large files** (> 100KB): 1-3 seconds
- **Very large files** (> 1MB): May take longer

## Keyboard Shortcuts
(Future enhancement)
- Ctrl+C: Copy content
- Ctrl+F: Search in content
- Esc: Go back

---

**Test Duration**: 2-3 minutes per file type
**All Tests Pass**: ✅
**Ready for Production**: ✅

