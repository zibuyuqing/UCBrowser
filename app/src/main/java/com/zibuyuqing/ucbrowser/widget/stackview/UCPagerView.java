package com.zibuyuqing.ucbrowser.widget.stackview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zibuyuqing.ucbrowser.R;

/**
 * Created by Xijun.Wang on 2017/12/4.
 */

public class UCPagerView extends FrameLayout implements View.OnClickListener {
    private final static String TAG = UCPagerView.class.getSimpleName();
    private ImageView ivClose, ivHome, ivPagePreview;
    private TextView tvUC;
    private RelativeLayout rlPageHead;
    private int key;
    private Context mContext;
    float mTaskProgress;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public UCPagerView(@NonNull Context context) {
        this(context, null);
    }

    public UCPagerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UCPagerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ivClose = (ImageView) findViewById(R.id.ivPageClose);
        ivPagePreview = (ImageView) findViewById(R.id.ivPagePreview);
        ivHome = (ImageView) findViewById(R.id.ivPageHome);
        rlPageHead = (RelativeLayout) findViewById(R.id.rlPageHead);
        ivClose.setOnClickListener(this);
        this.setOnClickListener(this);
    }

    public void setPagePreview(Bitmap bitmap) {
        ivPagePreview.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(View view) {

    }

}
