package com.yjing.imageeditlibrary.editimage.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yjing.imageeditlibrary.R;
import com.yjing.imageeditlibrary.editimage.EditImageActivity;
import com.yjing.imageeditlibrary.editimage.inter.ImageEditInte;
import com.yjing.imageeditlibrary.editimage.inter.SaveCompletedInte;
import com.yjing.imageeditlibrary.editimage.task.StickerTask;
import com.yjing.imageeditlibrary.editimage.view.CustomPaintView;
import com.yjing.imageeditlibrary.editimage.view.MainColorSelectorView;


/**
 * 用户自由绘制模式 操作面板
 * 可设置画笔粗细 画笔颜色
 * custom draw mode panel
 */
public class PaintFragment extends BaseFragment implements View.OnClickListener, ImageEditInte {

    private CustomPaintView mPaintView;
    private ImageView mRevokeView;
    private SaveCustomPaintTask mSavePaintImageTask;
    private final static int DEFAULT_PAINT_WIDTH = 20;
    private MainColorSelectorView colorSelectorView;

    public static PaintFragment newInstance(EditImageActivity activity) {
        PaintFragment fragment = new PaintFragment();
        fragment.activity = activity;
        fragment.mPaintView = activity.mPaintView;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_edit_paint, null);
        mRevokeView = (ImageView) mainView.findViewById(R.id.paint_revoke);
        colorSelectorView = (MainColorSelectorView) mainView.findViewById(R.id.colorSelectorView);
        colorSelectorView.setOnColorSelector(onColorSelector);
        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRevokeView.setOnClickListener(this);
        initPaintView();

    }

    private void initPaintView() {
        this.mPaintView.setWidth(DEFAULT_PAINT_WIDTH);
        this.mPaintView.setColor(Color.RED);
    }

    @Override
    public void onClick(View v) {
        if (v == mRevokeView) {//撤销功能
            mPaintView.undo();
        }
    }

    /**
     * 返回主菜单
     */
    public void backToMain() {
        activity.mainImage.setVisibility(View.VISIBLE);
        mPaintView.setIsOperation(false);
    }

    public void onShow() {
        mPaintView.setIsOperation(true);
    }

    /**
     * 设置画笔颜色
     *
     * @param paintColor
     */
    protected void setPaintColor(final int paintColor) {
        mPaintView.setColor(paintColor);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSavePaintImageTask != null && !mSavePaintImageTask.isCancelled()) {
            mSavePaintImageTask.cancel(true);
        }
    }

    /**
     * 保存涂鸦
     */
    @Override
    public void appleEdit(SaveCompletedInte inte) {
        if (mSavePaintImageTask != null && !mSavePaintImageTask.isCancelled()) {
            mSavePaintImageTask.cancel(true);
        }

        mSavePaintImageTask = new SaveCustomPaintTask(activity, inte);
        mSavePaintImageTask.execute(activity.mainBitmap);
    }

    @Override
    public void method2() {

    }

    @Override
    public void method3() {

    }

    private MainColorSelectorView.OnColorSelector onColorSelector = new MainColorSelectorView.OnColorSelector() {
        @Override
        public void onSelectColor(int color) {
            setPaintColor(color);
        }
    };

    /**
     * 文字合成任务
     * 合成最终图片
     */
    private final class SaveCustomPaintTask extends StickerTask {

        public SaveCustomPaintTask(EditImageActivity activity, SaveCompletedInte inte) {
            super(activity, inte);
        }

        @Override
        public void handleImage(Canvas canvas, Matrix m) {
            float[] f = new float[9];
            m.getValues(f);
            int dx = (int) f[Matrix.MTRANS_X];
            int dy = (int) f[Matrix.MTRANS_Y];
            float scale_x = f[Matrix.MSCALE_X];
            float scale_y = f[Matrix.MSCALE_Y];
            canvas.save();
            canvas.translate(dx, dy);
            canvas.scale(scale_x, scale_y);

            if (mPaintView.getPaintBit() != null) {
                canvas.drawBitmap(mPaintView.getPaintBit(), 0, 0, null);
            }
            canvas.restore();
        }

        @Override
        public void onPostResult(Bitmap result) {
            mPaintView.reset();
            activity.changeMainBitmap(result);
        }
    }
}
