# Tags: What's Built and What It Unlocks

Tags shipped as a cross-entity label: one `Tag` (name + color) can be attached to any number of
items via `TagAssignment` rows, currently scoped to Reminders and Notes. This document is a survey
of what the existing architecture already makes cheap to extend to Birthdays and Google Tasks, plus
the cross-cutting features (a unified "everything under this tag" view, tag-based Smart Lists, tag
Insights) that become possible once more than one surface shares the same tag. It exists so the next
person picking up tags work starts from what's actually there instead of re-deriving it.

## What's already built

- **Domain model** — [`Tag`](../domain/src/main/kotlin/com/github/naz013/domain/Tag.kt) (`id`,
  `name`, `color`, `version`, `syncState`), [`TagAssignment`](../domain/src/main/kotlin/com/github/naz013/domain/TagAssignment.kt)
  (`tagId`, `itemId`, `itemType`), and the extension point,
  [`TaggedItemType`](../domain/src/main/kotlin/com/github/naz013/domain/TaggedItemType.kt) — today
  just `REMINDER` and `NOTE`.
- **Repository layer** — [`TagRepository`](../repository-api/src/main/java/com/github/naz013/repository/TagRepository.kt)
  / [`TagAssignmentRepository`](../repository-api/src/main/java/com/github/naz013/repository/TagAssignmentRepository.kt)
  in `repository-api`, Room-backed in `repository`, same shape as every other entity in `AppDb`
  (`Tag`/`TagAssignment` tables, migration 29→30). `observeAll()`/`observeTagsForItem()` are
  Flow-based Room queries — a deliberate, scoped exception to the rest of the app's broadcast-based
  `TableChangeNotifier` reactivity, kept because it was already implemented and verified working.
- **UI** — [`TagChipPicker`](../tags/src/main/kotlin/com/github/naz013/tags/compose/TagChipPicker.kt)
  (a `FlowRow` of `FilterChip`s, embeddable in any edit screen), a manage/create screen
  (`TagsScreen.kt`/`TagEditScreen.kt` in `:tags`), wired into
  [`NoteEditViewModel`](../app/src/main/java/com/elementary/tasks/notes/create/NoteEditViewModel.kt)
  and [`BuildReminderViewModel`](../app/src/main/java/com/elementary/tasks/reminder/build/BuildReminderViewModel.kt)
  via `onTagToggle`.
- **Cloud sync** — `Tag` syncs generically as its own file type (`DataType.Tags`, `.tg1`, one file
  per tag, same per-id pipeline as every other entity). `TagAssignment` has no natural single id, so
  it syncs as a **whole-collection snapshot** (`DataType.TagAssignments`, `.tga1`, id `"app"`,
  modeled on the existing `Settings` singleton pattern) — rebuilt fresh on every upload, and
  **replaced wholesale** (not merged) on download, because a missing assignment row is meaningful
  ("this device untagged it").
- **Local backup** — `LocalBackupApiImpl` exports/imports both tables the same way: `Tag` upserts
  per row, `TagAssignment` does a full `replaceAll()`, matching the cloud-download semantics for the
  same reason.
- **Smart Lists precedent** — `AgendaViewModel` already filters the merged Reminders/Birthdays list
  by `SmartListFilter` (`TODAY`/`OVERDUE`/`THIS_WEEK`/`NO_GROUP`), with the predicate logic split
  into a pure per-entity object (`ReminderSmartListPredicate` in `usecase:reminders`,
  `BirthdaySmartListPredicate` in `app` since it needs `BirthdayDateCalculator`). This is the
  template a future **tag-based** smart list filter would follow.
- **Insights precedent** — the `:insights` module is a small set of pure aggregator classes
  (`ReminderStreakCalculator`, `CompletionStatsCalculator`) feeding a dashboard ViewModel. A
  "busiest tags" or "tag distribution" aggregator would slot into this exact shape.

## Why extending this is cheap: the architecture already generalizes

The most important fact for planning future work: **adding a new `TaggedItemType` case touches
almost nothing in the sync/backup layers.**

