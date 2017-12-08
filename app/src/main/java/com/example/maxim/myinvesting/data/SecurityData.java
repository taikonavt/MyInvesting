package com.example.maxim.myinvesting.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.maxim.myinvesting.utilities.MyApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import static com.example.maxim.myinvesting.data.Const.MULTIPLIER_FOR_MONEY;
import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 24.11.17.
 */

public class SecurityData {

    public String getTickerByIsin(String isin) {

        ContentValues cv;

        String result = null;

        // получаю данные о компании с ISIN из базы данных
        Cursor cursor = getInfoFromDb(Contract.SecuritiesEntry.COLUMN_ISIN, isin);

        // если данных нет, запрашиваю из интернета
        if (!cursor.moveToFirst()) {

            cv = askFromNet(isin);

            if (cv != null)
                result = cv.getAsString(Contract.SecuritiesEntry.COLUMN_TICKER);
        }
        // если данные есть, беру тикер из курсора
        else {

            int index = cursor.getColumnIndex(Contract.SecuritiesEntry.COLUMN_TICKER);

            result = cursor.getString(index);
        }

        cursor.close();

        return result;
    }

    public String getTickerByName(String name) {

        return null;
    }

    // передаю в метод название столбца и его значение, по которым нужно вернуть инфо о компании
    private Cursor getInfoFromDb(String key, String value) {

        Uri uri = Contract.SecuritiesEntry.CONTENT_URI;

        // SELECT ticker
        String [] projection = {
                Contract.SecuritiesEntry.COLUMN_TICKER,
                Contract.SecuritiesEntry.COLUMN_NAME,
                Contract.SecuritiesEntry.COLUMN_REGNUMBER,
                Contract.SecuritiesEntry.COLUMN_ISIN,
                Contract.SecuritiesEntry.COLUMN_GROUP
        };

        // WHERE ticker = 'SBER'
        String selection = key + " = '" + value + "'";

        Cursor cursor = MyApp.getAppContext().getContentResolver().query(uri, projection,
                selection, null, null);

        return cursor;
    }

    // запрашиваю инфо о бумаге с сайта ММВБ
    private ContentValues askFromNet(String request) {

        URL url = buildUrlForSecurityInfo(request);

        ContentValues cv = null;

        try {

            String results = getResponseFromHttpUrl(url);

            cv = getInfoFromJson(results);

            // добавляю отсутствующую информацию в БД
            MyApp.getAppContext().getContentResolver().insert(Contract.SecuritiesEntry.CONTENT_URI, cv);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return cv;
    }

    private String getResponseFromHttpUrl(URL url) throws IOException {

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

    // строю url для запроса на сайт
    private URL buildUrlForSecurityInfo (String request) {

        Uri.Builder builder = new Uri.Builder();

        builder.scheme("https")
                .authority("iss.moex.com")
                .appendPath("iss")
                .appendPath("securities.json")
                .appendQueryParameter("q", request);

        Uri uri = builder.build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    private ContentValues getInfoFromJson(String jsonString) {

        ContentValues cv = new ContentValues();

        try {

            JSONObject jsonObject = new JSONObject(jsonString);

            JSONObject securitiesJson = jsonObject.getJSONObject("securities");

            JSONArray dataJson = securitiesJson.getJSONArray("data");

            // выбираю 1й массив, т.к. TQBR стоит первым
            JSONArray infoJson = dataJson.getJSONArray(0);

            String ticker = infoJson.getString(1);

            String isin = infoJson.getString(5);

            String group = infoJson.getString(13);

            String registrationNumber = infoJson.getString(3);

            String name = infoJson.getString(4);

            cv.put(Contract.SecuritiesEntry.COLUMN_TICKER, ticker);
            cv.put(Contract.SecuritiesEntry.COLUMN_ISIN, isin);
            cv.put(Contract.SecuritiesEntry.COLUMN_GROUP, group);
            cv.put(Contract.SecuritiesEntry.COLUMN_REGNUMBER, registrationNumber);
            cv.put(Contract.SecuritiesEntry.COLUMN_NAME, name);

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return cv;
    }
}
