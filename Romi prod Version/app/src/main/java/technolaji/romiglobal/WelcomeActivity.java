package technolaji.romiglobal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;


public class WelcomeActivity extends AppCompatActivity {


    private final int SPLASH_DISPLAY_LENGTH = 1000;
    //    private UserLoginTask mAuthTask = null;
    private Button login, signup;
    //    private LoginButton loginButton;
//    private CallbackManager callbackManager;
    private View loginForm;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        login = (Button) findViewById(R.id.welcome_log_in);
        signup = (Button) findViewById(R.id.welcome_sign_up);
        login.setVisibility(View.GONE);
        signup.setVisibility(View.GONE);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (MobiComUserPreference.getInstance(WelcomeActivity.this).isLoggedIn()) {
                    Intent mainIntent = new Intent(WelcomeActivity.this, HomeActivity.class);
                    WelcomeActivity.this.startActivity(mainIntent);
                    WelcomeActivity.this.finish();
                } else {
                    login.setVisibility(View.VISIBLE);
                    signup.setVisibility(View.VISIBLE);
                }
            }
        }, SPLASH_DISPLAY_LENGTH);


        if (checkFirstTime()) {
            Intent i = new Intent(this, IntroActivity.class);
            startActivity(i);
        }
    }



    public void loginUser(View view) {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void signupUser(View view) {
        Intent i = new Intent(this, SignUpActivity.class);
        startActivity(i);
        finish();
    }

    private boolean checkFirstTime() {
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        //  Create a new boolean and preference and set it to true
        boolean isFirstStart = getPrefs.getBoolean("firstStart", true);
        return isFirstStart;
    }
}
