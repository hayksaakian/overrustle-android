package gg.destiny.app;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class YoutubeLayout extends ViewGroup {

	private final ViewDragHelper mDragHelper;

	private View mHeaderView;
//	private View mDescView; // unused

	private float mInitialMotionX;
	private float mInitialMotionY;

	private int mDragRange;
	private int mTop;
	private int mLeft;
	private float mDragOffset;
	
	private float mDisplayDensity;


	public YoutubeLayout(Context context) {
	  this(context, null);
	}

	public YoutubeLayout(Context context, AttributeSet attrs) {
	  this(context, attrs, 0);
	}

	@Override
	protected void onFinishInflate() {
		// TODO: setup layout
	    mHeaderView = findViewById(R.id.video);
//	    mDescView = findViewById(R.id.viewDesc);
	}

	public YoutubeLayout(Context context, AttributeSet attrs, int defStyle) {
	  super(context, attrs, defStyle);
	  mDragHelper = ViewDragHelper.create(this, 1f, new DragHelperCallback());
	}

	public void maximize() {
	    smoothSlideTo(0f);
	}

	boolean smoothSlideTo(float slideOffset) {
	    final int topBound = getPaddingTop();
	    int y = (int) (topBound + slideOffset * mDragRange);
	 // recalculate offset to account for when you just tap to maximize/minimize
		//mDragOffset = (float)y / mDragRange;
	    Log.d("Recalc Offset", String.valueOf(mDragOffset));
	
	    if (mDragHelper.smoothSlideViewTo(mHeaderView, mHeaderView.getLeft(), y)) {
	        ViewCompat.postInvalidateOnAnimation(this);
	        
	        return true;
	    }
	    return false;
	}
//	for left to right
//	boolean smoothSlideTo(float slideOffset) {
//	    final int leftBound = getPaddingLeft();
//	    int x = (int) (leftBound + slideOffset * mDragRange);
//	 // recalculate offset to account for when you just tap to maximize/minimize
//		//mDragOffset = (float)y / mDragRange;
//	    Log.d("Recalc Offset", String.valueOf(mDragOffset));
//	
//	    if (mDragHelper.smoothSlideViewTo(mHeaderView, x, mHeaderView.getTop())) {
//	        ViewCompat.postInvalidateOnAnimation(this);
//	        
//	        return true;
//	    }
//	    return false;
//	}

	private class DragHelperCallback extends ViewDragHelper.Callback {

	  @Override
	  public boolean tryCaptureView(View child, int pointerId) {
	        return child == mHeaderView;
	  }

	    @Override
	  public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
	      mTop = top; // this is the only place this is assigned
	      mLeft = left;

	      mDragOffset = (float) top / mDragRange;
//	      mDragOffset = (float) left / mDragRange;
	      

//	      facts: scaling here crops the videoview
	      // scaling onlayout does NOT!
//	        mHeaderView.setPivotX(mHeaderView.getWidth());
//	        mHeaderView.setPivotY(mHeaderView.getHeight());
//	        mHeaderView.setScaleX(1 - mDragOffset / 2); // does what it says
//	        mHeaderView.setScaleY(1 - mDragOffset / 2); // does what it says

	        //mDescView.setAlpha(1 - mDragOffset);

//	        TODO forcing a redraw should probably happen here!
	        requestLayout();
	  }

		  @Override
		  public void onViewReleased(View releasedChild, float xvel, float yvel) {
		      int top = getPaddingTop();
		      if (yvel > 0 || (yvel == 0 && mDragOffset > 0.5f)) {
		          top += mDragRange;
		      }
		      mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
		  }
//		  for side to side
//		  @Override
//		  public void onViewReleased(View releasedChild, float xvel, float yvel) {
//		      int left = getPaddingLeft();
//		      if (xvel > 0 || (xvel == 0 && mDragOffset > 0.5f)) {
//		          left += mDragRange;
//		      }
//		      mDragHelper.settleCapturedViewAt(left, releasedChild.getTop());
//		  }

	  @Override
	  public int getViewVerticalDragRange(View child) {
	      return mDragRange;
	  }
	  
//	  @Override
//	  public int getViewHorizontalDragRange(View child) {
//	      return mDragRange;
//	  }
//
//	  @Override
//	  public int clampViewPositionHorizontal(View child, int left, int dx) {
////	    Log.d("DragLayout", "clampViewPositionHorizontal " + left + "," + dx);
//
//	    final int leftBound = getPaddingLeft();
//	    final int rightBound = getWidth() - mHeaderView.getWidth();
//
//	    final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
//
//	    return newLeft;
//	  }
	  
	  @Override
	  public int clampViewPositionVertical(View child, int top, int dy) {
	      final int topBound = getPaddingTop();
	      final int bottomBound = getHeight() - mHeaderView.getHeight() - mHeaderView.getPaddingBottom();

	      final int newTop = Math.min(Math.max(top, topBound), bottomBound);
	      return newTop;
	  }

	}

	@Override
	public void computeScroll() {
	  if (mDragHelper.continueSettling(true)) {
	      ViewCompat.postInvalidateOnAnimation(this);
	  }
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
	  final int action = MotionEventCompat.getActionMasked(ev);

	  if (( action != MotionEvent.ACTION_DOWN)) {
	      mDragHelper.cancel();
	      return super.onInterceptTouchEvent(ev);
	  }

	  if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
	      mDragHelper.cancel();
	      return false;
	  }

	  final float x = ev.getX();
	  final float y = ev.getY();
	  boolean interceptTap = false;

	  switch (action) {
	      case MotionEvent.ACTION_DOWN: {
	          mInitialMotionX = x;
	          mInitialMotionY = y;
	            interceptTap = mDragHelper.isViewUnder(mHeaderView, (int) x, (int) y);
	          break;
	      }

	      case MotionEvent.ACTION_MOVE: {
	          final float adx = Math.abs(x - mInitialMotionX);
	          final float ady = Math.abs(y - mInitialMotionY);
	          final int slop = mDragHelper.getTouchSlop();
	          if (ady > slop && adx > ady) {
	              mDragHelper.cancel();
	              return false;
	          }
	      }
	  }

	  return mDragHelper.shouldInterceptTouchEvent(ev) || interceptTap;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
