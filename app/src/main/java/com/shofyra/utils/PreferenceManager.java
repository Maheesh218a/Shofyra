package com.shofyra.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * PreferenceManager — wraps all SharedPreferences operations.
 * Covers: Data Storage → Shared Preferences
 */
public class PreferenceManager {

    private static final String PREF_NAME = "shofyra_prefs";

    // Keys
    private static final String KEY_USER_NAME       = "user_name";
    private static final String KEY_USER_EMAIL      = "user_email";
    private static final String KEY_USER_PHONE      = "user_phone";
    private static final String KEY_USER_ADDRESS    = "user_address";
    private static final String KEY_USER_CITY       = "user_city";
    private static final String KEY_IS_LOGGED_IN    = "is_logged_in";
    private static final String KEY_FCM_TOKEN       = "fcm_token";
    private static final String KEY_NOTIFICATIONS   = "notifications_enabled";
    private static final String KEY_LAST_CATEGORY   = "last_category";
    private static final String KEY_SEARCH_HISTORY  = "search_history";
    private static final String KEY_ONBOARDED       = "onboarded";
    private static final String KEY_THEME_MODE      = "theme_mode";

    private final SharedPreferences prefs;
    private static PreferenceManager instance;

    private PreferenceManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PreferenceManager getInstance(Context context) {
        if (instance == null) instance = new PreferenceManager(context);
        return instance;
    }

    // ── User Profile ──────────────────────────────
    public void saveUserName(String name)       { put(KEY_USER_NAME, name); }
    public String getUserName()                  { return get(KEY_USER_NAME, "Guest"); }

    public void saveUserEmail(String email)     { put(KEY_USER_EMAIL, email); }
    public String getUserEmail()                 { return get(KEY_USER_EMAIL, ""); }

    public void saveUserPhone(String phone)     { put(KEY_USER_PHONE, phone); }
    public String getUserPhone()                 { return get(KEY_USER_PHONE, ""); }

    public void saveUserAddress(String address) { put(KEY_USER_ADDRESS, address); }
    public String getUserAddress()               { return get(KEY_USER_ADDRESS, ""); }

    public void saveUserCity(String city)       { put(KEY_USER_CITY, city); }
    public String getUserCity()                  { return get(KEY_USER_CITY, ""); }

    // ── Auth ──────────────────────────────────────
    public void setLoggedIn(boolean v)          { put(KEY_IS_LOGGED_IN, v); }
    public boolean isLoggedIn()                  { return getBool(KEY_IS_LOGGED_IN, false); }

    // ── FCM / Notifications ───────────────────────
    public void saveFcmToken(String token)      { put(KEY_FCM_TOKEN, token); }
    public String getFcmToken()                  { return get(KEY_FCM_TOKEN, ""); }

    public void setNotificationsEnabled(boolean v) { put(KEY_NOTIFICATIONS, v); }
    public boolean isNotificationsEnabled()     { return getBool(KEY_NOTIFICATIONS, true); }

    // ── UX State ──────────────────────────────────
    public void saveLastCategory(String cat)    { put(KEY_LAST_CATEGORY, cat); }
    public String getLastCategory()              { return get(KEY_LAST_CATEGORY, ""); }

    public void saveSearchHistory(String json)  { put(KEY_SEARCH_HISTORY, json); }
    public String getSearchHistory()             { return get(KEY_SEARCH_HISTORY, "[]"); }

    public void setOnboarded(boolean v)         { put(KEY_ONBOARDED, v); }
    public boolean isOnboarded()                 { return getBool(KEY_ONBOARDED, false); }

    public void setThemeMode(int mode)          { put(KEY_THEME_MODE, mode); }
    public int getThemeMode()                    { return getInt(KEY_THEME_MODE, -1); } // -1 = follow system

    // ── Clear ─────────────────────────────────────
    public void clearAll() { prefs.edit().clear().apply(); }

    // ── Internal helpers ──────────────────────────
    private void put(String key, String value)  { prefs.edit().putString(key, value).apply(); }
    private void put(String key, boolean value) { prefs.edit().putBoolean(key, value).apply(); }
    private void put(String key, int value)     { prefs.edit().putInt(key, value).apply(); }

    private String  get(String key, String def)  { return prefs.getString(key, def); }
    private boolean getBool(String key, boolean def) { return prefs.getBoolean(key, def); }
    private int     getInt(String key, int def)  { return prefs.getInt(key, def); }
}