package com.example.codeexp2021app2;

import android.app.Application;

import com.example.codeexp2021app2.utils.ContextUtils;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ContextUtils.init(this);
    }
}
