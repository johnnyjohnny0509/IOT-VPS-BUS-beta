package my.edu.tarc.tarucbustrackingsystem.Controller;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import my.edu.tarc.tarucbustrackingsystem.Constants.Constants;
import my.edu.tarc.tarucbustrackingsystem.Engine.BroadcastReceiver.AlarmReceiver;
import my.edu.tarc.tarucbustrackingsystem.Engine.BusEngine;
import my.edu.tarc.tarucbustrackingsystem.Engine.RouteEngine;
import my.edu.tarc.tarucbustrackingsystem.Engine.Service.GPSTracker_Service;
import my.edu.tarc.tarucbustrackingsystem.Engine.SharedPreferencesHelper;
import my.edu.tarc.tarucbustrackingsystem.Engine.SqlEngine;
import my.edu.tarc.tarucbustrackingsystem.R;


public class MainActivity extends FragmentActivity implements TextToSpeech.OnInitListener {

    private static final String MY_PREFS_NAME = "MyPrefsFile";
    public static GPSTracker_Service gpsTracker_service;
    public static Button btnStartTrack;
    private static CountDownTimer cdtAutoStop;
    public static Context contextOfApplication;
    SqlEngine sqlEngine;
    Handler handler;
    //LifeCycle
    private Context mContext;
    private SharedPreferencesHelper preferencesHelper;
    //private int routeID;
    private LayoutInflater layoutInflater;
    private ListView lvRouteList;
    private BusEngine busEngine;
    private String routeName;
    private TextView tvBusNo, tvRoute, tvID, tvOrder, tvSpeed;
    private AlarmManager alarmManager;
    private TextToSpeech textToSpeech;
    private ProgressDialog progressBar;

    //auto stop trip if not call in 10 min
    public static void resetAutoStopTimer() {
        cdtAutoStop.cancel();
        cdtAutoStop.start();
    }

    public static Context getContextOfApplication(){
        return contextOfApplication;
    }

    //region Life Cycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContext = this;
        busEngine = new BusEngine(mContext);
        layoutInflater = LayoutInflater.from(this);
        preferencesHelper = new SharedPreferencesHelper(mContext);
        contextOfApplication = getApplicationContext();
        setContentView(R.layout.main);

        tvRoute = (TextView) findViewById(R.id.tvRoute);
        tvID = (TextView) findViewById(R.id.tvRouteId);
        tvOrder = (TextView) findViewById(R.id.tvCurrentOrderID);
        tvBusNo = (TextView) findViewById(R.id.tvBusNo);
        tvSpeed = (TextView) findViewById(R.id.textViewSpeed);
        btnStartTrack = (Button) findViewById(R.id.btnStartTrack);
        lvRouteList = (ListView) findViewById(R.id.listViewRoute);

        //Show version code
        showVersion();

        sqlEngine = new SqlEngine(this);

        if (preferencesHelper.retrieveBooleanPreferences(Constants.PREFERENCE_ISFIRSTTIME) || !preferencesHelper.checkExistStringPreferences(Constants.PREFERENCE_STATE)) {

            promptStateSelectionDialog();
//            promptBusSelectionDialog();
            preferencesHelper.writeBooleanPreferences(Constants.PREFERENCE_ISFIRSTTIME, false);

        } else {
            String busNo = preferencesHelper.retrieveStringPreferences(Constants.PREFERENCE_BUS);
            tvBusNo.setText(getString(R.string.number_plate) + busNo);
        }

        if (!preferencesHelper.checkExistStringPreferences(Constants.PREFERENCE_AUTOSTOP)) {
            preferencesHelper.writeStringPreferences(Constants.PREFERENCE_AUTOSTOP, "5");
        }

