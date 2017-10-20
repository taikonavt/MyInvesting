package com.example.maxim.myinvesting.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.data.PortfolioItem;
import com.example.maxim.myinvesting.data.TickerItem;

import static com.example.maxim.myinvesting.data.Const.TAG;

import java.util.ArrayList;

/**
 * Created by maxim on 16.10.17.
 */

public class TickerLoader extends AsyncTaskLoader<ArrayList<TickerItem>> {

    private String nameOfPortfolio;
    private PortfolioItem portfolioItem;

    public TickerLoader(Context context, String nameOfPortfolio, PortfolioItem portfolioItem) {
        super(context);

        this.nameOfPortfolio = nameOfPortfolio;
        this.portfolioItem = portfolioItem;
    }

    @Override
    public ArrayList<TickerItem> loadInBackground() {
// TODO: 20.10.17 Добавить дивиденды в список (? подумать нужно ли)
        Cursor cursorBuy = getCursor("Buy");

        Cursor cursorSell = getCursor("Sell");

        ArrayList<TickerItem> arrayList = new ArrayList<>();

        boolean cursorBuyFlag = false;
        boolean cursorSellFlag = false;

        if (cursorBuy.moveToFirst()) cursorBuyFlag = true;
        if (cursorSell.moveToFirst()) cursorSellFlag = true;

        int volumeIndexBuy = cursorBuy.getColumnIndex(Contract.DealsEntry.COLUMN_VOLUME);
        int priceIndexBuy = cursorBuy.getColumnIndex(Contract.DealsEntry.COLUMN_PRICE);
        int dateIndexBuy = cursorBuy.getColumnIndex(Contract.DealsEntry.COLUMN_DATE);

        int volumeIndexSell = cursorSell.getColumnIndex(Contract.DealsEntry.COLUMN_VOLUME);
        int priceIndexSell = cursorSell.getColumnIndex(Contract.DealsEntry.COLUMN_PRICE);
        int dateIndexSell = cursorSell.getColumnIndex(Contract.DealsEntry.COLUMN_DATE);

        // используется для хранения разницы между объемом продажи и покупки для следующего этапа цикла
        int volumeTemp = 0;

        int volumeBuy = 0;
        long priceBuy = 0;
        long dateBuy = 0;

        int volumeSell = 0;
        long priceSell = 0;
        long dateSell = 0;

        do {
            // на предыдущем этапе vb=vs, поэтому ничего не переносится
            if (volumeTemp == 0) {

                // на предыдущем этапе были прочитаны оба курсора
                // вариант: 1. vb=10, vb=10; 2. vb=10, vb=10
                if (cursorBuyFlag && cursorSellFlag) {

                    volumeBuy = cursorBuy.getInt(volumeIndexBuy);
                    priceBuy = cursorBuy.getLong(priceIndexBuy);
                    dateBuy = cursorBuy.getLong(dateIndexBuy);

                    volumeSell = cursorSell.getInt(volumeIndexSell);
                    priceSell = cursorSell.getLong(priceIndexSell);
                    dateSell = cursorSell.getLong(dateIndexSell);

                    // выбираю меньший объем и устанавливаю его в массив
                    chooseVolumeAndSetTickerItem(arrayList, volumeBuy, priceBuy, dateBuy,
                            volumeSell, priceSell, dateSell);

                    // разницу переношу на следующий цикл
                    volumeTemp = volumeBuy - volumeSell;
                }

                // на предыдущем этапе был прочитан только бай курсор
                // вариант: 1. vb=10, vb=10; 2. vb=10
                else if (cursorBuyFlag && !cursorSellFlag) {

                    volumeBuy = cursorBuy.getInt(volumeIndexBuy);
                    priceBuy = cursorBuy.getLong(priceIndexBuy);
                    dateBuy = cursorBuy.getLong(dateIndexBuy);

//                    volumeSell = 0;
//                    priceSell = 0;
                    dateSell = DateUtils.getCurrentDateForMoscowInMillis();

                    // устанавливаю в массив объем бай как есть, объем сэл = объем бай
                    arrayList.add(new TickerItem(volumeBuy, priceBuy, dateBuy,
                            volumeBuy, priceBuy, dateSell));

                    // на следующий этап отправляю 0
                    volumeTemp = 0;
                }

                // на предыдущем этапе был прочитан только сел курсор
                // вариант: 1. vb=10, vb=10; 2. vs=10
                else if (!cursorBuyFlag && cursorSellFlag) {

//                    volumeBuy = 0;
//                    priceBuy = 0;
                    dateBuy = DateUtils.getCurrentDateForMoscowInMillis();

                    volumeSell = cursorSell.getInt(volumeIndexSell);
                    priceSell = cursorSell.getLong(priceIndexSell);
                    dateSell = cursorSell.getLong(dateIndexSell);

                    // устанавливаю в массив объем сэл как есть, объем бай = объем сэл
                    arrayList.add(new TickerItem(volumeSell, priceSell, dateBuy,
                            volumeSell, priceSell, dateSell));

                    // на следующий этап отправляю 0
                    volumeTemp = 0;
                }

                // вариант если нет ни одной строки - ничего не пишу
//                else if (!cursorBuyFlag && !cursorSellFlag) {
//                }

                cursorBuyFlag = cursorBuy.moveToNext();
                cursorSellFlag = cursorSell.moveToNext();
            }

            // на предыдущем этапе vb > vs
            else if (volumeTemp > 0) {

                volumeBuy = volumeTemp;

                // читаю только курсор сэл, в качестве курсора бай использую то, что остальность
                // с предыдущего этапа

                // если курсор сэл существует
                if (cursorSellFlag) {
                    volumeSell = cursorSell.getInt(volumeIndexSell);
                    priceSell = cursorSell.getLong(priceIndexSell);
                    dateSell = cursorSell.getLong(dateIndexSell);

                    chooseVolumeAndSetTickerItem(arrayList, volumeBuy, priceBuy, dateBuy,
                            volumeSell, priceSell, dateSell);

                    volumeTemp = volumeBuy - volumeSell;
                }
                // если курсора сэл нет
                else {
//                    volumeSell = 0;
//                    priceSell = 0;
                    dateSell = DateUtils.getCurrentDateForMoscowInMillis();

                    // если больше строк нет, то не нужно выбирать меньшее значение, а ставить как есть
                    arrayList.add(new TickerItem(volumeBuy, priceBuy, dateBuy,
                            volumeBuy, priceBuy, dateSell));

                    // на следующий этап отправляю 0
                    volumeTemp = 0;
                }

                cursorSellFlag = cursorSell.moveToNext();
            }

            // на предыдущем этапе vb < vs
            else if (volumeTemp < 0) {

                volumeSell = - volumeTemp;

                // читаю только курсор бай, в качестве курсора сэл использую то, что остальность
                // с предыдущего этапа

                // если курсор бай существует
                if (cursorBuyFlag) {
                    volumeBuy = cursorBuy.getInt(volumeIndexBuy);
                    priceBuy = cursorBuy.getLong(priceIndexBuy);
                    dateBuy = cursorBuy.getLong(dateIndexBuy);

                    // выбираю меньшее значение объема и устанавливаю в массив
                    chooseVolumeAndSetTickerItem(arrayList, volumeBuy, priceBuy, dateBuy,
                            volumeSell, priceSell, dateSell);

                    // на следующий этап отправляю 0
                    volumeTemp = volumeBuy - volumeSell;

                } else {
//                    volumeBuy = 0;
//                    priceBuy = 0;
                    dateBuy = DateUtils.getCurrentDateForMoscowInMillis();

                    // если больше строк нет, то не нужно выбирать меньшее значение, а ставить как есть
                    arrayList.add(new TickerItem(volumeSell, priceSell, dateBuy,
                            volumeSell, priceSell, dateSell));

                    // на следующий этап отправляю 0
                    volumeTemp = 0;
                }

                cursorBuyFlag = cursorBuy.moveToNext();
            }

        } while (cursorBuyFlag || cursorSellFlag || (volumeTemp != 0));
        // volumeTemp != 0 ввожу для случая 1. vt = 0, vb=20, vs=10, 0; 2. vt = 10, vb - false, vs - false

        cursorBuy.close();
        cursorSell.close();

        return arrayList;
    }

