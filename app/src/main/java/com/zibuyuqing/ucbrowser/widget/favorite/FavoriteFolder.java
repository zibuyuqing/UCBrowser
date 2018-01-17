package com.zibuyuqing.ucbrowser.widget.favorite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.zibuyuqing.common.utils.ViewUtil;
import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.model.bean.favorite.FavoriteFolderInfo;
import com.zibuyuqing.ucbrowser.widget.drawable.AlphaDrawable;

/**
 * Created by Xijun.Wang on 2017/12/26.
 */

public class FavoriteFolder extends RelativeLayout implements DropTarget {
    private static final String TAG = "FavoriteFolder";
    private static final float ICON_OVERSCROLL_WIDTH_FACTOR = 0.45f;
    private Context mContext;
    private RelativeLayout mFolderHead;
    private EditText mFolderName;
    private LinearLayout mFolderContentWrapper;
    private FavoriteWorkspace mFolderContent;
    private FavoriteFolderInfo mCurrentFolderInfo;
    private DragLayer mDragLayer;
    private DragController mDragController;
    private int mContentWidth,mContentHeight;
    private int mScreenWidth,mScreenHeight;
    private FavoriteFolderIcon mCurrentFolderIcon;
    private ImageView mFolderIconImageView;
    private Bitmap mFolderIconBitmap;
    private Canvas mFolderIconCanvas;
    private int[] mFolderIconLoc = new int[2];
    private AlphaDrawable mCoverBg;
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
        mScreenWidth = ViewUtil.getScreenSize(mContext).x;
        mContentWidth = mScreenWidth - mContext.getResources().getDimensionPixelSize(R.dimen.dimen_16dp);
        mContentHeight = mContext.getResources().getDimensionPixelSize(R.dimen.dimen_180dp);
        mScreenHeight = ViewUtil.getScreenSize(mContext).y;
        mCoverBg = new AlphaDrawable(mContext.getDrawable(R.drawable.draglayer_bg));
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
        mFolderContentWrapper = (LinearLayout)findViewById(R.id.folderContentWrapper);
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
        if (mFolderIconBitmap == null) {
            mFolderIconBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mFolderIconCanvas = new Canvas(mFolderIconBitmap);
        }
        DragLayer.LayoutParams lp;
        if (mFolderIconImageView.getLayoutParams() instanceof DragLayer.LayoutParams) {
            lp = (DragLayer.LayoutParams) mFolderIconImageView.getLayoutParams();
        } else {
            lp = new DragLayer.LayoutParams(width, height);
        }
        Log.e(TAG,"copyFolderIconToImage :: lp.width =:" + lp.width);
        mFolderIconCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        mCurrentFolderIcon.hideText();
        mCurrentFolderIcon.draw(mFolderIconCanvas);
        mFolderIconImageView.setImageBitmap(mFolderIconBitmap);
        if (mDragLayer.indexOfChild(mFolderIconImageView) != -1) {
            mDragLayer.removeView(mFolderIconImageView);
        }
        mFolderIconImageView.setTranslationX(mFolderIconLoc[0]);
        mFolderIconImageView.setTranslationY(mFolderIconLoc[1]);
        mDragLayer.addView(mFolderIconImageView,lp);
    }
    public void animateClosed() {
        if (!(getParent() instanceof DragLayer)) return;
        setBackground(null);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.8f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.8f);
        int[] transXY = new int[2];
        calculateTransParams(mCurrentFolderIcon,transXY);
        PropertyValuesHolder tx = PropertyValuesHolder.ofFloat("translationX", 0, transXY[0] * 0.2f);
        PropertyValuesHolder ty = PropertyValuesHolder.ofFloat("translationY", 0, transXY[1] * 0.2f);
        final ObjectAnimator oa =
                ObjectAnimator.ofPropertyValuesHolder(this, alpha, scaleX, scaleY,tx,ty);
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onCloseComplete();
                shrinkAndFadeInFolderIcon();
            }
            @Override
            public void onAnimationStart(Animator animation) {
                // animateBackgroundGradient(false,mMaterialExpandDuration,true);
            }
        });
        oa.setDuration(200);
        oa.start();
    }

    private void onCloseComplete() {
        mDragLayer.removeView(this);
        mDragController.removeDropTarget(this);
        clearDragInfo();
    }
    private void shrinkAndFadeInFolderIcon(){
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.2f,1.0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 2.0f,1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 2.0f,1.0f);
        copyFolderIconToImage();
        ObjectAnimator oa = ObjectAnimator.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(120);
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mDragLayer.removeView(mFolderIconImageView);
                mCurrentFolderIcon.setVisibility(VISIBLE);
                mCurrentFolderIcon.showText();
            }
        });
        oa.start();
    }
    public void close(){
        animateClosed();
    }

    public int getOffsetY(){
        return mFolderContent.getOffsetY() - mFolderContentWrapper.getTop()  - mFolderHead.getMeasuredHeight();
    }
    public int getOffsetX(){
        return - mFolderContentWrapper.getLeft();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mFolderContent.setVisualSize(mContentWidth,mContentHeight,true);
        setMeasuredDimension(mScreenWidth,mScreenHeight);
    }

    private void clearDragInfo() {
        mFolderContent.clearDragInfo();
    }
    public void open(FavoriteFolderIcon icon,boolean animate,final Runnable onCompleteRunnable){
        animateShow(icon,animate,onCompleteRunnable);
    }
    private void calculateTransParams(FavoriteFolderIcon icon,int[] transXY){
        mDragLayer.getLocationInDragLayer(icon, mFolderIconLoc);
        int iconLeft = (int) (mFolderIconLoc[0] + icon.getBackgroundLeftToEdge());
        int iconTop = (int) (mFolderIconLoc[1] + icon.getBackgroundTopToEdge());
        int startLeft = iconLeft - (mScreenWidth / 2);
        int startTop = iconTop - (mScreenHeight / 2);
        transXY[0] = startLeft + icon.getIconSize() / 2;
        transXY[1] = startTop + icon.getIconSize() / 2;
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
            LayoutParams lp = (LayoutParams) mFolderContentWrapper.getLayoutParams();
            if(lp != null){
                lp.width = mContentWidth;
                lp.leftMargin = lp.rightMargin = (mScreenWidth - mContentWidth) / 2;
                lp.topMargin = (mScreenHeight - mContentHeight) / 2;
                mFolderContentWrapper.setLayoutParams(lp);
            }
            mDragLayer.addView(this);
            mDragController.addDropTarget(this);
        }
        mCurrentFolderInfo = info;
        mCurrentFolderIcon = icon;
        bindInfo();
        if(animate) {
            setBackground(mCoverBg);
            float startScale = 0.8f;
            PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0, 1.0f);
            PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", startScale, 1.0f);
            PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", startScale, 1.0f);
            int[] transXY = new int[2];
            calculateTransParams(icon,transXY);
            PropertyValuesHolder tx = PropertyValuesHolder.ofFloat("translationX", transXY[0] * 0.2f, 0);
            PropertyValuesHolder ty = PropertyValuesHolder.ofFloat("translationY", transXY[1] * 0.2f, 0);
            ObjectAnimator bodyAnim = ObjectAnimator.ofPropertyValuesHolder(mFolderContentWrapper, alpha, scaleX, scaleY, tx, ty);
            bodyAnim.setDuration(300);
            bodyAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float alpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    mCoverBg.setAlpha((int) (alpha * 255));
                    invalidate();
                }
            });
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
                    mCurrentFolderIcon.setVisibility(INVISIBLE);
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
        Log.e(TAG,"onDrop :: ");
        mFolderContent.onDrop(dragObject);
    }

    @Override
    public void onDragEnter(DragObject dragObject) {
        Log.e(TAG,"onDragEnter :: ");
        mFolderContent.onDragEnter(dragObject);
    }

    @Override
    public void onDragOver(DragObject dragObject) {
        Log.e(TAG,"onDragOver :: ");
        mFolderContent.onDragOver(dragObject);
    }

    @Override
    public void onDragExit(DragObject dragObject) {
        Log.e(TAG,"onDragExit :: ");
//        close();
        mFolderContent.onDragExit(dragObject);
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        Rect rect = new Rect();
        mFolderContentWrapper.getHitRect(rect);
        outRect.set(
                rect.left,
                (int) (rect.top - mFolderContent.getCellHeight() * ICON_OVERSCROLL_WIDTH_FACTOR),
                rect.right,
                (int) (rect.bottom + mFolderContent.getCellHeight() * ICON_OVERSCROLL_WIDTH_FACTOR));
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
