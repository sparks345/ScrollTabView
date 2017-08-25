package com.tencent.jinjingcao.scrolltabview;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 滑动tab的容器
 * Created by jinjingcao on 2017/8/22.
 */

public class ScrollTabViewGroup extends ViewGroup implements OnTouchListener {

    private static final float DEFAULT_SLASH_DISTANCE = 50.0f;
    private static final float DEFAULT_CLICK_CAUSE_DISTANCE = 44.0f;

    private static final int DEFAULT_DURATION = 400;

    private static final String TAG = "ScrollTabViewGroup";

    float x1 = 0, y1 = 0, x2 = 0, y2 = 0;

    // 当前选中项索引
    private int mSelectedIndex;
    // 上一个选中的view
    private View mPreviousSelectedView;
    // 滚动插件
    private final Scroller mScroller;
    // 滑动动画的播放时间
    private int mDuration = DEFAULT_DURATION;
    // 切换tab时的监听事件
    private IOnSelectedListener mOnSelectedListener;

    public ScrollTabViewGroup(Context context) {
        super(context);
        mScroller = new Scroller(context);
        this.setOnTouchListener(this);
    }

    public ScrollTabViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
        this.setOnTouchListener(this);
    }


    public ScrollTabViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    public ScrollTabViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mScroller = new Scroller(context);
    }

    /**
     * 自己渲染平铺布局
     *
     * @param changed changed
     * @param l       left
     * @param t       top
     * @param r       right
     * @param b       bottom
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int left = 0;
        int right = 0;
        int selectedIndex = mSelectedIndex;// 默认选中的项
        int middlePos = 0;// 中间点

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (i < selectedIndex) {
                middlePos += childView.getMeasuredWidth();
            }
        }

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (i == 0) {
                left = (getWidth() - getChildAt(selectedIndex).getMeasuredWidth()) / 2 - middlePos;
            } else {
                View prevChildView = getChildAt(i - 1);
                left = prevChildView.getRight();
            }
            right = left + childView.getMeasuredWidth();

            childView.layout(left, t, right, b);
        }

        if (mPreviousSelectedView != null) {
            if (mPreviousSelectedView instanceof IScrollTabView) {
                ((IScrollTabView) mPreviousSelectedView).onMissSelected();
            }
        }

        View currentSelectedView = getChildAt(selectedIndex);
        if (currentSelectedView instanceof IScrollTabView) {
            ((IScrollTabView) currentSelectedView).onSelected();
        }
        mPreviousSelectedView = currentSelectedView;
    }

    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
        super.computeScroll();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /*
         * 获得此ViewGroup上级容器为其推荐的宽和高，以及计算模式
         */
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        final boolean isWrapContentWidth = widthMode != MeasureSpec.EXACTLY;
        final boolean isWrapContentHeight = heightMode != MeasureSpec.EXACTLY;

        // 计算出所有的childView的宽和高
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        /*
         * 记录如果是wrap_content是设置的宽和高
         */
        int width = 0;
        int height = 0;

        int cCount = getChildCount();

        int cWidth = 0;
        int cHeight = 0;
        MarginLayoutParams cParams = null;

        // 用于计算左边两个childView的高度
        int lHeight = 0;
        // 用于计算右边两个childView的高度，最终高度取二者之间大值
        int rHeight = 0;

        // 用于计算上边两个childView的宽度
        int tWidth = 0;
        // 用于计算下面两个childiew的宽度，最终宽度取二者之间大值
        int bWidth = 0;

        /*
         * 根据childView计算的出的宽和高，以及设置的margin计算容器的宽和高，主要用于容器是warp_content时
         */
        for (int i = 0; i < cCount; i++) {
            View childView = getChildAt(i);
            cWidth = childView.getMeasuredWidth();
            cHeight = childView.getMeasuredHeight();
            cParams = (MarginLayoutParams) childView.getLayoutParams();

            // 上面两个childView
            if (i == 0 || i == 1) {
                tWidth += cWidth + cParams.leftMargin + cParams.rightMargin;
            }

            if (i == 2 || i == 3) {
                bWidth += cWidth + cParams.leftMargin + cParams.rightMargin;
            }

            if (i == 0 || i == 2) {
                lHeight += cHeight + cParams.topMargin + cParams.bottomMargin;
            }

            if (i == 1 || i == 3) {
                rHeight += cHeight + cParams.topMargin + cParams.bottomMargin;
            }

        }

        width = Math.max(tWidth, bWidth);
        height = Math.max(lHeight, rHeight);

        /*
         * 补加padding值
         */