    // выбираю меньшее значение и устанавливаю его в массив
    private void chooseVolumeAndSetTickerItem(ArrayList<TickerItem> arrayList,
                               int volumeBuy, long priceBuy, long dateBuy,
                               int volumeSell, long priceSell, long dateSell) {

        TickerItem tickerItem;

        // выбираю меньшее значение для установки чтобы число купленных акций соответствовало проданым
        if (volumeBuy >= volumeSell) {

            tickerItem = new TickerItem(volumeSell, priceBuy, dateBuy,
                    volumeSell, priceSell, dateSell);
        }
        else {

            tickerItem = new TickerItem(volumeBuy, priceBuy, dateBuy,
                    volumeBuy, priceSell, dateSell);
        }

        arrayList.add(tickerItem);
    }


    private Cursor getCursor(String buyOrSell) {

        // GROUP_BY date,price
        Uri uri = Contract.DealsEntry.CONTENT_URI.buildUpon()
                .appendPath(Contract.PATH_DATE_PRICE)
                .build();

        // SELECT sum(volume) AS volume, price, date
        String [] projection = {
                "sum(" + Contract.DealsEntry.COLUMN_VOLUME + ") AS 'volume'",
                Contract.DealsEntry.COLUMN_PRICE,
                Contract.DealsEntry.COLUMN_DATE
        };

        // WHERE portfolio = '123' AND type = 'Buy' AND ticker = 'SBER'
        String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                nameOfPortfolio + "' AND " +
                Contract.DealsEntry.COLUMN_TYPE + " = '" + buyOrSell + "' AND " +
                Contract.DealsEntry.COLUMN_TICKER + " = '" +
                portfolioItem.getTicker() + "'";

        //ORDER_BY date ASC
        String orderBy = Contract.DealsEntry.COLUMN_DATE + " ASC";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                orderBy);

        return cursor;
    }

    @Override
    protected void onStartLoading() {

        super.onStartLoading();
        forceLoad();
    }

    @Override
    protected void onStopLoading() {

        cancelLoad();
    }

    @Override
    public void onCanceled(ArrayList<TickerItem> data) {

        super.onCanceled(data);
    }

    @Override
    protected void onReset() {

        super.onReset();

        onStopLoading();
    }

}
