package com.zibuyuqing.ucbrowser.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.base.BaseNewsFragment;

import butterknife.BindView;

/**
 * Created by Xijun.Wang on 2017/11/29.
 */

public class NewsListFragment extends BaseNewsFragment {
    private static final String TAG = "NewsListFragment";
    @BindView(R.id.tvNewsTitle)
    TextView mNewsTitle;
    @Override
    protected int layoutId() {
        return R.layout.fragment_news_list;
    }

    @Override
    protected void init() {
        Bundle bundle = getArguments();
        if(bundle != null){
            String title = bundle.getString("title");
            Log.e(TAG,"title = :" + title);
            if(!TextUtils.isEmpty(title)){
                mNewsTitle.setText(title);
            }
        }
    }
}
