package com.example.maxim.myinvesting.utilities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.maxim.myinvesting.AddDealActivity;
import com.example.maxim.myinvesting.AddInputActivity;
import com.example.maxim.myinvesting.MainActivity;
import com.example.maxim.myinvesting.R;
import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.data.PortfolioNames;
import com.example.maxim.myinvesting.data.SecurityData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Calendar;

import static com.example.maxim.myinvesting.data.Const.MULTIPLIER_FOR_MONEY;
import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 17.11.17.
 */

public class HtmlParser extends AsyncTask <String, Void, HtmlParser.BooleanWithMsg> {

    public static final String REFRESH = "refresh";

    // тип для обозначения налогов на дивидены
    private static final String TAX = "tax";

    private long lastDealInsertionId;

    private String callingClass;

    public HtmlParser(String callingClass) {

        this.callingClass = callingClass;
    }

    @Override
    protected BooleanWithMsg doInBackground(String... strings) {

        String path = strings[0];

        // флаг "операция разбора и вставки в базу данных прошла успешно"
        BooleanWithMsg operationOK = new BooleanWithMsg(false);

        try {
            if (callingClass.equals(AddDealActivity.class.getSimpleName())) {

                operationOK = openDealTable(path);
            }
            else if (callingClass.equals(AddInputActivity.class.getSimpleName())) {

                operationOK = openInputTable(path);
            }
        } catch (IOException e) {

            Log.e(TAG, "Html parsing - IOException");
            e.printStackTrace();

            operationOK.setValue(false);
        }

        catch (NullPointerException e) {

            Log.e(TAG, "Html parsing - NullPointerException");
            e.printStackTrace();

            operationOK.setValue(false);
        }

        catch (UnsupportedCharsetException e) {

            Log.e(TAG, HtmlParser.class.getSimpleName() +
                    " setInfo() " + e.getCharsetName());
            e.printStackTrace();

            operationOK.setValue(false);
        }

        return operationOK;
    }

    private BooleanWithMsg openDealTable(String path)
            throws IOException, NullPointerException, UnsupportedCharsetException
    {

        File file = new File(path);

        Document doc = Jsoup.parse(file, null, "com.example.maxim");

        // получаю все элементы с тагом "table"
        Elements tables = doc.getElementsByTag("table");

        // получаю первую таблицу, если таблиц нет то получаю null
        Element oneTable = tables.first();

        // в tables есть таблица со сделками
        BooleanWithMsg thereIsDealTable = new BooleanWithMsg(false);

        if (oneTable != null) {

            int i = 0;

            int size = tables.size();

            // проверяю есть ли в tables таблица со сделками.
            // если есть, то ставлю флаг и сохраняю ее в oneTable
            do {
                oneTable = tables.get(i);

                thereIsDealTable = checkIsItDealTable(oneTable);

                i++;

            } while (!thereIsDealTable.getValue() && (i < size));
        }

        // операция разбора файла прошла без ошибок
        if (thereIsDealTable.getValue()) {

            // вношу инофрмацию из таблицы со сделками в БД
            parseDealTable(oneTable);

            return thereIsDealTable;
        }
        else
            thereIsDealTable.setMessage(MyApp.getAppContext().getString(R.string.htmlParsingTableNotExists));

        // возвращаю false с сообщением
        return thereIsDealTable;
    }

    private void parseDealTable (Element table) throws NullPointerException, UnsupportedCharsetException {

        // получаю все строки из таблицы
        Elements rows = table.getElementsByTag("tr");

        for (int i = 2; i < (rows.size() - 1); i++) {

            // получаю одну строку
            Element oneRow = rows.get(i);

            parseRowOfDealTable(oneRow);
        }
    }

