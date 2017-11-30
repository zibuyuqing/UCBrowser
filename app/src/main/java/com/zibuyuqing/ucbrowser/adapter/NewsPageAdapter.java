package com.zibuyuqing.ucbrowser.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.zibuyuqing.ucbrowser.base.BaseNewsFragment;

import java.util.List;

import static com.zibuyuqing.ucbrowser.utils.Constants.NEWS_TITLE;


/**
 * Created by Xijun.Wang on 2017/11/29.
 */

public class NewsPageAdapter extends FragmentStatePagerAdapter {

    List<BaseNewsFragment> mFragments;
    public NewsPageAdapter(FragmentManager fm,List<BaseNewsFragment> fragments){
        super(fm);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return NEWS_TITLE[position % NEWS_TITLE.length];
    }
}
