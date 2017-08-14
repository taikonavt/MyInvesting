package com.example.maxim.myinvesting.utilities;

import android.util.Log;
import android.widget.Toast;

import static com.example.maxim.myinvesting.data.Const.*;

/**
 * Created by maxim on 14.08.17.
 */

public class NetworkUtils {

    public static int getCurrentPrice (String lTicker) {

        switch (lTicker) {

            case ("SBER"):
                return 165*MULTIPLIER_FOR_MONEY;
            case ("MSTT"):
                return (int) 120.2 * MULTIPLIER_FOR_MONEY;
            case ("AFKS"):
                return (int) 11.82 * MULTIPLIER_FOR_MONEY;
            case ("MTSS"):
                return (int) 243.85 * MULTIPLIER_FOR_MONEY;
            case ("MAGN"):
                return (int) 34.2 * MULTIPLIER_FOR_MONEY;
            default:
                Log.d(TAG, "Не известный тикер");
                return 0;
        }
    }
}
