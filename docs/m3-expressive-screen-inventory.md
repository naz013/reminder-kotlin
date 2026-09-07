# M3 Expressive — Screen Inventory & Migration Status

Companion tracking table to [`m3-expressive-adoption.md`](m3-expressive-adoption.md). Lists every
user-navigable screen in the app and its status toward the M3 Expressive design update. Update the
**Status** column as work lands — this file is the source of truth for "what's left."

To audit a screen (i.e. decide "Not started" → "Audited" or find what's missing for "In progress" →
"Done"), use the spec digest and per-screen checklist in
[`m3-expressive-guidelines.md`](m3-expressive-guidelines.md).

Scope: screens reachable through app navigation (Nav3 graphs, mostly one per `feature:*` module — see
`docs/architecture.md` for the module map), plus Activity-hosted Compose screens in `ui-common` and
`appwidgets`. Excludes dialogs/bottom sheets, intent-forwarding shells with no UI of their own, and
debug/admin-only modules (`reviewsadmin`, `cloudtestadmin`) per [`CLAUDE.md`](../CLAUDE.md). The app is
almost entirely Jetpack Compose already — there are no remaining XML/View-based full screens
(`app/src/main/res/layout/` has only a dialog layout and a notification `RemoteViews` layout, not
screens).

**Note on paths:** almost every screen listed here was extracted from `app` into a dedicated
`feature:feature-*` module since this doc was first written — paths below are the current,
verified locations (re-audited in full; see git history of this file for the prior, now-stale
`app/src/main/java/com/elementary/tasks/**`-rooted table).

**Status values:**

| Status | Meaning |
|---|---|
| Not started | No expressive-specific work done. Default for everything not listed otherwise. |
| Audited | Reviewed against the expressive tactics / gaps documented, no code changed yet. |
| In progress | Type/shape/color/motion changes landed for part of the screen. |
| Done | Screen fully reflects the `ui-common` expressive foundation (§3 of the adoption plan). |

## Home / Events

