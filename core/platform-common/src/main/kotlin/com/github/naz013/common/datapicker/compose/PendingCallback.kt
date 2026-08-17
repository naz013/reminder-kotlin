package com.github.naz013.common.datapicker.compose

/** Holds the per-call result callback for an `ActivityResultLauncher`-based picker between the
 *  trigger call and the launcher's result callback, since the launcher itself is registered once
 *  (via `rememberLauncherForActivityResult`) but each caller supplies its own callback. */
class PendingCallback<T> {
  var value: ((T) -> Unit)? = null
}
