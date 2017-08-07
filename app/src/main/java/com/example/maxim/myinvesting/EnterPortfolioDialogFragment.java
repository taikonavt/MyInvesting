package com.example.maxim.myinvesting;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import static com.example.maxim.myinvesting.data.Const.TAG;

/**
 * Created by maxim on 01.08.17.
 */
// класс для окна ввода имени портфеля
public class EnterPortfolioDialogFragment extends DialogFragment
                                    implements View.OnClickListener {

    FragmentListener mListener;
    EditText editText;

    // интерефейс реализуемый в MainActivity для взаимодействия активити и диалогфрагмента
    public interface FragmentListener {
        public void fragmentOnClickOKButton(String string);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.enter_name_of_portfolio_dialog_fragment, null);

        view.findViewById(R.id.btn_df_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_df_cancel).setOnClickListener(this);

        editText = (EditText) view.findViewById(R.id.et_df_enter_name);

        return view;
    }

    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_df_ok:

                // по нажатию кнопки читаем имя портфеля из окна. Если пусто выводим сообщение в Тоаст
                String nameOfPortfolio;

                try {

                    nameOfPortfolio = editText.getText().toString();

                    if (nameOfPortfolio.length() == 0)
                        throw new UnsupportedOperationException(
                                getString(R.string.toast_no_portfolio));

                } catch (UnsupportedOperationException e) {

                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, e.toString());
                    e.printStackTrace();

                    return;
                }

                try {
                    mListener = (FragmentListener) getActivity();
                } catch (ClassCastException e) {
                    throw new ClassCastException(getActivity().toString()
                                            + " must implement FragmentListener");
                }
                // TODO: 02.08.17 Попробовать запустить эту ошибку

                mListener.fragmentOnClickOKButton(nameOfPortfolio);

                dismiss();

                break;

            case R.id.btn_df_cancel:
                dismiss();
                break;
        }
    }
}
