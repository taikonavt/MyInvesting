package com.example.maxim.myinvesting.utilities;

import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.maxim.myinvesting.R;
import com.example.maxim.myinvesting.data.Contract;

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

public class HtmlParser extends AsyncTask <String, Void, Boolean> {

    @Override
    protected Boolean doInBackground(String... strings) {

        Log.d(TAG, "---------------------------------------------");

        String path = strings[0];

        File file = new File(path);

        Boolean operationOK = false;

        try {
            Document doc = Jsoup.parse(file, "windows-1251", "com.example.maxim");

            Elements tables = doc.getElementsByTag("table");

            Element oneTable = tables.first();

            boolean thereIsDealTable = false;

            if (oneTable != null) {

                int i = 0;

                int size = tables.size();

                do {

                    oneTable = tables.get(i);

                    thereIsDealTable = checkIsItDealTable(oneTable);

                    i++;

                } while (!thereIsDealTable && (i < size));
            }

            if (thereIsDealTable) {

                setInfo(oneTable);
            }

            Log.d(TAG, String.valueOf(checkIsItDealTable(oneTable)));

            operationOK = true;

        } catch (IOException e) {

            Log.e(TAG, "Html parsing - IOException");

            operationOK = false;
        }

        catch (NullPointerException e) {

            Log.e(TAG, "Html parsing - NullPointerException");

            operationOK = false;
        }

        return operationOK;
    }

    private boolean checkIsItDealTable(Element table) throws NullPointerException {

        Elements rows = table.getElementsByTag("tr");

        Element oneRow = rows.first();

        return (oneRow.text().equals("Исполненные сделки"));
    }

    private void setInfo(Element table) throws NullPointerException {

        Elements rows = table.getElementsByTag("tr");

        for (int i = 2; i < (rows.size() - 1); i++) {

            try {

                Element oneRow = rows.get(i);

                Elements cells = oneRow.getElementsByTag("td");

                String date = cells.get(2).text();
                String subDate = date.substring(0, 6);

                String code = cells.get(5).text();
                int firstSlash = code.indexOf("/", 0);
                int secondSlash = code.indexOf("/", firstSlash + 1);
                String isin = code.substring((firstSlash + 1), secondSlash);
                isin = isin.replace(" ", "");

                String portfolio = cells.get(20).text();

                // TODO: 21.11.17 Исправить!!!
                String ticker = "SBER";

                String type;

                String typeHtml = cells.get(4).text();
                String[] typeApp = MyApp.getAppContext().getResources().
                        getStringArray(R.array.spinType_deal_array);

                if (typeHtml.equals("Покупка"))
                    type = typeApp[1];
                else if (typeHtml.equals("Продажа"))
                    type = typeApp[0];
                else
                    throw new UnsupportedCharsetException(typeHtml);

                int year = Integer.parseInt(subDate.substring(0, 2));

                // проверяем введенную дату(двухзначную): если больше текущей значит 1900,
                // если нет - 2000
                if ((year + 2000) > Calendar.getInstance().get(Calendar.YEAR)) {
                    year = year + 1900;
                } else year = year + 2000;

                int month = Integer.parseInt(subDate.substring(2, 4));

                month = month - 1; // перевожу в значения константы Calendar.MONTH

                int day = Integer.parseInt(subDate.substring(4, 6));

                long dateInmillis = DateUtils.getTimeForMoscowInMillis(year, month, day);

                int price = (int) (Float.parseFloat(cells.get(7).text()) * MULTIPLIER_FOR_MONEY);

                int volume = Math.abs(Integer.parseInt(cells.get(6).text()));

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

                Log.d(TAG, "Portfolio: " + portfolio);
                Log.d(TAG, "DateInMillis: " + dateInmillis);
                Log.d(TAG, "Type: " + type);
                Log.d(TAG, "ISNI: " + isin);
                Log.d(TAG, "Volume: " + volume);
                Log.d(TAG, "Price: " + price);
                Log.d(TAG, "fee: " + fee);


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
            }
            catch (UnsupportedCharsetException e) {

                Log.e(TAG, HtmlParser.class.getSimpleName() +
                        " setInfo() " + e.getCharsetName());
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean operationOK) {
        super.onPostExecute(operationOK);

        if (operationOK) {

            Toast.makeText(MyApp.getAppContext(), MyApp.getAppContext().
                    getString(R.string.htmlParsingIsOK), Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(MyApp.getAppContext(), MyApp.getAppContext().
                    getString(R.string.htmlParsingIsWrong), Toast.LENGTH_LONG).show();

    }
}
