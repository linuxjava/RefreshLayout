package xiao.free.refreshlayoutlib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import xiao.free.refreshlayoutlib.base.SwipeTrigger;
import xiao.free.refreshlayoutlib.base.SwipeRefreshTrigger;

/**
 * Created by robincxiao on 2017/2/21.
 */

public abstract class SwipeRefreshHeaderLayout extends FrameLayout implements SwipeRefreshTrigger, SwipeTrigger {
    public SwipeRefreshHeaderLayout(Context context) {
        super(context);
    }

    public SwipeRefreshHeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeRefreshHeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
