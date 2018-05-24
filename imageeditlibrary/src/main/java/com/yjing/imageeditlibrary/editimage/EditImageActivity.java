package com.yjing.imageeditlibrary.editimage;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.theartofdev.edmodo.cropper.CropImage;
import com.yjing.imageeditlibrary.BaseActivity;
import com.yjing.imageeditlibrary.R;
import com.yjing.imageeditlibrary.editimage.contorl.SaveMode;
import com.yjing.imageeditlibrary.editimage.fragment.MainMenuFragment;
import com.yjing.imageeditlibrary.editimage.inter.ImageEditInte;
import com.yjing.imageeditlibrary.editimage.inter.OnViewTouthListener;
import com.yjing.imageeditlibrary.editimage.inter.SaveCompletedInte;
import com.yjing.imageeditlibrary.editimage.view.CropImageView;
import com.yjing.imageeditlibrary.editimage.view.CustomPaintView;
import com.yjing.imageeditlibrary.editimage.view.PinchImageView;
import com.yjing.imageeditlibrary.editimage.view.RotateImageView;
import com.yjing.imageeditlibrary.editimage.view.StickerView;
import com.yjing.imageeditlibrary.editimage.view.TextStickerView;
import com.yjing.imageeditlibrary.editimage.view.mosaic.MosaicView;
import com.yjing.imageeditlibrary.utils.BitmapUtils;
import com.yjing.imageeditlibrary.utils.FileUtils;

import java.io.File;

/**
 * 图片编辑 主页面
 * 包含 1.贴图 2.滤镜 3.剪裁 4.底图旋转 功能
 */
public class EditImageActivity extends BaseActivity {
    public static final String FILE_PATH = "file_path";
    public static final String EXTRA_OUTPUT = "extra_output";
    public static final String SAVE_FILE_PATH = "save_file_path";

    public static final String IMAGE_IS_EDIT = "image_is_edit";

    public static final int REQUESTCODE_ADDTEXT = 0xFF00;
    private static final String TAG = "EditImageActivity";

    public String filePath;// 需要编辑图片路径
    public String saveFilePath;// 生成的新图片路径
    private int imageWidth, imageHeight;// 展示图片控件 宽 高
    private LoadImageTask mLoadImageTask;

    protected int mOpTimes = 0;
    protected boolean isBeenSaved = false;

    private EditImageActivity mContext;
    public Bitmap mainBitmap;// 底层显示Bitmap
    public PinchImageView mainImage;
    private View backBtn;

    public ViewFlipper bannerFlipper;
    private View applyBtn;// 应用按钮
    private View saveBtn;// 保存按钮

    public StickerView mStickerView;// 贴图层View
    public CropImageView mCropPanel;// 剪切操作控件
    public RotateImageView mRotatePanel;// 旋转操作控件
    public TextStickerView mTextStickerView;//文本贴图显示View
    public CustomPaintView mPaintView;//涂鸦模式画板
    public MosaicView mMosaicView;//马赛克模式画板

    private MainMenuFragment mMainMenuFragment;// Menu

    private SaveImageTask mSaveImageTask;
    public SaveMode.EditFactory editFactory;
    public View fl_main_menu;
    public View banner;
    private View titleBar;
    private View rlBottomView;
    private View fl_edit_above_mainmenu;
    private int bottomViewVisibity = View.GONE;
    private Animation mAnim_In;
    private Animation mAnim_out;