- `TagAssignmentRepository` has no foreign key to the tagged item, by design (documented in
  `TagAssignmentEntity`'s KDoc) — an assignment row for a type that doesn't exist yet, or a deleted
  item, is harmless. There is no schema change to add `TaggedItemType.BIRTHDAY` or
  `TaggedItemType.GOOGLE_TASK`; it's an enum case.
- Cloud sync's `TagAssignments` snapshot uploads `tagAssignmentRepository.getAll()` verbatim —
  whatever rows exist, regardless of `itemType`, go in the file. Same for local backup's
  `tagAssignmentRepository.getAll()`/`replaceAll()`. Neither pipeline needs to know the new type
  exists.
- What genuinely is per-surface work, every time: wiring `TagChipPicker` into that surface's edit
  ViewModel/screen, calling `scheduleBackgroundWorkUseCase(WorkType.Upload, DataType.TagAssignments)`
  after attach/detach (mirroring `NoteEditViewModel`/`BuildReminderViewModel`'s `onTagToggle`), and
  any list/filter UI that wants to read tags back.

In short: the expensive part of this feature (cross-module architecture, sync, backup) is done.
What's left is UI plumbing, one surface at a time.

## Per-surface opportunities

### Notes — already taggable, but the Notes list can't filter by tag yet

`NoteEditViewModel` lets you attach tags while editing a note, but
[`NotesViewModel`](../app/src/main/java/com/elementary/tasks/notes/list/NotesViewModel.kt) (the list
screen) only filters by a text `searchQuery` — there's no way to browse "just my Recipes notes."
This is the cheapest win in this whole document: no new `TaggedItemType`, no sync changes, just a
tag filter chip row on the Notes list reading from `TagAssignmentRepository.getItemIdsForTag()`
(mirroring `AgendaScreen`'s existing `SmartListChipRow`).

### Reminders — already taggable, but Smart Lists can't filter by tag yet

Same gap as Notes, on the Agenda screen: `BuildReminderViewModel` supports tagging, but
`SmartListFilter` only has `TODAY`/`OVERDUE`/`THIS_WEEK`/`NO_GROUP` — no `TAG(tagId)` case. Adding
one means `SmartListFilter` (currently a plain enum) would need to become capable of carrying a
parameter for this one case, and `AgendaScreen`'s `SmartListChipRow` would need a way to present
"pick a tag" as a filter option (a submenu or a second chip row) rather than a fixed enum of chips.
Once in place, it applies to Reminders and Birthdays' `AgendaViewModel` filtering symmetrically for
free — and to Notes' equivalent list filter above.

### Birthdays — not taggable yet

`Birthday` has no group or tag concept at all today. Tagging would let users mark
Family/Friends/Coworkers, then filter the Birthdays list or the Agenda screen down to one group —
useful given birthdays have no other categorization mechanism (unlike Reminders, which have
`GroupV2`).

- Add `TaggedItemType.BIRTHDAY`.
- Wire `TagChipPicker` into
  [`EditBirthdayViewModel`](../app/src/main/java/com/elementary/tasks/birthdays/create/EditBirthdayViewModel.kt)
  and its screen, exactly like `BuildReminderViewModel.onTagToggle`.
- Extend `AgendaViewModel.filterBirthdays` (already smart-list-aware as of the recent bug fix — see
  `BirthdaySmartListPredicate`) to also accept a tag filter, sharing the tag-based `SmartListFilter`
  case proposed above for Reminders.
- **UX caution**: the birthday add/edit flow is intentionally short (name, date, a couple of
  reminder options). Tag picking should be optional/collapsed by default rather than adding a
  mandatory step to what's meant to be a 10-second entry flow.

### Google Tasks — not taggable yet, and can't sync tags back to Google

`GoogleTask` is a local mirror of a resource owned by Google's Tasks API (`taskId`, `eTag`,
`selfLink`, `kind`, `position` are all Google-assigned/managed fields — see
[`GoogleTask.kt`](../domain/src/main/kotlin/com/github/naz013/domain/GoogleTask.kt)). The Tasks API
has no custom-label field, so **any tags applied here are necessarily a local-only overlay** — they
would ride in this app's own `TagAssignment` sync (Drive/Dropbox) and local backup exactly like
today, but could never round-trip into Google's servers or be visible in Google's own Tasks UI. That
constraint should be stated in-product if this ships, so users don't expect tags to survive
re-adding the same Google account on a fresh install without this app's own backup/sync.

There's also a real design question before writing code:
[`GoogleTaskRepository`](../repository-api/src/main/java/com/github/naz013/repository/GoogleTaskRepository.kt)
already exposes `getByReminderId()`/`getAttachedToReminder()` — some Google Tasks are "promoted"
into a local `ReminderV2` for notifications, and that `ReminderV2` can already carry tags today. If
an ad-hoc `TagChipPicker` is added to the Google Task list/edit UI too, a reminder-linked task would
have *two* independent tag sets (its own, and its linked reminder's) unless one explicitly defers to
the other. The simpler product decision is probably: reminder-linked tasks show/inherit the linked
reminder's tags read-only; only standalone (non-promoted) tasks get their own `TagChipPicker`.

- Add `TaggedItemType.GOOGLE_TASK`.
- `itemId` = `GoogleTask.taskId` (the `@PrimaryKey` in `GoogleTaskEntity`, Google-assigned and
  stable across resync — not the app-local `uuId` field, which exists on the entity but isn't the
  primary key).
- Wire `TagChipPicker` into whatever Google Task edit surface exists, with the reminder-linked
  special case above.

## Cross-cutting payoff: once ≥2 surfaces share tags

These aren't per-surface work — they're new features that only make sense after tags span more than
one entity type, which is already true today (Reminders + Notes) and gets more valuable with each
surface added above.

- **A "tag detail" screen** — tap a tag anywhere (chip picker's manage screen, a Smart List filter,
  an Insights chart) and see every Reminder/Note/Birthday/Google Task carrying it, in one flat list.
  This is the actual point of a cross-entity tag model instead of a per-feature label — nothing
  today surfaces it. Straightforward to build: `TagAssignmentRepository.getItemIdsForTag(tagId)`
  already returns exactly the `(itemId, itemType)` pairs needed; the screen just needs a
  per-`itemType` lookup + a shared list-row renderer (there's already a precedent for merging
  heterogeneous item types into one list: `UiAgendaItemAdapter` does this for Reminders+Birthdays on
  the Agenda screen).
- **Tag-based Smart List filter** — described per-surface above; once built it's shared
  infrastructure, not a one-off.
- **Tag Insights** — a "busiest tags" or "tag distribution over time" aggregator in `:insights`,
  following `ReminderStreakCalculator`'s shape. Meaningful once there's enough tagged data across
  surfaces to be interesting (i.e., after Birthdays and/or Google Tasks join Reminders/Notes).
- **Tag-driven Workflow triggers** — the (currently proposed, not yet built) local-only workflow
  engine documented in [`workflow-engine-research.md`](workflow-engine-research.md) is entirely
  trigger/condition/action over local writes. "When a reminder tagged `Work` is completed, archive
  it automatically" is a natural condition once both features exist — worth keeping in mind if that
  engine gets built, so its condition vocabulary includes tag membership from day one rather than
  bolting it on later.
- **Tag-scoped export** — local backup currently exports everything; a "export only items tagged
  X" mode is a filtered variant of the exact same `LocalBackupApiImpl.export()` path, gated by
  `TagAssignmentRepository.getItemIdsForTag()`.

## Suggested sequencing

Roughly cheapest/highest-leverage first, though these aren't hard dependencies except where noted:

1. **Notes list tag filter** — no new `TaggedItemType`, no sync/backup changes, smallest possible
   slice that makes existing Note tags actually useful for browsing.
2. **Reminders/Agenda tag-based Smart List filter** — same cost profile as #1, and is the piece both
   Birthdays (below) and Google Tasks depend on if they want tag-based filtering rather than just
   tag-based Insights/export.
3. **Birthdays tagging** — new `TaggedItemType`, but mirrors the existing Note/Reminder wiring
   exactly; no open design questions.
4. **The "tag detail" cross-entity screen** — the moment tagging stops being "a label on one item"
   and becomes genuinely cross-entity navigation. Worth doing as soon as ≥3 surfaces are taggable
   (Reminders, Notes, Birthdays) rather than waiting for Google Tasks too.
5. **Google Tasks tagging** — new `TaggedItemType`, but has the one open design question above
   (reminder-linked tasks) that should be settled before writing code, plus a product-facing caveat
   about not round-tripping to Google.
6. **Tag Insights aggregators** — highest payoff once most surfaces are tagged; low value done
   early with only Reminders+Notes data.
7. **Tag-driven Workflow triggers** — blocked on the workflow engine itself existing; a "remember to
   include this" note for whoever builds that engine, not independent work today.

## Open questions worth deciding before this grows further

- **No tag hierarchy or nesting** — today it's a flat namespace. Fine at a handful of tags; if usage
  grows (especially once 4 entity types share the space), flat-list `TagChipPicker` UX and the
  manage screen may need search/grouping before it becomes unwieldy. Worth watching, not solving
  preemptively.
- **`TagChipPicker` renders every tag unfiltered** — fine at current scale; would need a search box
  or virtualization if a user accumulates dozens of tags.
- **Reminder-linked Google Tasks' dual-tag-set problem** — flagged above; needs a product decision,
  not just an engineering one, before Google Tasks tagging ships.
- **`SmartListFilter` becoming parameterized** — introducing a `TAG(tagId)` case turns it from a
  plain `enum class` into something closer to a sealed class, which is a real (if small) API change
  for every existing call site (`AgendaViewModel`, `AgendaScreen`'s chip row, both predicate
  objects). Worth designing once, not per-surface.
