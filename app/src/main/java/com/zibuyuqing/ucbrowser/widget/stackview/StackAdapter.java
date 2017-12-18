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
    public List<T> getData(){
        return mData;
    }
    public void addData(int index,T t,boolean notify){
        if(index < 0 || index > mData.size()){
            return;
        }
        mData.add(index,t);
        if(notify) notifyDataSetChanged();
    }
    public void addData(T t,boolean notify){
        mData.add(t);
        if(notify) notifyDataSetChanged();
    }
    public void removeData(T t,boolean notify){
        if(mData.contains(t)) {
            mData.remove(t);
            if (notify) notifyDataSetChanged();
        }
    }
    public void removeData(int index,boolean notify){
        if(index < 0 || index > mData.size()){
            return;
        }
        mData.remove(index);
        if(notify){
            notifyDataSetChanged();
        }
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
