package com.example.maxim.myinvesting.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.maxim.myinvesting.MainActivity;
import com.example.maxim.myinvesting.R;
import com.example.maxim.myinvesting.utilities.PortfolioLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import static com.example.maxim.myinvesting.data.Const.MILLIS_IN_DAY;
import static com.example.maxim.myinvesting.data.Const.MULTIPLIER_FOR_MONEY;
import static com.example.maxim.myinvesting.data.Const.TAG;
import static java.lang.Math.pow;

/**
 * Created by maxim on 21.08.17.
 */

public class PortfolioData {

    private final int DAYS_IN_YEAR = 365;

    private String name = null;

    private Context mContext;

    private ArrayList<PortfolioItem> portfolioItems = null;

    private long costOfPortfolio = 0;

    private double mProfitability = 0;

    private long untilDateInMillis = 0;

    private int periodInDays = 0;

    private TextView tvProfitability = null;
    private TextView tvCostOfPortfolio = null;


// Получаю контекст для определения имени портфеля и даты до которой считается портфель
    public PortfolioData(Context context, long l) {

        mContext = context;

        name = ((MainActivity) mContext).getNameOfPortfolio();
        untilDateInMillis = l;
    }

    // устанавливаю список бумаг в портфеле
    public void setPortfolioItems(ArrayList<PortfolioItem> arrayList) {

        portfolioItems = arrayList;

        // последним элементом добавляю наличные рубли с объемом = 1
        // количество свободных денег устанавливается в TotalTask.setPortfolioCost()
        int volumeOfRub = 1;
        portfolioItems.add(new PortfolioItem(portfolioItems.size(), "RUB", volumeOfRub));

        // создаю массив для передачи в поток без элемента "RUB"
        String[] strings = new String[arrayList.size() - 1];

        for (int i = 0; i < arrayList.size() - 1; i++) {

            strings[i] = arrayList.get(i).getTicker();
        }

        // запускаю поток, который получает цены, полное имя акции и размер лота,
        // а затем отправляет их в PortfolioItem каждой акции
        PricesTask pricesTask = new PricesTask();
        pricesTask.execute(strings);
    }

    public String getName() {
        return name;
    }

    public ArrayList<PortfolioItem> getPortfolioItems() {
        return portfolioItems;
    }

    public void setProfitAndCostOfPortfolio(TextView tvProfitability, TextView tvCostOfPortfolio) {

        this.tvProfitability = tvProfitability;
        this.tvCostOfPortfolio = tvCostOfPortfolio;

        TotalTask totalTask = new TotalTask();
        totalTask.execute();
    }

    public long getUntilDateInMillis() {
        return untilDateInMillis;
    }


    class TotalTask extends AsyncTask<Void, Void, Void> {

        private final int FIFTEEN_SECOND = 75;
        int TIME_TO_SLEEP = 200;

        @Override
        protected Void doInBackground(Void... params) {

            setPortfolioCost();

            setPortfolioProfitabilityAndPeriod();

            return null;
        }