        setupAlarmManager();
        //Bug :onResume gets called but still it is in animation stage
        //To solve this, add this into onresume.
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        //Start another Activity Here
                    default:
                        break;
                }
                return false;
            }
        });

        int autoStopInMilliSec = Integer.parseInt(preferencesHelper.retrieveStringPreferences(Constants.PREFERENCE_AUTOSTOP)) * 60 * 3000;
        cdtAutoStop = new CountDownTimer(autoStopInMilliSec, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (btnStartTrack.getText().equals(getString(R.string.button_stop))) {
                    btnStartTrack.performClick();
                    Toast.makeText(getApplication(), getString(R.string.auto_stop_message), Toast.LENGTH_LONG).show();
                }
            }
        };

    }

    private void showVersion() {
        try {
            PackageInfo pInfo = null;
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            TextView textViewVersion = (TextView) findViewById(R.id.textViewVersion);
            String version = pInfo.versionName;
            textViewVersion.setText(getString(R.string.version) + " " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showSpeed(){
        try {

        }catch (Exception e){

        }
    }

    @Override
    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onPause();
    }
    //endregion

    @Override
    public void onResume() {
        super.onResume();

        textToSpeech = new TextToSpeech(getApplicationContext(), this);

        handler.sendEmptyMessageDelayed(1, 1000);

        gpsTracker_service = new GPSTracker_Service(mContext);
        gpsTracker_service.startTracking("-1");

        if (gpsTracker_service != null)
            Log.e("GPS", "not null");
    }

    //region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //endregion

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        int index = 0;

        if (btnStartTrack.getText() == getString(R.string.button_stop)) {
            Toast.makeText(getApplication(), getString(R.string.toastText_isTrackingWarning), Toast.LENGTH_LONG).show();
            return true;
        }
        if (id == R.id.action_settings) {
            index = 1;
            promptBusSelectionDialog();
            //promptAuthenticationDialog(index);
            return true;
        } else if(id == R.id.action_state)
        {
            index = 2;
            promptStateSelectionDialog();
        } else if (id == R.id.action_timer) {
            index = 3;
            promptAuthenticationDialog(index);
            return true;
        } else if (id == R.id.action_autostop) {
            promptAutoStopDialog();
        }else if(id == R.id.action_update){
            if(isPortOpen()) {
                loadRoute();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //region Alert Dialog
    public void promptAuthenticationDialog(final int index) {

        AlertDialog.Builder authentication_alertDialog = new AlertDialog.Builder(mContext);

        View promptAuthentication = layoutInflater.inflate(R.layout.prompt_authentication, null);

        final TextView authenticate_password = (TextView) promptAuthentication.findViewById(R.id.editText_authenticatePassword);

        authentication_alertDialog.setTitle(getString(R.string.alertDialog_authentication_title))
                .setPositiveButton(getString(R.string.alertDialog_authentication_positiveButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isAdmin = Constants.ADMIN_PASSWORD.equals(authenticate_password.getText().toString());
                        if (isAdmin) {
                            busEngine.unRegisterBus(tvBusNo.getText().toString());
                            if (index == 1) {
                                promptBusSelectionDialog();
                            } else {
                                promptTimeDialog();
                            }
                        } else {
                            Toast.makeText(getApplication(), getString(R.string.alertDialog_authentication_loginFailedMessage), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.alertDialog_authentication_negativeButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setCancelable(false)
                .setView(promptAuthentication);
        authentication_alertDialog.show();
    }

    public void promptAutoStopDialog() {
        AlertDialog.Builder autostop_alertDialog = new AlertDialog.Builder(mContext);

        View promptAutoStopInterval = layoutInflater.inflate(R.layout.prompt_autostoptrip, null);

        final Spinner input_interval = (Spinner) promptAutoStopInterval.findViewById(R.id.spinner_interval);

        autostop_alertDialog.setTitle(getString(R.string.alertDialog_autostop_title))
                .setPositiveButton(getString(R.string.alertDialog_autostop_positiveButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        preferencesHelper.writeStringPreferences(Constants.PREFERENCE_AUTOSTOP, input_interval.getSelectedItem().toString());
                        reloadActivity();
                    }
                })
                .setCancelable(false)
                .setView(promptAutoStopInterval);
        autostop_alertDialog.show();
    }

    public void promptTimeDialog() {
        AlertDialog.Builder time_alertDialog = new AlertDialog.Builder(mContext);

        View promptShutDownTime = layoutInflater.inflate(R.layout.prompt_shutdowntime, null);

        final Spinner input_hour = (Spinner) promptShutDownTime.findViewById(R.id.spinner_hours);
        final Spinner input_minute = (Spinner) promptShutDownTime.findViewById(R.id.spinner_minute);

        time_alertDialog.setTitle(getString(R.string.alertDialog_time_title))
                .setPositiveButton(getString(R.string.alertDialog_time_positiveButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putInt("hours", Integer.parseInt(input_hour.getSelectedItem().toString()));
                        editor.putInt("minutes", Integer.parseInt(input_minute.getSelectedItem().toString()));
                        editor.commit();

                        setupAlarmManager();
                    }
                })
                .setNegativeButton(getString(R.string.alertDialog_time_negativeButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setCancelable(false)
                .setView(promptShutDownTime);
        time_alertDialog.show();
    }
    //endregion

    public void promptBusSelectionDialog() {

        final View promptsBusSelectionView = layoutInflater.inflate(R.layout.prompt_bussettings, null);

        final Spinner spnBusSelection = (Spinner) promptsBusSelectionView.findViewById(R.id.spinner_bus);

        busEngine.getBusesList(new BusEngine.BusEngine_CompletionHandler() {
            @Override
            public void onCompleted(List<String> busPlateNumberList) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, busPlateNumberList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnBusSelection.setAdapter(adapter);

                AlertDialog.Builder busSelection_alertDialog = new AlertDialog.Builder(mContext);

                if (promptsBusSelectionView.getParent() == null) {
                    busSelection_alertDialog.setView(promptsBusSelectionView);
                }

                busSelection_alertDialog.setTitle(getString(R.string.alertDialog_busSetting_title))
                        .setPositiveButton(getString(R.string.alertDialog_busSetting_positiveButton),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        preferencesHelper.writeStringPreferences(Constants.PREFERENCE_BUS, spnBusSelection.getSelectedItem().toString());
                                        busEngine.registerBus(spnBusSelection.getSelectedItem().toString());
                                        reloadActivity();
                                    }
                                })
                        .setCancelable(false);

                busSelection_alertDialog.show();


            }

            @Override
            public void onNothingReturned(String errorMessage) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Opps, sorry")
                        .setMessage(errorMessage)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
//                                finish();
                                promptStateSelectionDialog();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }

            @Override
            public void onFailed() {
            }
        });
    }

    public void reloadActivity() {
        finish();
        startActivity(getIntent());
    }

    @Override
    public void onBackPressed() {
    }

    public void setupAlarmManager() {

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), Constants.REQUESTCODE_ALARMMANAGER, intent, 0);

        Calendar alarmClock = Calendar.getInstance();

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        int hours = prefs.getInt("hours", 22);
        int minutes = prefs.getInt("minutes", 0);

        alarmClock.setTimeInMillis(System.currentTimeMillis());
        alarmClock.set(Calendar.HOUR_OF_DAY, hours);
        alarmClock.set(Calendar.MINUTE, minutes);
        alarmClock.set(Calendar.SECOND, Constants.ALARMANAGER_SECOND);
        alarmClock.set(Calendar.MILLISECOND, Constants.ALARMANAGER_MILLISECOND);

        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmClock.getTimeInMillis(), pendingIntent);
    }

    private void loadRoute() {
        //If not network is available, do not try to connect the server
        if(!isConnectingToInternet()){
            
            return;
        }

        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        if (isConnectingToInternet()) {
            progressBar.setMessage("Loading Route...");
        } else {
            progressBar.setMessage("No Internet Connection, Retrying...");
        }
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if (!progressBar.isShowing()) {
            progressBar.show();
        }

        Thread mThread = new Thread() {
            @Override
            public void run() {
                while (!isConnectingToInternet()) {
                }

                RouteEngine routeEngine = new RouteEngine();

                routeEngine.getRoutesList(mContext, new RouteEngine.RouteEngine_CompletionHandler()
                {
                    @Override
                    public void onCompleted(final ArrayList<HashMap<String, String>> routesArrayList) {
                        boolean loaded = false;
                        while (!loaded) {

                            setupRoutesList(routesArrayList);
                            if (preferencesHelper.checkExistStringPreferences(Constants.PREFERENCE_BUS)) {
                                mqttClearBusOnAllRoute(routesArrayList);
                            }
                            loaded = true;
                            sqlEngine.removedAllRecords();
                            sqlEngine.insertRoute(routesArrayList);

                            if (progressBar.isShowing()) {
                                progressBar.dismiss();
                            }
                        }
                    }

                    @Override
                    public void onFailed(String errorMessage) {
                        populateRoutesFromDatabase();
                    }

                    @Override
                    public void onNothingReturn(String errorMessage) {
                        if (progressBar.isShowing()) {
                            progressBar.dismiss();
                        }
                        new AlertDialog.Builder(mContext)
                                .setTitle("Opps, sorry")
                                .setMessage(errorMessage)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
//                                finish();
                                        promptStateSelectionDialog();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });

            }
        };
        mThread.start();
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(new Locale("id"));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }

    public void setupRoutesList(final ArrayList<HashMap<String, String>> routesArrayList) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                ListAdapter adapter = new SimpleAdapter(
                        mContext, routesArrayList,
                        R.layout.list_route, new String[]{Constants.JSON_TAG_ROUTEID,
                        Constants.JSON_TAG_ROUTENAME},
                        new int[]{R.id.pid, R.id.name});
                // updating listview
                lvRouteList.setAdapter(adapter);
            }
        });
    }

    public boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    public void populateRoutesFromDatabase() {
        ArrayList<HashMap<String, String>> routesArrayList = sqlEngine.retrieveRoutes();
        if (routesArrayList != null) {
            setupRoutesList(routesArrayList);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        preferencesHelper = new SharedPreferencesHelper(mContext);
        if(isPortOpen()) {

//            if (preferencesHelper.retrieveBooleanPreferences(Constants.PREFERENCE_ISFIRSTTIME) || !preferencesHelper.checkExistStringPreferences(Constants.PREFERENCE_BUS)) {
//
//                promptStateSelectionDialog();
//
//
//            }
            loadRoute();

            populateRoutesFromDatabase();

            //Enable item click
            lvRouteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                            .toString();
                    String routeName = ((TextView) view.findViewById(R.id.name)).getText()
                            .toString();

                    tvRoute.setText(getString(R.string.label_route) + routeName);
                    tvRoute.setTextColor(Color.BLACK);
                    tvID.setText(pid);
                    view.setSelected(true);
                    textToSpeech.speak(routeName, TextToSpeech.QUEUE_FLUSH, null);

                }
            });
        }else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("Service Terganggu");
            dialog.setTitle("Service Terganggu. Sila cuba sebentar lagi.");
            dialog.setPositiveButton("Cuba lagi", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (isPortOpen()) {
                        dialog.dismiss();
                        loadRoute();

                        populateRoutesFromDatabase();

                        //Enable item click
                        lvRouteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {

                                String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                                        .toString();
                                String routeName = ((TextView) view.findViewById(R.id.name)).getText()
                                        .toString();

                                tvRoute.setText(getString(R.string.label_route) + routeName);
                                tvRoute.setTextColor(Color.BLACK);
                                tvID.setText(pid);
                                view.setSelected(true);
                                textToSpeech.speak(routeName, TextToSpeech.QUEUE_FLUSH, null);

                            }
                        });
                    } else {
                        dialog.dismiss();
                        onStart();
                    }
                }
            });
            dialog.show();
        }
    }

    public void promptStateSelectionDialog()
    {
        final View promptsStateSelectionView = layoutInflater.inflate(R.layout.prompt_statesettings, null);
        final Spinner spnStateSelection = (Spinner) promptsStateSelectionView.findViewById(R.id.spinner_state);
        List<String> branch = new ArrayList<String>();


        branch.add("Kuala Lumpur");
        branch.add("Penang");
        branch.add("Perak");
        branch.add("Sabah");
        branch.add("Pahang");
        branch.add("Johor");

        Log.e("hello","YEAP IT IS HERE");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, branch);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnStateSelection.setAdapter(adapter);

        AlertDialog.Builder stateSelection_alertDialog = new AlertDialog.Builder(mContext);

        if (promptsStateSelectionView.getParent() == null) {
            stateSelection_alertDialog.setView(promptsStateSelectionView);
        }

        stateSelection_alertDialog.setTitle(getString(R.string.alertDialog_stateSetting_title))
                .setPositiveButton(getString(R.string.alertDialog_stateSetting_positiveButton),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Log.e("hello", spnStateSelection.getSelectedItemPosition()+"");
                                switch (spnStateSelection.getSelectedItemPosition())
                                {
                                    case 0 :
                                        preferencesHelper.writeStringPreferences(Constants.PREFERENCE_STATE, "kl_main/");
                                        promptBusSelectionDialog();
//                                        reloadActivity();
                                        break;
                                    case 1 :
                                        preferencesHelper.writeStringPreferences(Constants.PREFERENCE_STATE, "pn_branch/");
                                        promptBusSelectionDialog();
                                        break;
                                    case 2 :
                                        preferencesHelper.writeStringPreferences(Constants.PREFERENCE_STATE, "pr_branch/");
                                        promptBusSelectionDialog();
                                        break;
                                    case 3 :
                                        preferencesHelper.writeStringPreferences(Constants.PREFERENCE_STATE, "sb_branch/");
                                        promptBusSelectionDialog();
                                        break;
                                    case 4 :
                                        preferencesHelper.writeStringPreferences(Constants.PREFERENCE_STATE, "ph_branch/");
                                        promptBusSelectionDialog();
                                        break;
                                    case 5 :
                                        preferencesHelper.writeStringPreferences(Constants.PREFERENCE_STATE, "jh_branch/");
                                        promptBusSelectionDialog();
                                        break;

                                }
//                                preferencesHelper.writeStringPreferences(Constants.PREFERENCE_BUS, spnStateSelection.getSe);
                                    Log.e("hello1", preferencesHelper.retrieveStringPreferences(Constants.PREFERENCE_STATE) );
//                                reloadActivity();
                            }
                        })
                .setCancelable(false);

        stateSelection_alertDialog.show();



    }

    public static boolean isPortOpen() {
        //Check Server Status, url or ip address with port number can't be "ping" using ping command
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URI WebSocketUri = new URI(Constants.URL);
            InetAddress serverAddress = InetAddress.getByName(WebSocketUri.getHost());
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(serverAddress, Constants.ServerPort),10*1000);
            socket.close();
            return true;
        }catch(ConnectException ce){
            ce.printStackTrace();
            return false;
        }catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

