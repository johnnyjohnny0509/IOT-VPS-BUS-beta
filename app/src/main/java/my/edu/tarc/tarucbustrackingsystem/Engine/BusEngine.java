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
import my.edu.tarc.tarucbustrackingsystem.Model.Bus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by leewengyang on 1/15/16.
 * All Right Reserved
 */
public class BusEngine {

    private Context mContext;
    private RequestQueue requestQueue;
    private SharedPreferencesHelper preferencesHelper;

    public BusEngine(Context context){
        this.mContext = context;
        requestQueue = Volley.newRequestQueue(mContext);
        preferencesHelper = new SharedPreferencesHelper(context);

    }

    public void getBusesList(final BusEngine_CompletionHandler busEngine_CompletionHandler){

        String branchURL = preferencesHelper.retrieveStringPreferences(Constants.PREFERENCE_STATE);
        String completeURL = Constants.URLServer + branchURL + Constants.URL_BUS;
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constants.POST_PARAMETER_ACTION, Constants.POST_PARAMETER_ACTION_RETRIEVE);

        VolleyRequestWrapper jsObjRequest = new VolleyRequestWrapper(Request.Method.POST, completeURL, params,new Response.Listener<JSONObject>(){

            @Override
            public void onResponse(JSONObject responseJsonObject) {
                Log.d("Vol Bus",responseJsonObject.toString());
                ArrayList<Bus> busList = new ArrayList<>();
                List<String> busPlateNoList = new ArrayList<>();
                try {
                    int success = responseJsonObject.getInt(Constants.JSON_TAG_SUCCESS);
                    if (success == 1) {
                        JSONArray busesJsonArray = responseJsonObject.getJSONArray(Constants.JSON_TAG_BUSESARRAY);

                        for (int i = 0; i < busesJsonArray.length(); i++) {
                            JSONObject c = busesJsonArray.getJSONObject(i);

                            String busPlateNumber = c.getString(Constants.JSON_TAG_BUSPLATENUMBER);
                            String busCode = c.getString(Constants.JSON_TAG_BUSCODE);
                            String busRegistrationStatus = c.getString(Constants.JSON_TAG_BUSREGISTRATIONSTATUS);
                            String busStatus = c.getString(Constants.JSON_TAG_BUSSTATUS);

                            Bus bus = new Bus(busPlateNumber,busCode,busRegistrationStatus,busStatus);
                            busList.add(bus);
                        }
                        for (int i = 0; i < busList.size(); i++) {
                            busPlateNoList.add(busList.get(i).getBusPlateNumber());
                        }
                        Log.d("busPlateNoList :", busPlateNoList.toString());

                        if(busPlateNoList.size() > 0){
                            busEngine_CompletionHandler.onCompleted(busPlateNoList);
                        }
                        else{
                            busEngine_CompletionHandler.onNothingReturned("Opps, there are no bus is currently available, please contact administrative department");
                        }

                    }
                    else
                    {
                        busEngine_CompletionHandler.onNothingReturned("Opps, there are no bus is currently available, please contact administrative department");
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

    public void registerBus(String busPlateNumber){

        String branchURL = preferencesHelper.retrieveStringPreferences(Constants.PREFERENCE_STATE);
        String completeURL = Constants.URLServer + branchURL + Constants.URL_BUS;
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constants.POST_PARAMETER_ACTION, Constants.POST_PARAMETER_ACTION_REGISTER);
        params.put(Constants.POST_PARAMETER_BUSPLATENUMBER, busPlateNumber);

        VolleyRequestWrapper jsObjRequest = new VolleyRequestWrapper(Request.Method.POST, completeURL, params,new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject responseJsonObject) {
                Log.d("registerBusRequest:",responseJsonObject.toString());
            }
            },new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        requestQueue.add(jsObjRequest);


    }

    public void unRegisterBus(String busPlateNumber){
        String branchURL = preferencesHelper.retrieveStringPreferences(Constants.PREFERENCE_STATE);
        String completeURL = Constants.URLServer + branchURL + Constants.URL_BUS;
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constants.POST_PARAMETER_ACTION, Constants.POST_PARAMETER_ACTION_UNREGISTER);
        params.put(Constants.POST_PARAMETER_BUSPLATENUMBER, busPlateNumber);


        VolleyRequestWrapper jsObjRequest = new VolleyRequestWrapper(Request.Method.POST, completeURL, params,new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject responseJsonObject) {
                Log.d("registerBusRequest:",responseJsonObject.toString());
            }
        },new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        requestQueue.add(jsObjRequest);

    }


    public interface BusEngine_CompletionHandler{
        void onCompleted(List<String> busPlateNumberList);
        void onNothingReturned(String errorMessage);
        void onFailed();
    }
}