        // устанавливаю в portfolioData стоимость портфеля
        private void setPortfolioCost() {

            long costOfItems = 0;
            int j = 0;

            for (int i = 0; i < portfolioItems.size() - 1; i++) {

                while (!portfolioItems.get(i).priceIsReady) {

                    try {

                        Thread.sleep(TIME_TO_SLEEP);
                        j++;

                        if (j == FIFTEEN_SECOND) {

                            return;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                costOfItems = costOfItems + portfolioItems.get(i).getCost();
            }

            // вычисляю и устанавливаю в массив количество свободных денег
            int freeMoney = (int) (getInputs() - getBuys() +
                    getSells() + getDividends() - getOutputs() - getFees());

            int size = portfolioItems.size();

            // свободные деньги всегда последним пунктом в массиве
            portfolioItems.get(size-1).setPrice(freeMoney);

            costOfPortfolio = freeMoney + costOfItems;
        }

        // устанавливаю в portfolioData средневзвешенную доходность в разах (0,1 = 10%) годовых и
        // длительность существования портфеля
        private void setPortfolioProfitabilityAndPeriod() {

            // чистый доход за все время
            long netProfit = costOfPortfolio + getOutputs() - getInputs();

            // средневзвешенные инвестиции за все время
            AverageInvestment averageInvestment =
                    new AverageInvestment();

            // длительность существования портфеля
            periodInDays = averageInvestment.getTotalDays();

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

            mProfitability = profitability;
        }

        // получение суммы всех вводов
        private long getInputs() {

            long amountInput = 0;

            String [] strings = mContext.getResources().getStringArray(R.array.spinType_input_array);

            Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                    .appendPath(Contract.PATH_INPUT)
                    .appendPath(Contract.PATH_SUM)
                    .build();

            // SELECT portfolio, sum(amount) AS 'amount', sum(fee) AS 'fee'
            String[] projection = {
                    Contract.InputEntry.COLUMN_PORTFOLIO,
                            "sum (" + Contract.InputEntry.COLUMN_AMOUNT + ") AS '" +
                            Contract.InputEntry.COLUMN_AMOUNT +
                            "', sum(" + Contract.InputEntry.COLUMN_FEE + ") AS '" +
                            Contract.InputEntry.COLUMN_FEE + "'"
            };

            // WHERE portfolio = '5838199' AND date < 123 AND type = 'Input'
            String selection = Contract.InputEntry.COLUMN_PORTFOLIO + " = '" +
                    name + "' AND " +
                    Contract.InputEntry.COLUMN_DATE + " < " + untilDateInMillis + " AND " +
                    Contract.InputEntry.COLUMN_TYPE + " = '" + strings[0] + "'";

            Cursor cursor = mContext.getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    null,
                    null);

            int indexAmount = cursor.getColumnIndex(Contract.InputEntry.COLUMN_AMOUNT);
            int indexFee = cursor.getColumnIndex(Contract.InputEntry.COLUMN_FEE);

            if (cursor.moveToFirst()) {

                amountInput = cursor.getLong(indexAmount) - cursor.getLong(indexFee);
            }

            cursor.close();

            return amountInput;
        }

        // получение суммы всех выводов
        private long getOutputs() {

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
                            Contract.InputEntry.COLUMN_AMOUNT +
                            "', sum(" + Contract.InputEntry.COLUMN_FEE + ") AS '" +
                            Contract.InputEntry.COLUMN_FEE + "'"
            };

            // WHERE portfolio = '5838199' AND date < 123 AND type = 'Output'
            String selection = Contract.InputEntry.COLUMN_PORTFOLIO + " = '" +
                    name + "' AND " +
                    Contract.InputEntry.COLUMN_DATE + " < " + untilDateInMillis + " AND " +
                    Contract.InputEntry.COLUMN_TYPE + " = '" + strings[1] + "'";

            Cursor cursor = mContext.getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    null,
                    null);

            int indexAmount = cursor.getColumnIndex(Contract.InputEntry.COLUMN_AMOUNT);
            int indexFee = cursor.getColumnIndex(Contract.InputEntry.COLUMN_FEE);

            if (cursor.moveToFirst()) {

                amountOutput = cursor.getLong(indexAmount) + cursor.getLong(indexFee);
            }

            cursor.close();

            return amountOutput;
        }

