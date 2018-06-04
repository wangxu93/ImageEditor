package com.yjing.imageeditlibrary.editimage.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.yjing.imageeditlibrary.R;

import static com.yjing.imageeditlibrary.editimage.EditImageActivity.TYPE_CLOND;
import static com.yjing.imageeditlibrary.editimage.EditImageActivity.TYPE_DEFAULT;
import static com.yjing.imageeditlibrary.editimage.EditImageActivity.TYPE_FORWARD;
import static com.yjing.imageeditlibrary.editimage.EditImageActivity.TYPE_SAVE;

public class MenuPopupWindowView extends PopupWindow implements View.OnClickListener {
    private Button btnSaveToGallery;
    private Button btnShare;
    private Button btnCancel;
    private Button btnSaveCloud;



    public MenuPopupWindowView(Context context) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.topic_image_menu, null);
        btnSaveToGallery = view(root, R.id.btnSave);
        btnShare = view(root, R.id.btnShare);
        btnCancel = view(root, R.id.btnCancel);
        btnSaveToGallery.setOnClickListener(this);
        btnShare.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnSaveCloud = (Button) root.findViewById(R.id.btn_save_cloud);
        btnSaveCloud.setOnClickListener(this);

        setContentView(root);
        setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(android.R.color.transparent)));
        setOutsideTouchable(true);
        setAnimationStyle(R.style.popupwindow_anmation);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(btnSaveToGallery)) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(TYPE_SAVE);
            }
            dismiss();
        } else if (v.equals(btnCancel)) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(TYPE_DEFAULT);
            }
            dismiss();
        } else if (v.equals(btnShare)) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(TYPE_FORWARD);
            }
            dismiss();

        } else if (v.getId() == R.id.btn_save_cloud) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(TYPE_CLOND);
            }
            dismiss();
        }
    }

    private Button view(View view, int id) {
        return view.findViewById(id);
    }

    private OnItemClickListener onItemClickListener;

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(int type);
    }

}