//        width += (this.getPaddingLeft() + this.getPaddingRight());
//        height += (this.getPaddingTop() + this.getPaddingBottom());
//        sizeHeight += (this.getPaddingTop() + this.getPaddingBottom());

        /*
         * 如果是wrap_content设置为我们计算的值
         * 否则：直接设置为父容器计算的值
         */
        setMeasuredDimension(
                (widthMode == MeasureSpec.EXACTLY) ? sizeWidth : width,
                (heightMode == MeasureSpec.EXACTLY) ? sizeHeight : height
        );

//        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
//        setMeasuredDimension(myWidth, myHeight);
    }


    private void setSelected(int toIndex) {
        Log.d(TAG, "setSelected:" + toIndex);
        if (toIndex >= getChildCount() || toIndex < 0) {
            throw new IndexOutOfBoundsException("total:" + getChildCount() + " and current: " + toIndex);
        }

        if (toIndex == mSelectedIndex) {
            Log.i(TAG, "setSelected. toIndex equal.");
            return;
        }

        int fromIndex = mSelectedIndex;
        int toPos = getScrollDistance(fromIndex, toIndex);
        if (fromIndex > toIndex) {// 向右移动
            toPos = -toPos;
        }
        mScroller.startScroll(getScrollX(), 0, toPos, 0, mDuration);
        invalidate();
        mSelectedIndex = toIndex;
        if (mOnSelectedListener != null) {
            mOnSelectedListener.onSelected(getChildAt(toIndex));
        }
    }

    private int getScrollDistance(int fromIndex, int toIndex) {
        int ret = 0;

        // 调整为顺序方便计算移动距离
        if (fromIndex > toIndex) {
            int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
        }

        for (int i = fromIndex; i < toIndex; i++) {
//            Math.round((getChildAt(fromIndex).getWidth() + getChildAt(toIndex).getWidth()) / 2.0f);
            ret += Math.round((getChildAt(i).getWidth() + getChildAt(i + 1).getWidth()) / 2.0f);
        }
        return ret;
    }

    public void setOnTabSelected(IOnSelectedListener listener) {
        mOnSelectedListener = listener;
    }

    public void addTab(View view) {
//        view.setOnClickListener(this);
        this.addView(view);
        invalidate();
    }


    public void bindScrollEvent(View v) {
        v.setOnTouchListener(this);
    }

    private int getChildIndexByView(View v) {
        int ret = -1;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (v == childView) {
                ret = i;
                break;
            }
        }
        return ret;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.v(TAG, "onTouch:" + event.getAction() + ", p:" + event.getX());

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x1 = event.getX();
            y1 = event.getY();
            Log.d(TAG, "x1:" + x1 + ", y1:" + y1);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            x2 = event.getX();
            y2 = event.getY();
            Log.d(TAG, "x2:" + x2 + ", y2:" + y2);

            // 过滤掉纵向滑动意图更明显的情况
            if (Math.abs(x2 - x1) > DEFAULT_SLASH_DISTANCE) {
                if (Math.abs(y2 - y1) < Math.abs(x2 - x1)) {
                    if (x1 - x2 > DEFAULT_SLASH_DISTANCE) {
                        moveLeft();
                    } else if (x2 - x1 > DEFAULT_SLASH_DISTANCE) {
                        moveRight();
                    }
                } else {
                    return super.onTouchEvent(event);
                }
            } else if (Math.abs(x2 - x1) < DEFAULT_CLICK_CAUSE_DISTANCE) {
                // 获取点中的元素
                int clickedViewIndex = getClickedChildIndex(event.getRawX(), event.getRawY());
                if (clickedViewIndex > -1) {
                    setSelected(clickedViewIndex);
                }
            } else {
                return super.onTouchEvent(event);
            }
        }
        return true;//super.onTouchEvent(event);
    }

    private int getClickedChildIndex(float x2, float y2) {
        int ret = -1;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            Rect rect = new Rect();
            childView.getGlobalVisibleRect(rect);
            if (x2 >= rect.left && x2 <= rect.right
                    && y2 >= rect.top && y2 <= rect.bottom) {
                ret = i;
                break;
            }
        }
        return ret;
    }

    private void moveLeft() {
        if (mSelectedIndex < getChildCount() - 1) {
            setSelected(mSelectedIndex + 1);
        }
    }

    private void moveRight() {
        if (mSelectedIndex > 0) {
            setSelected(mSelectedIndex - 1);
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }


    private interface IOnSelectedListener {
        void onSelected(View v);
    }
}
