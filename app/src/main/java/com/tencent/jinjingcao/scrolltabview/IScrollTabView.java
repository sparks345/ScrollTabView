package com.tencent.jinjingcao.scrolltabview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * tab项目需要实现的接口
 * Created by jinjingcao on 2017/8/22.
 */

public interface IScrollTabView {

    void onSelected();

    void onMissSelected();
}
