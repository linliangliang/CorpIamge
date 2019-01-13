package com.zhengyuan.cropimageutil;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.yalantis.ucrop.UCrop;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mUcrop = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.btn_UCrop:
                intent = new Intent(this, UCropActivity.class);
                startActivity(intent);
                break;
        }
    }


    private void initView() {
        mUcrop = findViewById(R.id.btn_UCrop);
    }

    private void initEvent() {
        mUcrop.setOnClickListener(this);
    }
}
