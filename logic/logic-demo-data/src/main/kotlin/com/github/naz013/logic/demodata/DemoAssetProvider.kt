package com.github.naz013.logic.demodata

/** Platform seam for reading the bundled demo assets (e.g. the showcase note photo) that only
 * an Android-aware module can load from resources. */
interface DemoAssetProvider {
  fun demoNotePhotoBytes(): ByteArray
}
