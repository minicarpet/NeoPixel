package com.devadvance.ColorPicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.logangalloisext.Neopixel.R;

/**
 * Created by Minicarpet on 26/02/2017.
 */

public class ColorCircle extends View {

    private Paint paint;
    private float origin[] = {0.0f, 0.0f};
    private float size[] = {0.0f, 0.0f};
    private float position[] = {0.0f, 0.0f};
    private double angle = 0;
    private RectF rectF;
    private float value;
    private float radius;
    private float originX;
    private float originY;
    private int defaultColor;
    private int defaultColorPointer;
    private float radiusPointer;
    private float strokePointer;
    protected OnColorCircleListener mOnColorCircleListener;

    protected static final float DEFAULT_RADIUS = 250.0f;
    protected static final float DEFAULT_RADIUS_POINTER = 10.0f;

    public ColorCircle(Context context) {
        super(context);
        init(null, 0);
    }

    public ColorCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ColorCircle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        value = 1.0f;
        final TypedArray attrArray = getContext().obtainStyledAttributes(attrs, R.styleable.ColorCircle, defStyle, 0);
        radius = attrArray.getDimension(R.styleable.ColorCircle_circle_radius, DEFAULT_RADIUS);
        this.post(new Runnable() {
            @Override
            public void run() {
                originX = attrArray.getDimension(R.styleable.ColorCircle_circle_originX, getMeasuredWidth()/2);
                originY = attrArray.getDimension(R.styleable.ColorCircle_circle_originY, getMeasuredHeight()/2);
            }
        });
        defaultColorPointer = attrArray.getColor(R.styleable.ColorCircle_pointer_color, Color.BLACK);
        radiusPointer = attrArray.getDimension(R.styleable.ColorCircle_pointer_radius, DEFAULT_RADIUS_POINTER);
        strokePointer = attrArray.getDimension(R.styleable.ColorCircle_pointer_stroke_width, 5.0f);
        position[0] = radius;
        position[1] = 0.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int i;
        paint = new Paint();
        origin[0] = originX;
        origin[1] = originY;
        canvas.translate(origin[0], origin[1]);
        size[0] = radius;
        size[1] = size[0];
        float hsv[] = {0.0f,1.0f,value};
        rectF = new RectF(-size[0], -size[1], size[0], size[1]);
        for (i = 0; i < 360; i++) {
            hsv[0] = i;
            paint.setColor(Color.HSVToColor(hsv));
            canvas.drawArc(rectF, i, 2.0f, true, paint);
        }
        paint.setColor(Color.WHITE);
        canvas.drawCircle(0, 0, radius/10.0f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokePointer);
        paint.setColor(defaultColorPointer);
        canvas.drawCircle(position[0], position[1], radiusPointer, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = (int) (radius*2+DEFAULT_RADIUS_POINTER);
        int desiredHeight = (int) (radius*2+DEFAULT_RADIUS_POINTER);

        int width;
        int height;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean touchEvent = true;

        float distanceXFromCircleCenter = event.getX() - origin[0];
        float distanceYFromCircleCenter = event.getY() - origin[1];
        double distance = Math.sqrt(Math.pow(distanceXFromCircleCenter, 2) + Math.pow(distanceYFromCircleCenter, 2));

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                position[0] = distanceXFromCircleCenter;
                position[1] = distanceYFromCircleCenter;
                angle = (Math.toDegrees(-Math.atan2(distanceYFromCircleCenter, distanceXFromCircleCenter)) + 360.0) % 360.0;
                if(distance > radius) {
                    position[0] = (float) (radius * Math.sin(Math.toRadians(angle)+(Math.PI/2)));
                    position[1] = (float) (radius * Math.cos(Math.toRadians(angle)+(Math.PI/2)));
                } else if (distance < radius/10.0f) {
                    position[0] = 0;
                    position[1] = 0;
                    angle = 400;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if(mOnColorCircleListener != null) {
                    mOnColorCircleListener.onRelease(this, getColor(), (int) (getValue() * 100));
                }
                break;
            default:
                touchEvent = false;
                break;
        }
        return touchEvent;
    }

    public void setOnColorPickerListener(OnColorCircleListener l) {
        mOnColorCircleListener = l;
    }

    public interface OnColorCircleListener {
        void onRelease(View view, int color, int luminosity);
    }

    public int getColor() {
        int color;
        if (angle == 400) {
            color = Color.WHITE;
        } else {
            float hsv[] = {(float) (Math.abs(angle-720) % 360.0),1.0f,1.0f};
            color = Color.HSVToColor(0, hsv);
        }
        return color;
    }

    public void setValue(float newValue) {
        value = newValue;
        invalidate();
    }

    public float getValue() {
        return value;
    }
}
