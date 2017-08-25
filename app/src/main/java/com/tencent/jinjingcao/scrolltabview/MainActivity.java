package com.tencent.jinjingcao.scrolltabview;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ScrollTabViewGroup tabScroll = (ScrollTabViewGroup) findViewById(R.id.tab_scroll);
        tabScroll.bindScrollEvent(getRootView(this));
        TextView tv = new TextView(this);
        tv.setText("MMMMM");
        tabScroll.addTab(tv);
    }

    private static View getRootView(Activity context) {
        return ((ViewGroup) context.findViewById(android.R.id.content)).getChildAt(0);
    }
}
