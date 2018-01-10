package com.zibuyuqing.ucbrowser.widget.favorite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.model.bean.favorite.FavoriteFolderInfo;
import com.zibuyuqing.ucbrowser.model.bean.favorite.FavoriteShortcutInfo;
import com.zibuyuqing.ucbrowser.model.bean.favorite.ItemInfo;

/**
 * Created by xijun.wang on 2017/12/22.
 */

public class FavoriteFolderIcon extends FavoriteItemView {
    private FavoriteFolderInfo mFolderInfo;
    public static final int NUM_ITEMS_IN_PREVIEW = 4;
    private ImageView mPreviewBackground;
    private int mPerLineCount;
    private float mFinalItemPreviewIconScale = 1f;
    private float mFinalCanvasScale = 1f;
    private int mIntrinsicIconSize;
    private int mPreviewItemPadding;
    private int[] mCanvasOffset;
    private int mPreviewOffsetX;
    private int mPreviewOffsetY;
    private float mBaselineIconScale;
    private Paint mPaint;
    private PreviewItemDrawingParams mParams = new PreviewItemDrawingParams(0, 0, 0, 0);

    public FavoriteFolderIcon(Context context) {
        this(context, null);
    }

    public int getBackgroundWidth() {
        return mIntrinsicIconSize;
    }

    public int getPreviewItemPadding() {
        return mPreviewItemPadding;
    }

    @Override
    protected void init() {
        super.init();
        mCanvasOffset = new int[2];
        mPerLineCount = (int) Math.sqrt(NUM_ITEMS_IN_PREVIEW);
        mPreviewItemPadding = getContext().getResources().getDimensionPixelSize(R.dimen.folder_icon_item_preview_padding);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mRes.getColor(R.color.themeBlue, null));
        mPaint.setStyle(Paint.Style.FILL);
        computePreviewDrawingParams();
    }
    public ImageView getIconImageView(){
        return ivIcon;
    }
    @Override
    public void applyFromItemInfo(ItemInfo itemInfo) {
        mFolderInfo = (FavoriteFolderInfo) itemInfo;
        super.applyFromItemInfo(itemInfo);
    }

    private PreviewItemDrawingParams computePreviewItemDrawingParams(int index,
                                                                     PreviewItemDrawingParams params) {
        int lineY = index / mPerLineCount;
        float transX = (index - mPerLineCount * lineY) *
                mIntrinsicIconSize / mPerLineCount;
        float transY = mIntrinsicIconSize / mPerLineCount * lineY;
        if (params == null) {
            params = new PreviewItemDrawingParams(transX, transY, mBaselineIconScale, 0);
        } else {
            params.transX = transX;
            params.transY = transY;
            params.scale = mBaselineIconScale;
            params.overlayAlpha = 0;
        }
        return params;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

    }
    public void hideText(){
        tvDescription.setVisibility(GONE);
    }
    public void showText(){
        tvDescription.setVisibility(VISIBLE);
    }
    @Override
    protected void drawOthers(Canvas canvas) {
        super.drawOthers(canvas);
        if (mFolderInfo != null) {
            int nItemsInPreview = Math.min(mFolderInfo.getContents().size(), NUM_ITEMS_IN_PREVIEW);
            for (int i = 0; i < nItemsInPreview; i++) {
                mParams = computePreviewItemDrawingParams(i, mParams);
                mParams.bitmap = getItemPreview(i);
                Log.e(TAG, "i =:" + i + ",mParams =:" + mParams);
                drawPreviewItem(canvas, mParams);
            }
        }
    }

    public Bitmap getItemPreview(int index) {
        if (mFolderInfo == null) {
            return null;
        }
        if (index < 0 || index >= mFolderInfo.getContents().size()) {
            return null;
        }
        return mFolderInfo.getContents().get(index).getIcon();
    }

    public float getPreviewItemPaddingTopToEdge() {
        return  mRes.getDimensionPixelSize(R.dimen.dimen_12dp) +
                (getBackgroundWidth() - getBackgroundWidth() * mFinalCanvasScale) / 2;
    }
    public float getBackgroundLeftToEdge(){
        return (mWidth - getBackgroundWidth()) / 2;
    }
    public float getBackgroundTopToEdge(){
        return mRes.getDimensionPixelSize(R.dimen.dimen_12dp);
    }
    public float getPreviewItemPaddingLeftToEdge() {
        return (mWidth - getBackgroundWidth() +
                getBackgroundWidth() - getBackgroundWidth() * mFinalCanvasScale) / 2;
    }
    private void computePreviewDrawingParams() {
        mIntrinsicIconSize = mRes.getDimensionPixelSize(R.dimen.dimen_54dp);
        calculateIconScale();
        mBaselineIconScale = mFinalItemPreviewIconScale;
        mCanvasOffset[0] = (int) getPreviewItemPaddingLeftToEdge() ;
        mCanvasOffset[1] = (int) getPreviewItemPaddingTopToEdge();
    }

    public int getPreviewItemSize() {
        return (int) (mIntrinsicIconSize / mPerLineCount * mFinalCanvasScale);
    }

    public void addItem(FavoriteShortcutInfo item) {
        mFolderInfo.addItem(item);
    }

    private void calculateIconScale() {
        mFinalCanvasScale = (float) (getBackgroundWidth() - 2 * getContext().getResources().
                getDimensionPixelSize(R.dimen.folder_icon_content_padding) +
                getPreviewItemPadding()) / getBackgroundWidth();
        mFinalItemPreviewIconScale = (float) (getBackgroundWidth() * mFinalCanvasScale / mPerLineCount -
                getPreviewItemPadding()) / (getBackgroundWidth() * mFinalCanvasScale / mPerLineCount);
    }

    private void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams params) {
        canvas.save();
        canvas.translate(mCanvasOffset[0], mCanvasOffset[1]);
        canvas.scale(mFinalCanvasScale, mFinalCanvasScale);
        canvas.translate(params.transX, params.transY);
        Bitmap bitmap = params.bitmap;
        if (bitmap != null) {
            Bitmap finalBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    mIntrinsicIconSize / mPerLineCount,
                    mIntrinsicIconSize / mPerLineCount,
                    true);
            canvas.scale(params.scale, params.scale);
            canvas.drawBitmap(finalBitmap, 0, 0, mPaint);
        }
        canvas.restore();
    }

    public FavoriteFolderIcon(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FavoriteFolderIcon(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    class PreviewItemDrawingParams {
        PreviewItemDrawingParams(float transX, float transY, float scale, int overlayAlpha) {
            this.transX = transX;
            this.transY = transY;
            this.scale = scale;
            this.overlayAlpha = overlayAlpha;
        }

        float transX;
        float transY;
        float scale;
        int overlayAlpha;
        Bitmap bitmap;

        @Override
        public String toString() {
            return "PreviewItemDrawingParams [ transX :" + transX + ",transY :" + transX + ",bitmap :" + bitmap + "]";
        }
    }
}
