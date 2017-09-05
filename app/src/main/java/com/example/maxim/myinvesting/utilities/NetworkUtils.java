package com.example.maxim.myinvesting.utilities;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import static com.example.maxim.myinvesting.data.Const.*;
import static com.example.maxim.myinvesting.data.Contract.*;

/**
 * Created by maxim on 14.08.17.
 */

public class NetworkUtils {

    public int getCurrentPrice (String ticker) {

        URL url = buildUrl(ticker, DateUtils.getYesterdayDate());

        String results = null;

        try {
            results = NetworkUtils.getResponseFromHttpUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        double price;

        if (results == null)
            price = 0;
        else
            price = getPrice(results);

        return ((int) price * MULTIPLIER_FOR_MONEY);
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {

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

    public static URL buildUrl(String ticker, String date) {
Log.d(TAG, date + " buildUrl() " + NetworkUtils.class.getSimpleName());
        Uri builtUri = Uri.parse(MOEX_BASE_URI + ticker + PARAM_JSON).buildUpon()
                .appendQueryParameter(PARAM_FROM, date)
                .appendQueryParameter(PARAM_LIMIT, "1")
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public double getPrice(String historyJsonString) {

        final String DATA = "data";

        double price = 0;

        try {

            JSONObject JsonObject = new JSONObject(historyJsonString);

            JSONObject historyJson = JsonObject.getJSONObject("history");

            JSONArray dataJson = historyJson.getJSONArray(DATA);

            JSONArray dataJsonDay = dataJson.getJSONArray(0);

            price = dataJsonDay.getDouble(11);

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return price;
    }
}
