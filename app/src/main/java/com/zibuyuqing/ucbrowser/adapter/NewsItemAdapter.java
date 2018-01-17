package com.zibuyuqing.ucbrowser.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.model.bean.news.NewsItem;

import java.util.ArrayList;

/**
 * Created by Xijun.Wang on 2018/1/15.
 */

public class NewsItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<NewsItem> mNewsList;
    private static final int VIEW_TYPE_NEWS = 0;
    public NewsItemAdapter(ArrayList<NewsItem> mFeedItems, Context context) {
        mNewsList = mFeedItems;
        mContext = context;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 卡片效果
        View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recycler_card,
                parent,
                false);
        LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_uc_news_item, (ViewGroup) card, true);
        return new NewsViewHolder(card);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position < mNewsList.size()) {
            NewsItem feedItem = mNewsList.get(position);
            NewsViewHolder newsViewHolder = (NewsViewHolder) holder;
            bindNewsItem(newsViewHolder, (NewsItem) feedItem);
        }
    }
    private void bindNewsItem(NewsViewHolder newsViewHolder,NewsItem newsItem){

        newsViewHolder.title.setText(newsItem.getTitle());
        newsViewHolder.ptime.setText(newsItem.getPtime());
        newsViewHolder.digest.setText(newsItem.getDigest());

        Glide.with(mContext)
                .load(newsItem.getImgsrc())
                .into(newsViewHolder.photo);
    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }
    private static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView title;
        TextView ptime;
        TextView digest;

        NewsViewHolder(View feedCardView) {
            super(feedCardView);
            photo = (ImageView) feedCardView.findViewById(R.id.ivNewsItemPhoto);
            title = (TextView) feedCardView.findViewById(R.id.tvNewsItemTitle);
            ptime = (TextView) feedCardView.findViewById(R.id.tvNewsItemPtime);
            digest = (TextView) feedCardView.findViewById(R.id.tvNewsItemDigest);
        }
    }
}
