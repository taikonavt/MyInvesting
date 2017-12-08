package com.example.maxim.myinvesting;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 07.12.17.
 */

public class EnterSecurityDialogFragment extends DialogFragment
        implements View.OnClickListener {

    private EnterSecurityDialogFragment.FragmentSecurityListener mListener;
    private EditText editText;
    private TextView textView;

    private String askedTicker;

    // интерефейс реализуемый в MainActivity для взаимодействия активити и диалогфрагмента
    public interface FragmentSecurityListener {
        void fragmentSecurityOnClickOKButton(String string);
    }

    public void setAskedTicker(String askedTicker) {

        this.askedTicker = askedTicker;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.enter_name_of_secutiry_dialog_fragment, null);

        view.findViewById(R.id.btn_df_sec_ok).setOnClickListener(this);

        editText = (EditText) view.findViewById(R.id.et_df_enter_security);
        textView = (TextView) view.findViewById(R.id.tv_df_sec_question);

        String string = getString(R.string.unknown_security);

        string = string + askedTicker;

        textView.setText(string);

        return view;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_df_sec_ok:

                // по нажатию кнопки читаю тикер из окна. Если пусто выводим сообщение в Тоаст
                String ticker;

                try {

                    ticker = editText.getText().toString();

                    if (ticker.length() == 0)
                        throw new UnsupportedOperationException(
                                getString(R.string.toast_no_portfolio));

                } catch (UnsupportedOperationException e) {

                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, e.toString());
                    e.printStackTrace();

                    return;
                }

                try {
                    mListener = (EnterSecurityDialogFragment.FragmentSecurityListener) getActivity();
                } catch (ClassCastException e) {
                    throw new ClassCastException(getActivity().toString()
                            + " must implement FragmentListener");
                }
                // TODO: 02.08.17 Попробовать запустить эту ошибку

                mListener.fragmentSecurityOnClickOKButton(ticker);

                dismiss();

                break;

            case R.id.btn_df_cancel:
                dismiss();
                break;
        }
    }
}
