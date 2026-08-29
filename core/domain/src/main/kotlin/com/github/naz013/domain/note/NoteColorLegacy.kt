package com.github.naz013.domain.note

/** Size of one "palette" group in the pre-removal (color, palette) split - `Note.color` used to
 * be a 0-based position within a 20-color group and `Note.palette` selected which group, so a
 * note's actual displayed color was `palette * 20 + color`. Both concepts have since merged into
 * one flat `Note.color` (there is no more `Note.palette`), but old Room rows and old backup/
 * share files (`NoteV3Json`, `NoteV4Json`, `OldNote`, `SharedNote`) still store the pair - this
 * combines them back into the flat index those old sources meant, so reading them stays correct.
 */
private const val LEGACY_PALETTE_SIZE = 20

/** Folds a legacy (color, palette) pair - as still found in old backup/share files and pre-
 * migration Room rows - into the flat color index the current single-field model expects. */
fun combineLegacyNoteColor(color: Int, palette: Int): Int = palette * LEGACY_PALETTE_SIZE + color
