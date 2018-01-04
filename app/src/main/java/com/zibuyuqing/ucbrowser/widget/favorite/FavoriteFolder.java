package com.zibuyuqing.ucbrowser.widget.favorite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.zibuyuqing.common.utils.*;
import com.zibuyuqing.common.utils.ViewUtil;
import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.model.bean.favorite.FavoriteFolderInfo;

/**
 * Created by Xijun.Wang on 2017/12/26.
 */

public class FavoriteFolder extends LinearLayout implements DropTarget {
    private static final String TAG = "FavoriteFolder";
    private Context mContext;
    private RelativeLayout mFolderHead;
    private EditText mFolderName;
    private FavoriteWorkspace mFolderContent;
    private FavoriteFolderInfo mCurrentFolderInfo;
    private DragLayer mDragLayer;
    private DragController mDragController;
    private int mContentWidth,mContentHeight;
    private FavoriteFolderIcon mCurrentFolderIcon;
    private ImageView mFolderIconImageView;
    private Bitmap mFolderIconBitmap;
    private Canvas mFolderIconCanvas;
    public FavoriteFolder(Context context) {
        this(context,null);
    }

    public FavoriteFolder(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FavoriteFolder(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mContext = getContext();
        mContentWidth = ViewUtil.getScreenSize(mContext).x;
        mContentHeight = mContentWidth;
    }

    public static FavoriteFolder fromXml(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        return (FavoriteFolder) inflater.inflate(R.layout.layout_uc_favorite_folder, null);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mFolderHead = (RelativeLayout) findViewById(R.id.folderHead);
        mFolderName = (EditText) findViewById(R.id.folderName);
        mFolderContent = (FavoriteWorkspace) findViewById(R.id.folderContent);
        Log.e(TAG," FavoriteFolder onFinishInflate .......................");
        Log.e(TAG," FavoriteWorkspace onFinishInflate ....................... mContent =：" + mFolderContent.mContent+",mContentWrapper =:" + mFolderContent.mContentWrapper);
    }
    public void setup(DragLayer dragLayer){
        mDragLayer = dragLayer;
        mDragController = dragLayer.getDragController();
        mFolderContent.setup(dragLayer);
    }
    private void bindInfo(){
        setTag(mCurrentFolderInfo);
        mFolderName.setText(mCurrentFolderInfo.getDescription());
        mFolderContent.bindItems(mCurrentFolderInfo.getContents());
    }
    private void copyFolderIconToImage(){
        final int width = mCurrentFolderIcon.getMeasuredWidth();
        final int height = mCurrentFolderIcon.getMeasuredHeight();
        if (mFolderIconImageView == null) {
            mFolderIconImageView = new ImageView(mContext);
        }
        if (mFolderIconBitmap == null || mFolderIconBitmap.getWidth() != width ||
                mFolderIconBitmap.getHeight() != height) {
            mFolderIconBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mFolderIconCanvas = new Canvas(mFolderIconBitmap);
        }
        mFolderIconCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        mCurrentFolderIcon.draw(mFolderIconCanvas);
        mFolderIconImageView.setImageBitmap(mFolderIconBitmap);
        if (mDragLayer.indexOfChild(mFolderIconImageView) != -1) {
            mDragLayer.removeView(mFolderIconImageView);
        }
        mDragLayer.addView(mFolderIconImageView);
    }
    public void close(){
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f);
        copyFolderIconToImage();
        ObjectAnimator oa = ObjectAnimator.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(300);
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mDragLayer.removeView(mFolderIconImageView);
            }
        });
        oa.start();
        mDragLayer.removeView(this);
        mDragController.removeDropTarget(this);
        clearDragInfo();
    }
    private void clearDragInfo() {
        mFolderContent.clearDragInfo();
    }
    public void open(FavoriteFolderIcon icon,boolean animate,final Runnable onCompleteRunnable){
        animateShow(icon,animate,onCompleteRunnable);
    }
    private void animateShow(FavoriteFolderIcon icon, boolean animate, final Runnable onCompleteRunnable){
        if(icon == null){
            return;
        }
        FavoriteFolderInfo info = (FavoriteFolderInfo) icon.getTag();
        if(info == null){
            return;
        }
        if(getParent() == null){
            mDragLayer.addView(this);
            mDragController.addDropTarget(this);
        }
        mCurrentFolderInfo = info;
        mCurrentFolderIcon = icon;
        bindInfo();
        if(animate) {
            int iconSize = icon.getIconSize();
            float startScale = (float) iconSize / mContentWidth;
            PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0, 1.0f);
            PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", startScale, 1.0f);
            PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", startScale, 1.0f);
            int location[] = new int[2];
            mDragLayer.getLocationInDragLayer(icon, location);
            int transX = location[0];
            int transY = location[1];
            PropertyValuesHolder tx = PropertyValuesHolder.ofFloat("translationX", transX, 0);
            PropertyValuesHolder ty = PropertyValuesHolder.ofFloat("translationY", transY, 0);
            ObjectAnimator bodyAnim = ObjectAnimator.ofPropertyValuesHolder(this, alpha, scaleX, scaleY, tx, ty);
            bodyAnim.setDuration(500);
            bodyAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }
                }
            });
            bodyAnim.start();
        }
    }

    @Override
    public boolean isDropEnabled() {
        return mFolderContent.isDropEnabled();
    }

    @Override
    public void onDrop(DragObject dragObject) {
        mFolderContent.onDrop(dragObject);
    }

    @Override
    public void onDragEnter(DragObject dragObject) {
        mFolderContent.onDragEnter(dragObject);
    }

    @Override
    public void onDragOver(DragObject dragObject) {
        mFolderContent.onDragOver(dragObject);
    }

    @Override
    public void onDragExit(DragObject dragObject) {
        mFolderContent.onDragExit(dragObject);
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        getHitRect(outRect);
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        mFolderContent.onDragStart(source,info,dragAction);
    }

    @Override
    public void onDragEnd() {
        mFolderContent.onDragEnd();
    }
}
