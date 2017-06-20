package com.example.maxim.myinvesting;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import static com.example.maxim.myinvesting.data.Const.TAG;

// TODO: 15.06.17 Исправить активити для работы с фрагментом
public class MainActivity extends AppCompatActivity implements
        OnItemClickListener {

    private DrawerLayout drawerLayout = null;
    private ActionBarDrawerToggle toggle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView drawer = (ListView) findViewById(R.id.drawer);

        drawer.setAdapter(new ArrayAdapter<String>(
                this,
                R.layout.drawer_row,
                getResources().getStringArray(R.array.drawer_items_array)
        ));

        drawer.setOnItemClickListener(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        toggle =
                new ActionBarDrawerToggle(this, drawerLayout,
                        R.string.drawer_open,
                        R.string.drawer_close);

        //кнопка для открытия sliding меню
        drawerLayout.addDrawerListener(toggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        switch (position) {
            // Deals
            case 0: {
                drawerLayout.closeDrawers();
                InfoDealFragment fragment = new InfoDealFragment();
                onClick(fragment);
                break;
            }
            // Inputs
            case 1: {
                drawerLayout.closeDrawers();
                InfoInputFragment fragment = new InfoInputFragment();
                onClick(fragment);
                break;
            }
            // Transaction
            case 2:
                drawerLayout.closeDrawers();
                Intent intent = new Intent(this, AddDealActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void onClick(Fragment fragment) {
Log.d(TAG, "MainActivity.onClick()");
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.ll_main_activity, fragment)
                .commit();
    }
}
