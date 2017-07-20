package com.example.maxim.myinvesting;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.utilities.DateUtils;
import static com.example.maxim.myinvesting.data.Const.TAG;
import static com.example.maxim.myinvesting.data.Const.MULTIPLIER_FOR_MONEY;

import java.util.Calendar;

/**
 * Created by maxim on 27.03.17.
 */

public class AddDealActivity extends AppCompatActivity {
    // TODO: 23.05.17 NEXT: add page for input transaction
    EditText eTPortfolio;
    EditText eTTicker;
    Spinner spinnerType;
    EditText eTYear;
    EditText eTMonth;
    EditText eTDay;
    EditText eTPrice;
    EditText eTVolume;
    EditText eTFee;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_deal);

        eTPortfolio = (EditText) findViewById(R.id.eTPortfolio);
        eTTicker = (EditText) findViewById(R.id.eTTicker);
        spinnerType = (Spinner) findViewById(R.id.spinnerType);
        eTYear = (EditText) findViewById(R.id.eTYear);
        eTMonth = (EditText) findViewById(R.id.eTMonth);
        eTDay = (EditText) findViewById(R.id.eTDay);
        eTPrice = (EditText) findViewById(R.id.eTPrice);
        eTVolume = (EditText) findViewById(R.id.eTVolume);
        eTFee = (EditText) findViewById(R.id.eTFee);
    }

    String portfolio;
    String ticker;
    String type;
    int year;
    int month;
    int day;
    int price;
    int volume;
    int fee;

    public void onClick(View view) {

        try {

            portfolio = eTPortfolio.getText().toString();
            if (portfolio.length() == 0)
                throw new UnsupportedOperationException("Portfolio не задан");

            ticker = eTTicker.getText().toString();
            if (ticker.length() == 0)
                throw new UnsupportedOperationException("Ticker не задан");

            type = spinnerType.getSelectedItem().toString();

            year = Integer.valueOf(eTYear.getText().toString());

            // ввел это условие т.к. все мои сделки были сделаны после 2014 года
            if (year + 2000 > Calendar.getInstance().get(Calendar.YEAR)
                    || year + 2000 < 2010) {
                throw new UnsupportedOperationException
                        ("В это время сделка не могла быть выполнена");
            }

            // проверяем введенную дату(двухзначную): если больше текущей значит 1900,
            // если нет - 2000
            if ((year + 2000) > Calendar.getInstance().get(Calendar.YEAR)) {
                year = year + 1900;
            } else year = year + 2000;

            // TODO: 19.07.17 Проверить работоспособность при постом поле, ввести исключения
            month = Integer.valueOf(eTMonth.getText().toString());
            month = month - 1; // перевожу в значения константы Calendar.MONTH

            day = Integer.valueOf(eTDay.getText().toString());

            String strPrice = eTPrice.getText().toString();
            Float floatPrice = Float.valueOf(strPrice);
            //цену акции умножаю на 10000 чтобы уйти от запятой, т.е. 1 руб = 10000 ед.
            // т.к. на бирже акций меньше 0,0001 нет
            price = (int) (floatPrice * MULTIPLIER_FOR_MONEY);
            // если price и floatprice не равны значит разрядность цены слишком мала
            //  и часть после запятой будет отброшена
            if (price != floatPrice * MULTIPLIER_FOR_MONEY) {
                throw new UnsupportedOperationException("Разряд числа price не поддерживается");
            }

            volume = Integer.valueOf(eTVolume.getText().toString());

            // TODO: 19.07.17 изменить 10000 на 100, т.к. комиссия не может быть меньше копейки
            String strFee = eTFee.getText().toString();
            Float floatFee = Float.valueOf(strFee);
            //коммисию умножаю на 10000 чтобы уйти от запятой, т.е. 1 руб = 10000 ед.
            // и привожу к int. если есть значение после запятой оно будет отброшено
            fee = (int) (floatFee * MULTIPLIER_FOR_MONEY);
            // если price и floatprice не равны значит разрядность цены слишком мала
            if (fee != floatFee * MULTIPLIER_FOR_MONEY)
                throw new UnsupportedOperationException("Разряд числа fee не поддерживается");
            fee = Integer.valueOf(eTFee.getText().toString());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Дата указана не верно", Toast.LENGTH_LONG).show();
            Log.d(TAG, e.toString());
            e.printStackTrace();

            return;
        } catch (Exception e) {

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d(TAG, e.toString());
            e.printStackTrace();

            return;
        }

        ContentValues contentValues = new ContentValues();

        contentValues.put(Contract.DealsEntry.COLUMN_PORTFOLIO, portfolio);
        contentValues.put(Contract.DealsEntry.COLUMN_TICKER, ticker);
        contentValues.put(Contract.DealsEntry.COLUMN_TYPE, type);
        contentValues.put(Contract.DealsEntry.COLUMN_DATE,
                DateUtils.getTimeForMoscowInMillis(year, month, day));

        contentValues.put(Contract.DealsEntry.COLUMN_PRICE, price);
        contentValues.put(Contract.DealsEntry.COLUMN_VOLUME, volume);
        contentValues.put(Contract.DealsEntry.COLUMN_FEE, fee);

        Uri uri = getContentResolver().insert(Contract.DealsEntry.CONTENT_URI, contentValues);

        Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show();
    }
}
