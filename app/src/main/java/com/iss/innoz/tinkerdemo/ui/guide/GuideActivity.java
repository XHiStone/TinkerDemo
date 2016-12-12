package com.iss.innoz.tinkerdemo.ui.guide;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.iss.innoz.tinkerdemo.R;
import com.iss.innoz.tinkerdemo.ui.base.IBaseActivity;
import com.iss.innoz.tinkerdemo.ui.guide.view.GuideViewPager;
import com.iss.innoz.tinkerdemo.utils.FrescoUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class GuideActivity extends IBaseActivity {

    @BindView(R.id.guide_background)
    GuideViewPager guideBackground;
    @BindView(R.id.guide_foreground)
    GuideViewPager guideForeground;
    @BindView(R.id.tv_guide_skip)
    TextView tvGuideSkip;
    @BindView(R.id.btn_guide_enter)
    Button btnGuideEnter;
    @BindView(R.id.activity_guide)
    RelativeLayout activityGuide;

    @Override
    protected int getContentResource() {
        return R.layout.activity_guide;
    }

    @Override
    protected void startWork(Bundle savedInstanceState) {
        guideForeground.setEnterViewAndSkipView(btnGuideEnter, tvGuideSkip);
        guideBackground.setOverScrollMode(View.OVER_SCROLL_NEVER);
        guideForeground.setOverScrollMode(View.OVER_SCROLL_NEVER);
        guideBackground.setAdapter((pager, view, model, position) -> {
            if (model != null)
                FrescoUtil.displayImage((SimpleDraweeView) view, (String) model);
        });
        guideBackground.setData(Arrays.asList("res:///" + R.mipmap.uoko_guide_background_1,
                "res:///" + R.mipmap.uoko_guide_background_2,
                "res:///" + R.mipmap.uoko_guide_background_3), null);
        List<String> path = Arrays.asList("res:///" + R.mipmap.uoko_guide_foreground_1,
                "res:///" + R.mipmap.uoko_guide_foreground_2,
                "res:///" + R.mipmap.uoko_guide_foreground_3);
        List<View> views = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            SimpleDraweeView view = new SimpleDraweeView(this);
            FrescoUtil.displayImage(view, path.get(i));
            views.add(view);
        }
        guideForeground.setData(views);
//        toastor.showSingleLongToast("测试");
        toastor.showSingleLongToast("补丁");
    }

    @OnClick({R.id.tv_guide_skip,R.id.btn_guide_enter})
    void onBtnClick(View v){
        finish();
    }

    @Override
    protected void stopWork() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        guideBackground.setBackgroundResource(android.R.color.white);
    }
}
