// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.yjing.imageeditlibrary.coper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.yjing.imageeditlibrary.R;

import java.io.File;
import java.io.IOException;

/**
 * Built-in activity for image cropping.<br>
 * Use {@link CropImage#activity(Uri)} to create a builder to start this activity.
 */
public class CropImageActivity extends AppCompatActivity
        implements CropImageView.OnSetImageUriCompleteListener,
        CropImageView.OnCropImageCompleteListener {

    private static final String TAG = "CropImageActivity";
    /**
     * The crop image view library widget used in the activity
     */
    private CropImageView mCropImageView;

    /**
     * Persist URI image to crop URI if specific permissions are required
     */
    private Uri mCropImageUri;

    /**
     * the options that were set for the crop image
     */
    private CropImageOptions mOptions;
    private TextView tvReset;
    private View tvRoote;
    private View tvCancle;
    private TextView tvCrop;
    private Rect wholeImageRect;
    private int imageWidth;
    private int imageHeight;
    private boolean isChange = false;

    @Override
    @SuppressLint("NewApi")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        setContentView(R.layout.crop_image_activity);

        mCropImageView = findViewById(R.id.cropImageView);

        Bundle bundle = getIntent().getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE);
        mCropImageUri = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE);
        mOptions = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS);

        initViews();
        initListeners();
        if (savedInstanceState == null) {
            if (mCropImageUri == null || mCropImageUri.equals(Uri.EMPTY)) {
                if (CropImage.isExplicitCameraPermissionRequired(this)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    requestPermissions(
                            new String[]{Manifest.permission.CAMERA},
                            CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
                } else {
                    CropImage.startPickImageActivity(this);
                }
            } else if (CropImage.isReadExternalStoragePermissionsRequired(this, mCropImageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted, can start crop image activity
                mCropImageView.setImageUriAsync(mCropImageUri);
            }
        }


    }

    private void initListeners() {
        tvReset.setOnClickListener(new ClickListener());
        tvRoote.setOnClickListener(new ClickListener());
        tvCancle.setOnClickListener(new ClickListener());
        tvCrop.setOnClickListener(new ClickListener());
        mCropImageView.setOnSetCropOverlayMovedListener(onSetCropOverlayMovedListener);
        wholeImageRect = new Rect();
        ViewTreeObserver viewTreeObserver = mCropImageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                mCropImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                imageWidth = mCropImageView.getMeasuredWidth();
                imageHeight = mCropImageView.getMeasuredHeight();
            }
        });

    }

    private void initViews() {
        tvReset = findViewById(R.id.tvReset);
        tvRoote = findViewById(R.id.tvRoote);
        tvCancle = findViewById(R.id.tvCancle);
        tvCrop = ((TextView) findViewById(R.id.tvCrop));
        setBtnEnable();
    }

    private CropImageView.OnSetCropOverlayMovedListener onSetCropOverlayMovedListener = new CropImageView.OnSetCropOverlayMovedListener() {
        @Override
        public void onCropOverlayMoved(Rect rect) {
            Log.i(TAG, "onCropOverlayMoved: left" + rect.left + " right:" + rect.right + " top:" + rect.top + " bottom:" + rect.bottom);
            Log.i(TAG, "imageRectf: left" + wholeImageRect.left + " right:" + wholeImageRect.right + " top:" + wholeImageRect.top + " bottom:" + wholeImageRect.bottom);
            Rect cropRect = mCropImageView.getCropRect();
            setBtnStatus(cropRect);
        }
    };

    private void setBtnStatus(Rect rect) {
//        if (rect == null || imageHeight == 0 || imageWidth == 0) {
//            return;
//        }
//        int width = Math.abs(rect.right - rect.left);
//        int height = Math.abs(rect.bottom - rect.top);
//        if (Math.abs(imageHeight - height) < 10 && Math.abs(imageWidth - width) < 10) {
        setBtnAble();
        isChange = true;
//        }else{
//            setBtnEnable();
//        }
    }

    private void setBtnEnable() {
        tvCrop.setTextColor(Color.parseColor("#777777"));
        tvReset.setTextColor(Color.parseColor("#777777"));
        tvCrop.setEnabled(false);
        tvReset.setEnabled(false);
    }

    private void setBtnAble() {
        tvReset.setEnabled(true);
        tvCrop.setEnabled(true);
        tvReset.setTextColor(Color.WHITE);
        tvCrop.setTextColor(Color.WHITE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCropImageView.setOnSetImageUriCompleteListener(this);
        mCropImageView.setOnCropImageCompleteListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCropImageView.setOnSetImageUriCompleteListener(null);
        mCropImageView.setOnCropImageCompleteListener(null);
    }

    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.tvReset) {
                resetImage();
            } else if (id == R.id.tvRoote) {
                rotateImage(mOptions.rotationDegrees);
            } else if (id == R.id.tvCancle) {
                setResultCancel();
            } else if (id == R.id.tvCrop) {
                cropImage();
            }
        }
    }

    private void resetImage() {
        mCropImageView.resetCropRect();
        setBtnEnable();
        isChange = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResultCancel();
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the picker. We don't have anything to crop
                setResultCancel();
            }

            if (resultCode == Activity.RESULT_OK) {
                mCropImageUri = CropImage.getPickImageResultUri(this, data);

                // For API >= 23 we need to check specifically that we have permissions to read external
                // storage.
                if (CropImage.isReadExternalStoragePermissionsRequired(this, mCropImageUri)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                } else {
                    // no permissions required or already grunted, can start crop image activity
                    mCropImageView.setImageUriAsync(mCropImageUri);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null
                    && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                mCropImageView.setImageUriAsync(mCropImageUri);
            } else {
                Toast.makeText(this, R.string.crop_image_activity_no_permissions, Toast.LENGTH_LONG).show();
                setResultCancel();
            }
        }

        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            // Irrespective of whether camera permission was given or not, we show the picker
            // The picker will not add the camera intent if permission is not available
            CropImage.startPickImageActivity(this);
        }
    }

    @Override
    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
        if (error == null) {
            if (mOptions.initialCropWindowRectangle != null) {
                mCropImageView.setCropRect(mOptions.initialCropWindowRectangle);
            }
            if (mOptions.initialRotation > -1) {
                mCropImageView.setRotatedDegrees(mOptions.initialRotation);
            }
        } else {
            setResult(null, error, 1);
        }
    }

    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        setResult(result.getUri(), result.getError(), result.getSampleSize());
    }

    // region: Private methods

    /**
     * Execute crop image and save the result tou output uri.
     */
    protected void cropImage() {
        if (mOptions.noOutputImage) {
            setResult(null, null, 1);
        } else {
            Uri outputUri = getOutputUri();
            mCropImageView.saveCroppedImageAsync(
                    outputUri,
                    mOptions.outputCompressFormat,
                    mOptions.outputCompressQuality,
                    mOptions.outputRequestWidth,
                    mOptions.outputRequestHeight,
                    mOptions.outputRequestSizeOptions);
        }
    }

    /**
     * Rotate the image in the crop image view.
     */
    protected void rotateImage(int degrees) {
        mCropImageView.rotateImage(degrees);
        int rotatedDegrees = mCropImageView.getRotatedDegrees();
        if (rotatedDegrees % 360 == 0 && !isChange) {
            setBtnEnable();
        } else {
            setBtnAble();
        }
    }

    /**
     * Get Android uri to save the cropped image into.<br>
     * Use the given in options or create a temp file.
     */
    protected Uri getOutputUri() {
        Uri outputUri = mOptions.outputUri;
        if (outputUri == null || outputUri.equals(Uri.EMPTY)) {
            try {
                String ext =
                        mOptions.outputCompressFormat == Bitmap.CompressFormat.JPEG
                                ? ".jpg"
                                : mOptions.outputCompressFormat == Bitmap.CompressFormat.PNG ? ".png" : ".webp";
                outputUri = Uri.fromFile(File.createTempFile("cropped", ext, getCacheDir()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to create temp file for output image", e);
            }
        }
        return outputUri;
    }

    /**
     * Result with cropped image data or error if failed.
     */
    protected void setResult(Uri uri, Exception error, int sampleSize) {
        int resultCode = error == null ? RESULT_OK : CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
        setResult(resultCode, getResultIntent(uri, error, sampleSize));
        finish();
    }

    /**
     * Cancel of cropping activity.
     */
    protected void setResultCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Get intent instance to be used for the result of this activity.
     */
    protected Intent getResultIntent(Uri uri, Exception error, int sampleSize) {
        CropImage.ActivityResult result =
                new CropImage.ActivityResult(
                        mCropImageView.getImageUri(),
                        uri,
                        error,
                        mCropImageView.getCropPoints(),
                        mCropImageView.getCropRect(),
                        mCropImageView.getRotatedDegrees(),
                        mCropImageView.getWholeImageRect(),
                        sampleSize);
        Intent intent = new Intent();
        intent.putExtras(getIntent());
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result);
        return intent;
    }
    // endregion
}
