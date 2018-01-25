package com.zibuyuqing.ucbrowser.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zibuyuqing.stackview.adapter.StackAdapter;
import com.zibuyuqing.stackview.widget.UCStackView;
import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.web.Tab;
import com.zibuyuqing.ucbrowser.widget.stackview.UCPagerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xijun.Wang on 2018/1/25.
 */

public class UCTabAdapter extends StackAdapter<Tab> {
    private UCPagerView.CallBack mCallBack;
    private int mCurrent;
    private List<Tab> mTabs;
    public UCTabAdapter(Context context, UCPagerView.CallBack callBack) {
        super(context);
        mCallBack = callBack;
        mTabs = new ArrayList<Tab>();
        mCurrent = -1;
    }
    @Override
    public Tab getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }
    public long getItemId(int position){
        return position;
    }
    public void setCurrent(int index){
        mCurrent = index;
    }
    @Override
    public void bindView(Tab tab, int position, UCStackView.ViewHolder holder) {
        TabViewHolder pagerViewHolder = (TabViewHolder) holder;
        pagerViewHolder.bind(tab,position);
    }

    @Override
    protected UCStackView.ViewHolder onCreateView(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_uc_pager,parent,false);
        return new UCTabAdapter.TabViewHolder(view);
    }
    class TabViewHolder extends UCStackView.ViewHolder implements View.OnClickListener {

        View content;
        ImageView ivPagePreview,ivWebsiteIcon,ivClose;
        TextView tvPosition;
        Tab tab;
        int position;

        public TabViewHolder(View view) {
            super(view);
            content = view;
            ivPagePreview = (ImageView) view.findViewById(R.id.ivPagePreview);
            ivWebsiteIcon = (ImageView) view.findViewById(R.id.ivWebsiteIcon);
            ivClose = (ImageView) view.findViewById(R.id.ivPageClose);
            tvPosition = (TextView)view.findViewById(R.id.tvPagerUC);
        }

        public void bind(Tab tab,int position){
            String title = tab.getTitle();
            tvPosition.setText(title);
            Bitmap favicon = tab.getFavicon();
            if (favicon != null) {
                ivWebsiteIcon.setImageBitmap(favicon);
            }
            Bitmap preview = tab.getScreenshot();
            if (preview != null) {
                ivPagePreview.setImageBitmap(preview);
            }
            ivClose.setOnClickListener(this);
            content.setOnClickListener(this);
            this.tab = tab;
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            if(view == content){
                if(mCallBack != null){
                    mCallBack.onSelect(tab.getId());
                }
            } else if(view == ivClose){
                if(mCallBack != null){
                    mCallBack.onClose(tab.getId());
                }
            }
        }
    }
}
