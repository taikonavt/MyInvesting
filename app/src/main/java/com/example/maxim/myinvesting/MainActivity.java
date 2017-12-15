package com.example.maxim.myinvesting;

import android.Manifest;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
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
import com.example.maxim.myinvesting.data.InvestingDbHelper;
import com.example.maxim.myinvesting.data.PortfolioItem;
import com.example.maxim.myinvesting.data.PortfolioNames;
import com.example.maxim.myinvesting.utilities.HtmlParser;

import static com.example.maxim.myinvesting.data.Const.*;

// TODO: 12.12.17 Редактирование таблицы Атон на случай ошибки во вводе
// TODO: 12.12.17 Парсинг таблицы с коммисией за ведение ИИС
// TODO: 13.12.17 Добавить дивиденды в таблицу с историей тикера

public class MainActivity extends AppCompatActivity
                    implements EnterPortfolioDialogFragment.FragmentPortfolioListener,
                                EnterSecurityDialogFragment.FragmentSecurityListener {

    private DrawerLayout drawerLayout = null;
    private ActionBarDrawerToggle toggle = null;
    private NavigationView mDrawer;
    public String nameOfPortfolio;
    private final static String NAME_OF_PROTFOLIO_KEY = "nameOfPortfolioKey";
    private Toolbar toolbar;

    private InfoFragment fragment = null;
    private static String FRAGMENT_KEY = "key";
    private static final String UNKNOWN_STRINGS_KEY = "unknown";
    private static final String UNKNOWN_STR_INDEX_KEY = "unknown_index";

    private boolean showAddButton = true;
    private boolean showDeleteButton = false;

    public static final String REFRESH_KEY = "refresh";
    public static final String INSERTED_DEAL_ID_KEY = "insertedDealId";
    public static final String INSERTED_INPUT_ID_KEY = "insertedInputId";
    public static final String UNKNOWN_ISNIS_KEY = "unknownIsnis";
    public static final String BROADCAST_ACTION = "com.example.maxim.myinvesting";

    BroadcastReceiver br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // устанавливаю новую toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {

            nameOfPortfolio = savedInstanceState.getString(NAME_OF_PROTFOLIO_KEY);

            securityStringsUnknown = savedInstanceState.getStringArray(UNKNOWN_STRINGS_KEY);

            unknownSecIndex = savedInstanceState.getInt(UNKNOWN_STR_INDEX_KEY);

            Fragment saveFragment = getSupportFragmentManager().getFragment(savedInstanceState,
                    FRAGMENT_KEY);

            // TODO: 05.12.17 Save fragment in SaveInstance

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

        // слушаю broadcast сообщения от HtmlParser, если получил, то обновляю recyclerView InfoDealFragment
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String stringRefresh = intent.getStringExtra(REFRESH_KEY);

                // неизвестные названия компаний из таблицы сделок
                String[] stringsUnknown = intent.getStringArrayExtra(UNKNOWN_ISNIS_KEY);

                long[] dealsInsertedId = intent.getLongArrayExtra(INSERTED_DEAL_ID_KEY);

                long[] inputsInsertedId = intent.getLongArrayExtra(INSERTED_INPUT_ID_KEY);

                // если есть значение ключа refresh обновляю
                if (stringRefresh != null && stringRefresh.equals(HtmlParser.REFRESH)) {

                    // обновляю
                    refresh();
                }
                // если нет ключа refresh, но есть stringsUnknown
                else if (stringsUnknown != null) {

                    toKnowTicker(stringsUnknown, dealsInsertedId, inputsInsertedId);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);

        registerReceiver(br, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(br);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Fragment saveFragment = getSupportFragmentManager().findFragmentById(R.id.ll_main_activity);

        if (saveFragment != null) {
            getSupportFragmentManager().putFragment(outState, FRAGMENT_KEY, saveFragment);
        }

        outState.putString(NAME_OF_PROTFOLIO_KEY, nameOfPortfolio);

        outState.putStringArray(UNKNOWN_STRINGS_KEY, securityStringsUnknown);

        outState.putInt(UNKNOWN_STR_INDEX_KEY, unknownSecIndex);
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

    // обновляю список портфелей в дровере
    private void addItemsToDrawer(NavigationView mDrawer) {

        int GROUP_ID = 1;
        int SUBMENU_ID = 2;

        // получаю меню дровера
        Menu menu = mDrawer.getMenu();

        // удаляю список портфелей из подменю
        menu.removeItem(SUBMENU_ID);

        // добавляю подменю в дровер
        SubMenu subMenu = menu.addSubMenu(GROUP_ID, SUBMENU_ID, 100, R.string.nav_title_submenu);

        // добавляю в подменю кнопку + Add new
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

        // добавляю в подменю список портфелей
        for (int i = 0; i < length; i++) {
            subMenu
                    .add(GROUP_ID, i + SUB_MENU_ITEM_ID, i+1, strings[i]);
        }
    }

    // реализация интерфейса EnterPortfolioDialogFragment.FragmentListener
    // позволяет получить String с названием портфеля из EnterPortfolioDialogFragment
    @Override
    public void fragmentPortfolioOnClickOKButton(String nameOfPortfolio) {

        PortfolioNames.savePortfolioName(this, nameOfPortfolio);

        addItemsToDrawer(mDrawer);
    }

    // TODO: 21.11.17 попробовать реализоавть передачу имени вместо вызова функции
    // может быть ошибка если MainActivity была уничтожена
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

    // проверяю наличие разрешения на чтение и запись на внешнюю память
    // если нет, то запрашиваю
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

    // обновление recyclerView и списка портфелей
    private void refresh() {

        // fragment бывает только InfoDealFragment или InfoInputFragment, поэтому проверка не требуется
        Uri uri = fragment.getUri();

        // если fragment DealFragment и фрагмент прикреплен
        if (uri == Contract.DealsEntry.CONTENT_URI && fragment.isAdded()) {

            InfoDealFragment fragmentTemp = (InfoDealFragment) fragment;

            // перезагружаю курсор
            fragmentTemp.getLoaderManager().restartLoader(InfoFragment.INFO_LOADER_ID, null, fragmentTemp);

            // обновляю recyclerView
            fragmentTemp.notifyAdapter();
        }

        // если fragment InputFragment и фрагмент прикреплен
        if (uri == Contract.InputEntry.CONTENT_URI && fragment.isAdded()) {

            InfoInputFragment fragmentTemp = (InfoInputFragment) fragment;

            // перезагружаю курсор
            fragmentTemp.getLoaderManager().restartLoader(InfoFragment.INFO_LOADER_ID, null, fragmentTemp);

            // обновляю recyclerView
            fragmentTemp.notifyAdapter();
        }

        // обновляю список портфелей в дровере
        addItemsToDrawer(mDrawer);
    }

    // неизвестные компании из таблицы сделок
    private String[] securityStringsUnknown;

    private int unknownSecIndex;

    private void toKnowTicker(String[] stringsUnknown, long[] dealsInsertedId, long[] inputsInsertedId) {

        securityStringsUnknown = stringsUnknown;

        drawerLayout.closeDrawers();

        EnterSecurityDialogFragment dialogFragment = new EnterSecurityDialogFragment();

        unknownSecIndex = 0;

        // запускаю dialogFragment для ручного ввода тикера неизвестной компании
        dialogFragment.setAskedTicker(securityStringsUnknown[unknownSecIndex]);

        dialogFragment.show(getFragmentManager(), "Enter security fragment");

        Uri uriDeal = Contract.DealsEntry.CONTENT_URI;

        for (long l :
                dealsInsertedId) {

            Uri tempUri = uriDeal.buildUpon().appendPath(String.valueOf(l)).build();

            getContentResolver().delete(tempUri, null, null);
        }

        for (long l :
                inputsInsertedId) {

            Uri tempUri = uriDeal.buildUpon().appendPath(String.valueOf(l)).build();

            getContentResolver().delete(tempUri, null, null);
        }
    }

    @Override
    public void fragmentSecurityOnClickOKButton(String ticker) {

        // вставляю тикер компании в БД атон
        InvestingDbHelper openHelper = new InvestingDbHelper(this);

        final SQLiteDatabase db = openHelper.getReadableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(Contract.AtonEntry.COLUMN_ATON_NAME, securityStringsUnknown[unknownSecIndex]);

        cv.put(Contract.AtonEntry.COLUMN_TICKER, ticker);

        db.insert(Contract.AtonEntry.TABLE_NAME, null, cv);

        unknownSecIndex++;

        // запускаю диалог со следующей неизвестной компанией
        if (unknownSecIndex < securityStringsUnknown.length) {

            drawerLayout.closeDrawers();

            EnterSecurityDialogFragment dialogFragment = new EnterSecurityDialogFragment();

            dialogFragment.setAskedTicker(securityStringsUnknown[unknownSecIndex]);

            dialogFragment.show(getFragmentManager(), "Enter security fragment");
        }
        else {

            Toast.makeText(this, getString(R.string.repeat_last_operation), Toast.LENGTH_LONG).show();
        }
    }
}
