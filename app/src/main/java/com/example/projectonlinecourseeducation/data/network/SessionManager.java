package com.example.projectonlinecourseeducation.data.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.projectonlinecourseeducation.core.model.user.User;
import com.google.gson.Gson;

/**
 * SessionManager - Manages user session, token, and current user data
 * Uses SharedPreferences for persistent storage
 */
public class SessionManager {

    private static final String PREF_NAME = "OnlineCourseSession";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER = "current_user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    /**
     * Save login session
     */
    public void saveSession(String token, User user) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER, gson.toJson(user));
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Get JWT token
     */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        String userJson = prefs.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    /**
     * Update current user (after profile update)
     */
    public void updateCurrentUser(User user) {
        editor.putString(KEY_USER, gson.toJson(user));
        editor.apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Clear session (logout)
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    /**
     * Update token only
     */
    public void updateToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }
}