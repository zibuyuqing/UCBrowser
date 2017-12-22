package com.zibuyuqing.ucbrowser.widget.favorite;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.zibuyuqing.ucbrowser.R;
import com.zibuyuqing.ucbrowser.model.bean.favorite.ItemInfo;

/**
 * Created by xijun.wang on 2017/12/22.
 */

public class FavoriteShortcut extends FavoriteItemView {
    private Bitmap mIcon;
    protected int mIconSize;
    protected String mDescription;
    protected Context mContext;
    protected Resources mRes;
    protected ImageView ivPreview;
    protected TextView tvDescription;
    protected ItemInfo mInfo;
    public FavoriteShortcut(Context context) {
        this(context,null);
    }

    public FavoriteShortcut(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FavoriteShortcut(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mContext = getContext();
        mRes = getResources();
        mIconSize = mRes.getDimensionPixelSize(R.dimen.dimen_64dp);
    }
    public void applyFromItemInfo(ItemInfo itemInfo){
        setTag(itemInfo);
        mInfo = itemInfo;
        setIcon(itemInfo.icon);
        setDescription(itemInfo.description);
    }
    public void setIcon(Bitmap icon){
        mIcon = icon;
        ivPreview.setImageBitmap(icon);
    }
    public void setDescription(String des){
        tvDescription.setText(des);
    }
}
