package com.yjing.imageeditlibrary.editimage.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.yjing.imageeditlibrary.R;
import com.yjing.imageeditlibrary.editimage.EditImageActivity;
import com.yjing.imageeditlibrary.editimage.inter.ImageEditInte;
import com.yjing.imageeditlibrary.editimage.inter.SaveCompletedInte;
import com.yjing.imageeditlibrary.editimage.task.StickerTask;
import com.yjing.imageeditlibrary.editimage.view.MainColorSelectorView;
import com.yjing.imageeditlibrary.editimage.view.TextStickerView;


/**
 * 添加文本
 */
public class AddTextFragment extends BaseFragment implements ImageEditInte {

    private View mainView;

    private EditText mInputText;//输入框
    private MainColorSelectorView colorSeekBar;//颜色选择器
    private TextStickerView mTextStickerView;// 文字贴图显示控件
    private int mCurrentColor = Color.RED;

    private InputMethodManager imm;

    private SaveTextStickerTask mSaveTask;
    private View save_btn;

    public static AddTextFragment newInstance(EditImageActivity activity) {
        AddTextFragment fragment = new AddTextFragment();
        fragment.activity = activity;
        fragment.mTextStickerView = activity.mTextStickerView;
        return fragment;
    }

    public static AddTextFragment newInstance(){
        return new AddTextFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mainView = inflater.inflate(R.layout.fragment_edit_image_add_text, null);

        mInputText = (EditText) mainView.findViewById(R.id.text_input);
        save_btn = mainView.findViewById(R.id.save_btn);
        colorSeekBar = (MainColorSelectorView) mainView.findViewById(R.id.colorSlider);
        mInputText.setTextColor(mCurrentColor);
//        changeTextColor(Color.RED);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        colorSeekBar.setOnColorSelector(new MainColorSelectorView.OnColorSelector() {
            @Override
            public void onSelectColor(int color) {
                mInputText.setTextColor(color);
                mCurrentColor = color;
//                changeTextColor(color);
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //隐藏键盘
                hideInput();
                //保存文字
                String text = mInputText.getText().toString().trim();
                if (mTextStickerView == null) {
                    Intent data = new Intent();
                    data.putExtra("text",text);
                    data.putExtra("textcolor",mCurrentColor);
                    getActivity().setResult(Activity.RESULT_OK,data);
                    getActivity().finish();
                    return;
                }
                mTextStickerView.setText(text);
                activity.editFactory.setContainerVisiable(AddTextFragment.this, View.GONE);
                mainView.setVisibility(View.GONE);

            }
        });

//        mTextStickerView.setEditText(mInputText);

    }

        @Override
    public void appleEdit(SaveCompletedInte inte) {
        if (mSaveTask != null) {
            mSaveTask.cancel(true);
        }
        //启动任务
        mSaveTask = new SaveTextStickerTask(activity, inte);
        mSaveTask.execute(activity.mainBitmap);
    }

    @Override
    public void onShow() {
        mainView.setVisibility(View.VISIBLE);
//        mTextStickerView.setVisibility(View.VISIBLE);
//        mTextStickerView.setIsOperation(true);
//        if (mInputText != null) {
//            mInputText.clearFocus();
//        }
        //弹起键盘
        showInput();
    }

    @Override
    public void method2() {

    }

    @Override
    public void method3() {

    }

    /**
     * 修改字体颜色
     *
     * @param newColor
     */
    private void changeTextColor(int newColor) {
        mTextStickerView.setTextColor(newColor);
    }

    public void hideInput() {
        if (getActivity() != null && getActivity().getCurrentFocus() != null && isInputMethodShow()) {
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void showInput() {
        mInputText.setFocusable(true);
        mInputText.setFocusableInTouchMode(true);
        mInputText.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) mInputText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(mInputText, 0);
    }

    public boolean isInputMethodShow() {
        return imm.isActive();
    }

    /**
     * 返回主菜单
     */
    public void backToMain() {
        hideInput();
//        appleEdit(null);
        activity.mainImage.setVisibility(View.VISIBLE);
//        mTextStickerView.setVisibility(View.GONE);
        mTextStickerView.setIsOperation(false);
    }

    /**
     * 文字合成任务
     * 合成最终图片
     */
    private final class SaveTextStickerTask extends StickerTask {

        public SaveTextStickerTask(EditImageActivity activity, SaveCompletedInte inte) {
            super(activity, inte);
        }

        @Override
        public void handleImage(Canvas canvas, Matrix m) {
            if (mTextStickerView == null) {
                return;
            }
            float[] f = new float[9];
            m.getValues(f);
            int dx = (int) f[Matrix.MTRANS_X];
            int dy = (int) f[Matrix.MTRANS_Y];
            float scale_x = f[Matrix.MSCALE_X];
            float scale_y = f[Matrix.MSCALE_Y];
            canvas.save();
            canvas.translate(dx, dy);
            canvas.scale(scale_x, scale_y);
            mTextStickerView.drawText(canvas, mTextStickerView.layout_x,
                    mTextStickerView.layout_y, mTextStickerView.mScale, mTextStickerView.mRotateAngle);
            canvas.restore();
        }

        @Override
        public void onPostResult(Bitmap result) {
            if (mTextStickerView == null) {
                return;
            }
            mTextStickerView.clearTextContent();
            mTextStickerView.resetView();

            activity.changeMainBitmap(result);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSaveTask != null && !mSaveTask.isCancelled()) {
            mSaveTask.cancel(true);
        }
    }
}
