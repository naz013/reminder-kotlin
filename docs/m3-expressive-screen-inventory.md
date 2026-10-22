# M3 Expressive — Screen Inventory

Per-screen status for the M3 Expressive update. Conventions are in
[`m3-expressive-adoption.md`](m3-expressive-adoption.md); the audit checklist is in
[`m3-expressive-guidelines.md`](m3-expressive-guidelines.md). Update the **Status** column when a screen
changes, and add a row when a new user-navigable screen is added.

Scope: screens reachable through app navigation (Nav3 graphs, mostly one per `feature:*` module — see
[`architecture.md`](architecture.md)), plus Activity-hosted Compose screens in `ui-common` and `appwidgets`.
Excludes dialogs and bottom sheets, intent-forwarding shells with no UI, and debug/admin-only modules
(`reviewsadmin`, `cloudtestadmin`).

**Status values:** Not started, Audited (reviewed, no code changed), In progress, Done (fully reflects the
`ui-common` expressive foundation). Every row below is currently Done.

## Home / Events

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Home | Compose | `feature/feature-home/.../feature/home/HomeScreen.kt`, `.../ChronologicalHomeScreen.kt` | Done |
| Agenda | Compose | `feature/feature-agenda/.../feature/agenda/AgendaScreen.kt` | Done |

## Reminders

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Reminder Editor (Build Reminder) | Compose | `feature/feature-reminder/.../feature/reminder/build/BuildReminderScreen.kt` | Done |
| Reminder Help | Compose | `feature/feature-reminder/.../feature/reminder/build/help/ReminderHelpScreen.kt` | Done |
| Recurrence Help | Compose | `feature/feature-reminder/.../feature/reminder/recur/RecurHelpScreen.kt` | Done |
| Select Application | Compose | `feature/feature-reminder/.../feature/reminder/apps/SelectApplicationScreen.kt` | Done |
| Map Value Editor | Compose | `feature/feature-reminder/.../feature/reminder/build/valuedialog/editor/MapEditorScreen.kt` | Done |
| Reminder Preview | Compose | `feature/feature-reminder/.../feature/reminder/preview/PreviewReminderScreen.kt` | Done |
| Reminder Fullscreen Map | Compose | `feature/feature-reminder/.../feature/reminder/preview/ReminderFullscreenMapScreen.kt` | Done |
| Reminders Archive | Compose | `feature/feature-reminder/.../feature/reminder/lists/removed/RemindersArchiveScreen.kt` | Done |
| Reminder Action (alarm/ringing) | Compose (Activity-hosted) | Screen: `feature/feature-reminder/.../feature/reminder/dialog/ReminderActionScreen.kt`; Activity: `app/src/main/java/com/elementary/tasks/reminder/dialog/ReminderActionActivity.kt` | Done |
| Todo Editor | Compose | `feature/feature-reminder/.../feature/reminder/todo/TodoEditScreen.kt` | Done |

## Notes

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Notes List | Compose | `feature/feature-note/.../feature/note/list/NotesScreen.kt` | Done |
| Notes Archive | Compose | `feature/feature-note/.../feature/note/NotesNavGraph.kt` (`NotesArchiveEntry`, reuses `NotesScreen`) | Done |
| Note Editor | Compose | `feature/feature-note/.../feature/note/create/NoteEditScreen.kt` | Done |
| Note Preview | Compose | `feature/feature-note/.../feature/note/preview/PreviewNoteScreen.kt` | Done |
| Note Image Preview | Compose | `feature/feature-note/.../feature/note/preview/ImagePreviewScreen.kt` | Done |

## Birthdays

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Birthdays List | Compose | `feature/feature-birthday/.../feature/birthday/list/BirthdaysScreen.kt` | Done |
| Birthday Editor | Compose | `feature/feature-birthday/.../feature/birthday/create/EditBirthdayScreen.kt` | Done |
| Birthday Preview | Compose | `feature/feature-birthday/.../feature/birthday/preview/PreviewBirthdayScreen.kt` | Done |
| Birthday Action (alarm/ringing) | Compose (Activity-hosted) | Screen: `feature/feature-birthday/.../feature/birthday/dialog/BirthdayActionScreen.kt`; Activity: `app/src/main/java/com/elementary/tasks/birthdays/dialog/BirthdayActionActivity.kt` | Done |

## Groups / Tags / Places

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Groups List | Compose | `feature/feature-group/.../group/list/GroupsScreen.kt` | Done |
| Group Details | Compose | `feature/feature-group/.../group/details/GroupDetailsScreen.kt` | Done |
| Group Editor | Compose | `feature/feature-group/.../group/create/EditGroupScreen.kt` | Done |
| Tags Manage | Compose | `feature/feature-tags/.../tags/compose/TagsScreen.kt` | Done |
| Tag Editor | Compose | `feature/feature-tags/.../tags/compose/TagEditScreen.kt` | Done |
| Tag Details | Compose | `feature/feature-tags/.../tags/details/TagDetailsScreen.kt` | Done |
| Places List | Compose | `feature/feature-places/.../feature/places/list/PlacesScreen.kt` | Done |
| Place Editor | Compose | `feature/feature-places/.../feature/places/create/EditPlaceScreen.kt` | Done |

