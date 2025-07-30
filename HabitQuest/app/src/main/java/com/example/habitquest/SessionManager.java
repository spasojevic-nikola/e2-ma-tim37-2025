package com.example.habitquest;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "HabitQuestSession";
    private static final String IS_LOGGED_IN = "isLoggedIn";

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public void saveUserData(String email, String username) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("email", email);
        editor.putString("username", username);
        editor.apply();
    }

    public String getUserEmail() {
        return pref.getString("email", null);
    }

    public String getUsername() {
        return pref.getString("username", null);
    }

}
