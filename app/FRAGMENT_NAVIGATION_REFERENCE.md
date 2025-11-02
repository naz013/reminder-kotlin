# Fragment Navigation Quick Reference

**App Module Navigation Graph Analysis**
**File:** `app/src/main/res/navigation/home_nav.xml`
**Date:** 2025-11-02

---

## Quick Stats

| Metric | Count |
|--------|-------|
| Total Fragments | 47 |
| Top-Level Fragments (Bottom Nav) | 5 |
| Navigation Actions | 40 |
| Fragments with Parameters | 5 |
| Required Parameters | 3 |
| Optional Parameters | 2 |
| Deep Links | 1 |
| Nested Navigation Graphs | 1 |

---

## All Fragments Alphabetical Reference

| Fragment ID | Class Name | Package | Parameters | Incoming Actions | Outgoing Actions |
|-------------|------------|---------|------------|------------------|------------------|
| `actionCalendar` | `CalendarFragment` | `calendar.monthview` | None | Bottom Nav | 2 |
| `actionEvents` | `HomeEventsFragment` | `home.eventsview` | None | Bottom Nav | 3 |
| `actionGoogle` | `GoogleTasksFragment` | `googletasks` | None | Bottom Nav | 1 |
| `actionHome` | `HomeFragment` | `home` | None | Bottom Nav (START) | 3 |
| `actionNotes` | `NotesFragment` | `notes.list` | None | Bottom Nav | 2 |
| `addPinFragment` | `AddPinFragment` | `settings.security` | None | 1 | 0 |
| `archiveFragment` | `ArchiveFragment` | `reminder.lists.removed` | None | 1 | 0 |
| `archivedNotesFragment` | `ArchivedNotesFragment` | `notes.list.archived` | None | 1 | 0 |
| `birthdaySettingsFragment` | `BirthdaySettingsFragment` | `settings` | None | 1 | 0 |
| `buildReminderFragment` | `BuildReminderFragment` | `reminder.build` | None | 0 (Orphan) | 0 |
| `calendarSettingsFragment` | `CalendarSettingsFragment` | `settings.calendar` | None | 2 | 1 |
| `changePinFragment` | `ChangePinFragment` | `settings.security` | None | 1 | 0 |
| `changesFragment` | `WhatsNewFragment` | `settings.other` | None | 3 (DUPLICATE) | 0 |
| `dayViewFragment` | `WeekViewFragment` | `calendar.dayview` | **date: Long** | 1 | 0 |
| `disablePinFragment` | `DisablePinFragment` | `settings.security` | None | 1 | 0 |
| `editBirthdayFragment` | `EditBirthdayFragment` | `birthdays.create` | None | 1 | 0 |
| `editGoogleTaskFragment` | `EditGoogleTaskFragment` | `googletasks.task` | None | 0 (Orphan) | 0 |
| `editGoogleTaskListFragment` | `EditGoogleTaskListFragment` | `googletasks.tasklist` | None | 0 (Orphan) | 0 |
| `editGroupFragment` | `EditGroupFragment` | `groups.create` | None | 0 (Orphan) | 0 |
| `editPlaceFragment` | `EditPlaceFragment` | `places.create` | None | 0 (Orphan) | 0 |
| `exportSettingsFragment` | `ExportSettingsFragment` | `settings.export` | None | 1 | 1 |
| `fragmentCloudDrives` | `FragmentCloudDrives` | `settings.export` | None | 3 | 0 |
| `fragmentEventsImport` | `FragmentEventsImport` | `settings.calendar` | None | 2 | 0 |
| `fragmentProVersion` | `FragmentProVersion` | `settings` | None | 1 | 0 |
| `fragmentTroubleshooting` | `FragmentSettingsTroubleshooting` | `settings.troubleshooting` | None | 1 | 0 |
| `fullscreenMapFragment` | `FullscreenMapFragment` | `reminder.preview` | None | 0 (Orphan) | 0 |
| `generalSettingsFragment` | `GeneralSettingsFragment` | `settings.general` | None | 1 | 1 |
| `groupsFragment` | `GroupsFragment` | `groups.list` | None | 1 | 0 |
| `locationSettingsFragment` | `LocationSettingsFragment` | `settings.location` | None | 1 | 2 |
| `managePresetsFragment` | `ManagePresetsFragment` | `settings.reminders` | None | 2 | 0 |
| `mapStyleFragment` | `MapStyleFragment` | `settings.location` | None | 1 | 0 |
| `noteSettingsFragment` | `NoteSettingsFragment` | `settings` | screen_title?: String | 3 | 0 |
| `notificationSettingsFragment` | `NotificationSettingsFragment` | `settings` | None | 1 | 0 |
| `objectExportTestFragment` | `ObjectExportTestFragment` | `settings.test` | None | 1 | 0 |
| `ossFragment` | `OssFragment` | `settings.other` | None | 1 | 0 |
| `otherSettingsFragment` | `OtherSettingsFragment` | `settings.other` | None | 1 (Nested Start) | 5 |
| `permissionsFragment` | `PermissionsFragment` | `settings.other` | None | 1 | 0 |
| `placesFragment` | `PlacesFragment` | `places.list` | None | 1 | 0 |
| `previewBirthdayFragment` | `PreviewBirthdayFragment` | `birthdays.preview` | None | 0 (Orphan) | 1 |
| `previewGoogleTaskFragment` | `PreviewGoogleTaskFragment` | `googletasks.preview` | **item_id: String** | 1 | 0 |
| `previewNoteFragment` | `PreviewNoteFragment` | `notes.preview` | None | 0 (Orphan) | 0 |
| `previewReminderFragment` | `PreviewReminderFragment` | `reminder.preview` | None | 0 (Orphan) | 0 |
| `privacyPolicyFragment` | `PrivacyPolicyFragment` | `settings.other` | None | 1 | 0 |
| `remindersSettingsFragment` | `RemindersSettingsFragment` | `settings.reminders` | screen_title?: String | 2 | 1 |
| `settingsFragment` | `SettingsFragment` | `settings` | None | 1 | 12 |
| `taskListFragment` | `TaskListFragment` | `googletasks.list` | **arg_id: String** | 1 | 1 |
| `termsFragment` | `TermsFragment` | `settings.other` | None | 1 | 0 |
| `testsFragment` | `TestsFragment` | `settings.test` | None | 1 | 1 |
| `uiPreviewFragment` | `UiPreviewFragment` | `settings.general` | None | 1 | 0 |

