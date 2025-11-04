# Fragment Navigation Graph - Reminder Kotlin App

**Generated on:** 2025-11-02
**Navigation Graph File:** `app/src/main/res/navigation/home_nav.xml`

---

## Table of Contents
1. [Overview](#overview)
2. [Top-Level Fragments (Bottom Navigation)](#top-level-fragments-bottom-navigation)
3. [Complete Fragment Hierarchy](#complete-fragment-hierarchy)
4. [Fragment Details with Parameters](#fragment-details-with-parameters)
5. [Navigation Flow Diagram](#navigation-flow-diagram)

---

## Overview

The app uses a single navigation graph (`home_nav.xml`) with a bottom navigation bar that provides access to 5 main sections. The start destination is `actionHome`.

**Total Fragments:** 47
**Total Navigation Actions:** 40
**Nested Navigation Graphs:** 1 (Other Settings)

---

## Top-Level Fragments (Bottom Navigation)

These fragments are accessible from the bottom navigation bar:

| ID | Fragment Class | Label | Description |
|---|---|---|---|
| `actionHome` | `com.elementary.tasks.home.HomeFragment` | Events | Main home screen (START DESTINATION) |
| `actionEvents` | `com.elementary.tasks.home.eventsview.HomeEventsFragment` | Events | Events view |
| `actionCalendar` | `com.elementary.tasks.calendar.monthview.CalendarFragment` | Calendar | Month calendar view |
| `actionNotes` | `com.elementary.tasks.notes.list.NotesFragment` | Notes | Notes list |
| `actionGoogle` | `com.elementary.tasks.googletasks.GoogleTasksFragment` | Tasks | Google Tasks list |

---

## Complete Fragment Hierarchy

### 1. Home Section (`actionHome`)

```
actionHome (HomeFragment) [START DESTINATION]
├── settingsFragment (SettingsFragment)
│   ├── generalSettingsFragment (GeneralSettingsFragment)
│   │   └── uiPreviewFragment (UiPreviewFragment)
│   ├── remindersSettingsFragment (RemindersSettingsFragment) [screen_title: String?]
│   │   └── managePresetsFragment (ManagePresetsFragment)
│   ├── exportSettingsFragment (ExportSettingsFragment)
│   │   └── fragmentCloudDrives (FragmentCloudDrives)
│   ├── notificationSettingsFragment (NotificationSettingsFragment)
│   ├── calendarSettingsFragment (CalendarSettingsFragment)
│   │   └── fragmentEventsImport (FragmentEventsImport)
│   ├── birthdaySettingsFragment (BirthdaySettingsFragment)
│   ├── securitySettingsFragment (SecuritySettingsFragment)
│   │   ├── addPinFragment (AddPinFragment)
│   │   ├── changePinFragment (ChangePinFragment)
│   │   └── disablePinFragment (DisablePinFragment)
│   ├── locationSettingsFragment (LocationSettingsFragment)
│   │   ├── mapStyleFragment (MapStyleFragment)
│   │   └── placesFragment (PlacesFragment)
│   ├── navigation2 (Nested Navigation - Other Settings)
│   │   └── otherSettingsFragment (OtherSettingsFragment) [START]
│   │       ├── permissionsFragment (PermissionsFragment)
│   │       ├── changesFragment (WhatsNewFragment)
│   │       ├── ossFragment (OssFragment)
│   │       ├── privacyPolicyFragment (PrivacyPolicyFragment)
│   │       └── termsFragment (TermsFragment)
│   ├── noteSettingsFragment (NoteSettingsFragment) [screen_title: String?]
│   ├── testsFragment (TestsFragment)
│   │   └── objectExportTestFragment (ObjectExportTestFragment)
│   ├── fragmentProVersion (FragmentProVersion)
│   └── fragmentTroubleshooting (FragmentSettingsTroubleshooting)
├── fragmentCloudDrives (FragmentCloudDrives)
└── changesFragment (WhatsNewFragment)
```

### 2. Events Section (`actionEvents`)

```
actionEvents (HomeEventsFragment)
├── groupsFragment (GroupsFragment)
├── archiveFragment (ArchiveFragment)
└── remindersSettingsFragment (RemindersSettingsFragment) [screen_title: String?]
    └── managePresetsFragment (ManagePresetsFragment)
```

### 3. Calendar Section (`actionCalendar`)

```
actionCalendar (CalendarFragment)
├── dayViewFragment (WeekViewFragment) [REQUIRED: date: Long] [DEEP LINK: reminderapp.com/calendar/{date}]
└── calendarSettingsFragment (CalendarSettingsFragment)
    └── fragmentEventsImport (FragmentEventsImport)
```

### 4. Notes Section (`actionNotes`)

```
actionNotes (NotesFragment)
├── archivedNotesFragment (ArchivedNotesFragment)
└── noteSettingsFragment (NoteSettingsFragment) [screen_title: String?]
```

### 5. Google Tasks Section (`actionGoogle`)

```
actionGoogle (GoogleTasksFragment)
└── taskListFragment (TaskListFragment) [REQUIRED: arg_id: String]
    └── previewGoogleTaskFragment (PreviewGoogleTaskFragment) [REQUIRED: item_id: String]
```

### 6. Standalone Fragments (No Parent in Navigation Graph)

These fragments are defined in the navigation graph but have no incoming actions from other fragments. They may be accessed programmatically or from other navigation graphs:

```
- groupsFragment (GroupsFragment)
- archiveFragment (ArchiveFragment)
- editBirthdayFragment (EditBirthdayFragment)
- previewBirthdayFragment (PreviewBirthdayFragment)
  └── editBirthdayFragment (EditBirthdayFragment)
- editGroupFragment (EditGroupFragment)
- editPlaceFragment (EditPlaceFragment)
- editGoogleTaskFragment (EditGoogleTaskFragment)
- editGoogleTaskListFragment (EditGoogleTaskListFragment)
- previewReminderFragment (PreviewReminderFragment)
- fullscreenMapFragment (FullscreenMapFragment)
- buildReminderFragment (BuildReminderFragment)
- previewNoteFragment (PreviewNoteFragment)
```

---

## Fragment Details with Parameters

### Fragments with Required Parameters

| Fragment ID | Fragment Class | Parameters | Type | Nullable | Notes |
|---|---|---|---|---|---|
| `dayViewFragment` | `WeekViewFragment` | `date` | `Long` | No | Day/week view for calendar |
| `taskListFragment` | `TaskListFragment` | `arg_id` | `String` | No | Google Task list ID |
| `previewGoogleTaskFragment` | `PreviewGoogleTaskFragment` | `item_id` | `String` | No | Google Task item ID |

### Fragments with Optional Parameters

| Fragment ID | Fragment Class | Parameters | Type | Nullable | Notes |
|---|---|---|---|---|---|
| `remindersSettingsFragment` | `RemindersSettingsFragment` | `screen_title` | `String` | Yes | Custom screen title |
| `noteSettingsFragment` | `NoteSettingsFragment` | `screen_title` | `String` | Yes | Custom screen title |

### Fragments with Deep Links

| Fragment ID | Deep Link URI | Parameters |
|---|---|---|
| `dayViewFragment` | `reminderapp.com/calendar/{date}` | `date: Long` |

---

## Navigation Flow Diagram

### Main Navigation Paths

#### 1. Settings Flow (Most Complex)
```
Home → Settings (Main Hub)
    ├─→ General Settings → UI Preview
    ├─→ Reminders Settings → Manage Presets
    ├─→ Export Settings → Cloud Drives
    ├─→ Notification Settings
    ├─→ Calendar Settings → Import Events
    ├─→ Birthday Settings
    ├─→ Security Settings
    │   ├─→ Add PIN
    │   ├─→ Change PIN
    │   └─→ Disable PIN
    ├─→ Location Settings
    │   ├─→ Map Style
    │   └─→ Places
    ├─→ Other Settings (Nested Graph)
    │   ├─→ Permissions
    │   ├─→ What's New
    │   ├─→ Open Source Licenses
    │   ├─→ Privacy Policy
    │   └─→ Terms & Conditions
    ├─→ Note Settings
    ├─→ Tests → Object Export Test
    ├─→ Pro Version
    └─→ Troubleshooting
```

#### 2. Events Management Flow
```
Events View
    ├─→ Groups
    ├─→ Archive (Trash)
    └─→ Reminders Settings → Manage Presets
```

#### 3. Calendar Flow
```
Calendar (Month View)
    ├─→ Day View [requires: date]
    └─→ Calendar Settings → Import Events
```

#### 4. Notes Flow
```
Notes List
    ├─→ Archived Notes
    └─→ Note Settings
```

#### 5. Google Tasks Flow
```
Google Tasks List
    └─→ Task List [requires: arg_id]
        └─→ Preview Task [requires: item_id]
```

---

## Fragment Categories

### By Function

#### **Home & Dashboard**
- `HomeFragment` - Main home screen
- `HomeEventsFragment` - Events timeline view

#### **Reminders & Events**
- `PreviewReminderFragment` - View reminder details
- `BuildReminderFragment` - Create/edit reminders
- `ArchiveFragment` - Deleted reminders (trash)
- `FullscreenMapFragment` - Map view for location reminders

#### **Calendar**
- `CalendarFragment` - Month view
- `WeekViewFragment` (dayViewFragment) - Week/day view

#### **Notes**
- `NotesFragment` - Active notes list
- `ArchivedNotesFragment` - Archived notes
- `PreviewNoteFragment` - View note details

#### **Google Tasks**
- `GoogleTasksFragment` - Task lists overview
- `TaskListFragment` - Individual task list
- `PreviewGoogleTaskFragment` - Task details
- `EditGoogleTaskFragment` - Create/edit task
- `EditGoogleTaskListFragment` - Create/edit task list

#### **Birthdays**
- `PreviewBirthdayFragment` - View birthday
- `EditBirthdayFragment` - Create/edit birthday

#### **Groups & Places**
- `GroupsFragment` - Reminder groups list
- `EditGroupFragment` - Create/edit group
- `PlacesFragment` - Saved places list
- `EditPlaceFragment` - Create/edit place

#### **Settings - General**
- `SettingsFragment` - Main settings hub
- `GeneralSettingsFragment` - General app settings
- `UiPreviewFragment` - Theme preview
- `NotificationSettingsFragment` - Notification settings

#### **Settings - Feature-Specific**
- `RemindersSettingsFragment` - Reminder settings
- `ManagePresetsFragment` - Recurrence presets
- `NoteSettingsFragment` - Note settings
- `CalendarSettingsFragment` - Calendar settings
- `FragmentEventsImport` - Import calendar events
- `BirthdaySettingsFragment` - Birthday settings

#### **Settings - Cloud & Export**
- `ExportSettingsFragment` - Backup settings
- `FragmentCloudDrives` - Cloud services management

#### **Settings - Security**
- `SecuritySettingsFragment` - Security hub
- `AddPinFragment` - Set up PIN
- `ChangePinFragment` - Change existing PIN
- `DisablePinFragment` - Remove PIN

#### **Settings - Location**
- `LocationSettingsFragment` - Location settings
- `MapStyleFragment` - Map appearance settings

#### **Settings - Other**
- `OtherSettingsFragment` - Miscellaneous settings
- `PermissionsFragment` - App permissions info
- `WhatsNewFragment` (changesFragment) - Changelog
- `OssFragment` - Open source licenses
- `PrivacyPolicyFragment` - Privacy policy
- `TermsFragment` - Terms and conditions
- `FragmentProVersion` - Pro version info
- `FragmentSettingsTroubleshooting` - Troubleshooting guide

#### **Testing**
- `TestsFragment` - Test menu
- `ObjectExportTestFragment` - Export testing

---

## Navigation Action Summary

### Total Actions by Source Fragment

| Source Fragment | Outgoing Actions | Destinations |
|---|---|---|
| `settingsFragment` | 12 | General, Reminders, Export, Notification, Calendar, Birthday, Security, Location, Other, Note Settings, Tests, Pro Version, Troubleshooting |
| `securitySettingsFragment` | 3 | Add PIN, Change PIN, Disable PIN |
| `otherSettingsFragment` | 5 | Permissions, Changes, OSS, Privacy Policy, Terms |
| `locationSettingsFragment` | 2 | Map Style, Places |
| `generalSettingsFragment` | 1 | UI Preview |
| `remindersSettingsFragment` | 1 | Manage Presets |
| `exportSettingsFragment` | 1 | Cloud Drives |
| `calendarSettingsFragment` | 1 | Events Import |
| `actionHome` | 3 | Settings, Cloud Drives, Changes |
| `actionEvents` | 3 | Groups, Archive, Reminders Settings |
| `actionCalendar` | 2 | Day View, Calendar Settings |
| `actionNotes` | 2 | Archived Notes, Note Settings |
| `actionGoogle` | 1 | Task List |
| `taskListFragment` | 1 | Preview Google Task |
| `testsFragment` | 1 | Object Export Test |
| `previewBirthdayFragment` | 1 | Edit Birthday |

---

## Key Insights

### 1. **Central Hub Pattern**
The `SettingsFragment` acts as the main hub with 12 outgoing navigation actions, making it the most connected fragment in the graph.

### 2. **Reusable Fragments**
Several fragments can be accessed from multiple places:
- `remindersSettingsFragment` - From Settings and Events view
- `fragmentCloudDrives` - From Settings and Home
- `changesFragment` (What's New) - From Settings and Home
- `noteSettingsFragment` - From Settings and Notes list

### 3. **Parameter Patterns**
- **Required IDs**: Used for viewing specific items (tasks, dates)
- **Optional Titles**: Used for customizing screen headers (`screen_title`)

### 4. **Orphan Fragments**
Several edit/preview fragments have no incoming actions in the navigation graph, suggesting they are invoked programmatically through other means (e.g., FAB clicks, list item clicks).

### 5. **Nested Navigation**
The app uses one nested navigation graph for "Other Settings" to organize related settings screens.

### 6. **Deep Linking**
Only the calendar day view supports deep linking, allowing external apps to open specific dates.

---

## Recommendations

### For Developers

1. **Missing Navigation Links**: The following fragments have no incoming navigation actions and may need to be added to the graph or documented:
   - `editBirthdayFragment` (only accessible from preview)
   - `editGroupFragment`
   - `editPlaceFragment`
   - `editGoogleTaskFragment`
   - `editGoogleTaskListFragment`
   - `previewReminderFragment`
   - `fullscreenMapFragment`
   - `buildReminderFragment`
   - `previewNoteFragment`

2. **Duplicate Fragment ID**: `changesFragment` is defined twice in the navigation graph (once at root level, once inside the nested navigation). This should be resolved.

3. **Consider More Deep Links**: Add deep links for:
   - Specific reminders
   - Specific notes
   - Specific Google Tasks

4. **Parameter Documentation**: Document the expected format/range for parameters like `date` (Long timestamp) and `arg_id`/`item_id` format.

---

## Animation Schemes

The app uses two main animation schemes:

### 1. Slide from Top (Settings & Modals)
- Enter: `fragment_slide_top`
- Exit: `fragment_wait`
- Pop Enter: `fragment_wait`
- Pop Exit: `fragment_slide_bottom`

**Used for:**
- Settings access from Home
- Cloud Drives from Home
- What's New from Home
- Note Settings from Notes

### 2. Default Navigation (Standard Flow)
- Enter: `nav_default_enter_anim`
- Exit: `nav_default_exit_anim`
- Pop Enter: `nav_default_pop_enter_anim`
- Pop Exit: `nav_default_pop_exit_anim`

**Used for:**
- All other navigation actions

---

**End of Document**

