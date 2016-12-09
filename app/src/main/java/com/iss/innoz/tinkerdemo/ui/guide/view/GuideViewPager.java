package com.iss.innoz.tinkerdemo.ui.guide.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.iss.innoz.tinkerdemo.R;
import com.iss.innoz.tinkerdemo.ui.guide.transformer.BGAPageTransformer;
import com.iss.innoz.tinkerdemo.ui.guide.transformer.TransitionEffect;
import com.iss.innoz.tinkerdemo.utils.BaseTools;
import com.iss.innoz.tinkerdemo.utils.DensityUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * SampleAndroidProject
 * com.app.sampleandroidproject.ui.guide.view
 *
 * @Author: xie
 * @Time: 2016/12/6 9:56
 * @Description:
 */


public class GuideViewPager extends RelativeLayout implements ViewPager.OnPageChangeListener, GuideBasePager.AutoPlayDelegate {
    private Context context;
    private List<View> mViews;
    private List<View> mHackyViews;
    private List<? extends Object> mModels;
    private List<String> mTips;

    private AutoPlayTask mAutoPlayTask;
    private float mPageScrollPositionOffset;
    private int mPageScrollPosition;

    private int mPointLeftRightMargin;
    private int mPointTopBottomMargin;
    private int mPointContainerLeftRightPadding;
    private int mTipTextSize;
    private int mNumberIndicatorTextSize;
    private TextView mTipTv;
    private ImageView mPlaceholderIv;
    private TextView mNumberIndicatorTv;
    private LinearLayout mPointRealContainerLl;
    private TransitionEffect mTransitionEffect;
    private Drawable mNumberIndicatorBackground;
    private boolean mIsNeedShowIndicatorOnOnlyOnePage;
    private Drawable mPointContainerBackgroundDrawable;
    private View mSkipView;
    private View mEnterView;
    private Adapter mAdapter;
    private GuideBasePager mViewPager;
    private OnItemClickListener mOnItemClickListener;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;

    private int mOverScrollMode = OVER_SCROLL_ALWAYS;
    private boolean mAllowUserScrollable = true;
    private static final int VEL_THRESHOLD = 400;
    private int mAutoPlayInterval = 3000;
    private int mPageChangeDuration = 800;
    private int mPlaceholderDrawableResId = -1;
    private int mTipTextColor = Color.WHITE;
    private boolean mAutoPlayAble = true;
    private boolean mIsNumberIndicator = false;
    private int mNumberIndicatorTextColor = Color.WHITE;
    private int mPointDrawableResId = R.drawable.bga_banner_selector_point_solid;
    private int mPointGravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
    private static final int RMP = LayoutParams.MATCH_PARENT;
    private static final int RWC = LayoutParams.WRAP_CONTENT;
    private static final int LWC = LinearLayout.LayoutParams.WRAP_CONTENT;

