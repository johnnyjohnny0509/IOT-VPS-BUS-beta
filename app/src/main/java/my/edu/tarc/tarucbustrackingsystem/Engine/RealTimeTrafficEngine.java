package my.edu.tarc.tarucbustrackingsystem.Engine;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import my.edu.tarc.tarucbustrackingsystem.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import my.edu.tarc.tarucbustrackingsystem.Constants.Constants;
import my.edu.tarc.tarucbustrackingsystem.Controller.MainActivity;
import my.edu.tarc.tarucbustrackingsystem.Engine.AsyncTask.VolleyRequestWrapper;
import my.edu.tarc.tarucbustrackingsystem.Model.RealTimeTraffic;
import my.edu.tarc.tarucbustrackingsystem.Model.Route;

/**
 * Created by leewengyang on 1/15/16.
 * All Right Reserved
 */
public class RealTimeTrafficEngine {
    public Location loc;
    ArrayList<Route> routeArrayList = new ArrayList<>();
    AlertDialog closealert;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    boolean tripDetection = false;
    int lastPoint = 0;
    int previousOrderNumber = -2; // -2 is never use as order number, at here asume is zero
    long previousUpdateTime;
    //int count = 0;
    //int midPoint = routeArrayList.size() / 2;
    //boolean fallBack = false;
    private RealTimeTraffic realTimeTraffic;
    private int latestPoint = 0;
    private Context mContext;
    private RequestQueue requestQueue;
    //MQTT
    private MqttAndroidClient client;
    private String stoppedRouteID;

    private int iniIndex = 0;
    private boolean outtrip = true;
    private int initializeGPS = 0;

    private SharedPreferencesHelper preferencesHelper;
    String branchURL;


    public RealTimeTrafficEngine(Context context, String selectedRouteId, String selectedBusPlateNumber) {
        initialize(context, selectedRouteId, selectedBusPlateNumber);
        preferencesHelper = new SharedPreferencesHelper(context);
        branchURL = preferencesHelper.retrieveStringPreferences(Constants.PREFERENCE_STATE).toUpperCase();
        Log.e("Branch",branchURL.toUpperCase());
    }

    private void initialize(Context context, String selectedRouteId, String selectedBusPlateNumber) {

        realTimeTraffic = new RealTimeTraffic(Integer.parseInt(selectedRouteId), "ON", selectedBusPlateNumber);
        Log.e("BUS", selectedRouteId + " " + selectedBusPlateNumber);
        this.mContext = context;
        requestQueue = Volley.newRequestQueue(mContext);
        firstConnectMqtt();
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                //reconnectMqtt();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        //SharedPreferencesHelper preferencesHelper = new SharedPreferencesHelper(mContext);
    }

    private void updateRealTimeTraffic(RealTimeTraffic realTimeTraffic) {
        Calendar c = Calendar.getInstance();
        String completeURL = Constants.URLServer + branchURL.toLowerCase() + Constants.URL_REALTIMETRAFFICCONTROLLER;
        Map<String, String> params = new HashMap<String, String>();
        //  params.put(Constants.POST_PARAMETER_ACTION, Constants.POST_PARAMETER_ACTION_UPDATE);
        params.put(Constants.POST_PARAMETER_LATITUDE, "" + realTimeTraffic.getLat());
        params.put(Constants.POST_PARAMETER_LONGITUDE, "" + realTimeTraffic.getLon());
        params.put(Constants.POST_PARAMETER_STATUS, realTimeTraffic.getStatus());
        params.put(Constants.POST_PARAMETER_ROUTEID, "" + realTimeTraffic.getRouteId());
        params.put(Constants.POST_PARAMETER_BUSPLATENUMBER, realTimeTraffic.getBusPlateNumber());
        params.put(Constants.POST_PARAMETER_date1, formatter.format(c.getTime()));
        params.put(Constants.POST_PARAMETER_orderNum, "" + realTimeTraffic.getOrderNumber());
        params.put(Constants.POST_PARAMETER_bus_speed, "" + realTimeTraffic.getSpeed());

        if (!realTimeTraffic.getBusPlateNumber().isEmpty()) {
            mqttPublishBus(realTimeTraffic);

            VolleyRequestWrapper jsObjRequest = new VolleyRequestWrapper(
                    Request.Method.POST,
                    completeURL,
                    params,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject responseJsonObject) {
                            //Log.d("Response", responseJsonObject.toString());
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    //Log.d("Error response", volleyError.getMessage());
                }
            });

