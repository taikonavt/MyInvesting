package com.example.maxim.myinvesting.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.widget.Toast;

import com.example.maxim.myinvesting.MainActivity;
import com.example.maxim.myinvesting.R;
import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.data.PortfolioItem;

import static com.example.maxim.myinvesting.data.Const.MILLIS_IN_DAY;
import static com.example.maxim.myinvesting.data.Const.TAG;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by maxim on 09.08.17.
 */

public class PortfolioLoader extends AsyncTaskLoader<ArrayList<PortfolioItem>> {

    private Context mContext;

    public PortfolioLoader(Context context) {
        super(context);

        mContext = context;
    }

    @Override
    public ArrayList<PortfolioItem> loadInBackground() {

        Uri uri = Contract.PortfolioEntry.CONTENT_URI.buildUpon()
                .appendPath(
                        Contract.DealsEntry.COLUMN_TICKER)
                .build();

        // SELECT ticker
        String [] projection = {
                Contract.DealsEntry.COLUMN_TICKER
        };

        // WHERE portfolio = '5838194'
        String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = "
                + ((MainActivity) mContext).getNameOfPortfolio();

        // ORDER BY ticker ASC
        String orderBy = Contract.DealsEntry.COLUMN_TICKER + " ASC";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                orderBy);

        ArrayList<PortfolioItem> arrayList = null;

        if (cursor.moveToFirst()) {

            int numOfRows = cursor.getCount();

            arrayList = new ArrayList<>(numOfRows);

            int tickerIndex = cursor.getColumnIndex(Contract.DealsEntry.COLUMN_TICKER);

            int i = 0;

            do {

                String ticker = cursor.getString(tickerIndex);

                int volume = getVolume(ticker);

                int price = getPrice(ticker);

                getInputs(((MainActivity) mContext).getNameOfPortfolio(),
                        Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow")).
                                getTimeInMillis());

                getBuys(((MainActivity) mContext).getNameOfPortfolio(),
                        Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow")).
                                getTimeInMillis());

                getSells(((MainActivity) mContext).getNameOfPortfolio(),
                        Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow")).
                                getTimeInMillis());

                getDividends(((MainActivity) mContext).getNameOfPortfolio(),
                        Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow")).
                                getTimeInMillis());

                getAverageInvestment(((MainActivity) mContext).getNameOfPortfolio(),
                        Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow")).
                                getTimeInMillis());

                PortfolioItem portfolioItem = new PortfolioItem(i, ticker, volume, price);
                arrayList.add(portfolioItem);

                i++;

            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, "Portfolio have 0 items");
//            Toast.makeText(mContext, "Portfolio have 0 items", Toast.LENGTH_LONG).show();
        }

        cursor.close();

