# Note Markdown shortcuts

The note editor recognises line-level Markdown markers as the user types. Typing a single space right
after a marker at the very start of a line removes the marker and formats that line:

| Typed at line start | Result |
|---|---|
| `# ` | Heading 1 |
| `## ` | Heading 2 |
| `### ` | Heading 3 |
| `- ` or `* ` | Bullet item |

Rules:
- Only a single typed space triggers it; pastes, speech results, restores and loading never do.
- Markers not at the start of a line, and four or more `#`, are left as plain text.
- Spans after the marker are shifted so existing formatting stays aligned; the cursor lands at the line start.
- There is no inline conversion (`**bold**` stays literal text) and the editor has no undo stack to integrate with.

Detection lives in `MarkdownShortcuts.kt` (`detectMarkdownShortcut`); the hook is
`NoteEditViewModel.onTextFieldValueChange`.

Markdown export of notes is not implemented.
