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
import android.widget.Button;
import android.widget.EditText;


/**
 * Created by bolajiabiodun on 05/04/2017.
 */

public class UserDetailsFragment extends Fragment {

    private EditText etPassword, etConfirmPassword, etUserName;
    private TextInputLayout userName, password, confirmPassword;
    private String strPassword, strConfirmPassword, strUserName;
    private CommunicationFrag commFrag;
    private Button next;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_user_details_fragment, container, false);


        userName = (TextInputLayout) view.findViewById(R.id.ti_user_name);
        password = (TextInputLayout) view.findViewById(R.id.ti_password);
        confirmPassword = (TextInputLayout) view.findViewById(R.id.ti_confirm_password);


        etPassword = (EditText) view.findViewById(R.id.et_password);
        etConfirmPassword = (EditText) view.findViewById(R.id.et_confirm_password);
        etUserName = (EditText) view.findViewById(R.id.et_user_name);


        next = (Button) getActivity().findViewById(R.id.signup_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
                v.startAnimation(buttonClick);


                switch (checkFields()) {
                    case 0:
                        commFrag.setPassword(strPassword);
                        commFrag.setUserName(strUserName);
                        changeFragment();
                        break;
                    case 1:
                        etPassword.setError("Passwords do not match");
                        etConfirmPassword.setError("Passwords do not match");
                        break;
                    case 2:
                        etPassword.setError("Passwords should be 6 characters");
                        etConfirmPassword.setError("Passwords should be 6 characters");
                        break;
                    case 3:
                        userName.setError("Enter Valid User Name");
                        break;
                    default:
                        break;
                }
            }
        });
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commFrag = (CommunicationFrag) getActivity();
    }


    private int checkFields() {
        strPassword = etPassword.getText().toString();
        strConfirmPassword = etConfirmPassword.getText().toString();
        strUserName = etUserName.getText().toString();
        int success = 0;

        if (!TextUtils.isEmpty(strUserName)) {
            if (etPassword.length() > 5) {
                if (strPassword.equals(strConfirmPassword)) {
                    success = 0;
                } else {
                    success = 1;
                }
            } else {
                success = 2;
            }
        } else {
            success = 3;
        }
        return success;
    }


    private void changeFragment() {
        PersonalDetailsFragment personalDetailsFragment = new PersonalDetailsFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace((R.id.signup_fragment_container), personalDetailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
