package com.zibuyuqing.ucbrowser.widget.favorite;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.zibuyuqing.common.utils.ViewUtil;
import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.model.bean.favorite.FavoriteFolderInfo;
import com.zibuyuqing.ucbrowser.model.bean.favorite.FavoriteShortcutInfo;
import com.zibuyuqing.ucbrowser.model.bean.favorite.ItemInfo;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xijun.wang on 2017/12/23.
 */

public class FavoriteWorkspace extends LinearLayout implements DragSource,DropTarget, View.OnClickListener, View.OnLongClickListener, View.OnFocusChangeListener, View.OnKeyListener {
    private static final String TAG ="FavoriteWorkspace";
    public static final int DRAG_BITMAP_PADDING = 2;
    private static final int REORDER_DELAY = 250;
    private static final float ICON_OVERSCROLL_WIDTH_FACTOR = 0.45f;
    private static final int SCROLL_VELOCITY = 10;
    public ScrollView mContentWrapper;
    public CellLayout mContent;
    private ArrayList<ItemInfo> mInfos = new ArrayList<>();
    private final ArrayList<View> mItemsInReadingOrder = new ArrayList<View>();
    private static final Rect sTempRect = new Rect();
    private final int[] mTempXY = new int[2];
    private DragController mDragController;
    private DragLayer mDragLayer;
    private ItemInfo mCurrentDragInfo;
    private View mCurrentDragView;
    private Canvas mCanvas = new Canvas();
    private int mTargetRank, mPrevTargetRank, mEmptyCellRank;
    private final Alarm mReorderAlarm = new Alarm();
    private Resources mResources;
    private Context mContext;
    private LayoutInflater mInflater;
    private static final int[] sTempPosArray = new int[2];
    private int mVisualAreaWidth;
    private int mVisualAreaHeight;
    private int mContentAreaWidth;
    private int mContentAreaHeight;
    private int mScreenWidth;
    private int mScreenHeight;
    private Bitmap mDragOutline = null;
    private OnItemClickListener mListener;
    public FavoriteWorkspace(Context context) {
        this(context,null);
    }

