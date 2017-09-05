package com.example.maxim.myinvesting.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.widget.Toast;

import com.example.maxim.myinvesting.MainActivity;
import com.example.maxim.myinvesting.R;
import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.data.PortfolioData;
import com.example.maxim.myinvesting.data.PortfolioItem;

import static com.example.maxim.myinvesting.data.Const.MILLIS_IN_DAY;
import static com.example.maxim.myinvesting.data.Const.TAG;
import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by maxim on 09.08.17.
 */

public class PortfolioLoader extends AsyncTaskLoader<PortfolioData> {

    private Context mContext;

    private final int DAYS_IN_YEAR = 365;

    public PortfolioLoader(Context context) {
        super(context);

        mContext = context;
    }

    @Override
    public PortfolioData loadInBackground() {

        PortfolioData portfolioData = new PortfolioData(
                        ((MainActivity) mContext).getNameOfPortfolio(),
                        Calendar.getInstance().getTimeInMillis());

        setPortfolioListAndCost(portfolioData);

        setPortfolioProfitabilityAndPeriod(portfolioData);

        return portfolioData;
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
    public void onCanceled(PortfolioData data) {

        super.onCanceled(data);
        onReleaseResources();
    }

    @Override
    protected void onReset() {

        super.onReset();

        onStopLoading();
    }

    private void onReleaseResources() {
    }

    // Устанавливает в portfolioData массив с акциями портфеля и стоимость всего портфеля
    private void setPortfolioListAndCost(PortfolioData data) {

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

        ArrayList<PortfolioItem> arrayList = new ArrayList<>();
        long costOfItems = 0;
        int i = 0; // id для строк массива

        if (cursor.moveToFirst()) {

            int tickerIndex = cursor.getColumnIndex(Contract.DealsEntry.COLUMN_TICKER);

            do {

                String ticker = cursor.getString(tickerIndex);

                int volume = getVolume(ticker);

                int price = getPrice(ticker);

                costOfItems = costOfItems + (volume * price);

                double profit = getProfitOfShare(data.getName(), data.getUntilDateInMillis(),
                        ticker, volume*price);

                PortfolioItem portfolioItem = new PortfolioItem(i, ticker, volume, price, profit);
                arrayList.add(portfolioItem);

                i++;

            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, "Portfolio have 0 items");
//            Toast.makeText(mContext, "Portfolio have 0 items", Toast.LENGTH_LONG).show();
        }

        cursor.close();

        setPortfolioCost(data, costOfItems);

        // вычисляю и добавляю в массив количество свободных денег
        int freeMoney = (int) (data.getCostOfPortfolio() - costOfItems);

        arrayList.add(new PortfolioItem(i, "RUB", 1, freeMoney, 0));

        data.setPortfolioItems(arrayList);
    }

    // устанавливаю в portfolioData стоимость портфеля
    private void setPortfolioCost(PortfolioData data, long costOfItems) {

        String name = data.getName();
        long date = data.getUntilDateInMillis();

        long costOfPortfolio = getInputs(name, date) - getBuys(name, date) +
                getSells(name, date) + costOfItems + getDividends(name, date) -
                getOutputs(name, date);

        data.setCostOfPortfolio(costOfPortfolio);
    }

    // устанавливаю в portfolioData средневзвешенную доходность в разах (0,1 = 10%) годовых и
    // длительность существования портфеля
    private void setPortfolioProfitabilityAndPeriod(PortfolioData data) {

        // чистый доход за все время
        long netProfit = data.getCostOfPortfolio() +
                getOutputs(data.getName(), data.getUntilDateInMillis()) -
                getInputs(data.getName(), data.getUntilDateInMillis());

        // средневзвешенные инвестиции за все время
        AverageInvestment averageInvestment =
                new AverageInvestment(data.getName(), data.getUntilDateInMillis());

        // длительность существования портфеля
        data.setPeriodInDays(averageInvestment.getTotalDays());

        double profitability = 0;

        // считаю доходность: D = (1 + dS / V)^365/T -1;
        try {
            profitability = (pow((1 + ((double) netProfit / averageInvestment.getAverageInvestment())),
                    ((double) DAYS_IN_YEAR / averageInvestment.getTotalDays())) - 1);

        } catch (ArithmeticException e) {
            // если в знаменателе 0, то пишу в лог. Доходность остается = 0
            Log.d(TAG, "Ошибка в вычислениях " + PortfolioLoader.class.getSimpleName());
            e.printStackTrace();
        }

        data.setProfitability(profitability);
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

        NetworkUtils networkUtils = new NetworkUtils();

        return networkUtils.getCurrentPrice(lTicker);
    }

    // получение суммы всех вводов
    private long getInputs(String lPortfolio, long lDate) {

        long amountInput = 0;

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
            amountInput = cursor.getLong(index);
        }

        cursor.close();

        return amountInput;
    }

