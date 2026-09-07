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
(both rows were already "In progress" from §20). Reminders Archive moves to "In progress" too: its
back-button content description was fixed in
[§28](m3-expressive-adoption.md#28-remindersarchivescreenkt-back-button-fix--landed) — §20/§23 had wrongly
claimed this closed out every §6 finding, but this screen's bug (and its scroll-shadow app bar, still open)
had actually been missed; §28 corrects that. The other rows below stay "Audited" (not "In progress") until a
fix actually lands.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Reminder Editor (Build Reminder) | Compose | `feature/feature-reminder/.../feature/reminder/build/BuildReminderScreen.kt` | In progress |
| Reminder Help | Compose | `feature/feature-reminder/.../feature/reminder/build/help/ReminderHelpScreen.kt` | Audited |
| Recurrence Help | Compose | `feature/feature-reminder/.../feature/reminder/recur/RecurHelpScreen.kt` | Audited |
| Select Application | Compose | `feature/feature-reminder/.../feature/reminder/apps/SelectApplicationScreen.kt` | In progress |
| Map Value Editor | Compose | `feature/feature-reminder/.../feature/reminder/build/valuedialog/editor/MapEditorScreen.kt` | In progress |
| Reminder Preview | Compose | `feature/feature-reminder/.../feature/reminder/preview/PreviewReminderScreen.kt` | In progress |
| Reminder Fullscreen Map | Compose | `feature/feature-reminder/.../feature/reminder/preview/ReminderFullscreenMapScreen.kt` | In progress |
| Reminders Archive | Compose | `feature/feature-reminder/.../feature/reminder/lists/removed/RemindersArchiveScreen.kt` | In progress |
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
Birthdays List, Birthday Editor, and Birthday Preview all move to "In progress" too: their back/close
content descriptions, app-bar color tokens, `BirthdaysList`'s bare `Icons.Default.FilterList`/alpha-blend
empty state, and `PreviewBirthdayScreen.kt`'s literal motion were fixed in
[§27](m3-expressive-adoption.md#27-birthdays-half-of-8--backclose-app-bar-token-alpha-blend-bare-icon-and-motion-fixes--landed).
That section also flagged a real gap in the earlier Reminders work: `RemindersArchiveScreen.kt`'s identical
back-button bug and scroll-shadow app bar were never actually fixed despite §20/§23 claiming §6 was "fully
closed." The back-button half of that gap was fixed in
[§28](m3-expressive-adoption.md#28-remindersarchivescreenkt-back-button-fix--landed) (see the Reminders
section above); the scroll-shadow app bar remains open there, same as it does here.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Birthdays List | Compose | `feature/feature-birthday/.../feature/birthday/list/BirthdaysScreen.kt` | In progress |
| Birthday Editor | Compose | `feature/feature-birthday/.../feature/birthday/create/EditBirthdayScreen.kt` | In progress |
| Birthday Preview | Compose | `feature/feature-birthday/.../feature/birthday/preview/PreviewBirthdayScreen.kt` | In progress |
| Birthday Action (alarm/ringing) | Compose (Activity-hosted) | Screen: `feature/feature-birthday/.../feature/birthday/dialog/BirthdayActionScreen.kt`; Activity: `app/src/main/java/com/elementary/tasks/birthdays/dialog/BirthdayActionActivity.kt` | In progress |

## Groups / Tags / Places

Audited — see [`m3-expressive-adoption.md` §9](m3-expressive-adoption.md#9-groups--tags--places-screens--audit).
§9 praised the shared `ColorPickerCard`/`ColorSlider` chrome as genuinely spec-correct on everything but
the slider's own accessibility semantics, which turned out to be a real gap fixed since in
[§16](m3-expressive-adoption.md#16-colorslider-accessibility-fix--landed) — moving every screen below that
renders a color picker (list-level quick recolor included) to "In progress." All 8 rows are now "In
progress" for three more reasons: [§29](m3-expressive-adoption.md#29-groupstagsplaces-screens-backsave-button-content-description-fixes--landed)
fixed the back/close/save `contentDescription = null` bug §9 found on every one of these screens,
[§30](m3-expressive-adoption.md#30-groupstagsplaces-empty-states-migrated-onto-shared-emptystatekt--landed)
migrated the 4 duplicated alpha-blended empty states (`Groups List`, `Tags Manage`, `Tag Details`,
`Places List`) onto `ui-common`'s shared `EmptyState.kt`, fixing its `onSurfaceVariant` gap along the way,
and [§31](m3-expressive-adoption.md#31-groupstagsplaces-topappbars-pointed-at-shared-topappbarcolor-token--landed)
pointed all 8 screens' `TopAppBar`s at the shared `TopAppbarColor` token instead of a hand-rolled
`TopAppBarDefaults.topAppBarColors` call.
[§32](m3-expressive-adoption.md#32-groupstagsplaces-list-row-type-role-split-unified-on-titlemedium--landed)
then unified `GroupListItem`/`TagListItem`/`PlaceListItemCard`'s 3-way list-row type-role split on
`titleMedium`, and
[§33](m3-expressive-adoption.md#33-groupstagsplaces-drawablecatalogappicons-convention-cleanup--landed)
finished the `DrawableCatalog`/`AppIcons` convention cleanup (a repo-hygiene rule, not an M3 spec gap) —
**§9 is now fully closed**, save for two individually-legitimate, explicitly-lower-priority inconsistencies
(delete-placement, list-item container-color) §9 itself flagged as "worth a look if touched again," not
confirmed defects.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Groups List | Compose | `feature/feature-group/.../group/list/GroupsScreen.kt` | In progress |
| Group Details | Compose | `feature/feature-group/.../group/details/GroupDetailsScreen.kt` | In progress |
| Group Editor | Compose | `feature/feature-group/.../group/create/EditGroupScreen.kt` | In progress |
| Tags Manage | Compose | `feature/feature-tags/.../tags/compose/TagsScreen.kt` | In progress |
| Tag Editor | Compose | `feature/feature-tags/.../tags/compose/TagEditScreen.kt` | In progress |
| Tag Details | Compose | `feature/feature-tags/.../tags/details/TagDetailsScreen.kt` | In progress |
| Places List | Compose | `feature/feature-places/.../feature/places/list/PlacesScreen.kt` | In progress |
| Place Editor | Compose | `feature/feature-places/.../feature/places/create/EditPlaceScreen.kt` | In progress |

## Calendar

Audited — see [`m3-expressive-adoption.md` §10](m3-expressive-adoption.md#10-calendar--google-tasks-screens--audit).
All 3 rows moved to "In progress":
[§34](m3-expressive-adoption.md#34-calendargoogle-tasks-screens-back-button-content-description-fixes--landed)
fixed the back/close `contentDescription = null` bug §10 found on every screen in this group, and
[§35](m3-expressive-adoption.md#35-calendargoogle-tasks-topappbars-pointed-at-shared-topappbarcolor-token--landed)
pointed every `TopAppBar` at the shared `TopAppbarColor` token (including the 2 Calendar screens' distinct
`Color.Transparent` variant, confirmed visually equivalent since neither screen layers its own background
under the app bar). [§37](m3-expressive-adoption.md#37-timelinepagerkt-off-scale-corner-radius-fix--landed)
fixed `TimelinePager.kt`'s (the composable behind Calendar Timeline's grid) two off-scale
`RoundedCornerShape(6.dp)` chip corners, moved onto `MaterialTheme.shapes.extraSmall`. §10's other
findings — missing `detailScreenContentWidth()` on 3 Google Tasks screens, the ad hoc alpha-blend
de-emphasis pattern, and the Calendar Month/Timeline breakpoint-adaptation gap — are still open.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Calendar (Month) | Compose | `feature/feature-calendar/.../feature/calendar/monthview/CalendarScreen.kt` | In progress |
| Calendar Timeline (Day / 3-day / 7-day) | Compose | `feature/feature-calendar/.../feature/calendar/timeline/TimelineScreen.kt` | In progress |
| Google Calendar Event Preview | Compose | `feature/feature-calendar/.../feature/calendar/preview/GoogleCalendarEventPreviewScreen.kt` | In progress |

## Google Tasks

Audited — see [`m3-expressive-adoption.md` §10](m3-expressive-adoption.md#10-calendar--google-tasks-screens--audit).
Task List Editor was already "In progress": its list-color picker renders through the shared `ColorSlider`,
fixed for accessibility in [§16](m3-expressive-adoption.md#16-colorslider-accessibility-fix--landed). The
other 4 rows join it now for the same two reasons as Calendar's:
[§34](m3-expressive-adoption.md#34-calendargoogle-tasks-screens-back-button-content-description-fixes--landed)
fixed their back-button bug, and
[§35](m3-expressive-adoption.md#35-calendargoogle-tasks-topappbars-pointed-at-shared-topappbarcolor-token--landed)
pointed all 5 screens' `TopAppBar`s at the shared `TopAppbarColor` token. On top of that,
[§36](m3-expressive-adoption.md#36-google-tasks-deprecated-baseline-fab-fixes--landed) replaced the
deprecated baseline `ExtendedFloatingActionButton` on `Google Task Lists`, `Task List`, and `Task Preview`
with `SmallExtendedFloatingActionButton`.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Google Task Lists | Compose | `feature/feature-googletask/.../feature/googletask/GoogleTasksScreen.kt` | In progress |
| Task List (tasks in a list) | Compose | `feature/feature-googletask/.../feature/googletask/TaskListScreen.kt` | In progress |
| Task Preview | Compose | `feature/feature-googletask/.../feature/googletask/preview/PreviewGoogleTaskScreen.kt` | In progress |
| Task Editor | Compose | `feature/feature-googletask/.../feature/googletask/task/EditGoogleTaskScreen.kt` | In progress |
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
