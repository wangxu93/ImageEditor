package com.yjing.imageeditlibrary.module;

import android.app.Activity;



/**
 * Created by sdj on 2017/5/3.
 */

public class SelectSmailHelper {
    private SelectSmailCallbacks callbacks;


    private SelectSmailHelper() {
    }

    public static SelectSmailHelper getInstance() {
        return SingleHolder.sInstance;
    }

    private static class SingleHolder {
        private static SelectSmailHelper sInstance;

        static {
            sInstance = new SelectSmailHelper();
        }
    }

    public void setCallbacks(SelectSmailCallbacks callbacks) {
        this.callbacks = callbacks;
    }


    public void openSelectSmailPage(Activity activity, int requestCode) {
        if (callbacks != null) {
            callbacks.openSelectSmailPage(activity,requestCode);
        }
    }


}
