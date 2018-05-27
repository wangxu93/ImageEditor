package com.yjing.imageeditlibrary.editimage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
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
    private static final float MIDDLE_VALUE = 50;
    private Bitmap mBitmap;
    private Bitmap mCopyBitmap;
    private View tvHum;
    private View tvSatura;
    private View tvLum;
    private String mOutPath;
    private File mInputFile;
    private View tvBack;
    private View tvFinish;

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
        }else{
            mOutPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        mOutPath += getOutPutFileName();
        return true;
    }

    private void initData() {
        mBitmap = BitmapFactory.decodeFile(mInputFile.getAbsolutePath());
        mCopyBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.RGB_565);
        ivLogo.setImageBitmap(mBitmap);
    }

    private void initListener() {
        sbHum.setOnSeekBarChangeListener(this);
        sbSatura.setOnSeekBarChangeListener(this);
        sbLum.setOnSeekBarChangeListener(this);

        tvHum.setOnClickListener(new ClickListener());
        tvSatura.setOnClickListener(new ClickListener());
        tvLum.setOnClickListener(new ClickListener());
        tvBack.setOnClickListener(new ClickListener());
        tvFinish.setOnClickListener(new ClickListener());
    }

    private void initView() {
        ivLogo = ((ImageView) findViewById(R.id.ivLogo));
        sbHum = ((AppCompatSeekBar) findViewById(R.id.sbHum));
        sbSatura = ((AppCompatSeekBar) findViewById(R.id.sbSatura));
        sbLum = ((AppCompatSeekBar) findViewById(R.id.sbLum));

        tvBack = findViewById(R.id.tvBack);
        tvFinish = findViewById(R.id.tvFinish);

        tvHum = findViewById(R.id.tvHum);
        tvSatura = findViewById(R.id.tvSatura);
        tvLum = findViewById(R.id.tvLum);

        showKindsOfSeekbar(tvHum.getId());
    }

    private String getOutPutFileName(){
        return System.currentTimeMillis()+".png";
    }

    private void imageEffect(Bitmap bitmap, float hue, float saturation, float lum) {

        Canvas canvas = new Canvas(mCopyBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        //色相
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setRotate(0, hue);
        colorMatrix.setRotate(1, hue);
        colorMatrix.setRotate(2, hue);

        //饱和度
        ColorMatrix satureColorMatrix = new ColorMatrix();
        satureColorMatrix.setSaturation(saturation);

        //亮度
        ColorMatrix lumColorMatrix = new ColorMatrix();
        lumColorMatrix.setScale(lum, lum, lum, 1);

        ColorMatrix totalMatrix = new ColorMatrix();
        totalMatrix.postConcat(colorMatrix);
        totalMatrix.postConcat(satureColorMatrix);
        totalMatrix.postConcat(lumColorMatrix);

        paint.setColorFilter(new ColorMatrixColorFilter(totalMatrix));

        canvas.drawBitmap(bitmap, 0, 0, paint);

        ivLogo.setImageBitmap(mCopyBitmap);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float saturaValue = getClacValue(sbSatura.getProgress());
        float humValue = getClacValue(sbHum.getProgress());
        float lumValue = getClacValue(sbLum.getProgress());
        Log.i(TAG, "onProgressChanged: stature:" + saturaValue + "   humValue:" + humValue + "    lumValue:" + lumValue);
        imageEffect(mBitmap, humValue, saturaValue, lumValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private float getClacValue(int progress) {
        return progress / MIDDLE_VALUE;
    }

    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.tvBack) {
                onBackPressed();
            }else if(id == R.id.tvFinish){
                onSaveImage();
            }else {
                showKindsOfSeekbar(v.getId());
            }
        }
    }

    private void onSaveImage() {
        new SaveImageTask().execute();
    }


    private void showKindsOfSeekbar(int id) {
        if (id == R.id.tvHum) {
            sbLum.setVisibility(View.GONE);
            sbSatura.setVisibility(View.GONE);
            sbHum.setVisibility(View.VISIBLE);
        } else if (id == R.id.tvLum) {
            sbSatura.setVisibility(View.GONE);
            sbHum.setVisibility(View.GONE);
            sbLum.setVisibility(View.VISIBLE);
        } else if (id == R.id.tvSatura) {
            sbHum.setVisibility(View.GONE);
            sbLum.setVisibility(View.GONE);
            sbSatura.setVisibility(View.VISIBLE);
        }
    }

    public static void launch(Activity context, Bundle bundle) {
        Intent intent = new Intent(context, PaletteActivity.class);
        intent.putExtras(bundle);
        context.startActivityForResult(intent, RESULT_CODE_OUTPUT_URL);
    }

    private class SaveImageTask extends AsyncTask<Bitmap,Void,Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Boolean doInBackground(Bitmap... bitmaps) {
            return  BitmapUtils.saveBitmap(mCopyBitmap,mOutPath);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                onBackPressed();
            }else{
                Toast.makeText(PaletteActivity.this, "保存图片出现问题", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
