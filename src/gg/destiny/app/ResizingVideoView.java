package gg.destiny.app;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

public class ResizingVideoView extends VideoView {

	private int mForceHeight = 5;
    private int ResizingVideoView = 5;
	private int mForceWidth = 5;
	
    public ResizingVideoView(Context context) {
        super(context);
    }

    public ResizingVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResizingVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public void setDimensions(int w, int h) {
        this.mForceHeight = h;
        this.mForceWidth  = w;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.i("@@@@", "onMeasure");

        //setMeasuredDimension(mForceWidth, mForceHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
