package com.yjing.imageeditlibrary.editimage.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yjing.imageeditlibrary.R;
import com.yjing.imageeditlibrary.editimage.EditImageActivity;
import com.yjing.imageeditlibrary.editimage.contorl.SaveMode;
import com.yjing.imageeditlibrary.editimage.inter.ImageEditInte;
import com.yjing.imageeditlibrary.editimage.inter.SaveCompletedInte;
import com.yjing.imageeditlibrary.editimage.model.StickerBean;
import com.yjing.imageeditlibrary.editimage.task.StickerTask;
import com.yjing.imageeditlibrary.editimage.view.StickerItem;
import com.yjing.imageeditlibrary.editimage.view.StickerView;
import com.yjing.imageeditlibrary.module.SelectSmailHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 贴图分类fragment
 */
public class StirckerFragment extends BaseFragment implements ImageEditInte {


    public static final String TAG = StirckerFragment.class.getName();
    public static final String STICKER_FOLDER = "stickers";


    private View mainView;
    private StickerView mStickerView;// 贴图显示控件

    private List<StickerBean> stickerBeanList = new ArrayList<StickerBean>();

    private SaveStickersTask mSaveTask;

    public static StirckerFragment newInstance(EditImageActivity activity) {
        StirckerFragment fragment = new StirckerFragment();
        fragment.activity = activity;
        fragment.mStickerView = activity.mStickerView;
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_edit_image_sticker_type, null);
        return mainView;
    }



    /**
     * 保存贴图层 合成一张图片
     */
    @Override
    public void appleEdit(SaveCompletedInte inte) {
        if (mSaveTask != null) {
            mSaveTask.cancel(true);
        }
        mSaveTask = new SaveStickersTask((EditImageActivity) getActivity(), inte);
        mSaveTask.execute(activity.mainBitmap);
    }

    @Override
    public void onShow() {
        mStickerView.setIsOperation(true);
        SelectSmailHelper.getInstance().openSelectSmailPage(activity,EditImageActivity.REQUEST_SELECT_SMAIL);
    }

    @Override
    public void method2() {

    }

    @Override
    public void method3() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * 选择贴图加入到页面中
     *
     * @param rid
     */
    public void selectedStickerItem(int rid) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), rid);
        mStickerView.addBitImage(bitmap);
    }

    public void backToMain() {
//        mStickerView.setIsOperation(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EditImageActivity.REQUEST_SELECT_SMAIL) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                int rid = data.getIntExtra("rid", -1);
                if (rid != -1) {
                    selectedStickerItem(rid);
                }
            }
            SaveMode.getInstant().setMode(SaveMode.EditMode.NONE);
        }

    }

    /**
     * 保存贴图任务
     *
     * @author panyi
     */
    private final class SaveStickersTask extends StickerTask {
        public SaveStickersTask(EditImageActivity activity, SaveCompletedInte inte) {
            super(activity, inte);
        }

        @Override
        public void handleImage(Canvas canvas, Matrix m) {
            LinkedHashMap<Integer, StickerItem> addItems = mStickerView.getBank();
            for (Integer id : addItems.keySet()) {
                StickerItem item = addItems.get(id);
                item.matrix.postConcat(m);// 乘以底部图片变化矩阵
                canvas.drawBitmap(item.bitmap, item.matrix, null);
            }// end for
        }

        @Override
        public void onPostResult(Bitmap result) {
            mStickerView.clear();
            activity.changeMainBitmap(result);
        }
    }// end inner class

}// end class