## Calendar

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Calendar (Month) | Compose | `feature/feature-calendar/.../feature/calendar/monthview/CalendarScreen.kt` | Done |
| Calendar Timeline (Day / 3-day / 7-day) | Compose | `feature/feature-calendar/.../feature/calendar/timeline/TimelineScreen.kt` | Done |
| Google Calendar Event Preview | Compose | `feature/feature-calendar/.../feature/calendar/preview/GoogleCalendarEventPreviewScreen.kt` | Done |

## Google Tasks

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Google Task Lists | Compose | `feature/feature-googletask/.../feature/googletask/GoogleTasksScreen.kt` | Done |
| Task List (tasks in a list) | Compose | `feature/feature-googletask/.../feature/googletask/TaskListScreen.kt` | Done |
| Task Preview | Compose | `feature/feature-googletask/.../feature/googletask/preview/PreviewGoogleTaskScreen.kt` | Done |
| Task Editor | Compose | `feature/feature-googletask/.../feature/googletask/task/EditGoogleTaskScreen.kt` | Done |
| Task List Editor | Compose | `feature/feature-googletask/.../feature/googletask/tasklist/EditGoogleTaskListScreen.kt` | Done |

## Workflow (automation rules)

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Workflow Gallery | Compose | `feature/feature-workflow/.../feature/workflow/WorkflowGalleryScreen.kt` | Done |
| Workflow Rules for Group | Compose | `feature/feature-workflow/.../feature/workflow/WorkflowRulesForGroupScreen.kt` | Done |
| Workflow Rules for Reminder | Compose | `feature/feature-workflow/.../feature/workflow/WorkflowRulesForReminderScreen.kt` | Done |
| Workflow Rule Builder | Compose | `feature/feature-workflow/.../feature/workflow/builder/WorkflowRuleBuilderScreen.kt` | Done |

## Routines

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Routines List | Compose | `feature/feature-routine/.../feature/routine/list/RoutinesListScreen.kt` | Done |
| Routine Editor | Compose | `feature/feature-routine/.../feature/routine/edit/RoutineEditScreen.kt` | Done |
| Routine Preview | Compose | `feature/feature-routine/.../feature/routine/preview/RoutinePreviewScreen.kt` | Done |
| Routine Execution | Compose | `feature/feature-routine/.../feature/routine/execution/RoutineExecutionScreen.kt` | Done |

