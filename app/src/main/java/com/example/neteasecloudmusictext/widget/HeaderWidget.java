package com.example.neteasecloudmusic.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;


import androidx.annotation.Nullable;

public class HeaderWidget extends androidx.appcompat.widget.AppCompatImageView {

    private Bitmap mBitmap;
    private final Rect rect  = new Rect();
    private final PaintFlagsDrawFilter paintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG);
    private final Paint paint = new Paint();
    private final Path path = new Path();


    public HeaderWidget(Context context) {
        super(context);
    }

    public HeaderWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
    }

    public void setBitmap(Bitmap bitmap){
        this.mBitmap = bitmap;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mBitmap==null){
          return;
        }
        rect.set(0,0,getWidth(),getHeight());
        canvas.save();
        canvas.setDrawFilter(paintFlagsDrawFilter);
        path.addCircle(getWidth() / 2, getWidth() / 2, getHeight() / 2, Path.Direction.CCW);
        canvas.clipPath(path, Region.Op.REPLACE);
        canvas.drawBitmap(mBitmap, null, rect, paint);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }
}
