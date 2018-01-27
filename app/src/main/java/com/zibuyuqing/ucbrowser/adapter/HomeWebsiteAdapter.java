package com.zibuyuqing.ucbrowser.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.utils.Constants;
import com.zibuyuqing.ucbrowser.web.UiController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Xijun.Wang on 2018/1/27.
 */

public class HomeWebsiteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    Map<String,String> mData;
    List<String> keys = new ArrayList<>();
    Context mContext;
    UiController mController;
    public HomeWebsiteAdapter(Context context, Map<String,String> data, UiController controller){
        mContext = context;
        mData = data;
        keys.addAll(mData.keySet());
        mController = controller;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WebsiteViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_uc_website_item,null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        WebsiteViewHolder viewHolder = (WebsiteViewHolder) holder;
        final String website = keys.get(position);
        viewHolder.website.setText(website);
        Drawable icon = mContext.getDrawable(Constants.WEBSITE_ICON_RES[position]);
        icon.setBounds(new Rect(0,0,icon.getIntrinsicWidth(),icon.getIntrinsicHeight()));
        viewHolder.website.setCompoundDrawables(null,icon,null,null);
        viewHolder.website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.onWebsiteIconClicked(mData.get(website));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
    private static class WebsiteViewHolder extends RecyclerView.ViewHolder {
        TextView website;
        WebsiteViewHolder(View view) {
            super(view);
            website = (TextView) view.findViewById(R.id.tvHomeWebsiteItem);
        }
    }
}