    public FavoriteWorkspace(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FavoriteWorkspace(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }
    public void bindItems(ArrayList<? extends ItemInfo> infos){
        mInfos.addAll(infos);
        ArrayList<View> icons = new ArrayList<View>();
        for(ItemInfo item : infos){
            View icon = createNewView(item);
            icon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mListener != null) {
                        mListener.onItemClick(view);
                    }
                }
            });
            icons.add(icon);
        }
        Log.e(TAG,"bindItems :: infos = " + infos.size() +",icons =:" + icons.size());
        arrangeChildren(icons, icons.size());
    }

    private void arrangeChildren(ArrayList<View> icons, int itemCount) {
        int position = 0;
        int newX,newY,rank;
        rank = 0;
        int countX = mContent.getCountX();
        mContent.resetContentDimensions(itemCount);
        for(int i = 0;i < itemCount ; i++){
            View v = icons.size() > i ? icons.get(i) : null;
            if(v != null){
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v.getLayoutParams();
                newX = position % countX;
                newY = position / countX;
                ItemInfo info = (ItemInfo) v.getTag();
                if(info.cellX != newX || info.cellY != newY || info.rank != rank){
                    info.cellX = newX;
                    info.cellY = newY;
                    info.rank = rank;
                }
                lp.cellX = info.cellX;
                lp.cellY = info.cellY;
                Log.e(TAG,"bindItems :: info = " + info +" \n lp =:" + lp);
                mContent.addViewToCellLayout(v,-1,lp,true);
            }
            rank ++;
            position ++;
        }
        requestLayout();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mContentWrapper = (ScrollView)findViewById(R.id.contentWrapper);
        mContent = (CellLayout)findViewById(R.id.content);
        Log.e(TAG," FavoriteWorkspace onSizeChanged ....................... mContent =：" + mContent+",mContentWrapper =:" + mContentWrapper);
    }

    private void init() {
        mContext = getContext();
        mResources = mContext.getResources();
        mInflater = LayoutInflater.from(mContext);
        mScreenWidth = ViewUtil.getScreenSize(mContext).x;
        mScreenHeight = ViewUtil.getScreenSize(mContext).y;
        mVisualAreaWidth = mScreenWidth;
        mVisualAreaHeight = mScreenHeight -
                ViewUtil.getNavBarHeight(mContext) - mResources.getDimensionPixelSize(R.dimen.dimen_112dp);
        mContentAreaWidth = mScreenWidth;
        mContentAreaHeight = mScreenHeight;
    }

    public void setup(DragLayer dragLayer){
        Log.e(TAG,"setup ::------------ ");
        mDragLayer = dragLayer;
        mDragController = dragLayer.getDragController();
        int countX = 4;
        int countY = 4;
        mContent.setGridSize(countX,countY);
        int cellWidth = ViewUtil.getScreenSize(mContext).x / 4;
        int cellHeight = (int) (5 * cellWidth * 0.25f);
        mContent.setCellDimensions(cellWidth,cellHeight);
    }
    public DragLayer getDragLayer(){
        return mDragLayer;
    }
    public DragController getDragController(){
        return mDragController;
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentWrapper = (ScrollView)findViewById(R.id.contentWrapper);
        mContent = (CellLayout)findViewById(R.id.content);
        Log.e(TAG," FavoriteWorkspace onFinishInflate ....................... mContent =：" + mContent+",mContentWrapper =:" + mContentWrapper);
    }

    private Bitmap createDragBitmap(View child, AtomicInteger aPadding) {
        int padding = aPadding.get();
        Bitmap b = Bitmap.createBitmap(
                child.getWidth() + padding,
                child.getHeight() + padding,
                Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(b);
        drawDragView(child,mCanvas,padding);
        mCanvas.setBitmap(null);
        return b;
    }

    private void drawDragView(View child, Canvas canvas, int padding) {
        final Rect clipRect = sTempRect;
        child.getDrawingRect(clipRect);
        canvas.translate(-child.getScrollX() + padding / 2, -child.getScrollY() + padding / 2);
        canvas.clipRect(clipRect, Region.Op.REPLACE);
        child.draw(canvas);
    }
    private boolean beginDragShared(View v){
        Object tag = v.getTag();
        if(tag instanceof ItemInfo) {
            if(!v.isInTouchMode()){
                return false;
            }
            ItemInfo item = (ItemInfo) tag;
            mCurrentDragInfo = item;
            mEmptyCellRank = item.rank;
            mCurrentDragView = v;
            mContent.removeAndUnMakerView(mCurrentDragView);
            mInfos.remove(item);
            beginDragShared(v, new Point(), this);
        }
        return true;
    }
    public boolean onLongClick(View v){
        return beginDragShared(v);
    }
    public void beginDragShared(View child, Point relativeTouchPos,DragSource source){
        child.clearFocus();
        child.setPressed(false);
        mDragOutline = createDragOutline(child);
        AtomicInteger padding = new AtomicInteger(DRAG_BITMAP_PADDING);
        final Bitmap b = createDragBitmap(child, padding);
        final int bmpWidth = b.getWidth();
        final int bmpHeight = b.getHeight();
        float scale = mDragLayer.getLocationInDragLayer(child, mTempXY);
        int dragLayerX = Math.round(mTempXY[0] -
                (bmpWidth - scale * child.getWidth()) / 2);
        int dragLayerY = Math.round(mTempXY[1] -
                (bmpHeight - scale * bmpHeight) / 2 - padding.get() / 2);
        Point dragVisualizeOffset = null;
        Rect dragRect = null;
        if(child instanceof FavoriteItemView){
            dragVisualizeOffset = new Point(-padding.get() / 2,
                    padding.get() / 2 - child.getPaddingTop());
            dragRect = new Rect(0, child.getPaddingTop(), child.getWidth(), child.getHeight());
        }
        ItemInfo info = (ItemInfo) child.getTag();
        getParent().requestDisallowInterceptTouchEvent(false);
        mDragController.startDrag(b, dragLayerX, dragLayerY, source, info,
                DragController.DRAG_ACTION_MOVE, dragVisualizeOffset, dragRect, scale);
    }
    public View createAndAddViewForRank(ItemInfo item, int rank) {
        View icon = createNewView(item);
        addViewForRank(icon, item, rank);
        return icon;
    }

    public View createNewView(ItemInfo item) {
        FavoriteItemView itemView = null;
        if(item instanceof FavoriteShortcutInfo){
            itemView = (FavoriteShortcut)mInflater.inflate(R.layout.layout_favorite_shortcut, null, false);
            Log.e(TAG,"createNewView shortcut");
        } else if(item instanceof FavoriteFolderInfo){
            itemView = (FavoriteFolderIcon)mInflater.inflate(R.layout.layout_favorite_folder_icon, null, false);
            Log.e(TAG,"createNewView folder");
        }
        if(itemView != null) {
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnFocusChangeListener(this);
            itemView.setOnKeyListener(this);

            itemView.setLayoutParams(new CellLayout.LayoutParams(
                    item.cellX, item.cellY));
            itemView.applyFromItemInfo(item);

        }
        return itemView;
    }
    public void addViewForRank(View view, ItemInfo item, int rank) {
        mContent.addViewForRank(view,item,rank);
    }
    private int findNearestArea(int pixelX, int pixelY) {
       mContent.findNearestArea(pixelX, pixelY ,sTempPosArray);
       Log.e(TAG,"findNearestArea :: sTempPosArray[1] =: " + sTempPosArray[1] +",sTempPosArray[0] =:" + sTempPosArray[0]);
       return sTempPosArray[1] * mContent.getCountX() + sTempPosArray[0];
    }
    private int getTargetRank(DragObject d, float[] recycle) {
        recycle = d.getVisualCenter(recycle);
        Log.e(TAG,"getTargetRank recycle [0] =:" + recycle[0] + ",recycle =:" + recycle[1]);
        return findNearestArea(
                (int) recycle[0] - getPaddingLeft(), (int) recycle[1] - getPaddingTop());
    }
    private OnAlarmListener mReorderAlarmListener = new OnAlarmListener() {
        public void onAlarm(Alarm alarm) {
            Log.e(TAG,"mEmptyCellRank = : " + mEmptyCellRank + ",mTargetRank =:" + mTargetRank);
            mContent.realTimeReorder(mEmptyCellRank, mTargetRank);
            mEmptyCellRank = mTargetRank;
        }
    };
    public void clearDragInfo() {
        mCurrentDragInfo = null;
        mCurrentDragView = null;
    }
    public Bitmap createDragOutline(View child){
        child.setDrawingCacheEnabled(true);
        try {
            Bitmap bitmap = child.getDrawingCache();
            mCanvas.setBitmap(bitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setAlpha(150);
            mCanvas.drawRoundRect(new RectF(0,0,child.getWidth(),child.getHeight()),8,8,paint);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onFocusChange(View view, boolean b) {

    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        return true;
    }


    @Override
    public boolean isDropEnabled() {
        return true;
    }

    @Override
    public void onDrop(DragObject dragObject) {
        Log.e(TAG,"onDrop :: dragObject =:" + dragObject);
        View currentDragView = mCurrentDragView;
        ItemInfo info = mCurrentDragInfo;
        mContent.addViewForRank(currentDragView, info, mEmptyCellRank);
        if(dragObject.dragView.hasDrawn()){
            float scaleX = getScaleX();
            float scaleY = getScaleY();
            setScaleX(1.0f);
            setScaleY(1.0f);
            mDragLayer.animateViewIntoPosition(dragObject.dragView,currentDragView,null,null);
            setScaleX(scaleX);
            setScaleY(scaleY);
        } else {
            currentDragView.setVisibility(VISIBLE);
        }
        mInfos.add(info);
        mCurrentDragInfo = null;
        mContent.onDropChild(currentDragView);
    }

    @Override
    public void onDragEnter(DragObject dragObject) {
        Log.e(TAG,"onDragEnter :: d =:" + dragObject +",mTargetRank =:" + mTargetRank + ",mPrevTargetRank =:" + mPrevTargetRank);
        mPrevTargetRank = -1;
    }
    public int getScrollOffsetY(){
        return mContentWrapper.getScrollY();
    }
    public float getVisualAreaHeight() {
        return mVisualAreaHeight;
    }
    public int getContentAreaHeight() {
        if(mContent == null){
            return mScreenHeight;
        }
        return mContent.getCellHeight() * mContent.getCountY();
    }
    public float getContentAreaWidth() {
        return mContentAreaHeight;
    }
    public float getVisualAreaWidth() {
        return mVisualAreaWidth;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mContentAreaHeight = getContentAreaHeight();
        int contentAreaWidthSpec = MeasureSpec.makeMeasureSpec(mContentAreaWidth, MeasureSpec.EXACTLY);
        int contentAreaHeightSpec = MeasureSpec.makeMeasureSpec(mContentAreaHeight, MeasureSpec.EXACTLY);
        int scrollViewAreaHeightSpec = MeasureSpec.makeMeasureSpec(mVisualAreaHeight , MeasureSpec.EXACTLY);
        mContentWrapper.measure(contentAreaWidthSpec,scrollViewAreaHeightSpec);
        mContent.measure(contentAreaWidthSpec,contentAreaHeightSpec);
        Log.e(TAG,"onMeasure contentHeight =:" + mContentAreaHeight +",mScrollAreaHeight =:" + mVisualAreaHeight);
        setMeasuredDimension(mVisualAreaWidth,mVisualAreaHeight);
    }
    private boolean isOverScroll(){
        return (mContent.getScrollY() + mVisualAreaHeight >= mContentAreaHeight) ||
                (mContent.getScrollY() < 0);
    }
    private void onDragOver(DragObject d, int reorderDelay) {
        final float[] r = new float[2];
        mTargetRank = getTargetRank(d, r);
        if (mTargetRank != mPrevTargetRank) {
            mReorderAlarm.cancelAlarm();
            mReorderAlarm.setOnAlarmListener(mReorderAlarmListener);
            mReorderAlarm.setAlarm(REORDER_DELAY);
            mPrevTargetRank = mTargetRank;
        }
        float y = r[1] - getScrollOffsetY();
        float cellOverlap = mContent.getCellHeight() * ICON_OVERSCROLL_WIDTH_FACTOR;
        boolean isOutsideTopEdge = y < cellOverlap;
        boolean isOutsideBottomEdge = y > (getVisualAreaHeight() - cellOverlap);
        Log.e(TAG,"onDragOver :: isOverScroll =:" + isOverScroll() + "," + mContent.getScrollY() + "isOutsideBottomEdge =:" + isOutsideBottomEdge +",isOutsideTopEdge =:" + isOutsideTopEdge);
        if (isOutsideBottomEdge) {
            mContentWrapper.scrollBy(0, SCROLL_VELOCITY);
        }
        if (isOutsideTopEdge) {
            mContentWrapper.scrollBy(0, - SCROLL_VELOCITY);
        }

    }

    @Override
    public void onDragOver(DragObject dragObject) {
        onDragOver(dragObject, REORDER_DELAY);
    }

    @Override
    public void onDragExit(DragObject dragObject) {
        mReorderAlarm.cancelAlarm();
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        getHitRect(outRect);
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {

    }

    @Override
    public void onDragEnd() {

    }

    @Override
    public void onDropCompleted(View target, DragObject d, boolean success) {
        Log.e(TAG,"onDropCompleted :: --------------");
    }
    public interface OnItemClickListener {
        void onItemClick(View view);
    }
}
