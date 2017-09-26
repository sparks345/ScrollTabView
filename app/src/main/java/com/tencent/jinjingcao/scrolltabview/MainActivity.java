package com.tencent.jinjingcao.scrolltabview;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.jinjingcao.scrolltabview.ScrollTabViewGroup.IOnSelectedListener;

public class MainActivity extends AppCompatActivity implements IOnSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ScrollTabViewGroup tabScroll = (ScrollTabViewGroup) findViewById(R.id.tab_scroll);
        tabScroll.bindScrollEvent(getRootView(this));

        TextView tv = new TextView(this);
        tv.setText("MMMMM");
        tabScroll.addTab(tv);

        ScrollTabView tv2 = new ScrollTabView(this);
        tv2.setText("XXDFSLON::");

        tabScroll.addTab(tv2);

        ScrollTabView tv3 = new ScrollTabView(this);
        tv3.setText("XXDFSLON333::");
        tabScroll.addTab(tv3);

        tabScroll.setOnTabSelected(this);
    }

    private static View getRootView(Activity context) {
        return ((ViewGroup) context.findViewById(android.R.id.content)).getChildAt(0);
    }

    @Override
    public void onSelected(View v) {
        CharSequence text = v instanceof TextView ? ((TextView) v).getText() : "null";
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}
