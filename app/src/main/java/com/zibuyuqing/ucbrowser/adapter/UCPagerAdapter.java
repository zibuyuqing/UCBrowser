package com.zibuyuqing.ucbrowser.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.widget.stackview.StackAdapter;
import com.zibuyuqing.ucbrowser.widget.stackview.UCStackView;

/**
 * Created by Xijun.Wang on 2017/12/5.
 */

public class UCPagerAdapter extends StackAdapter<Integer> {

    public UCPagerAdapter(Context context) {
        super(context);
    }
    @Override
    public void bindView(Integer data, int position, UCStackView.ViewHolder holder) {
        PagerViewHolder pagerViewHolder = (PagerViewHolder) holder;
        pagerViewHolder.bind(data,position);
    }

    @Override
    protected UCStackView.ViewHolder onCreateView(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_uc_pager,parent,false);
        return new PagerViewHolder(view);
    }
    class PagerViewHolder extends UCStackView.ViewHolder{
        ImageView ivPagePreview;
        TextView tvPosition;
        public PagerViewHolder(View view) {
            super(view);
            ivPagePreview = (ImageView) view.findViewById(R.id.ivPagePreview);
            tvPosition = (TextView)view.findViewById(R.id.tvPagerUC);
        }
        public void bind(Integer data,int position){
            ivPagePreview.setImageResource(data);
            tvPosition.setText(position +"");
        }
    }
}