    private void parseRowOfDealTable(Element row) throws UnsupportedCharsetException {

        // получаю все ячейки из строки
        Elements cells = row.getElementsByTag("td");

        // получаю код ISIN
        String code = cells.get(5).text();
        int firstSlash = code.indexOf("/", 0);
        int secondSlash = code.indexOf("/", firstSlash + 1);
        String isin = code.substring((firstSlash + 1), secondSlash);

        // если в полученном коде есть пробелы убираю их
        isin = isin.replace(" ", "");

        // получаю номер портфеля
        String portfolio = cells.get(20).text();

        String [] portfolios = PortfolioNames.readPortfoliosNames(MyApp.getAppContext());

        boolean isExists = false;

        for (String str :
                portfolios) {
            if (str.equals(portfolio))
                isExists = true;
        }

        if (!isExists)
            PortfolioNames.savePortfolioName(MyApp.getAppContext(), portfolio);

        SecurityData securityData = new SecurityData();

        // получаю тикер по ISIN
        String ticker = securityData.getTickerByIsin(isin);

        String type;

        // получаю тип операции
        String typeHtml = cells.get(4).text();
        String[] typeApp = MyApp.getAppContext().getResources().
                getStringArray(R.array.spinType_deal_array);

        if (typeHtml.equals("Покупка"))
            type = typeApp[1];
        else if (typeHtml.equals("Продажа"))
            type = typeApp[0];
        else
            throw new UnsupportedCharsetException(typeHtml);

        // получаю дату в виде yymmdd
        String date = cells.get(2).text();
        String subDate = date.substring(0, 6);

        int year = Integer.parseInt(subDate.substring(0, 2));

        // проверяем введенную дату(двухзначную): если больше текущей значит 1900,
        // если нет - 2000
        if ((year + 2000) > Calendar.getInstance().get(Calendar.YEAR)) {
            year = year + 1900;
        } else year = year + 2000;

        int month = Integer.parseInt(subDate.substring(2, 4));

        month = month - 1; // перевожу в значения константы Calendar.MONTH

        int day = Integer.parseInt(subDate.substring(4, 6));

        //получаю цену
        int price = (int) (Float.parseFloat(cells.get(7).text()) * MULTIPLIER_FOR_MONEY);

        // получаю объем
        int volume = Math.abs(Integer.parseInt(cells.get(6).text()));

        // получаю накопленный купонный доход для облигации
        String couponStr = cells.get(13).text();

        int couponInt;

        // если ячейка не пустая
        if (couponStr.length() > 0)
            couponInt = (int) Math.abs(Float.parseFloat(couponStr) * MULTIPLIER_FOR_MONEY);
        else
            couponInt = 0;

        // делю на объем, т.к. указан купон для всех облигаций
        int couponForEachBond = couponInt / volume;

        price = price + couponForEachBond;

        // получаю комиссию
        String firstFeeStr = cells.get(16).text();
        String secondFeeStr = cells.get(17).text();

        float firstFeeFlt;
        if (firstFeeStr.length() > 0)
            firstFeeFlt = Math.abs(Float.parseFloat(firstFeeStr));
        else
            firstFeeFlt = 0;

        float secondFeeFlt;
        if (secondFeeStr.length() > 0)
            secondFeeFlt = Math.abs(Float.parseFloat(secondFeeStr));
        else
            secondFeeFlt = 0;

        int fee = (int) ((firstFeeFlt +
                secondFeeFlt) * MULTIPLIER_FOR_MONEY);

        setDealInfo(portfolio, ticker, type, year, month, day, price, volume, fee);
    }

    private long setDealInfo(String portfolio, String ticker, String type, int year,
                             int month, int day, long price, int volume, int fee)
            throws UnsupportedOperationException {

        ContentValues contentValues = new ContentValues();

        contentValues.put(Contract.DealsEntry.COLUMN_PORTFOLIO, portfolio);
        contentValues.put(Contract.DealsEntry.COLUMN_TICKER, ticker);
        contentValues.put(Contract.DealsEntry.COLUMN_TYPE, type);
        contentValues.put(Contract.DealsEntry.COLUMN_DATE,
                DateUtils.getTimeForMoscowInMillis(year, month, day));

        contentValues.put(Contract.DealsEntry.COLUMN_PRICE, price);
        contentValues.put(Contract.DealsEntry.COLUMN_VOLUME, volume);
        contentValues.put(Contract.DealsEntry.COLUMN_FEE, fee);

        Uri uri = MyApp.getAppContext().getContentResolver().
                insert(Contract.DealsEntry.CONTENT_URI, contentValues);

        // сообщение о вставке транзакции в базу данных
        String string = uri.getPathSegments().get(1);

        long id = Long.parseLong(string);

        if (id < 0)
            // использую UnsupportedCharsetException т.к. выше есть такое же исключение
            // и обработать это можно в том же кэтче
            throw new UnsupportedCharsetException("Database insertion error");

        return id;
    }

    private BooleanWithMsg openInputTable(String path) throws IOException, NullPointerException,
            UnsupportedCharsetException {

        File file = new File(path);

        Document doc = Jsoup.parse(file, "windows-1251", "com.example.maxim");

        // получаю все элементы с тагом "table"
        Elements tables = doc.getElementsByTag("table");

        // получаю первую таблицу, если таблиц нет то получаю null
        Element oneTable = tables.first();

        // в tables есть таблица со сделками
        BooleanWithMsg thereIsInputTable = new BooleanWithMsg(false);

        if (oneTable != null) {

            int i = 0;

            int size = tables.size();

            // проверяю есть ли в tables таблица со сделками.
            // если есть, то ставлю флаг и сохраняю ее в oneTable
            do {
                oneTable = tables.get(i);

                thereIsInputTable = checkIsItInputTable(oneTable);

                i++;

            } while (!thereIsInputTable.getValue() && (i < size));
        }

        // операция разбора файла прошла без ошибок
        if (thereIsInputTable.getValue()) {

            // вношу инофрмацию из таблицы со сделками в БД
            parseInputTable(oneTable);

            return thereIsInputTable;
        }
        else {

            thereIsInputTable.setMessage(MyApp.getAppContext().getString(R.string.htmlParsingTableNotExists));

            // возвращаю false с сообщением
            return thereIsInputTable;
        }
    }

