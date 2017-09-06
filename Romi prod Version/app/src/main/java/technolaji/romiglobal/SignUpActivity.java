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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


import com.applozic.audiovideo.activity.AudioCallActivityV2;
import com.applozic.audiovideo.activity.VideoActivity;
import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.api.translation.language.Language;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import technolaji.romiglobal.models.User;
import technolaji.romiglobal.utils.PreferenceManager;


public class SignUpActivity extends AppCompatActivity implements CommunicationFrag {

    private UserLoginTask mAuthTask = null;
    private UserDetailsFragment userDetailsFragment;
    private FragmentManager manager;
    private FragmentTransaction transaction;
    private User user;
    private Button next;
    private View loginForm;
    private View mProgressView;

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        userDetailsFragment = new UserDetailsFragment();
        user = new User();
        mProgressView = findViewById(R.id.signup_progress);
        loginForm = findViewById(R.id.signup_fragment_container);
        next = (Button) findViewById(R.id.signup_next);
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();
        transaction.add(R.id.signup_fragment_container, userDetailsFragment, "personalDetails").commit();
    }


    @Override
    public void setNames(String firstName, String lastName) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
    }

    @Override
    public void setEmail(String email) {
        user.setEmail(email);
    }

    @Override
    public void setUserName(String userName) {
        user.setUserId(userName);
    }


    @Override
    public void setPassword(String password) {
        user.setPassword(password);
    }

    @Override
    public void prepareSignUp() {
        //showProgress(true);
        attemptLogin(User.AuthenticationType.APPLOZIC);
    }

    @Override
    public void setPhoneNumber(String phoneNumber) {
        user.setPhoneNumber(phoneNumber);
    }

    @Override
    public void setCountry(String country) {
        user.setCountry(country);
    }

    @Override
    public void setLanguage(Language language) {
        user.setLanguage(language);
    }

    @Override
    public void setStatus(String status) {
        user.setStatus(status);
    }


    public void attemptLogin(User.AuthenticationType authenticationType) {
        if (mAuthTask != null) {
            return;
        }

        showProgress(true);

        // callback for login process
        final Activity activity = SignUpActivity.this;
        UserLoginTask.TaskListener listener = new UserLoginTask.TaskListener() {

            @Override
            public void onSuccess(RegistrationResponse registrationResponse, final Context context) {
                mAuthTask = null;
                showProgress(false);

                ApplozicClient.getInstance(context).hideChatListOnNotification();

                //Basic settings...

                ApplozicClient.getInstance(context).setContextBasedChat(true).setHandleDial(true).setIPCallEnabled(true);

                Map<ApplozicSetting.RequestCode, String> activityCallbacks = new HashMap<ApplozicSetting.RequestCode, String>();
                activityCallbacks.put(ApplozicSetting.RequestCode.USER_LOOUT, WelcomeActivity.class.getName());
                activityCallbacks.put(ApplozicSetting.RequestCode.AUDIO_CALL, AudioCallActivityV2.class.getName());
                activityCallbacks.put(ApplozicSetting.RequestCode.VIDEO_CALL, VideoActivity.class.getName());

                ApplozicSetting.getInstance(context).setActivityCallbacks(activityCallbacks);



                //Set activity callbacks
                    /*Map<ApplozicSetting.RequestCode, String> activityCallbacks = new HashMap<ApplozicSetting.RequestCode, String>();
                    activityCallbacks.put(ApplozicSetting.RequestCode.MESSAGE_TAP, MainActivity.class.getName());
                    ApplozicSetting.getInstance(context).setActivityCallbacks(activityCallbacks);*/

                //Start GCM registration....

                PushNotificationTask.TaskListener pushNotificationTaskListener = new PushNotificationTask.TaskListener() {
                    @Override
                    public void onSuccess(RegistrationResponse registrationResponse) {

                    }

                    @Override
                    public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

                    }
                };
                PushNotificationTask pushNotificationTask = new PushNotificationTask(Applozic.getInstance(context).getDeviceRegistrationId(), pushNotificationTaskListener, context);
                pushNotificationTask.execute((Void) null);


                //starting main MainActivity
                Intent mainActvity = new Intent(context, HomeActivity.class);
                startActivity(mainActvity);
                finish();
            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                mAuthTask = null;
                showProgress(false);

                AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
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

        com.applozic.mobicomkit.api.account.user.User applozicUser = new com.applozic.mobicomkit.api.account.user.User();

        preferenceManager = new PreferenceManager(this);
        List<String> featureList =  new ArrayList<>();
        featureList.add(User.Features.IP_AUDIO_CALL.getValue());// FOR AUDIO
        featureList.add(User.Features.IP_VIDEO_CALL.getValue());// FOR VIDEO
        applozicUser.setFeatures(featureList);

        applozicUser.setUserId(user.getUserId());
        applozicUser.setEmail(user.getEmail());
        applozicUser.setPassword(user.getPassword());
        applozicUser.setDisplayName(user.getFirstName() + user.getLastName());
        applozicUser.setContactNumber(user.getPhoneNumber());
        applozicUser.setAuthenticationTypeId(authenticationType.getValue());
        applozicUser.setLanguage(user.getLanguage());

        preferenceManager.storeUser(this, user);
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

