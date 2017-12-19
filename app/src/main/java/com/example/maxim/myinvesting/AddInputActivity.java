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

    private static final int REQUEST_CODE = 746;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_input);

        // устанавливаю новую toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.add_input));
        setSupportActionBar(toolbar);

        String [] strings = PortfolioNames.readPortfoliosNames(this);

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

            // если длина строки начиная с точки больше разрядности мультиплекатора, то разряд не поддерживается
            {
                int indexOfDec = strAmount.indexOf(".");

                int lengthOfMult = (int) Math.log10(MULTIPLIER_FOR_CURRENCY);

                if (indexOfDec >= 0) {
                    if (strAmount.substring(indexOfDec).length() > lengthOfMult + 1)
                        throw new UnsupportedOperationException("Разряд числа amount не поддерживается");
                }
            }

            Double floatAmount = Double.valueOf(strAmount);

            //сумму ввода умножаю на 1 000 000 чтобы уйти от запятой
            amount = Math.round(floatAmount * MULTIPLIER_FOR_MONEY);

Log.d(TAG, AddInputActivity.class.getSimpleName() + " onClick() " + amount);

            String strFee = eTFee.getText().toString();

            if (strFee.length() == 0)
                throw new UnsupportedOperationException("Fee не задан");

            // если длина строки начиная с точки больше разрядности мультиплекатора, то разряд не поддерживается
            {
                int indexOfDec = strAmount.indexOf(".");

                int lengthOfMult = (int) Math.log10(MULTIPLIER_FOR_CURRENCY);

                if (indexOfDec >= 0) {

                    if (strAmount.substring(indexOfDec).length() > lengthOfMult + 1)
                        throw new UnsupportedOperationException("Разряд числа amount не поддерживается");
                }
            }

            Float floatFee = Float.valueOf(strFee);

            //сумму ввода умножаю на 1 000 000 чтобы уйти от запятой
            fee = Math.round(floatFee * MULTIPLIER_FOR_MONEY);

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

            Toast.makeText(this, getResources().getString(R.string.db_got_item), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, getResources().getString(R.string.db_have_not_got_item), Toast.LENGTH_LONG).show();
    }

    void parseHtml(String path) {

        HtmlParser htmlParser = new HtmlParser(this.getClass().getSimpleName());

        String[] paths = {path};

        htmlParser.execute(paths);

        finish();
    }


}
