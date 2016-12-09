package com.iss.innoz.tinkerdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.iss.innoz.tinkerdemo.ui.base.IBaseActivity;
import com.iss.innoz.tinkerdemo.ui.guide.GuideActivity;

public class MainActivity extends IBaseActivity {

    @Override
    protected int getContentResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void startWork(Bundle savedInstanceState) {
        setTittleText("首页");

    }

    @Override
    protected void stopWork() {

    }

    public void guide(View v) {
        startActivity(new Intent(this, GuideActivity.class));
    }

}