        return arrayList;
    }

    // Получение количества акций Ticker в портфеле
    private int getVolume(String lTicker) {

        int volumeBuy = 0;
        int volumeSell = 0;
        int volumeResult;

            Uri uri = Contract.PortfolioEntry.CONTENT_URI.buildUpon()
                    .appendPath(
                            Contract.DealsEntry.COLUMN_TICKER) // GROUP BY ticker
                    .build();

            // SELECT ticker
            String[] projection = {
                    Contract.DealsEntry.COLUMN_TICKER,
                    "sum (" + Contract.DealsEntry.COLUMN_VOLUME
                            + ") AS '" + Contract.DealsEntry.COLUMN_VOLUME + "'"
            };

            // WHERE portfolio = '5838194' for cursorBuy
            String selectionBuy = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                    ((MainActivity) mContext).getNameOfPortfolio() + "' AND " +
                    Contract.DealsEntry.COLUMN_TICKER + " = '" + lTicker + "' AND " +
                    Contract.DealsEntry.COLUMN_TYPE + " = 'Buy'";

            Cursor cursorBuy = getContext().getContentResolver().query(
                    uri,
                    projection,
                    selectionBuy,
                    null,
                    null);

            if (cursorBuy.moveToFirst()) {

                int volumeBuyIndex = cursorBuy.getColumnIndex(Contract.DealsEntry.COLUMN_VOLUME);
                volumeBuy = cursorBuy.getInt(volumeBuyIndex);
            }

            cursorBuy.close();

            // WHERE portfolio = '5838194' for cursorSell
            String selectionSell = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                    ((MainActivity) mContext).getNameOfPortfolio() + "' AND " +
                    Contract.DealsEntry.COLUMN_TICKER + " = '" + lTicker + "' AND " +
                    Contract.DealsEntry.COLUMN_TYPE + " = 'Sell'";

            Cursor cursorSell = getContext().getContentResolver().query(
                    uri,
                    projection,
                    selectionSell,
                    null,
                    null);

            if (cursorSell.moveToFirst()) {

                int volumeSellIndex = cursorSell.getColumnIndex(Contract.DealsEntry.COLUMN_VOLUME);
                volumeSell = cursorSell.getInt(volumeSellIndex);
            }

            cursorSell.close();

            volumeResult = volumeBuy - volumeSell;

        return volumeResult;
    }

    private int getPrice(String lTicker) {

        return NetworkUtils.getCurrentPrice(lTicker);
    }

    // получение суммы всех вводов
    private void getInputs(String lPortfolio, long lDate) {

        int amountInput = 0;

        String [] strings = mContext.getResources().getStringArray(R.array.spinType_input_array);

        Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                .appendPath(Contract.PATH_INPUT)
                .appendPath(Contract.PATH_SUM)
                .build();

        // SELECT portfolio, sum(amount) AS 'amount'
        String[] projection = {
                Contract.InputEntry.COLUMN_PORTFOLIO,
                "sum (" + Contract.InputEntry.COLUMN_AMOUNT + ") AS '" +
                        Contract.InputEntry.COLUMN_AMOUNT + "'"
        };

        // WHERE portfolio = '5838199' AND date < 123 AND type = 'Input'
        String selection = Contract.InputEntry.COLUMN_PORTFOLIO + " = '" +
                lPortfolio + "' AND " +
                Contract.InputEntry.COLUMN_DATE + " < " + lDate + " AND " +
                Contract.InputEntry.COLUMN_TYPE + " = '" + strings[0] + "'";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                null);

        if (cursor.moveToFirst()) {

            int index = cursor.getColumnIndex(Contract.InputEntry.COLUMN_AMOUNT);
            amountInput = cursor.getInt(index);
        }

        cursor.close();
    }

    // получение суммы всех выводов
    private void getOutputs(String lPortfolio, long lDate) {

        int amountOutput = 0;

        String [] strings = mContext.getResources().getStringArray(R.array.spinType_input_array);

        Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                .appendPath(Contract.PATH_INPUT)
                .appendPath(Contract.PATH_SUM)
                .build();

        // SELECT portfolio, sum(amount) AS 'amount'
        String[] projection = {
                Contract.InputEntry.COLUMN_PORTFOLIO,
                "sum (" + Contract.InputEntry.COLUMN_AMOUNT + ") AS '" +
                        Contract.InputEntry.COLUMN_AMOUNT + "'"
        };

        // WHERE portfolio = '5838199' AND date < 123 AND type = 'Output'
        String selection = Contract.InputEntry.COLUMN_PORTFOLIO + " = '" +
                lPortfolio + "' AND " +
                Contract.InputEntry.COLUMN_DATE + " < " + lDate + " AND " +
                Contract.InputEntry.COLUMN_TYPE + " = '" + strings[1] + "'";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                null);

        if (cursor.moveToFirst()) {

            int index = cursor.getColumnIndex(Contract.InputEntry.COLUMN_AMOUNT);
            amountOutput = cursor.getInt(index);
        }

        cursor.close();
    }

    // получение суммы всех покупок акций
    private void getBuys(String lPortfolio, long lDate) {

        int costOfBuys = 0;
        final String COLUMN_COST = "cost";

        String [] strings = mContext.getResources().getStringArray(R.array.spinType_deal_array);

        Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                .appendPath(Contract.PATH_DEALS)
                .appendPath(Contract.PATH_SUM)
                .build();

        // SELECT portfolio, sum(price * volume) AS 'cost'
        String[] projection = {
                Contract.DealsEntry.COLUMN_PORTFOLIO,
                "sum (" + Contract.DealsEntry.COLUMN_PRICE + " * " +
                        Contract.DealsEntry.COLUMN_VOLUME+ ") AS '" +
                        COLUMN_COST + "'"
        };

        // WHERE portfolio = '5838199' AND date < 123 AND type = 'Buy'
        String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                lPortfolio + "' AND " +
                Contract.DealsEntry.COLUMN_DATE + " < " + lDate + " AND " +
                Contract.DealsEntry.COLUMN_TYPE + " = '" + strings[1] + "'";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                null);

        if (cursor.moveToFirst()) {

            int index = cursor.getColumnIndex(COLUMN_COST);
            costOfBuys = cursor.getInt(index);
        }

        cursor.close();
    }

    // получение суммы всех продаж акций
    private void getSells (String lPortfolio, long lDate) {

        int costOfSells = 0;
        final String COLUMN_COST = "cost";

        String [] strings = mContext.getResources().getStringArray(R.array.spinType_deal_array);

        Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                .appendPath(Contract.PATH_DEALS)
                .appendPath(Contract.PATH_SUM)
                .build();

        // SELECT portfolio, sum(price * volume) AS 'cost'
        String[] projection = {
                Contract.DealsEntry.COLUMN_PORTFOLIO,
                "sum (" + Contract.DealsEntry.COLUMN_PRICE + " * " +
                        Contract.DealsEntry.COLUMN_VOLUME+ ") AS '" +
                        COLUMN_COST + "'"
        };

        // WHERE portfolio = '5838199' AND date < 123 AND type = 'Sell'
        String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                lPortfolio + "' AND " +
                Contract.DealsEntry.COLUMN_DATE + " < " + lDate + " AND " +
                Contract.DealsEntry.COLUMN_TYPE + " = '" + strings[0] + "'";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                null);

        if (cursor.moveToFirst()) {

            int index = cursor.getColumnIndex(COLUMN_COST);
            costOfSells = cursor.getInt(index);
        }

        cursor.close();
    }

    // получение суммы всех дивидендов
    private void getDividends (String lPortfolio, long lDate) {

        int costOfDivs = 0;
        final String COLUMN_COST = "cost";

        String [] strings = mContext.getResources().getStringArray(R.array.spinType_deal_array);

        Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                .appendPath(Contract.PATH_DEALS)
                .appendPath(Contract.PATH_SUM)
                .build();

        // SELECT portfolio, sum(price * volume) AS 'cost'
        String[] projection = {
                Contract.DealsEntry.COLUMN_PORTFOLIO,
                "sum (" + Contract.DealsEntry.COLUMN_PRICE + " * " +
                        Contract.DealsEntry.COLUMN_VOLUME+ ") AS '" +
                        COLUMN_COST + "'"
        };

        // WHERE portfolio = '5838199' AND date < 123 AND type = 'Dividend'
        String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                lPortfolio + "' AND " +
                Contract.DealsEntry.COLUMN_DATE + " < " + lDate + " AND " +
                Contract.DealsEntry.COLUMN_TYPE + " = '" + strings[2] + "'";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                null);

        if (cursor.moveToFirst()) {

            int index = cursor.getColumnIndex(COLUMN_COST);
            costOfDivs = cursor.getInt(index);
        }

        cursor.close();
    }

    // получаю средневзвешенную сумму инвестиций за период
    private void getAverageInvestment(String lPortfolio, long lDate) {

        // переменная для хранения текущего результата
        long averageInvestment = 0;
        // переменная для подсчета общего количества дней
        int totalDays = 0;

        // 1) делаю запрос к базе данных
        Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                .appendPath(Contract.PATH_INPUT)
                .build();

        // SELECT portfolio, type, amount, date
        String[] projection = {
                Contract.InputEntry.COLUMN_PORTFOLIO,
                Contract.InputEntry.COLUMN_TYPE,
                Contract.InputEntry.COLUMN_AMOUNT,
                Contract.InputEntry.COLUMN_DATE
        };

        // WHERE portfolio = '5838199' AND date < 123
        String selection = Contract.InputEntry.COLUMN_PORTFOLIO + " = '" +
                lPortfolio + "' AND " +
                Contract.InputEntry.COLUMN_DATE + " < " + lDate;

        String sortOrder = Contract.InputEntry.COLUMN_DATE + " ASC";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                sortOrder);

        // 2) читаю курсор
        if (cursor.moveToFirst()) {

            long previousTime = 0; // время начала подпериода

            int dateIndex = cursor.getColumnIndex(Contract.InputEntry.COLUMN_DATE);
            int typeIndex = cursor.getColumnIndex(Contract.InputEntry.COLUMN_TYPE);
            int amountIndex = cursor.getColumnIndex(Contract.InputEntry.COLUMN_AMOUNT);

            // переменная для хранения считанного значения количества Input
            long tempAmount;

            // тип операции (Input, Output)
            String[] strings = mContext.getResources().getStringArray(R.array.spinType_input_array);

            // 3) читаю первую строку курсора
            // если тип = Input то amount берем со знаком "+"
            if (cursor.getString(typeIndex).equals(strings[0])) {
                tempAmount = cursor.getInt(amountIndex);
            }
            // если тип = Output то amount берем со знаком "-"
            else if (cursor.getString(typeIndex).equals(strings[1])) {
                tempAmount = - cursor.getInt(amountIndex);
            // если тип = чему то другому, то пишем в лог
            } else {
                tempAmount = 0;
                Log.d(TAG, cursor.getString(typeIndex) + "Не верный тип ввода");
            }

            // считываем начало периода из курсора
            previousTime = cursor.getLong(dateIndex);

            long amountForZeroCase = 0; // хранение количества ввода(вывода) в случае нулевого периода
            long sumOfPreviousInputs = 0; // сумма всех предыдущих вводов

            // 4) читаю значения со второй до предпоследней строки курсора
            while (cursor.moveToNext()){

                // конец подпериода
                long tempTime = cursor.getLong(dateIndex);

                // длительность подпериода
                long tempPeriod = tempTime - previousTime;

                // длительность подпериода в днях
                int periodDays = (int) (tempPeriod / MILLIS_IN_DAY);

                // общее количество дней расчитываемого периода
                totalDays = totalDays + periodDays;

                // TODO: 18.08.17 сделать решение для случая нескольких подпериодов = 0
                // если подпериод = 0, то сохраняем вносимое количество до следующего цикла
                if (periodDays == 0) {

                    amountForZeroCase = tempAmount;

                } else {

                    // считаем сумму для подпериода
                    sumOfPreviousInputs = sumOfPreviousInputs + tempAmount + amountForZeroCase;

                    // умножаем сумму подпериода на длительность подпериода и прибавляем
                    // к предыдущему результату инвестирования
                    averageInvestment = averageInvestment + (periodDays * sumOfPreviousInputs);

                    // обнуляем случай нулевого подпериода
                    amountForZeroCase = 0;
                }

                if (cursor.getString(typeIndex).equals(strings[0])) {
                    tempAmount = cursor.getInt(amountIndex);
                }
                else if (cursor.getString(typeIndex).equals(strings[1])) {

                    tempAmount = - cursor.getInt(amountIndex);
                } else {
                    tempAmount = 0;
                    Log.d(TAG, "Не верный тип ввода");
                }

                // задаю начало слудующего подпериода
                previousTime = tempTime;
            }

            // 5) читаю последнюю строку курсора

            // последний подпериод
            long tempPeriod = lDate - previousTime;

            int periodDays = (int) (tempPeriod / MILLIS_IN_DAY);
            totalDays = totalDays + periodDays;

            if (periodDays == 0) {

                amountForZeroCase = tempAmount;

            } else {

                sumOfPreviousInputs = sumOfPreviousInputs + tempAmount + amountForZeroCase;

                averageInvestment = averageInvestment + (periodDays * sumOfPreviousInputs);

                amountForZeroCase = 0;
            }

            averageInvestment = averageInvestment / totalDays;
        }

        cursor.close();
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
    public void onCanceled(ArrayList<PortfolioItem> data) {
        super.onCanceled(data);
        onReleaseResources();
    }

    protected void onReleaseResources() {

    }


}