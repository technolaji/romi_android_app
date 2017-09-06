package technolaji.romiglobal;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;


/**
 * Created by bolajiabiodun on 05/04/2017.
 */

public class PersonalDetailsFragment extends Fragment {
    private TextInputLayout firstName, lastName;
    private EditText etFirstName, etLastName, etEmail, etPhoneNumber;
    private TextInputLayout email, phoneNumber;
    private String strFirstName, strLastName, strEmail, strPhoneNumber;
    private CommunicationFrag commFrag;
    private Button next;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_personal_details, container, false);
        commFrag = (CommunicationFrag) getActivity();
        firstName = (TextInputLayout) view.findViewById(R.id.ti_first_name);
        lastName = (TextInputLayout) view.findViewById(R.id.ti_last_name);
        etFirstName = (EditText) view.findViewById(R.id.et_first_name);
        etLastName = (EditText) view.findViewById(R.id.et_last_name);

        email = (TextInputLayout) view.findViewById(R.id.ti_email);
        phoneNumber = (TextInputLayout) view.findViewById(R.id.ti_phone_number);
        etEmail = (EditText) view.findViewById(R.id.et_email);
        etPhoneNumber = (EditText) view.findViewById(R.id.et_phone_number);

        firstName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        lastName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });


        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });


        phoneNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });





        next = (Button) getActivity().findViewById(R.id.signup_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
                v.startAnimation(buttonClick);
                switch (checkFields()) {
                    case 0:
                        commFrag.setNames(strFirstName, strLastName);
                        commFrag.setEmail(strEmail);
                        commFrag.setPhoneNumber(strPhoneNumber);
                        changeFragment();
                        break;
                    case 1:
                        email.setError("Enter valid email address");
                        break;
                    case 2:
                        phoneNumber.setError("Enter Phone Number");
                        break;
                    case 3:
                        lastName.setError("Enter Last Name");
                        break;
                    case 4:
                        firstName.setError("Enter First Name");
                    default:
                        break;
                }
            }
        });
        return view;
    }


    private int checkFields() {
        strFirstName = etFirstName.getText().toString();
        strLastName = etLastName.getText().toString();
        strPhoneNumber = etPhoneNumber.getText().toString();
        strEmail = etEmail.getText().toString().toLowerCase(Locale.ENGLISH);
        int errorCode = 0;

        if (!TextUtils.isEmpty(strFirstName)) {
            if (!TextUtils.isEmpty(strLastName)) {
                if (!TextUtils.isEmpty(strEmail)) {
                    if (!TextUtils.isEmpty(strPhoneNumber)) {
                        if (android.util.Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
                            errorCode = 0;
                        }
                    } else {
                        errorCode = 1;
                    }
                } else {
                    errorCode = 2;
                }
            } else {
                errorCode = 3;
            }
        } else {
            errorCode = 4;
        }
        return errorCode;
    }

    private void changeFragment() {
        ProfileDetailsFragment profileDetailsFragment = new ProfileDetailsFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace((R.id.signup_fragment_container), profileDetailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
