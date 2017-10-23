package com.example.maxim.myinvesting;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.utilities.DateUtils;

import java.util.Calendar;

import static com.example.maxim.myinvesting.data.Const.*;

/**
 * Created by maxim on 19.07.17.
 */

public class AddInputActivity extends AppCompatActivity {

    Spinner spinnerPortfolio;
    Spinner spinnerType;
    EditText eTYear;
    EditText eTMonth;
    EditText eTDay;
    EditText eTAmount;
    EditText eTFee;
    EditText eTNote;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_input);

        String [] strings = readPortfoliosNames();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, strings);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerPortfolio = (Spinner) findViewById(R.id.spinner_portfolio_input);
        spinnerPortfolio.setAdapter(adapter);

        spinnerType = (Spinner) findViewById(R.id.spinnerType_input);
        eTYear = (EditText) findViewById(R.id.eTYear_input);
        eTMonth = (EditText) findViewById(R.id.eTMonth_input);
        eTDay = (EditText) findViewById(R.id.eTDay_input);
        eTAmount = (EditText) findViewById(R.id.eTAmount_input);
        eTFee = (EditText) findViewById(R.id.eTFee_input);
        eTNote = (EditText) findViewById(R.id.eTNote_input);
    }

    String portfolio = null;
    String type = null;
    int year = 0;
    int month = 0;
    int day = 0;
    long amount = 0;
    String currency = "RUB";
    int fee = 0;
    String note = null;

    public void onClick(View view) {

        try {

            portfolio = spinnerPortfolio.getSelectedItem().toString();

            type = spinnerType.getSelectedItem().toString();

            year = Integer.valueOf(eTYear.getText().toString());

            // ввел это условие т.к. все мои вводы были сделаны после 2014 года
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

            month = Integer.valueOf(eTMonth.getText().toString());
            month = month - 1; // перевожу в значения константы Calendar.MONTH

            day = Integer.valueOf(eTDay.getText().toString());

            String strAmount = eTAmount.getText().toString();
            if (strAmount.length() == 0)
                throw new UnsupportedOperationException("Amount не задан");
            Float floatAmount = Float.valueOf(strAmount);

            //сумму ввода умножаю на 100 чтобы уйти от запятой
            amount = (long) (floatAmount * MULTIPLIER_FOR_CURRENCY);

            // если amount и floatAmount не равны значит разрядность цены слишком мала
            //  и часть после запятой будет отброшена
            if (amount != floatAmount * MULTIPLIER_FOR_CURRENCY) {
                throw new UnsupportedOperationException("Разряд числа amount не поддерживается");
            }
            // домножаю на 100, чтобы привести все деньги в программе
            // к одной разрядности 1 руб = 10000 ед.
            amount = (amount / MULTIPLIER_FOR_CURRENCY) * MULTIPLIER_FOR_MONEY;

            String strFee = eTFee.getText().toString();
            if (strFee.length() == 0)
                throw new UnsupportedOperationException("Fee не задан");

            Float floatFee = Float.valueOf(strFee);

            //сумму ввода умножаю на 100 чтобы уйти от запятой
            fee = (int) (floatFee * MULTIPLIER_FOR_CURRENCY);

            // если fee и floatFee не равны значит разрядность цены слишком мала
            //  и часть после запятой будет отброшена
            if (fee != floatFee * MULTIPLIER_FOR_CURRENCY) {
                throw new UnsupportedOperationException("Разряд числа fee не поддерживается");
            }

            // домножаю на 100, чтобы привести все деньги в программе
            // к одной разрядности 1 руб = 10000 ед.
            fee = fee * MULTIPLIER_FOR_MONEY / MULTIPLIER_FOR_CURRENCY;

            note = eTNote.getText().toString();

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

        contentValues.put(Contract.InputEntry.COLUMN_TYPE, type);
        contentValues.put(Contract.InputEntry.COLUMN_DATE,
                DateUtils.getTimeForMoscowInMillis(year, month, day));
        contentValues.put(Contract.InputEntry.COLUMN_AMOUNT, amount);
        contentValues.put(Contract.InputEntry.COLUMN_CURRENCY, currency);
        contentValues.put(Contract.InputEntry.COLUMN_FEE, fee);
        contentValues.put(Contract.InputEntry.COLUMN_PORTFOLIO, portfolio);
        contentValues.put(Contract.InputEntry.COLUMN_NOTE, note);

        Uri uri = getContentResolver().insert(Contract.InputEntry.CONTENT_URI, contentValues);

        // сообщение о вставке транзакции в базу данных
        String string = uri.getPathSegments().get(1);

        if (Long.parseLong(string) >= 0) {

            Toast.makeText(this, getResources().getString(R.string.db_got_item), Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(this, getResources().getString(R.string.db_have_not_got_item), Toast.LENGTH_LONG).show();
    }

    // получаю имена существующих портфелей из SharedPreferences
    public String[] readPortfoliosNames() {

        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        int arraySize = sharedPreferences.getInt(ARRAY_SIZE, 0);

        if (arraySize == 0)
            return null;

        String [] portfolios = new String[arraySize];

        for (int i = 0; i < arraySize; i++) {
            portfolios[i] = sharedPreferences.getString(KEY + i, null);
        }

        return portfolios;
    }
}
