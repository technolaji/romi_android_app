package technolaji.romiglobal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.introtitle1), getResources().getString(R.string.introDescription1), R.mipmap.ic_launcher, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.introtitle2), getResources().getString(R.string.introDescription2), R.drawable.chat_icon, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.introtitle3), getResources().getString(R.string.introDescription3), R.drawable.attach_romi_icon, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.introtitle4), getResources().getString(R.string.introDescription4), R.drawable.globe_icon, getResources().getColor(R.color.colorPrimary)));

    }

    private void addFragments() {

        // SHOW or HIDE the statusbar
        showStatusBar(true);

        // Hide Skip/Done button
        showSkipButton(false);
        showDoneButton(false);

        // Animations -- use only one of the below. Using both could cause errors.
        setFadeAnimation(); // OR

        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());


    }

    @Override
    public void onDonePressed() {
        super.onDonePressed();
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        SharedPreferences.Editor e = getPrefs.edit();
        e.putBoolean("firstStart", false);
        e.apply();

        Intent i = new Intent(IntroActivity.this, WelcomeActivity.class);
        startActivity(i);
        finish();

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        SharedPreferences.Editor e = getPrefs.edit();
        e.putBoolean("firstStart", false);
        e.apply();

        Intent i = new Intent(IntroActivity.this, WelcomeActivity.class);
        startActivity(i);
        finish();


    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        SharedPreferences.Editor e = getPrefs.edit();
        e.putBoolean("firstStart", false);
        e.apply();

        Intent i = new Intent(IntroActivity.this, WelcomeActivity.class);
        startActivity(i);
        finish();


    }

    @Override
    public void onSkipPressed() {
        super.onSkipPressed();

        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        SharedPreferences.Editor e = getPrefs.edit();
        e.putBoolean("firstStart", false);
        e.apply();

        Intent i = new Intent(IntroActivity.this, WelcomeActivity.class);
        startActivity(i);
        finish();

    }
}
