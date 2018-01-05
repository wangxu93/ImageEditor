package com.yjing.imageeditlibrary.editimage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.yjing.imageeditlibrary.BaseActivity;
import com.yjing.imageeditlibrary.R;
import com.yjing.imageeditlibrary.editimage.fragment.AddTextFragment;

/**
 * Created by wangxu on 2018/1/5.
 */

public class AddTextActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_continer);
        AddTextFragment addTextFragment = AddTextFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment_continer,addTextFragment).commit();
    }

    public static void launch(Activity activity,int requestCode){
        Intent intent = new Intent(activity,AddTextActivity.class);
        activity.startActivityForResult(intent,requestCode);
    }
}
