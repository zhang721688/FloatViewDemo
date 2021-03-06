package com.zxn.floatwindow;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;

public class FloatWindow {

//    /**
//     * 触摸点按下相对view的左上角的位置
//     * 用于跟滑动后手指相对屏幕左上角位置计算view相对屏幕的位置x、y
//     */
//    private float downX;
//    private float downY;
//
//    /**
//     * 触摸滑动后手指相对屏幕左上角的位置坐标
//     */
//    private float rowX;
//    private float rowY;

    /**
     * 悬浮窗起始位置坐标
     */
    private int startX;
    private int startY;

    /**
     * 悬浮窗内部view的宽高
     */
    private int width;
    private int height;

    private Context context;
    private WindowManager windowManager;
    private DisplayMetrics displayMetrics;
    private WindowManager.LayoutParams layoutParams;
    private FloatView floatView;

    /**
     * 悬浮窗显示标记
     */
    private boolean isShowing;
    private boolean isAddView;

    public FloatWindow(FloatConfig floatConfig){
        this.context = floatConfig.getContext();
        this.width = floatConfig.getWidth();
        this.height = floatConfig.getHeight();
        this.startX = floatConfig.getStartX();
        this.startY = floatConfig.getStartY();
        initWindowManager();
        initlayoutParams();
        initFloatView(floatConfig.getContentView());
    }

    private void initWindowManager() {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
    }

    private void initlayoutParams() {
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        if (width!=layoutParams.width){
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        }
        if (height!=layoutParams.height){
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        }
        //其他窗口将始终显示在使用 TYPE_APPLICATION_OVERLAY 窗口类型的窗口下方
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0新特性
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        layoutParams.x = startX;
        layoutParams.y = startY;
    }

    private void initFloatView(View contentView) {
        if (floatView==null){
            floatView = new FloatView(context,contentView,floatViewCallBack);
        }
    }

    private FloatView.FloatViewCallBack floatViewCallBack = new FloatView.FloatViewCallBack() {
        @Override
        public void updateLocation(float offsetX, float offsetY) {
            FloatWindow.this.updateLocation(offsetX,offsetY);
        }

        @Override
        public void autoAlign(float rawX) {
            FloatWindow.this.autoAlign(rawX);
        }
    };

    /**
     * 更新位置
     */
    private void updateLocation(float offsetX, float offsetY) {
        layoutParams.x = (int) offsetX;
        layoutParams.y = (int) offsetY;
        windowManager.updateViewLayout(floatView, layoutParams);
    }

    /**
     * 自动贴边
     */
    private void autoAlign(float rawX) {
        float fromX = layoutParams.x;

        if (rawX <= displayMetrics.widthPixels / 2) {
            layoutParams.x = 0;
        } else {
            layoutParams.x = displayMetrics.widthPixels-floatView.getWidth();
        }
        //这里使用ValueAnimator来平滑计算起始X坐标到结束X坐标之间的值，并更新悬浮窗位置
        ValueAnimator animator = ValueAnimator.ofFloat(fromX, layoutParams.x);
        animator.setDuration(500);
        animator.setInterpolator(new BounceInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //这里会返回fromX ~ mLayoutParams.x之间经过计算的过渡值
                float toX = (float) animation.getAnimatedValue();
                //我们直接使用这个值来更新悬浮窗位置
                updateLocation(toX, layoutParams.y);
            }
        });
        animator.start();
    }

    /**
     * 将窗体添加到屏幕上
     */
    @SuppressLint("NewApi")
    public void show() {
        if (!isShowing()) {
            floatView.setVisibility(View.VISIBLE);

            if (!isAddView) {
                windowManager.addView(floatView, layoutParams);
                isAddView = true;
            }
            isShowing = true;
        }
    }

    public void hidden() {
        isShowing = false;
        if (floatView != null) {
            floatView.setVisibility(View.GONE);
        }
    }

    /**
     * 悬浮窗是否正在显示
     *
     * @return true if it's showing.
     */
    private boolean isShowing() {
        if (floatView != null && floatView.getVisibility() == View.VISIBLE) {
            return isShowing;
        }
        return false;
    }

}
