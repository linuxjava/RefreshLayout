package xiao.free.refreshlayoutlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import xiao.free.refreshlayoutlib.base.OnLoadMoreListener;
import xiao.free.refreshlayoutlib.base.OnRefreshListener;
import xiao.free.refreshlayoutlib.base.SwipeLoadMoreTrigger;
import xiao.free.refreshlayoutlib.base.SwipeRefreshTrigger;
import xiao.free.refreshlayoutlib.base.SwipeTrigger;

/**
 * Created by robincxiao on 2017/2/21.
 * 1.创建刷新回调实现接口
 * 2.布局header、target、footer三个view
 * 3.处理drag事件
 * 3.1 自然滑动效果
 * 3.2 header可见且向上滑动时，target滑动到顶部场景的处理;footer可见且向下滑动时，target滑动到底部场景的处理
 * 3.3 header滑动到最大距离的处理;footer滑动到最大距离的处理
 * 4.处理手指UP事件
 * 4.1 在SWIPING_TO_REFRESH和SWIPING_TO_LOAD_MORE状态释放
 * 4.2 在RELEASE_TO_REFRESH和RELEASE_TO_LOAD_MORE状态释放
 * 4.3 在SWIPING_TO_REFRESH、SWIPING_TO_LOAD_MORE、RELEASE_TO_REFRESH和RELEASE_TO_LOAD_MORE四种状态释放后，手指再次按下，需停止滚动
 * 5.状态回调
 * 5.1 自动刷新启动和关闭（在REFRESHING和LOADING_MORE状态自动回到DEFAULT状态，DEFAULT状态到REFRESHING和LOADING_MORE状态）
 * 5.2 完成header和footer各状态的回调
 * 6.多手指触摸bug解决
 * 6.1 第二个手指按下时需更新当前activePoniterId，以及mLastX和mLastY
 * 6.1 当非最后一个手指up时，需判断当前抬起的手指是否是当前活跃的activePoniterId，然后更新activePoniterId以及mLastX和mLastY
 * 7.完成各种类型刷新header和footer
 * 8.header和footer模式支持bottom、above、scale模式
 * 9.自定义属性
 */
public class SwipeRefreshLayout extends ViewGroup implements AutoScroller.ScrollerCallback {
    private static final String TAG = "xiao1";
    private static final int DEFAULT_SWIPING_TO_REFRESH_TO_DEFAULT_SCROLLING_DURATION = 200;
    private static final int DEFAULT_RELEASE_TO_REFRESHING_SCROLLING_DURATION = 200;
    private static final int DEFAULT_REFRESH_COMPLETE_DELAY_DURATION = 300;
    private static final int DEFAULT_REFRESH_COMPLETE_TO_DEFAULT_SCROLLING_DURATION = 500;
    private static final int DEFAULT_DEFAULT_TO_REFRESHING_SCROLLING_DURATION = 500;
    private static final int DEFAULT_SWIPING_TO_LOAD_MORE_TO_DEFAULT_SCROLLING_DURATION = 200;
    private static final int DEFAULT_RELEASE_TO_LOADING_MORE_SCROLLING_DURATION = 200;
    private static final int DEFAULT_LOAD_MORE_COMPLETE_DELAY_DURATION = 300;
    private static final int DEFAULT_LOAD_MORE_COMPLETE_TO_DEFAULT_SCROLLING_DURATION = 300;
    private static final int DEFAULT_DEFAULT_TO_LOADING_MORE_SCROLLING_DURATION = 300;
    private static final float DEFAULT_DRAG_RATIO = 0.5f;
    private static final int INVALID_POINTER = -1;
    private static final int INVALID_COORDINATE = -1;
    private int mSwipingToRefreshToDefaultScrollingDuration = DEFAULT_SWIPING_TO_REFRESH_TO_DEFAULT_SCROLLING_DURATION;
    private int mReleaseToRefreshToRefreshingScrollingDuration = DEFAULT_RELEASE_TO_REFRESHING_SCROLLING_DURATION;
    private int mRefreshCompleteDelayDuration = DEFAULT_REFRESH_COMPLETE_DELAY_DURATION;
    private int mRefreshCompleteToDefaultScrollingDuration = DEFAULT_REFRESH_COMPLETE_TO_DEFAULT_SCROLLING_DURATION;
    private int mDefaultToRefreshingScrollingDuration = DEFAULT_DEFAULT_TO_REFRESHING_SCROLLING_DURATION;
    private int mReleaseToLoadMoreToLoadingMoreScrollingDuration = DEFAULT_SWIPING_TO_LOAD_MORE_TO_DEFAULT_SCROLLING_DURATION;
    private int mLoadMoreCompleteDelayDuration = DEFAULT_RELEASE_TO_LOADING_MORE_SCROLLING_DURATION;
    private int mLoadMoreCompleteToDefaultScrollingDuration = DEFAULT_LOAD_MORE_COMPLETE_DELAY_DURATION;
    private int mSwipingToLoadMoreToDefaultScrollingDuration = DEFAULT_LOAD_MORE_COMPLETE_TO_DEFAULT_SCROLLING_DURATION;
    private int mDefaultToLoadingMoreScrollingDuration = DEFAULT_DEFAULT_TO_LOADING_MORE_SCROLLING_DURATION;
    private AutoScroller mAutoScroller;
    //当前活跃PointerId
    private int mActivePointerId;
    //drag阻尼系数
    private float mDragRatio = 0.5f;
    //刷新header
    private View mHeaderView;
    //目标TargetView
    private View mTargetView;
    //刷新footer
    private View mFooterView;
    //Header高度(包含topMargin和bottomMargin)
    private int mHeaderHeight;
    //Footer高度(包含topMargin和bottomMargin)
    private int mFooterHeight;
    //下拉刷新TriggerOffset
    private float mRefreshTriggerOffset;
    //上拉加载更多TriggerOffset
    private float mLoadMoreTriggerOffset;
    //header最大偏移值
    private float mRefreshFinalDragOffset;
    //footer最大偏移值
    private float mLoadMoreFinalDragOffset;
    //当前header的offset
    private int mHeaderOffset;
    //当前target的offset
    private int mTargetOffset;
    //当前footer的offset
    private int mFooterOffset;
    private boolean mHasHeaderView;
    private boolean mHasFooterView;
    private float mInitX;
    private float mInitY;
    private float mLastX;
    private float mLastY;
    private int mTouchSlop;
    //当前滑动状态
    private int mStatus = STATUS.STATUS_DEFAULT;
    //是否自动刷新
    private boolean mAutoLoading;
    //header使能开关
    private boolean mRefreshEnabled = true;
    //footer使能开关
    private boolean mLoadMoreEnabled = true;
    //刷新回调
    private OnRefreshListener mRefreshListener;
    //加载更多回调
    private OnLoadMoreListener mLoadMoreListener;

