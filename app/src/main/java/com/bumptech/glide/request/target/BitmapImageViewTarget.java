package com.bumptech.glide.request.target;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * A {@link Target} that can display an {@link
 * Bitmap} in an {@link ImageView}.
 */
public class BitmapImageViewTarget extends ImageViewTarget<Bitmap> {
  // Public API.
  @SuppressWarnings("WeakerAccess")
  public BitmapImageViewTarget(ImageView view) {
    super(view);
  }

  /**
   * @deprecated Use {@link #waitForLayout()} instead.
   */
  // Public API.
  @SuppressWarnings({"unused", "deprecation"})
  @Deprecated
  public BitmapImageViewTarget(ImageView view, boolean waitForLayout) {
    super(view, waitForLayout);
  }

  /**
   * Sets the {@link Bitmap} on the view using {@link
   * ImageView#setImageBitmap(Bitmap)}.
   *
   * @param resource The bitmap to display.
   */
  @Override
  protected void setResource(Bitmap resource) {
    view.setImageBitmap(resource);
  }
}
