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
for the detailed gap analysis and plan behind these two. Formally audited (for the first time, despite being
the earliest-touched screens) in
[§49](m3-expressive-adoption.md#49-home--events-screens--audit): a `DrawableCatalog` convention gap spanning
both files, a scroll-elevation drop-shadow pattern that's now behind the Expressive-recommended color-fill
approach, Agenda's back-button `contentDescription = null` and bare `Icons.Default.FilterList`, its two
alpha-blended empty-state captions, and Home's literal `tween()` (while its own sibling file in the same
module already uses `motionScheme` correctly).
[§50](m3-expressive-adoption.md#50-agendascreenkt-back-button-content-description-fix--landed) fixed
Agenda's back-button bug, and
[§51](m3-expressive-adoption.md#51-agendascreenkt-bare-iconsdefaultfilterlist-fix--landed) fixed its bare
`Icons.Default.FilterList` (same fix as §27's `BirthdaysScreen.kt`), and
[§52](m3-expressive-adoption.md#52-homeevents-drawablecatalog-cleanup--landed) fixed the `DrawableCatalog`
convention gap spanning `HomeScreenState.kt` and `AgendaScreen.kt` (`ChronologicalHomeScreen.kt`'s `AddButton`
half of this same fix turned out not to have landed despite the doc's claim — caught and actually fixed in
§70), and [§53](m3-expressive-adoption.md#53-agendascreenkt-alpha-blend-fixes--landed) fixed Agenda's two
alpha-blended filter-sheet captions, and
[§54](m3-expressive-adoption.md#54-homescreenkt-literal-tween-fix--landed) fixed Home's literal `tween()`
banner transitions, moving them onto `MaterialTheme.motionScheme` to match `ChronologicalHomeScreen.kt`'s
own sibling pattern. The scroll-shadow-to-color-fill item (cross-cutting #2) was claimed landed in
[§64](m3-expressive-adoption.md#64-scroll-shadow-to-color-fill-migration--landed) for both screens, but like
the `DrawableCatalog` gap above, that claim didn't hold for `ChronologicalHomeScreen.kt` — actually fixed in
§70, which also confirmed the identical gap is still open in `AgendaScreen.kt`.
[§70](m3-expressive-adoption.md#70-home--full-adoption-pass-promoted-to-done) did a full adoption
pass on Home specifically: re-verified every prior claim against current source (catching the two gaps
above), fixed two further findings from a fresh read (a sub-48dp `EventCard` action touch target, a
container/on-container color mismatch in its default case), and promoted Home to "Done."
[§71](m3-expressive-adoption.md#71-agenda--full-adoption-pass-promoted-to-done) did the same for Agenda, and
found it worse: every one of §50/§51/§52/§53/§64's claimed Agenda-specific fixes — including the back-button
accessibility bug, not just foundation-adoption items — turned out to still be unlanded in current source.
All fixed now, plus gaps found in the shared `AgendaListItem.kt`/`SelectionTopBar.kt`/`ReminderAgendaRow.kt`/
`BirthdayAgendaRow.kt` components this screen renders through (also benefiting Groups, Reminders Archive,
Notes, Birthdays, Tags, and every multiselect screen). Both Home and Agenda are now "Done" — this group is
closed out.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Home | Compose | `feature/feature-home/.../feature/home/HomeScreen.kt`, `.../ChronologicalHomeScreen.kt` | Done |
| Agenda | Compose | `feature/feature-agenda/.../feature/agenda/AgendaScreen.kt` | Done |

## Reminders

Promoted to "Done" in
[§83](m3-expressive-adoption.md#83-reminders--full-adoption-pass-promoted-to-done), a full adoption pass on
top of the original audit ([§6](m3-expressive-adoption.md#6-reminders-screens--audit)) and its long fix
history (§17, §19-§23, §28, §48, §58, re-verified in
[§72](m3-expressive-adoption.md#72-reminders-group--re-verification-of-17192123285848s-claims)). §83 closed
out §6's two oldest never-landed cross-cutting findings — the off-scale `RoundedCornerShape(24.dp)` on
`BuildReminderScreen.kt`/`TodoEditScreen.kt` (→ `AppShapes.pill`) and the `TopAppBar` color-token bypass on
`SelectApplicationScreen.kt`/`PreviewReminderScreen.kt` (→ `TopAppbarColor`; `RemindersArchiveScreen.kt`'s
`Color.Transparent` turned out to be a false positive, already correctly paired with its §64 animated-`Surface`
scroll treatment) — plus the group's first `modifier`-order and `DrawableCatalog` sweep of the ~20-file
`build/valuedialog/editor/` value-editor package that renders inside `BuildReminderScreen`'s
`ValueEditorSheet.kt`, never read by any prior audit of this group. Reminder Help/Recurrence Help were
already "Done" via §48.

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

Promoted to "Done" in
[§85](m3-expressive-adoption.md#85-notesbirthdays--full-adoption-pass-promoted-to-done), a full adoption
pass on top of the original audit
([§8](m3-expressive-adoption.md#8-notes--birthdays-screens--audit)) and its landed fixes (§16, §24-§27,
§58). §85's largest finding was a ~30-call-site `DrawableCatalog` cleanup across the whole group (this
group never had one before), plus: `NotesScreen.kt`'s hand-rolled empty state migrated onto the shared
`EmptyState`; its long-deferred `SelectableOptionRow` `FontWeight` (state-driven "context" trigger, deferred
since §24) finally swapped to `titleMediumEmphasized`; `NoteEditImageGrid.kt` and
`PreviewNoteImageCarousel.kt` (never in §8's original scope) both had hand-tuned `spring()`/`tween()`
stagger animations migrated to `motionScheme`, and the former's raw `Icons.Default.Close` (the same
anti-pattern §27 already fixed once for `BirthdaysScreen.kt`) fixed too. Left open: `NoteEditImageGrid.kt`'s
20dp remove-image badge, a real sub-48dp touch target that needs restructuring (not a simple resize) to fix
without changing its visual size.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Notes List | Compose | `feature/feature-note/.../feature/note/list/NotesScreen.kt` | Done |
| Notes Archive | Compose | `feature/feature-note/.../feature/note/NotesNavGraph.kt` (`NotesArchiveEntry`, reuses `NotesScreen`) | Done |
| Note Editor | Compose | `feature/feature-note/.../feature/note/create/NoteEditScreen.kt` | Done |
| Note Preview | Compose | `feature/feature-note/.../feature/note/preview/PreviewNoteScreen.kt` | Done |
| Note Image Preview | Compose | `feature/feature-note/.../feature/note/preview/ImagePreviewScreen.kt` | Done |

## Birthdays

Promoted to "Done" in
[§85](m3-expressive-adoption.md#85-notesbirthdays--full-adoption-pass-promoted-to-done), alongside Notes
(see above for the shared history — §8/§18/§27/§28/§64/§73 — and this pass's group-wide `DrawableCatalog`
sweep). §85's Birthdays-specific findings: `BirthdayActionScreen.kt` — never re-read since its original §18
fix, seven sections ago — was confirmed still fully correct (emphasized type, `AppShapes.tile`, default
card elevation all holding), plus two more icon fixes matching Reminders' identical
`ReminderActionScreen.kt` twin; `BirthdaysScreen.kt`'s hand-rolled empty state migrated onto the shared
`EmptyState` (self-caught and fixed two stray unused imports plus an import-ordering slip this migration
introduced, via the usual detekt stash-compare check).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Birthdays List | Compose | `feature/feature-birthday/.../feature/birthday/list/BirthdaysScreen.kt` | Done |
| Birthday Editor | Compose | `feature/feature-birthday/.../feature/birthday/create/EditBirthdayScreen.kt` | Done |
| Birthday Preview | Compose | `feature/feature-birthday/.../feature/birthday/preview/PreviewBirthdayScreen.kt` | Done |
| Birthday Action (alarm/ringing) | Compose (Activity-hosted) | Screen: `feature/feature-birthday/.../feature/birthday/dialog/BirthdayActionScreen.kt`; Activity: `app/src/main/java/com/elementary/tasks/birthdays/dialog/BirthdayActionActivity.kt` | Done |

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

A fresh full re-audit in
[§66](m3-expressive-adoption.md#66-groupstagsplaces-group--fresh-re-audit-modifier-order-fixes--landed)
confirmed every §29-§33 fix is still holding and found two things the original audit never checked: the
`modifier`-parameter-order convention violation (same pattern §65 fixed in Widget Configuration) across the
3 list screens and every list-row/dispatcher composable, now fixed; and `EditGroupScreen.kt`'s
`DelayMinutes` dialog, a third leftover hand-rolled seek/slider `AlertDialog` (after §61/§65) — found but
deliberately not migrated onto the shared `SeekValueDialog`, since its conditional "inherit from settings"
toggle doesn't fit that component's shape without growing its API for a single caller.
[§74](m3-expressive-adoption.md#74-groupstagsplaces-group--re-verification-of-29-33-66s-claims) re-verified
every §29-§33/§66 claim against source a third time and found nothing new — the cleanest re-verification
result in this doc's whole series. A full adoption pass in
[§79](m3-expressive-adoption.md#79-groupstagsplaces--full-adoption-pass-promoted-to-done) worked through the
screen-audit checklist's non-mechanical items (emphasized type, breakpoints, elevation, components, states)
against that already-clean baseline, fixed the one remaining literal shape (`GroupListItem.kt`'s
`DefaultChip`, `RoundedCornerShape(8.dp)` → `MaterialTheme.shapes.small`), and **promoted all 8 rows to
"Done."** What's left open — the container-color/delete-placement/save-button-component divergences, and
`EditGroupScreen.kt`'s hand-rolled `DelayMinutes` dialog — are each individually-legitimate, previously-judged
non-defects, not gaps blocking "Done."

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

Promoted to "Done" in
[§84](m3-expressive-adoption.md#84-calendargoogle-tasks--full-adoption-pass-promoted-to-done), a full
adoption pass on top of the original audit
([§10](m3-expressive-adoption.md#10-calendar--google-tasks-screens--audit)), its six landed fix sections
(§34-§39), a fresh re-audit (§67), a `DrawableCatalog`/`AppIcons` cleanup (§68), and a re-verification pass
(§75). §84 found and fixed two real gaps in `CloudBubble.kt` — the shared `ui-common` speech-bubble popup
behind this group's view-mode switcher and add-event/event-detail bubbles, never read by any prior audit of
this group — a `modifier`-order gap and an internally-inconsistent off-scale `tonalElevation`, plus a
sub-48dp touch target in `CalendarModeToggleButton.kt`'s view-mode list and one more literal
`RoundedCornerShape` in Google Tasks (see below). Left open, still correctly deliberate: the Calendar
Month/Timeline breakpoint-adaptation gap and the Timeline grid's own sub-48dp event blocks (both real
layout/density trade-offs, not mechanical fixes).

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Calendar (Month) | Compose | `feature/feature-calendar/.../feature/calendar/monthview/CalendarScreen.kt` | Done |
| Calendar Timeline (Day / 3-day / 7-day) | Compose | `feature/feature-calendar/.../feature/calendar/timeline/TimelineScreen.kt` | Done |
| Google Calendar Event Preview | Compose | `feature/feature-calendar/.../feature/calendar/preview/GoogleCalendarEventPreviewScreen.kt` | Done |

## Google Tasks

Promoted to "Done" in
[§84](m3-expressive-adoption.md#84-calendargoogle-tasks--full-adoption-pass-promoted-to-done), alongside
Calendar (see above for the shared-history links and this pass's `CloudBubble.kt` findings). §84's own
Google-Tasks-specific fix: `GoogleTasksScreen.kt`'s `TaskListTile` had a literal `RoundedCornerShape(12.dp)`
→ `MaterialTheme.shapes.medium`; its `color.copy(alpha = 0.12f)` tinted background was reviewed and kept —
`color` is the task list's own chosen accent color, the same legitimate "tint from a brand color" pattern
already established elsewhere (§80), not the de-emphasis anti-pattern.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Google Task Lists | Compose | `feature/feature-googletask/.../feature/googletask/GoogleTasksScreen.kt` | Done |
| Task List (tasks in a list) | Compose | `feature/feature-googletask/.../feature/googletask/TaskListScreen.kt` | Done |
| Task Preview | Compose | `feature/feature-googletask/.../feature/googletask/preview/PreviewGoogleTaskScreen.kt` | Done |
| Task Editor | Compose | `feature/feature-googletask/.../feature/googletask/task/EditGoogleTaskScreen.kt` | Done |
| Task List Editor | Compose | `feature/feature-googletask/.../feature/googletask/tasklist/EditGoogleTaskListScreen.kt` | Done |

## Workflow (automation rules)

Audited — see [`m3-expressive-adoption.md` §7](m3-expressive-adoption.md#7-workflow--routines-screens--audit).
All 4 rows moved to "In progress":
[§40](m3-expressive-adoption.md#40-workflowroutines-screens-back-button-content-description-fixes--landed)
fixed the back-button `contentDescription = null` bug §7 found on all 8 of 8 screens in this group (the
worst hit rate of any group audited so far), and
[§43](m3-expressive-adoption.md#43-workflowroutines-topappbars-pointed-at-shared-topappbarcolor-token--landed)
pointed all 4 of these screens' `TopAppBar`s at the shared `TopAppbarColor` token. §7 has no remaining
findings for this group; what's left (`RoutinePreviewScreen.kt`'s deprecated FAB/sub-48dp touch target,
`RoutineExecutionScreen.kt`'s `FontWeight`/elevation gaps) is tracked under Routines below. A fresh full
re-audit in
[§69](m3-expressive-adoption.md#69-workflowroutines-group--fresh-re-audit-modifier-order-fontweightemphasized-drop-shadow-touch-target-and-motion-spec-fixes--landed)
confirmed all 6 of this module's composables already had `modifier` first (no fix needed) and found no other
gaps — `feature-workflow` is fully closed out.
[§76](m3-expressive-adoption.md#76-workflowroutines-group--re-verification-of-7-40-43-69s-claims) re-verified
every §7/§40-§43/§69 claim a third time and found nothing new. A full adoption pass in
[§80](m3-expressive-adoption.md#80-workflowroutines--full-adoption-pass-promoted-to-done) worked the
screen-audit checklist's non-mechanical items against that clean baseline — including, for the first time,
the shared `ui-routine` module components (`RoutineCard.kt`, `CircularStepTimer.kt`, `RoutineColorPicker.kt`,
`RoutineIconPicker.kt`) no prior pass of this group had read — found and fixed
`RoutineIconPicker.kt`'s sub-48dp trigger bubble and `BuilderListItemCard.kt`'s `modifier`-parameter order,
and **promoted all 4 rows to "Done."**

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Workflow Gallery | Compose | `feature/feature-workflow/.../feature/workflow/WorkflowGalleryScreen.kt` | Done |
| Workflow Rules for Group | Compose | `feature/feature-workflow/.../feature/workflow/WorkflowRulesForGroupScreen.kt` | Done |
| Workflow Rules for Reminder | Compose | `feature/feature-workflow/.../feature/workflow/WorkflowRulesForReminderScreen.kt` | Done |
| Workflow Rule Builder | Compose | `feature/feature-workflow/.../feature/workflow/builder/WorkflowRuleBuilderScreen.kt` | Done |

## Routines

New feature area (module `feature-routine`) added since this doc was first written. Audited — see
[`m3-expressive-adoption.md` §7](m3-expressive-adoption.md#7-workflow--routines-screens--audit). Routine
Editor was already "In progress": its color picker (`RoutineColorPicker` → shared `ColorPickerCard`/
`ColorSlider`) was fixed for accessibility in
[§16](m3-expressive-adoption.md#16-colorslider-accessibility-fix--landed). The other 3 rows join it now:
[§40](m3-expressive-adoption.md#40-workflowroutines-screens-back-button-content-description-fixes--landed)
fixed their back-button bug too, and
[§41](m3-expressive-adoption.md#41-routinepreviewscreenkt-deprecated-baseline-fab-fix--landed) replaced
`RoutinePreviewScreen.kt`'s deprecated baseline `ExtendedFloatingActionButton` with
`SmallExtendedFloatingActionButton`, and
[§42](m3-expressive-adoption.md#42-routineslistscreenkts-routinesemptystate-migrated-onto-shared-emptystatekt--landed)
migrated `RoutinesListScreen.kt`'s alpha-blended `RoutinesEmptyState` onto `ui-common`'s shared `EmptyState`,
and
[§43](m3-expressive-adoption.md#43-workflowroutines-topappbars-pointed-at-shared-topappbarcolor-token--landed)
pointed `RoutinesListScreen.kt`'s and `RoutinePreviewScreen.kt`'s `TopAppBar`s at the shared `TopAppbarColor`
token (`RoutineEditScreen.kt`/`RoutineExecutionScreen.kt` already used it correctly). §7 has no remaining
findings that don't require touching `RoutineExecutionScreen.kt` or `RoutinePreviewScreen.kt` anyway —
[§69](m3-expressive-adoption.md#69-workflowroutines-group--fresh-re-audit-modifier-order-fontweightemphasized-drop-shadow-touch-target-and-motion-spec-fixes--landed)
closed out the last two: `RoutinePreviewScreen.kt`'s 40dp check-toggle touch target is now 48dp and its
literal `tween()` motion is now `MaterialTheme.motionScheme`-driven, and `RoutineExecutionScreen.kt`'s
`FontWeight.Bold` cluster is now `*Emphasized` typography and its off-scale `shadowElevation = 4.dp` bottom
bar is now a `surfaceContainer` color fill. §69 also fixed a `modifier`-parameter-order gap across all 4
screens plus 6 private composables in this module (`feature-workflow`'s composables already had it right).
Only §7's two explicitly-logged non-defects remain: `RoutineEditScreen.kt`'s indirect up-chevron-via-rotation
implementation and `WorkflowTemplateCard.kt`'s `titleSmall`-for-description type-role mismatch.
[§76](m3-expressive-adoption.md#76-workflowroutines-group--re-verification-of-7-40-43-69s-claims) re-verified
every claim above a third time and found nothing new — the cleanest re-verification result in this group's
history. A full adoption pass in
[§80](m3-expressive-adoption.md#80-workflowroutines--full-adoption-pass-promoted-to-done) read the shared
`ui-routine` module (`RoutineCard.kt`, `CircularStepTimer.kt`, `RoutineColorPicker.kt`,
`RoutineIconPicker.kt`) for the first time in this group's audit history, fixed
`RoutineIconPicker.kt`'s 44dp icon-picker trigger bubble (below the 48dp touch-target minimum) and
`BuilderListItemCard.kt`'s `modifier`-parameter order, deliberately left `CircularStepTimer.kt`'s countdown
text as plain `displaySmall` (a considered "don't dilute the screen's one hero moment" call, not an
oversight), and **promoted all 4 rows to "Done."**

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Routines List | Compose | `feature/feature-routine/.../feature/routine/list/RoutinesListScreen.kt` | Done |
| Routine Editor | Compose | `feature/feature-routine/.../feature/routine/edit/RoutineEditScreen.kt` | Done |
| Routine Preview | Compose | `feature/feature-routine/.../feature/routine/preview/RoutinePreviewScreen.kt` | Done |
| Routine Execution | Compose | `feature/feature-routine/.../feature/routine/execution/RoutineExecutionScreen.kt` | Done |

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
progress." Notification Customization Help moves to "Done": §11's own audit text found it already the one
screen in its batch getting both the back-button description and `TopAppbarColor` right, with "little
further Compose-layer surface area to audit" — re-verified directly against current source in
[§48](m3-expressive-adoption.md#48-leftover-audited-only-screens-re-verified-and-promoted-to-done--landed),
no code changes needed. `ManagePresetsScreen.kt`'s alpha-blended private `EmptyState` (§11 finding, item 3 of
its suggested fix order) has since been migrated onto the shared `EmptyState.kt` — see
[§59](m3-expressive-adoption.md#59-managepresetsscreenkt-empty-state-fix--landed).
[§60](m3-expressive-adoption.md#60-holidaycountryscreenkt-alpha-blend-fix-and-generalsettingsscreensinglechoicedialog-dedup--landed)
landed the same finding's `HolidayCountryScreen.kt` half (a plain token swap — its empty state has no icon,
so it doesn't fit `EmptyState.kt`'s shape), and separately deduped `GeneralSettingsScreen`'s private
`SingleChoiceDialog` onto the shared `ui-common` one (§11 finding #6/item 4), porting that screen's
height-cap/scroll behavior into the shared component first since the two weren't byte-identical.
[§61](m3-expressive-adoption.md#61-shared-seekvaluedialog-consolidation--landed) landed §11's last item:
the seek/slider dialogs in `RemindersSettingsScreen.kt`, `BirthdaySettingsScreen.kt`,
`NoteSettingsScreen.kt`, and `LocationSettingsScreen.kt` (which turned out to have two, not one) all now
call one shared `SeekValueDialog` in `ui-common`. §11's suggested fix order is now fully closed.
[§62](m3-expressive-adoption.md#62-12-items-4-and-5--headeritemssettingsscreenkt-drag-handle-a11y-fix-and-proversionscreenkt-emphasized-type--landed)
landed §12's last two items: `HeaderItemsSettingsScreen.kt`'s drag handle now has a real 48dp touch target
and TalkBack-reachable move-up/move-down actions (following `SubTasksValueEditor.kt`'s reference pattern),
and `ProVersionScreen.kt`'s headline/advantage text now uses real `Emphasized` typography tokens instead of
color alone to carry its hero-moment weight. §12's suggested fix order is now fully closed too.
[§63](m3-expressive-adoption.md#63-screen-inventory-re-diff-negative-result-and-othersettingsscreenkt-icon-fix--landed)
re-diffed `*Screen.kt` files against this doc again (the §55 method) and found no new gaps this time — the
inventory is currently complete — and separately fixed `OtherSettingsScreen.kt`'s two icon-less rows
(Permissions, Allow Permission), the one remaining §12 screen-specific finding cheap enough to land outright.
[§77](m3-expressive-adoption.md#77-settings-group--re-verification-of-11-12-14-15-59-63s-claims) re-verified
every claim above and found nothing new — a clean result. A full adoption pass in
[§81](m3-expressive-adoption.md#81-settings--full-adoption-pass-promoted-to-done) — the largest of the three
"Done"-promotion passes — found and fixed what no prior section had checked: `SettingsScaffold.kt`'s
`modifier`-parameter order (rippling to ~24 screens, all call sites verified named-argument-safe first) plus
20 individual screens' own `modifier` order, and a ~35-call-site `DrawableCatalog`/`AppIcons` cleanup across
8 files that had never had one. Also landed `SettingsItem.kt`'s literal `tween()` motion, flagged by §11
itself but never fixed. `SettingsItem.kt`'s own `modifier` order was deliberately left alone — used by 26+
files across 6 modules, many via a single unnamed positional `title` argument, the same "wider convention
gap" shape §66/§79 already established for `ColorPickerCard.kt`. **Promoted all 29 rows to "Done"** except
`Developer`/`Object Export`, which stay explicitly out of scope (debug-only).

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

Promoted to "Done" in
[§82](m3-expressive-adoption.md#82-backupinsightsonboardingwidget-configuration--full-adoption-pass-promoted-to-done),
a full adoption pass on top of the original audit
([§13](m3-expressive-adoption.md#13-backup--insights--onboarding--widget-configuration-screens--audit)) and
its landed fixes (back-button content descriptions, §44; shared `TopAppbarColor`, §45; `InsightsScreen.kt`'s
alpha-blend cleanup, §46). §82 removed a dead-import leftover on `LocalBackupScreen.kt` and investigated (but
could not cleanly fix, for a documented real-API-constraint reason) `InsightsScreen.kt`'s bar-chart corner
radius. Still open, deliberately not required for "Done": both screens' baseline
`CircularProgressIndicator` — the newer `LoadingIndicator` replacement needs an `@ExperimentalMaterial3ExpressiveApi`
opt-in nothing else in the app uses yet.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Local Backup (Export) | Compose | `extensions/localbackup/.../localbackup/compose/LocalBackupScreen.kt` | Done |
| Local Backup (Import) | Compose | same file, `LocalBackupNavKey.Import` | Done |
| Insights Dashboard | Compose | `feature/feature-insights/.../insights/compose/InsightsScreen.kt` | Done |

## App Shell (Splash)

Promoted to "Done" in
[§82](m3-expressive-adoption.md#82-backupinsightsonboardingwidget-configuration--full-adoption-pass-promoted-to-done).
`BottomNavSplashScreen.kt` was missing from this doc entirely until
[§55](m3-expressive-adoption.md#55-onboarding--splash-screens--audit) added it; its app-name reveal animation
was migrated onto `MaterialTheme.motionScheme` in
[§57](m3-expressive-adoption.md#57-bottomnavsplashscreenkt-motion-scheme-fix--landed). §82 re-read both files
fresh: no back button (correctly, it's a transient splash), no app bar, no alpha-blend, and its
`AnimatedGradientBackground` backdrop's own literal `tween()` is a deliberate, correct exception (an
unbounded ambient loop, not the kind of state transition `motionScheme` specs are for). `AppLauncherIcon.kt`'s
raw `painterResource(R.drawable.ic_launcher_foreground)` and hardcoded launcher-gradient hex values remain a
documented, justified exception (recreating an adaptive launcher icon from its mipmap leaves, pixel-matching
the actual OS asset) — not `DrawableCatalog`/color-token gaps.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| Bottom Nav Splash | Compose | `app/.../navigation/BottomNavSplashScreen.kt`, `AppLauncherIcon.kt` | Done |

## Onboarding / Login

Promoted to "Done" in
[§82](m3-expressive-adoption.md#82-backupinsightsonboardingwidget-configuration--full-adoption-pass-promoted-to-done).
`PinLoginScreen.kt`/`PinInput.kt` were already this group's positive counter-example on content descriptions
(§13/§44) and had their icons cataloged in
[§47](m3-expressive-adoption.md#47-widgetconfigscaffoldktpinloginscreenktpininputkt-drawablecatalog-cleanup--landed);
§82 fixed the one remaining item, `PinInput.kt`'s off-scale `2.dp` tonal elevation on `PinDigitButton`
(changed to the correct `1.dp` resting level). `PinLoginActivity.kt`, read directly for the first time this
pass, is pure Activity glue with no theming decisions.

`OnboardingScreen.kt` was missing from this doc entirely until
[§55](m3-expressive-adoption.md#55-onboarding--splash-screens--audit) added it; its alpha-blend page-indicator
bug was fixed in [§56](m3-expressive-adoption.md#56-onboardingscreenkt-alpha-blend-fix--landed). §82 re-read
it fresh: icons already route through `AppIcons.Fluent.*`, no manual `FontWeight` override exists to trigger
the `*Emphasized` typography migration, and its translucent icon-chip background
(`surface.copy(alpha = 0.85f)` over `AnimatedGradientBackground`) remains a deliberate, different
"frosted glass over gradient" effect, not the de-emphasis pattern item 1 targeted.

| Screen | Type | File(s) | Status |
|---|---|---|---|
| PIN Login | Compose (Activity-hosted) | `ui/ui-common/.../ui/common/login/PinLoginScreen.kt`, `PinLoginActivity.kt` | Done |
| Onboarding | Compose | `feature/feature-onboarding/.../onboarding/compose/OnboardingScreen.kt` | Done |

## Widget Configuration (appwidgets module)

Promoted to "Done" in
[§82](m3-expressive-adoption.md#82-backupinsightsonboardingwidget-configuration--full-adoption-pass-promoted-to-done).
The shared `WidgetConfigScaffold.kt` (back button, app-bar color token, `AppIcons.Fluent.Dismiss`) and
`ColorSlider` (accessibility semantics) were fixed in §13/§14/§16/§47, and a fresh full re-audit in
[§65](m3-expressive-adoption.md#65-widget-configuration-group--fresh-re-audit-and-two-fixes--landed) fixed
the group's `modifier`-parameter-order gap and migrated `EventsWidgetConfigScreen.kt`'s hand-rolled text-size
dialog onto the shared `SeekValueDialog`. §82 re-read all 7 screens' full bodies against the guidelines'
complete per-screen checklist (not just re-checking prior findings) and found nothing further: every
screen's real (non-mock-preview) content correctly uses `AppShapes`/`MaterialTheme.shapes`, on-scale
elevation, and no manual typography overrides; the mock-preview composables' raw `painterResource`/
`dimensionResource` calls remain the established, deliberate "matches the real home-screen widget" exception,
not a `DrawableCatalog`/shape-token gap. Still open, low-priority: `EventsWidgetConfigScreen.kt`'s
dialog-based vs. `SingleNoteWidgetConfigScreen.kt`'s inline-`Slider`-based text-size UX inconsistency
(a product call, not a compliance gap).

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
