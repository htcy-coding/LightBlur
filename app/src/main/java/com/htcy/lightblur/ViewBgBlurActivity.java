package com.htcy.lightblur;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.models.album.entity.Photo;

import java.util.ArrayList;

public class ViewBgBlurActivity extends AppCompatActivity {

    private FrameLayout fl_blurred;
    private TextView tv_fps;
    private TextView tv_mspf;
    private CheckBox cb_anti_alias;
    private CheckBox cb_fit_xy;
    private ImageView iv_original;
    private ImageView iv_blurred;
    private SeekBar sb_radius;
    private TextView tv_radius;
    private SeekBar sb_scale;
    private TextView tv_scale;

    private float lastX = 0;
    private float lastY = 0;
    private float downX = 0;
    private float downY = 0;

//    private PictureSelectorHelper mHelper;
    private ViewBgBlurWrapper mBlurred = null;

    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bg_blur);
        fl_blurred = findViewById(R.id.fl_blurred);
        tv_fps = findViewById(R.id.tv_fps);
        tv_mspf = findViewById(R.id.tv_mspf);
        cb_anti_alias = findViewById(R.id.cb_anti_alias);
        cb_fit_xy = findViewById(R.id.cb_fit_xy);
        iv_original = findViewById(R.id.iv_original);
        iv_blurred = findViewById(R.id.iv_blurred);
        sb_radius = findViewById(R.id.sb_radius);
        tv_radius = findViewById(R.id.tv_radius);
        sb_scale = findViewById(R.id.sb_scale);
        tv_scale = findViewById(R.id.tv_scale);
        tv_scale = findViewById(R.id.tv_scale);
        frameLayout = findViewById(R.id.fl);

        cb_anti_alias.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mBlurred != null) {
                    mBlurred.antiAlias(isChecked);
                }
            }
        });
        cb_fit_xy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mBlurred != null) {
                    mBlurred.fitIntoViewXY(isChecked);
                }
            }
        });
        sb_radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mBlurred != null) {
                    mBlurred.radius(progress);
                }
                tv_radius.setText("" + progress);
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
                if (mBlurred != null) {
                    mBlurred.scale(1F / (progress <= 0 ? 1 : progress));
                }
                tv_scale.setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        frameLayout.post(new Runnable() {
            @Override
            public void run() {
                mBlurred = ViewBgBlurWrapper.with(ViewBgBlurActivity.this,frameLayout, frameLayout.getWidth(), frameLayout.getHeight())
                        .fitIntoViewXY(cb_fit_xy.isChecked())
                        .antiAlias(cb_anti_alias.isChecked())
                        .backgroundColor(Color.WHITE)
                        .scale(1F / (sb_scale.getProgress() <= 0 ? 1 : sb_scale.getProgress()))
                        .radius(sb_radius.getProgress())
                        .fpsListener(new ViewBgBlurWrapper.FpsListener() {
                            @Override
                            public void currFps(float fps) {
                                tv_fps.setText(String.format("fps%.1f", fps));
                            }
                        })
                        .listener(new ViewBgBlurWrapper.Listener() {
                            @Override
                            public void begin() {
                                start = System.currentTimeMillis();
                            }

                            @Override
                            public void end() {
                                long end = System.currentTimeMillis();
                                long off = end - start;
                                tv_mspf.setText(String.format("mspf%d", off));
                            }
                        });

                mBlurred.doBlur(iv_blurred);
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_real_time_blur, menu);
        return super.onCreateOptionsMenu(menu);
    }
    /**
     * ??????????????????
     */
    private ArrayList<Photo> selectedPhotoList = new ArrayList<>();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_choose) {
            EasyPhotos.createAlbum(ViewBgBlurActivity.this, true, false, GlideEngine.getInstance())
                    .setFileProviderAuthority("com.htcy.lightblur.fileprovider")
                    .start(101);//?????????????????????????????????
        }
        return true;
    }

    private long start = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            if (requestCode == 101) {
                //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                selectedPhotoList =
                        data.getParcelableArrayListExtra(EasyPhotos.RESULT_PHOTOS);
                if (selectedPhotoList != null && selectedPhotoList.size() > 0) {
                    Glide.with(ViewBgBlurActivity.this)
                            .load(selectedPhotoList.get(0).path)
                            .into(iv_original);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBlurred.recycler();
    }


}