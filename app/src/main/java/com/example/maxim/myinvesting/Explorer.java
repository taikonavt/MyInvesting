package com.example.maxim.myinvesting;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 15.11.17.
 */
 public class Explorer extends AppCompatActivity {

     ExplorerAdapter adapter;

     String[] folderContent;

     RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_explorer);

        checkPermission();

        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        String path = file.getPath();

        // устанавливаю новую toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(path);
        setSupportActionBar(toolbar);

        // список файлов и папок в file
        folderContent = file.list();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView = (RecyclerView) findViewById(R.id.rv_activity_explorer);

        recyclerView.setHasFixedSize(true);

        adapter = new ExplorerAdapter();

        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(layoutManager);

        adapter.swap(folderContent);
    }

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
        }
        else Log.d(TAG, "permission OK");
    }
}
