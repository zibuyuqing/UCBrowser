package com.zibuyuqing.ucbrowser.widget.xrecyclerview;

/**
 * Created by Xijun.Wang on 2017/8/1.
 */
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class XRecyclerView extends RecyclerView {
    public static final int ITEM_VIEW_TYPE_HEADER = 2;
    public static final int ITEM_VIEW_TYPE_NORMAL = 0;
    public static final int ITEM_VIEW_TYPE_FOOTER = -2;

    private static final int HEADER_INIT_INDEX = 10000;
    private static final float DRAG_RATE = 3;
    private boolean isLoadingData = false;
    private boolean isNoMore = false;
    private float mLastY = -1;

    private ArrowRefreshHeader mHeaderView;
    private LoadingMoreFooter mFooterView;
    private View mEmptyView;
    private LoadingListener mListener;
    private WrapAdapter mWrapAdapter;
    private Context mContext;
    private final AdapterDataObserver mDataObserver = new DataObserver();
    public XRecyclerView(Context context) {
        this(context,null);
    }

    public XRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public XRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }
    private void init(){
        mHeaderView = new ArrowRefreshHeader(mContext);
        mFooterView = new LoadingMoreFooter(mContext);
    }
    public void loadMoreComplete(){
        isLoadingData = false;
        mFooterView.setState(LoadingMoreFooter.STATE_COMPLETE);
    }
    public void setNoMore(boolean noMore){
        isNoMore = noMore;
        mFooterView.setState(LoadingMoreFooter.STATE_NOMORE);
    }
    public void setLoadingListener(LoadingListener listener){
        mListener = listener;
    }
    public void refreshComplete(){
        mHeaderView.onComplete();
    }
    public void reset(){
        loadMoreComplete();
        setNoMore(false);
    }
    @Override
    public void setAdapter(Adapter adapter) {
        mWrapAdapter = new WrapAdapter(adapter);
        super.setAdapter(mWrapAdapter);
        adapter.registerAdapterDataObserver(mDataObserver);
        mDataObserver.onChanged();
    }
    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE && mListener != null && !isLoadingData) {
            LayoutManager layoutManager = getLayoutManager();
            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = findMax(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            if (layoutManager.getChildCount() > 0
                    && lastVisibleItemPosition >= layoutManager.getItemCount() - 3
                    && layoutManager.getItemCount() > layoutManager.getChildCount()
                    && !isNoMore && mHeaderView.getState() < ArrowRefreshHeader.STATE_REFRESHING) {
                isLoadingData = true;
                mFooterView.setState(LoadingMoreFooter.STATE_LOADING);
                mListener.onLoadMore();
            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                    mHeaderView.onDrag(deltaY / DRAG_RATE);
                break;
            default:
                mLastY = -1; // reset
                    if (mHeaderView.onRelease()) {
                        if (mListener != null) {
                            mListener.onRefresh();
                        }
                    }
                break;
        }
        return super.onTouchEvent(ev);
    }
    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
    private boolean isOnTop() {
        return !(mHeaderView == null || mHeaderView.getParent() != null);
    }
    private class DataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            Adapter<?> adapter = getAdapter();
            if (adapter != null && mEmptyView != null) {
                int emptyCount = 2;
                if (adapter.getItemCount() == emptyCount) {
                    mEmptyView.setVisibility(View.VISIBLE);
                    XRecyclerView.this.setVisibility(View.GONE);
                } else {
                    mEmptyView.setVisibility(View.GONE);
                    XRecyclerView.this.setVisibility(View.VISIBLE);
                }
            }
            if (mWrapAdapter != null) {
                mWrapAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mWrapAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    }

    private class WrapAdapter extends Adapter<ViewHolder> {
        private Adapter adapter;
        public WrapAdapter(Adapter adapter){
            this.adapter = adapter;
        }
        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            LayoutManager layoutManager = recyclerView.getLayoutManager();
            if(layoutManager instanceof GridLayoutManager){
                final GridLayoutManager manager = (GridLayoutManager) layoutManager;
                manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (isHeader(position) || isFooter(position) ?
                                manager.getSpanCount() :1);
                    }
                });
            }
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if(lp != null
                    && lp instanceof StaggeredGridLayoutManager.LayoutParams
                    &&(isHeader(holder.getLayoutPosition()) || isFooter(holder.getLayoutPosition()))){
                ((StaggeredGridLayoutManager.LayoutParams) lp).setFullSpan(true);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType){
                case ITEM_VIEW_TYPE_HEADER:
                    return new SimpleViewHolder(mHeaderView);
                case ITEM_VIEW_TYPE_FOOTER:
                    return new SimpleViewHolder(mFooterView);
            }
            return adapter.createViewHolder(parent,viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if(isHeader(position) || isFooter(position)){
                return;
            }
            int adjPosition = position - 1;
            int count;
            if(adapter != null) {
                count = adapter.getItemCount();
                if (adjPosition < count) {
                    adapter.onBindViewHolder(holder,position);
                }
            }
        }

        @Override
        public int getItemCount() {
            if(adapter != null){
                return adapter.getItemCount() + 2;
            } else {
                return 2; // headed and footer
            }
        }

        @Override
        public int getItemViewType(int position) {
            if(isHeader(position)){
                return ITEM_VIEW_TYPE_HEADER;
            }
            if(isFooter(position)){
                return ITEM_VIEW_TYPE_FOOTER;
            }
            int adjPosition = position - 1;
            int count;
            if(adapter != null) {
                count = adapter.getItemCount();
                if (adjPosition < count) {
                    return adapter.getItemViewType(position);
                }
            }
            return ITEM_VIEW_TYPE_NORMAL;
        }

        @Override
        public long getItemId(int position) {
            if(adapter != null && position >= 1){
                int count = getItemCount();
                int adjPosition = position - 1;
                if(adjPosition < count){
                    return adapter.getItemId(position);
                }
            }
            return -1;
        }
        /*
        @Override
        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            if(adapter != null){
                adapter.registerAdapterDataObserver(observer);
            }
        }

        @Override
        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            if(adapter != null){
                adapter.unregisterAdapterDataObserver(observer);
            }
        }
        */
        public boolean isHeader(int position){
            return position == 0;
        }
        public boolean isFooter(int position){
            return position == getItemCount() - 1;
        }
    }
    public boolean hasHeader(){
        return mHeaderView != null;
    }
    public boolean hasFooter(){
        return mFooterView != null;
    }
    private class SimpleViewHolder extends ViewHolder {

        public SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }
    public interface LoadingListener{
        void onRefresh();
        void onLoadMore();
    }
}

