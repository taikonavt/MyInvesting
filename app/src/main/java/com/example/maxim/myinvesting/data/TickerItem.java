package com.example.maxim.myinvesting.data;

import static com.example.maxim.myinvesting.data.Const.MILLIS_IN_DAY;

/**
 * Created by maxim on 16.10.17.
 */

public class TickerItem {

    private int volumeBuy;
    private int volumeSell;

    private long priceBuy;
    private long priceSell;

    private long dateBuy;
    private long dateSell;

    public TickerItem(int volumeBuy, long priceBuy, long dateBuy,
               int volumeSell, long priceSell, long dateSell) {

        this.volumeBuy = volumeBuy;
        this.volumeSell = volumeSell;
        this.priceBuy = priceBuy;
        this.priceSell = priceSell;
        this.dateBuy = dateBuy;
        this.dateSell = dateSell;
    }

    public int getVolumeBuy() {
        return volumeBuy;
    }

    public int getVolumeSell() {
        return volumeSell;
    }

    public long getDateBuy() {
        return dateBuy;
    }

    public long getDateSell() {
        return dateSell;
    }

    public long getPriceBuy() {
        return priceBuy;
    }

    public long getPriceSell() {
        return priceSell;
    }

    public int getPeriod() {

        return (int) ((dateSell - dateBuy) / MILLIS_IN_DAY);
    }

    public long getProfit() {

        return volumeSell * priceSell - volumeBuy * priceBuy;
    }

}