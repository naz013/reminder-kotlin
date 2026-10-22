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
