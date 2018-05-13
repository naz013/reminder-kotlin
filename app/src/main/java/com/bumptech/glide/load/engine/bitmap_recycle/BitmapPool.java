package com.bumptech.glide.load.engine.bitmap_recycle;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

/**
 * An interface for a pool that allows users to reuse {@link Bitmap} objects.
 */
public interface BitmapPool {

  /**
   * Returns the current maximum size of the pool in bytes.
   */
  long getMaxSize();

  /**
   * Multiplies the initial size of the pool by the given multiplier to dynamically and
   * synchronously allow users to adjust the size of the pool.
   *
   * <p> If the current total size of the pool is larger than the max size after the given
   * multiplier is applied, {@link Bitmap}s should be evicted until the pool is smaller than the new
   * max size. </p>
   *
   * @param sizeMultiplier The size multiplier to apply between 0 and 1.
   */
  void setSizeMultiplier(float sizeMultiplier);

  /**
   * Adds the given {@link Bitmap} if it is eligible to be re-used and the pool
   * can fit it, or calls {@link Bitmap#recycle()} on the Bitmap and discards it.
   *
   * <p> Callers must <em>not</em> continue to use the Bitmap after calling this method. </p>
   *
   * @param bitmap The {@link Bitmap} to attempt to add.
   * @see Bitmap#isMutable()
   * @see Bitmap#recycle()
   */
  void put(Bitmap bitmap);

  /**
   * Returns a {@link Bitmap} of exactly the given width, height, and
   * configuration, and containing only transparent pixels.
   *
   * <p> If no Bitmap with the requested attributes is present in the pool, a new one will be
   * allocated. </p>
   *
   * <p> Because this method erases all pixels in the {@link Bitmap}, this method is slightly slower
   * than {@link #getDirty(int, int, Bitmap.Config)}. If the {@link
   * Bitmap} is being obtained to be used in {@link android.graphics.BitmapFactory}
   * or in any other case where every pixel in the {@link Bitmap} will always be
   * overwritten or cleared, {@link #getDirty(int, int, Bitmap.Config)} will be
   * faster. When in doubt, use this method to ensure correctness. </p>
   *
   * <pre>
   *     Implementations can should clear out every returned Bitmap using the following:
   *
   * {@code
   * bitmap.eraseColor(Color.TRANSPARENT);
   * }
   * </pre>
   *
   * @param width  The width in pixels of the desired {@link Bitmap}.
   * @param height The height in pixels of the desired {@link Bitmap}.
   * @param config The {@link Bitmap.Config} of the desired {@link
   *               Bitmap}.
   * @see #getDirty(int, int, Bitmap.Config)
   */
  @NonNull
  Bitmap get(int width, int height, Bitmap.Config config);

  /**
   * Identical to {@link #get(int, int, Bitmap.Config)} except that any returned
   * {@link Bitmap} may <em>not</em> have been erased and may contain random data.
   *
   * <p>If no Bitmap with the requested attributes is present in the pool, a new one will be
   * allocated. </p>
   *
   * <p> Although this method is slightly more efficient than {@link #get(int, int,
   * Bitmap.Config)} it should be used with caution and only when the caller is
   * sure that they are going to erase the {@link Bitmap} entirely before writing
   * new data to it. </p>
   *
   * @param width  The width in pixels of the desired {@link Bitmap}.
   * @param height The height in pixels of the desired {@link Bitmap}.
   * @param config The {@link Bitmap.Config} of the desired {@link
   *               Bitmap}.
   * @return A {@link Bitmap} with exactly the given width, height, and config
   * potentially containing random image data or null if no such {@link Bitmap}
   * could be obtained from the pool.
   * @see #get(int, int, Bitmap.Config)
   */
  @NonNull
  Bitmap getDirty(int width, int height, Bitmap.Config config);

  /**
   * Removes all {@link Bitmap}s from the pool.
   */
  void clearMemory();

  /**
   * Reduces the size of the cache by evicting items based on the given level.
   *
   * @param level The level from {@link android.content.ComponentCallbacks2} to use to determine how
   *              many {@link Bitmap}s to evict.
   * @see android.content.ComponentCallbacks2
   */
  void trimMemory(int level);
}
