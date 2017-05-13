package com.example.maxim.myinvesting;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity implements
        OnItemClickListener {

    final static int MULTIPLIER_FOR_MONEY = 10000;
    public static final String TAG = "MyLog";

    private DrawerLayout drawerLayout = null;
    private ActionBarDrawerToggle toggle = null; // в чем разница между v4 и v7?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView drawer = (ListView) findViewById(R.id.drawer);
Log.d(TAG, "after drawer");
        drawer.setAdapter(new ArrayAdapter<String>(
                this,
                R.layout.drawer_row,
                getResources().getStringArray(R.array.drawer_items_array)
        ));
Log.d(TAG, "after setAdapter");
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
Log.d(TAG, "after drawerLayout");
        toggle =
                new ActionBarDrawerToggle(this, drawerLayout,
                        R.string.drawer_open,
                        R.string.drawer_close);
Log.d(TAG, "after toggle");
        //кнопка для открытия sliding меню
        drawerLayout.setDrawerListener(toggle);
Log.d(TAG, "after setDrawerListener");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
Log.d(TAG, "after setDisplayHome");
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public void onClickDeal(View view) {

        Intent intent = new Intent(this, InfoDealActivity.class);
        startActivity(intent);
    }

    public void onClickInput(View view) {

        Intent intent = new Intent(this, InfoInputActivity.class);
        startActivity(intent);
    }
}
