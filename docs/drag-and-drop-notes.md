# Drag and Drop for Note Editing — Implementation Plan

## Overview

This document describes the plan for extending the Drag and Drop (D&D) feature in the Note
creation/editing screen (`CreateNoteFragment` + `CreateNoteViewModel`). The goal is to support
dropping not only images but also **plain text**, **text files** (`.txt`, `.md`, `.csv`, etc.),
and **PDF files** into the note editor.

---

## Current State Analysis

### What already exists

| Component | Description |
|-----------|-------------|
| `ViewUtils.registerDragAndDrop` | Registers a drag listener on a View; handles visual highlight, permission request, and forwards `ClipData` on drop |
| `CreateNoteFragment.onStart()` | Registers D&D on the fragment's window decor view for `MIMETYPE_TEXT_PLAIN` and `UriUtil.ANY_MIME` |
| `CreateNoteViewModel.parseDrop()` | Receives `ClipData`; extracts inline text or routes all URIs to `addMultiple()` |
| `ImageDecoder` | Decodes URIs into `UiNoteImage`; **rejects** any non-`image/*` MIME type as `ERROR` |

### Gaps / Bugs

1. **Text files dropped as URIs** go through `addMultiple()` → `ImageDecoder` → rejected with `ERROR` state.
2. **PDF files** have no handling whatsoever.
3. **Null URIs** for text-only clip items are added to the URI list (minor NPE risk).
4. **Mixed drops** (some images + some text files) only process the first category recognised.
5. No user feedback when a dropped file type is not supported.

---

## Architecture of the New Flow

```
ClipData
   │
   ▼
DroppedContentParser.parse(clipData)
   │
   ├─ InlineText    ──────────────────────────────► combine into note text
   ├─ ImageUri      ──────► ImageDecoder ──────────► images list
   ├─ TextFileUri   ──────► ContentResolver.open ──► combine into note text
   ├─ PdfUri        ──────► PDFBox text strip ──────► combine into note text
   └─ UnsupportedUri ─────────────────────────────► show error toast
```

---

## Implementation Phases

### Phase 1 — Sealed Type Hierarchy ✅

**File:** `app/src/main/java/com/elementary/tasks/notes/create/drop/DroppedItemType.kt`

Introduces a sealed class `DroppedItemType` to represent each clip item after classification:

| Subtype | Description |
|---------|-------------|
| `InlineText` | Pure text carried directly in the clip item |
| `ImageUri` | URI resolving to an `image/*` MIME type |
| `TextFileUri` | URI resolving to a `text/*` MIME type |
| `PdfUri` | URI resolving to `application/pdf` |
| `UnsupportedUri` | URI with unrecognised or unhandled MIME type |

---

### Phase 2 — Content Parser ✅

**File:** `app/src/main/java/com/elementary/tasks/notes/create/drop/DroppedContentParser.kt`

A new class `DroppedContentParser(context: Context)` with:

- `parse(clipData: ClipData): ParseResult` — classifies all items, reads content, returns
  aggregated `ParseResult(textContent, imageUris, unsupportedCount)`.
- `resolveMimeType(uri)` — queries `ContentResolver`; falls back to file extension via
  `MimeTypeMap`.
- `readTextFromUri(uri)` — opens an `InputStream` and decodes as UTF-8 text.
- `extractTextFromPdf(uri)` — uses **Apache PDFBox for Android** to strip text from all pages.

Registered in `DI.kt` as `factory { DroppedContentParser(get()) }`.

---

### Phase 3 — PDF Text Extraction Dependency ✅

**Library:** `com.tom-roush:pdfbox-android:2.0.27.0`

Added to:
- `gradle/libs.versions.toml` — new version + library entries.
- `app/build.gradle.kts` — implementation dependency + required packaging exclusions.

---

### Phase 4 — ViewModel Refactoring ✅

**File:** `app/src/main/java/com/elementary/tasks/notes/create/CreateNoteViewModel.kt`

`parseDrop()` is refactored to:

1. Delegate classification and content extraction to `DroppedContentParser`.
2. Combine the existing note text with all extracted text (inline + files + PDF).
3. Pass only image URIs to `addMultiple()`.
4. Show `R.string.unsupported_file_format` toast if any unsupported files were dropped.

`DroppedContentParser` is injected via constructor and registered in `notes/KoinModule.kt`.

---

### Phase 5 — Visual Feedback (Future)

> **Status: Planned (not yet implemented)**

Potential improvements:
- Show a distinct drag-target overlay with accepted file type icons.
- Display a Snackbar instead of a Toast for unsupported types, with a "Learn more" action.
- Animate the text field when text content is successfully extracted.

---

## File Change Summary

| File | Change type |
|------|-------------|
| `docs/drag-and-drop-notes.md` | **New** — this document |
| `notes/create/drop/DroppedItemType.kt` | **New** — sealed type hierarchy |
| `notes/create/drop/DroppedContentParser.kt` | **New** — MIME-based content router + extractors |
| `gradle/libs.versions.toml` | **Modified** — pdfbox-android version + library alias |
| `app/build.gradle.kts` | **Modified** — dependency + packaging excludes |
| `core/utils/DI.kt` | **Modified** — factory for `DroppedContentParser` |
| `notes/KoinModule.kt` | **Modified** — inject `DroppedContentParser` into `CreateNoteViewModel` |
| `notes/create/CreateNoteViewModel.kt` | **Modified** — new constructor param, refactored `parseDrop` |

---

## Progress Tracker

| Phase | Status |
|-------|--------|
| Phase 1 — Sealed Type Hierarchy | ✅ Done |
| Phase 2 — Content Parser | ✅ Done |
| Phase 3 — PDF Dependency | ✅ Done |
| Phase 4 — ViewModel Refactoring | ✅ Done |
| Phase 5 — Visual Feedback | 🔲 Planned |

---

## Implementation Notes

### pdfbox-android package namespace

The `com.tom-roush:pdfbox-android` library uses `com.tom_roush.pdfbox` (underscores, not dashes)
as its Java package root. Imports must use this form:

```kotlin
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
```

### PDFBox initialisation

`PDFBoxResourceLoader.init(applicationContext)` must be called once at application startup
(in `ReminderApp.onCreate`) before any PDF extraction is attempted.

### Kotlin nested block comments

Kotlin supports nested block comments (`/* /* */ */`). Avoid writing `/*` inside KDoc
comment bodies (e.g. in MIME-type examples like `image/*`) because the Kotlin parser
treats them as opening a nested comment that must be explicitly closed.

### Packaging exclusions (pdfbox-android + Bouncy Castle)

The following entries were added to `app/build.gradle.kts` `packaging.resources.excludes`
to prevent duplicate-file merge errors from Bouncy Castle:

```
META-INF/BCKEY.DSA
META-INF/BCKEY.SF
META-INF/BCKEY.RSA
META-INF/BC2048KE.DSA
META-INF/BC2048KE.SF
```

---

## Testing Notes

Unit tests for `DroppedContentParser` should cover:

- Inline text extraction from clip items.
- Image URI classification.
- `.txt` / `.md` URI reading (mocked `ContentResolver`).
- PDF URI text extraction (mocked `InputStream`).
- Graceful handling of null URIs and empty items.
- `ParseResult` aggregation across mixed clip data.
