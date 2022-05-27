package com.blackghost.extendedcontrols;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class ArcSlider extends View {

    private int startAngle;
    private int sweepAngle;

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

            if (attr == R.styleable.ArcSlider_startAngle) {
                startAngle = a.getInt(attr, 0);
            } else if (attr == R.styleable.ArcSlider_sweepAngle) {
                sweepAngle = a.getInt(attr, 360);
            }
//            else if (attr == R.styleable.ArcSlider_ico) {
//                ic = a.getDrawable(attr);
//            } else if (attr == R.styleable.ArcSlider_onConfirmPressed) {
//                if (context.isRestricted()) {
//                    throw new IllegalStateException("The android:onClick attribute cannot "
//                            + "be used within a restricted context");
//                }
//
//                final String handlerName = a.getString(attr);
//                if (handlerName != null) {
//                    setOnConfirmPressed(new DeclaredOnClickListener(this, handlerName));
//                }
//            }

        }

        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


    }
}
