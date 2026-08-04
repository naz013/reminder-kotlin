# M3 Expressive — Screen Inventory & Migration Status

Companion tracking table to [`m3-expressive-adoption.md`](m3-expressive-adoption.md). Lists every
user-navigable screen in the app and its status toward the M3 Expressive design update. Update the
**Status** column as work lands — this file is the source of truth for "what's left."

Scope: screens reachable through app navigation (Nav3 graphs under `app/src/main/java/com/elementary/tasks/**`,
plus Activity-hosted Compose screens in `ui-common` and `appwidgets`). Excludes dialogs/bottom sheets,
intent-forwarding shells with no UI of their own, and debug/admin-only modules (`reviewsadmin`,
`cloudtestadmin`) per [`CLAUDE.md`](../CLAUDE.md). The app is almost entirely Jetpack Compose already — there
are no remaining XML/View-based full screens (`app/src/main/res/layout/` has only a dialog layout and a
notification `RemoteViews` layout, not screens).

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
| Home | Compose | `home/HomeScreen.kt`, `home/ChronologicalHomeScreen.kt` | Audited |
| Events | Compose | `home/eventsview/EventsScreen.kt` | Audited |

## Reminders

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Reminder Editor (Build Reminder) | Compose | `reminder/build/BuildReminderScreen.kt` | Not started |
| Reminder Help | Compose | `reminder/build/help/ReminderHelpScreen.kt` | Not started |
| Recurrence Help | Compose | `reminder/recur/RecurHelpScreen.kt` | Not started |
| Select Application | Compose | `core/apps/SelectApplicationScreen.kt` | Not started |
| Map Value Editor | Compose | `reminder/build/valuedialog/editor/MapEditorScreen.kt` | Not started |
| Reminder Preview | Compose | `reminder/preview/PreviewReminderScreen.kt` | Not started |
| Reminder Fullscreen Map | Compose | `reminder/preview/ReminderFullscreenMapScreen.kt` | Not started |
| Reminders Archive | Compose | `reminder/lists/removed/RemindersArchiveScreen.kt` | Not started |
| Reminder Action (alarm/ringing) | Compose (Activity-hosted) | `reminder/dialog/ReminderActionScreen.kt`, `ReminderActionActivity.kt` | Not started |

## Notes

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Notes List | Compose | `notes/list/NotesScreen.kt` | Not started |
| Notes Archive | Compose | `notes/NotesNavGraph.kt` (`NotesArchiveEntry`, reuses `NotesScreen`) | Not started |
| Note Editor | Compose | `notes/create/NoteEditScreen.kt` | Not started |
| Note Preview | Compose | `notes/preview/PreviewNoteScreen.kt` | Not started |
| Note Image Preview | Compose | `notes/preview/ImagePreviewScreen.kt` | Not started |

## Birthdays

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Birthday Editor | Compose | `birthdays/create/EditBirthdayScreen.kt` | Not started |
| Birthday Preview | Compose | `birthdays/preview/PreviewBirthdayScreen.kt` | Not started |
| Birthday Action (alarm/ringing) | Compose (Activity-hosted) | `birthdays/dialog/BirthdayActionScreen.kt`, `BirthdayActionActivity.kt` | Not started |

## Groups / Tags / Places

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Groups List | Compose | `groups/list/GroupsScreen.kt` | Not started |
| Group Details | Compose | `groups/details/GroupDetailsScreen.kt` | Not started |
| Group Editor | Compose | `groups/create/EditGroupScreen.kt` | Not started |
| Tags Manage | Compose | `tags/src/main/kotlin/.../tags/compose/TagsScreen.kt` | Not started |
| Tag Editor | Compose | `tags/src/main/kotlin/.../tags/compose/TagEditScreen.kt` | Not started |
| Places List | Compose | `places/list/PlacesScreen.kt` | Not started |
| Place Editor | Compose | `places/create/EditPlaceScreen.kt` | Not started |

## Calendar

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Calendar (Month) | Compose | `calendar/monthview/CalendarScreen.kt` | Not started |
| Calendar (Day/Week) | Compose | `calendar/dayview/WeekViewScreen.kt` | Not started |

## Google Tasks

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Google Task Lists | Compose | `googletasks/GoogleTasksScreen.kt` | Not started |
| Task List (tasks in a list) | Compose | `googletasks/list/TaskListScreen.kt` | Not started |
| Task Preview | Compose | `googletasks/preview/PreviewGoogleTaskScreen.kt` | Not started |
| Task Editor | Compose | `googletasks/task/EditGoogleTaskScreen.kt` | Not started |
| Task List Editor | Compose | `googletasks/tasklist/EditGoogleTaskListScreen.kt` | Not started |

## Workflow (automation rules)

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Workflow Gallery | Compose | `workflow/WorkflowGalleryScreen.kt` | Not started |
| Workflow Rules for Group | Compose | `workflow/WorkflowRulesForGroupScreen.kt` | Not started |
| Workflow Rule Builder | Compose | `workflow/builder/WorkflowRuleBuilderScreen.kt` | Not started |