---

## Fragments by Incoming Actions

### Multiple Entry Points (3+ incoming actions)

| Fragment | Incoming From | Total |
|----------|---------------|-------|
| `fragmentCloudDrives` | Home, Settings/Export, Events | 3 |
| `changesFragment` | Home, Settings/Other, Events | 3 |
| `noteSettingsFragment` | Settings, Notes, Events | 3 |

### Two Entry Points

| Fragment | Incoming From | Total |
|----------|---------------|-------|
| `remindersSettingsFragment` | Settings, Events | 2 |
| `calendarSettingsFragment` | Settings, Calendar | 2 |
| `fragmentEventsImport` | Calendar Settings, Settings/Calendar | 2 |
| `managePresetsFragment` | Reminders Settings (2 paths) | 2 |

### One Entry Point (Standard)

Most fragments (32 total) have exactly one incoming navigation action.

### No Entry Points (Orphan Fragments)

These 9 fragments have no explicit navigation actions pointing to them:

1. `buildReminderFragment` - Create reminder
2. `editBirthdayFragment` - Edit birthday (except from preview)
3. `editGoogleTaskFragment` - Edit Google task
4. `editGoogleTaskListFragment` - Edit task list
5. `editGroupFragment` - Edit group
6. `editPlaceFragment` - Edit place
7. `fullscreenMapFragment` - Map fullscreen
8. `previewBirthdayFragment` - Preview birthday
9. `previewNoteFragment` - Preview note
10. `previewReminderFragment` - Preview reminder

---

## Fragments by Outgoing Actions

### High Complexity (10+ actions)

| Fragment | Destinations | Description |
|----------|--------------|-------------|
| `settingsFragment` | 12 | Main settings hub |

### Medium Complexity (3-5 actions)

| Fragment | Destinations | Description |
|----------|--------------|-------------|
| `otherSettingsFragment` | 5 | Other settings sub-menu |
| `securitySettingsFragment` | 3 | PIN management |
| `actionHome` | 3 | Home screen |
| `actionEvents` | 3 | Events timeline |

### Low Complexity (2 actions)

| Fragment | Destinations | Description |
|----------|--------------|-------------|
| `actionCalendar` | 2 | Calendar view |
| `actionNotes` | 2 | Notes list |
| `locationSettingsFragment` | 2 | Location settings |

### Simple Navigation (1 action)

15 fragments have exactly one outgoing navigation action.

### Terminal Fragments (0 actions)

26 fragments have no outgoing navigation actions (leaf nodes in the navigation tree).

---

## Parameter Details

### Required Parameters

| Fragment | Parameter Name | Type | Description | Deep Link |
|----------|---------------|------|-------------|-----------|
| `dayViewFragment` | `date` | Long | Unix timestamp | reminderapp.com/calendar/{date} |
| `taskListFragment` | `arg_id` | String | Google Task list ID | No |
| `previewGoogleTaskFragment` | `item_id` | String | Google Task item ID | No |

### Optional Parameters

