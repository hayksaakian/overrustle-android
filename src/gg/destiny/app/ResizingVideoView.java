package gg.destiny.app;

import android.content.*;
import android.media.*;
import android.util.*;
import android.widget.*;
import android.view.*;

public class ResizingVideoView extends VideoView {
	
	public ProgressBar progressBar;

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

	public void showProgress(){
		// TODO: Implement this method
		if(progressBar != null){
			progressBar.setVisibility(View.VISIBLE);
		}
	}
    public void setDimensions(int w, int h) {
        this.mForceHeight = h;
        this.mForceWidth  = w;

    }
	
	//@Override
	//l

	@Override
	public void start(){
		//showProgress();
		super.start();
	}
	
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.i("@@@@", "onMeasure");

        //setMeasuredDimension(mForceWidth, mForceHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
