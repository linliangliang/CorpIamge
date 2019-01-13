package com.zhengyuan.cropimageutil;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.hmy.popwindow.PopItemAction;
import com.hmy.popwindow.PopWindow;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by 林亮 on 2019/1/13
 */

public class UCropActivity extends Activity implements View.OnClickListener {

    private ImageView avatarImage = null;
    private final static String CROPIMAGEROOT = Environment.getExternalStorageDirectory() + "/myxmpp/";

    public final static int REQUEST_PICTURE_CHOOSE = 1;
    public final static int REQUEST_CAMERA_IMAGE = 2;
    public final static int REQUEST_CROP_IMAGE = 3;

    public static File mPictureFile;//照相产生的临时图片
    private static Uri imageUri = null;//拍照后，照片的Uri

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ucrop);
        //严格模式，解决7.0拍照问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }
        init();
        initView();
        initEvent();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.avatarImage:
                showPopWindow();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            //相册
            case REQUEST_PICTURE_CHOOSE:
                if (resultCode == RESULT_OK) {
                    Uri sourceUri = data.getData();
                    startUCrop(sourceUri);

                } else {
                    Toast.makeText(this, "选图失败！", Toast.LENGTH_SHORT).show();
                }
                break;
            //照相
            case REQUEST_CAMERA_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        startUCrop(imageUri);
                    } else {
                        Toast.makeText(this, "拍照失败！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            //裁剪后的效果
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    Uri resultUri = UCrop.getOutput(data);
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                        avatarImage.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            //错误裁剪的结果
            case UCrop.RESULT_ERROR:
                if (resultCode == RESULT_OK) {
                    final Throwable cropError = UCrop.getError(data);
                    handleCropError(cropError);
                }
                break;
        }
    }

    private void init() {
        //获取权限
        getPermission();
        //常见app根目录
        makeDir();
    }

    private void initView() {
        avatarImage = findViewById(R.id.avatarImage);
    }

    private void initEvent() {
        avatarImage.setOnClickListener(this);
    }

    /**
     * 从相册选择图片
     *
     * @param activity
     */
    public static void choosePhoto(Activity activity) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        activity.startActivityForResult(intent, REQUEST_PICTURE_CHOOSE);
    }

    /**
     * 打开相机拍照
     *
     * @param activity
     * @return
     */
    public static void openCamera(Activity activity) {
        mPictureFile = new File(CROPIMAGEROOT, "_" + System.currentTimeMillis() + ".jpg");
        imageUri = Uri.fromFile(mPictureFile);
        // 启动拍照,并保存到临时文件
        Intent mIntent = new Intent();
        mIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        mIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPictureFile));
        mIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
        activity.startActivityForResult(mIntent, REQUEST_CAMERA_IMAGE);
    }

    /**
     * popwindow选择拍照还是选择图片
     */
    private void showPopWindow() {
        PopWindow popWindow = new PopWindow.Builder(this)
                .setStyle(PopWindow.PopWindowStyle.PopUp)
                .addItemAction(new PopItemAction("选择照片", PopItemAction.PopItemStyle.Normal, new PopItemAction.OnClickListener() {
                    @Override
                    public void onClick() {
                        choosePhoto(UCropActivity.this);
                    }
                }))
                .addItemAction(new PopItemAction("拍照", PopItemAction.PopItemStyle.Warning, new PopItemAction.OnClickListener() {
                    @Override
                    public void onClick() {
                        openCamera(UCropActivity.this);
                    }
                }))
                .addItemAction(new PopItemAction("取消", PopItemAction.PopItemStyle.Cancel))
                .create();
        popWindow.show();
    }


    /**
     * 裁剪图片
     *
     * @param sourceUri
     */
    private void startUCrop(Uri sourceUri) {
        UCrop.Options options = new UCrop.Options();
        //裁剪后图片保存在文件夹中
        Uri destinationUri = Uri.fromFile(new File(CROPIMAGEROOT, "uCrop.jpg"));
        UCrop uCrop = UCrop.of(sourceUri, destinationUri);//第一个参数是裁剪前的uri,第二个参数是裁剪后的uri
        uCrop.withAspectRatio(1, 1);//设置裁剪框的宽高比例
        //下面参数分别是缩放,旋转,裁剪框的比例
        options.setAllowedGestures(com.yalantis.ucrop.UCropActivity.ALL, com.yalantis.ucrop.UCropActivity.NONE, com.yalantis.ucrop.UCropActivity.ALL);
        options.setToolbarTitle("移动和缩放");//设置标题栏文字
        options.setCropGridStrokeWidth(2);//设置裁剪网格线的宽度(我这网格设置不显示，所以没效果)
        //options.setCropFrameStrokeWidth(1);//设置裁剪框的宽度
        options.setMaxScaleMultiplier(3);//设置最大缩放比例
        //options.setHideBottomControls(true);//隐藏下边控制栏
        options.setShowCropGrid(true);  //设置是否显示裁剪网格
        //options.setOvalDimmedLayer(true);//设置是否为圆形裁剪框
        options.setShowCropFrame(true); //设置是否显示裁剪边框(true为方形边框)
        options.setToolbarWidgetColor(Color.parseColor("#ffffff"));//标题字的颜色以及按钮颜色
        options.setDimmedLayerColor(Color.parseColor("#AA000000"));//设置裁剪外颜色
        options.setToolbarColor(Color.parseColor("#000000")); // 设置标题栏颜色
        options.setStatusBarColor(Color.parseColor("#000000"));//设置状态栏颜色
        options.setCropGridColor(Color.parseColor("#ffffff"));//设置裁剪网格的颜色
        options.setCropFrameColor(Color.parseColor("#ffffff"));//设置裁剪框的颜色
        uCrop.withOptions(options);
        /*//裁剪后保存到文件中
        Uri destinationUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/myxmpp/" + "test1.jpg"));
        UCrop uCrop = UCrop.of(sourceUri, destinationUri);
        UCrop.Options options = new UCrop.Options();
        //设置裁剪图片可操作的手势
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        //设置toolbar颜色
        options.setToolbarColor(ActivityCompat.getColor(this, R.color.orange2));
        //设置状态栏颜色
        options.setStatusBarColor(ActivityCompat.getColor(this, R.color.orange2));
        //是否能调整裁剪框
        options.setFreeStyleCropEnabled(true);
        options.setToolbarWidgetColor(Color.parseColor("#ffffff"));//标题字的颜色以及按钮颜色
        options.setDimmedLayerColor(Color.parseColor("#AA000000"));//设置裁剪外颜色
        options.setToolbarColor(Color.parseColor("#000000")); // 设置标题栏颜色
        options.setStatusBarColor(Color.parseColor("#000000"));//设置状态栏颜色
        options.setCropGridColor(Color.parseColor("#ffffff"));//设置裁剪网格的颜色
        options.setCropFrameColor(Color.parseColor("#ffffff"));//设置裁剪框的颜色
        //options.setShowCropFrame(false); //设置是否显示裁剪边框(true为方形边框)

        uCrop.withOptions(options);*/
        uCrop.start(this);
    }


    //处理剪切失败的返回值
    private void handleCropError(Throwable cropError) {
        deleteTempPhotoFile();
        if (cropError != null) {
            Toast.makeText(UCropActivity.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(UCropActivity.this, "无法剪切选择图片", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 删除拍照临时文件
     */
    private void deleteTempPhotoFile() {
        File tempFile = new File(Environment.getExternalStorageDirectory() + File.separator + "output_iamge.jpg");
        if (tempFile.exists() && tempFile.isFile()) {
            tempFile.delete();
        }
    }

    //动态申请权限
    private void getPermission() {
        PackageManager pm = getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.CAMERA", "com.zhengyuan.cropimageutil"));
        if (permission) {
            //"有这个权限"
            //Toast.makeText(Carout.this, "有权限", Toast.LENGTH_SHORT).show();
        } else {
            //"木有这个权限"
            //如果android版本大于等于6.0，权限需要动态申请
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 15);
            }
        }
        permission = PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", "com.zhengyuan.cropimageutil");
        if (permission) {
            //"有这个权限"
            //Toast.makeText(Carout.this, "有权限", Toast.LENGTH_SHORT).show();
        } else {
            //"木有这个权限"
            //如果android版本大于等于6.0，权限需要动态申请
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 15);
            }
        }
        permission = PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.READ_EXTERNAL_STORAGE", "com.zhengyuan.cropimageutil");
        if (permission) {
            //"有这个权限"
            //Toast.makeText(Carout.this, "有权限", Toast.LENGTH_SHORT).show();
        } else {
            //"木有这个权限"
            //如果android版本大于等于6.0，权限需要动态申请
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 15);
            }
        }
    }


    private void makeDir() {
        File file = new File(CROPIMAGEROOT);
        if (!file.exists()) {
            file.mkdir();
        }
    }

}
