package com.example.maxim.myinvesting;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.data.PortfolioNames;
import com.example.maxim.myinvesting.utilities.DateUtils;
import com.example.maxim.myinvesting.utilities.HtmlParser;

import static com.example.maxim.myinvesting.data.Const.*;

import java.util.Calendar;

/**
 * Created by maxim on 27.03.17.
 */

public class AddDealActivity extends AppCompatActivity {

    Spinner spinnerPortfolio;
    EditText eTTicker;
    Spinner spinnerType;
    EditText eTYear;
    EditText eTMonth;
    EditText eTDay;
    EditText eTPrice;
    EditText eTVolume;
    EditText eTFee;

    private static final int REQUEST_CODE = 745;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_deal);

        // устанавливаю новую toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.add_deal));
        setSupportActionBar(toolbar);

        String [] strings = PortfolioNames.readPortfoliosNames(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, strings);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerPortfolio = (Spinner) findViewById(R.id.spinnerPortfolio);
        spinnerPortfolio.setAdapter(adapter);

        eTTicker = (EditText) findViewById(R.id.eTTicker);
        spinnerType = (Spinner) findViewById(R.id.spinnerType);
        eTYear = (EditText) findViewById(R.id.eTYear);
        eTMonth = (EditText) findViewById(R.id.eTMonth);
        eTDay = (EditText) findViewById(R.id.eTDay);
        eTPrice = (EditText) findViewById(R.id.eTPrice);
        eTVolume = (EditText) findViewById(R.id.eTVolume);
        eTFee = (EditText) findViewById(R.id.eTFee);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.add_deal_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.open_deals_button: {

                Intent intent = new Intent(this, Explorer.class);

                startActivityForResult(intent, REQUEST_CODE);

                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data == null) {return;}

        parseHtml(data.getStringExtra(Explorer.PATH_KEY));
    }

    String portfolio = null;
    String ticker = null;
    String type = null;
    int year = 0;
    int month = 0;
    int day = 0;
    int price = 0;
    int volume = 0;
    int fee = 0;

    public void onClick(View view) {

        try {

            portfolio = spinnerPortfolio.getSelectedItem().toString();

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

            month = Integer.valueOf(eTMonth.getText().toString());
            month = month - 1; // перевожу в значения константы Calendar.MONTH

            day = Integer.valueOf(eTDay.getText().toString());

            String strPrice = eTPrice.getText().toString();
            if (strPrice.length() == 0)
                throw new UnsupportedOperationException("Price не задан");

            Float floatPrice = Float.valueOf(strPrice);

            //цену акции умножаю на 10000 чтобы уйти от запятой, т.е. 1 руб = 10000 ед.
            // т.к. на бирже акций меньше 0,0001 нет
            price = (int) (floatPrice * MULTIPLIER_FOR_MONEY);

            // если price и floatprice не равны значит разрядность цены слишком мала
            //  и часть после запятой будет отброшена
            if (price != floatPrice * MULTIPLIER_FOR_MONEY) {
                throw new UnsupportedOperationException("Разряд числа price не поддерживается");
            }

            String strVolume = eTVolume.getText().toString();
            if (strVolume.length() == 0)
                throw new UnsupportedOperationException("Volume не задан");
            volume = Integer.valueOf(strVolume);

            String strFee = eTFee.getText().toString();
            if (strFee.length() == 0)
                throw new UnsupportedOperationException("Fee не задан");

            Float floatFee = Float.valueOf(strFee);

            //сумму ввода умножаю на 100 чтобы уйти от запятой
            fee = (int) (floatFee * MULTIPLIER_FOR_CURRENCY);

Log.d(TAG, AddDealActivity.class.getSimpleName() + " onClick(); " + (floatFee * MULTIPLIER_FOR_CURRENCY) + " " + fee);

            // если fee и floatFee не равны значит разрядность цены слишком мала
            //  и часть после запятой будет отброшена
            if (fee != floatFee * MULTIPLIER_FOR_CURRENCY) {
                throw new UnsupportedOperationException("Разряд числа fee не поддерживается");
            }

            // домножаю на 100, чтобы привести все деньги в программе
            // к одной разрядности 1 руб = 10000 ед.
            fee = fee * MULTIPLIER_FOR_MONEY / MULTIPLIER_FOR_CURRENCY;

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

        // сообщение о вставке транзакции в базу данных
        String string = uri.getPathSegments().get(1);

        if (Long.parseLong(string) >= 0) {

            Toast.makeText(this, getResources().getString(R.string.db_got_item),
                    Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, getResources().getString(R.string.db_have_not_got_item),
                    Toast.LENGTH_LONG).show();
    }

    void parseHtml(String path) {

        HtmlParser htmlParser = new HtmlParser(this.getClass().getSimpleName());

        String[] paths = {path};

        htmlParser.execute(paths);

        finish();
    }
}
