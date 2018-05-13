package com.bumptech.glide.request.transition;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;

/**
 * A {@link Transition} that can apply a {@link Animation Animation} to a
 * {@link View View} using
 * {@link View#startAnimation(Animation)}.
 *
 * @param <R> The type of the resource that will be transitioned into a view.
 */
public class ViewTransition<R> implements Transition<R> {

  private final ViewTransitionAnimationFactory viewTransitionAnimationFactory;

  /**
   * Constructs a new ViewAnimation that will start the given {@link android.view.animation
   * .Animation}.
   */
  ViewTransition(ViewTransitionAnimationFactory viewTransitionAnimationFactory) {
    this.viewTransitionAnimationFactory = viewTransitionAnimationFactory;
  }

  /**
   * Always clears the current animation on the view using
   * {@link View#clearAnimation()}, then starts the
   * {@link Animation} given in the constructor using
   * {@link View#startAnimation(Animation)} and then returns
   * {@code false} because the animation does not actually put the current resource on the view.
   *
   * @param current {@inheritDoc}
   * @param adapter {@inheritDoc}
   * @return {@inheritDoc}
   */
  @Override
  public boolean transition(R current, ViewAdapter adapter) {
    View view = adapter.getView();
    if (view != null) {
      view.clearAnimation();
      Animation animation = viewTransitionAnimationFactory.build(view.getContext());
      view.startAnimation(animation);
    }

    return false;
  }

  interface ViewTransitionAnimationFactory {
    Animation build(Context context);
  }
}
