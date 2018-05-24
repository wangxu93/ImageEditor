package com.yjing.imageeditlibrary.editimage.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.yjing.imageeditlibrary.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangxu on 2017/12/27.
 */

public class MainColorSelectorView extends RelativeLayout {

    private View rlWhite;
    private View rlBlack;
    private View rlRed;
    private View rlYellow;
    private View rlGreen;
    private View rlBlue;
    private View rlPurple;

    private Map<Integer, Integer> colors = new HashMap<>();
    private View rlPink;

    public MainColorSelectorView(Context context) {
        this(context, null);
    }

    public MainColorSelectorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainColorSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_main_color_selector, this, true);
        initViews();
        initListener();
        initColors();
    }

    private void initColors() {
        colors.put(R.id.rlWhite, getResources().getColor(R.color.white));
        colors.put(R.id.rlBlack, getResources().getColor(R.color.black));
        colors.put(R.id.rlRed, getResources().getColor(R.color.red));
        colors.put(R.id.rlYellow, getResources().getColor(R.color.yellow));
        colors.put(R.id.rlGreen, getResources().getColor(R.color.green));
        colors.put(R.id.rlBlue, getResources().getColor(R.color.blue));
        colors.put(R.id.rlPurple, getResources().getColor(R.color.magenta));
        colors.put(R.id.rlPink, getResources().getColor(R.color.materialcolorpicker__dribble));
    }


    private void initViews() {
        rlWhite = findViewById(R.id.rlWhite);
        rlBlack = findViewById(R.id.rlBlack);
        rlRed = findViewById(R.id.rlRed);
        rlYellow = findViewById(R.id.rlYellow);
        rlGreen = findViewById(R.id.rlGreen);
        rlBlue = findViewById(R.id.rlBlue);
        rlPurple = findViewById(R.id.rlPurple);
        rlPink = findViewById(R.id.rlPink);
    }

    private void initListener() {
        rlWhite.setOnClickListener(new ClickListener());
        rlRed.setOnClickListener(new ClickListener());
        rlBlack.setOnClickListener(new ClickListener());
        rlYellow.setOnClickListener(new ClickListener());
        rlGreen.setOnClickListener(new ClickListener());
        rlBlue.setOnClickListener(new ClickListener());
        rlPurple.setOnClickListener(new ClickListener());
        rlPink.setOnClickListener(new ClickListener());
    }


    private class ClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (onColorSelector == null) {
                return;
            }
            clearButtonsBackGround();
            int id = v.getId();
            v.setBackgroundResource(R.drawable.shape_range_bg);
            onColorSelector.onSelectColor(colors.get(id));

        }
    }

    private void clearButtonsBackGround() {
        rlWhite.setBackgroundResource(R.color.transparent);
        rlBlack.setBackgroundResource(R.color.transparent);
        rlRed.setBackgroundResource(R.color.transparent);
        rlYellow.setBackgroundResource(R.color.transparent);
        rlGreen.setBackgroundResource(R.color.transparent);
        rlBlue.setBackgroundResource(R.color.transparent);
        rlPurple.setBackgroundResource(R.color.transparent);
        rlPink.setBackgroundResource(R.color.transparent);
    }

    private OnColorSelector onColorSelector;

    public void setOnColorSelector(OnColorSelector onColorSelector) {
        this.onColorSelector = onColorSelector;
    }

    public interface OnColorSelector {
        void onSelectColor(int color);
    }
}
