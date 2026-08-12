package com.github.naz013.ui.common.compose.foundation.preview

import androidx.compose.ui.tooling.preview.Preview

/**
 * Multipreview annotation: renders the annotated `@Composable` once per
 * [DeviceScreenConfiguration][com.github.naz013.ui.common.compose.foundation.DeviceScreenConfiguration]
 * bucket, at dimensions chosen to land on the correct side of that enum's own width/height
 * breakpoints (600/840 width, 480/900 height - see `WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND`
 * etc). Use this instead of hand-picking `@Preview(widthDp=..., heightDp=...)` values on every new
 * shared component, so "what does this look like on a tablet" stays answerable at a glance and
 * every component gets checked against the same six configurations.
 *
 * Apply directly to a `@Composable` preview function in place of `@Preview`:
 * ```
 * @AppScreenSizePreviews
 * @Composable
 * private fun MyComponentPreview() { ... }
 * ```
 */
@Preview(name = "Mobile portrait", group = "Screen sizes", widthDp = 411, heightDp = 891)
@Preview(name = "Mobile landscape", group = "Screen sizes", widthDp = 891, heightDp = 411)
@Preview(name = "Tablet portrait", group = "Screen sizes", widthDp = 800, heightDp = 1280)
@Preview(name = "Tablet landscape", group = "Screen sizes", widthDp = 1280, heightDp = 800)
@Preview(name = "Desktop small", group = "Screen sizes", widthDp = 700, heightDp = 600)
@Preview(name = "Desktop normal", group = "Screen sizes", widthDp = 1600, heightDp = 1000)
annotation class AppScreenSizePreviews
