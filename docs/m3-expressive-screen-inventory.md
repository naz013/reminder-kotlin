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
`FontWeight` overrides vs. emphasized type, etc.) and per-screen findings. Reminder Action moved to "In
progress": its `FontWeight`/alpha-blend/off-scale-elevation gaps were fixed in
[§17](m3-expressive-adoption.md#17-reminderactionscreenkt-typeelevationcolor-token-fixes--landed). Map
Value Editor moved to "In progress" too: its hand-rolled bottom sheet's literal `tween()` motion, missing
640dp max-width, and hardcoded scrim color were fixed in
[§19](m3-expressive-adoption.md#19-mapeditorscreenkt-motionmax-widthscrim-token-fixes--landed).
`SubTasksValueEditor.kt`'s literal `tween()` and sub-48dp check/remove buttons were fixed in
[§20](m3-expressive-adoption.md#20-subtasksvalueeditorkt-motiontouch-target-fixes--landed) — it's rendered
from both Reminder Editor (via `ValueEditorSheet.kt`'s checklist builder item) and Todo Editor directly, so
both move to "In progress" too. Select Application and Reminder Preview move to "In progress" as well:
their hand-built list rows and (for Reminder Preview) inconsistent surface-container roles were fixed in
[§21](m3-expressive-adoption.md#21-selectapplicationscreenktpreviewreminderscreenkt-list-row-and-surface-container-fixes--landed).
Reminder Fullscreen Map moves to "In progress" too: its deprecated baseline `ExtendedFloatingActionButton`
was swapped for the real `SmallExtendedFloatingActionButton` in
[§22](m3-expressive-adoption.md#22-reminderfullscreenmapscreenkt-deprecated-baseline-extended-fab--landed).
Reminder Editor and Todo Editor's duplicated `OfflineOnlyRow` composable was deduplicated into a shared
internal `feature-reminder` composable in
[§23](m3-expressive-adoption.md#23-todoeditscreenktbuildreminderscreenkt-duplicated-offlineonlyrow-dedup--landed)
(both rows were already "In progress" from §20). This closes out every finding from the Reminders audit.
The other rows below stay "Audited" (not "In progress") until a fix actually lands.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Reminder Editor (Build Reminder) | Compose | `feature/feature-reminder/.../feature/reminder/build/BuildReminderScreen.kt` | In progress |
| Reminder Help | Compose | `feature/feature-reminder/.../feature/reminder/build/help/ReminderHelpScreen.kt` | Audited |
| Recurrence Help | Compose | `feature/feature-reminder/.../feature/reminder/recur/RecurHelpScreen.kt` | Audited |
| Select Application | Compose | `feature/feature-reminder/.../feature/reminder/apps/SelectApplicationScreen.kt` | In progress |
| Map Value Editor | Compose | `feature/feature-reminder/.../feature/reminder/build/valuedialog/editor/MapEditorScreen.kt` | In progress |
| Reminder Preview | Compose | `feature/feature-reminder/.../feature/reminder/preview/PreviewReminderScreen.kt` | In progress |
| Reminder Fullscreen Map | Compose | `feature/feature-reminder/.../feature/reminder/preview/ReminderFullscreenMapScreen.kt` | In progress |
| Reminders Archive | Compose | `feature/feature-reminder/.../feature/reminder/lists/removed/RemindersArchiveScreen.kt` | Audited |
| Reminder Action (alarm/ringing) | Compose (Activity-hosted) | Screen: `feature/feature-reminder/.../feature/reminder/dialog/ReminderActionScreen.kt`; Activity: `app/src/main/java/com/elementary/tasks/reminder/dialog/ReminderActionActivity.kt` | In progress |
| Todo Editor | Compose | `feature/feature-reminder/.../feature/reminder/todo/TodoEditScreen.kt` | In progress |

## Notes

Audited — see [`m3-expressive-adoption.md` §8](m3-expressive-adoption.md#8-notes--birthdays-screens--audit).
Note Editor moved to "In progress": its background-color/gradient panels render through `ui-common`'s
shared `ColorSlider`, fixed for accessibility in
[§16](m3-expressive-adoption.md#16-colorslider-accessibility-fix--landed). Notes List and Notes Archive
move to "In progress" too: `NotesScreen.kt`'s back-button content description, app-bar color token, and
empty-state alpha-blend were fixed in
[§24](m3-expressive-adoption.md#24-notesscreenkt-back-buttonapp-bar-tokenalpha-blend-fixes--landed) — both
rows share the same composable, so one fix covers both. Note Editor's floating toolbar
(`NoteEditFloatingBar.kt`) also had its off-scale elevation and literal `tween()`/`spring()` calls fixed in
[§25](m3-expressive-adoption.md#25-noteeditfloatingbarkt-elevationmotion-fixes--landed) (row already "In
progress" from §16). Note Preview moves to "In progress" too: its attached-reminder row
(`PreviewNoteReminderRow.kt`) had its literal `tween()` motion fixed in
[§26](m3-expressive-adoption.md#26-previewnotereminderrowkt-motion-fix--landed).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Notes List | Compose | `feature/feature-note/.../feature/note/list/NotesScreen.kt` | In progress |
| Notes Archive | Compose | `feature/feature-note/.../feature/note/NotesNavGraph.kt` (`NotesArchiveEntry`, reuses `NotesScreen`) | In progress |
| Note Editor | Compose | `feature/feature-note/.../feature/note/create/NoteEditScreen.kt` | In progress |
| Note Preview | Compose | `feature/feature-note/.../feature/note/preview/PreviewNoteScreen.kt` | In progress |
| Note Image Preview | Compose | `feature/feature-note/.../feature/note/preview/ImagePreviewScreen.kt` | Audited |

## Birthdays

Audited — see [`m3-expressive-adoption.md` §8](m3-expressive-adoption.md#8-notes--birthdays-screens--audit).
Birthday Action moved to "In progress": its `FontWeight`/alpha-blend/off-scale-elevation/off-scale-shape
gaps (the twin of Reminder Action's §17 fix) were fixed in
[§18](m3-expressive-adoption.md#18-birthdayactionscreenkt-typeelevationshapecolor-token-fixes--landed).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Birthdays List | Compose | `feature/feature-birthday/.../feature/birthday/list/BirthdaysScreen.kt` | Audited |
| Birthday Editor | Compose | `feature/feature-birthday/.../feature/birthday/create/EditBirthdayScreen.kt` | Audited |
| Birthday Preview | Compose | `feature/feature-birthday/.../feature/birthday/preview/PreviewBirthdayScreen.kt` | Audited |
| Birthday Action (alarm/ringing) | Compose (Activity-hosted) | Screen: `feature/feature-birthday/.../feature/birthday/dialog/BirthdayActionScreen.kt`; Activity: `app/src/main/java/com/elementary/tasks/birthdays/dialog/BirthdayActionActivity.kt` | In progress |

## Groups / Tags / Places

Audited — see [`m3-expressive-adoption.md` §9](m3-expressive-adoption.md#9-groups--tags--places-screens--audit).
§9 praised the shared `ColorPickerCard`/`ColorSlider` chrome as genuinely spec-correct on everything but
the slider's own accessibility semantics, which turned out to be a real gap fixed since in
[§16](m3-expressive-adoption.md#16-colorslider-accessibility-fix--landed) — moving every screen below that
renders a color picker (list-level quick recolor included) to "In progress."

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Groups List | Compose | `feature/feature-group/.../group/list/GroupsScreen.kt` | In progress |
| Group Details | Compose | `feature/feature-group/.../group/details/GroupDetailsScreen.kt` | Audited |
| Group Editor | Compose | `feature/feature-group/.../group/create/EditGroupScreen.kt` | In progress |
| Tags Manage | Compose | `feature/feature-tags/.../tags/compose/TagsScreen.kt` | In progress |
| Tag Editor | Compose | `feature/feature-tags/.../tags/compose/TagEditScreen.kt` | In progress |
| Tag Details | Compose | `feature/feature-tags/.../tags/details/TagDetailsScreen.kt` | Audited |
| Places List | Compose | `feature/feature-places/.../feature/places/list/PlacesScreen.kt` | Audited |
| Place Editor | Compose | `feature/feature-places/.../feature/places/create/EditPlaceScreen.kt` | In progress |

## Calendar

Audited — see [`m3-expressive-adoption.md` §10](m3-expressive-adoption.md#10-calendar--google-tasks-screens--audit).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Calendar (Month) | Compose | `feature/feature-calendar/.../feature/calendar/monthview/CalendarScreen.kt` | Audited |
| Calendar Timeline (Day / 3-day / 7-day) | Compose | `feature/feature-calendar/.../feature/calendar/timeline/TimelineScreen.kt` | Audited |
| Google Calendar Event Preview | Compose | `feature/feature-calendar/.../feature/calendar/preview/GoogleCalendarEventPreviewScreen.kt` | Audited |

## Google Tasks

Audited — see [`m3-expressive-adoption.md` §10](m3-expressive-adoption.md#10-calendar--google-tasks-screens--audit).
Task List Editor moved to "In progress": its list-color picker renders through the shared `ColorSlider`,
fixed for accessibility in [§16](m3-expressive-adoption.md#16-colorslider-accessibility-fix--landed).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Google Task Lists | Compose | `feature/feature-googletask/.../feature/googletask/GoogleTasksScreen.kt` | Audited |
| Task List (tasks in a list) | Compose | `feature/feature-googletask/.../feature/googletask/TaskListScreen.kt` | Audited |
| Task Preview | Compose | `feature/feature-googletask/.../feature/googletask/preview/PreviewGoogleTaskScreen.kt` | Audited |
| Task Editor | Compose | `feature/feature-googletask/.../feature/googletask/task/EditGoogleTaskScreen.kt` | Audited |
| Task List Editor | Compose | `feature/feature-googletask/.../feature/googletask/tasklist/EditGoogleTaskListScreen.kt` | In progress |

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
[`m3-expressive-adoption.md` §7](m3-expressive-adoption.md#7-workflow--routines-screens--audit). Routine
Editor moved to "In progress": its color picker (`RoutineColorPicker` → shared `ColorPickerCard`/
`ColorSlider`) was fixed for accessibility in
[§16](m3-expressive-adoption.md#16-colorslider-accessibility-fix--landed).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Routines List | Compose | `feature/feature-routine/.../feature/routine/list/RoutinesListScreen.kt` | Audited |
| Routine Editor | Compose | `feature/feature-routine/.../feature/routine/edit/RoutineEditScreen.kt` | In progress |
| Routine Preview | Compose | `feature/feature-routine/.../feature/routine/preview/RoutinePreviewScreen.kt` | Audited |
| Routine Execution | Compose | `feature/feature-routine/.../feature/routine/execution/RoutineExecutionScreen.kt` | Audited |

## Settings

Most screens below live in `feature/feature-settings`; a few moved into the feature module they
configure instead (noted per row). Audited — see
[`m3-expressive-adoption.md` §11](m3-expressive-adoption.md#11-settings-screens-part-a--audit) (Hub through
PIN screens) and [§12](m3-expressive-adoption.md#12-settings-screens-part-b--audit) (Cloud Backup through
Pro Version). Both halves traced their worst finding — missing back-button content descriptions and an
app-bar color-token bypass — to the same shared `SettingsScaffold.kt`; that fix has landed (see
[§14](m3-expressive-adoption.md#14-shared-scaffold-fixes--landed)), moving the 24 screens it covers to "In
progress." `HolidayCountryScreen.kt` (Select Holiday Country) independently duplicated the same bug outside
the shared scaffold; it has since been switched onto `SettingsScaffold` directly, fixing it too and moving
it to "In progress." `NotificationCustomizationHelpScreen.kt`, `CloudServicesScreen.kt`,
`WhatsNewScreen.kt`, and `ProVersionScreen.kt` don't use `SettingsScaffold` and were unaffected by that
change (the first was already correct). The latter three's own gap — a triplicated gradient-hero
header/card, per §12 finding #3 — has since been fixed too by extracting shared `GradientScreenHeader`/
`GradientHeroCard` composables (see
[§15](m3-expressive-adoption.md#15-gradient-hero-headercard-dedup--landed)), moving all three to "In
progress."

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Settings Hub | Compose | `feature/feature-settings/.../feature/settings/SettingsHubScreen.kt` | In progress |
| General Settings | Compose | `feature/feature-settings/.../feature/settings/general/GeneralSettingsScreen.kt` | In progress |
| Backup Settings | Compose | `feature/feature-settings/.../feature/settings/backup/BackupSettingsScreen.kt` | In progress |
| Reminders Settings | Compose | `feature/feature-reminder/.../feature/reminder/settings/RemindersSettingsScreen.kt` (moved to feature-reminder) | In progress |
| Manage Presets | Compose | `feature/feature-reminder/.../feature/reminder/settings/ManagePresetsScreen.kt` (moved to feature-reminder) | In progress |
| Notification Customization Help | Compose | `feature/feature-reminder/.../feature/reminder/settings/help/NotificationCustomizationHelpScreen.kt` (moved to feature-reminder) | Audited |
| Calendar Settings | Compose | `feature/feature-settings/.../feature/settings/calendar/CalendarSettingsScreen.kt` | In progress |
| Select Holiday Country | Compose | `feature/feature-settings/.../feature/settings/calendar/country/HolidayCountryScreen.kt` | In progress |
| Birthday Settings | Compose | `feature/feature-birthday/.../feature/birthday/settings/BirthdaySettingsScreen.kt` (moved to feature-birthday) | In progress |
| Note Settings | Compose | `feature/feature-settings/.../feature/settings/NoteSettingsScreen.kt` | In progress |
| Location Settings | Compose | `feature/feature-settings/.../feature/settings/location/LocationSettingsScreen.kt` | In progress |
| Map Style | Compose | `feature/feature-settings/.../feature/settings/location/MapStyleScreen.kt` | In progress |
| Security Settings | Compose | `feature/feature-settings/.../feature/settings/security/SecuritySettingsScreen.kt` | In progress |
| Add PIN | Compose | `feature/feature-settings/.../feature/settings/security/AddPinScreen.kt` | In progress |
| Change PIN | Compose | `feature/feature-settings/.../feature/settings/security/ChangePinScreen.kt` | In progress |
| Disable PIN | Compose | `feature/feature-settings/.../feature/settings/security/DisablePinScreen.kt` | In progress |
| Cloud Backup Settings | Compose | `feature/feature-settings/.../feature/settings/export/CloudBackupSettingsScreen.kt` | In progress |
| Cloud Services (connect) | Compose | `feature/feature-settings/.../feature/settings/export/services/CloudServicesScreen.kt` | In progress |
| Other Settings | Compose | `feature/feature-settings/.../feature/settings/other/OtherSettingsScreen.kt` | In progress |
| Permissions | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`PermissionsEntry`) | In progress |
| Open Source Licenses | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`OssEntry`, `SettingsWebView`) | In progress |
| Privacy Policy | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`PrivacyPolicyEntry`, `SettingsWebView`) | In progress |
| Terms of Service | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`TermsEntry`, `SettingsWebView`) | In progress |
| What's New | Compose | `feature/feature-settings/.../feature/settings/other/whatsnew/WhatsNewScreen.kt` | In progress |
| Gemini Functions (App Functions) | Compose | `feature/feature-settings/.../feature/settings/other/OtherNavGraph.kt` (`GeminiFunctionsEntry`) | In progress |
| AI Digest Settings | Compose | `feature/feature-settings/.../feature/settings/digest/DigestSettingsScreen.kt` | In progress |
| Header Items Settings | Compose | `feature/feature-settings/.../feature/settings/headeritems/HeaderItemsSettingsScreen.kt` | In progress |
| Troubleshooting | Compose | `feature/feature-settings/.../feature/settings/troubleshooting/TroubleshootingScreen.kt` | In progress |
| Pro Version | Compose | `feature/feature-settings/.../feature/settings/proversion/ProVersionScreen.kt` | In progress |
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
The shared `WidgetConfigScaffold.kt`'s back-button content description and app-bar color-token bypass have
been fixed (see [§14](m3-expressive-adoption.md#14-shared-scaffold-fixes--landed)), and the shared
`ColorSlider` every one of these 7 screens uses for its color pickers has since had its accessibility gap
fixed too (see [§16](m3-expressive-adoption.md#16-colorslider-accessibility-fix--landed)) — both moving all
7 screens below to "In progress." Each still has its own remaining open findings from §13 that neither fix
touched.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Single Note Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../singlenote/SingleNoteWidgetConfigScreen.kt` | In progress |
| Notes Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../notes/NotesWidgetConfigScreen.kt` | In progress |
| Calendar Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../calendar/CalendarWidgetConfigScreen.kt` | In progress |
| Events Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../events/EventsWidgetConfigScreen.kt` | In progress |
| Birthdays Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../birthdays/BirthdaysWidgetConfigScreen.kt` | In progress |
| Combined Buttons Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../combinedbuttons/CombinedWidgetConfigScreen.kt` | In progress |
| Google Tasks Widget Config | Compose (Activity-hosted) | `extensions/appwidgets/.../googletasks/TasksWidgetConfigScreen.kt` | In progress |

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