    // получение суммы всех выводов
    private long getOutputs(String lPortfolio, long lDate) {

        long amountOutput = 0;

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
            amountOutput = cursor.getLong(index);
        }

        cursor.close();

        return amountOutput;
    }

    // получение суммы всех покупок акций
    private long getBuys(String lPortfolio, long lDate) {

        long costOfBuys = 0;
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
            costOfBuys = cursor.getLong(index);
        }

        cursor.close();

        return costOfBuys;
    }

    // получение суммы всех продаж акций
    private long getSells (String lPortfolio, long lDate) {

        long costOfSells = 0;
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
            costOfSells = cursor.getLong(index);
        }

        cursor.close();

        return costOfSells;
    }

    // получение суммы всех дивидендов
    private long getDividends (String lPortfolio, long lDate) {

        long costOfDivs = 0;
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
            costOfDivs = cursor.getLong(index);
        }

        cursor.close();

        return costOfDivs;
    }


    // класс для хранения и вычисления средневзвешенных инвестиций и
    // длительности существования портфеля
    private class AverageInvestment {

        // переменная для хранения общего количества дней
        int totalDays = 0;

        // переменная для хранения текущего результата
        long averageInvestment = 0;

        // получаю средневзвешенную сумму инвестиций за период и подсчитываю период
        private AverageInvestment(String lPortfolio, long lDate) {

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

                        // если денег выведено больше чем введено, то подпериод не учитывается
                        if (sumOfPreviousInputs <= 0) {
                            periodDays = 0;
                            sumOfPreviousInputs = 0;
                        }

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

                    // если денег выведено больше чем введено, то подпериод не учитывается
                    if (sumOfPreviousInputs <= 0) {
                        periodDays = 0;
                        sumOfPreviousInputs = 0;
                    }

                    averageInvestment = averageInvestment + (periodDays * sumOfPreviousInputs);

                    amountForZeroCase = 0;
                }

                averageInvestment = averageInvestment / totalDays;
            }

            cursor.close();
        }

        long getAverageInvestment() {

            return averageInvestment;
        }

        int getTotalDays() {

            return totalDays;
        }
    }

    // TODO: 05.09.17 Удалить если не понадобиться
    private double getProfitOfShare(String lPortfolio, long untilDate, String lTicker, long lCost) {

        long netProfit = lCost - getBuysOfTicker(lPortfolio, lTicker, untilDate) +
                getSellsOfTicker(lPortfolio, lTicker, untilDate) +
                getDividendsOfTicker(lPortfolio, lTicker, untilDate);

        AverageInvestmentOfTicker investment = new AverageInvestmentOfTicker(
                lPortfolio, lTicker, untilDate);

        double profitability = 0;

        // считаю доходность: D = (1 + dS / V)^365/T -1;
        try {
            profitability = (pow((1 + ((double) netProfit / investment.getAverageInvestment())),
                    ((double) DAYS_IN_YEAR / investment.getTotalDays())) - 1);

        } catch (ArithmeticException e) {
            // если в знаменателе 0, то пишу в лог. Доходность остается = 0
            Log.d(TAG, "Ошибка в вычислениях " + PortfolioLoader.class.getSimpleName());
            e.printStackTrace();
        }

        return profitability;
    }

    // получение суммы всех покупок конкретной акции
    private long getBuysOfTicker (String lPortfolio, String lTicker, long lDate) {

        long costOfBuys = 0;
        final String COLUMN_COST = "cost";

        String [] strings = mContext.getResources().getStringArray(R.array.spinType_deal_array);

        Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                .appendPath(Contract.PATH_DEALS)
                .appendPath(Contract.PATH_SUM)
                .build();

        // SELECT portfolio, ticker, sum(price * volume) AS 'cost'
        String[] projection = {
                Contract.DealsEntry.COLUMN_PORTFOLIO,
                Contract.DealsEntry.COLUMN_TICKER,
                "sum (" + Contract.DealsEntry.COLUMN_PRICE + " * " +
                        Contract.DealsEntry.COLUMN_VOLUME+ ") AS '" +
                        COLUMN_COST + "'"
        };

        // WHERE portfolio = '5838199' AND date < 123 AND type = 'Buy' AND ticker = 'SBER'
        String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                lPortfolio + "' AND " +
                Contract.DealsEntry.COLUMN_DATE + " < " + lDate + " AND " +
                Contract.DealsEntry.COLUMN_TYPE + " = '" + strings[1] + "'" + " AND " +
                Contract.DealsEntry.COLUMN_TICKER + " = '" + lTicker + "'";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                null);

        if (cursor.moveToFirst()) {

            int index = cursor.getColumnIndex(COLUMN_COST);
            costOfBuys = cursor.getLong(index);
        }

        cursor.close();

        return costOfBuys;
    }

    // получение суммы всех продаж конкретной акции
    private long getSellsOfTicker (String lPortfolio, String lTicker, long lDate) {

        long costOfSells = 0;
        final String COLUMN_COST = "cost";

        String [] strings = mContext.getResources().getStringArray(R.array.spinType_deal_array);

        Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                .appendPath(Contract.PATH_DEALS)
                .appendPath(Contract.PATH_SUM)
                .build();

        // SELECT portfolio, ticker, sum(price * volume) AS 'cost'
        String[] projection = {
                Contract.DealsEntry.COLUMN_PORTFOLIO,
                Contract.DealsEntry.COLUMN_TICKER,
                "sum (" + Contract.DealsEntry.COLUMN_PRICE + " * " +
                        Contract.DealsEntry.COLUMN_VOLUME+ ") AS '" +
                        COLUMN_COST + "'"
        };

        // WHERE portfolio = '5838199' AND date < 123 AND type = 'Sell' AND ticker = 'SBER'
        String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                lPortfolio + "' AND " +
                Contract.DealsEntry.COLUMN_DATE + " < " + lDate + " AND " +
                Contract.DealsEntry.COLUMN_TYPE + " = '" + strings[0] + "' AND " +
                Contract.DealsEntry.COLUMN_TICKER + " = '" + lTicker + "'";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                null);

        if (cursor.moveToFirst()) {

            int index = cursor.getColumnIndex(COLUMN_COST);
            costOfSells = cursor.getLong(index);
        }

        cursor.close();

        return costOfSells;
    }

    // получение суммы всех дивидендов конкретной акции
    private long getDividendsOfTicker (String lPortfolio, String lTicker, long lDate) {

        long costOfDivs = 0;
        final String COLUMN_COST = "cost";

        String [] strings = mContext.getResources().getStringArray(R.array.spinType_deal_array);

        Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                .appendPath(Contract.PATH_DEALS)
                .appendPath(Contract.PATH_SUM)
                .build();

        // SELECT portfolio, ticker, sum(price * volume) AS 'cost'
        String[] projection = {
                Contract.DealsEntry.COLUMN_PORTFOLIO,
                Contract.DealsEntry.COLUMN_TICKER,
                "sum (" + Contract.DealsEntry.COLUMN_PRICE + " * " +
                        Contract.DealsEntry.COLUMN_VOLUME+ ") AS '" +
                        COLUMN_COST + "'"
        };

        // WHERE portfolio = '5838199' AND date < 123 AND type = 'Dividend' AND ticker = 'SBER'
        String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                lPortfolio + "' AND " +
                Contract.DealsEntry.COLUMN_DATE + " < " + lDate + " AND " +
                Contract.DealsEntry.COLUMN_TYPE + " = '" + strings[2] + "' AND " +
                Contract.DealsEntry.COLUMN_TICKER + " = '" + lTicker + "'";

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                null);

        if (cursor.moveToFirst()) {

            int index = cursor.getColumnIndex(COLUMN_COST);
            costOfDivs = cursor.getLong(index);
        }

        cursor.close();

        return costOfDivs;
    }

    // класс для хранения и вычисления средневзвешенных инвестиций и
    // длительности существования портфеля конкретной акции
    private class AverageInvestmentOfTicker {

        // переменная для хранения общего количества дней
        int totalDays = 0;

        // переменная для хранения текущего результата
        long averageInvestment = 0;

        // получаю средневзвешенную сумму инвестиций за период и подсчитываю период
        private AverageInvestmentOfTicker(String lPortfolio, String lTicker, long lDate) {

            final String COLUMN_COST = "cost";

            // 1) делаю запрос к базе данных
            Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                    .appendPath(Contract.PATH_DEALS)
                    .build();

            // SELECT portfolio, ticker, type, (price * volume) as 'cost', date
            String[] projection = {
                    Contract.DealsEntry.COLUMN_PORTFOLIO,
                    Contract.DealsEntry.COLUMN_TICKER,
                    Contract.DealsEntry.COLUMN_TYPE,
                    Contract.DealsEntry.COLUMN_PRICE + " * " + Contract.DealsEntry.COLUMN_VOLUME +
                    " AS '" + COLUMN_COST + "'",
                    Contract.DealsEntry.COLUMN_DATE
            };

            // WHERE portfolio = '5838199' AND date < 123 AND ticker = 'SBER'
            String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                    lPortfolio + "' AND " +
                    Contract.DealsEntry.COLUMN_DATE + " < " + lDate + " AND " +
                    Contract.DealsEntry.COLUMN_TICKER + " = '" + lTicker + "' AND " +
                    Contract.DealsEntry.COLUMN_TYPE + " = 'Sell' OR " +
                    Contract.DealsEntry.COLUMN_TYPE + " = 'Buy'";

            String sortOrder = Contract.DealsEntry.COLUMN_DATE + " ASC";

            Cursor cursor = getContext().getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    null,
                    sortOrder);

            // 2) читаю курсор
            if (cursor.moveToFirst()) {

                long previousTime = 0; // время начала подпериода

                int dateIndex = cursor.getColumnIndex(Contract.DealsEntry.COLUMN_DATE);
                int typeIndex = cursor.getColumnIndex(Contract.DealsEntry.COLUMN_TYPE);
                int costIndex = cursor.getColumnIndex(COLUMN_COST);

                // переменная для хранения считанного значения количества Input
                long tempAmount;

                // тип операции (Sell, Buy)
                String[] strings = mContext.getResources().getStringArray(R.array.spinType_deal_array);

                // 3) читаю первую строку курсора
                // если тип = Buy то amount берем со знаком "+"
                if (cursor.getString(typeIndex).equals(strings[1])) {
                    tempAmount = cursor.getInt(costIndex);
                }
                // если тип = Sell то amount берем со знаком "-"
                else if (cursor.getString(typeIndex).equals(strings[0])) {
                    tempAmount = - cursor.getInt(costIndex);
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

                    // TODO: 18.08.17 сделать решение для случая нескольких подпериодов = 0
                    // если подпериод = 0, то сохраняем вносимое количество до следующего цикла
                    if (periodDays == 0) {

                        amountForZeroCase = tempAmount;

                    } else {

                        // считаем сумму для подпериода
                        sumOfPreviousInputs = sumOfPreviousInputs + tempAmount + amountForZeroCase;

                        // если денег выведено больше чем введено, то подпериод не учитывается
                        if (sumOfPreviousInputs <= 0) {
                            periodDays = 0;
                            sumOfPreviousInputs = 0;
                        }

                        // умножаем сумму подпериода на длительность подпериода и прибавляем
                        // к предыдущему результату инвестирования
                        averageInvestment = averageInvestment + (periodDays * sumOfPreviousInputs);

                        amountForZeroCase = 0;
                    }

                    if (cursor.getString(typeIndex).equals(strings[1])) {
                        tempAmount = cursor.getInt(costIndex);
                    }
                    else if (cursor.getString(typeIndex).equals(strings[0])) {

                        tempAmount = - cursor.getInt(costIndex);
                    } else {
                        tempAmount = 0;
                        Log.d(TAG, "Не верный тип ввода");
                    }

                    // общее количество дней расчитываемого периода
                    totalDays = totalDays + periodDays;

                    // задаю начало слудующего подпериода
                    previousTime = tempTime;
                }

                // 5) читаю последнюю строку курсора

                // последний подпериод
                long tempPeriod = lDate - previousTime;

                int periodDays = (int) (tempPeriod / MILLIS_IN_DAY);

                if (periodDays == 0) {

                    amountForZeroCase = tempAmount;

                } else {

                    sumOfPreviousInputs = sumOfPreviousInputs + tempAmount + amountForZeroCase;

                    // если денег выведено больше чем введено, то подпериод не учитывается
                    if (sumOfPreviousInputs <= 0) {
                        periodDays = 0;
                        sumOfPreviousInputs = 0;
                    }

                    averageInvestment = averageInvestment + (periodDays * sumOfPreviousInputs);

                    amountForZeroCase = 0;
                }

                totalDays = totalDays + periodDays;

                averageInvestment = averageInvestment / totalDays;
            }

            cursor.close();
        }

        long getAverageInvestment() {

            return averageInvestment;
        }

        int getTotalDays() {

            return totalDays;
        }
    }
}