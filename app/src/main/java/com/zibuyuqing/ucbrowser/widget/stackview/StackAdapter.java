package com.zibuyuqing.ucbrowser.widget.stackview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xijun.Wang on 2017/12/5.
 */

public abstract class StackAdapter<T> extends UCStackView.Adapter<UCStackView.ViewHolder> {
    protected final Context mContext;
    protected final LayoutInflater mInflater;
    private List<T> mData;
    public StackAdapter(Context context){
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mData = new ArrayList<>();
    }
    public void setData(List<T> data){
        if(data == null){
            return;
        }
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }
    public void updateData(List<T> data){
        setData(data);
    }
    @Override
    protected void onBindViewHolder(UCStackView.ViewHolder holder, int position) {
        T data = mData.get(position);
        bindView(data,position,holder);
    }

    public abstract void bindView(T data, int position, UCStackView.ViewHolder holder);

    @Override
    public int getItemCount() {
        return mData.size();
    }
    public T getItem(int position){
        return mData.get(position);
    }
}
