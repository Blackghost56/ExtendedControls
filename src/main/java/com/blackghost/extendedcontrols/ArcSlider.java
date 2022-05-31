package com.blackghost.extendedcontrols;

import static com.blackghost.extendedcontrols.Tools.checkRangeAndTrim;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class ArcSlider extends View {

    private static final String TAG = ArcSlider.class.getSimpleName();

    private final Drawable thumbDef = ContextCompat.getDrawable(getContext(), R.drawable.ic_thumb_2);

    private Drawable thumb = thumbDef;
    private float trackWidth = 7;
    private int trackColor = ContextCompat.getColor(getContext(), R.color.gray1);
    private int maxProgress = 100;
    private String helperTextSuffix = "";
    private int helperTextSize = 20;
    private float labelWidth = 7;
    private int labelColor = ContextCompat.getColor(getContext(), R.color.gray1);
    private int labelPointColor = ContextCompat.getColor(getContext(), R.color.gray5);
    private int selectorColor = ContextCompat.getColor(getContext(), R.color.red500);
    private int valueOffset = 0;

    private float startAngle;
    private float sweepAngle;
    private float arcRadius;
    private float arcCenterX;
    private float arcCenterY;
    private int progress = 0;
    private int labelPosition = 0;
    private float progressStep;
    private int thumbX;
    private int thumbY;
    private int labelX;
    private int labelY;
    private float helperCenterX;
    private float helperCenterY;
    private int thumbHalfWidth;
    private  int thumbHalfHeight;
    private Path track;
    private Paint trackPaint;
    private Paint helperPaint;
    private Paint helperTextPaint;
    private Paint helperTextRectPaint;
    private Paint helperTextRectFillPaint;
    private Paint labelPointPaint;
    private final RectF arcRect = new RectF();

    private OnChangeListener changeListener;


    public ArcSlider(Context context) {
        this(context, null);
    }

    public ArcSlider(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ArcSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcSlider, defStyleAttr, 0);

        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);

            if (attr == R.styleable.ArcSlider_thumb) {
                thumb = a.getDrawable(attr);
                if (thumb == null)
                    thumb = thumbDef;
            } else if (attr == R.styleable.ArcSlider_trackWidth){
                trackWidth = a.getDimension(attr, trackWidth);
            } else if (attr == R.styleable.ArcSlider_trackColor){
                trackColor = a.getColor(attr, trackColor);
            } if (attr == R.styleable.ArcSlider_maxProgress){
                maxProgress = a.getInt(attr, maxProgress);
            }  if (attr == R.styleable.ArcSlider_valueOffset){
                valueOffset = a.getInt(attr, valueOffset);
            } if (attr == R.styleable.ArcSlider_valueTextSize){
                helperTextSize = a.getDimensionPixelSize(attr, helperTextSize);
            } if (attr == R.styleable.ArcSlider_valueSuffix){
                helperTextSuffix = a.getString(attr);
            } if (attr == R.styleable.ArcSlider_progress){
                progress = a.getInt(attr, progress);
            } if (attr == R.styleable.ArcSlider_labelWidth){
                labelWidth = a.getDimension(attr, labelWidth);
            } if (attr == R.styleable.ArcSlider_labelColor){
                labelColor = a.getColor(attr, labelColor);
            } if (attr == R.styleable.ArcSlider_labelPointColor){
                labelPointColor = a.getColor(attr, labelPointColor);
            } if (attr == R.styleable.ArcSlider_selectorColor){
                selectorColor = a.getColor(attr, selectorColor);
            }

        }

        a.recycle();

        updatePaint();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int height = View.getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int width = View.getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        clackParameters(width, height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void clackParameters(int width, int height){

        final float centerY = height / 2.0f;

        thumbHalfWidth = (int) (thumb.getIntrinsicWidth() / 2.0f);
        thumbHalfHeight = (int) (thumb.getIntrinsicHeight() / 2.0f);

        final float borderX = Math.max(thumbHalfWidth, trackWidth) + 5;
        final float borderY = Math.max(thumbHalfHeight, trackWidth) + 5;

        final float drawBoxSizeY =  height - 2.0f * borderY - getPaddingTop() - getPaddingBottom();
        final float drawBoxSizeX = Math.min(width - 2.0f * borderX - getPaddingStart() - getPaddingEnd(), drawBoxSizeY / 2.0f);

        arcRadius = drawBoxSizeX / 2.0f + drawBoxSizeY * drawBoxSizeY / (8.0f * drawBoxSizeX);      // We fit the arch of the circle into the rectangle

        arcCenterX = borderX + arcRadius;
        arcCenterY = centerY;

        final float left = arcCenterX - arcRadius;
        final float top = arcCenterY - arcRadius;

        arcRect.set(left, top, left + 2.0f * arcRadius, top + 2.0f * arcRadius);

        // Find sector
        final float rightDrawBoxX = width - borderX - getPaddingEnd();
        final float dx = arcCenterX - rightDrawBoxX;
        final float angle = (float) Math.toDegrees(Math.acos(dx / arcRadius));

        startAngle = 180.0f - angle;
        sweepAngle = 180.0f + angle - startAngle;

        progressStep = sweepAngle / maxProgress;

        // Helper
        helperCenterX = rightDrawBoxX;
        helperCenterY = arcCenterY;

        updateTrackPath();
        updateThumbPosition();

        updateHelper();
        updateLabelPosition();
    }

    private void updateThumbPosition() {
        int thumbAngle = (int) (startAngle + progress * progressStep + 180.0f);
        thumbX = (int) (arcCenterX - (float) (arcRadius * Math.cos(Math.toRadians(thumbAngle))));
        thumbY = (int) (arcCenterY - (float) (arcRadius * Math.sin(Math.toRadians(thumbAngle))));
        thumb.setBounds(thumbX - thumbHalfWidth, thumbY - thumbHalfHeight, thumbX + thumbHalfWidth, thumbY + thumbHalfHeight);
    }

    private void updateLabelPosition() {
        final int angle = (int) (startAngle + labelPosition * progressStep + 180.0f);
        labelX = (int) (arcCenterX - (float) (arcRadius * Math.cos(Math.toRadians(angle))));
        labelY = (int) (arcCenterY - (float) (arcRadius * Math.sin(Math.toRadians(angle))));
    }


    private boolean touchTransactionOpen = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (checkTouchOnArc(event.getX(), event.getY())){
                        touchTransactionOpen = true;
                        float currentAngle = checkRangeAndTrim(getTouchDegrees(event.getX(), event.getY()), startAngle, startAngle + sweepAngle);
                        progress = (int) angleToProgress(currentAngle);
                        updateThumbPosition();
                        updateHelper();
                        if (changeListener != null)
                            changeListener.onStartTrackingTouch(this);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (touchTransactionOpen){
                        float currentAngle = checkRangeAndTrim(getTouchDegrees(event.getX(), event.getY()), startAngle, startAngle + sweepAngle);
                        progress = (int) angleToProgress(currentAngle);
                        updateThumbPosition();
                        updateHelper();
                        if (changeListener != null)
                            changeListener.onProgressChanged(this, progress, true);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    touchTransactionOpen = false;
                    updateHelper();
                    if (changeListener != null)
                        changeListener.onStopTrackingTouch(this);
                    break;
            }
        }
        return isEnabled();
    }

    private boolean checkTouchOnArc(float x, float y){
        double distToCircleCenter = Math.sqrt(Math.pow(arcCenterX - x, 2.0) + Math.pow(arcCenterY - y, 2.0));

        return Math.abs(distToCircleCenter - arcRadius) < thumbHalfHeight * 2;
    }

    private float getTouchDegrees(float x, float y) {
        float deltaX = x - arcCenterX;
        float deltaY = y - arcCenterY;

        float angle = (float) Math.toDegrees(Math.atan2(deltaY, deltaX));
        if (angle < 0) {
            angle = 360 + angle;
        }
        return angle;
    }

    private float angleToProgress(double angle){
        double trimAngle = checkRangeAndTrim(angle, (double)startAngle, (double) startAngle + sweepAngle);
        float deltaAngle = (float) (trimAngle - startAngle);

        return deltaAngle / progressStep;
    }

    Path helperPath;


    private float helperTextX;
    private float helperTextY;
    private String helperText;
    private RectF helperTextRect;

    private void updatePaint(){
        trackPaint = new Paint();
        trackPaint.setAntiAlias(true);
        trackPaint.setColor(trackColor);
        trackPaint.setStrokeWidth(trackWidth);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        helperPaint = new Paint();
        helperPaint.setAntiAlias(true);
        helperPaint.setColor(trackColor);
        helperPaint.setStrokeWidth(3);
        helperPaint.setStyle(Paint.Style.STROKE);
        helperPaint.setStrokeCap(Paint.Cap.ROUND);

        helperTextRectPaint = new Paint();
        helperTextRectPaint.setAntiAlias(true);
        helperTextRectPaint.setColor(trackColor);
        helperTextRectPaint.setStrokeWidth(1);
        helperTextRectPaint.setStyle(Paint.Style.STROKE);

        helperTextRectFillPaint = new Paint();
        helperTextRectFillPaint.setAntiAlias(true);
        helperTextRectFillPaint.setColor(Color.WHITE);
        helperTextRectFillPaint.setStyle(Paint.Style.FILL);

        helperTextPaint = new Paint();
        helperTextPaint.setAntiAlias(true);
        helperTextPaint.setColor(trackColor);
        helperTextPaint.setTextSize(helperTextSize);
        helperTextPaint.setStyle(Paint.Style.FILL);
        helperTextPaint.setStrokeCap(Paint.Cap.ROUND);

        labelPointPaint = new Paint();
        labelPointPaint.setAntiAlias(true);
        labelPointPaint.setColor(labelPointColor);
        labelPointPaint.setStrokeWidth(labelWidth);
        labelPointPaint.setStyle(Paint.Style.STROKE);
        labelPointPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void updateTrackPath(){
        track = new Path();
        track.addArc(arcRect, startAngle, sweepAngle);

        invalidate();
    }

    private void updateHelper(){
        helperPath = new Path();

        if  (touchTransactionOpen) {
            helperPath.moveTo(helperCenterX, helperCenterY);
            helperPath.lineTo(thumbX, thumbY);

            helperPaint.setColor(selectorColor);               // todo simplify
            helperTextRectPaint.setColor(selectorColor);

            helperText = String.format(Locale.ENGLISH, "%d%s", progress + valueOffset, helperTextSuffix);

            float helperTextWidth = helperTextPaint.measureText(helperText);
            Rect helperTextBounds = new Rect();
            helperTextPaint.getTextBounds(helperText, 0, helperText.length(), helperTextBounds);
            float helperTextHeight = helperTextBounds.height();

            helperTextX = thumbX + (helperCenterX - thumbX) / 2.0f - helperTextWidth / 2;
            helperTextY = thumbY + (helperCenterY - thumbY) / 2.0f + helperTextHeight / 2;

            final float rectMargin = helperTextHeight * 0.2f;
            helperTextRect = new RectF(helperTextX - rectMargin, helperTextY - helperTextHeight - rectMargin, helperTextX + helperTextWidth + rectMargin, helperTextY + rectMargin);
        } else {
            helperPath.moveTo(helperCenterX, helperCenterY);
            helperPath.lineTo(labelX, labelY);

            helperPaint.setColor(labelColor);
            helperTextRectPaint.setColor(labelColor);

            helperText = String.format(Locale.ENGLISH, "%d%s", labelPosition + valueOffset, helperTextSuffix);

            float helperTextWidth = helperTextPaint.measureText(helperText);
            Rect helperTextBounds = new Rect();
            helperTextPaint.getTextBounds(helperText, 0, helperText.length(), helperTextBounds);
            float helperTextHeight = helperTextBounds.height();

            helperTextX = labelX + (helperCenterX - labelX) / 2.0f - helperTextWidth / 2;
            helperTextY = labelY + (helperCenterY - labelY) / 2.0f + helperTextHeight / 2;

            final float rectMargin = helperTextHeight * 0.2f;
            helperTextRect = new RectF(helperTextX - rectMargin, helperTextY - helperTextHeight - rectMargin, helperTextX + helperTextWidth + rectMargin, helperTextY + rectMargin);
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(track, trackPaint);

//        canvas.drawPoint(arcCenterX, arcCenterY, helperPaint);
        canvas.drawPath(helperPath, helperPaint);

        canvas.drawPoint(labelX, labelY, labelPointPaint);

        canvas.drawRoundRect(helperTextRect, 5, 5, helperTextRectFillPaint);
        canvas.drawRoundRect(helperTextRect, 5, 5, helperTextRectPaint);
        canvas.drawText(helperText, helperTextX, helperTextY, helperTextPaint);

        thumb.draw(canvas);
    }

    public boolean getOnTouchAction(){
        return touchTransactionOpen;
    }

    public void setOnChangeListener(OnChangeListener changeListener){
        this.changeListener = changeListener;
    }

    public interface OnChangeListener {

        void onStartTrackingTouch(ArcSlider view);
        void onProgressChanged(ArcSlider view, int progress, boolean fromUser);
        void onStopTrackingTouch(ArcSlider view);
    }

    public void setProgress(int progress){
        this.progress = checkRangeAndTrim(progress, 0, maxProgress);
        if (changeListener != null)
            changeListener.onProgressChanged(this, progress, false);
        updateThumbPosition();
        updateHelper();
    }

    public int getProgress(){
        return progress;
    }

    public void setLabelPosition(int value){
        labelPosition = checkRangeAndTrim(value, 0, maxProgress);
        updateLabelPosition();
        updateHelper();
        invalidate();
    }
}