    private void parseInputTable (Element table) throws NullPointerException, UnsupportedCharsetException {

        // получаю все строки из таблицы
        Elements rows = table.getElementsByTag("tr");

        for (int i = 3; i < (rows.size()); i++) {

            // получаю одну строку
            Element oneRow = rows.get(i);

            parseRowOfInputTable(oneRow);
        }
    }

    private void parseRowOfInputTable(Element row) throws UnsupportedCharsetException {

        // получаю все ячейки из строки
        Elements cells = row.getElementsByTag("td");

        // получаю дату в виде 03.10.17
        String date = cells.get(1).text();

        int year = Integer.parseInt(date.substring(6));

        // проверяем введенную дату(двухзначную): если больше текущей значит 1900,
        // если нет - 2000
        if ((year + 2000) > Calendar.getInstance().get(Calendar.YEAR)) {
            year = year + 1900;
        } else year = year + 2000;

        int month = Integer.parseInt(date.substring(3, 5));

        month = month - 1; // перевожу в значения константы Calendar.MONTH

        int day = Integer.parseInt(date.substring(0, 2));

        String type;

        // получаю тип операции
        String typeHtml = cells.get(3).text();

        String[] typeInput = MyApp.getAppContext().getResources().
                getStringArray(R.array.spinType_input_array);

        String[] typeDeal = MyApp.getAppContext().getResources().
                getStringArray(R.array.spinType_deal_array);

        String tempStrAmount = cells.get(6).text();

        if (tempStrAmount.isEmpty()) {

            tempStrAmount = cells.get(5).text();
        }

        // беру значение со знаком, чтобы верно определить тип операции
        // по модулю беру ниже
        long amount = (long) (Float.parseFloat(tempStrAmount) * MULTIPLIER_FOR_MONEY);

        String currency = "RUB";
        int fee = 0;
        String note = null;

        if (typeHtml.equals("Поступило на счет.") ||
                (typeHtml.startsWith("перевод денег") && (amount > 0))) {// перевод денег со знаком +

            // Input
            type = typeInput[0];

Log.d(TAG, HtmlParser.class.getSimpleName() + " parseRowOfInputTable() " + typeHtml + " " + type);
        }

        // todo в дальнейшем исправить "Списано со счета."
        else if (typeHtml.equals("Списано со счета.") ||
                (typeHtml.startsWith("перевод денег") && (amount < 0))) {// перевод денег со знаком -

            // Output
            type = typeInput[1];

Log.d(TAG, HtmlParser.class.getSimpleName() + " parseRowOfInputTable() " + typeHtml + " " + type);
        }

        else if (typeHtml.startsWith("Дивиденды")) {

            type = typeDeal[2];

Log.d(TAG, HtmlParser.class.getSimpleName() + " parseRowOfInputTable() " + typeHtml + " " + type);
        }

        else if (typeHtml.startsWith("Налог")) {

            type = TAX;

Log.d(TAG, HtmlParser.class.getSimpleName() + " parseRowOfInputTable() " + typeHtml + " " + type);
        }

        else if (typeHtml.startsWith("Проценты по сделке") ||
                typeHtml.equals("Выдача денежных средств") ||
                typeHtml.equals("Возврат денежных средств")) {

            type = "unusable";

Log.d(TAG, HtmlParser.class.getSimpleName() + " parseRowOfInputTable() " + typeHtml + " " + type);
        }

        else {

Log.d(TAG, HtmlParser.class.getSimpleName() + " parseRowOfInputTable() " + typeHtml);

            throw new UnsupportedCharsetException(typeHtml);
        }

        amount = Math.abs(amount);

        // получаю номер портфеля
        String portfolio = cells.get(7).text();

        String [] portfolios = PortfolioNames.readPortfoliosNames(MyApp.getAppContext());

        boolean isExists = false;

        for (String str :
                portfolios) {
            if (str.equals(portfolio))
                isExists = true;
        }

        // если нет в списке, то добавляю
        if (!isExists)
            PortfolioNames.savePortfolioName(MyApp.getAppContext(), portfolio);


        // Input
        if (type.equals(typeInput[0])) {

            setInputInfo(type, year, month, day, amount, currency, fee, portfolio, note);
        }

        // Output
        if (type.equals(typeInput[1])) {

            setInputInfo(type, year, month, day, amount, currency, fee, portfolio, note);
        }

        String ticker = null;

        // Dividend
        if (type.equals(typeDeal[2])) {

            // получаю registration number
            String code = cells.get(3).text();

            String regNum = findRegNum(code);

Log.d(TAG, HtmlParser.class.getSimpleName() + " parseRowOfInputTable() regNum " + regNum);

            SecurityData securityData = new SecurityData();

            // получаю тикер по ISIN
            ticker = securityData.getTickerByIsin(regNum);

Log.d(TAG, HtmlParser.class.getSimpleName() + " parseRowOfInputTable() ticker " + ticker + " " + amount);

            lastDealInsertionId = setDealInfo(portfolio, ticker, type, year, month, day, amount, 1, fee);
        }

        // налог на дивиденды
        if (type.equals(TAX)) {

            Uri uri = Contract.BASE_CONTENT_URI.buildUpon()
                    .appendPath(Contract.PATH_DEALS)
                    .appendPath(String.valueOf(lastDealInsertionId))
                    .build();

            MyApp.getAppContext().getContentResolver().update(uri, null, String.valueOf(amount), null);

Log.d(TAG, HtmlParser.class.getSimpleName() + " parseRowOfInputTable() TAX " + amount);
        }
    }

