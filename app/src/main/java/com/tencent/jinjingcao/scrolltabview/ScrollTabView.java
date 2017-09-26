package com.tencent.jinjingcao.scrolltabview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * 滑动组件tab
 * Created by jinjingcao on 2017/9/26.
 */

public class ScrollTabView extends android.support.v7.widget.AppCompatTextView implements IScrollTabView {

    public ScrollTabView(Context context) {
        super(context);
        this.onMissSelected();
    }

    public ScrollTabView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.onMissSelected();
    }

    public ScrollTabView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.onMissSelected();
    }


    @Override
    public void onSelected() {
        this.setTextColor(getResources().getColor(R.color.colorAccent));
    }

    @Override
    public void onMissSelected() {
        this.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
    }


}
