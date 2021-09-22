package com.htcy.lightblur;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.huantansheng.easyphotos.utils.permission.PermissionUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ImageView iv_original;
    private TextView tv_original;
    private ImageView iv_blurred;
    private TextView tv_blurred;
    private SeekBar sb_radius;
    private TextView tv_radius;
    private SeekBar sb_scale;
    private TextView tv_scale;
    private CheckBox cb_keep_size;
    private CheckBox cb_real_time;
    private Bitmap mBitmapOriginal;
    private Bitmap mBitmapBlurred;

    private BitmapBlurWrapper mBlurred = null;

    private long start = 0;

    /**
     * 选择的图片集
     */
    private ArrayList<Photo> selectedPhotoList = new ArrayList<>();

    private int color1 = 0;
    private int color2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (PermissionUtil.checkAndRequestPermissionsInActivity(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            preLoadAlbums();
        }

        mBlurred = new BitmapBlurWrapper(MainActivity.this)
                .bitmap(mBitmapOriginal)
                .staySize(cb_keep_size.isChecked())
                .recycleOriginal(false)
                .listenBlurTime(new BitmapBlurWrapper.Listener() {
                    @Override
                    public void begin() {
                        start = System.currentTimeMillis();
                    }

                    @Override
                    public void end() {
                        long end = System.currentTimeMillis();
                        long off = end - start;
                        setInfo(true, off);
                    }
                })
                .scale(1f / sb_scale.getProgress())
                .radius(sb_radius.getProgress());

    }

    private void initView() {
        color1 = Color.parseColor("#33000000");
        color2 = Color.parseColor("#33ff0000");

        iv_original = findViewById(R.id.iv_original);
        tv_original = findViewById(R.id.tv_original);
        iv_blurred = findViewById(R.id.iv_blurred);  //待模糊图片
        tv_blurred = findViewById(R.id.tv_blurred); //所用时间及图片大小
        sb_radius = findViewById(R.id.sb_radius);
        tv_radius = findViewById(R.id.tv_radius);
        sb_scale = findViewById(R.id.sb_scale);
        tv_scale = findViewById(R.id.tv_scale);
        cb_keep_size = findViewById(R.id.cb_keep_size); //保持尺寸
        cb_real_time = findViewById(R.id.cb_real_time);//实时模糊

        cb_keep_size.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBlurred.staySize(isChecked);
            }
        });


        sb_radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_radius.setText("" + progress);
                if (cb_real_time.isChecked()) {
                    mBlurred.radius(sb_radius.getProgress());
                    blurAndUpdateView();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        sb_scale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_scale.setText("" + progress);
                if (cb_real_time.isChecked()) {
                    mBlurred.scale(1f / sb_scale.getProgress());
                    blurAndUpdateView();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        InnerClickListener innerClickListener = new InnerClickListener();
        iv_original.setOnClickListener(innerClickListener);
        iv_blurred.setOnClickListener(innerClickListener);

    }

    class InnerClickListener implements View.OnClickListener {

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            int vId = v.getId();
            switch (vId) {
                default:
                    break;
                case R.id.iv_original:
                    EasyPhotos.createAlbum(MainActivity.this, true, false, GlideEngine.getInstance())
                            .setFileProviderAuthority("com.htcy.lightblur.fileprovider")
                            .start(101);//也可以选择链式调用写法
                    break;
                case R.id.iv_blurred:
                    blurAndUpdateView();
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_real_time_blur) {
            startActivity(new Intent(this, ViewBgBlurActivity.class));
        }
        return true;
    }


    private void blurAndUpdateView() {
        if (mBitmapOriginal == null) return;
        mBlurred.doBlur(new BitmapBlurWrapper.Callback() {
            @Override
            public void down(Bitmap bitmap) {
                mBitmapBlurred = bitmap;
            }
        });
        if (mBitmapBlurred != null) {
            iv_blurred.setImageBitmap(mBitmapBlurred);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBlurred.recycler();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            if (requestCode == 101) {
                selectedPhotoList =
                        data.getParcelableArrayListExtra(EasyPhotos.RESULT_PHOTOS);
                if (selectedPhotoList != null && selectedPhotoList.size() > 0) {
                    mBitmapOriginal = BitmapFactory.decodeFile(selectedPhotoList.get(0).path);
                    iv_original.setImageBitmap(mBitmapOriginal);
                    iv_blurred.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
                    mBlurred.bitmap(mBitmapOriginal);
                    setInfo(false, 0);
                }
            }
        }
    }

    private void setInfo(boolean isBlurred, long time) {
        if (time <= 16) {
            tv_blurred.setBackgroundColor(color1);
        } else {
            tv_blurred.setBackgroundColor(color2);
        }
        Bitmap bitmap;
        TextView textView;
        if (isBlurred) {
            bitmap = mBitmapBlurred;
            textView = tv_blurred;
        } else {
            bitmap = mBitmapOriginal;
            textView = tv_original;
        }
        String info = "";
        if (bitmap != null) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            info = w + "*" + h;
            if (isBlurred) {
                info = info + ", time:" + time + "ms";
            }
        }
        textView.setText(info);
    }

    private void preLoadAlbums() {
        EasyPhotos.preLoad(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionUtil.onPermissionResult(this, permissions, grantResults,
                new PermissionUtil.PermissionCallBack() {
                    @Override
                    public void onSuccess() {
                        preLoadAlbums();
                    }

                    @Override
                    public void onShouldShow() {
                    }

                    @Override
                    public void onFailed() {
                    }
                });
    }
}