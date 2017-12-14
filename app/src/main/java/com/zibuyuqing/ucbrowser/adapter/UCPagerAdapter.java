package com.zibuyuqing.ucbrowser.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.model.bean.UCPager;
import com.zibuyuqing.ucbrowser.widget.stackview.StackAdapter;
import com.zibuyuqing.ucbrowser.widget.stackview.UCPagerView;
import com.zibuyuqing.ucbrowser.widget.stackview.UCStackView;

/**
 * Created by Xijun.Wang on 2017/12/5.
 */

public class UCPagerAdapter extends StackAdapter<UCPager> {
    private UCPagerView.CallBack mCallBack;
    public UCPagerAdapter(Context context, UCPagerView.CallBack callBack) {
        super(context);
        mCallBack = callBack;
    }
    @Override
    public void bindView(UCPager pager, int position, UCStackView.ViewHolder holder) {
        PagerViewHolder pagerViewHolder = (PagerViewHolder) holder;
        pagerViewHolder.bind(pager,position);
    }

    @Override
    protected UCStackView.ViewHolder onCreateView(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_uc_pager,parent,false);
        return new PagerViewHolder(view);
    }
    class PagerViewHolder extends UCStackView.ViewHolder implements View.OnClickListener {
        View content;
        ImageView ivPagePreview,ivWebsiteIcon,ivClose;
        TextView tvPosition;
        UCPager ucPager;
        public PagerViewHolder(View view) {
            super(view);
            content = view;
            ivPagePreview = (ImageView) view.findViewById(R.id.ivPagePreview);
            ivWebsiteIcon = (ImageView) view.findViewById(R.id.ivWebsiteIcon);
            ivClose = (ImageView) view.findViewById(R.id.ivPageClose);
            tvPosition = (TextView)view.findViewById(R.id.tvPagerUC);
        }
        public void bind(UCPager pager,int position){
            int websiteIcon = pager.getWebsiteIcon();
            String title = pager.getTitle();
            Bitmap pagerPreview = pager.getPagerPreview();
            ivWebsiteIcon.setImageResource(websiteIcon);
            ivPagePreview.setImageBitmap(pagerPreview);
            tvPosition.setText(title);
            ivClose.setOnClickListener(this);
            content.setOnClickListener(this);
            ucPager = pager;
        }

        @Override
        public void onClick(View view) {
            if(view == content){
                if(mCallBack != null){
                    mCallBack.onSelect(ucPager.getKey());
                }
            } else if(view == ivClose){
                if(mCallBack != null){
                    mCallBack.onClose(ucPager.getKey());
                }
            }
        }
    }
}