## Settings

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Settings Hub | Compose | `settings/SettingsHubScreen.kt` | Not started |
| General Settings | Compose | `settings/general/GeneralSettingsScreen.kt` | Not started |
| Backup Settings | Compose | `settings/backup/BackupSettingsScreen.kt` | Not started |
| Reminders Settings | Compose | `settings/reminders/RemindersSettingsScreen.kt` | Not started |
| Manage Presets | Compose | `settings/reminders/ManagePresetsScreen.kt` | Not started |
| Notification Customization Help | Compose | `settings/reminders/help/NotificationCustomizationHelpScreen.kt` | Not started |
| Calendar Settings | Compose | `settings/calendar/CalendarSettingsScreen.kt` | Not started |
| Birthday Settings | Compose | `settings/birthday/BirthdaySettingsScreen.kt` | Not started |
| Note Settings | Compose | `settings/NoteSettingsScreen.kt` | Not started |
| Location Settings | Compose | `settings/location/LocationSettingsScreen.kt` | Not started |
| Map Style | Compose | `settings/location/MapStyleScreen.kt` | Not started |
| Security Settings | Compose | `settings/security/SecuritySettingsScreen.kt` | Not started |
| Add PIN | Compose | `settings/security/AddPinScreen.kt` | Not started |
| Change PIN | Compose | `settings/security/ChangePinScreen.kt` | Not started |
| Disable PIN | Compose | `settings/security/DisablePinScreen.kt` | Not started |
| Cloud Backup Settings | Compose | `settings/export/CloudBackupSettingsScreen.kt` | Not started |
| Cloud Services (connect) | Compose | `settings/export/services/CloudServicesScreen.kt` | Not started |
| Other Settings | Compose | `settings/other/OtherSettingsScreen.kt` | Not started |
| Permissions | Compose | `settings/other/OtherNavGraph.kt` (`PermissionsEntry`) | Not started |
| Open Source Licenses | Compose | `settings/other/OtherNavGraph.kt` (`OssEntry`, `SettingsWebView`) | Not started |
| Privacy Policy | Compose | `settings/other/OtherNavGraph.kt` (`PrivacyPolicyEntry`, `SettingsWebView`) | Not started |
| Terms of Service | Compose | `settings/other/OtherNavGraph.kt` (`TermsEntry`, `SettingsWebView`) | Not started |
| What's New | Compose | `settings/other/whatsnew/WhatsNewScreen.kt` | Not started |
| Gemini Functions (App Functions) | Compose | `settings/other/OtherNavGraph.kt` (`GeminiFunctionsEntry`) | Not started |
| Troubleshooting | Compose | `settings/troubleshooting/TroubleshootingScreen.kt` | Not started |
| Pro Version | Compose | `settings/proversion/ProVersionScreen.kt` | Not started |
| Developer (debug tools) | Compose | `settings/test/DeveloperScreen.kt` | Out of scope (debug-only) |
| Object Export (debug) | Compose | `settings/test/ObjectExportScreen.kt` | Out of scope (debug-only) |

## Backup / Insights (PRO)

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Local Backup (Export) | Compose | `localbackup/src/main/kotlin/.../localbackup/compose/LocalBackupScreen.kt` | Not started |
| Local Backup (Import) | Compose | same file, `LocalBackupNavKey.Import` | Not started |
| Insights Dashboard | Compose | `insights/src/main/kotlin/.../insights/compose/InsightsScreen.kt` | Not started |

## Onboarding / Login

| Screen | Type | File(s) | Status |
|---|---|---|---|
| PIN Login | Compose (Activity-hosted) | `ui-common/src/main/kotlin/.../login/PinLoginScreen.kt`, `PinLoginActivity.kt` | Not started |

## Widget Configuration (appwidgets module)

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Single Note Widget Config | Compose (Activity-hosted) | `appwidgets/.../singlenote/SingleNoteWidgetConfigScreen.kt` | Not started |
| Notes Widget Config | Compose (Activity-hosted) | `appwidgets/.../notes/NotesWidgetConfigScreen.kt` | Not started |
| Calendar Widget Config | Compose (Activity-hosted) | `appwidgets/.../calendar/CalendarWidgetConfigScreen.kt` | Not started |
| Events Widget Config | Compose (Activity-hosted) | `appwidgets/.../events/EventsWidgetConfigScreen.kt` | Not started |
| Birthdays Widget Config | Compose (Activity-hosted) | `appwidgets/.../birthdays/BirthdaysWidgetConfigScreen.kt` | Not started |
| Combined Buttons Widget Config | Compose (Activity-hosted) | `appwidgets/.../combinedbuttons/CombinedWidgetConfigScreen.kt` | Not started |
| Google Tasks Widget Config | Compose (Activity-hosted) | `appwidgets/.../googletasks/TasksWidgetConfigScreen.kt` | Not started |

## Notes on scope

- **Alarm/trigger screens** (Reminder Action, Birthday Action) are full-screen but reached via
  notification/alarm rather than in-app nav — style them consistently, but they're lower priority since
  they're seen far less often than list/editor screens.
- **WebView-hosted screens** (Licenses, Privacy Policy, Terms) render remote/static HTML — expressive work
  here is limited to the surrounding Compose chrome (top bar, loading state), not the web content itself.
- **Developer / Object Export** are internal debug tools, out of scope for design polish.
- Widget *configuration* screens (this table) are regular in-app Compose Activities, distinct from the
  actual home-screen widgets themselves (Glance-based `RemoteViews`, tracked separately — see the
  `feature/REM-1082_Rewrite_widgets_with_compose` branch work, not part of this document).
