package com.example.maxim.myinvesting;

import android.Manifest;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import com.example.maxim.myinvesting.data.Contract;
import com.example.maxim.myinvesting.data.PortfolioItem;
import com.example.maxim.myinvesting.data.PortfolioNames;

import static com.example.maxim.myinvesting.data.Const.*;


public class MainActivity extends AppCompatActivity
                    implements EnterPortfolioDialogFragment.FragmentListener {

    private DrawerLayout drawerLayout = null;
    private ActionBarDrawerToggle toggle = null;
    private NavigationView mDrawer;
    public String nameOfPortfolio;
    private final static String NAME_OF_PROTFOLIO_KEY = "nameOfPortfolioKey";
    private Toolbar toolbar;

    private InfoFragment fragment = null;
    private final String FRAGMENT_KEY = "key";

    private boolean showAddButton = true;
    private boolean showDeleteButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // устанавливаю новую toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {

            nameOfPortfolio = savedInstanceState.getString(NAME_OF_PROTFOLIO_KEY);

            Fragment saveFragment = getSupportFragmentManager().getFragment(savedInstanceState,
                    FRAGMENT_KEY);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.ll_main_activity, saveFragment)
                    .commit();

        } else
            showFragment();


        mDrawer = (NavigationView) findViewById(R.id.drawer);

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
        getSupportActionBar().setTitle(null);

        checkPermission();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Fragment saveFragment = getSupportFragmentManager().findFragmentById(R.id.ll_main_activity);

        if (saveFragment != null) {
            getSupportFragmentManager().putFragment(outState, FRAGMENT_KEY, saveFragment);
        }

        outState.putString(NAME_OF_PROTFOLIO_KEY, nameOfPortfolio);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_fragment_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.setGroupVisible(R.id.add_group_info_fragment, showAddButton);
        menu.setGroupVisible(R.id.delete_group_portfolio_fragment, showDeleteButton);

        return super.onPrepareOptionsMenu(menu);
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

                    // получаем Uri фрагмента. В зависимости от полученного uri вызываем ту или иную активити
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
                            ("MainActivity.java, onOptionItemSelected(), Unknown Uri");
                } catch (UnsupportedOperationException e) {
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }

                break;

            case R.id.delete_portfolio_fragment: {

                AlertDialog diaBox = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.title_deletion))
                        .setMessage(getResources().getString(R.string.tv_confirm_hint))
                        .setPositiveButton(getResources().getString(R.string.btn_df_ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Uri uri = Contract.PortfolioEntry.CONTENT_URI;

                                        String selectionArgs [] = {getNameOfPortfolio()};

                                        getContentResolver().delete(uri, null, selectionArgs);

                                        invalidateOptionsMenu();

                                        fragment = null;

                                        addItemsToDrawer(mDrawer);

                                        showFragment();

                                        showAddButton = true;
                                        showDeleteButton = false;

                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.btn_df_cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();
                                    }
                                })
                        .create();

                diaBox.show();
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    // выбор пунктов в drawer меню
    public void selectDrawerItem(MenuItem item) {

        int id = item.getItemId();

        // Deals
        if (id == R.id.nav_deal_item) {

            showAddButton = true;
            showDeleteButton = false;
            invalidateOptionsMenu();

            drawerLayout.closeDrawers();
            fragment = new InfoDealFragment();
            showFragment();
        }

        // Inputs
        else if (id == R.id.nav_input_item) {

            // показывает кнопку add (+) вверху экрана
            showAddButton = true;
            showDeleteButton = false;
            invalidateOptionsMenu();

            drawerLayout.closeDrawers();
            fragment = new InfoInputFragment();
            showFragment();
        }

        // Add new portfolio name
        else if (id == ADD_BUTTON_ID) {

            drawerLayout.closeDrawers();
            DialogFragment dialogFragment = new EnterPortfolioDialogFragment();
            dialogFragment.show(getFragmentManager(), "Enter name fragment");
        }

        // Open portfolio
        else if (SUB_MENU_ITEM_ID < id || id < (SUB_MENU_ITEM_ID + 1000)) {

            // скрываю кнопку "Add" в Toolbar, т.к. она не нужна
            showAddButton = false;
            showDeleteButton = true;
            invalidateOptionsMenu();

            nameOfPortfolio = (String) item.getTitle();

            drawerLayout.closeDrawers();

            PortfolioFrarment portfolioFrarment = new PortfolioFrarment();

            if (!portfolioFrarment.isVisible()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.ll_main_activity, portfolioFrarment)
                        .commit();
            }
        }

        else
            Toast.makeText(this, item.getItemId() +
                    getString(R.string.unknown_command), Toast.LENGTH_LONG).show();
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

        int GROUP_ID = 1;
        int SUBMENU_ID = 2;

        Menu menu = mDrawer.getMenu();

        menu.removeItem(SUBMENU_ID);

        SubMenu subMenu = menu.addSubMenu(GROUP_ID, SUBMENU_ID, 100, R.string.nav_title_submenu);

        subMenu
                .add(GROUP_ID, ADD_BUTTON_ID, 1, R.string.nev_add_new_subitem)
                .setIcon(R.drawable.ic_add_black_24dp);

        String [] strings = PortfolioNames.readPortfoliosNames(this);

        int length;
        try {
            length = strings.length;
        }
        catch (NullPointerException e) {
            length = 0;
        }

        for (int i = 0; i < length; i++) {
            subMenu
                    .add(GROUP_ID, i + SUB_MENU_ITEM_ID, i+1, strings[i]);
        }
    }

    // реализация интерфейса EnterPortfolioDialogFragment.FragmentListener
    // позволяет получить String с названием портфеля из EnterPortfolioDialogFragment
    @Override
    public void fragmentOnClickOKButton(String nameOfPortfolio) {

        PortfolioNames.savePortfolioName(this, nameOfPortfolio);

        addItemsToDrawer(mDrawer);
    }

    public String getNameOfPortfolio() {
        return nameOfPortfolio;
    }

    // обработка нажатия на пункт portfolioFragment
    public void onPortfolioItemClick(PortfolioItem portfolioItem) {

        TickerFragment tickerFragment = new TickerFragment();

        tickerFragment.putPortfolioItem(portfolioItem);

        if (!tickerFragment.isVisible()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.ll_main_activity, tickerFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
        }
        else Log.d(TAG, "permission OK");
    }
}
