package com.iss.innoz.tinkerdemo.ui.guide.view;

import android.content.Context;
import android.widget.Scroller;

/**
 * SampleAndroidProject
 * com.app.sampleandroidproject.ui.guide.view
 *
 * @Author: xie
 * @Time: 2016/12/8 18:04
 * @Description:
 */


public class GuideBaseScoller extends Scroller {
    private int mDuration = 1000;

    public GuideBaseScoller(Context context, int duration) {
        super(context);
        mDuration = duration;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }
}