    private void setInputInfo(String type, int year, int month, int day, long amount,
                              String currency, int fee, String portfolio, String note)
            throws UnsupportedOperationException {

        ContentValues contentValues = new ContentValues();

        contentValues.put(Contract.InputEntry.COLUMN_TYPE, type);
        contentValues.put(Contract.InputEntry.COLUMN_DATE,
                DateUtils.getTimeForMoscowInMillis(year, month, day));
        contentValues.put(Contract.InputEntry.COLUMN_AMOUNT, amount);
        contentValues.put(Contract.InputEntry.COLUMN_CURRENCY, currency);
        contentValues.put(Contract.InputEntry.COLUMN_FEE, fee);
        contentValues.put(Contract.InputEntry.COLUMN_PORTFOLIO, portfolio);
        contentValues.put(Contract.InputEntry.COLUMN_NOTE, note);

        Uri uri = MyApp.getAppContext().getContentResolver().
                insert(Contract.InputEntry.CONTENT_URI, contentValues);

        // сообщение о вставке транзакции в базу данных
        String string = uri.getPathSegments().get(1);

        if (Long.parseLong(string) < 0)
            throw new UnsupportedCharsetException("Database insertion error");
    }

    private BooleanWithMsg checkIsItDealTable(Element table) throws NullPointerException {

        Elements rows = table.getElementsByTag("tr");

        Element oneRow = rows.first();

        return new BooleanWithMsg(oneRow.text().equals("Исполненные сделки"));
    }

    private BooleanWithMsg checkIsItInputTable(Element table) throws NullPointerException {

        Elements rows = table.getElementsByTag("tr");

        Element oneRow = rows.first();

        return new BooleanWithMsg(oneRow.text().startsWith("Операции с денежными средствами"));
    }

    private String findRegNum(String string) {

        if (!string.isEmpty()) {

            // разбиваю фразу на слова
            String[] strings = string.split(" ");

            for (String word :
                    strings) {

                int i = 0;
                boolean flag = false;

                // т.к. regNums все больше 7 букв
                if (word.length() >= 7) {
                    do {

                        // в слове используются только латинские буквы, цифры или знаки
                        // (без русских букв)
                        if (word.charAt(i) <= 'z') {

                            flag = true;
                        }
                        else
                            flag = false;

                        i++;
                    }
                    while (flag && (i < 7));
                }

                if (flag) {

                    return word;
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(BooleanWithMsg operationOK) {
        super.onPostExecute(operationOK);

        if (operationOK.getValue()) {

            Intent intent = new Intent(MainActivity.BROADCAST_ACTION);

            intent.putExtra(MainActivity.REFRESH_KEY, REFRESH);

            MyApp.getAppContext().sendBroadcast(intent);

            Toast.makeText(MyApp.getAppContext(), MyApp.getAppContext().
                    getString(R.string.htmlParsingIsOK), Toast.LENGTH_LONG).show();
        }
        else {

            String message;

            if (operationOK.getMessage().isEmpty())
                message = MyApp.getAppContext().getString(R.string.htmlParsingIsWrong);
            else
                message = operationOK.getMessage();

            Toast.makeText(MyApp.getAppContext(), message, Toast.LENGTH_LONG).show();
        }
    }


    class BooleanWithMsg {

        private boolean value;

        private String message;

        BooleanWithMsg(boolean value) {

            this.value = value;
        }

        String getMessage() {

            if (message == null)
                return "";

            else
                return message;
        }

        void setMessage(String msg) {
            message = msg;
        }

        boolean getValue() {
            return value;
        }

        void setValue(boolean value) {
            this.value = value;
        }
    }
}
