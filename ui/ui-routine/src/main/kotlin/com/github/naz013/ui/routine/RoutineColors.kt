package com.github.naz013.ui.routine

import androidx.compose.ui.graphics.Color

/** Solid color palette for routines - intentionally identical to `NoteColorEngine`'s palette
 * (`ui-note`) so routines and notes share the same set of colors. Copied here by value rather than
 * depending on `ui-note` directly, since no `ui-*` -> `ui-*` dependency exists anywhere in this
 * codebase (see `docs/architecture.md`'s `ui-<feature>` allowed-dependency list). Keep in sync by
 * hand if the Notes palette ever changes. */
object RoutineColors {
  val ALL = listOf(
    Color(0XFF86E3CE),
    Color(0XFFD0E6A5),
    Color(0XFFFFDD94),
    Color(0XFFFA897B),
    Color(0XFFCCABD8),

    Color(0XFFF5CEC7),
    Color(0XFFE79796),
    Color(0XFFFFC98B),
    Color(0XFFFFB284),
    Color(0XFFC6C09C),

    Color(0XFFAAC9CE),
    Color(0XFFB6B4C2),
    Color(0XFFC9BBC8),
    Color(0XFFE5C1CD),
    Color(0XFFF3DBCF),

    Color(0XFF80BEAF),
    Color(0XFFB3DDD1),
    Color(0XFFD1DCE2),
    Color(0XFFF5B994),
    Color(0XFFEE9C6C),

    Color(0XFFCCABD8),
    Color(0XFF8474A1),
    Color(0XFF6EC6CA),
    Color(0XFF08979D),
    Color(0XFF055B5C),

    Color(0XFFFCF5EF),
    Color(0XFFFEA735),
    Color(0XFFFE7235),
    Color(0XFF00C3FF),
    Color(0XFF0077FF),

    Color(0XFF513485),
    Color(0XFF7E5AB8),
    Color(0XFFD79CD2),
    Color(0XFFFDD51E),
    Color(0XFF758B5C),

    Color(0XFF999999),
    Color(0XFF555555),
    Color(0XFFFB6602),
    Color(0XFFF6A705),
    Color(0XFFAF948F),

    Color(0XFFAB0068),
    Color(0XFFE00702),
    Color(0XFFFF6C02),
    Color(0XFFFEC106),
    Color(0XFFFF4747),

    Color(0XFF00BCB4),
    Color(0XFFC4E86B),
    Color(0XFFFFB547),
    Color(0XFF7EC384),
    Color(0XFF3F8756),

    Color(0XFF45625D),
    Color(0XFF6AA5A9),
    Color(0XFFFFCEBE),
    Color(0XFFCD2C6C),
    Color(0XFF5F023A),

    Color(0XFF108292),
    Color(0XFF8B776D),
    Color(0XFF463C52),
    Color(0XFF9D94BA),
    Color(0XFFD1D5E8),
  )
}
