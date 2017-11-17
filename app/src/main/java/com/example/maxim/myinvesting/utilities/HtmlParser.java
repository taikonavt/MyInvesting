package com.example.maxim.myinvesting.utilities;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 17.11.17.
 */

public class HtmlParser extends AsyncTask <String, Void, Void> {

    @Override
    protected Void doInBackground(String... strings) {

        Log.d(TAG, "---------------------------------------------");

        String path = strings[0];

        File file = new File(path);

        try {
            Document doc = Jsoup.parse(file, "windows-1251", "com.example.maxim");

            Elements tables = doc.getElementsByTag("table");

            Element oneTable = tables.first();

            if (oneTable != null) {

                int i = 0;

                int size = tables.size();

                boolean flag = false;

                do {

                    oneTable = tables.get(i);

                    flag = checkIsItDealTable(oneTable);

                    i++;

                } while (!flag && (i < size));
            }

            setInfo(oneTable);

            Log.d(TAG, String.valueOf(checkIsItDealTable(oneTable)));

        } catch (IOException e) {

            Log.d(TAG, "IOException");
        }

        return null;
    }

    private boolean checkIsItDealTable(Element table) {

        Elements rows = table.getElementsByTag("tr");

        Element oneRow = rows.first();

        return (oneRow.text().equals("Исполненные сделки"));
    }

    private void setInfo(Element table) {

        Elements rows = table.getElementsByTag("tr");

        for (int i = 2; i < (rows.size() - 1); i++) {

            Element oneRow = rows.get(i);

            Elements cells = oneRow.getElementsByTag("td");

            String date = cells.get(2).text();
            String subDate = date.substring(0, 6);

            String code = cells.get(5).text();
            int firstSlash = code.indexOf("/", 0);
            int secondSlash = code.indexOf("/", firstSlash + 1);
            String isin = code.substring((firstSlash + 1), secondSlash);
            isin = isin.replace(" ", "");

            Log.d(TAG, "Date: " + subDate);
            Log.d(TAG, "Type: " + cells.get(4).text());
            Log.d(TAG, "Code: " + isin);
            Log.d(TAG, "Amount: " + cells.get(6).text());
            Log.d(TAG, "Price: " + cells.get(7).text());
        }
    }
}
