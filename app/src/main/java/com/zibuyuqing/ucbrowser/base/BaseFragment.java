package com.zibuyuqing.ucbrowser.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Xijun.Wang on 2017/11/29.
 */

public abstract class BaseFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(layoutId(),container,false);
        ButterKnife.bind(this,view);
        init();
        return view;
    }
    protected abstract int layoutId();
    protected abstract void init();
    public void showTips(String msg){
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
