package com.tencent.jinjingcao.scrolltabview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 滑动tab的容器
 * Created by jinjingcao on 2017/8/22.
 *
 * @see {http://blog.csdn.net/qiaoidea/article/details/23604529} for A good refreence.
 */

public class ScrollTabViewGroup extends ViewGroup implements OnTouchListener {
    // default padding left and right for tab added program.
    public static int DEFAULT_TAB_PADDING = 10;

    private static final int DEFAULT_LAYOUT_GRAVITY = Gravity.TOP | Gravity.START;

    private static final float DEFAULT_SLASH_DISTANCE = 50.0f;
    private static final float DEFAULT_CLICK_CAUSE_DISTANCE = 44.0f;

    private static final int DEFAULT_DURATION = 400;

    private static final String TAG = "ScrollTabViewGroup";
    private final int mLayoutGravity;

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
        mLayoutGravity = DEFAULT_LAYOUT_GRAVITY;
        this.setOnTouchListener(this);
    }

    public ScrollTabViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
        mLayoutGravity = getAttrLayoutGravity(context, attrs);
        this.setOnTouchListener(this);
    }

    public ScrollTabViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
        mLayoutGravity = getAttrLayoutGravity(context, attrs);
        this.setOnTouchListener(this);
    }


    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    public ScrollTabViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mScroller = new Scroller(context);
        mLayoutGravity = getAttrLayoutGravity(context, attrs);
        this.setOnTouchListener(this);
    }

    private int getAttrLayoutGravity(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ScrollTabViewGroup);
        int ret = ta.getInt(R.styleable.ScrollTabViewGroup_layout_gravity, -1);
        ta.recycle();
        return ret;
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

        final int parentTop = getPaddingTop();// getPaddingTopWithForeground();
        final int parentBottom = b - t - parentTop;

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) childView.getLayoutParams();
                int gravity = mLayoutGravity;//lp.gravity;
                if (gravity == -1) {
                    gravity = DEFAULT_LAYOUT_GRAVITY;
                }

                // 简化模型，暂不考虑margin值
                if (i == 0) {
                    left = (getWidth() - getChildAt(0).getMeasuredWidth()) / 2;
                } else {
                    View prevChildView = getChildAt(i - 1);
                    left = prevChildView.getRight();
                }
                right = left + childView.getMeasuredWidth();


                final int height = childView.getMeasuredHeight();
                int childTop;

                // final int horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
                switch (verticalGravity) {
                    case Gravity.TOP:
                        childTop = parentTop + lp.topMargin;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                                lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = parentBottom - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                }

                childView.layout(left, childTop, right, childTop + height);
            }
        }

        // 页面重绘触发layout时，会按照index重新排版，此时会把scroll位置叠加进去，需要复位scroll到0，否则位置会偏移
        mScroller.startScroll(0, 0, 0, 0, 0);

        onTabChecked(selectedIndex);
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
//        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int cCount = getChildCount();

        MarginLayoutParams cParams = null;

        int desireWidth = 0;
        int desireHeight = 0;
        /*
         * 根据childView计算的出的宽和高，以及设置的margin计算容器的宽和高，主要用于容器是warp_content时
         */
        for (int i = 0; i < cCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                LayoutParams lp = (LayoutParams) childView.getLayoutParams();
                measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                desireWidth += childView.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                desireHeight = Math.max(desireHeight, childView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            }

        }

        desireWidth += getPaddingLeft() + getPaddingRight();
        desireHeight += getPaddingTop() + getPaddingBottom();

        desireWidth = Math.max(desireWidth, getSuggestedMinimumWidth());
        desireHeight = Math.max(desireHeight, getSuggestedMinimumHeight());

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
//        setMeasuredDimension(
//                (widthMode == MeasureSpec.EXACTLY) ? sizeWidth : width,
//                (heightMode == MeasureSpec.EXACTLY) ? sizeHeight : height);

        setMeasuredDimension(resolveSize(desireWidth, widthMeasureSpec), resolveSize(desireHeight, heightMeasureSpec));

//        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
    }

    private void onTabChecked(int selectedIndex) {
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
        postInvalidate();

        mSelectedIndex = toIndex;
        if (mOnSelectedListener != null) {
            mOnSelectedListener.onSelected(getChildAt(toIndex));
        }

        onTabChecked(toIndex);
    }

    private int getScrollDistance(int fromIndex, int toIndex) {
        int ret = 0;

        // 调整为顺序方便计算移动距离
        if (fromIndex > toIndex) {
            int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
        }

//        float firstOffset = getChildAt(0).getWidth() / 2.0f;
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
        if (view.getPaddingLeft() == 0 && view.getPaddingRight() == 0) {
            int padding = (int) (getResources().getDisplayMetrics().density * DEFAULT_TAB_PADDING);
            view.setPadding(padding, view.getPaddingTop(), padding, view.getPaddingBottom());
        }
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
        // used for default child element
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

//            for (int i = 0; i < attrs.getAttributeCount(); i++) {
//                String name = attrs.getAttributeName(i);
//                Object value = attrs.getAttributeValue(i);
//                Log.d(TAG, name + " : " + value + " of type " + value.getClass().getSimpleName());
//            }
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }


    protected interface IOnSelectedListener {
        void onSelected(View v);
    }
}