    /**
     * @param context
     * @param editImagePath 原图路径
     * @param outputPath    图片保存路径
     * @param requestCode
     */
    public static void start(Activity context, final String editImagePath, final String outputPath, final int requestCode) {
        if (TextUtils.isEmpty(editImagePath)) {
            Toast.makeText(context, R.string.no_choose, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent it = new Intent(context, EditImageActivity.class);
        it.putExtra(EditImageActivity.FILE_PATH, editImagePath);
        it.putExtra(EditImageActivity.EXTRA_OUTPUT, outputPath);
        context.startActivityForResult(it, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkInitImageLoader();
        setContentView(R.layout.activity_image_edit);
        initView();
        getData();
        initAnim();
    }

    private void initAnim() {
        mAnim_In = AnimationUtils.loadAnimation(this, R.anim.anim_in);
        mAnim_out = AnimationUtils.loadAnimation(this, R.anim.anim_out);
    }

    private void getData() {
        filePath = getIntent().getStringExtra(FILE_PATH);
        saveFilePath = getIntent().getStringExtra(EXTRA_OUTPUT);// 保存图片路径
        loadImage(filePath);
    }

    private void initView() {
        mContext = this;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageWidth = metrics.widthPixels / 2;
        imageHeight = metrics.heightPixels / 2;

        bannerFlipper = (ViewFlipper) findViewById(R.id.banner_flipper);
        bannerFlipper.setInAnimation(this, R.anim.in_bottom_to_top);
        bannerFlipper.setOutAnimation(this, R.anim.out_bottom_to_top);
        banner = findViewById(R.id.banner);
        applyBtn = findViewById(R.id.apply);
        applyBtn.setOnClickListener(new ApplyBtnClick());
        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new SaveBtnClick(true, null));

        mainImage = (PinchImageView) findViewById(R.id.main_image);
        backBtn = findViewById(R.id.back_btn);// 退出按钮
        backBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        titleBar = findViewById(R.id.titleBar);
        rlBottomView = findViewById(R.id.flBottomView);
        mainImage.addOuterMatrixChangedListener(new PinchImageView.OuterMatrixChangedListener() {
            @Override
            public void onOuterMatrixChanged(PinchImageView pinchImageView) {
                getMainImageParams(pinchImageView);
            }
        });

        mStickerView = (StickerView) findViewById(R.id.sticker_panel);
        mCropPanel = (CropImageView) findViewById(R.id.crop_panel);
        mRotatePanel = (RotateImageView) findViewById(R.id.rotate_panel);
        mTextStickerView = (TextStickerView) findViewById(R.id.text_sticker_panel);
        mPaintView = (CustomPaintView) findViewById(R.id.custom_paint_view);
        mMosaicView = (MosaicView) findViewById(R.id.mosaic_view);


        mMosaicView.setOnViewTouthListener(onViewTouthListener);
        mPaintView.setOnViewTouthListener(onPaintViewTouthListener);
        mTextStickerView.setOnViewTouthListener(onViewTouthListener);

        //放功能键的容器
        View fl_edit_bottom_height = findViewById(R.id.fl_edit_bottom_height);
        View fl_edit_bottom_full = findViewById(R.id.fl_edit_bottom_full);
        fl_edit_above_mainmenu = findViewById(R.id.fl_edit_above_mainmenu);
        editFactory = new SaveMode.EditFactory(this, fl_edit_bottom_height, fl_edit_bottom_full, fl_edit_above_mainmenu);

        //主要按键布局
        fl_main_menu = findViewById(R.id.fl_main_menu);
        mMainMenuFragment = MainMenuFragment.newInstance(this);
        this.getSupportFragmentManager().beginTransaction().add(R.id.fl_main_menu, mMainMenuFragment)
                .show(mMainMenuFragment).commit();
    }

    private void getMainImageParams(PinchImageView view) {
        if (view == null) {
            return;
        }
        Matrix ma = view.getOuterMatrix(null);
        RectF imageBound = view.getImageBound(null);
        mPaintView.setMainLevelMatrix(ma, imageBound);
        mMosaicView.setMainLevelMatrix(ma, imageBound);
        mTextStickerView.setMainLevelMatrix(ma, imageBound);
    }

    private boolean visMode = false;
    private OnViewTouthListener onViewTouthListener = new OnViewTouthListener() {
        @Override
        public void onTouchDown() {
            if (paintVisMode) {
                return;
            }
            bottomViewVisibity = fl_edit_above_mainmenu.getVisibility();
        }

        @Override
        public void onTouchMove() {
            if (visMode) {
                return;
            }
            visMode = true;
            setMainPageCoverViewStatus(View.GONE);
        }

        @Override
        public void onTouchUp() {
            visMode = false;
            setMainPageCoverViewStatus(View.VISIBLE);
        }
    };

    private boolean paintVisMode = false; //将一次操作时间延长，产品要求如果快速画的时候不能老是显示隐藏titlebar
    private boolean moveDeleteMode = false; //有时候不能remove掉msg，就要使用状态过滤
    private OnViewTouthListener onPaintViewTouthListener = new OnViewTouthListener() {
        @Override
        public void onTouchDown() {
            if (paintVisMode) {
                return;
            }
            bottomViewVisibity = fl_edit_above_mainmenu.getVisibility();
        }

        @Override
        public void onTouchMove() {
            moveDeleteMode = true;
            if (paintVisMode) {
                return;
            }
            paintVisMode = true;
            setMainPageCoverViewStatus(View.GONE);
        }

        @Override
        public void onTouchUp() {
            moveDeleteMode = false;   //每次抬起设置false
            sendShowDelayedMessage();
        }
    };

    private void sendShowDelayedMessage() {
        mOptHandler.removeMessages(View.VISIBLE);
        mOptHandler.sendEmptyMessageDelayed(View.VISIBLE, 600);
    }


    private Handler mOptHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (moveDeleteMode) {   //检查当前是否为抬起，有些情况remove不掉msg，所以要加一层判断
                return;
            }
            paintVisMode = false;
            setMainPageCoverViewStatus(View.VISIBLE);
        }
    };

