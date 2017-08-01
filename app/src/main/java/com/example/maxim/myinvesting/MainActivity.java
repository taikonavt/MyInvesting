package com.example.maxim.myinvesting;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import com.example.maxim.myinvesting.data.Contract;

import static com.example.maxim.myinvesting.data.Const.TAG;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout = null;
    private ActionBarDrawerToggle toggle = null;
    private InfoFragment fragment = null;

    private final static int ADD_BUTTON_ID = 25943;
    private final static String KEY = "key";
    private final static String ARRAY_SIZE = "array_size";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // если экран пустой, то показываем Deal фрагмент
        if (getFragmentManager().findFragmentById(R.id.ll_main_activity) == null) {
            showFragment();
        }

        NavigationView mDrawer = (NavigationView) findViewById(R.id.drawer);

        addItemsToDrawer(mDrawer);

        mDrawer.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        selectDrawerItem(item);
                        return true;
                    }
                }
        );

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_fragment_menu, menu);
        return true;
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

        // срабатывает при нажатии кнопки "add"
        switch (item.getItemId()) {
            case R.id.add_info_fragment:

                try {

                    // получаем Uri фрагмента. В зависимости от полученного uri вызываем тот или иной фрагмент
                    Uri uri = fragment.getUri();

                    if (uri == Contract.DealsEntry.CONTENT_URI) {
                        drawerLayout.closeDrawers();
                        Intent intent = new Intent(this, AddDealActivity.class);
                        startActivity(intent);
                    } else if (uri == Contract.InputEntry.CONTENT_URI) {
                        drawerLayout.closeDrawers();
                        Intent intent = new Intent(this, AddInputActivity.class);
                        startActivity(intent);
                    } else throw new UnsupportedOperationException
                            ("MainActivity.java, onOptionItemSelected, Unknown Uri");
                } catch (UnsupportedOperationException e) {
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
        }

        return super.onOptionsItemSelected(item);
    }

    // выбор пунктов в drawer меню
    public void selectDrawerItem(MenuItem item) {

        switch (item.getItemId()) {

            // Deals
            case R.id.nav_deal_item:
                drawerLayout.closeDrawers();
                fragment = new InfoDealFragment();
                showFragment();
                break;

            // Inputs
            case R.id.nav_input_item:
                drawerLayout.closeDrawers();
                fragment = new InfoInputFragment();
                showFragment();
                break;

            // Add new portfolio name
            case ADD_BUTTON_ID: {
                drawerLayout.closeDrawers();
                DialogFragment dialogFragment = new EnterPortfolioDialogFragment();
                dialogFragment.show(getFragmentManager(), "Enter portfolio name fragment");
                break;
            }

            default:
                Toast.makeText(this, "Неизвестная команда", Toast.LENGTH_LONG).show();
        }
    }

    private void showFragment() {
        // срабатывает при запуске программы
        if (fragment == null) {
            fragment = new InfoDealFragment();
        }

        // срабатывает при нажатии на пункт дровера
        if (!fragment.isVisible()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.ll_main_activity, fragment)
                    .commit();
        }
    }

    private void addItemsToDrawer(NavigationView mDrawer) {

        Menu menu = mDrawer.getMenu();

        SubMenu subMenu = menu.addSubMenu(R.string.nav_title_submenu);

        int i = subMenu.size();

        subMenu
                .add(0, ADD_BUTTON_ID, 1, R.string.nev_add_new_subitem)
                .setIcon(R.drawable.ic_add_black_24dp);
    }

    private String[] readPortfoliosNames() {

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);

        int arraySize = sharedPreferences.getInt(ARRAY_SIZE, 0);

        if (arraySize == 0)
            return null;

        String [] portfolios = new String[arraySize];

        for (int i = 0; i < arraySize; i++) {
            portfolios[i] = sharedPreferences.getString(KEY + i, null);
        }

        return portfolios;
    }

    private void savePortfolioName(String string) {

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);

        int arraySize = sharedPreferences.getInt(ARRAY_SIZE, 0);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY+ arraySize, string);

        editor.putInt(ARRAY_SIZE, arraySize + 1);

        editor.commit();
    }
}
