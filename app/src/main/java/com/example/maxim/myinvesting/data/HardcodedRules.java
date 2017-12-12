package com.example.maxim.myinvesting.data;

import android.util.Log;

import com.example.maxim.myinvesting.utilities.DateUtils;

import java.awt.font.TextAttribute;
import java.util.Calendar;

import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 12.12.17.
 */

public class HardcodedRules {

    public PriceAndVolume checkTicker(String ticker, long time, long price, int volume) {

        PriceAndVolume priceAndVolume = new PriceAndVolume();

        priceAndVolume.price = price;

        priceAndVolume.volume = volume;

        if (ticker.equals("IRAO") && (time < DateUtils.getTimeForMoscowInMillis(2015, Calendar.JANUARY, 1))) {

            priceAndVolume.price = priceAndVolume.price * 100;

            priceAndVolume.volume = priceAndVolume.volume / 100;
        }

        return priceAndVolume;
    }

    public class PriceAndVolume {

        long price;

        int volume;

        public long getPrice() {
            return price;
        }

        public int getVolume() {
            return volume;
        }
    }
}
