# Note Title/Body Text — Jetpack Compose Line-Height Behavior

This documents a Compose text-rendering gotcha hit twice now (once in the note-create rewrite,
REM-1027; once in the note-preview rewrite, REM-1030) and the rule to follow so it doesn't
resurface in the next note-related Compose screen.

## The problem

Notes let the user pick an arbitrary font size (6–150sp) and font family (custom typefaces from
assets) per note, independently for the title and the body. When that size/family is applied to a
`Text`/`TextField` by only overriding `fontSize`/`fontFamily`, the rendered line spacing does not
scale with the font size the way the legacy `TextView.setTextSize()` behavior did:

- Large font sizes: lines visually overlap/clip into each other.
- Small font sizes: lines are spaced far apart, with excessive empty gaps.

## Root cause

`Text`'s default style (and `TextField`'s, when not given an explicit `textStyle`) resolves from
`LocalTextStyle.current`, which is `MaterialTheme.typography.bodyLarge` — a `TextStyle` with both
`fontSize` (16sp) **and** `lineHeight` (24sp) fixed for that type scale. Two ways of applying a
custom size hit the same trap:

- `Text(fontSize = x.sp, fontFamily = ...)` — the convenience params get merged onto
  `LocalTextStyle.current`; `lineHeight` isn't part of the merge, so `bodyLarge`'s 24sp survives.
- `MaterialTheme.typography.bodyLarge.copy(fontSize = x.sp, fontFamily = ...)` — `.copy()` only
  changes the fields you pass; `lineHeight` again stays at 24sp.

Either way, `lineHeight` ends up as a **fixed 24sp** regardless of the actual `fontSize` in use,
because it was never told to derive from the new size.

## The fix

Explicitly set `lineHeight = TextUnit.Unspecified` whenever the font size and/or font family is
note-controlled (not a fixed design-system value):

```kotlin
Text(
  text = state.text,
  style = MaterialTheme.typography.bodyLarge.copy(
    color = contentColor,
    fontSize = state.textSize.sp,
    fontFamily = state.typeface?.let { FontFamily(it) } ?: FontFamily.Default,
    lineHeight = TextUnit.Unspecified
  )
)
```

`TextUnit.Unspecified` tells Compose to derive the line height from the resolved font's own
metrics at the actual `fontSize`, instead of inheriting a fixed value from the base type scale —
this is what restores the auto-scaling behavior `TextView` gave for free. Same rule applies to
`TextField(textStyle = ...)`.

## Rule for future note screens

**Any composable that renders or edits note title/body text with a font size or font family that
comes from note data must set `lineHeight = TextUnit.Unspecified` in its `TextStyle`.** This is
independent of whether the text is editable (`TextField`) or read-only (`Text`).

Applies today in:
- `notes/create/NoteEditScreen.kt` — title/body `TextField`s
- `notes/preview/PreviewNoteScreen.kt` — title/body `Text`s

Apply the same rule in any future Compose rework that displays note title/body content with its
own font size/family — e.g. a Compose notes-list item, widget preview, or share-sheet preview.

If a third screen ends up needing this, consider extracting a small shared helper (e.g.
`noteTextStyle(fontSize, fontFamily, color): TextStyle` in `ui-common`) instead of copy-pasting the
`.copy(..., lineHeight = TextUnit.Unspecified)` pattern a third time — not done yet since two
call sites don't justify the abstraction.