| Fragment | Parameter Name | Type | Default | Usage |
|----------|---------------|------|---------|-------|
| `remindersSettingsFragment` | `screen_title` | String? | null | Custom title |
| `noteSettingsFragment` | `screen_title` | String? | null | Custom title |

---

## Navigation Paths from Start Destination

### Level 0: Start
- `actionHome` (HomeFragment)

### Level 1: Direct from Home
- `settingsFragment`
- `fragmentCloudDrives`
- `changesFragment`

### Level 2: From Settings
- `generalSettingsFragment`
- `remindersSettingsFragment`
- `exportSettingsFragment`
- `notificationSettingsFragment`
- `calendarSettingsFragment`
- `birthdaySettingsFragment`
- `securitySettingsFragment`
- `locationSettingsFragment`
- `navigation2` (Other Settings)
- `noteSettingsFragment`
- `testsFragment`
- `fragmentProVersion`
- `fragmentTroubleshooting`

### Level 3: Deeper Navigation
Multiple fragments accessible through Level 2 fragments.

### Maximum Depth
**5 levels** from start destination:
1. Home
2. Settings
3. Security Settings
4. Change PIN
5. (Terminal)

---

## Deep Link Reference

| URI Pattern | Fragment | Parameter | Type | Description |
|-------------|----------|-----------|------|-------------|
| `reminderapp.com/calendar/{date}` | `dayViewFragment` | date | Long | Open calendar day view |

---

## Animation Reference

### Slide from Top (4 usages)
Used for modal-like navigation (Settings, Cloud, What's New)

**Animations:**
- Enter: `fragment_slide_top`
- Exit: `fragment_wait`
- Pop Enter: `fragment_wait`
- Pop Exit: `fragment_slide_bottom`

**Used by:**
- `action_actionHome_to_settingsFragment`
- `action_actionHome_to_cloudDrives`
- `action_actionHome_to_changesFragment`
- `action_actionNotes_to_noteSettingsFragment`

### Default Navigation (36 usages)
Standard navigation for all other transitions

**Animations:**
- Enter: `nav_default_enter_anim`
- Exit: `nav_default_exit_anim`
- Pop Enter: `nav_default_pop_enter_anim`
- Pop Exit: `nav_default_pop_exit_anim`

---

## Known Issues

### 1. Duplicate Fragment ID
**Fragment:** `changesFragment`
**Issue:** Defined twice in navigation graph (root level + nested navigation)
**Impact:** Potential navigation conflicts
**Recommendation:** Remove one definition or rename

### 2. Orphan Fragments
**Count:** 9 fragments
**Issue:** No incoming navigation actions
**Impact:** Not discoverable through navigation graph
**Recommendation:** Add actions or document programmatic access

### 3. Missing Deep Links
**Opportunity:** Add deep links for:
- Specific reminders
- Specific notes
- Specific birthdays
- Specific Google tasks

---

## Package Organization

| Package | Fragment Count | Purpose |
|---------|----------------|---------|
| `settings` | 14 | Settings screens |
| `settings.other` | 5 | Misc settings |
| `settings.security` | 4 | Security & PIN |
| `settings.reminders` | 2 | Reminder settings |
| `settings.calendar` | 2 | Calendar settings |
| `settings.export` | 2 | Backup & cloud |
| `settings.location` | 2 | Location settings |
| `settings.general` | 2 | General settings |
| `settings.test` | 2 | Testing tools |
| `settings.troubleshooting` | 1 | Help & support |
| `googletasks` | 5 | Google Tasks |
| `reminder` | 3 | Reminders |
| `notes` | 3 | Notes |
| `calendar` | 2 | Calendar views |
| `birthdays` | 2 | Birthday management |
| `groups` | 2 | Reminder groups |
| `places` | 2 | Location places |
| `home` | 2 | Home screens |

---

## Testing Checklist

Use this checklist to verify navigation:

### Bottom Navigation
- [ ] Home tab loads correctly
- [ ] Events tab loads correctly
- [ ] Calendar tab loads correctly
- [ ] Notes tab loads correctly
- [ ] Google Tasks tab loads correctly

### Main Flows
- [ ] Settings accessible from Home
- [ ] All 12 settings destinations work
- [ ] Back navigation works correctly
- [ ] Deep link to calendar opens correct date
- [ ] Parameters passed correctly to fragments

### Edge Cases
- [ ] Optional parameters work with null values
- [ ] Required parameters validate input
- [ ] Orphan fragments accessible programmatically
- [ ] Duplicate `changesFragment` doesn't cause conflicts

---

**Document Generated:** 2025-11-02
**Navigation Graph:** `home_nav.xml`
**Total Fragments Analyzed:** 47
**Total Navigation Actions:** 40

