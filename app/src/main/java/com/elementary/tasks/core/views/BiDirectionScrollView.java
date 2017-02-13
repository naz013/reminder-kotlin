package com.elementary.tasks.core.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.elementary.tasks.core.utils.MeasureUtils;

/**
 * Copyright 2017 Nazar Suhovich
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

public class BiDirectionScrollView extends FrameLayout {

    /**
     * Factor after which a scrolling direction cancels the other. For example
     * if I scroll 10 up and X left, this variable will be used so that X is
     * changed to 0 if X * FACTOR < 10. <br>
     * For flinging an overriden direction will be canceled until the end of the
     * flinging animation<br>
     * For regular scrolling, at the time the scroll threshold gets passed, if a
     * direction overrides another, it will stay so until the pointer is
     * released.
     */
    private static final int SCROLL_DIRECTION_OVERRIDE_FACTOR = 3;
    private static final float SCROLL_THRESHOLD_DISTANCE_DIP = 48;

    private class BiDirectionScrollViewFlinger implements Runnable {
        private final BiDirectionScrollView view;
        private final Scroller scroller;

        private int lastX = 0;
        private int lastY = 0;

        BiDirectionScrollViewFlinger(final BiDirectionScrollView _view) {
            view = _view;
            scroller = new Scroller(_view.getContext());
        }

        void start(final int _velocityX, final int _velocityY) {
            final View firstChild = view.getChildAt(0);
            if (firstChild == null) {
                return;
            }

            final int velocityX;
            final int velocityY;
            if (view.currentScrollGestureYOverride) {
                velocityX = 0;
            } else {
                velocityX = _velocityX;
            }
            if (view.currentScrollGestureXOverride) {
                velocityY = 0;
            } else {
                velocityY = _velocityY;
            }

            int initialX = view.getScrollX();
            int initialY = view.getScrollY();
            scroller.fling(0, 0, velocityX, velocityY, -Integer.MAX_VALUE, Integer.MAX_VALUE, -Integer.MAX_VALUE,
                    Integer.MAX_VALUE);

            lastX = initialX;
            lastY = initialY;
            view.post(this);
        }

        @Override
        public void run() {
            if (scroller.isFinished()) {
                return;
            }

            boolean more = scroller.computeScrollOffset();
            int x = scroller.getCurrX();
            int y = scroller.getCurrY();
            view.scrollTo(lastX - x, lastY - y);

            if (more) {
                view.post(this);
            }
        }

        boolean isFlinging() {
            return !scroller.isFinished();
        }

        void forceFinished() {
            if (!scroller.isFinished()) {
                scroller.forceFinished(true);
            }
        }
    }

    private final BiDirectionScrollViewFlinger flinger;
    private final GestureDetector scrollGestureDetector;
    private final float scrollThresholdDistance;
    private OnTouchListener touchListener = null;

    public BiDirectionScrollView(final Context context) {
        super(context);
        flinger = new BiDirectionScrollViewFlinger(BiDirectionScrollView.this);
        scrollGestureDetector = createScrollGestureDetector();
        scrollThresholdDistance = MeasureUtils.dp2px(getContext(), (int) SCROLL_THRESHOLD_DISTANCE_DIP);
    }

    public BiDirectionScrollView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        flinger = new BiDirectionScrollViewFlinger(BiDirectionScrollView.this);
        scrollGestureDetector = createScrollGestureDetector();
        scrollThresholdDistance = MeasureUtils.dp2px(getContext(), (int) SCROLL_THRESHOLD_DISTANCE_DIP);
    }

    public BiDirectionScrollView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        flinger = new BiDirectionScrollViewFlinger(BiDirectionScrollView.this);
        scrollGestureDetector = createScrollGestureDetector();
        scrollThresholdDistance = MeasureUtils.dp2px(getContext(), (int) SCROLL_THRESHOLD_DISTANCE_DIP);
    }

    private GestureDetector createScrollGestureDetector() {
        final GestureDetector res = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX,
                                    final float distanceY) {
                if (flinger.isFlinging()) {
                    flinger.forceFinished();
                }
                if (currentScrollGestureBroken || !currentlyIntoScrollGesture) {
                    return false;
                }
                BiDirectionScrollView.this.scrollBy(currentScrollGestureYOverride ? 0 : (int) distanceX,
                        currentScrollGestureXOverride ? 0 : (int) distanceY);
                return true;
            }

            @Override
            public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX,
                                   final float velocityY) {
                if (flinger.isFlinging()) {
                    flinger.forceFinished();
                }
                flinger.start((int) velocityX, (int) velocityY);
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
        res.setIsLongpressEnabled(false);
        return res;
    }

    @Override
    public void scrollTo(final int x, final int y) {
        super.scrollTo(x, y);
        fixScrollIfOutOfBounds();
    }

    @Override
    public void scrollBy(final int x, final int y) {
        super.scrollBy(x, y);
        fixScrollIfOutOfBounds();
    }

    private void fixScrollIfOutOfBounds() {
        if (getScrollX() < 0) {
            scrollTo(0, getScrollY());
        }

        if (getScrollY() < 0) {
            scrollTo(getScrollX(), 0);
        }

        final View firstChild = getChildAt(0);
        if (firstChild == null) {
            return;
        }

        int childWidth = firstChild.getWidth();
        int childHeight = firstChild.getHeight();

        if (childWidth > getWidth() && getScrollX() + getWidth() > childWidth) {
            scrollTo(childWidth - getWidth(), getScrollY());
        }

        if (childHeight > getHeight() && getScrollY() + getHeight() > childHeight) {
            scrollTo(getScrollX(), childHeight - getHeight());
        }
    }

    private boolean currentScrollGestureBroken = false;
    private boolean currentlyIntoScrollGesture = false;
    private boolean currentScrollGestureXOverride = false;
    private boolean currentScrollGestureYOverride = false;
    private int initialDownX = -1;
    private int initialDownY = -1;

    @Override
    public void setOnTouchListener(final OnTouchListener l) {
        touchListener = l;
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            currentScrollGestureBroken = false;
            currentlyIntoScrollGesture = false;
            initialDownX = (int) ev.getX();
            initialDownY = (int) ev.getY();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            initialDownX = -1;
            initialDownY = -1;
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            final float scrolledXDistance = Math.abs(ev.getX() - initialDownX);
            final float scrolledYDistance = Math.abs(ev.getY() - initialDownY);
            if (!currentlyIntoScrollGesture
                    && (scrolledXDistance > scrollThresholdDistance || scrolledYDistance > scrollThresholdDistance)) {
                currentScrollGestureXOverride = scrolledYDistance * SCROLL_DIRECTION_OVERRIDE_FACTOR < scrolledXDistance;
                currentScrollGestureYOverride = scrolledXDistance * SCROLL_DIRECTION_OVERRIDE_FACTOR < scrolledYDistance;
                currentlyIntoScrollGesture = true;
            }
        }
        super.dispatchTouchEvent(ev);
        if (touchListener != null) {
            touchListener.onTouch(this, ev);
        }

        scrollGestureDetector.onTouchEvent(ev);
        return true;
    }

    /**
     * Breaks the current scroll gesture until next time a pointer is pressed.
     * Allows to prevent scrolling while dragging in the view if other children
     * views want to handle that event without scrolling being triggered.
     */
    public void breakCurrentScrollGestureUntilNextPress() {
        currentScrollGestureBroken = true;
    }

    /**
     * @return true if the view is currently scrolling, false otherwise. This
     *         allows to filter long click events being triggered if scrolling
     *         started by grabbing a point over a child view.
     */
    public boolean isCurrentlyIntoScrollGesture() {
        return !currentScrollGestureBroken && currentlyIntoScrollGesture;
    }
}