## Settings

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Settings Hub | Compose | `feature/feature-settings/.../feature/settings/SettingsHubScreen.kt` | Done |
| General Settings | Compose | `feature/feature-settings/.../feature/settings/general/GeneralSettingsScreen.kt` | Done |
| Backup Settings | Compose | `feature/feature-settings/.../feature/settings/backup/BackupSettingsScreen.kt` | Done |
| Reminders Settings | Compose | `feature/feature-reminder/.../feature/reminder/settings/RemindersSettingsScreen.kt` (moved to feature-reminder) | Done |
| Manage Presets | Compose | `feature/feature-reminder/.../feature/reminder/settings/ManagePresetsScreen.kt` (moved to feature-reminder) | Done |
| Notification Customization Help | Compose | `feature/feature-reminder/.../feature/reminder/settings/help/NotificationCustomizationHelpScreen.kt` (moved to feature-reminder) | Done |
| Calendar Settings | Compose | `feature/feature-settings/.../feature/settings/calendar/CalendarSettingsScreen.kt` | Done |
| Select Holiday Country | Compose | `feature/feature-settings/.../feature/settings/calendar/country/HolidayCountryScreen.kt` | Done |
| Birthday Settings | Compose | `feature/feature-birthday/.../feature/birthday/settings/BirthdaySettingsScreen.kt` (moved to feature-birthday) | Done |
| Note Settings | Compose | `feature/feature-settings/.../feature/settings/NoteSettingsScreen.kt` | Done |
| Location Settings | Compose | `feature/feature-settings/.../feature/settings/location/LocationSettingsScreen.kt` | Done |
| Map Style | Compose | `feature/feature-settings/.../feature/settings/location/MapStyleScreen.kt` | Done |
| Security Settings | Compose | `feature/feature-settings/.../feature/settings/security/SecuritySettingsScreen.kt` | Done |
| Add PIN | Compose | `feature/feature-settings/.../feature/settings/security/AddPinScreen.kt` | Done |
| Change PIN | Compose | `feature/feature-settings/.../feature/settings/security/ChangePinScreen.kt` | Done |
| Disable PIN | Compose | `feature/feature-settings/.../feature/settings/security/DisablePinScreen.kt` | Done |
| Cloud Backup Settings | Compose | `feature/feature-settings/.../feature/settings/export/CloudBackupSettingsScreen.kt` | Done |
| Cloud Services (connect) | Compose | `feature/feature-settings/.../feature/settings/export/services/CloudServicesScreen.kt` | Done |
| Other Settings | Compose | `feature/feature-settings/.../feature/settings/other/OtherSettingsScreen.kt` | Done |
| Permissions | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`PermissionsEntry`) | Done |
| Open Source Licenses | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`OssEntry`, `SettingsWebView`) | Done |
| Privacy Policy | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`PrivacyPolicyEntry`, `SettingsWebView`) | Done |
| Terms of Service | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`TermsEntry`, `SettingsWebView`) | Done |
| What's New | Compose | `feature/feature-settings/.../feature/settings/other/whatsnew/WhatsNewScreen.kt` | Done |
| Gemini Functions (App Functions) | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`GeminiFunctionsEntry`) | Done |
| AI Digest Settings | Compose | `feature/feature-settings/.../feature/settings/digest/DigestSettingsScreen.kt` | Done |
| Header Items Settings | Compose | `feature/feature-settings/.../feature/settings/headeritems/HeaderItemsSettingsScreen.kt` | Done |
| Troubleshooting | Compose | `feature/feature-settings/.../feature/settings/troubleshooting/TroubleshootingScreen.kt` | Done |
| Pro Version | Compose | `feature/feature-settings/.../feature/settings/proversion/ProVersionScreen.kt` | Done |
| Developer (debug tools) | Compose | `feature/feature-settings/.../feature/settings/debug/DeveloperScreen.kt` | Out of scope (debug-only) |
| Object Export (debug) | Compose | `feature/feature-settings/.../feature/settings/debug/ObjectExportScreen.kt` | Out of scope (debug-only) |

## Backup / Insights (PRO)

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Local Backup (Export) | Compose | `extensions/localbackup/.../localbackup/compose/LocalBackupScreen.kt` | Done |
| Local Backup (Import) | Compose | same file, `LocalBackupNavKey.Import` | Done |
| Insights Dashboard | Compose | `feature/feature-insights/.../insights/compose/InsightsScreen.kt` | Done |

## App Shell (Splash)

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Bottom Nav Splash | Compose | `app/.../navigation/BottomNavSplashScreen.kt`, `AppLauncherIcon.kt` | Done |

## Onboarding / Login

| Screen | Type | File(s) | Status |
|---|---|---|---|
| PIN Login | Compose (Activity-hosted) | `ui/ui-common/.../ui/common/login/PinLoginScreen.kt`, `PinLoginActivity.kt` | Done |
| Onboarding | Compose | `feature/feature-onboarding/.../onboarding/compose/OnboardingScreen.kt` | Done |

## Widget Configuration (appwidgets module)

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Single Note Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../singlenote/SingleNoteWidgetConfigScreen.kt` | Done |
| Notes Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../notes/NotesWidgetConfigScreen.kt` | Done |
| Calendar Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../calendar/CalendarWidgetConfigScreen.kt` | Done |
| Events Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../events/EventsWidgetConfigScreen.kt` | Done |
| Birthdays Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../birthdays/BirthdaysWidgetConfigScreen.kt` | Done |
| Combined Buttons Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../combinedbuttons/CombinedWidgetConfigScreen.kt` | Done |
| Google Tasks Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../googletasks/TasksWidgetConfigScreen.kt` | Done |

## Notes on scope

- **Alarm/trigger screens** (Reminder Action, Birthday Action) are full-screen but reached via
  notification/alarm rather than in-app nav — style them consistently, but they're lower priority since
  they're seen far less often than list/editor screens. Their hosting Activities remain in `app` even
  though the screen composables themselves moved into their feature modules.
- **WebView-hosted screens** (Licenses, Privacy Policy, Terms) render remote/static HTML — expressive work
  here is limited to the surrounding Compose chrome (top bar, loading state), not the web content itself.
- **Developer / Object Export** are internal debug tools, out of scope for design polish.
- Widget *configuration* screens (this table) are regular in-app Compose Activities, distinct from the
  actual home-screen widgets themselves (Glance-based `RemoteViews`, tracked separately — see the
  `feature/REM-1082_Rewrite_widgets_with_compose` branch work, not part of this document).
- `app/src/main/java/com/elementary/tasks/navigation/BottomNavSplashScreen.kt` still lives in the legacy
  `app` module — it's an internal splash/loading composable inside `BottomNavActivity`, not a distinct
  user-navigable destination, so it isn't tracked as its own row here.
