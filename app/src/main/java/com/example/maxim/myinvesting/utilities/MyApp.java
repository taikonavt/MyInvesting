package com.example.maxim.myinvesting.utilities;

import android.app.Application;
import android.content.Context;

/**
 * Created by maxim on 21.11.17.
 */

// возвращает AplicationContext
// TODO: 21.11.17 Посмотреть где лучше использовать, поменять где надо.

public class MyApp extends Application {

    private static MyApp instance;

    public MyApp() {
        instance = this;
    }

    public static Context getAppContext() {
        return instance;
    }
}
