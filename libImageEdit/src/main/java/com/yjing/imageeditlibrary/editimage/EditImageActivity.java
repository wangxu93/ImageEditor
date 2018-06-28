package com.yjing.imageeditlibrary.editimage;

import android.app.Activity;
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
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.yjing.imageeditlibrary.BaseActivity;
import com.yjing.imageeditlibrary.R;
import com.yjing.imageeditlibrary.coper.CropImage;
import com.yjing.imageeditlibrary.editimage.contorl.SaveMode;
import com.yjing.imageeditlibrary.editimage.fragment.MainMenuFragment;
import com.yjing.imageeditlibrary.editimage.inter.ImageEditInte;
import com.yjing.imageeditlibrary.editimage.inter.OnViewTouthListener;
import com.yjing.imageeditlibrary.editimage.inter.SaveCompletedInte;
import com.yjing.imageeditlibrary.editimage.view.CustomPaintView;
import com.yjing.imageeditlibrary.editimage.view.MenuPopupWindowView;
import com.yjing.imageeditlibrary.editimage.view.PinchImageView;
import com.yjing.imageeditlibrary.editimage.view.StickerView;
import com.yjing.imageeditlibrary.editimage.view.TextStickerView;
import com.yjing.imageeditlibrary.editimage.view.mosaic.MosaicView;
import com.yjing.imageeditlibrary.utils.BitmapUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * 图片编辑 主页面
 * 包含 1.贴图 2.滤镜 3.剪裁 4.底图旋转 功能
 */
public class EditImageActivity extends BaseActivity {
    private static final String TAG = "EditImageActivity";
    public static final String FILE_PATH = "file_path";
    public static final String EXTRA_OUTPUT = "extra_output";
    public static final String SAVE_FILE_PATH = "save_file_path";
    public static final String MENU_ITEM = "menuItem";
    public static final String TAG_BACKGROUND_COLOR = "backgroundcolor";
    public static final String TAG_RETURN_IMAGE_TYPE = "returnImageType";


    public static final String IMAGE_IS_EDIT = "image_is_edit";


    public static final String RESULT_TYPE = "resultType";
    public static final String SHOW_MUNU = "showMenu";

    public static final int REQUESTCODE_ADDTEXT = 0xFF00;
    public static final int REQUEST_SELECT_SMAIL = 0x00F0;

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
    private Animation mAnim_In;
    private Animation mAnim_out;
    private Handler mHandler = new Handler();
    private View loading;
    private int showMenuWindow = 0;

    private String resultType;
    private ArrayList<String> mMenuItems;
    private String groundColor;
    private int resultImageType = 0;   //返回图片的格式 0 png，1 jpg