    public GuideViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context.getApplicationContext();
        initDefaultAttrs(context);
        initCustomAttrs(context, attrs);
        initView(context);
    }

    public GuideViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context.getApplicationContext();
        initDefaultAttrs(context);
        initCustomAttrs(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        RelativeLayout pointContainerRl = new RelativeLayout(context);
        if (Build.VERSION.SDK_INT >= 16) {
            pointContainerRl.setBackground(mPointContainerBackgroundDrawable);
        } else {
            pointContainerRl.setBackgroundDrawable(mPointContainerBackgroundDrawable);
        }
        pointContainerRl.setPadding(mPointContainerLeftRightPadding,
                mPointTopBottomMargin,
                mPointContainerLeftRightPadding,
                mPointTopBottomMargin);
        LayoutParams pointContainerLp = new LayoutParams(RMP, RWC);
        // 处理圆点在顶部还是底部
        if ((mPointGravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.TOP) {
            pointContainerLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        } else {
            pointContainerLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        addView(pointContainerRl, pointContainerLp);
        LayoutParams indicatorLp = new LayoutParams(RWC, RWC);
        indicatorLp.addRule(CENTER_VERTICAL);
        if (mIsNumberIndicator) {
            mNumberIndicatorTv = new TextView(context);
            mNumberIndicatorTv.setId(R.id.indicatorId);
            mNumberIndicatorTv.setGravity(Gravity.CENTER_VERTICAL);
            mNumberIndicatorTv.setSingleLine(true);
            mNumberIndicatorTv.setEllipsize(TextUtils.TruncateAt.END);
            mNumberIndicatorTv.setTextColor(mNumberIndicatorTextColor);
            mNumberIndicatorTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mNumberIndicatorTextSize);
            mNumberIndicatorTv.setVisibility(View.INVISIBLE);
            if (mNumberIndicatorBackground != null) {
                if (Build.VERSION.SDK_INT >= 16) {
                    mNumberIndicatorTv.setBackground(mNumberIndicatorBackground);
                } else {
                    mNumberIndicatorTv.setBackgroundDrawable(mNumberIndicatorBackground);
                }
            }
            pointContainerRl.addView(mNumberIndicatorTv, indicatorLp);
        } else {
            mPointRealContainerLl = new LinearLayout(context);
            mPointRealContainerLl.setId(R.id.indicatorId);
            mPointRealContainerLl.setOrientation(LinearLayout.HORIZONTAL);
            pointContainerRl.addView(mPointRealContainerLl, indicatorLp);
        }
        LayoutParams tipLp = new LayoutParams(RMP, RWC);
        tipLp.addRule(CENTER_VERTICAL);
        mTipTv = new TextView(context);
        mTipTv.setGravity(Gravity.CENTER_VERTICAL);
        mTipTv.setSingleLine(true);
        mTipTv.setEllipsize(TextUtils.TruncateAt.END);
        mTipTv.setTextColor(mTipTextColor);
        mTipTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTipTextSize);
        pointContainerRl.addView(mTipTv, tipLp);
        int horizontalGravity = mPointGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        // 处理圆点在左边、右边还是水平居中
        if (horizontalGravity == Gravity.LEFT) {
            indicatorLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tipLp.addRule(RelativeLayout.RIGHT_OF, R.id.indicatorId);
            mTipTv.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        } else if (horizontalGravity == Gravity.RIGHT) {
            indicatorLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            tipLp.addRule(RelativeLayout.LEFT_OF, R.id.indicatorId);
        } else {
            indicatorLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            tipLp.addRule(RelativeLayout.LEFT_OF, R.id.indicatorId);
        }
        if (mPlaceholderDrawableResId != -1) {
            mPlaceholderIv = BaseTools.getItemImageView(context, mPlaceholderDrawableResId);
            addView(mPlaceholderIv);
        }
    }

    private void initCustomAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.guide_pager);
        final int N = typedArray.getIndexCount();
        for (int i = 0; i < N; i++) {
            initCustomAttr(typedArray.getIndex(i), typedArray);
        }
        typedArray.recycle();
    }

    private void initCustomAttr(int attr, TypedArray typedArray) {
        if (attr == R.styleable.guide_pager_pointDrawable) {
            mPointDrawableResId = typedArray.getResourceId(attr, R.drawable.bga_banner_selector_point_solid);
        } else if (attr == R.styleable.guide_pager_pointContainerBackground) {
            mPointContainerBackgroundDrawable = typedArray.getDrawable(attr);
        } else if (attr == R.styleable.guide_pager_pointLeftRightMargin) {
            mPointLeftRightMargin = typedArray.getDimensionPixelSize(attr, mPointLeftRightMargin);
        } else if (attr == R.styleable.guide_pager_pointContainerLeftRightPadding) {
            mPointContainerLeftRightPadding = typedArray.getDimensionPixelSize(attr, mPointContainerLeftRightPadding);
        } else if (attr == R.styleable.guide_pager_pointTopBottomMargin) {
            mPointTopBottomMargin = typedArray.getDimensionPixelSize(attr, mPointTopBottomMargin);
        } else if (attr == R.styleable.guide_pager_indicatorGravity) {
            mPointGravity = typedArray.getInt(attr, mPointGravity);
        } else if (attr == R.styleable.guide_pager_pointAutoPlayAble) {
            mAutoPlayAble = typedArray.getBoolean(attr, mAutoPlayAble);
        } else if (attr == R.styleable.guide_pager_pointAutoPlayInterval) {
            mAutoPlayInterval = typedArray.getInteger(attr, mAutoPlayInterval);
        } else if (attr == R.styleable.guide_pager_pageChangeDuration) {
            mPageChangeDuration = typedArray.getInteger(attr, mPageChangeDuration);
        } else if (attr == R.styleable.guide_pager_transitionEffect) {
            int ordinal = typedArray.getInt(attr, TransitionEffect.Accordion.ordinal());
            mTransitionEffect = TransitionEffect.values()[ordinal];
        } else if (attr == R.styleable.guide_pager_tipTextColor) {
            mTipTextColor = typedArray.getColor(attr, mTipTextColor);
        } else if (attr == R.styleable.guide_pager_tipTextSize) {
            mTipTextSize = typedArray.getDimensionPixelSize(attr, mTipTextSize);
        } else if (attr == R.styleable.guide_pager_placeholderDrawable) {
            mPlaceholderDrawableResId = typedArray.getResourceId(attr, mPlaceholderDrawableResId);
        } else if (attr == R.styleable.guide_pager_isNumberIndicator) {
            mIsNumberIndicator = typedArray.getBoolean(attr, mIsNumberIndicator);
        } else if (attr == R.styleable.guide_pager_numberIndicatorTextColor) {
            mNumberIndicatorTextColor = typedArray.getColor(attr, mNumberIndicatorTextColor);
        } else if (attr == R.styleable.guide_pager_numberIndicatorTextSize) {
            mNumberIndicatorTextSize = typedArray.getDimensionPixelSize(attr, mNumberIndicatorTextSize);
        } else if (attr == R.styleable.guide_pager_numberIndicatorBackground) {
            mNumberIndicatorBackground = typedArray.getDrawable(attr);
        } else if (attr == R.styleable.guide_pager_isNeedShowIndicatorOnOnlyOnePage) {
            mIsNeedShowIndicatorOnOnlyOnePage = typedArray.getBoolean(attr, mIsNeedShowIndicatorOnOnlyOnePage);
        }
    }

    private void initDefaultAttrs(Context context) {

        mPointLeftRightMargin = DensityUtils.dp2px(context, 3);
        mPointTopBottomMargin = DensityUtils.dp2px(context, 6);
        mPointContainerLeftRightPadding = DensityUtils.dp2px(context, 10);
        mPointContainerBackgroundDrawable = new ColorDrawable(Color.parseColor("#44aaaaaa"));
        mTransitionEffect = TransitionEffect.Default;
        mTipTextSize = DensityUtils.sp2px(context, 10);
        mNumberIndicatorTextSize = DensityUtils.sp2px(context, 10);
    }

    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
    }

    /**
     * 设置每一页的控件集合，主要针对引导页的情况
     *
     * @param views 每一页的控件集合
     */
    public void setData(List<View> views) {
        setData(views, null, null);
    }

    /**
     * 设置数据模型和文案，布局资源默认为ImageView
     *
     * @param models 每一页的数据模型集合
     * @param tips   每一页的提示文案集合
     */
    public void setImgData(List<? extends Object> models, List<String> tips) {
        setData(R.layout.bga_banner_item_image, models, tips);
    }

    /**
     * 设置布局资源id、数据模型和文案
     *
     * @param layoutResId item布局文件资源id
     * @param models      每一页的数据模型集合
     * @param tips        每一页的提示文案集合
     */
    public void setData(@LayoutRes int layoutResId, List<? extends Object> models, List<String> tips) {
        mViews = new ArrayList<>();
        for (int i = 0; i < models.size(); i++) {
            mViews.add(View.inflate(getContext(), layoutResId, null));
        }
        if (mAutoPlayAble && mViews.size() < 3) {
            mHackyViews = new ArrayList<>(mViews);
            mHackyViews.add(View.inflate(getContext(), layoutResId, null));
            if (mHackyViews.size() == 2) {
                mHackyViews.add(View.inflate(getContext(), layoutResId, null));
            }
        }
        setData(mViews, models, tips);
    }

    /**
     * 设置数据模型和文案，布局资源默认为ImageView
     *
     * @param models 每一页的数据模型集合
     * @param tips   每一页的提示文案集合
     */
    public void setData(List<String> models, List<String> tips) {
        mViews = new ArrayList<>();
        for (int i = 0; i < models.size(); i++) {
            SimpleDraweeView view = new SimpleDraweeView(context);
            mViews.add(view);
        }
        if (mAutoPlayAble && mViews.size() < 3) {
            mHackyViews = new ArrayList<>(mViews);
            SimpleDraweeView view = new SimpleDraweeView(context);
            mHackyViews.add(view);
            if (mHackyViews.size() == 2) {
                SimpleDraweeView v = new SimpleDraweeView(context);
                mHackyViews.add(v);
            }
        }
        setData(mViews, models, tips);
    }

    public void setData(List<View> views, List<? extends Object> models, List<String> tips) {
        if (mAutoPlayAble && views.size() < 3 && mHackyViews == null) {
            mAutoPlayAble = false;
        }
        mModels = models;
        mViews = views;
        mTips = tips;
        initIndicator();
        initViewPager();
        removePlaceholder();
    }

    /**
     * 获取广告页面总个数
     *
     * @return
     */
    public int getItemCount() {
        return mViews == null ? 0 : mViews.size();
    }

    private void initViewPager() {
        if (mViewPager != null && this.equals(mViewPager.getParent())) {
            this.removeView(mViewPager);
            mViewPager = null;
        }

        mViewPager = new GuideBasePager(getContext());
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(new PageAdapter());
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOverScrollMode(mOverScrollMode);
        mViewPager.setAllowUserScrollable(mAllowUserScrollable);
        mViewPager.setPageTransformer(true, BGAPageTransformer.getPageTransformer(mTransitionEffect));


        addView(mViewPager, 0, new LayoutParams(RMP, RMP));
        setPageChangeDuration(mPageChangeDuration);

        if (mEnterView != null || mSkipView != null) {
            mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (position == getItemCount() - 2) {
                        if (mEnterView != null) {
                            ViewCompat.setAlpha(mEnterView, positionOffset);
                        }
                        if (mSkipView != null) {
                            ViewCompat.setAlpha(mSkipView, 1.0f - positionOffset);
                        }

                        if (positionOffset > 0.5f) {
                            if (mEnterView != null) {
                                mEnterView.setVisibility(View.VISIBLE);
                            }
                            if (mSkipView != null) {
                                mSkipView.setVisibility(View.GONE);
                            }
                        } else {
                            if (mEnterView != null) {
                                mEnterView.setVisibility(View.GONE);
                            }
                            if (mSkipView != null) {
                                mSkipView.setVisibility(View.VISIBLE);
                            }
                        }
                    } else if (position == getItemCount() - 1) {
                        if (mSkipView != null) {
                            mSkipView.setVisibility(View.GONE);
                        }
                        if (mEnterView != null) {
                            mEnterView.setVisibility(View.VISIBLE);
                            ViewCompat.setAlpha(mEnterView, 1.0f);
                        }
                    } else {
                        if (mSkipView != null) {
                            mSkipView.setVisibility(View.VISIBLE);
                            ViewCompat.setAlpha(mSkipView, 1.0f);
                        }
                        if (mEnterView != null) {
                            mEnterView.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }

        if (mAutoPlayAble) {
            mViewPager.setAutoPlayDelegate(this);

            int zeroItem = Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2) % mViews.size();
            mViewPager.setCurrentItem(zeroItem);

            startAutoPlay();
        } else {
            switchToPoint(0);
        }
    }

    public void startAutoPlay() {
        stopAutoPlay();
        if (mAutoPlayAble) {
            postDelayed(mAutoPlayTask, mAutoPlayInterval);
        }
    }

    /**
     * 切换到下一页
     */
    private void switchToNextPage() {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        }
    }


    public void stopAutoPlay() {
        if (mAutoPlayAble) {
            removeCallbacks(mAutoPlayTask);
        }
    }

    private static class AutoPlayTask implements Runnable {
        private final WeakReference<GuideViewPager> mBanner;

        private AutoPlayTask(GuideViewPager banner) {
            mBanner = new WeakReference<>(banner);
        }

        @Override
        public void run() {
            GuideViewPager banner = mBanner.get();
            if (banner != null) {
                banner.switchToNextPage();
                banner.startAutoPlay();
            }
        }
    }

    private void switchToPoint(int newCurrentPoint) {
        if (mPointRealContainerLl != null && mViews != null && (mIsNeedShowIndicatorOnOnlyOnePage || (!mIsNeedShowIndicatorOnOnlyOnePage && mViews.size() > 1))) {
            for (int i = 0; i < mPointRealContainerLl.getChildCount(); i++) {
                mPointRealContainerLl.getChildAt(i).setEnabled(false);
            }
            mPointRealContainerLl.getChildAt(newCurrentPoint).setEnabled(true);
        }

        if (mTipTv != null && mTips != null) {
            mTipTv.setText(mTips.get(newCurrentPoint));
        }

        if (mNumberIndicatorTv != null && mViews != null && (mIsNeedShowIndicatorOnOnlyOnePage || (!mIsNeedShowIndicatorOnOnlyOnePage && mViews.size() > 1))) {
            mNumberIndicatorTv.setText((newCurrentPoint + 1) + "/" + mViews.size());
        }
    }

    @Override
    public void handleAutoPlayActionUpOrCancel(float xVelocity) {
        if (mViewPager != null && mPageScrollPosition < mViewPager.getCurrentItem()) {
            // 往右滑
            if (xVelocity > VEL_THRESHOLD || (mPageScrollPositionOffset < 0.7f && xVelocity > -VEL_THRESHOLD)) {
                mViewPager.setBannerCurrentItemInternal(mPageScrollPosition);
            } else {
                mViewPager.setBannerCurrentItemInternal(mPageScrollPosition + 1);
            }
        } else {
            // 往左滑
            if (xVelocity < -VEL_THRESHOLD || (mPageScrollPositionOffset > 0.3f && xVelocity < VEL_THRESHOLD)) {
                mViewPager.setBannerCurrentItemInternal(mPageScrollPosition + 1);
            } else {
                mViewPager.setBannerCurrentItemInternal(mPageScrollPosition);
            }
        }
    }


    private class PageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mViews == null ? 0 : (mAutoPlayAble ? Integer.MAX_VALUE : mViews.size());
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final int finalPosition = position % mViews.size();

            View view = null;
            if (mHackyViews == null) {
                view = mViews.get(finalPosition);
            } else {
                view = mHackyViews.get(position % mHackyViews.size());
            }

            if (container.equals(view.getParent())) {
                container.removeView(view);
            }

            if (mOnItemClickListener != null) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemClickListener.onBannerItemClick(GuideViewPager.this, view, mModels == null ? null : mModels.get(finalPosition), finalPosition);
                    }
                });
            }

            if (mAdapter != null) {
                mAdapter.fillBannerItem(GuideViewPager.this, view, mModels == null ? null : mModels.get(finalPosition), finalPosition);
            }

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }


    /**
     * 设置页码切换过程的时间长度
     *
     * @param duration 页码切换过程的时间长度
     */
    public void setPageChangeDuration(int duration) {
        if (duration >= 0 && duration <= 2000) {
            mPageChangeDuration = duration;
            if (mViewPager != null) {
                mViewPager.setPageChangeDuration(duration);
            }
        }
    }

    private void initIndicator() {
        if (mPointRealContainerLl != null) {
            mPointRealContainerLl.removeAllViews();

            if (mIsNeedShowIndicatorOnOnlyOnePage || (!mIsNeedShowIndicatorOnOnlyOnePage && mViews.size() > 1)) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LWC, LWC);
                lp.setMargins(mPointLeftRightMargin, mPointTopBottomMargin, mPointLeftRightMargin, mPointTopBottomMargin);
                ImageView imageView;
                for (int i = 0; i < mViews.size(); i++) {
                    imageView = new ImageView(getContext());
                    imageView.setLayoutParams(lp);
                    imageView.setImageResource(mPointDrawableResId);
                    mPointRealContainerLl.addView(imageView);
                }
            }
        }
        if (mNumberIndicatorTv != null) {
            if (mIsNeedShowIndicatorOnOnlyOnePage || (!mIsNeedShowIndicatorOnOnlyOnePage && mViews.size() > 1)) {
                mNumberIndicatorTv.setVisibility(View.VISIBLE);
            } else {
                mNumberIndicatorTv.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void removePlaceholder() {
        if (mPlaceholderIv != null && this.equals(mPlaceholderIv.getParent())) {
            removeView(mPlaceholderIv);
            mPlaceholderIv = null;
        }
    }

    /**
     * 设置进入按钮和跳过按钮控件
     *
     * @param enterView 进入按钮控件
     * @param skipView  跳过按钮控件
     */
    public void setEnterViewAndSkipView(View enterView, View skipView) {
        mEnterView = enterView;
        mSkipView = skipView;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mPageScrollPosition = position;
        mPageScrollPositionOffset = positionOffset;
        if (mTipTv != null && mTips != null) {
            if (positionOffset > 0.5) {
                mTipTv.setText(mTips.get((position + 1) % mTips.size()));
                ViewCompat.setAlpha(mTipTv, positionOffset);
            } else {
                ViewCompat.setAlpha(mTipTv, 1 - positionOffset);
                mTipTv.setText(mTips.get(position % mTips.size()));
            }
        }

        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrolled(position % mViews.size(), positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        position = position % mViews.size();
        switchToPoint(position);

        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrollStateChanged(state);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOverScrollMode(int overScrollMode) {
        mOverScrollMode = overScrollMode;
        if (mViewPager != null) {
            mViewPager.setOverScrollMode(mOverScrollMode);
        }
    }

    public interface OnItemClickListener {
        void onBannerItemClick(GuideViewPager pager, View view, Object model, int position);
    }

    public interface Adapter {
        void fillBannerItem(GuideViewPager pager, View view, Object model, int position);
    }
}
