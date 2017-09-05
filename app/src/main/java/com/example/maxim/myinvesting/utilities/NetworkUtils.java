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

/**
 * Created by maxim on 14.08.17.
 */

public class NetworkUtils {

    private static final String MOEX_BASE_URI =
            "https://iss.moex.com/iss/engines/stock/markets/shares/boards/tqbr/securities/";

    private static final String PARAM_JSON = ".json";

    private static String PARAM_FROM = "from";

    private static String PARAM_LIMIT = "limit";

    int getCurrentPrice (String ticker) {

        URL url = buildUrlForCurrentPrice(ticker);

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

    public static URL buildUrlForCurrentPrice(String ticker) {

        Uri builtUri = Uri.parse(MOEX_BASE_URI + ticker + PARAM_JSON);

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public double getPrice(String historyJsonString) {

        double price = 0;

        try {

            JSONObject JsonObject = new JSONObject(historyJsonString);

            JSONObject marketdataJson = JsonObject.getJSONObject("marketdata");

            JSONArray dataJson = marketdataJson.getJSONArray("data");

            JSONArray dataJsonDay = dataJson.getJSONArray(0);

            price = dataJsonDay.getDouble(12);

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return price;
    }
}
