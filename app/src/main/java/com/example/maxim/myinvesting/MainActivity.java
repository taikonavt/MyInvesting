package com.example.maxim.myinvesting;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.example.maxim.myinvesting.data.Contract;

import static com.example.maxim.myinvesting.data.Const.TAG;

public class MainActivity extends AppCompatActivity implements
        OnItemClickListener {

    private DrawerLayout drawerLayout = null;
    private ActionBarDrawerToggle toggle = null;
    private InfoFragment fragment = null;
    private ListView drawer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // если экран пустой, то показываем Deal фрагмент
        if (getFragmentManager().findFragmentById(R.id.ll_main_activity) == null) {
            showFragment();
        }

        drawer = (ListView) findViewById(R.id.drawer);

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            // Deals
            case 0:
                drawerLayout.closeDrawers();
                fragment = new InfoDealFragment();
                showFragment();
                break;

            // Inputs
            case 1:
                drawerLayout.closeDrawers();
                fragment = new InfoInputFragment();
                showFragment();
                break;

            // Transaction
            case 2: {
                drawerLayout.closeDrawers();
                Intent intent = new Intent(this, AddDealActivity.class);
                startActivity(intent);
                break;
            }
            case 3: {
                drawerLayout.closeDrawers();
                Intent intent = new Intent(this, AddInputActivity.class);
                startActivity(intent);
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
}
