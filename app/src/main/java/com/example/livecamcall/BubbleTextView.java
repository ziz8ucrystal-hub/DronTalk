package com.example.livecamcall;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.widget.TextView;

public class BubbleTextView extends TextView {

    private Paint bubblePaint;
    private Paint arrowPaint;
    private int bubbleColor = Color.parseColor("#2196F3");
    private int arrowPosition = 1;

    public BubbleTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setColor(bubbleColor);
        bubblePaint.setStyle(Paint.Style.FILL);
        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(bubbleColor);
        setTextColor(Color.WHITE);
        setPadding(40, 30, 40, 30);
    }

    public void setBubbleColor(int color) {
        bubbleColor = color;
        bubblePaint.setColor(color);
        arrowPaint.setColor(color);
        invalidate();
    }

    public void setArrowPosition(int position) {
        arrowPosition = position;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        RectF rect = new RectF(10, 10, width - 10, height - 10);
        canvas.drawRoundRect(rect, 30, 30, bubblePaint);
        float centerX = width / 2;
        if (arrowPosition == 1) {
            canvas.drawCircle(centerX, 5, 10, arrowPaint);
        }
    }
}