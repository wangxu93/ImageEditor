package com.yjing.imageeditlibrary.editimage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yjing.imageeditlibrary.BaseActivity;
import com.yjing.imageeditlibrary.R;
import com.yjing.imageeditlibrary.utils.BitmapUtils;

import java.io.File;

/**
 * Created by wangxu on 2018/5/26.
 */

public class PaletteActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "MainActivity";
    public static final String FIELD_INPUT_URI = "inputUrl";
    public static final String FIELD_OUTPUT_URI = "outputUrl";
    public static final int RESULT_CODE_OUTPUT_URL = 0x00FF;
    private ImageView ivLogo;
    private AppCompatSeekBar sbHum;
    private AppCompatSeekBar sbSatura;
    private AppCompatSeekBar sbLum;
    private AppCompatSeekBar sbDefinit;
    private static final float MIDDLE_VALUE = 50;
    private Bitmap mBitmap;
    private TextView tvHum;
    private TextView tvSatura;
    private TextView tvLum;
    private String mOutPath;
    private File mInputFile;
    private View tvBack;
    private View tvFinish;
    private View llDefinit;
    private ImageView ivDefinit;
    private TextView tvDefinit;
    private View llSatura;
    private ImageView ivSatura;
    private View llLum;
    private ImageView ivLum;
    private View llHum;
    private ImageView ivHum;
    private ColorMatrixColorFilter mColorFilter;
    private Animation mAnim_In;
    private Animation mAnim_out;
    private View llBottom;
    private View titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette);
        if (!initParams()) {
            finish();
            return;
        }
        initView();
        initListener();
        initData();
        initAnim();
    }

    private void initAnim() {
        mAnim_In = AnimationUtils.loadAnimation(this, R.anim.anim_in);
        mAnim_out = AnimationUtils.loadAnimation(this, R.anim.anim_out);
    }

    private boolean initParams() {
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        if (data == null) {
            return false;
        }
        Uri uri = data.getParcelable(FIELD_INPUT_URI);
        if (uri == null) {
            return false;
        }
        String path = uri.getSchemeSpecificPart();
        mInputFile = new File(path);
        if (!mInputFile.exists()) {
            return false;
        }

        Uri outUri = data.getParcelable(FIELD_OUTPUT_URI);
        if (outUri != null) {
            mOutPath = outUri.getSchemeSpecificPart();
        } else {
            mOutPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        }
        mOutPath += getOutPutFileName();
        return true;
    }

    private void initData() {
        mBitmap = BitmapFactory.decodeFile(mInputFile.getAbsolutePath());
        ivLogo.setImageBitmap(mBitmap);
    }

    private void initListener() {
        sbHum.setOnSeekBarChangeListener(this);
        sbSatura.setOnSeekBarChangeListener(this);
        sbLum.setOnSeekBarChangeListener(this);
        sbDefinit.setOnSeekBarChangeListener(this);
        sbLum.setMax(200);
        sbLum.setProgress(100);

        llHum.setOnClickListener(new ClickListener());
        llSatura.setOnClickListener(new ClickListener());
        llLum.setOnClickListener(new ClickListener());
        llDefinit.setOnClickListener(new ClickListener());

        tvBack.setOnClickListener(new ClickListener());
        tvFinish.setOnClickListener(new ClickListener());
        ivLogo.setOnClickListener(new ClickListener());
    }

    private void setViewAnim(View v, int vis) {
        if (vis == View.GONE) {
            v.startAnimation(mAnim_out);
        } else {
            v.startAnimation(mAnim_In);
        }
    }

    private void initView() {
        llBottom = findViewById(R.id.llBottom);
        titleBar = findViewById(R.id.titleBar);
        ivLogo = ((ImageView) findViewById(R.id.ivLogo));
        sbHum = ((AppCompatSeekBar) findViewById(R.id.sbHum));
        sbSatura = ((AppCompatSeekBar) findViewById(R.id.sbSatura));
        sbLum = ((AppCompatSeekBar) findViewById(R.id.sbLum));
        sbDefinit = ((AppCompatSeekBar) findViewById(R.id.sbDefinit));

        tvBack = findViewById(R.id.back_btn);
        tvFinish = findViewById(R.id.save_btn);

        llDefinit = findViewById(R.id.llDefinit);
        ivDefinit = ((ImageView) findViewById(R.id.ivDefinit));
        tvDefinit = ((TextView) findViewById(R.id.tvDefinit));

        llSatura = findViewById(R.id.llSatura);
        ivSatura = ((ImageView) findViewById(R.id.ivSatura));
        tvSatura = findViewById(R.id.tvSatura);

        llLum = findViewById(R.id.llLum);
        ivLum = ((ImageView) findViewById(R.id.ivLum));
        tvLum = findViewById(R.id.tvLum);

        llHum = findViewById(R.id.llHum);
        ivHum = ((ImageView) findViewById(R.id.ivHum));
        tvHum = findViewById(R.id.tvHum);

        showKindsOfSeekbar(llLum.getId());
    }

    private String getOutPutFileName() {
        return System.currentTimeMillis() + ".png";
    }

    private void imageEffect(float hue, float saturation, float lum) {

        //亮度
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(new float[]{
                1, 0, 0, 0, lum,
                0, 1, 0, 0, lum,
                0, 0, 1, 0, lum,
                0, 0, 0, 1, 0});
        //对比度
        ColorMatrix lumColorMatrix = new ColorMatrix();
        lumColorMatrix.setScale(hue, hue, hue, 1);

        //饱和度
        ColorMatrix satureColorMatrix = new ColorMatrix();
        satureColorMatrix.setSaturation(saturation);


        ColorMatrix totalMatrix = new ColorMatrix();
        totalMatrix.postConcat(colorMatrix);
        totalMatrix.postConcat(satureColorMatrix);
        totalMatrix.postConcat(lumColorMatrix);
        String str = "";
        for (float v : totalMatrix.getArray()) {
            str = str + v + ",";
        }

        mColorFilter = new ColorMatrixColorFilter(totalMatrix);
        ivLogo.setColorFilter(mColorFilter);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float saturaValue = getClacValue(sbSatura.getProgress());
        float humValue = getHumValue(sbHum.getProgress());
        float lumValue = getlumValue(sbLum.getProgress());
        Log.i(TAG, "onProgressChanged: stature:" + saturaValue + "   humValue:" + humValue + "    lumValue:" + lumValue);
        imageEffect(humValue, saturaValue, lumValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private float getHumValue(int progress) {
        return progress / 100.0f + 0.5f;
    }

    private float getlumValue(int progress) {
        return progress - 100;
    }

    private float getClacValue(int progress) {
        return progress / MIDDLE_VALUE;
    }

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.back_btn) {
                onBackPressed();
            } else if (id == R.id.save_btn) {
                onSaveImage();
            } else if(id == R.id.ivLogo){
                setMenuVisibity();
            }else {
                showKindsOfSeekbar(v.getId());
            }
        }
    }
    private boolean showMenu = true;
    private void setMenuVisibity() {
        if (showMenu) {
            llBottom.setVisibility(View.GONE);
            setViewAnim(llBottom,View.GONE);
            titleBar.setVisibility(View.GONE);
            setViewAnim(titleBar,View.GONE);
        }else{
            llBottom.setVisibility(View.VISIBLE);
            setViewAnim(llBottom,View.VISIBLE);
            titleBar.setVisibility(View.VISIBLE);
            setViewAnim(titleBar,View.VISIBLE);
        }
        showMenu = !showMenu;
    }

    private void onSaveImage() {
        new SaveImageTask().execute();
    }


    private void showKindsOfSeekbar(int id) {
        if (id == R.id.llHum) {
            sbLum.setVisibility(View.GONE);
            sbSatura.setVisibility(View.GONE);
            sbDefinit.setVisibility(View.GONE);
            sbHum.setVisibility(View.VISIBLE);
            ivDefinit.setEnabled(false);
            ivHum.setEnabled(true);
            ivLum.setEnabled(false);
            ivSatura.setEnabled(false);

            tvDefinit.setTextColor(Color.WHITE);
            tvHum.setTextColor(Color.parseColor("#0099ff"));
            tvLum.setTextColor(Color.WHITE);
            tvSatura.setTextColor(Color.WHITE);
        } else if (id == R.id.llLum) {
            sbSatura.setVisibility(View.GONE);
            sbHum.setVisibility(View.GONE);
            sbDefinit.setVisibility(View.GONE);
            sbLum.setVisibility(View.VISIBLE);
            ivDefinit.setEnabled(false);
            ivHum.setEnabled(false);
            ivLum.setEnabled(true);
            ivSatura.setEnabled(false);
            tvDefinit.setTextColor(Color.WHITE);
            tvHum.setTextColor(Color.WHITE);
            tvLum.setTextColor(Color.parseColor("#0099ff"));
            tvSatura.setTextColor(Color.WHITE);
        } else if (id == R.id.llSatura) {
            sbHum.setVisibility(View.GONE);
            sbLum.setVisibility(View.GONE);
            sbDefinit.setVisibility(View.GONE);
            sbSatura.setVisibility(View.VISIBLE);
            ivDefinit.setEnabled(false);
            ivHum.setEnabled(false);
            ivLum.setEnabled(false);
            ivSatura.setEnabled(true);

            tvDefinit.setTextColor(Color.WHITE);
            tvHum.setTextColor(Color.WHITE);
            tvLum.setTextColor(Color.WHITE);
            tvSatura.setTextColor(Color.parseColor("#0099ff"));
        } else if (id == R.id.llDefinit) {
            sbHum.setVisibility(View.GONE);
            sbLum.setVisibility(View.GONE);
            sbSatura.setVisibility(View.GONE);
            sbDefinit.setVisibility(View.VISIBLE);
            ivDefinit.setEnabled(true);
            ivHum.setEnabled(false);
            ivLum.setEnabled(false);
            ivSatura.setEnabled(false);

            tvDefinit.setTextColor(Color.parseColor("#0099ff"));
            tvHum.setTextColor(Color.WHITE);
            tvLum.setTextColor(Color.WHITE);
            tvSatura.setTextColor(Color.WHITE);
        }
    }

    public static void launch(Activity context, Bundle bundle) {
        Intent intent = new Intent(context, PaletteActivity.class);
        intent.putExtras(bundle);
        context.startActivityForResult(intent, RESULT_CODE_OUTPUT_URL);
    }

    private class SaveImageTask extends AsyncTask<Bitmap, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Boolean doInBackground(Bitmap... bitmaps) {
            Bitmap mCopyBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(mCopyBitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColorFilter(mColorFilter);
            canvas.drawBitmap(mBitmap, 0, 0, paint);
            boolean b = BitmapUtils.saveBitmap(mCopyBitmap, mOutPath);
            mCopyBitmap.recycle();
            mCopyBitmap = null;
            return b;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                buildResult();
                finish();
            } else {
                Toast.makeText(PaletteActivity.this, "保存图片出现问题", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void buildResult() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable(FIELD_OUTPUT_URI, Uri.fromFile(new File(mOutPath)));
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
    }
}