    private void setMainPageCoverViewStatus(int status) {
        titleBar.setVisibility(status);
        setViewAnim(titleBar, status);
        rlBottomView.setVisibility(status);
        setViewAnim(rlBottomView, status);
        if (status == View.GONE) {
            fl_edit_above_mainmenu.setVisibility(View.GONE);
            setViewAnim(fl_edit_above_mainmenu, View.GONE);
        } else {
            fl_edit_above_mainmenu.setVisibility(bottomViewVisibity);
            setViewAnim(fl_edit_above_mainmenu, bottomViewVisibity);
        }
    }

    private void setViewAnim(View v, int vis) {
        if (vis == View.GONE) {
            v.startAnimation(mAnim_out);
        } else {
            v.startAnimation(mAnim_In);
        }
    }

    public void backToMain() {
        ImageEditInte currentMode = editFactory.getCurrentMode();
        if (SaveMode.getInstant().getMode() != SaveMode.EditMode.NONE && currentMode != null) {
            //退出当前模式
            currentMode.backToMain();
            //将新的模式保存至SaveMode
            SaveMode.getInstant().setMode(SaveMode.EditMode.NONE);
            //更改banner状态
//            bannerFlipper.showPrevious();
        }
        //更改当前fragment
        editFactory.setCurrentEditMode(SaveMode.EditMode.NONE);
    }

