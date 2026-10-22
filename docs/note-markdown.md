# Note Markdown and "Share as"

## Typing shortcuts

The note editor recognises line-level Markdown markers as the user types. Typing a single space right
after a marker at the very start of a line removes the marker and formats that line:

| Typed at line start | Result |
|---|---|
| `# ` | Heading 1 |
| `## ` | Heading 2 |
| `### ` | Heading 3 |
| `- ` or `* ` | Bullet item |

- Only a single typed space triggers it; pastes, speech results, restores and loading never do.
- Markers not at the start of a line, and four or more `#`, are left as plain text.
- Spans after the marker are shifted so existing formatting stays aligned; the cursor lands at the line start.
- There is no inline conversion (`**bold**` stays literal text).

Detection lives in `create/MarkdownShortcuts.kt` (`detectMarkdownShortcut`); the hook is
`NoteEditViewModel.onTextFieldValueChange`.

## Share as

The note preview overflow menu and the editor's share button open one bottom sheet (`NoteShareSheet`)
with four formats (`NoteShareFormat`):

| Format | Payload |
|---|---|
| Original note | The `.note` file from `CreateSharedNoteFileUseCase`, sent by the existing `NoteIntentSender.send` flow |
| Plain text | `NoteDocument.text` only, `text/plain` |
| Markdown | Hybrid Markdown (below), `text/plain` |
| HTML | A complete HTML document, `text/html` |

The subject of every text format is the note title. Text formats travel in `ACTION_SEND` as `EXTRA_TEXT`; HTML
also sets `EXTRA_HTML_TEXT` to the same document. HTML is shared as text rather than as an `.html` file because
that needs no FileProvider entry or temp file, and receiving apps that understand `EXTRA_HTML_TEXT` render it.
`NoteShareContentBuilder` produces the payload; the view models emit `ShareText(NoteShareText)`.

### Hybrid Markdown

Markdown expresses what it can, and inline HTML carries the rest so no formatting is lost.

| Note formatting | Output |
|---|---|
| Heading 1-3 | `#` / `##` / `###` prefix |
| Bullet | `- ` prefix |
| Bold / Italic / both | `**` / `*` / `***` |
| Strikethrough | `~~` |
| Underline | `<u>` |
| Solid color | `<span style="color:#RRGGBB">`, or `rgba(r,g,b,a)` when alpha is not 255 |
| Gradient | `<span style="background:linear-gradient(<angle>deg,<hex stops>);-webkit-background-clip:text;color:transparent">` |
| Font size | `font-size:<n>px` in a span style |
| Font family | `font-family:'<name>'` in a span style; the code is resolved through `NoteFontProvider.getFontNames()` and omitted when unknown |

Rules:
- Color, gradient, size and family covering the same characters are merged into one `<span>`. Where several
  spans of one family overlap (two colors, say) the last one in the span list wins.
- HTML wraps outermost and Markdown markers sit inside: `<span ...><u>~~**text**~~</u></span>`.
- Spans are split into per-line runs, so a multi-line span is closed and reopened on every line.
- Leading and trailing blanks of a run stay outside all markers and tags; whitespace-only ranges are never wrapped.
- Adjacent decorated runs are separated by `<!---->` so their `*` / `~` markers do not fuse.
- Literal text is not escaped (so a literal `<b>` in a note stays raw HTML), with one exception: a plain line that
  starts like a heading or bullet (`# `, `- `, `* `, `+ `) gets a leading backslash.
- Consecutive plain lines end with two spaces (hard break) and a plain line right after a bullet is preceded by a
  blank line, so line breaks survive rendering.

### HTML

`<!DOCTYPE html>` document with `<meta charset="UTF-8">`, `<title>` = note title and the note colors on `<body>`
(`background-color`, `color`). Headings become `<h1>`-`<h3>`, consecutive bullet lines one `<ul>`, plain lines
`<p>` with `<br>` between lines (a blank line starts a new paragraph, repeated blank lines collapse). Inline
formatting uses `<strong>`, `<em>`, `<s>`, `<u>` and the same style span as Markdown. All text is escaped
(`& < > "`).

Both converters share the span flattening in `share/NoteFlattening.kt`; they live in `share/NoteMarkdownExporter.kt`
and `share/NoteHtmlExporter.kt`.