    /**
     * @param context
     * @param editImagePath 原图路径
     * @param outputPath    图片保存路径
     * @param requestCode
     */
    public static void start(Activity context, int showMenuView, final String editImagePath, final String outputPath, final int requestCode) {
        if (TextUtils.isEmpty(editImagePath)) {
            Toast.makeText(context, R.string.no_choose, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent it = new Intent(context, EditImageActivity.class);
        it.putExtra(EditImageActivity.FILE_PATH, editImagePath);
        it.putExtra(EditImageActivity.EXTRA_OUTPUT, outputPath);
        it.putExtra(SHOW_MUNU, showMenuView);
        context.startActivityForResult(it, requestCode);
    }

    @Override
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
        showMenuWindow = getIntent().getIntExtra(SHOW_MUNU, 0);
        mMenuItems = getIntent().getStringArrayListExtra(MENU_ITEM);
        if (mMenuItems == null) {
            mMenuItems = new ArrayList<>();
        }
        resultImageType = getIntent().getIntExtra(TAG_RETURN_IMAGE_TYPE,0);
        groundColor = getIntent().getStringExtra(TAG_BACKGROUND_COLOR);

        loadImage(filePath);
    }

    private void initView() {
        mContext = this;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageWidth = metrics.widthPixels / 2;
        imageHeight = metrics.heightPixels / 2;
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        bannerFlipper = (ViewFlipper) findViewById(R.id.banner_flipper);
        bannerFlipper.setInAnimation(this, R.anim.in_bottom_to_top);
        bannerFlipper.setOutAnimation(this, R.anim.out_bottom_to_top);
        banner = findViewById(R.id.banner);
        applyBtn = findViewById(R.id.apply);
        applyBtn.setOnClickListener(new ApplyBtnClick());
        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showMenuWindow == 1) {
                    showPopupWindow();
                } else {
                    new SaveBtnClick(true, null).onClick(saveBtn);
                }

            }
        });

        mainImage = (PinchImageView) findViewById(R.id.main_image);
        if (!TextUtils.isEmpty(groundColor)) {
            String replaceColor = groundColor.replace("0x", "#");  //将 0x替换成#
            try {
                mainImage.setBackgroundColor(Color.parseColor(replaceColor));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        mTextStickerView = (TextStickerView) findViewById(R.id.text_sticker_panel);
        mPaintView = (CustomPaintView) findViewById(R.id.custom_paint_view);
        mMosaicView = (MosaicView) findViewById(R.id.mosaic_view);


        mMosaicView.setOnViewTouthListener(onPaintViewTouthListener);
        mPaintView.setOnViewTouthListener(onPaintViewTouthListener);
        mTextStickerView.setOnViewTouthListener(onViewTouthListener);
        mStickerView.setOnViewTouthListener(onViewTouthListener);
        mainImage.setOnViewTouthListener(onViewTouthListener);

        //放功能键的容器
        View fl_edit_bottom_height = findViewById(R.id.fl_edit_bottom_height);
        View fl_edit_bottom_full = findViewById(R.id.fl_edit_bottom_full);
        fl_edit_above_mainmenu = findViewById(R.id.fl_edit_above_mainmenu);
        editFactory = new SaveMode.EditFactory(this, fl_edit_bottom_height, fl_edit_bottom_full, fl_edit_above_mainmenu);

        //主要按键布局
        fl_main_menu = findViewById(R.id.fl_main_menu);
        mMainMenuFragment = MainMenuFragment.newInstance(this);
        mMainMenuFragment.setOnModeChangeListener(onModeChangeListener);
        this.getSupportFragmentManager().beginTransaction().add(R.id.fl_main_menu, mMainMenuFragment)
                .show(mMainMenuFragment).commit();
    }

    private void getMainImageParams(PinchImageView view) {
        if (view == null || !view.isReady()) {
            return;
        }
        Matrix ma = view.getOuterMatrix(null);
        RectF imageBound = view.getImageBound(null);
        mPaintView.setMainLevelMatrix(ma, imageBound);
        mMosaicView.setMainLevelMatrix(ma, imageBound);
        mTextStickerView.setMainLevelMatrix(ma, imageBound);
        mStickerView.setMainLevelMatrix(ma, imageBound);
    }

    private void showPopupWindow() {
        MenuPopupWindowView menuPopupWindow = new MenuPopupWindowView(this,mMenuItems);
        menuPopupWindow.setOnItemClickListener(new MenuPopupWindowView.OnItemClickListener() {
            @Override
            public void onItemClick(String str) {
                resultType = str;
                new SaveBtnClick(true, null).onClick(saveBtn);
            }
        });
        menuPopupWindow.showAtLocation(titleBar, Gravity.BOTTOM, 0, 0);
    }

    private MainMenuFragment.OnModeChangeListener onModeChangeListener = new MainMenuFragment.OnModeChangeListener() {
        @Override
        public void change() {

        }
    };

    private OnViewTouthListener onViewTouthListener = new OnViewTouthListener() {
        private boolean optMainMenu = false;  //记录画笔菜单是否显示出来了
        private boolean isMenuShow = true;    //记录当前菜单是否是显示状态，单机的时候要用到

        @Override
        public void onTouchDown() {
            Log.i(TAG, "onTouchDown: ");
            moveStatus = false;
            optMainMenu = getMainMenuVisiblity() == View.VISIBLE;
        }

        @Override
        public void onTouchMove() {

            if (moveStatus) {
                return;
            }
            moveStatus = true;

            if (!isMenuShow) {
                return;
            }
            setMainPageCoverViewStatus(View.GONE, optMainMenu);
            Log.i(TAG, "onTouchMove: ");
        }

        @Override
        public void onTouchUp() {
            Log.i(TAG, "onTouchUp: ");
            if (moveStatus) {
                setMainPageCoverViewStatus(View.VISIBLE, optMainMenu);
                isMenuShow = true;
            } else {
                if (isMenuShow) {
                    setMainPageCoverViewStatus(View.GONE, optMainMenu);
                } else {
                    setMainPageCoverViewStatus(View.VISIBLE, optMainMenu);
                }
                isMenuShow = !isMenuShow;
            }
            moveStatus = false;
        }
    };


    private boolean moveStatus = false;  ///记录是否为move状态，多次move只执行第一次
    private boolean delayedMoveStatus = false; //延长每次划线的时间，产品要求如果用户短时间多次的划线，就不要显示出菜单了，。。。，结合handler

    private OnViewTouthListener onPaintViewTouthListener = new OnViewTouthListener() {
        private boolean isMenuShow = true;   //记录当前菜单是否是显示状态，单机的时候要用到

        @Override
        public void onTouchDown() {


        }

        @Override
        public void onTouchMove() {
            delayedMoveStatus = true;
            if (moveStatus) {
                return;
            }
            moveStatus = true;
            if (!isMenuShow) {
                return;
            }
            setMainPageCoverViewStatus(View.GONE, true);
        }

        @Override
        public void onTouchUp() {
            delayedMoveStatus = false;
            if (moveStatus) {
                isMenuShow = true;
                sendShowDelayedMessage();
            } else {
                if (isMenuShow) {
                    setMainPageCoverViewStatus(View.GONE, true);
                    isMenuShow = false;
                } else {
                    setMainPageCoverViewStatus(View.VISIBLE, true);
                    isMenuShow = true;
                    moveStatus = false;
                }
            }
        }
    };

    private void sendShowDelayedMessage() {
        mOptHandler.removeMessages(View.VISIBLE);
        mOptHandler.sendEmptyMessageDelayed(View.VISIBLE, 600);
    }


    private Handler mOptHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage: ");
            if (delayedMoveStatus) {
                return;
            }
            setMainPageCoverViewStatus(View.VISIBLE, true);
            moveStatus = false;
        }
    };

    private void setMainPageCoverViewStatus(int status, boolean optMainMenu) {

        titleBar.setVisibility(status);
        setViewAnim(titleBar, status);
        rlBottomView.setVisibility(status);
        setViewAnim(rlBottomView, status);
        if (optMainMenu) {
            fl_edit_above_mainmenu.setVisibility(status);
            setViewAnim(fl_edit_above_mainmenu, status);
        }

    }

    private int getMainMenuVisiblity() {
        return fl_edit_above_mainmenu.getVisibility();
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
            mHandler.postDelayed(new Runnable() {  //延时大法好
                @Override
                public void run() {
                    getMainImageParams(mainImage);
                    mMainMenuFragment.selectPaintMode();
                }
            }, 300);
//            mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
            // mainImage.setDisplayType(DisplayType.FIT_TO_SCREEN);
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
            final boolean shouldBack = v != null;
            applyEdit(v, shouldBack);
        }


        /**
         * 迭代方式一层一层保存图片
         */
        private void applyEdit(final View v, final boolean shouldBack) {

            if (modes[modeIndex] == SaveMode.EditMode.NONE || modes[modeIndex] == SaveMode.EditMode.CROP || modes[modeIndex] == SaveMode.EditMode.PALETTE) {
                modeIndex++;
                if (modeIndex < modes.length) {
                    applyEdit(v, shouldBack);
                } else {
                    if (isSaveImageToLocal) {
                        if (mOpTimes == 0) {//并未修改图片
                            onSaveTaskDone();
                        } else {
                            doSaveImage(shouldBack, inte);
                        }
                    } else {
                        doSaveImage(shouldBack, inte);
                    }
                }
                return;
            }
            ImageEditInte fragment = (ImageEditInte) editFactory.getFragment(modes[modeIndex++]);
            if (fragment != null) {
                fragment.appleEdit(new SaveCompletedInte() {
                    @Override
                    public void completed() {
                        if (modeIndex < modes.length) {
                            applyEdit(v, shouldBack);
                        } else {
                            if (isSaveImageToLocal) {
                                if (mOpTimes == 0) {//并未修改图片
                                    onSaveTaskDone();
                                } else {
                                    doSaveImage(shouldBack, inte);
                                }
                            } else {
                                doSaveImage(shouldBack, inte);
                            }
                        }
                    }
                });
            }
        }
    }

    protected void doSaveImage(boolean shouldBack, SaveCompletedInte inte) {
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
        returnIntent.putExtra(RESULT_TYPE, resultType);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    /**
     * 保存图像
     * 完成后退出
     */
    private final class SaveImageTask extends AsyncTask<Bitmap, Void, Boolean> {
        private boolean shouldBack;
        private SaveCompletedInte mListener;

        public SaveImageTask(boolean shouldBack) {
            this.shouldBack = shouldBack;
        }

        @Override
        protected Boolean doInBackground(Bitmap... params) {
            if (TextUtils.isEmpty(saveFilePath))
                return false;

            return BitmapUtils.saveBitmap(params[0], saveFilePath,resultImageType);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            loading.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled(Boolean result) {
            super.onCancelled(result);
            loading.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            loading.setVisibility(View.GONE);
            if (mListener != null) {
                mListener.completed();
            }
            if (result) {
                resetOpTimes();
                if (shouldBack) {
                    onSaveTaskDone();
                }
            } else {
                if (isFinishing()) {
                    return;
                }
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
        } else if (requestCode == PaletteActivity.RESULT_CODE_OUTPUT_URL) {
            if (data != null && resultCode == Activity.RESULT_OK) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    Uri uri = bundle.getParcelable(PaletteActivity.FIELD_OUTPUT_URI);
                    if (uri != null) {
                        loadImage(uri.getSchemeSpecificPart());
                    }
                    File file = new File(saveFilePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
            SaveMode.getInstant().setMode(SaveMode.EditMode.NONE);
        } else if (requestCode == REQUEST_SELECT_SMAIL) {
            editFactory.mStirckerFragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