    /**
     * 异步载入编辑图片
     *
     * @param filepath
     */
    public void loadImage(String filepath) {
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }
        mLoadImageTask = new LoadImageTask();
        mLoadImageTask.execute(filepath);
    }

    private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {

            return BitmapUtils.getSampledBitmap(params[0], imageWidth,
                    imageHeight);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (mainBitmap != null) {
                mainBitmap.recycle();
                mainBitmap = null;
                System.gc();
            }
            mainBitmap = result;
            mainImage.setImageBitmap(result);
        }
    }

    @Override
    public void onBackPressed() {
        Boolean ifFinish = true;
        //添加文字页面比较特殊 全屏操作 需要返回至主界面 而不是退出
        if ((SaveMode.getInstant().getMode() == SaveMode.EditMode.CROP || SaveMode.getInstant().getMode() == SaveMode.EditMode.TEXT) && editFactory.getCurrentMode() != null) {
            ifFinish = false;
        }
        backToMain();

        if (!ifFinish) {
            return;
        }

        if (canAutoExit()) {
            onSaveTaskDone();
        } else if (false) {//不弹框提示
            //图片还未被保存    弹出提示框确认
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(R.string.exit_without_save)
                    .setCancelable(false).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mContext.finish();
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            finish();
        }
    }

    /**
     * 图片处理保存
     */
    private final class ApplyBtnClick implements OnClickListener {
        @Override
        public void onClick(View v) {
            ImageEditInte currentMode = editFactory.getCurrentMode();
            currentMode.appleEdit(null);
        }
    }

    /**
     * 保存按钮 点击退出
     */
    public final class SaveBtnClick implements OnClickListener {
        private final Boolean isSaveImageToLocal;
        private final SaveCompletedInte inte;
        private SaveMode.EditMode[] modes;
        private int modeIndex;

        public SaveBtnClick(Boolean isSaveImageToLocal, SaveCompletedInte inte) {
            this.isSaveImageToLocal = isSaveImageToLocal;
            this.inte = inte;
        }

        @Override
        public void onClick(View v) {
            //迭代法去保存图片
            modes = SaveMode.EditMode.values();
            modeIndex = 0;
            applyEdit(v);
        }


        /**
         * 迭代方式一层一层保存图片
         */
        private void applyEdit(final View v) {
            final boolean shouldBack = v != null;
            if (modes[modeIndex] == SaveMode.EditMode.NONE || modes[modeIndex] == SaveMode.EditMode.CROP) {
                modeIndex++;
                if (modeIndex < modes.length) {
                    applyEdit(v);
                } else {
                    if (isSaveImageToLocal) {
                        if (mOpTimes == 0) {//并未修改图片
                            onSaveTaskDone();
                        } else {
                            doSaveImage(shouldBack,inte);
                        }
                    } else {
                        doSaveImage(shouldBack,inte);
                    }
//                    if (inte != null) {
//                        inte.completed();
//                    }
                }
                return;
            }
            ImageEditInte fragment = (ImageEditInte) editFactory.getFragment(modes[modeIndex++]);
            fragment.appleEdit(new SaveCompletedInte() {
                @Override
                public void completed() {
                    if (modeIndex < modes.length) {
                        applyEdit(v);
                    } else {
                        if (isSaveImageToLocal) {
                            if (mOpTimes == 0) {//并未修改图片
                                onSaveTaskDone();
                            } else {
                                doSaveImage(shouldBack,inte);
                            }
                        } else {
                            doSaveImage(shouldBack,inte);
                        }
//                        if (inte != null) {
//                            inte.completed();
//                        }
                    }
                }
            });
        }
    }

    protected void doSaveImage(boolean shouldBack,SaveCompletedInte inte) {
        if (mOpTimes <= 0)
            return;

        if (mSaveImageTask != null) {
            mSaveImageTask.cancel(true);
        }

        mSaveImageTask = new SaveImageTask(shouldBack);
        mSaveImageTask.setLintener(inte);
        mSaveImageTask.execute(mainBitmap);
    }

    /**
     * 切换底图Bitmap
     *
     * @param newBit
     */
    public void changeMainBitmap(Bitmap newBit) {
        if (mainBitmap != null) {
            if (!mainBitmap.isRecycled()) {// 回收
                mainBitmap.recycle();
            }
        }
        mainBitmap = newBit;
        mainImage.setImageBitmap(mainBitmap);
//        mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

        increaseOpTimes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }

        if (mSaveImageTask != null) {
            mSaveImageTask.cancel(true);
        }
    }

    public void increaseOpTimes() {
        mOpTimes++;
        isBeenSaved = false;
    }

    public void resetOpTimes() {
        isBeenSaved = true;
    }

    public boolean canAutoExit() {
        return isBeenSaved || mOpTimes == 0;
    }

    protected void onSaveTaskDone() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(SAVE_FILE_PATH, saveFilePath);
        returnIntent.putExtra(IMAGE_IS_EDIT, mOpTimes > 0);

        FileUtils.ablumUpdate(this, saveFilePath);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    /**
     * 保存图像
     * 完成后退出
     */
    private final class SaveImageTask extends AsyncTask<Bitmap, Void, Boolean> {
        private Dialog dialog;
        private boolean shouldBack;
        private SaveCompletedInte mListener;

        public SaveImageTask(boolean shouldBack) {
            this.shouldBack = shouldBack;
        }

        @Override
        protected Boolean doInBackground(Bitmap... params) {
            if (TextUtils.isEmpty(saveFilePath))
                return false;

            return BitmapUtils.saveBitmap(params[0], saveFilePath);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialog.dismiss();
        }

        @Override
        protected void onCancelled(Boolean result) {
            super.onCancelled(result);
            dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = EditImageActivity.getLoadingDialog(mContext, R.string.saving_image, false);
//            dialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            dialog.dismiss();
            Log.i(TAG, "onPostExecute: ");
            if (mListener != null) {
                mListener.completed();
            }
            if (result) {
                resetOpTimes();
                if (shouldBack) {
                    onSaveTaskDone();
                }
            } else {
                Toast.makeText(mContext, R.string.save_error, Toast.LENGTH_SHORT).show();
            }
        }

        public void setLintener(SaveCompletedInte inte) {
            mListener = inte;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE_ADDTEXT) {
            if (data != null && resultCode == Activity.RESULT_OK) {
                String text = data.getStringExtra("text");
                int color = data.getIntExtra("textcolor", Color.RED);
                if (TextUtils.isEmpty(text)) {
                    return;
                }
                mTextStickerView.setTextColor(color);
                mTextStickerView.setText(text);
                mTextStickerView.setIsOperation(true);
            }

            SaveMode.getInstant().setMode(SaveMode.EditMode.NONE);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (data != null && resultCode == Activity.RESULT_OK) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (result != null) {
                    Uri uri = result.getUri();
                    if (uri != null) {
                        loadImage(uri.getSchemeSpecificPart());
                    }
                }
            }
            File file = new File(saveFilePath);
            if (file.exists()) {
                file.delete();
            }
            SaveMode.getInstant().setMode(SaveMode.EditMode.NONE);
        }
    }
}
