package com.example.maxim.myinvesting.data;

import android.net.Uri;
import android.os.StrictMode;
import android.provider.BaseColumns;

/**
 * Created by maxim on 26.03.17.
 */

public class Contract {

    public static final String CONTENT_AUTHORITY = "com.example.maxim.myinvesting";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_DEALS = "deals";

    public static final String PATH_INPUT = "input";

    public static final String PATH_PORTFOLIO = "portfolio";

    public static final String PATH_SUM = "type"; // используется при запросе к БД о получении всех вводов и сделок

    public static final String PATH_DATE_PRICE = "group_by_date_and_price";

    public static final class DealsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_DEALS)
                .build();

        public static final String TABLE_NAME = "deals";

        // Номер портфеля
        public static final String COLUMN_PORTFOLIO = "portfolio";

        // Тикер акции или номер облигации
        public static final String COLUMN_TICKER = "ticker";

        // Тип бумаги или операции:
        public static final String COLUMN_TYPE = "type";

        // Дата проведения операции в виде 1702
        public static final String COLUMN_DATE = "date";

        //Цена сделки
        public static final String COLUMN_PRICE = "price";

        // количество единиц в сделке
        public static final String COLUMN_VOLUME = "volume";

        // комиссия брокера
        public static final String COLUMN_FEE = "fee";
    }


    public static final class InputEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_INPUT)
                .build();

        public static final String TABLE_NAME = "input";

        // input, output
        public static final String COLUMN_TYPE = "type";

        public static final String COLUMN_DATE = "date";

        public static final String COLUMN_AMOUNT = "amount";

        public static final String COLUMN_CURRENCY = "currency";

        public static final String COLUMN_FEE = "fee";

        public static final String COLUMN_PORTFOLIO = "portfolio";

        public static final String COLUMN_NOTE = "note";
    }

    public static final class PortfolioEntry {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_PORTFOLIO)
                .build();
    }

}
