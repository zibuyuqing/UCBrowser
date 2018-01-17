package com.zibuyuqing.ucbrowser.widget.xrecyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zibuyuqing.ucbrowser.R;

/**
 * Created by Xijun.Wang on 2017/8/2.
 */

public class LoadingMoreFooter extends LinearLayout {
    public final static int STATE_LOADING = 0;
    public final static int STATE_COMPLETE = 1;
    public final static int STATE_NOMORE = 2;
    private LinearLayout mContainer;
    private TextView mLoadingStatus;
    private ProgressBar mLoadingProgress;
    public LoadingMoreFooter(Context context) {
        this(context,null);
    }

    public LoadingMoreFooter(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LoadingMoreFooter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    void initView(){
        mContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(
                R.layout.layout_loading_more_footer, null);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(mContainer,lp);
        mLoadingStatus = (TextView)mContainer.findViewById(R.id.tv_loading_more_message);
        mLoadingProgress = (ProgressBar)mContainer.findViewById(R.id.pg_loading_more);
    }
    public void setState(int state){
        switch (state){
            case STATE_COMPLETE:
                mLoadingStatus.setText(R.string.loading);
                setVisibility(GONE);
                break;
            case STATE_NOMORE:
                mLoadingProgress.setVisibility(GONE);
                mLoadingStatus.setText(R.string.nomore_loading);
                setVisibility(VISIBLE);
                break;
            case STATE_LOADING:
                mLoadingStatus.setText(R.string.loading);
                mLoadingProgress.setVisibility(VISIBLE);
                setVisibility(VISIBLE);
                break;

        }
    }
}