First screens targeted — see [adoption plan §2–4](m3-expressive-adoption.md#2-where-this-repo-already-stands)
for the detailed gap analysis and plan behind these two.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Home | Compose | `feature/feature-home/.../feature/home/HomeScreen.kt`, `.../ChronologicalHomeScreen.kt` | In progress |
| Agenda | Compose | `feature/feature-agenda/.../feature/agenda/AgendaScreen.kt` | In progress |

## Reminders

Audited in full — see [`m3-expressive-adoption.md` §6](m3-expressive-adoption.md#6-reminders-screens--audit)
for cross-cutting patterns (missing back-button content descriptions, off-scale hardcoded shapes, manual
`FontWeight` overrides vs. emphasized type, etc.) and per-screen findings. No code changed yet — all rows
below stay "Audited" (not "In progress") until a fix actually lands.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Reminder Editor (Build Reminder) | Compose | `feature/feature-reminder/.../feature/reminder/build/BuildReminderScreen.kt` | Audited |
| Reminder Help | Compose | `feature/feature-reminder/.../feature/reminder/build/help/ReminderHelpScreen.kt` | Audited |
| Recurrence Help | Compose | `feature/feature-reminder/.../feature/reminder/recur/RecurHelpScreen.kt` | Audited |
| Select Application | Compose | `feature/feature-reminder/.../feature/reminder/apps/SelectApplicationScreen.kt` | Audited |
| Map Value Editor | Compose | `feature/feature-reminder/.../feature/reminder/build/valuedialog/editor/MapEditorScreen.kt` | Audited |
| Reminder Preview | Compose | `feature/feature-reminder/.../feature/reminder/preview/PreviewReminderScreen.kt` | Audited |
| Reminder Fullscreen Map | Compose | `feature/feature-reminder/.../feature/reminder/preview/ReminderFullscreenMapScreen.kt` | Audited |
| Reminders Archive | Compose | `feature/feature-reminder/.../feature/reminder/lists/removed/RemindersArchiveScreen.kt` | Audited |
| Reminder Action (alarm/ringing) | Compose (Activity-hosted) | Screen: `feature/feature-reminder/.../feature/reminder/dialog/ReminderActionScreen.kt`; Activity: `app/src/main/java/com/elementary/tasks/reminder/dialog/ReminderActionActivity.kt` | Audited |
| Todo Editor | Compose | `feature/feature-reminder/.../feature/reminder/todo/TodoEditScreen.kt` | Audited |

## Notes

Audited — see [`m3-expressive-adoption.md` §8](m3-expressive-adoption.md#8-notes--birthdays-screens--audit).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Notes List | Compose | `feature/feature-note/.../feature/note/list/NotesScreen.kt` | Audited |
| Notes Archive | Compose | `feature/feature-note/.../feature/note/NotesNavGraph.kt` (`NotesArchiveEntry`, reuses `NotesScreen`) | Audited |
| Note Editor | Compose | `feature/feature-note/.../feature/note/create/NoteEditScreen.kt` | Audited |
| Note Preview | Compose | `feature/feature-note/.../feature/note/preview/PreviewNoteScreen.kt` | Audited |
| Note Image Preview | Compose | `feature/feature-note/.../feature/note/preview/ImagePreviewScreen.kt` | Audited |

## Birthdays

Audited — see [`m3-expressive-adoption.md` §8](m3-expressive-adoption.md#8-notes--birthdays-screens--audit).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Birthdays List | Compose | `feature/feature-birthday/.../feature/birthday/list/BirthdaysScreen.kt` | Audited |
| Birthday Editor | Compose | `feature/feature-birthday/.../feature/birthday/create/EditBirthdayScreen.kt` | Audited |
| Birthday Preview | Compose | `feature/feature-birthday/.../feature/birthday/preview/PreviewBirthdayScreen.kt` | Audited |
| Birthday Action (alarm/ringing) | Compose (Activity-hosted) | Screen: `feature/feature-birthday/.../feature/birthday/dialog/BirthdayActionScreen.kt`; Activity: `app/src/main/java/com/elementary/tasks/birthdays/dialog/BirthdayActionActivity.kt` | Audited |

## Groups / Tags / Places

Audited — see [`m3-expressive-adoption.md` §9](m3-expressive-adoption.md#9-groups--tags--places-screens--audit).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Groups List | Compose | `feature/feature-group/.../group/list/GroupsScreen.kt` | Audited |
| Group Details | Compose | `feature/feature-group/.../group/details/GroupDetailsScreen.kt` | Audited |
| Group Editor | Compose | `feature/feature-group/.../group/create/EditGroupScreen.kt` | Audited |
| Tags Manage | Compose | `feature/feature-tags/.../tags/compose/TagsScreen.kt` | Audited |
| Tag Editor | Compose | `feature/feature-tags/.../tags/compose/TagEditScreen.kt` | Audited |
| Tag Details | Compose | `feature/feature-tags/.../tags/details/TagDetailsScreen.kt` | Audited |
| Places List | Compose | `feature/feature-places/.../feature/places/list/PlacesScreen.kt` | Audited |
| Place Editor | Compose | `feature/feature-places/.../feature/places/create/EditPlaceScreen.kt` | Audited |

## Calendar

Audited — see [`m3-expressive-adoption.md` §10](m3-expressive-adoption.md#10-calendar--google-tasks-screens--audit).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Calendar (Month) | Compose | `feature/feature-calendar/.../feature/calendar/monthview/CalendarScreen.kt` | Audited |
| Calendar Timeline (Day / 3-day / 7-day) | Compose | `feature/feature-calendar/.../feature/calendar/timeline/TimelineScreen.kt` | Audited |
| Google Calendar Event Preview | Compose | `feature/feature-calendar/.../feature/calendar/preview/GoogleCalendarEventPreviewScreen.kt` | Audited |

## Google Tasks

Audited — see [`m3-expressive-adoption.md` §10](m3-expressive-adoption.md#10-calendar--google-tasks-screens--audit).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Google Task Lists | Compose | `feature/feature-googletask/.../feature/googletask/GoogleTasksScreen.kt` | Audited |
| Task List (tasks in a list) | Compose | `feature/feature-googletask/.../feature/googletask/TaskListScreen.kt` | Audited |
| Task Preview | Compose | `feature/feature-googletask/.../feature/googletask/preview/PreviewGoogleTaskScreen.kt` | Audited |
| Task Editor | Compose | `feature/feature-googletask/.../feature/googletask/task/EditGoogleTaskScreen.kt` | Audited |
| Task List Editor | Compose | `feature/feature-googletask/.../feature/googletask/tasklist/EditGoogleTaskListScreen.kt` | Audited |

## Workflow (automation rules)

Audited — see [`m3-expressive-adoption.md` §7](m3-expressive-adoption.md#7-workflow--routines-screens--audit).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Workflow Gallery | Compose | `feature/feature-workflow/.../feature/workflow/WorkflowGalleryScreen.kt` | Audited |
| Workflow Rules for Group | Compose | `feature/feature-workflow/.../feature/workflow/WorkflowRulesForGroupScreen.kt` | Audited |
| Workflow Rules for Reminder | Compose | `feature/feature-workflow/.../feature/workflow/WorkflowRulesForReminderScreen.kt` | Audited |
| Workflow Rule Builder | Compose | `feature/feature-workflow/.../feature/workflow/builder/WorkflowRuleBuilderScreen.kt` | Audited |

## Routines

New feature area (module `feature-routine`) added since this doc was first written. Audited — see
[`m3-expressive-adoption.md` §7](m3-expressive-adoption.md#7-workflow--routines-screens--audit).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Routines List | Compose | `feature/feature-routine/.../feature/routine/list/RoutinesListScreen.kt` | Audited |
| Routine Editor | Compose | `feature/feature-routine/.../feature/routine/edit/RoutineEditScreen.kt` | Audited |
| Routine Preview | Compose | `feature/feature-routine/.../feature/routine/preview/RoutinePreviewScreen.kt` | Audited |
| Routine Execution | Compose | `feature/feature-routine/.../feature/routine/execution/RoutineExecutionScreen.kt` | Audited |

## Settings

Most screens below live in `feature/feature-settings`; a few moved into the feature module they
configure instead (noted per row). Audited — see
[`m3-expressive-adoption.md` §11](m3-expressive-adoption.md#11-settings-screens-part-a--audit) (Hub through
PIN screens) and [§12](m3-expressive-adoption.md#12-settings-screens-part-b--audit) (Cloud Backup through
Pro Version). Both halves trace their worst finding — missing back-button content descriptions — to the
same shared `SettingsScaffold.kt`, so one fix there covers nearly the whole section.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Settings Hub | Compose | `feature/feature-settings/.../feature/settings/SettingsHubScreen.kt` | Audited |
| General Settings | Compose | `feature/feature-settings/.../feature/settings/general/GeneralSettingsScreen.kt` | Audited |
| Backup Settings | Compose | `feature/feature-settings/.../feature/settings/backup/BackupSettingsScreen.kt` | Audited |
| Reminders Settings | Compose | `feature/feature-reminder/.../feature/reminder/settings/RemindersSettingsScreen.kt` (moved to feature-reminder) | Audited |
| Manage Presets | Compose | `feature/feature-reminder/.../feature/reminder/settings/ManagePresetsScreen.kt` (moved to feature-reminder) | Audited |
| Notification Customization Help | Compose | `feature/feature-reminder/.../feature/reminder/settings/help/NotificationCustomizationHelpScreen.kt` (moved to feature-reminder) | Audited |
| Calendar Settings | Compose | `feature/feature-settings/.../feature/settings/calendar/CalendarSettingsScreen.kt` | Audited |
| Select Holiday Country | Compose | `feature/feature-settings/.../feature/settings/calendar/country/HolidayCountryScreen.kt` | Audited |
| Birthday Settings | Compose | `feature/feature-birthday/.../feature/birthday/settings/BirthdaySettingsScreen.kt` (moved to feature-birthday) | Audited |
| Note Settings | Compose | `feature/feature-settings/.../feature/settings/NoteSettingsScreen.kt` | Audited |
| Location Settings | Compose | `feature/feature-settings/.../feature/settings/location/LocationSettingsScreen.kt` | Audited |
| Map Style | Compose | `feature/feature-settings/.../feature/settings/location/MapStyleScreen.kt` | Audited |
| Security Settings | Compose | `feature/feature-settings/.../feature/settings/security/SecuritySettingsScreen.kt` | Audited |
| Add PIN | Compose | `feature/feature-settings/.../feature/settings/security/AddPinScreen.kt` | Audited |
| Change PIN | Compose | `feature/feature-settings/.../feature/settings/security/ChangePinScreen.kt` | Audited |
| Disable PIN | Compose | `feature/feature-settings/.../feature/settings/security/DisablePinScreen.kt` | Audited |
| Cloud Backup Settings | Compose | `feature/feature-settings/.../feature/settings/export/CloudBackupSettingsScreen.kt` | Audited |
| Cloud Services (connect) | Compose | `feature/feature-settings/.../feature/settings/export/services/CloudServicesScreen.kt` | Audited |
| Other Settings | Compose | `feature/feature-settings/.../feature/settings/other/OtherSettingsScreen.kt` | Audited |
| Permissions | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`PermissionsEntry`) | Audited |
| Open Source Licenses | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`OssEntry`, `SettingsWebView`) | Audited |
| Privacy Policy | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`PrivacyPolicyEntry`, `SettingsWebView`) | Audited |
| Terms of Service | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`TermsEntry`, `SettingsWebView`) | Audited |
| What's New | Compose | `feature/feature-settings/.../feature/settings/other/whatsnew/WhatsNewScreen.kt` | Audited |
| Gemini Functions (App Functions) | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`GeminiFunctionsEntry`) | Audited |
| AI Digest Settings | Compose | `feature/feature-settings/.../feature/settings/digest/DigestSettingsScreen.kt` | Audited |
| Header Items Settings | Compose | `feature/feature-settings/.../feature/settings/headeritems/HeaderItemsSettingsScreen.kt` | Audited |
| Troubleshooting | Compose | `feature/feature-settings/.../feature/settings/troubleshooting/TroubleshootingScreen.kt` | Audited |
| Pro Version | Compose | `feature/feature-settings/.../feature/settings/proversion/ProVersionScreen.kt` | Audited |
| Developer (debug tools) | Compose | `feature/feature-settings/.../feature/settings/debug/DeveloperScreen.kt` | Out of scope (debug-only) |
| Object Export (debug) | Compose | `feature/feature-settings/.../feature/settings/debug/ObjectExportScreen.kt` | Out of scope (debug-only) |

## Backup / Insights (PRO)

Audited — see [`m3-expressive-adoption.md` §13](m3-expressive-adoption.md#13-backup--insights--onboarding--widget-configuration-screens--audit).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Local Backup (Export) | Compose | `extensions/localbackup/.../localbackup/compose/LocalBackupScreen.kt` | Audited |
| Local Backup (Import) | Compose | same file, `LocalBackupNavKey.Import` | Audited |
| Insights Dashboard | Compose | `feature/feature-insights/.../insights/compose/InsightsScreen.kt` | Audited |

## Onboarding / Login

Audited — see [`m3-expressive-adoption.md` §13](m3-expressive-adoption.md#13-backup--insights--onboarding--widget-configuration-screens--audit).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| PIN Login | Compose (Activity-hosted) | `ui/ui-common/.../ui/common/login/PinLoginScreen.kt`, `PinLoginActivity.kt` | Audited |

## Widget Configuration (appwidgets module)

Audited — see [`m3-expressive-adoption.md` §13](m3-expressive-adoption.md#13-backup--insights--onboarding--widget-configuration-screens--audit).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Single Note Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../singlenote/SingleNoteWidgetConfigScreen.kt` | Audited |
| Notes Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../notes/NotesWidgetConfigScreen.kt` | Audited |
| Calendar Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../calendar/CalendarWidgetConfigScreen.kt` | Audited |
| Events Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../events/EventsWidgetConfigScreen.kt` | Audited |
| Birthdays Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../birthdays/BirthdaysWidgetConfigScreen.kt` | Audited |
| Combined Buttons Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../combinedbuttons/CombinedWidgetConfigScreen.kt` | Audited |
| Google Tasks Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../googletasks/TasksWidgetConfigScreen.kt` | Audited |

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
