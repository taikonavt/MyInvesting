package com.example.maxim.myinvesting;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
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

     RecyclerView recyclerView;

     String path;

     public static final String PATH_KEY = "path_key";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_explorer);

        checkPermission();

        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        path = file.getPath();

        // устанавливаю новую toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(path);
        setSupportActionBar(toolbar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView = (RecyclerView) findViewById(R.id.rv_activity_explorer);

        recyclerView.setHasFixedSize(true);

        adapter = new ExplorerAdapter();

        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(layoutManager);

        // список файлов и папок в file
        String [] folderContent = file.list();

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

    public void setPath(String path) {

        File file;

        if (path == "..") {

            file = new File(this.path);

            file = file.getParentFile();

            this.path = file.getAbsolutePath();

            String [] folderContent = file.list();

            adapter.swap(folderContent);
        }
        else {

            this.path = this.path + "/" + path;

            file = new File(this.path);


            if (file.isDirectory()) {

                String[] temp = file.list();

                String[] folderContent = new String[temp.length + 1];

                folderContent[0] = "..";

                for (int i = 0; i < temp.length; i++) {

                    folderContent[i + 1] = temp[i];
                }

                adapter.swap(folderContent);

            } else if (file.isFile()) {

                Intent intent = new Intent();

                intent.putExtra(PATH_KEY, file.getAbsolutePath());

                setResult(RESULT_OK, intent);

                finish();
            }
        }
    }
}
