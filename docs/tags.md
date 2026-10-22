# Tags

A `Tag` (name + color) is a cross-entity label. `TagAssignment` rows attach it to any number of items.
This replaces `tags-future-roadmap.md`, which was a plan for work that has since shipped.

## What exists

- **Domain:** `Tag`, `TagAssignment` and `TaggedItemType` in `core:domain`. Taggable types today: `REMINDER`,
  `NOTE`, `BIRTHDAY`, `GOOGLE_TASK`, `ROUTINE`.
- **Persistence:** `TagRepository` / `TagAssignmentRepository` (`data:repository-api`), Room-backed in the shared
  `AppDb`. `TagAssignmentEntity` deliberately has no foreign key to the tagged item, so adding a
  `TaggedItemType` case needs no schema change.
- **UI:** `ui:ui-tag` (`TagChipPicker`, `TagChipRow`, `TagFilterRow`), and `feature:feature-tags` (manage/edit
  screens and a tag details screen listing every item that carries a tag). `logic:logic-tag` holds
  `ToggleTagAssignmentUseCase`.
- **Filtering:** tag filter chips on the Notes, Birthdays, Google Tasks and Agenda lists.
- **Sync and backup:** `Tag` syncs as its own file type. `TagAssignment` syncs as a whole-collection snapshot
  that is replaced wholesale on download, because a missing row means "untagged on this device". Local
  encrypted backup does the same (`replaceAll()`).
- **Workflows:** the `HasTag` condition and the `ApplyTag` / `RemoveTag` actions (see [workflows.md](workflows.md)).

## Adding a new taggable type

1. Add the `TaggedItemType` case.
2. Wire `TagChipPicker` into the item's edit ViewModel/screen and call `ToggleTagAssignmentUseCase`.
3. Schedule `WorkType.Upload` for `DataType.TagAssignments` after attach/detach.
4. Optionally add a `TagFilterRow` to the list screen.

Cloud sync and backup need no change; they serialize whatever assignment rows exist.

## Constraints worth remembering

- **Google Tasks tags are a local overlay.** The Tasks API has no label field, so they travel only through this
  app's own sync and backup, never to Google. A reminder-linked Google Task and its linked reminder can carry
  independent tag sets.
- **Flat namespace.** No hierarchy. `TagChipPicker` renders every tag unfiltered; it would need search if users
  accumulate dozens.

## Not built

- Tag-based insights (busiest tags, distribution over time).
- Tag-scoped export (a filtered variant of `LocalBackupApiImpl.export()`).
