package com.example.assessment0;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PaintWindow extends View {

    private Path path;
    private Paint paint;

    private Bitmap bitmapSave;
    private Canvas canvasSave;
    public int currentColor;
    public int currentStrokeWidth;

    // undo redo
    private List<Stroke> strokeAllList = new ArrayList<>();
    private List<Stroke> strokeRemovedList = new ArrayList<>();

    // eraser
    private boolean eraser = false;
    public int currentEraserWidth;
    public PaintWindow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        path = new Path();
        paint = new Paint();


        // initialize
//        bitmapSave = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
//        canvasSave = new Canvas(bitmapSave);
        currentColor= Color.BLUE;
        currentStrokeWidth = 20;
        currentEraserWidth = 20;

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(currentColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(currentStrokeWidth);
    }

    public void initializeBitmapCanvas(int width, int height) {
        bitmapSave = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvasSave = new Canvas(bitmapSave);
    }

    public void setColor(int color) {
        currentColor = color;
    }

    public void setStrokeWidth(int width) {
        currentStrokeWidth = width;
    }

    public void undo() {
        if (strokeAllList.size() > 0) {
            Stroke strokeRemoved = strokeAllList.remove(strokeAllList.size() - 1);
            strokeRemovedList.add(strokeRemoved);
            invalidate();
        }
    }

    public void redo() {
        if (strokeRemovedList.size() > 0) {
            Stroke strokeToRedo = strokeRemovedList.remove(strokeRemovedList.size() - 1);
            strokeAllList.add(strokeToRedo);
            invalidate();
        }
    }

    public boolean getEraser() {
        return eraser;
    }
    public void setEraser(boolean valueEraser) {
        eraser = valueEraser;
    }

    public void setEraserWidth(int width) {
        currentEraserWidth = width;
    }

    public Bitmap save() {
        return bitmapSave;
    }

    private void touchDown(float x, float y) {
        path = new Path();
        Stroke stroke = new Stroke();
        if (eraser) {
            stroke.setColor(Color.WHITE);
            stroke.setStrokeWidth(currentEraserWidth);
        } else {
            stroke.setColor(currentColor);
            stroke.setStrokeWidth(currentStrokeWidth);
        }
        stroke.setPath(path);
        strokeAllList.add(stroke);

        path.reset();
        path.moveTo(x, y);

        // remove all Redos
        strokeRemovedList.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currX = event.getX();
        float currY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(currX, currY);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(currX, currY);
                break;
            default:
                return false;
        }
        postInvalidate();
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i("kk", "onDraw() ::: ");
        canvas.save();

        canvasSave.drawColor(Color.WHITE);

        for (Stroke stroke : strokeAllList) {
            paint.setColor(stroke.getColor());
            paint.setStrokeWidth(stroke.getStrokeWidth());
            canvasSave.drawPath(stroke.getPath(), paint);
        }

        canvas.drawBitmap(bitmapSave, 0, 0, null);
        canvas.restore();
//        canvas.drawPath(path, paint);
    }
}
