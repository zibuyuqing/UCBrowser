package com.zibuyuqing.ucbrowser;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;

import com.zibuyuqing.ucbrowser.ui.fragment.UCPagerAdapter;
import com.zibuyuqing.ucbrowser.widget.stackview.UCPagerView;
import com.zibuyuqing.ucbrowser.widget.stackview.UCStackView;

import java.util.Arrays;

/**
 * Created by Xijun.Wang on 2017/12/4.
 */

public class TestActivity extends Activity {
    private UCStackView ucStackView;
    private UCPagerAdapter adapter;
    public static Integer[] TEST_DATAS = new Integer[]{
            R.drawable.test_uc_screen,
            R.drawable.test_uc_screen,
            R.drawable.test_uc_screen,
            R.drawable.test_uc_screen,
            R.drawable.test_uc_screen,
            R.drawable.test_uc_screen,
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ucStackView = (UCStackView)findViewById(R.id.ucStackView);
        adapter = new UCPagerAdapter(this);
        ucStackView.setAdapter(adapter);

        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        adapter.updateData(Arrays.asList(TEST_DATAS));
                    }
                }
                , 200
        );
    }
}