//    public boolean isInternetAvailable(){
//        //TODO: Checking server status here
//        try {
//            String pingCommand = "ping -i 5 -c 1 " + Constants.ServerUrl;
//            return (Runtime.getRuntime().exec(pingCommand).waitFor() == 0);
//        }catch(InterruptedException e) {
//            Log.i("Warning","No internet Connection");
//            return false;
//        }catch (IOException ioE) {
//            Log.i("Warning","No internet Connection");
//            return false;
//        }
//    }

    public void startStopTrip(View v) {
        if(isPortOpen()) {
            if (btnStartTrack.getText().equals(getString(R.string.button_start))) {

                if (tvRoute.getText().equals(getString(R.string.route_noRouteWarning))) {
                    textToSpeech.speak(getString(R.string.toastText_noRouteWarning), TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(this, getString(R.string.toastText_noRouteWarning), Toast.LENGTH_LONG).show();
                } else {
                    textToSpeech.speak(getString(R.string.button_start), TextToSpeech.QUEUE_FLUSH, null);
                    routeName = tvID.getText().toString();
                    try {
                        if (gpsTracker_service != null) {
                            gpsTracker_service.startTracking(routeName);
                            resetAutoStopTimer();
                            mqttClearBusOnAllRoute(sqlEngine.retrieveRoutes());
                            StartTackingUiSettings();
                        } else {
                            Toast.makeText(this, "Service not available.", Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                textToSpeech.speak(getString(R.string.berhenti_speech), TextToSpeech.QUEUE_FLUSH, null);
                MainActivity.gpsTracker_service.stopTracking();
                mqttStopPublishBus(routeName);
                //MainActivity.gpsTracker_service.startTracking("-1");


                StopTrackingUiSettings();
            }
        }else{
                AlertDialog.Builder msg = new AlertDialog.Builder(this.mContext);
                msg.setTitle("Error");
                msg.setMessage(getString(R.string.no_internet));

                //add a ok button to confirm
                msg.setPositiveButton("OK",null);

                //show the msg in dialog box
                AlertDialog dialog = msg.create();
                dialog.show();
            }

    }


    public void mqttStopPublishBus(String stoppedRouteID) {
        if (!stoppedRouteID.equals("12")) { //ID 12 = Outstation
            final String rtID = stoppedRouteID;
            final MqttAndroidClient client;
            final String branchURL = preferencesHelper.retrieveStringPreferences(Constants.PREFERENCE_STATE).toUpperCase();
            String clientId = MqttClient.generateClientId();
            client = new MqttAndroidClient(mContext, Constants.MQTT_HOST, clientId);
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

                        String payload = Constants.MQTT_STOP_CMD + Constants.MQTT_SEPARATOR
                                + preferencesHelper.retrieveStringPreferences(Constants.PREFERENCE_BUS);
                        byte[] encodedPayload = new byte[0];
                        String topic = Constants.MQTT_TOPIC_PREFIX + branchURL + rtID + "/"
                                + preferencesHelper.retrieveStringPreferences(Constants.PREFERENCE_BUS);
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setRetained(true);
                            message.setQos(1);
                            client.publish(topic, message);
                        } catch (UnsupportedEncodingException | MqttException e) {
                            e.printStackTrace();
                        }
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
    }

    public void mqttClearBusOnAllRoute(ArrayList<HashMap<String, String>> arrRoute) {
        for (HashMap<String, String> route : arrRoute) {
            mqttStopPublishBus(route.get(Constants.JSON_TAG_ROUTEID));
        }
    }

    public void StartTackingUiSettings() {
        btnStartTrack.setText(R.string.button_stop);
        btnStartTrack.setBackgroundColor(Color.RED);
        btnStartTrack.setTextColor(Color.WHITE);
        lvRouteList.setEnabled(false);
    }

    public void StopTrackingUiSettings() {
        btnStartTrack.setText(R.string.button_start);
        btnStartTrack.setBackgroundColor(Color.GREEN);
        btnStartTrack.setTextColor(Color.BLACK);
        //tvRoute.setText(getText(R.string.route_noRouteWarning));
        lvRouteList.setEnabled(true);
        tvOrder.setText("");
        tvSpeed.setText("KM/H");
    }

   /* @Override
    protected void onDestroy() {
        //super.onDestroy();

        if (routeName != null) {
            MainActivity.gpsTracker_service.stopTracking();
            mqttStopPublishBus(routeName);
        }

    }*/
}