    //header、footer显示模式
    public static final class STYLE {
        public static final int CLASSIC = 0;
        public static final int ABOVE = 1;
        public static final int BLOW = 2;
        public static final int SCALE = 3;
    }

    private int mStyle = STYLE.CLASSIC;


    public SwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout, defStyleAttr, 0);
        try {
            final int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.RefreshLayout_refresh_enabled) {
                    setRefreshEnabled(a.getBoolean(attr, true));

                } else if (attr == R.styleable.RefreshLayout_load_more_enabled) {
                    setLoadMoreEnabled(a.getBoolean(attr, true));

                } else if (attr == R.styleable.RefreshLayout_swipe_style) {
                    setSwipeStyle(a.getInt(attr, STYLE.CLASSIC));

                } else if (attr == R.styleable.RefreshLayout_drag_ratio) {
                    setDragRatio(a.getFloat(attr, DEFAULT_DRAG_RATIO));

                } else if (attr == R.styleable.RefreshLayout_refresh_final_drag_offset) {
                    setRefreshFinalDragOffset(a.getDimensionPixelOffset(attr, 0));

                } else if (attr == R.styleable.RefreshLayout_load_more_final_drag_offset) {
                    setLoadMoreFinalDragOffset(a.getDimensionPixelOffset(attr, 0));

                } else if (attr == R.styleable.RefreshLayout_refresh_trigger_offset) {
                    setRefreshTriggerOffset(a.getDimensionPixelOffset(attr, 0));

                } else if (attr == R.styleable.RefreshLayout_load_more_trigger_offset) {
                    setLoadMoreTriggerOffset(a.getDimensionPixelOffset(attr, 0));

                } else if (attr == R.styleable.RefreshLayout_swiping_to_refresh_to_default_scrolling_duration) {
                    setSwipingToRefreshToDefaultScrollingDuration(a.getInt(attr, DEFAULT_SWIPING_TO_REFRESH_TO_DEFAULT_SCROLLING_DURATION));

                } else if (attr == R.styleable.RefreshLayout_release_to_refreshing_scrolling_duration) {
                    setReleaseToRefreshingScrollingDuration(a.getInt(attr, DEFAULT_RELEASE_TO_REFRESHING_SCROLLING_DURATION));

                } else if (attr == R.styleable.RefreshLayout_refresh_complete_delay_duration) {
                    setRefreshCompleteDelayDuration(a.getInt(attr, DEFAULT_REFRESH_COMPLETE_DELAY_DURATION));

                } else if (attr == R.styleable.RefreshLayout_refresh_complete_to_default_scrolling_duration) {
                    setRefreshCompleteToDefaultScrollingDuration(a.getInt(attr, DEFAULT_REFRESH_COMPLETE_TO_DEFAULT_SCROLLING_DURATION));

                } else if (attr == R.styleable.RefreshLayout_default_to_refreshing_scrolling_duration) {
                    setDefaultToRefreshingScrollingDuration(a.getInt(attr, DEFAULT_DEFAULT_TO_REFRESHING_SCROLLING_DURATION));

                } else if (attr == R.styleable.RefreshLayout_swiping_to_load_more_to_default_scrolling_duration) {
                    setSwipingToLoadMoreToDefaultScrollingDuration(a.getInt(attr, DEFAULT_SWIPING_TO_LOAD_MORE_TO_DEFAULT_SCROLLING_DURATION));

                } else if (attr == R.styleable.RefreshLayout_release_to_loading_more_scrolling_duration) {
                    setReleaseToLoadingMoreScrollingDuration(a.getInt(attr, DEFAULT_RELEASE_TO_LOADING_MORE_SCROLLING_DURATION));

                } else if (attr == R.styleable.RefreshLayout_load_more_complete_delay_duration) {
                    setLoadMoreCompleteDelayDuration(a.getInt(attr, DEFAULT_LOAD_MORE_COMPLETE_DELAY_DURATION));

                } else if (attr == R.styleable.RefreshLayout_load_more_complete_to_default_scrolling_duration) {
                    setLoadMoreCompleteToDefaultScrollingDuration(a.getInt(attr, DEFAULT_LOAD_MORE_COMPLETE_TO_DEFAULT_SCROLLING_DURATION));

                } else if (attr == R.styleable.RefreshLayout_default_to_loading_more_scrolling_duration) {
                    setDefaultToLoadingMoreScrollingDuration(a.getInt(attr, DEFAULT_DEFAULT_TO_LOADING_MORE_SCROLLING_DURATION));
                }
            }
        } finally {
            a.recycle();
        }

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mAutoScroller = new AutoScroller(this);
        mAutoScroller.setScrollerCallback(this);
    }

    public boolean isRefreshEnabled() {
        return mRefreshEnabled;
    }

    public void setRefreshEnabled(boolean enable) {
        this.mRefreshEnabled = enable;
    }

    public boolean isLoadMoreEnabled() {
        return mLoadMoreEnabled;
    }

    public void setLoadMoreEnabled(boolean enable) {
        this.mLoadMoreEnabled = enable;
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mRefreshListener = listener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mLoadMoreListener = listener;
    }

    public void setSwipeStyle(int style) {
        this.mStyle = style;
        requestLayout();
    }

    public void setDragRatio(float dragRatio) {
        this.mDragRatio = dragRatio;
    }

    public void setRefreshTriggerOffset(int offset) {
        mRefreshTriggerOffset = offset;
    }

    public void setLoadMoreTriggerOffset(int offset) {
        mLoadMoreTriggerOffset = offset;
    }

    public void setRefreshFinalDragOffset(int offset) {
        mRefreshFinalDragOffset = offset;
    }

    public void setLoadMoreFinalDragOffset(int offset) {
        mLoadMoreFinalDragOffset = offset;
    }

    public void setSwipingToRefreshToDefaultScrollingDuration(int duration) {
        this.mSwipingToRefreshToDefaultScrollingDuration = duration;
    }

    public void setReleaseToRefreshingScrollingDuration(int duration) {
        this.mReleaseToRefreshToRefreshingScrollingDuration = duration;
    }

    public void setRefreshCompleteDelayDuration(int duration) {
        this.mRefreshCompleteDelayDuration = duration;
    }

    public void setRefreshCompleteToDefaultScrollingDuration(int duration) {
        this.mRefreshCompleteToDefaultScrollingDuration = duration;
    }

    public void setDefaultToRefreshingScrollingDuration(int duration) {
        this.mDefaultToRefreshingScrollingDuration = duration;
    }

    public void setSwipingToLoadMoreToDefaultScrollingDuration(int duration) {
        this.mSwipingToLoadMoreToDefaultScrollingDuration = duration;
    }

    public void setReleaseToLoadingMoreScrollingDuration(int duration) {
        this.mReleaseToLoadMoreToLoadingMoreScrollingDuration = duration;
    }

    public void setLoadMoreCompleteDelayDuration(int duration) {
        this.mLoadMoreCompleteDelayDuration = duration;
    }

    public void setLoadMoreCompleteToDefaultScrollingDuration(int duration) {
        this.mLoadMoreCompleteToDefaultScrollingDuration = duration;
    }

    public void setDefaultToLoadingMoreScrollingDuration(int duration) {
        this.mDefaultToLoadingMoreScrollingDuration = duration;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final int childNum = getChildCount();
        if (childNum == 0) {
            // no child return
            return;
        } else if (0 < childNum && childNum < 4) {
            mHeaderView = findViewById(R.id.swipe_refresh_header);
            mTargetView = findViewById(R.id.swipe_target);
            mFooterView = findViewById(R.id.swipe_load_more_footer);
        } else {
            throw new IllegalStateException("Children num must equal or less than 3");
        }

        if (mTargetView == null) {
            return;
        }
        if (mHeaderView != null && mHeaderView instanceof SwipeTrigger) {
            mHeaderView.setVisibility(GONE);
        }
        if (mFooterView != null && mFooterView instanceof SwipeTrigger) {
            mFooterView.setVisibility(GONE);
        }
    }

    /**
     * auto refresh or cancel
     *
     * @param refreshing
     */
    public void setRefreshing(boolean refreshing) {
        if (!isRefreshEnabled() || mHeaderView == null) {
            return;
        }
        this.mAutoLoading = refreshing;

        if (refreshing) {
            if (STATUS.isStatusDefault(mStatus)) {
                setStatus(STATUS.STATUS_SWIPING_TO_REFRESH);
                scrollDefaultToRefreshing();
            }
        } else {
            if (STATUS.isRefreshing(mStatus)) {
                mRefreshCallback.onComplete();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollRefreshingToDefault();
                    }
                }, mRefreshCompleteDelayDuration);
            }
        }
    }

    /**
     * auto loading more or cancel
     *
     * @param loadingMore
     */
    public void setLoadingMore(boolean loadingMore) {
        if (!isLoadMoreEnabled() || mFooterView == null) {
            return;
        }

        this.mAutoLoading = loadingMore;

        if (loadingMore) {
            if (STATUS.isStatusDefault(mStatus)) {
                setStatus(STATUS.STATUS_SWIPING_TO_LOAD_MORE);
                scrollDefaultToLoadingMore();
            }
        } else {
            if (STATUS.isLoadingMore(mStatus)) {
                mLoadMoreCallback.onComplete();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollLoadingMoreToDefault();
                    }
                }, mLoadMoreCompleteDelayDuration);
            }
        }
    }

    /**
     * Measure child
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mHeaderView != null) {
            final View headerView = mHeaderView;
            measureChildWithMargins(headerView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = ((MarginLayoutParams) headerView.getLayoutParams());

            mHeaderHeight = headerView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            if (mRefreshTriggerOffset < mHeaderHeight) {
                mRefreshTriggerOffset = mHeaderHeight;
            }
        }

        if (mFooterView != null) {
            final View footerView = mFooterView;
            measureChildWithMargins(footerView, widthMeasureSpec, 0, heightMeasureSpec, 0);

            MarginLayoutParams lp = ((MarginLayoutParams) footerView.getLayoutParams());
            mFooterHeight = footerView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            if (mLoadMoreTriggerOffset < mFooterHeight) {
                mLoadMoreTriggerOffset = mFooterHeight;
            }
        }

        if (mTargetView != null) {
            final View targetView = mTargetView;
            measureChildWithMargins(targetView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren();

        mHasHeaderView = (mHeaderView != null);
        mHasFooterView = (mFooterView != null);
    }

    private void layoutChildren() {
        int height = getMeasuredHeight();

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        if (mTargetView == null) {
            return;
        }

        if (mHeaderView != null) {
            final View headerView = mHeaderView;
            MarginLayoutParams lp = (MarginLayoutParams) headerView.getLayoutParams();
            int left = paddingLeft + lp.leftMargin;
            int top;
            switch (mStyle) {
                case STYLE.CLASSIC:
                    top = paddingTop + lp.topMargin - mHeaderHeight + mHeaderOffset;
                    break;
                case STYLE.ABOVE:
                    top = paddingTop + lp.topMargin - mHeaderHeight + mHeaderOffset;
                    break;
                case STYLE.BLOW:
                    top = paddingTop + lp.topMargin;
                    break;
                case STYLE.SCALE:
                    top = paddingTop + lp.topMargin - mHeaderHeight / 2 + mHeaderOffset / 2;
                    break;
                default:
                    // classic
                    top = paddingTop + lp.topMargin - mHeaderHeight + mHeaderOffset;
                    break;
            }
            int right = left + headerView.getMeasuredWidth();
            int bottom = top + headerView.getMeasuredHeight();

            headerView.layout(left, top, right, bottom);
        }

        if (mTargetView != null) {
            final View targetView = mTargetView;
            MarginLayoutParams lp = (MarginLayoutParams) targetView.getLayoutParams();
            int left = paddingLeft + lp.leftMargin;
            int top;
            switch (mStyle) {
                case STYLE.CLASSIC:
                    top = paddingTop + lp.topMargin + mTargetOffset;
                    break;
                case STYLE.ABOVE:
                    top = paddingTop + lp.topMargin;
                    break;
                case STYLE.BLOW:
                    top = paddingTop + lp.topMargin + mTargetOffset;
                    break;
                case STYLE.SCALE:
                    top = paddingTop + lp.topMargin + mTargetOffset;
                    break;
                default:
                    // classic
                    top = paddingTop + lp.topMargin + mTargetOffset;
                    break;
            }
            int right = left + targetView.getMeasuredWidth();
            int bottom = top + targetView.getMeasuredHeight();

            targetView.layout(left, top, right, bottom);
        }

        if (mFooterView != null) {
            final View footerView = mFooterView;
            MarginLayoutParams lp = (MarginLayoutParams) footerView.getLayoutParams();
            int left = paddingLeft + lp.leftMargin;
            int bottom;
            switch (mStyle) {
                case STYLE.CLASSIC:
                    bottom = height - paddingBottom - lp.bottomMargin + mFooterHeight + mFooterOffset;
                    break;
                case STYLE.ABOVE:
                    bottom = height - paddingBottom - lp.bottomMargin + mFooterHeight + mFooterOffset;
                    break;
                case STYLE.BLOW:
                    bottom = height - paddingBottom - lp.bottomMargin;
                    break;
                case STYLE.SCALE:
                    bottom = height - paddingBottom - lp.bottomMargin + mFooterHeight / 2 + mFooterOffset / 2;
                    break;
                default:
                    // classic
                    bottom = height - paddingBottom - lp.bottomMargin + mFooterHeight + mFooterOffset;
                    break;
            }

            int top = bottom - footerView.getMeasuredHeight();
            int right = left + footerView.getMeasuredWidth();

            footerView.layout(left, top, right, bottom);
        }

        if (mStyle == STYLE.CLASSIC
                || mStyle == STYLE.ABOVE) {
            if (mHeaderView != null) {
                mHeaderView.bringToFront();
            }
            if (mFooterView != null) {
                mFooterView.bringToFront();
            }
        } else if (mStyle == STYLE.BLOW || mStyle == STYLE.SCALE) {
            if (mTargetView != null) {
                mTargetView.bringToFront();
            }
        }
    }

    /**
     * TODO add gravity
     * LayoutParams of RefreshLoadMoreLayout
     */
    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new SwipeRefreshLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new SwipeRefreshLayout.LayoutParams(p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new SwipeRefreshLayout.LayoutParams(getContext(), attrs);
    }

    /**
     * 多点触控时，非最后一个点UP，需要进行当前活跃的PointerId切换
     *
     * @param ev
     */
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    private float getMotionEventY(MotionEvent event, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(event, activePointerId);
        if (index < 0) {
            return INVALID_COORDINATE;
        }
        return MotionEventCompat.getY(event, index);
    }

    private float getMotionEventX(MotionEvent event, int activePointId) {
        final int index = MotionEventCompat.findPointerIndex(event, activePointId);
        if (index < 0) {
            return INVALID_COORDINATE;
        }
        return MotionEventCompat.getX(event, index);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onActionUp();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //获取action需要使用getActionMasked不能使用ev.getAction()，否则多点触控时有bug(第二个手指按下后再抬起，这时第一个手指滑动失效)
        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mLastX = mInitX = getMotionEventX(ev, mActivePointerId);
                mLastY = mInitY = getMotionEventY(ev, mActivePointerId);
                /**
                 * 在SWIPING_TO_REFRESH、SWIPING_TO_LOAD_MORE、RELEASE_TO_REFRESH和RELEASE_TO_LOAD_MORE四种状态释放后，
                 * 手指再次按下，需停止滚动
                 */
                if (STATUS.isSwipingToRefresh(mStatus) || STATUS.isSwipingToLoadMore(mStatus) ||
                        STATUS.isReleaseToRefresh(mStatus) || STATUS.isReleaseToLoadMore(mStatus)) {
                    mAutoScroller.abortIfRunning();
                }
                //注意此处不能与上面的合并，因为abortIfRunning有可能改变mStatus
                if (STATUS.isSwipingToRefresh(mStatus) || STATUS.isReleaseToRefresh(mStatus)
                        || STATUS.isSwipingToLoadMore(mStatus) || STATUS.isReleaseToLoadMore(mStatus)) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }

                float x = getMotionEventX(ev, mActivePointerId);
                float y = getMotionEventY(ev, mActivePointerId);
                float deltaX = x - mInitX;
                float deltaY = y - mInitY;
                mLastX = x;
                mLastY = y;

                //是否竖直滑动
                boolean isMove = Math.abs(deltaX) < Math.abs(deltaY) && Math.abs(deltaY) > mTouchSlop;
                //是否能下拉刷新
                boolean isCanRefresh = deltaY > 0 && isMove && onCheckCanRefresh();
                //是否能上拉加载
                boolean isCanLoadMore = deltaY < 0 && isMove && onCheckCanLoadMore();

                if (isCanRefresh || isCanLoadMore) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取action需要使用getActionMasked不能使用ev.getAction()，否则多点触控时有bug(第二个手指按下后再抬起，这时第一个手指滑动失效)
        final int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                //注意：此处徐返回true，否则在onInterceptTouchEvent中处理的功能失效(在SWIPING_TO_REFRESH、SWIPING_TO_LOAD_MORE、
                // RELEASE_TO_REFRESH和RELEASE_TO_LOAD_MORE四种状态释放后，手指再次按下，需停止滚动)
                return true;
            case MotionEvent.ACTION_MOVE:
                Log.d("xiao1", "test5");
                float x = getMotionEventX(event, mActivePointerId);
                float y = getMotionEventY(event, mActivePointerId);
                float deltaX = x - mLastX;
                float deltaY = y - mLastY;
                mLastX = x;
                mLastY = y;

                if (Math.abs(deltaX) >= Math.abs(deltaY) && Math.abs(deltaX) > mTouchSlop) {
                    return false;
                }

                if (STATUS.isStatusDefault(mStatus)) {
                    if (deltaY > 0 && onCheckCanRefresh()) {//从Default开始下拉
                        mRefreshCallback.onPrepare();
                        setStatus(STATUS.STATUS_SWIPING_TO_REFRESH);
                    } else if (deltaY < 0 && onCheckCanLoadMore()) {//从Default开始上拉
                        mLoadMoreCallback.onPrepare();
                        setStatus(STATUS.STATUS_SWIPING_TO_LOAD_MORE);
                    }
                } else if (STATUS.isRefreshStatus(mStatus)) {
                    if (mTargetOffset <= 0) {
                        //header可见且向上滑动时，target已经滑动到顶部，则reset
                        setStatus(STATUS.STATUS_DEFAULT);
                        fixCurrentStatusLayout();
                        return false;
                    }
                } else if (STATUS.isLoadMoreStatus(mStatus)) {
                    if (mTargetOffset >= 0) {
                        //footer可见且向下滑动时，target已经滑动到底部时，则reset
                        setStatus(STATUS.STATUS_DEFAULT);
                        fixCurrentStatusLayout();
                        return false;
                    }
                }

                if (STATUS.isRefreshStatus(mStatus)) {
                    if (STATUS.isSwipingToRefresh(mStatus) || STATUS.isReleaseToRefresh(mStatus)) {
                        if (mTargetOffset >= mRefreshTriggerOffset) {
                            setStatus(STATUS.STATUS_RELEASE_TO_REFRESH);
                        } else {
                            setStatus(STATUS.STATUS_SWIPING_TO_REFRESH);
                        }

                        fingerScroll(deltaY);
                    }
                } else if (STATUS.isLoadMoreStatus(mStatus)) {
                    if (STATUS.isSwipingToLoadMore(mStatus) || STATUS.isReleaseToLoadMore(mStatus)) {
                        if (-mTargetOffset >= mLoadMoreTriggerOffset) {
                            setStatus(STATUS.STATUS_RELEASE_TO_LOAD_MORE);
                        } else {
                            setStatus(STATUS.STATUS_SWIPING_TO_LOAD_MORE);
                        }

                        fingerScroll(deltaY);
                    }
                }
                return true;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(event);
                final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);
                //第二个手指按下时，更新mActivePointerId、mLastX、mLastY
                if (pointerId != INVALID_POINTER) {
                    mActivePointerId = pointerId;
                }
                mLastX = getMotionEventX(event, mActivePointerId);
                mLastY = getMotionEventY(event, mActivePointerId);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                //非第一个手指up时，更新mActivePointerId、mLastX、mLastY
                onSecondaryPointerUp(event);
                mLastX = getMotionEventX(event, mActivePointerId);
                mLastY = getMotionEventY(event, mActivePointerId);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return super.onTouchEvent(event);
    }

    private void fixCurrentStatusLayout() {
        if (STATUS.isRefreshing(mStatus)) {
            mTargetOffset = (int) (mRefreshTriggerOffset + 0.5f);
            mHeaderOffset = mTargetOffset;
            mFooterOffset = 0;
            layoutChildren();
            invalidate();
        } else if (STATUS.isStatusDefault(mStatus)) {
            //回复到default状态
            mTargetOffset = 0;
            mHeaderOffset = 0;
            mFooterOffset = 0;
            layoutChildren();
            invalidate();
        } else if (STATUS.isLoadingMore(mStatus)) {
            mTargetOffset = -(int) (mLoadMoreTriggerOffset + 0.5f);
            mHeaderOffset = 0;
            mFooterOffset = mTargetOffset;
            layoutChildren();
            invalidate();
        }
    }

    private void fingerScroll(final float yDiff) {
        float yScroll = yDiff * mDragRatio;

        /**
         * 处理如下两种场景下的滑动值
         * header可见且向上滑动时，target滑动到顶部场景的处理;footer可见且向下滑动时，target滑动到底部场景的处理
         */
        float tmpTargetOffset = mTargetOffset + yScroll;
        if ((mTargetOffset > 0 && tmpTargetOffset < 0) || (mTargetOffset < 0 && tmpTargetOffset > 0)) {
            yScroll = -mTargetOffset;
        }

        /**
         * 处理下拉或上拉超过最大距离（如果设置了最大DragOffset）
         */
        if (mRefreshFinalDragOffset > mRefreshTriggerOffset && tmpTargetOffset > mRefreshFinalDragOffset) {
            yScroll = mRefreshFinalDragOffset - mTargetOffset;
        } else if (mLoadMoreFinalDragOffset > mLoadMoreTriggerOffset && -tmpTargetOffset > mLoadMoreTriggerOffset) {
            yScroll = -mLoadMoreFinalDragOffset - mTargetOffset;
        }

        if (STATUS.isRefreshStatus(mStatus)) {
            mRefreshCallback.onMove(mTargetOffset, false, false);
        } else if (STATUS.isLoadMoreStatus(mStatus)) {
            mLoadMoreCallback.onMove(mTargetOffset, false, false);
        }

        updateScroll(yScroll);
    }

    /**
     * 更新滚动，计算mTargetOffset、mHeaderOffset、mFooterOffset的偏移值
     *
     * @param yScroll
     */
    private void updateScroll(final float yScroll) {
        if (yScroll == 0) {
            return;
        }

        mTargetOffset += yScroll;

        if (STATUS.isRefreshStatus(mStatus)) {
            mHeaderOffset = mTargetOffset;
            mFooterOffset = 0;
        } else if (STATUS.isLoadMoreStatus(mStatus)) {
            mFooterOffset = mTargetOffset;
            mHeaderOffset = 0;
        }

        layoutChildren();
        invalidate();
    }

    /**
     * on active finger up
     * 处理手指up状态
     */
    private void onActionUp() {
        if (STATUS.isSwipingToRefresh(mStatus)) {//在Default到SWIPING_TO_REFRESH的过程中
            scrollSwipingToRefreshToDefault();
        } else if (STATUS.isSwipingToLoadMore(mStatus)) {//在Default到SWIPING_TO_LOAD_MORE的过程中
            scrollSwipingToLoadMoreToDefault();
        } else if (STATUS.isReleaseToRefresh(mStatus)) {//在刷新释放过程中
            mRefreshCallback.onRelease();
            scrollReleaseToRefreshToRefreshing();
        } else if (STATUS.isReleaseToLoadMore(mStatus)) {//在加载更多释放过程中
            mLoadMoreCallback.onRelease();
            scrollReleaseToLoadMoreToLoadingMore();
        }
    }

    @Override
    public void scrollBy(float yScrolled) {
        if (STATUS.isSwipingToRefresh(mStatus)) {
            mRefreshCallback.onMove(mTargetOffset, false, true);
        } else if (STATUS.isReleaseToRefresh(mStatus)) {
            mRefreshCallback.onMove(mTargetOffset, false, true);
        } else if (STATUS.isRefreshing(mStatus)) {
            mRefreshCallback.onMove(mTargetOffset, true, true);
        } else if (STATUS.isSwipingToLoadMore(mStatus)) {
            mLoadMoreCallback.onMove(mTargetOffset, false, true);
        } else if (STATUS.isReleaseToLoadMore(mStatus)) {
            mLoadMoreCallback.onMove(mTargetOffset, false, true);
        } else if (STATUS.isLoadingMore(mStatus)) {
            mLoadMoreCallback.onMove(mTargetOffset, true, true);
        }

        updateScroll(yScrolled);
    }

    @Override
    public void scrollFinished() {
        if (STATUS.isReleaseToRefresh(mStatus)) {
            setStatus(STATUS.STATUS_REFRESHING);
            fixCurrentStatusLayout();
            mRefreshCallback.onRefresh();
        } else if (STATUS.isRefreshing(mStatus)) {
            setStatus(STATUS.STATUS_DEFAULT);
            fixCurrentStatusLayout();
            mRefreshCallback.onReset();
        } else if (STATUS.isSwipingToRefresh(mStatus)) {
            if (mAutoLoading) {
                mAutoLoading = false;
                setStatus(STATUS.STATUS_REFRESHING);
                fixCurrentStatusLayout();
                mRefreshCallback.onRefresh();
            } else {
                setStatus(STATUS.STATUS_DEFAULT);
                fixCurrentStatusLayout();
                mRefreshCallback.onReset();
            }
        } else if (STATUS.isStatusDefault(mStatus)) {

        } else if (STATUS.isSwipingToLoadMore(mStatus)) {
            if (mAutoLoading) {
                mAutoLoading = false;
                setStatus(STATUS.STATUS_LOADING_MORE);
                fixCurrentStatusLayout();
                mLoadMoreCallback.onLoadMore();
            } else {
                setStatus(STATUS.STATUS_DEFAULT);
                fixCurrentStatusLayout();
                mLoadMoreCallback.onReset();
            }
        } else if (STATUS.isLoadingMore(mStatus)) {
            setStatus(STATUS.STATUS_DEFAULT);
            fixCurrentStatusLayout();
            mLoadMoreCallback.onReset();
        } else if (STATUS.isReleaseToLoadMore(mStatus)) {
            setStatus(STATUS.STATUS_LOADING_MORE);
            fixCurrentStatusLayout();
            mLoadMoreCallback.onLoadMore();
        }
    }

    private void scrollDefaultToRefreshing() {
        mAutoScroller.autoScroll((int) (mRefreshTriggerOffset + 0.5f), mDefaultToRefreshingScrollingDuration);
    }

    private void scrollDefaultToLoadingMore() {
        mAutoScroller.autoScroll(-(int) (mLoadMoreTriggerOffset + 0.5f), mDefaultToLoadingMoreScrollingDuration);
    }

    private void scrollSwipingToRefreshToDefault() {
        mAutoScroller.autoScroll(-mHeaderOffset, mSwipingToRefreshToDefaultScrollingDuration);
    }

    private void scrollSwipingToLoadMoreToDefault() {
        mAutoScroller.autoScroll(-mFooterOffset, mSwipingToLoadMoreToDefaultScrollingDuration);
    }

    private void scrollReleaseToRefreshToRefreshing() {
        mAutoScroller.autoScroll(mHeaderHeight - mHeaderOffset, mReleaseToRefreshToRefreshingScrollingDuration);
    }

    private void scrollReleaseToLoadMoreToLoadingMore() {
        mAutoScroller.autoScroll(-mFooterOffset - mFooterHeight, mReleaseToLoadMoreToLoadingMoreScrollingDuration);
    }

    private void scrollRefreshingToDefault() {
        mAutoScroller.autoScroll(-mHeaderOffset, mRefreshCompleteToDefaultScrollingDuration);
    }

    private void scrollLoadingMoreToDefault() {
        mAutoScroller.autoScroll(-mFooterOffset, mLoadMoreCompleteToDefaultScrollingDuration);
    }

    /**
     * check if it can refresh
     *
     * @return
     */
    private boolean onCheckCanRefresh() {
        return mRefreshEnabled && !ViewUtils.canChildScrollUp(mTargetView) && mHasHeaderView && mRefreshTriggerOffset > 0;
    }

    /**
     * check if it can load more
     *
     * @return
     */
    private boolean onCheckCanLoadMore() {
        return mLoadMoreEnabled && !ViewUtils.canChildScrollDown(mTargetView) && mHasFooterView && mLoadMoreTriggerOffset > 0;
    }

    RefreshCallback mRefreshCallback = new RefreshCallback() {
        @Override
        public void onPrepare() {
            if (mHeaderView != null && mHeaderView instanceof SwipeTrigger && STATUS.isStatusDefault(mStatus)) {
                mHeaderView.setVisibility(VISIBLE);
                ((SwipeTrigger) mHeaderView).onPrepare();
            }
        }

        @Override
        public void onMove(int y, boolean isComplete, boolean automatic) {
            if (mHeaderView != null && mHeaderView instanceof SwipeTrigger && STATUS.isRefreshStatus(mStatus)) {
                if (mHeaderView.getVisibility() != VISIBLE) {
                    mHeaderView.setVisibility(VISIBLE);
                }
                ((SwipeTrigger) mHeaderView).onMove(y, isComplete, automatic);
            }
        }

        @Override
        public void onRelease() {
            if (mHeaderView != null && mHeaderView instanceof SwipeTrigger && STATUS.isReleaseToRefresh(mStatus)) {
                ((SwipeTrigger) mHeaderView).onRelease();
            }
        }

        @Override
        public void onRefresh() {
            if (mHeaderView != null && STATUS.isRefreshing(mStatus)) {
                if (mHeaderView instanceof SwipeRefreshTrigger) {
                    ((SwipeRefreshTrigger) mHeaderView).onRefresh();
                }
                if (mRefreshListener != null) {
                    mRefreshListener.onRefresh();
                }
            }
        }

        @Override
        public void onComplete() {
            if (mHeaderView != null && mHeaderView instanceof SwipeTrigger) {
                ((SwipeTrigger) mHeaderView).onComplete();
            }
        }

        @Override
        public void onReset() {
            if (mHeaderView != null && mHeaderView instanceof SwipeTrigger && STATUS.isStatusDefault(mStatus)) {
                ((SwipeTrigger) mHeaderView).onReset();
                mHeaderView.setVisibility(GONE);
            }
        }
    };

    LoadMoreCallback mLoadMoreCallback = new LoadMoreCallback() {

        @Override
        public void onPrepare() {
            if (mFooterView != null && mFooterView instanceof SwipeTrigger && STATUS.isStatusDefault(mStatus)) {
                mFooterView.setVisibility(VISIBLE);
                ((SwipeTrigger) mFooterView).onPrepare();
            }
        }

        @Override
        public void onMove(int y, boolean isComplete, boolean automatic) {
            if (mFooterView != null && mFooterView instanceof SwipeTrigger && STATUS.isLoadMoreStatus(mStatus)) {
                if (mFooterView.getVisibility() != VISIBLE) {
                    mFooterView.setVisibility(VISIBLE);
                }
                ((SwipeTrigger) mFooterView).onMove(y, isComplete, automatic);
            }
        }

        @Override
        public void onRelease() {
            if (mFooterView != null && mFooterView instanceof SwipeTrigger && STATUS.isReleaseToLoadMore(mStatus)) {
                ((SwipeTrigger) mFooterView).onRelease();
            }
        }

        @Override
        public void onLoadMore() {
            if (mFooterView != null && STATUS.isLoadingMore(mStatus)) {
                if (mFooterView instanceof SwipeLoadMoreTrigger) {
                    ((SwipeLoadMoreTrigger) mFooterView).onLoadMore();
                }
                if (mLoadMoreListener != null) {
                    mLoadMoreListener.onLoadMore();
                }
            }
        }

        @Override
        public void onComplete() {
            if (mFooterView != null && mFooterView instanceof SwipeTrigger) {
                ((SwipeTrigger) mFooterView).onComplete();
            }
        }

        @Override
        public void onReset() {
            if (mFooterView != null && mFooterView instanceof SwipeTrigger && STATUS.isStatusDefault(mStatus)) {
                ((SwipeTrigger) mFooterView).onReset();
                mFooterView.setVisibility(GONE);
            }
        }
    };

    /**
     * refresh event callback
     */
    abstract class RefreshCallback implements SwipeTrigger, SwipeRefreshTrigger {
    }

    /**
     * load more event callback
     */
    abstract class LoadMoreCallback implements SwipeTrigger, SwipeLoadMoreTrigger {
    }

    /**
     * 设置当前状态
     *
     * @param status
     */
    private void setStatus(int status) {
        mStatus = status;
    }

    /**
     * 滑动状态
     */
    private final static class STATUS {
        private static final int STATUS_REFRESH_RETURNING = -4;
        private static final int STATUS_REFRESHING = -3;
        private static final int STATUS_RELEASE_TO_REFRESH = -2;
        private static final int STATUS_SWIPING_TO_REFRESH = -1;
        private static final int STATUS_DEFAULT = 0;
        private static final int STATUS_SWIPING_TO_LOAD_MORE = 1;
        private static final int STATUS_RELEASE_TO_LOAD_MORE = 2;
        private static final int STATUS_LOADING_MORE = 3;
        private static final int STATUS_LOAD_MORE_RETURNING = 4;

        private static boolean isRefreshing(final int status) {
            return status == STATUS.STATUS_REFRESHING;
        }

        private static boolean isLoadingMore(final int status) {
            return status == STATUS.STATUS_LOADING_MORE;
        }

        private static boolean isReleaseToRefresh(final int status) {
            return status == STATUS.STATUS_RELEASE_TO_REFRESH;
        }

        private static boolean isReleaseToLoadMore(final int status) {
            return status == STATUS.STATUS_RELEASE_TO_LOAD_MORE;
        }

        private static boolean isSwipingToRefresh(final int status) {
            return status == STATUS.STATUS_SWIPING_TO_REFRESH;
        }

        private static boolean isSwipingToLoadMore(final int status) {
            return status == STATUS.STATUS_SWIPING_TO_LOAD_MORE;
        }

        private static boolean isRefreshStatus(final int status) {
            return status < STATUS.STATUS_DEFAULT;
        }

        public static boolean isLoadMoreStatus(final int status) {
            return status > STATUS.STATUS_DEFAULT;
        }

        private static boolean isStatusDefault(final int status) {
            return status == STATUS.STATUS_DEFAULT;
        }

        private static String getStatus(int status) {
            final String statusInfo;
            switch (status) {
                case STATUS_REFRESH_RETURNING:
                    statusInfo = "status_refresh_returning";
                    break;
                case STATUS_REFRESHING:
                    statusInfo = "status_refreshing";
                    break;
                case STATUS_RELEASE_TO_REFRESH:
                    statusInfo = "status_release_to_refresh";
                    break;
                case STATUS_SWIPING_TO_REFRESH:
                    statusInfo = "status_swiping_to_refresh";
                    break;
                case STATUS_DEFAULT:
                    statusInfo = "status_default";
                    break;
                case STATUS_SWIPING_TO_LOAD_MORE:
                    statusInfo = "status_swiping_to_load_more";
                    break;
                case STATUS_RELEASE_TO_LOAD_MORE:
                    statusInfo = "status_release_to_load_more";
                    break;
                case STATUS_LOADING_MORE:
                    statusInfo = "status_loading_more";
                    break;
                case STATUS_LOAD_MORE_RETURNING:
                    statusInfo = "status_load_more_returning";
                    break;
                default:
                    statusInfo = "status_illegal!";
                    break;
            }
            return statusInfo;
        }
    }
}
