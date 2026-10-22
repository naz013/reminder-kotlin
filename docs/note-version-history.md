# Note version history

Saving an existing note from the editor first stores the previously saved `Note.content` (text and spans) as a
`NoteVersion`, but only when the content actually changed. Color, tags and image-only edits create no version, new
notes have none, and a save is skipped when the newest version is less than 60 seconds old. At most 20 versions are
kept per note (oldest pruned). `SaveNoteVersionUseCase` holds this logic; `NoteEditViewModel.saveNote` calls it and a
failure there never blocks the save.

Versions are **local only**: they live in the Room table `NoteVersion` (migration 45 to 46, `NoteVersionRepository`
in `data:repository-api`) and are deliberately absent from the `*Json` DTOs, `DataConverterImpl`, `data:sync` and
backups. They are deleted with their note (`NoteRepositoryImpl.delete` / `deleteAllNotes`).

The note preview overflow menu has a "Version history" action opening a bottom sheet (`NoteVersionHistorySheet`):
the list shows timestamp and the first two lines; tapping one shows its full text read-only with a Restore button.
`RestoreNoteVersionUseCase` snapshots the current content (always, ignoring the 60 s rule), replaces the content,
bumps the note version and marks it `WaitingForUpload` so it re-syncs normally.

## Compare with current

Selecting a version in the history sheet offers "Compare with current", which closes the sheet and opens the full
screen `NotesNavKey.CompareVersion(noteKey, versionId)` (`feature/note/compare`). `CompareNoteVersionViewModel`
loads the version (`NoteVersionRepository`) and the current note (`NoteRepository`) and exposes a
`CompareNoteVersionState`; a missing version, note, or a version of another note yields `CompareStatus.Unavailable`.

`diffLines` is a pure line-level LCS diff: lines only in the version are `Removed` (tinted in the history pane), lines
only in the current text are `Added` (tinted in the current pane), each prefixed with a `-` / `+` marker so the
change is not conveyed by color alone. Identical texts show a hint banner. Layout comes from `compareLayoutFor`
over `DeviceScreenConfiguration`: `MobilePortrait` stacks the panes (version on top, current below), every wider
bucket shows them side by side (version left, current right); each pane scrolls independently.

The screen also has a Restore action that reuses `RestoreNoteVersionUseCase` and pops back to the note preview.
