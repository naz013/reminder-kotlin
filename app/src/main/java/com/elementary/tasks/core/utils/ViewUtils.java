package com.elementary.tasks.core.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;

import com.elementary.tasks.R;

/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public final class ViewUtils {

    private ViewUtils() {
    }

    public static ColorStateList getFabState(Context context, @ColorRes int colorNormal, @ColorRes int colorPressed) {
        int[][] states = {
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_focused}, new int[]{}
        };
        int colorP = getColor(context, colorPressed);
        int colorN = getColor(context, colorNormal);
        int colors[] = {colorP, colorN, colorN};
        return new ColorStateList(states, colors);
    }

    public static ColorStateList getFabState(@ColorInt int colorNormal, @ColorInt int colorPressed) {
        int[][] states = {
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_focused}, new int[]{}
        };
        int colors[] = {colorPressed, colorNormal, colorNormal};
        return new ColorStateList(states, colors);
    }

    public static Drawable getDrawable(Context context, @DrawableRes int resource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return context.getResources().getDrawable(resource, null);
        } else {
            return context.getResources().getDrawable(resource);
        }
    }

    @ColorInt
    public static int getColor(Context context, @ColorRes int resource) {
        try {
            if (Module.isMarshmallow()) {
                return context.getResources().getColor(resource, null);
            } else {
                return context.getResources().getColor(resource);
            }
        } catch (Resources.NotFoundException e) {
            return 0;
        }
    }

    public static void slideInUp(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    public static void slideOutDown(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        view.startAnimation(animation);
        view.setVisibility(View.GONE);
    }

    public static void slideOutUp(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_up_out);
        view.startAnimation(animation);
        view.setVisibility(View.GONE);
    }

    public static void slideInDown(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_down_in);
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    public static void fadeInAnimation(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setStartOffset(400);
        fadeIn.setDuration(400);
        view.setAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    public static void fadeOutAnimation(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(400);
        view.setAnimation(fadeOut);
        view.setVisibility(View.GONE);
    }

    public static void show(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setStartOffset(400);
        fadeIn.setDuration(400);
        view.setAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    public static void hide(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(400);
        view.setAnimation(fadeOut);
        view.setVisibility(View.INVISIBLE);
    }

    public static void showOver(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new OvershootInterpolator());
        fadeIn.setDuration(300);
        view.setAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    public static void hideOver(View view) {
        Animation fadeIn = new AlphaAnimation(1, 0);
        fadeIn.setInterpolator(new OvershootInterpolator());
        fadeIn.setDuration(300);
        view.setAnimation(fadeIn);
        view.setVisibility(View.GONE);
    }

    public static void show(Context context, View v, AnimationCallback callback) {
        Animation scaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_zoom);
        scaleUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (callback != null) {
                    callback.onAnimationFinish(1);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(scaleUp);
        v.setVisibility(View.VISIBLE);
    }

    public static void hide(Context context, View v, AnimationCallback callback) {
        Animation scaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_zoom_out);
        scaleDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (callback != null) {
                    callback.onAnimationFinish(0);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(scaleDown);
        v.setVisibility(View.GONE);
    }

    public static void showReveal(View v) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeIn.setDuration(300);
        v.setAnimation(fadeIn);
        v.setVisibility(View.VISIBLE);
    }

    public static void hideReveal(View v) {
        Animation fadeIn = new AlphaAnimation(1, 0);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeIn.setDuration(300);
        v.setAnimation(fadeIn);
        v.setVisibility(View.GONE);
    }

    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public interface AnimationCallback {
        void onAnimationFinish(int code);
    }
}
