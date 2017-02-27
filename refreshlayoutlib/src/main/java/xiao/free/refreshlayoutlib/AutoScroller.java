package xiao.free.refreshlayoutlib;

import android.view.View;
import android.widget.Scroller;

/**
 * Created by robincxiao on 2017/2/23.
 */

public class AutoScroller implements Runnable{
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Scroller mScroller;
    private ScrollerCallback mScrollerCallback;
    private int mmLastY;

    private boolean mRunning = false;

    private boolean mAbort = false;

    public AutoScroller(SwipeRefreshLayout refreshLayout) {
        mSwipeRefreshLayout = refreshLayout;
        mScroller = new Scroller(refreshLayout.getContext());
    }

    public void setScrollerCallback(ScrollerCallback mScrollerCallback) {
        this.mScrollerCallback = mScrollerCallback;
    }

    @Override
    public void run() {
        boolean finish = !mScroller.computeScrollOffset() || mScroller.isFinished();
        int currY = mScroller.getCurrY();
        int yDiff = currY - mmLastY;
        if (finish) {
            finish();
        } else {
            mmLastY = currY;
            if(mScrollerCallback != null){
                mScrollerCallback.scrollBy(yDiff);
            }
            mSwipeRefreshLayout.post(this);
        }
    }

    /**
     * remove the post callbacks and reset default values
     */
    private void finish() {
        mmLastY = 0;
        mRunning = false;
        mSwipeRefreshLayout.removeCallbacks(this);
        // if abort by user, don't call
        if (!mAbort) {
            if(mScrollerCallback != null){
                mScrollerCallback.scrollFinished();
            }
        }
    }

    /**
     * abort scroll if it is scrolling
     */
    public void abortIfRunning() {
        if (mRunning) {
            if (!mScroller.isFinished()) {
                mAbort = true;
                mScroller.forceFinished(true);
            }
            finish();
            mAbort = false;
        }
    }

    /**
     * The param yScrolled here isn't final pos of y.
     * It's just like the yScrolled param in the
     * {@link #updateScroll(float yScrolled)}
     *
     * @param yScrolled
     * @param duration
     */
    public void autoScroll(int yScrolled, int duration) {
        mSwipeRefreshLayout.removeCallbacks(this);
        mmLastY = 0;
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
        mScroller.startScroll(0, 0, 0, yScrolled, duration);
        mSwipeRefreshLayout.post(this);
        mRunning = true;
    }

    public interface ScrollerCallback{
        /**
         * 滑动过程回调
         * @param yScrolled 每次计算滑动的距离
         */
        void scrollBy(final float yScrolled);

        /**
         * 滑动结束
         */
        void scrollFinished();
    }
}
