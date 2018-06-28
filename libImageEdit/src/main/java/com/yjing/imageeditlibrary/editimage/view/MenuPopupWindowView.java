package com.yjing.imageeditlibrary.editimage.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.yjing.imageeditlibrary.R;

import java.util.ArrayList;

public class MenuPopupWindowView extends PopupWindow implements View.OnClickListener {


    private Button btnCancel;


    public MenuPopupWindowView(Context context, ArrayList<String> strs) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.window_image_menu, null);
        LinearLayout llContaner = root.findViewById(R.id.llContaner);
        btnCancel = view(root, R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        for (String str : strs) {
            View llBtn = inflater.inflate(R.layout.view_menu_btn, null);
            Button button = llBtn.findViewById(R.id.button);
            button.setText(str);
            button.setOnClickListener(this);
            llContaner.addView(llBtn);
        }

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
        if (v.equals(btnCancel)) {
            dismiss();
        } else if (v.getId() == R.id.button) {
            if (onItemClickListener != null && v instanceof Button) {
                String str = ((Button) v).getText().toString();
                onItemClickListener.onItemClick(str);
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

    public interface OnItemClickListener {
        void onItemClick(String str);
    }

}