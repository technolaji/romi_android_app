package technolaji.romiglobal.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import technolaji.romiglobal.models.User;


/**
 * Created by bolajiabiodun on 02/10/2016.
 */

public class PreferenceManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "TrimIt WelcomeActivity";
    public static final String USER_KEY = "USER_KEY";
    public static final String BOTTOM_BAR = "BOTTOM_BAR_KEY";
    public static final String PREFS = "prefs";


    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String IS_LOGGED_IN = "IsLoggedIn";

    public PreferenceManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public void setUserLoggedIn(boolean isLoggedIn){
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn);
        editor.commit();
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGGED_IN, false);
    }

    public void storeUser(Context context, User user) {
        String userJson = null;
        if (user != null) {
            userJson = new Gson().toJson(user);
        }
        editor.putString(USER_KEY, userJson);
        editor.apply();
    }

    public User getUser() {
        String userJson = pref.getString(USER_KEY, null);
        User user = null;
        if (userJson != null) {
            user = new Gson().fromJson(userJson, User.class);
        }
        return user;
    }
}