            requestQueue.add(jsObjRequest);
        }
    }

    private void mqttPublishBus(RealTimeTraffic rtTraffic) {
        int rtID = rtTraffic.getRouteId();
        if (rtID > 0 && rtID != 12 && MainActivity.btnStartTrack.getText().equals(mContext.getString(R.string.button_stop))) { //Route ID is not for Outstation
            Calendar c = Calendar.getInstance();
            String payload = Constants.MQTT_START_CMD + Constants.MQTT_SEPARATOR
                    + rtTraffic.getLat() + Constants.MQTT_SEPARATOR
                    + rtTraffic.getLon() + Constants.MQTT_SEPARATOR
                    + rtTraffic.getStatus() + Constants.MQTT_SEPARATOR
                    + rtTraffic.getRouteId() + Constants.MQTT_SEPARATOR
                    + rtTraffic.getBusPlateNumber() + Constants.MQTT_SEPARATOR
                    + formatter.format(c.getTime()) + Constants.MQTT_SEPARATOR
                    + rtTraffic.getOrderNumber() + Constants.MQTT_SEPARATOR
                    + rtTraffic.getSpeed();

            byte[] encodedPayload;
            String topic = Constants.MQTT_TOPIC_PREFIX + branchURL +realTimeTraffic.getRouteId() + "/"
                    + realTimeTraffic.getBusPlateNumber();
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void firstConnectMqtt() {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(mContext, Constants.MQTT_HOST, clientId);
        reconnectMqtt();
    }

    private void reconnectMqtt() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(Constants.MQTT_USERNAME);
        options.setPassword(Constants.MQTT_PASSWORD.toCharArray());
        options.setKeepAliveInterval(5000);

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("MQTT connection", "Connect Successful");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("MQTT connection", "Connect Failed");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void updateTextView(String text) {
        TextView txtView = (TextView) ((Activity) mContext).findViewById(R.id.tvCurrentOrderID);
        txtView.setText(text);
    }

    private void updateSpeed(String text){
        TextView txtViewSpeed = (TextView) ((Activity) mContext).findViewById(R.id.textViewSpeed);
        txtViewSpeed.setText(text);
    }

    public void setRouteData(Location location) {
        this.loc = location;
        if (realTimeTraffic.getRouteId() <= 0) {
            if(previousOrderNumber != 0) {
                realTimeTraffic.setLat(location.getLatitude());
                realTimeTraffic.setLon(location.getLongitude());
                realTimeTraffic.setOrderNumber(0);
                realTimeTraffic.setSpeed(location.getSpeed());
                updateRealTimeTraffic(realTimeTraffic);
                previousOrderNumber = realTimeTraffic.getOrderNumber();
            }
        } else {
            if (routeArrayList.isEmpty())
                getRouteData(Constants.URLServer + branchURL.toLowerCase() + Constants.URL_SELECTSTATIONS);
            else
                transmitRealTimeTraffic();
        }

    }

    public void transmitRealTimeTraffic() {
        Calendar calendar = Calendar.getInstance();
        if (tripDetection == false) {
            latestPoint = findNearest(loc, routeArrayList);
        } else if (tripDetection == true) {
            int currentPoint = findNearest(loc, routeArrayList);
            if (previousOrderNumber != currentPoint) {
                latestPoint = findNearest(loc, routeArrayList);
                previousOrderNumber = latestPoint;
                Log.e("index:", latestPoint + " Outtrip:" + outtrip);
                if (latestPoint == -1) {
                    realTimeTraffic.setOrderNumber(latestPoint);
                } else {
                    realTimeTraffic.setOrderNumber(setboundary());
                }
                updateTextView("Current:" + realTimeTraffic.getOrderNumber() + "");
                realTimeTraffic.setLon(loc.getLongitude());
                realTimeTraffic.setLat(loc.getLatitude());
                realTimeTraffic.setSpeed(loc.getSpeed());
                updateSpeed("Speed:" + realTimeTraffic.getSpeed() * 3.6 + " KM/H");
                updateRealTimeTraffic(realTimeTraffic);
                previousUpdateTime = calendar.getTimeInMillis();
            }else{
                if(calendar.getTimeInMillis()-previousUpdateTime>=6000){
                    latestPoint = findNearest(loc, routeArrayList);
                    previousOrderNumber = latestPoint;
                    Log.e("index:", latestPoint + " Outtrip:" + outtrip);
                    if (latestPoint == -1) {
                        realTimeTraffic.setOrderNumber(latestPoint);
                    } else {
                        realTimeTraffic.setOrderNumber(setboundary());
                    }
                    updateTextView("Current:" + realTimeTraffic.getOrderNumber() + "");
                    realTimeTraffic.setLon(loc.getLongitude());
                    realTimeTraffic.setLat(loc.getLatitude());
                    realTimeTraffic.setSpeed(loc.getSpeed());
                    updateSpeed("Speed:" + realTimeTraffic.getSpeed() * 3.6 + " KM/H");
                    updateRealTimeTraffic(realTimeTraffic);
                    previousUpdateTime = calendar.getTimeInMillis();
                }else{
                    realTimeTraffic.setLon(loc.getLongitude());
                    realTimeTraffic.setLat(loc.getLatitude());
                    realTimeTraffic.setSpeed(loc.getSpeed());
                    updateSpeed("Speed:" + realTimeTraffic.getSpeed() * 3.6 + " KM/H");
                }
            }
        }

    }

    //Identify boundary of a route
    public int setboundary() {
        int ordernum;
        ordernum = routeArrayList.get(latestPoint).getSequence();
        for (int i = latestPoint; i < routeArrayList.size(); i++) {
            Route r = routeArrayList.get(i);
            if (r.getIsStation() == 1) {
                int a = r.getSequence();
                int mina = a - 2;   //Identify a station that is within 4 points away
                int maxa = a + 2;
                if (ordernum >= mina && ordernum <= maxa) {
                    ordernum = a;
                    break;
                }
            }
        }
        return ordernum;
    }

    public void endTransmitRealTimeTraffic() {
        realTimeTraffic.setLat(0);
        realTimeTraffic.setLon(0);
        realTimeTraffic.setStatus("OFF");
        realTimeTraffic.setRouteId(0);
        realTimeTraffic.setOrderNumber(0);
        realTimeTraffic.setSpeed(0);
        latestPoint = 0;
        tripDetection = false;
        lastPoint = 0;
        outtrip = true;
        updateRealTimeTraffic(realTimeTraffic);
        previousOrderNumber = -2;
    }

    private int findNearest(Location current, ArrayList<Route> routeList) {
        int tempIndex = 0;

        int i = 0;
        if (latestPoint <= -1)
            latestPoint = 0;
        if (latestPoint >= routeList.size() - 10)
            latestPoint = 0;
        int maxInt = 0;
        if (initializeGPS > 10) {//initialize GPS for 10 times
            if (tripDetection == false) {
                maxInt = routeList.size() / 2;
                i = findDistance(maxInt, current, routeList);
                if (i <= 10) // if less than 10 mean at college
                {
                    Log.e("maxInx", maxInt + " index " + tripDetection + " current index" + i + "latest point" + latestPoint);

                    outtrip = true;
                    tripDetection = true;
                } else if (i >= 10) {
                    i = findDistance(maxInt, current, routeList);
                    Log.e("maxInx", maxInt + " index " + tripDetection + " current index" + i + "latest point" + latestPoint);
                    if (i >= (routeList.size() / 2) - 2) {
                        outtrip = false;

                    } else {
                        tempIndex = i;

                        if (latestPoint != 0) {

                            if (tempIndex > latestPoint) {
                                outtrip = true;
                                tripDetection = true;
                                Log.e("maxInx", "outtrip" + outtrip);
                            } else if (tempIndex < latestPoint) {
                                outtrip = false;
                                tripDetection = true;
                                Log.e("maxInx", "outtrip" + outtrip);
                            } else if (tempIndex == latestPoint) {
                                Log.e("Not moving", "Not moving");
                                // tripDetection = 0;
                            }
                        }

                    }
                }
            } else {
                if (outtrip == true) {
                    maxInt = routeList.size() / 2;
                } else if (outtrip == false) {
                    latestPoint = routeList.size() / 2;
                    maxInt = routeList.size();
                    Log.e("Outtrip:", "Yes" + " " + latestPoint);
                }

                i = findDistance(maxInt, current, routeList);


                if (i >= (routeList.size() / 2) - 2)
                    outtrip = false;

                if (i >= (routeList.size() - 30)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Do you want to stop it?")
                            .setTitle("You have arrived college")
                            .setCancelable(false)
                            .setPositiveButton("No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {


                                        }
                                    }
                            )
                            .setNegativeButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            MainActivity.btnStartTrack.performClick();
                                            MainActivity.gpsTracker_service.startTracking("-1");

                                        }
                                    }
                            );
                    if (closealert == null) {
                        closealert = builder.create();
                        closealert.show();
                    }

                }

            }
        } else {

            Log.e("maxint123", initializeGPS + "");

            initializeGPS++;
        }
        return i;

    }


    public int findDistance(int maxInt, Location current, ArrayList<Route> routeList) {
        int i = -1;
        double shortest = 5;
        double distance;
        float result[] = new float[1];
        int latestIndex = 0;
        if (tripDetection == true)
            iniIndex = latestPoint;
        for (int index = iniIndex; index < maxInt; index++) {

            Route locationSearch = routeList.get(index);
            android.location.Location.distanceBetween(current.getLatitude(), current.getLongitude(), locationSearch.getLat(), locationSearch.getLon(), result);
            distance = result[0] / 1000;

            if (5 >= distance) {
                if (shortest >= distance) {
                    shortest = distance;
                    i = index;
                }
            }
        }
        return i;
    }

    private double findDistanceNextfive(Location current, int index, ArrayList<Route> routeList) {
        Route locationSearch = routeList.get(index);
        float result[] = new float[1];
        android.location.Location.distanceBetween(current.getLatitude(), current.getLongitude(), locationSearch.getLat(), locationSearch.getLon(), result);
        return result[0 / 1000];
    }

    public void getRouteData(String url) {

        //mPostCommentResponse.requestStarted();
        com.android.volley.RequestQueue queue = Volley.newRequestQueue(mContext);

        //Send data
        try {
            StringRequest postRequest = new StringRequest(
                    Request.Method.POST,
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray locations = jsonObject.getJSONArray("route");
                                routeArrayList.clear();
                                Route ro;
                                for (int i = 0; i < locations.length(); i++) {
                                    JSONObject c = locations.getJSONObject(i);
                                    int routeID = c.getInt("route_id");
                                    String routeName = c.getString("routename");
                                    String loc_name = c.getString("Loc_name");
                                    double lat = c.getDouble("lat");
                                    double lon = c.getDouble("lon");
                                    int sequence = c.getInt("order_num");
                                    int isStation = c.getInt("is_station");
                                    int station_num = c.getInt("station_num");

                                    ro = new Route(routeID, routeName, loc_name, lat, lon, sequence, isStation, station_num);

                                    routeArrayList.add(ro);
                                }

                                Log.e("ro", "" + routeArrayList.size());
                                transmitRealTimeTraffic();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("error",error.getMessage());
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", "" + realTimeTraffic.getRouteId());
                    //  params.put(SyncStateContract.Constants.POST_PARAMETER_ACTION, Constants.POST_PARAMETER_ACTION_LOGIN );
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }

                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded";
                }

            };
            queue.add(postRequest);
            queue.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}