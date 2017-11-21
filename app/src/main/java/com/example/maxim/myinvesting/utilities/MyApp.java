package com.example.maxim.myinvesting.utilities;

import android.app.Application;
import android.content.Context;

/**
 * Created by maxim on 21.11.17.
 */

public class MyApp extends Application {

    private static MyApp instance;

    public MyApp() {
        instance = this;
    }

    public static Context getAppContext() {
        return instance;
    }
}
