package my.edu.tarc.tarucbustrackingsystem.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import my.edu.tarc.tarucbustrackingsystem.Controller.MainActivity;

/**
 * Created by leewengyang on 1/5/16.
 */

public final class Constants {
    //server directory
    //public static final String URLServer = "http://119.28.98.52/collegebus/";
    //103 is college server, 119 is ezjoy
    //public static final String URLServer = "http://i2hub.tarc.edu.my:8888/collegebus/";
    //public static final String URL = "http://i2hub.tarc.edu.my:8888";


    public static final String URLServer = "http://i2hub.tarc.edu.my:4887/collegebus/";
    public static final String URL = "http://i2hub.tarc.edu.my:4887";
    public static final String SERVER_URL = "http://i2hub.tarc.edu.my:4887/collegebus/";
    public static final int ServerPort = 4887;
//    public static final String URL_BUSCONTROLLER = URLServer + "select_buses.php";
//    public static final String URL_ROUTECONTROLLER = URLServer+"select_routes.php";
    public static final String URL_ROUTE = "select_routes.php";
    public static final String URL_BUS = "select_buses.php";
//    public static final String URL_REALTIMETRAFFICCONTROLLER = URLServer + "insert_RealTraffic.php";
//    public static final String URL_SELECTSTATIONS= URLServer + "select_stationss.php";
    public static final String URL_REALTIMETRAFFICCONTROLLER =  "insert_RealTraffic.php";
    public static final String URL_SELECTSTATIONS= "select_stationss.php";
    //POST input parameter key
    public static final String POST_PARAMETER_ACTION = "action";
    public static final String POST_PARAMETER_ROUTEID = "route_id";
    public static final String POST_PARAMETER_BUSPLATENUMBER = "busPlateNum";
    public static final String POST_PARAMETER_LATITUDE = "lat";
    public static final String POST_PARAMETER_LONGITUDE = "lon";
    public static final String POST_PARAMETER_STATUS = "status";
    public static final String POST_PARAMETER_orderNum = "order_num";
    public static final String POST_PARAMETER_date1 = "date1";
    public static final String POST_PARAMETER_bus_speed = "bus_speed";
    //POST input parameter value
    public static final String POST_PARAMETER_ACTION_UPDATE = "update";
    public static final String POST_PARAMETER_ACTION_RETRIEVE  = "retrieve";
    public static final String POST_PARAMETER_ACTION_REGISTER  = "register";
    public static final String POST_PARAMETER_ACTION_UNREGISTER  = "unregister";
    
    //JSON returned tag
    public static final String JSON_TAG_SUCCESS  = "success";

    public static final String JSON_TAG_BUSESARRAY = "buses";
    public static final String JSON_TAG_BUSPLATENUMBER = "busPlateNum";
    public static final String JSON_TAG_BUSCODE = "buscode";
    public static final String JSON_TAG_BUSREGISTRATIONSTATUS = "busregistrationstatus";
    public static final String JSON_TAG_BUSSTATUS = "busstatus";

    public static final String JSON_TAG_ROUTEID = "route_id";
    public static final String JSON_TAG_ROUTENAME = "name";
    public static final String JSON_TAG_ROUTESARRAY = "route";

    //Preferences
    public static final String PREFERENCES = "TarucBusTrackingSystem_UserPreferences";
    public static final String PREFERENCE_BUS = "Bus";
    public static final String PREFERENCE_STATE = "State";
    public static final String PREFERENCE_AUTOSTOP = "Autostop";
    public static final String PREFERENCE_ISFIRSTTIME = "isFirstTime";

    //Admin
    public static final String ADMIN_PASSWORD = "123";

    //AlarmManager
    public static final int REQUESTCODE_ALARMMANAGER = 3100;
    public static final int ALARMANAGER_SECOND = 0 ;
    public static final int ALARMANAGER_MILLISECOND = 0 ;

    //MQTT
    // 103 is college server, 119 is ezjoy server
    //public static final String MQTT_HOST = "tcp://103.52.192.245:2222";
    public static final String MQTT_HOST = "tcp://i2hub.tarc.edu.my:6788";
    //public static final String IPAddress = "103.52.192.245";
    public static final String MQTT_USERNAME = "ezride";
    public static final String MQTT_PASSWORD = "ezride2018";
    //public static final String MQTT_HOST = "tcp://119.28.98.52:1883";
    //public static final String MQTT_USERNAME = "mrsee";
    //public static final String MQTT_PASSWORD = "123456";
    public static final String MQTT_TOPIC_PREFIX = "MY/TARUC/VPS/TRACKING/";
    public static final String MQTT_SEPARATOR = "*|+";
    public static final String MQTT_START_CMD = "00071001        ";
    public static final String MQTT_STOP_CMD = "00071002        ";
}
