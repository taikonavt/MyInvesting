package com.example.maxim.myinvesting;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by maxim on 01.08.17.
 */

public class EnterPortfolioDialogFragment extends DialogFragment
                                    implements View.OnClickListener {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.enter_name_of_portfolio_dialog_fragment, null);

        view.findViewById(R.id.btn_df_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_df_cancel).setOnClickListener(this);

        return view;
    }

    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_df_ok:
                dismiss();
                break;
            case R.id.btn_df_cancel:
                dismiss();
                break;
        }
    }
}
