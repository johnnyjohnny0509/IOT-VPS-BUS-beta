package my.edu.tarc.tarucbustrackingsystem.Engine;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import my.edu.tarc.tarucbustrackingsystem.Constants.Constants;
import my.edu.tarc.tarucbustrackingsystem.Engine.AsyncTask.VolleyRequestWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leewengyang on 1/15/16.
 * All Right Reserved
 */

public class RouteEngine {

    private SharedPreferencesHelper preferencesHelper;
    public void getRoutesList(Context context,final RouteEngine_CompletionHandler routeEngine_CompletionHandler){

        preferencesHelper = new SharedPreferencesHelper(context);
        String branchURL = preferencesHelper.retrieveStringPreferences(Constants.PREFERENCE_STATE);
        String completeURL = Constants.URLServer + branchURL + Constants.URL_ROUTE;
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constants.POST_PARAMETER_ACTION, Constants.POST_PARAMETER_ACTION_RETRIEVE);

        Log.e("hello123", completeURL);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        VolleyRequestWrapper jsObjRequest = new VolleyRequestWrapper(Request.Method.POST, completeURL, params,new Response.Listener<JSONObject>(){

            @Override
            public void onResponse(JSONObject responseJsonObject) {
                ArrayList<HashMap<String, String>> routesArrayList = new ArrayList<>();
                try {
                    int success = responseJsonObject.getInt(Constants.JSON_TAG_SUCCESS);
                    if (success == 1) {
                        JSONArray routesJsonArray = responseJsonObject.getJSONArray(Constants.JSON_TAG_ROUTESARRAY);

                        for (int i = 0; i < routesJsonArray.length(); i++) {
                            JSONObject c = routesJsonArray.getJSONObject(i);

                            String id = c.getString(Constants.JSON_TAG_ROUTEID);
                            String name = c.getString(Constants.JSON_TAG_ROUTENAME);

                            HashMap<String, String> map = new HashMap<>();

                            map.put(Constants.JSON_TAG_ROUTEID, id);
                            map.put(Constants.JSON_TAG_ROUTENAME, name);

                            routesArrayList.add(map);
                        }
                        Log.d("VOL Array ", routesArrayList.toString());
                        routeEngine_CompletionHandler.onCompleted(routesArrayList);
                    }
                    else if (success == 0)
                    {
                        routeEngine_CompletionHandler.onNothingReturn("There is no route available for the selected state. Please contact administrative department");
                    }
                    else {
                        //TODO : onFailedExecution
                        routeEngine_CompletionHandler.onFailed("Failed retrieve through Internet Connection");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        requestQueue.add(jsObjRequest);

    }



    public interface RouteEngine_CompletionHandler{
        void onCompleted(ArrayList<HashMap<String, String>> routesArrayList);
        void onFailed(String errorMessage);
        void onNothingReturn (String errorMessage);
    }

}
