package com.iss.innoz.tinkerdemo.ui.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.iss.innoz.tinkerdemo.R;
import com.iss.innoz.tinkerdemo.app.AppManagers;
import com.iss.innoz.tinkerdemo.app.RequestManager;
import com.iss.innoz.tinkerdemo.event.FEvent;
import com.iss.innoz.tinkerdemo.event.StopEvent;
import com.iss.innoz.tinkerdemo.utils.BusProvider;
import com.iss.innoz.tinkerdemo.utils.Toastor;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.List;

import butterknife.ButterKnife;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * TinkerDemo
 * com.iss.innoz.tinkerdemo.ui.base
 *
 * @Author: xie
 * @Time: 2016/12/9 9:45
 * @Description:
 */


public abstract class IBaseActivity extends BaseActivity implements RequestManager.OnRequestListener {
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private TextView tittle_text;
    private ProgressDialog progressDialog;
    private SystemBarTintManager tintManager;
    private ViewStub stub;
    private ImageView leftImg, rightImg, titleImg;
    private SimpleDraweeView titleHead;
    private TextView leftTv, rightTv;
    protected Toastor toastor;
    private FrameLayout content;
    private View.OnClickListener leftClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AppManagers.getActivitiesManager().addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_base);
        getContentView();
        ButterKnife.bind(this);
        BusProvider.register(this);
        toastor = AppManagers.getToastor();
        AppManagers.getRequestManager().addOnRequestListener(this);
        startWork(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager == null) {
            AppManagers.getActivitiesManager().finishActivity();
        } else {
            if (fragmentManager.getBackStackEntryCount() == 0) {
                AppManagers.getActivitiesManager().finishActivity();
            } else {
                fragmentManager.popBackStack();
            }
        }
        super.onBackPressed();
    }

    private void getContentView() {
        content = (FrameLayout) findViewById(R.id.content_frame);
        content.addView(LayoutInflater.from(this).inflate(getContentResource(), null));
    }

    @Override
    protected void onDestroy() {
        AppManagers.getRequestManager().removeOnRequestListener(this);
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null && fragments.size() != 0) {
            for (Fragment fragment : fragments) fragment = null;
        }
        subscriptions.clear();
        AppManagers.getActivitiesManager().removeActivity(this);
        stopWork();
        BusProvider.unregister(this);
        super.onDestroy();
    }

    private void headerInit() {
        stub = ((ViewStub) findViewById(R.id.vs_title));
        stub.inflate();
        tittle_text = (TextView) findViewById(R.id.tittle_text);
    }

    protected void setTittleText(int resId) {
        setTittleText(getString(resId));
    }

    protected void setTittleText(String title) {
        if (tittle_text == null)
            headerInit();
        if (title == null) {
            stub.setVisibility(View.GONE);
            if (tintManager == null)
                tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.colorPrimary);
        } else {
            if (tintManager != null)
                tintManager.setStatusBarTintEnabled(false);
            stub.setVisibility(View.VISIBLE);
            tittle_text.setText(title);
        }
    }

    protected void setLeftView(int visibility) {
        if (leftImg == null) {
            leftImg = (ImageView) findViewById(R.id.tittle_logo_img);
            leftTv = (TextView) findViewById(R.id.tittle_left_text);
        }
        if (View.VISIBLE == visibility) {
            leftImg.setImageResource(R.mipmap.back);
            leftTv.setText(R.string.title_back);
        } else {
            leftImg.setVisibility(visibility);
            leftTv.setVisibility(visibility);
        }
        leftClick = v -> onBackPressed();
        leftTv.setOnClickListener(leftClick);
        leftImg.setOnClickListener(leftClick);
    }

    public View.OnClickListener getLeftClick() {
        return leftClick;
    }

    public void setTitleHead(int visibility, String url) {
        if (titleHead == null)
            titleHead = (SimpleDraweeView) findViewById(R.id.sdv_title_head);
        titleHead.setVisibility(visibility);
        titleHead.setImageURI(url);
    }

    public void setTitleImg(int visibility, int id, View.OnClickListener onClickListener) {
        if (titleImg == null)
            titleImg = (ImageView) findViewById(R.id.img_title_right);
        titleImg.setVisibility(View.VISIBLE);
        titleImg.setImageResource(id);
        titleImg.setOnClickListener(onClickListener);
    }

    private void initRightView() {
        rightImg = (ImageView) findViewById(R.id.tittle_img_drawer);
        rightTv = (TextView) findViewById(R.id.tittle_login_text);
    }

    protected void setRightView(int rightId, String name, View.OnClickListener onClickListener) {
        if (rightImg == null) {
            initRightView();
        }
        rightImg.setImageResource(rightId);
        rightImg.setOnClickListener(onClickListener);
        rightTv.setText(name);
        rightTv.setOnClickListener(onClickListener);
    }

    protected View getRightView() {
        if (rightImg == null) {
            initRightView();
        }
        return rightImg;
    }

    public void htttpRequest(Subscription subscription) {
        if (subscription != null) {
            subscriptions.add(subscription);
            showProgressDialog();
        }
    }

    @Override
    public void onStop(StopEvent e) {
        dissmissProgressDialog();
    }

    @Override
    public void onError(FEvent e) {
        dissmissProgressDialog();
    }


    protected void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.process_dialog_message));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        } else {
            if (progressDialog.isShowing())
                progressDialog.cancel();
            progressDialog.show();
        }
    }

    protected void dissmissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

}
