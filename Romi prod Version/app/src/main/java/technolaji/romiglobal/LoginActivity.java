package technolaji.romiglobal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;


import com.applozic.audiovideo.activity.AudioCallActivityV2;
import com.applozic.audiovideo.activity.VideoActivity;
import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.api.translation.language.Language;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import technolaji.romiglobal.utils.PreferenceManager;


public class LoginActivity extends AppCompatActivity {

    private UserLoginTask mAuthTask = null;
    private EditText etPassword, etUserName;
    private TextInputLayout userName, password, language;
    private String strPassword, strUserName, strLanguage;
    private AutoCompleteTextView acLanguage;
    private Button login;
    private View mProgressView;
    private View loginForm;
    private PreferenceManager preferenceManager;
    private Map<String, Language> languageMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgressView = findViewById(R.id.login_progress);
        loginForm = findViewById(R.id.login_fragment_container);

        userName = (TextInputLayout) findViewById(R.id.ti_login_username);
        password = (TextInputLayout) findViewById(R.id.ti_login_password);
        language = (TextInputLayout) findViewById(R.id.ti_login_language);


        etPassword = (EditText) findViewById(R.id.et_login_Password);
        etUserName = (EditText) findViewById(R.id.et_login_username);
        acLanguage = (AutoCompleteTextView) findViewById(R.id.ac_login_language);

        ArrayAdapter<String> languageAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.languages_array));
        acLanguage.setAdapter(languageAdapter);
        acLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                acLanguage.showDropDown();
            }
        });


        login = (Button) findViewById(R.id.login_next);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
                v.startAnimation(buttonClick);

                switch (checkFields()) {
                    case 0:
                        login();
                        break;
                    case 1:
                        password.setError("Please add password");
                        break;
                    case 2:
                        userName.setError("Please add username");
                        break;
                    case 3:
                        language.setError("PLease add langauge from the list");
                    case 4:
                        language.setError("Please add language");
                    default:
                        break;
                }
            }
        });
    }

    private int checkFields() {
        strUserName = etUserName.getText().toString();
        strPassword = etPassword.getText().toString();
        strLanguage = acLanguage.getText().toString();
        String languages[] = getResources().getStringArray(R.array.languages_array);
        Set<String> language = new HashSet<String>(Arrays.asList(languages));


        languageMap = new TreeMap<String, Language>(String.CASE_INSENSITIVE_ORDER);
        for (Language language1 : Language.values()) {
            languageMap.put(language1.toString(), Language.valueOf(language1.toString()));
        }


        int success = 0;

        if (!TextUtils.isEmpty(strLanguage)) {
            if (language.contains(strLanguage)) {
                if (!TextUtils.isEmpty(strUserName)) {
                    if (!TextUtils.isEmpty(strPassword)) {
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
        } else {
            success = 4;
        }
        return success;
    }

    private void login() {
        attemptLogin(User.AuthenticationType.APPLOZIC);

    }


    public void attemptLogin(User.AuthenticationType authenticationType) {
        if (mAuthTask != null) {
            return;
        }

        showProgress(true);

        // callback for login process
        final Activity activity = LoginActivity.this;
        UserLoginTask.TaskListener listener = new UserLoginTask.TaskListener() {

            @Override
            public void onSuccess(RegistrationResponse registrationResponse, final Context context) {
                mAuthTask = null;
                showProgress(false);

                Log.e("LOG IN SUCCESSFUL", "LOGG SUCCESSFUL");

                ApplozicClient.getInstance(context).hideChatListOnNotification();
                //Basic settings...

                ApplozicClient.getInstance(context).setContextBasedChat(true).setHandleDial(true).setIPCallEnabled(true);

                Map<ApplozicSetting.RequestCode, String> activityCallbacks = new HashMap<ApplozicSetting.RequestCode, String>();
                activityCallbacks.put(ApplozicSetting.RequestCode.USER_LOOUT, WelcomeActivity.class.getName());

                activityCallbacks.put(ApplozicSetting.RequestCode.AUDIO_CALL, AudioCallActivityV2.class.getName());
                activityCallbacks.put(ApplozicSetting.RequestCode.VIDEO_CALL, VideoActivity.class.getName());

                ApplozicSetting.getInstance(context).setActivityCallbacks(activityCallbacks);
                PushNotificationTask.TaskListener pushNotificationTaskListener = new PushNotificationTask.TaskListener() {
                    @Override
                    public void onSuccess(RegistrationResponse registrationResponse) {
                        Log.i("Push Notification:", "SUCESS");
                    }

                    @Override
                    public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                        Log.i("Push Notification:", "FAILURE");
                    }
                };
                PushNotificationTask pushNotificationTask = new PushNotificationTask(Applozic.getInstance(context).getDeviceRegistrationId(), pushNotificationTaskListener, context);
                pushNotificationTask.execute((Void) null);

                //starting main MainActivity
                Intent mainActivity = new Intent(context, HomeActivity.class);
                startActivity(mainActivity);
                finish();
            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                mAuthTask = null;
                showProgress(false);

                Log.e("LOG IN FAIL", "LOGG FAIL");

                AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                alertDialog.setTitle(getString(R.string.text_alert));
                alertDialog.setMessage(exception.toString());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                if (!isFinishing()) {
                    alertDialog.show();
                }
            }
        };

        User applozicUser = new User();
        preferenceManager = new PreferenceManager(this);

//      TODO SET USERNAME FOR RETURNING USERS

        User user = new User();
        user.setUserId(strUserName);
        user.setPassword(strPassword);
        user.setLanguage(languageMap.get(strLanguage));

        applozicUser.setUserId(user.getUserId());
//        applozicUser.setEmail(user.getEmail());
        applozicUser.setPassword(user.getPassword());
        applozicUser.setLanguage(user.getLanguage());
//        applozicUser.setDisplayName(user.getFirstName() + user.getLastName());
//        applozicUser.setContactNumber(user.getPhoneNumber());
        applozicUser.setAuthenticationTypeId(authenticationType.getValue());

        List<String> featureList = new ArrayList<>();
        featureList.add(User.Features.IP_AUDIO_CALL.getValue());// FOR AUDIO
        featureList.add(User.Features.IP_VIDEO_CALL.getValue());// FOR VIDEO
        applozicUser.setFeatures(featureList);
        mAuthTask = new UserLoginTask(applozicUser, listener, this);
        mAuthTask.execute((Void) null);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            loginForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}









