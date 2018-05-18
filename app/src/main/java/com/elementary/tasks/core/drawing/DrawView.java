package com.elementary.tasks.core.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.elementary.tasks.core.interfaces.Observable;
import com.elementary.tasks.core.interfaces.Observer;
import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/*
  CanvasView.java
  <p>
  Copyright (c) 2014 Tomohiro IKEDA (Korilakkuma)
  Released under the MIT license
 */

/**
 * This class defines fields and methods for drawing.
 */
public class DrawView extends View implements Observable {

    private static final String TAG = "DrawView";

    private List<Observer> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.setUpdate(this.historyPointer);
        }
    }

    // Enumeration for Mode
    public enum Mode {
        DRAW,
        TEXT,
        IMAGE,
        LAYERS,
        ERASER
    }

    // Enumeration for Drawer
    public enum Drawer {
        PEN,
        LINE,
        RECTANGLE,
        CIRCLE,
        ELLIPSE,
        QUADRATIC_BEZIER
    }

    private List<Drawing> elements = new ArrayList<>();

    @ColorInt
    private int baseColor = Color.WHITE;

    // for Undo, Redo
    private int historyPointer = 0;

    // Flags
    private Mode mode = Mode.DRAW;
    private Drawer drawer = Drawer.PEN;
    private boolean isDown = false;

    // for Paint
    private Paint.Style paintStyle = Paint.Style.STROKE;
    @ColorInt
    private int paintStrokeColor = Color.BLACK;
    @ColorInt
    private int paintFillColor = Color.BLACK;
    private float paintStrokeWidth = 3F;
    private int opacity = 255;
    private float blur = 0F;
    private Paint.Cap lineCap = Paint.Cap.ROUND;
    private PathEffect drawPathEffect = null;

    private int fontFamily = 9;
    private float fontSize = 32F;
    private Paint.Align textAlign = Paint.Align.RIGHT;  // fixed
    private Paint textPaint = new Paint();
    private Canvas mCanvas;

    // for Drawer
    private float startX = 0F;
    private float startY = 0F;
    private float controlX = 0F;
    private float controlY = 0F;
    private float bmpStartX = 0F;
    private float bmpStartY = 0F;

    private Drawing currentItem;
    private DrawCallback mCallback;

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setup();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setup();
    }

    public DrawView(Context context) {
        super(context);
        this.setup();
    }

    public void setHistoryPointer(int historyPointer) {
        this.historyPointer = historyPointer;
        this.invalidate();
    }

    public int getHistoryPointer() {
        return historyPointer;
    }

    public List<Drawing> getElements() {
        return elements;
    }

    /**
     * Common initialization.
     */
    private void setup() {
        this.historyPointer = 0;
        this.elements.clear();
        this.elements.add(new Background(baseColor));
        this.historyPointer++;
        this.textPaint.setARGB(0, 255, 255, 255);
    }

    /**
     * This method creates the instance of Paint.
     * In addition, this method sets styles for Paint.
     *
     * @return paint This is returned as the instance of Paint
     */
    private Paint createPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(this.paintStyle);
        paint.setStrokeWidth(this.paintStrokeWidth);
        paint.setStrokeCap(this.lineCap);
        paint.setStrokeJoin(Paint.Join.MITER);  // fixed
        // for Text
        if (this.mode == Mode.TEXT) {
            paint.setTypeface(AssetsUtil.getTypeface(getContext(), this.fontFamily));
            paint.setTextSize(this.fontSize);
            paint.setTextAlign(this.textAlign);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(0F);
        }
        if (this.mode == Mode.ERASER) {
            // Eraser
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            paint.setARGB(0, 0, 0, 0);
            paint.setColor(this.baseColor);
            // paint.setShadowLayer(this.blur, 0F, 0F, this.baseColor);
        } else {
            // Otherwise
            paint.setColor(this.paintStrokeColor);
            paint.setShadowLayer(this.blur, 0F, 0F, this.paintStrokeColor);
            paint.setAlpha(this.opacity);
            paint.setPathEffect(this.drawPathEffect);
        }
        return paint;
    }

    /**
     * This method initialize Path.
     * Namely, this method creates the instance of Path,
     * and moves current position.
     *
     * @param event This is argument of onTouchEvent method
     * @return path This is returned as the instance of Path
     */
    private Path createPath(MotionEvent event) {
        Path path = new Path();
        // Save for ACTION_MOVE
        this.startX = event.getX();
        this.startY = event.getY();
        path.moveTo(this.startX, this.startY);
        return path;
    }

    /**
     * This method updates the lists for the instance of Path and Paint.
     * "Undo" and "Redo" are enabled by this method.
     *
     * @param path the instance of Path
     */
    private void updateHistory(Path path) {
        if (this.historyPointer == this.elements.size()) {
            this.elements.add(new Figure(path, createPaint()));
            this.historyPointer++;
        } else {
            this.elements.set(this.historyPointer, new Figure(path, createPaint()));
            this.historyPointer++;
            for (int i = this.historyPointer, size = this.elements.size(); i < size; i++) {
                this.elements.remove(this.historyPointer);
            }
        }
    }

    /**
     * This method gets the instance of Path that pointer indicates.
     *
     * @return the instance of Path
     */
    private Path getCurrentPath() {
        Drawing drawing = this.elements.get(this.historyPointer - 1);
        if (drawing instanceof Figure) {
            return ((Figure) drawing).getPath();
        }
        return null;
    }

    private Drawing getCurrent() {
        return this.elements.get(this.historyPointer - 1);
    }

    private void sendCallback() {
        if (mCallback != null) {
            mCallback.onDrawEnd();
        }
    }

    /**
     * This method defines processes on MotionEvent.ACTION_DOWN
     *
     * @param event This is argument of onTouchEvent method
     */
    private void onActionDown(MotionEvent event) {
        switch (this.mode) {
            case DRAW:
            case ERASER:
                if ((this.drawer != Drawer.QUADRATIC_BEZIER)) {
                    this.updateHistory(this.createPath(event));
                    this.isDown = true;
                } else {
                    if ((this.startX == 0F) && (this.startY == 0F)) {
                        this.updateHistory(this.createPath(event));
                    } else {
                        this.controlX = event.getX();
                        this.controlY = event.getY();
                        this.isDown = true;
                    }
                }
                break;
            case TEXT:
            case IMAGE:
                this.startX = event.getX();
                this.startY = event.getY();
                this.bmpStartX = currentItem.getX();
                this.bmpStartY = currentItem.getY();
                break;
            default:
                break;
        }
    }

    /**
     * This method defines processes on MotionEvent.ACTION_MOVE
     *
     * @param event This is argument of onTouchEvent method
     */
    private void onActionMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (this.mode) {
            case IMAGE:
                moveBitmap(x, y);
                break;
            case DRAW:
            case ERASER:
                if ((this.drawer != Drawer.QUADRATIC_BEZIER)) {
                    if (!isDown) {
                        return;
                    }
                    Path path = this.getCurrentPath();
                    if (path == null) {
                        return;
                    }
                    switch (this.drawer) {
                        case PEN:
                            path.lineTo(x, y);
                            break;
                        case LINE:
                            path.reset();
                            path.moveTo(this.startX, this.startY);
                            path.lineTo(x, y);
                            break;
                        case RECTANGLE:
                            path.reset();
                            float left = Math.min(this.startX, x);
                            float right = Math.max(this.startX, x);
                            float top = Math.min(this.startY, y);
                            float bottom = Math.max(this.startY, y);
                            path.addRect(left, top, right, bottom, Path.Direction.CCW);
                            break;
                        case CIRCLE:
                            double distanceX = Math.abs((double) (this.startX - x));
                            double distanceY = Math.abs((double) (this.startY - y));
                            double radius = Math.sqrt(Math.pow(distanceX, 2.0) + Math.pow(distanceY, 2.0));
                            path.reset();
                            path.addCircle(this.startX, this.startY, (float) radius, Path.Direction.CCW);
                            break;
                        case ELLIPSE:
                            RectF rect = new RectF(this.startX, this.startY, x, y);
                            path.reset();
                            path.addOval(rect, Path.Direction.CCW);
                            break;
                        default:
                            break;
                    }
                } else {
                    if (!isDown) {
                        return;
                    }
                    Path path = this.getCurrentPath();
                    if (path == null) {
                        return;
                    }
                    path.reset();
                    path.moveTo(this.startX, this.startY);
                    path.quadTo(this.controlX, this.controlY, x, y);
                }
                break;
            case TEXT:
                moveText(x, y);
                break;
            default:
                break;
        }
    }

    private void moveText(float x, float y) {
        Drawing drawing = getCurrent();
        if (drawing instanceof Text) {
            drawing.setX(x);
            drawing.setY(y);
            this.invalidate();
        }
    }

    private void moveBitmap(float x, float y) {
        Drawing drawing = getCurrent();
        if (drawing instanceof Image) {
            drawing.setX(bmpStartX - (startX - x));
            drawing.setY(bmpStartY - (startY - y));
            this.invalidate();
        }
    }

    /**
     * This method defines processes on MotionEvent.ACTION_DOWN
     */
    private void onActionUp() {
        if (isDown) {
            this.startX = 0F;
            this.startY = 0F;
            this.isDown = false;
        }
        sendCallback();
    }

    /**
     * This method updates the instance of Canvas (View)
     *
     * @param canvas the new instance of Canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.elements != null && !this.elements.isEmpty()) {
            for (int i = 0; i < historyPointer; i++) {
                try {
                    elements.get(i).draw(canvas, false);
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
        }
        this.mCanvas = canvas;
        notifyObservers();
    }

    /**
     * This method set event listener for drawing.
     *
     * @param event the instance of MotionEvent
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.onActionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                this.onActionMove(event);
                break;
            case MotionEvent.ACTION_UP:
                this.onActionUp();
                break;
            default:
                break;
        }
        // Re draw
        this.invalidate();
        return true;
    }

    public void setCallback(DrawCallback mCallback) {
        this.mCallback = mCallback;
    }

    /**
     * This method is getter for mode.
     *
     * @return
     */
    public Mode getMode() {
        return this.mode;
    }

    /**
     * This method is setter for mode.
     *
     * @param mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * This method is getter for drawer.
     *
     * @return
     */
    public Drawer getDrawer() {
        return this.drawer;
    }

    /**
     * This method is setter for drawer.
     *
     * @param drawer
     */
    public void setDrawer(Drawer drawer) {
        this.drawer = drawer;
    }

    /**
     * This method checks if Undo is available
     *
     * @return If Undo is available, this is returned as true. Otherwise, this is returned as false.
     */
    public boolean canUndo() {
        return this.historyPointer > 1;
    }

    /**
     * This method checks if Redo is available
     *
     * @return If Redo is available, this is returned as true. Otherwise, this is returned as false.
     */
    public boolean canRedo() {
        return this.historyPointer < this.elements.size();
    }

    /**
     * This method draws canvas again for Undo.
     *
     * @return If Undo is enabled, this is returned as true. Otherwise, this is returned as false.
     */
    public boolean undo() {
        if (canUndo()) {
            this.historyPointer--;
            this.currentItem = getCurrent();
            this.invalidate();
            sendCallback();
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method draws canvas again for Redo.
     *
     * @return If Redo is enabled, this is returned as true. Otherwise, this is returned as false.
     */
    public boolean redo() {
        if (canRedo()) {
            this.historyPointer++;
            this.currentItem = getCurrent();
            this.invalidate();
            sendCallback();
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method initializes canvas.
     *
     * @return
     */
    public void clear() {
        this.setup();
        this.invalidate();
        sendCallback();
    }

    /**
     * This method is getter for canvas background color
     *
     * @return
     */
    @ColorInt
    public int getBaseColor() {
        return this.baseColor;
    }

    /**
     * This method is setter for canvas background color
     *
     * @param color
     */
    public void setBaseColor(@ColorInt int color) {
        this.baseColor = color;
        Drawing element = elements.get(0);
        if (element instanceof Background) {
            ((Background) element).setColor(baseColor);
        }
        this.invalidate();
    }

    /**
     * This method is setter for drawn text.
     *
     * @param text
     */
    public void addText(String text) {
        this.currentItem = new Text(text, fontSize, createPaint());
        this.currentItem.setX(startX);
        this.currentItem.setY(startY);
        if (this.historyPointer == this.elements.size()) {
            this.elements.add(currentItem);
        } else {
            this.elements.add(historyPointer, currentItem);
        }
        this.historyPointer++;
        this.invalidate();
    }

    public void setText(String text) {
        this.currentItem = getCurrent();
        if (currentItem instanceof Text) {
            ((Text) currentItem).setText(text);
            this.invalidate();
        }
    }

    /**
     * This method is getter for stroke or fill.
     *
     * @return
     */
    public Paint.Style getPaintStyle() {
        return this.paintStyle;
    }

    /**
     * This method is setter for stroke or fill.
     *
     * @param style
     */
    public void setPaintStyle(Paint.Style style) {
        this.paintStyle = style;
    }

    /**
     * This method is getter for stroke color.
     *
     * @return
     */
    @ColorInt
    public int getPaintStrokeColor() {
        return this.paintStrokeColor;
    }

    /**
     * This method is setter for stroke color.
     *
     * @param color
     */
    public void setPaintStrokeColor(@ColorInt int color) {
        this.paintStrokeColor = color;
        if (this.mode == Mode.TEXT) {
            Drawing drawing = getCurrent();
            if (drawing instanceof Text) {
                ((Text) drawing).setTextColor(color);
                this.invalidate();
            }
        }
    }

    /**
     * This method is getter for fill color.
     * But, current Android API cannot set fill color (?).
     *
     * @return
     */
    @ColorInt
    public int getPaintFillColor() {
        return this.paintFillColor;
    }

    /**
     * This method is setter for fill color.
     * But, current Android API cannot set fill color (?).
     *
     * @param color
     */
    public void setPaintFillColor(@ColorInt int color) {
        this.paintFillColor = color;
    }

    /**
     * This method is getter for stroke width.
     *
     * @return
     */
    public float getPaintStrokeWidth() {
        Drawing drawing = getCurrent();
        if (drawing instanceof Figure) {
            return drawing.getStrokeWidth();
        } else {
            return this.paintStrokeWidth;
        }
    }

    /**
     * This method is setter for stroke width.
     *
     * @param width
     */
    public void setPaintStrokeWidth(float width) {
        if (width >= 0) {
            this.paintStrokeWidth = width;
        } else {
            this.paintStrokeWidth = 3F;
        }
        Drawing drawing = getCurrent();
        if (drawing instanceof Figure) {
            drawing.setStrokeWidth(this.paintStrokeWidth);
            this.invalidate();
        }
    }

    /**
     * This method is getter for alpha.
     *
     * @return
     */
    public int getOpacity() {
        Drawing drawing = getCurrent();
        return drawing.getOpacity();
//        return this.opacity;
    }

    /**
     * This method is setter for alpha.
     * The 1st argument must be between 0 and 255.
     *
     * @param opacity
     */
    public void setOpacity(int opacity, Mode mode) {
        if ((opacity >= 0) && (opacity <= 255)) {
            this.opacity = opacity;
        } else {
            this.opacity = 255;
        }
        Drawing drawing = getCurrent();
        if (mode == Mode.DRAW && drawing instanceof Figure) {
            drawing.setOpacity(this.opacity);
        } else if (mode == Mode.TEXT && drawing instanceof Text) {
            drawing.setOpacity(this.opacity);
        } else if (mode == Mode.IMAGE && (drawing instanceof Background || drawing instanceof Image)) {
            drawing.setOpacity(this.opacity);
        }
        this.invalidate();
    }

    public int getScale() {
        Drawing drawing = getCurrent();
        if (drawing instanceof Image) {
            return ((Image) drawing).getPercentage();
        }
        return 100;
    }

    public void setScale(int scale, Mode mode) {
        if ((scale >= 1) && (scale <= 100)) {
            Drawing drawing = getCurrent();
            if (mode == Mode.IMAGE && drawing instanceof Image) {
                ((Image) drawing).setScalePercentage(scale);
            }
        }
        this.invalidate();
    }

    /**
     * This method is getter for amount of blur.
     *
     * @return
     */
    public float getBlur() {
        return this.blur;
    }

    /**
     * This method is setter for amount of blur.
     * The 1st argument is greater than or equal to 0.0.
     *
     * @param blur
     */
    public void setBlur(float blur) {
        if (blur >= 0) {
            this.blur = blur;
        } else {
            this.blur = 0F;
        }
    }

    /**
     * This method is getter for line cap.
     *
     * @return
     */
    public Paint.Cap getLineCap() {
        return this.lineCap;
    }

    /**
     * This method is setter for line cap.
     *
     * @param cap
     */
    public void setLineCap(Paint.Cap cap) {
        this.lineCap = cap;
    }

    /**
     * This method is getter for path effect of drawing.
     *
     * @return drawPathEffect
     */
    public PathEffect getDrawPathEffect() {
        return drawPathEffect;
    }

    /**
     * This method is setter for path effect of drawing.
     *
     * @param drawPathEffect
     */
    public void setDrawPathEffect(PathEffect drawPathEffect) {
        this.drawPathEffect = drawPathEffect;
    }

    /**
     * This method is getter for font size,
     *
     * @return
     */
    public float getFontSize() {
        Drawing drawing = getCurrent();
        if (drawing instanceof Text) {
            return ((Text) drawing).getFontSize();
        }
        return this.fontSize;
    }

    /**
     * This method is setter for font size.
     * The 1st argument is greater than or equal to 0.0.
     *
     * @param size
     */
    public void setFontSize(float size) {
        if (size >= 0F) {
            this.fontSize = size;
        } else {
            this.fontSize = 32F;
        }
        Drawing drawing = getCurrent();
        if (drawing instanceof Text) {
            ((Text) drawing).setFontSize(this.fontSize);
            this.invalidate();
        }
    }

    /**
     * This method is getter for font-family.
     *
     * @return
     */
    public int getFontFamily() {
        Drawing drawing = getCurrent();
        if (drawing instanceof Text) {
            return ((Text) drawing).getFontFamily();
        }
        return this.fontFamily;
    }

    /**
     * This method is setter for font-family.
     */
    public void setFontFamily(int position) {
        this.fontFamily = position;
        Drawing drawing = getCurrent();
        if (drawing instanceof Text) {
            ((Text) drawing).setFontFamily(this.fontFamily, AssetsUtil.getTypeface(getContext(), position));
            this.invalidate();
        }
    }

    /**
     * This method gets current canvas as bitmap.
     *
     * @return This is returned as bitmap.
     */
    public Bitmap getBitmap() {
        this.setDrawingCacheEnabled(false);
        this.setDrawingCacheEnabled(true);
        return Bitmap.createBitmap(this.getDrawingCache());
    }

    /**
     * This method gets current canvas as scaled bitmap.
     *
     * @return This is returned as scaled bitmap.
     */
    public Bitmap getScaleBitmap(int w, int h) {
        this.setDrawingCacheEnabled(false);
        this.setDrawingCacheEnabled(true);
        return Bitmap.createScaledBitmap(this.getDrawingCache(), w, h, true);
    }

    /**
     * This method draws the designated bitmap to canvas.
     *
     * @param bitmap
     */
    public void addBitmap(Bitmap bitmap) {
        this.currentItem = new Image(bitmap);
        if (this.historyPointer == this.elements.size()) {
            this.elements.add(currentItem);
        } else {
            this.elements.add(historyPointer, currentItem);
        }
        this.historyPointer++;
        if (mCanvas != null) {
            LogUtil.d(TAG, "addBitmap: " + bitmap.getWidth() + ", " + bitmap.getHeight() + ", " + mCanvas.getWidth() + ", " + mCanvas.getHeight());
        }
        this.invalidate();
    }

    public void setBitmap(Bitmap bitmap) {
        this.currentItem = getCurrent();
        if (currentItem instanceof Image) {
            ((Image) currentItem).setBitmap(bitmap);
        }
        this.invalidate();
    }

    /**
     * This method draws the designated byte array of bitmap to canvas.
     *
     * @param byteArray This is returned as byte array of bitmap.
     */
    public void addBitmap(byte[] byteArray) {
        this.addBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
    }

    public void setBitmap(byte[] byteArray) {
        this.setBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
    }

    /**
     * This static method gets the designated bitmap as byte array.
     *
     * @param bitmap
     * @param format
     * @param quality
     * @return This is returned as byte array of bitmap.
     */
    public static byte[] getBitmapAsByteArray(Bitmap bitmap, CompressFormat format, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * This method gets the bitmap as byte array.
     *
     * @param format
     * @param quality
     * @return This is returned as byte array of bitmap.
     */
    public byte[] getBitmapAsByteArray(CompressFormat format, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.getBitmap().compress(format, quality, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * This method gets the bitmap as byte array.
     * Bitmap format is PNG, and quality is 100.
     *
     * @return This is returned as byte array of bitmap.
     */
    public byte[] getBitmapAsByteArray() {
        return this.getBitmapAsByteArray(CompressFormat.PNG, 100);
    }

    public interface DrawCallback {
        void onDrawEnd();
    }
}