//		if(true){
//			return false;
//		}
	  mDragHelper.processTouchEvent(ev);

	  final int action = ev.getAction();
	    final float x = ev.getX();
	    final float y = ev.getY();

	    boolean isHeaderViewUnder = mDragHelper.isViewUnder(mHeaderView, (int) x, (int) y);
	    switch (action & MotionEventCompat.ACTION_MASK) {
	      case MotionEvent.ACTION_DOWN: {
	          mInitialMotionX = x;
	          mInitialMotionY = y;
	          break;
	      }

	      case MotionEvent.ACTION_UP: {
	          final float dx = x - mInitialMotionX;
	          final float dy = y - mInitialMotionY;
	          final int slop = mDragHelper.getTouchSlop();
	          if (dx * dx + dy * dy < slop * slop && isHeaderViewUnder) {
	              if (mDragOffset == 0) {
	                  smoothSlideTo(1f);
	              } else {
	                  smoothSlideTo(0f);
	              }
	          }
	          break;
	      }
	  }


	  return isHeaderViewUnder && isViewHit(mHeaderView, (int) x, (int) y);
	}


	private boolean isViewHit(View view, int x, int y) {
	    int[] viewLocation = new int[2];
	    view.getLocationOnScreen(viewLocation);
	    int[] parentLocation = new int[2];
	    this.getLocationOnScreen(parentLocation);
	    int screenX = parentLocation[0] + x;
	    int screenY = parentLocation[1] + y;
	    return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.getWidth() &&
	            screenY >= viewLocation[1] && screenY < viewLocation[1] + view.getHeight();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    measureChildren(widthMeasureSpec, heightMeasureSpec);

	    int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
	    int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

	    setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
	            resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	  // below works fine by itself when dragging (with minor errors in the bottom right)
		
	  mDragRange = getHeight() - mHeaderView.getHeight();
//		mDragRange = getWidth() - mHeaderView.getWidth();
		  
	  
	  //resize here because videoview crops instead of scaling

//      mHeaderView.setPivotX(mHeaderView.getWidth());
//      mHeaderView.setPivotY(mHeaderView.getHeight());
	  float computedScale = (1 - mDragOffset / 2);
//	  Log.d("SCALE IS",String.valueOf(computedScale)); // verified 1 at full and ~0.5  at min
	  int height = mHeaderView.getMeasuredHeight();
	  int width = mHeaderView.getMeasuredWidth();
//      mHeaderView.setScaleX(1 - mDragOffset / 2); // does what it says
//      mHeaderView.setScaleY(1 - mDragOffset / 2); // does what it says
  // TODO: left, top, right, bottom
	  int normalL = 0;
	  int normalT = mTop; 
	  int normalR = r;
	  int normalB =  mTop + mHeaderView.getMeasuredHeight(); // the measured height may be constant

//		DisplayMetrics metrics = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(metrics);
//		int wd = (int) (160*metrics.density);
//		int ht = (int) (90*metrics.density);

	  int newL = normalR - (int)(((float)normalR)*computedScale); // perfect
	  int newT = (mTop + (int)(height*((1f-computedScale)/2))); 

	  /**
	   	at full top should be mTop
	   	at min it should be mTop + half of height
	  	computed scale ranges from 1 (max) to 0.5 (min)
	   * mTop + (height * ((1-scale)/2))
	   *  
	   * **/
	  
	  
	  //+ (int)(((float)normalB)*computedScale);
//	  int newR = (int)(((float)normalR)*computedScale);
//	  int newB = (int)(((float)normalB)*computedScale);
//	  tweaked
	    mHeaderView.layout(
	            newL,
	            newT,
	            normalR,
	           	normalB);
	  
//	  normal
//      mHeaderView.layout(
//              0,
//              mTop,
//              r,
//              mTop + mHeaderView.getMeasuredHeight());
	  
//	  left to right
//      mHeaderView.layout(
//              mLeft,
//              0,
//              mLeft + mHeaderView.getMeasuredWidth(),
//              (int)(height*computedScale));
//      
//	  tweaked left to right
//      mHeaderView.layout(
//              mLeft,
//              0,
//              mLeft + mHeaderView.getMeasuredWidth(),
//              (int)(height*computedScale));
//
//	    mDescView.layout(
//	            0,
//	            mTop + mHeaderView.getMeasuredHeight(),
//	            r,
//	            mTop  + b);
	}
}