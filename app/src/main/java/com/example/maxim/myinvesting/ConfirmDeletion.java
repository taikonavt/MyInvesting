package com.example.maxim.myinvesting;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 24.08.17.
 */

public class ConfirmDeletion extends DialogFragment
        implements View.OnClickListener {

    Button btnOK;
    Button btnCancel;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.confirm_deletion, null);

        Log.d(TAG, v.findViewById(R.id.btn_df_confirm_ok) + ConfirmDeletion.class.getSimpleName());
        Log.d(TAG, v.findViewById(R.id.btn_df_confirm_cancel) + ConfirmDeletion.class.getSimpleName());

        btnOK = (Button) v.findViewById(R.id.btn_df_confirm_ok);
        btnCancel = (Button) v.findViewById(R.id.btn_df_confirm_cancel);

        btnCancel.setOnClickListener(this);
        btnOK.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_df_confirm_ok:


                dismiss();

                break;

            case R.id.btn_df_confirm_cancel:
                dismiss();
        }
    }
}
