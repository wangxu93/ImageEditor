package com.yjing.imageeditandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.ImageView;

/**
 * Created by wangxu on 2018/1/19.
 */

public class TestActivity extends AppCompatActivity {

    private String mPath;
    private Bitmap mBitmap;
    private ImageView ivTest;
    private int imageWidth;
    private int imageHeight;
//    private MosicView mosicView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageWidth = metrics.widthPixels / 2;
        imageHeight = metrics.heightPixels / 2;
        mPath = getIntent().getStringExtra("path");
        decodeBitmapFile();
        ivTest = ((ImageView) findViewById(R.id.ivTest));
   /*     mosicView = ((MosicView) findViewById(R.id.mosicView));
        ivTest.setImageBitmap(mBitmap);
        mosicView.setBitmap(mBitmap);*/
    }



    private void decodeBitmapFile(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mPath,options);
        options.inSampleSize = calculateInSampleSize(options,imageWidth,imageHeight);
        options.inJustDecodeBounds = false;
        mBitmap = BitmapFactory.decodeFile(mPath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