        // получение суммы всех покупок акций
        private long getBuys() {

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
                    name + "' AND " +
                    Contract.DealsEntry.COLUMN_DATE + " < " + untilDateInMillis + " AND " +
                    Contract.DealsEntry.COLUMN_TYPE + " = '" + strings[1] + "'";

            Cursor cursor = mContext.getContentResolver().query(
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
        private long getSells () {

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
                    name + "' AND " +
                    Contract.DealsEntry.COLUMN_DATE + " < " + untilDateInMillis + " AND " +
                    Contract.DealsEntry.COLUMN_TYPE + " = '" + strings[0] + "'";

            Cursor cursor = mContext.getContentResolver().query(
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
        private long getDividends () {

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
                    name + "' AND " +
                    Contract.DealsEntry.COLUMN_DATE + " < " + untilDateInMillis + " AND " +
                    Contract.DealsEntry.COLUMN_TYPE + " = '" + strings[2] + "'";

            Cursor cursor = mContext.getContentResolver().query(
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

        private long getFees() {

            long costOfFees = 0;

            // GROUP BY "portfolio"
            Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                    .appendPath(Contract.PATH_DEALS)
                    .appendPath(Contract.PATH_FEES)
                    .build();

            // SELECT sum (fee) AS 'fee'
            String[] projection = {
                    "sum(" + Contract.DealsEntry.COLUMN_FEE + ") AS '" +
                            Contract.DealsEntry.COLUMN_FEE + "'"
            };

            // WHERE portfolio = '5838199' AND date < 123
            String selection = Contract.DealsEntry.COLUMN_PORTFOLIO + " = '" +
                    name + "' AND " +
                    Contract.DealsEntry.COLUMN_DATE + " < " + untilDateInMillis;

            Cursor cursor = mContext.getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    null,
                    null);

            if (cursor.moveToFirst()) {

                int index = cursor.getColumnIndex(Contract.DealsEntry.COLUMN_FEE);
                costOfFees = cursor.getLong(index);
            }

            cursor.close();

            return costOfFees;
        }


        // класс для хранения и вычисления средневзвешенных инвестиций и
        // длительности существования портфеля
        private class AverageInvestment {

            // переменная для хранения общего количества дней
            int totalDays = 0;

            // переменная для хранения текущего результата
            long averageInvestment = 0;

            // получаю средневзвешенную сумму инвестиций за период и подсчитываю период
            private AverageInvestment() {

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
                        name + "' AND " +
                        Contract.InputEntry.COLUMN_DATE + " < " + untilDateInMillis;

                String sortOrder = Contract.InputEntry.COLUMN_DATE + " ASC";

                Cursor cursor = mContext.getContentResolver().query(
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
                    long tempPeriod = untilDateInMillis - previousTime;

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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            tvCostOfPortfolio.setText(String.valueOf(costOfPortfolio / MULTIPLIER_FOR_MONEY));

            DecimalFormat df = new DecimalFormat("#.##");
            String str = df.format(mProfitability * 100)  + "%";
            tvProfitability.setText(str);
        }
    }


    private class PricesTask extends AsyncTask<String[], Void, Void> {

        @Override
        protected Void doInBackground(String[] ... array) {

            String[] strings = array[0];

            URL url = buildUrlForCurrentPrice(strings);

            String results = null;

            try {

                results = getResponseFromHttpUrl(url);

                getPriceFromJson(results);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        // TODO: 17.10.17 вылетает если нет подключения. Исправить
        String getResponseFromHttpUrl(URL url) throws IOException {

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try {

                InputStream in = urlConnection.getInputStream();

                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");

                boolean hasInput = scanner.hasNext();
                if (hasInput) {

                    String string = scanner.next();

                    return string;
                } else {

                    return null;
                }
            } finally {
                urlConnection.disconnect();
            }
        }

        URL buildUrlForCurrentPrice(String[] param) {

            Uri.Builder builder = new Uri.Builder();

            builder.scheme("https")
                    .authority("iss.moex.com")
                    .appendPath("iss")
                    .appendPath("engines")
                    .appendPath("stock")
                    .appendPath("markets")
                    .appendPath("shares")
                    .appendPath("boards")
                    .appendPath("tqbr")
                    .appendPath("securities.json");

            String str = "";
            for (int i = 0; i < param.length; i++)
                str = str + "," + param[i];

            builder.appendQueryParameter("securities", str);

            Uri uri = builder.build();

            URL url = null;
            try {
                url = new URL(uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return url;
        }

        void getPriceFromJson(String jsonString) {

            try {

                JSONObject jsonObject = new JSONObject(jsonString);

                JSONObject marketDataJson = jsonObject.getJSONObject("marketdata");

                JSONArray dynamicDataJson = marketDataJson.getJSONArray("data");

                JSONObject securitiesJson = jsonObject.getJSONObject("securities");

                JSONArray staticDataJson = securitiesJson.getJSONArray("data");

                for (int i = 0; i < dynamicDataJson.length(); i++) {

                    JSONArray tickerDynamicDataJson = dynamicDataJson.getJSONArray(i);

                    String ticker = tickerDynamicDataJson.getString(0);

                    int price = (int) (tickerDynamicDataJson.getDouble(12) * MULTIPLIER_FOR_MONEY);

                    JSONArray tickerStaticDataJson = staticDataJson.getJSONArray(i);

                    int lotSize = tickerStaticDataJson.getInt(4);

                    String nameOfSec = tickerStaticDataJson.getString(9);

                    setItemsInfo(ticker, price, lotSize, nameOfSec);
                }

            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }

        private void setItemsInfo(String ticker, int price, int lotSize, String nameOfSec) {

            for (PortfolioItem portfolioItem : portfolioItems) {

                if (portfolioItem.getTicker().equals(ticker)) {

                    portfolioItem.setName(nameOfSec);

                    portfolioItem.setLotSize(lotSize);

                    portfolioItem.setPrice(price);
                }
            }
        }
    }
}
