package com.elementary.tasks.core.os.datapicker.compose

/** Holds the per-call result callback for an `ActivityResultLauncher`-based picker between the
 *  trigger call and the launcher's result callback, since the launcher itself is registered once
 *  (via `rememberLauncherForActivityResult`) but each caller supplies its own callback. */
internal class PendingCallback<T> {
  var value: ((T) -> Unit)? = null
}
