/*
Create by Nguyen Nguyen
 */
package app.tiengiangportal.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import app.tiengiangportal.R;

public class SharedPref {
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences prefs;

    public static final String GCM_PREF_KEY = "app.thecity.data.GCM_PREF_KEY";
    public static final String SERVER_FLAG_KEY = "app.thecity.data.SERVER_FLAG_KEY";
    public static final String THEME_COLOR_KEY = "app.thecity.data.THEME_COLOR_KEY";

    // need refresh
    public static final String REFRESH_PLACES = "app.thecity.data.REFRESH_PLACES";

    public SharedPref(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences("MAIN_PREF", Context.MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setGCMRegId(String gcmRegId){
        sharedPreferences.edit().putString(GCM_PREF_KEY, gcmRegId).commit();
    }

    public String getGCMRegId(){
        return sharedPreferences.getString(GCM_PREF_KEY, null);
    }

    public boolean isGcmRegIdEmpty(){
        return TextUtils.isEmpty(getGCMRegId());
    }

    public void setRegisteredOnServer(boolean registered){
        sharedPreferences.edit().putBoolean(SERVER_FLAG_KEY, registered).commit();
    }

    public boolean isRegisteredOnServer(){
        return sharedPreferences.getBoolean(SERVER_FLAG_KEY, false);
    }

    public boolean isNeedRegisterGcm(){
        return ( isGcmRegIdEmpty() || !isRegisteredOnServer() );
    }

    /**
     * For notifications flag
     */
    public boolean getNotification(){
        return prefs.getBoolean(context.getString(R.string.pref_key_notif), true);
    }

    public String getRingtone(){
        return prefs.getString(context.getString(R.string.pref_key_ringtone), "content://settings/system/notification_sound");
    }

    public boolean getVibration(){
        return prefs.getBoolean(context.getString(R.string.pref_key_vibrate), true);
    }

    /**
     * Refresh user data
     * When phone receive GCM notification this flag will be enable.
     * so when user open the app all data will be refresh
     */
    public boolean isRefreshPlaces(){
        return sharedPreferences.getBoolean(REFRESH_PLACES, false);
    }

    public void setRefreshPlaces(boolean need_refresh){
        sharedPreferences.edit().putBoolean(REFRESH_PLACES, need_refresh).apply();
    }


    /**
     * For theme color
     */
    public void setThemeColor(String color){
        sharedPreferences.edit().putString(THEME_COLOR_KEY, color).commit();
    }

    public String getThemeColor(){
        return sharedPreferences.getString(THEME_COLOR_KEY, "");
    }

    public int getThemeColorInt(){
        if(getThemeColor().equals("")){
           return context.getResources().getColor(R.color.colorPrimary);
        }
        return Color.parseColor(getThemeColor());
    }
}
