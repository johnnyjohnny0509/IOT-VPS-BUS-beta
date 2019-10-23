package my.edu.tarc.tarucbustrackingsystem.Engine;

import android.content.Context;
import android.content.SharedPreferences;

import my.edu.tarc.tarucbustrackingsystem.Constants.Constants;

/**
 * Created by leewengyang on 10/29/15.
 */

public class SharedPreferencesHelper {


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferencesEditor;

    public SharedPreferencesHelper(Context context){

        sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        preferencesEditor = sharedPreferences.edit();
    }

    public String retrieveStringPreferences(String preferenceName) {
        return sharedPreferences.getString(preferenceName,"");
    }

    public void writeStringPreferences(String preferenceName, String preferenceValue){
        preferencesEditor.putString(preferenceName,preferenceValue);
        preferencesEditor.commit();
    }

    public void writeBooleanPreferences(String preferenceName , boolean preferenceValue){
        preferencesEditor.putBoolean(preferenceName, preferenceValue);
        preferencesEditor.commit();

    }

    public boolean retrieveBooleanPreferences(String preferenceName){
        return sharedPreferences.getBoolean(preferenceName,true);
    }

    public boolean checkExistStringPreferences(String preferenceName){
        return !(retrieveStringPreferences(preferenceName)==null || retrieveStringPreferences(preferenceName).equals(""));
    }



}
