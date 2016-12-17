package com.infracity.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by pragadeesh on 15/12/16.
 */
public class HollowView extends RelativeLayout {
    private Bitmap bitmap;

    private Point[] points;

    public HollowView(Context context) {
        super(context);
    }

    public HollowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HollowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (bitmap == null) {
            createWindowFrame();
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    protected void createWindowFrame() {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas osCanvas = new Canvas(bitmap);

        RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xAA000000);
        osCanvas.drawRect(outerRectangle, paint);

        paint.setColor(Color.TRANSPARENT);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = (int) (centerX/1.5);
        int left = centerX - radius;
        int top = centerY - radius;
        int right = centerX + radius;
        int bottom = centerY + radius;
        points = new Point[4];
        points[0] = new Point(left, top);
        points[1] = new Point(right, top);
        points[2] = new Point(right, bottom);
        points[3] = new Point(left, bottom);
        RectF innerRectangle = new RectF(left, top, right, bottom);
        osCanvas.drawRect(innerRectangle, paint);
    }

    public Point[] getPoints() {
        return points;
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        bitmap = null;
    }
}
