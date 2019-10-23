package my.edu.tarc.tarucbustrackingsystem.Engine.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import my.edu.tarc.tarucbustrackingsystem.Constants.Constants;
import my.edu.tarc.tarucbustrackingsystem.Controller.MainActivity;
import my.edu.tarc.tarucbustrackingsystem.Engine.RealTimeTrafficEngine;
import my.edu.tarc.tarucbustrackingsystem.Engine.SharedPreferencesHelper;
import my.edu.tarc.tarucbustrackingsystem.Engine.ShellCommandHelper;

/**
 * Created by leewengyang on 1/12/16.
 * All Right Reserved
 */
public class GPSTracker_Service extends Service implements LocationListener {

    private Context mContext;

    private LocationManager mlocationManager;

    private boolean isTracking ;

    private RealTimeTrafficEngine realTimeTrafficEngine;

    private SharedPreferencesHelper sharedPreferencesHelper;

    private Location previousLocation;

    public GPSTracker_Service(Context mContext){
        this.mContext = mContext;
        sharedPreferencesHelper = new SharedPreferencesHelper(mContext);

        isTracking = false;

        mlocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

        checkLocationServiceStatus();
        checkNetworkConnectionStatus();

        mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        realTimeTrafficEngine = new RealTimeTrafficEngine(mContext, "-1", sharedPreferencesHelper.retrieveStringPreferences(Constants.PREFERENCE_BUS));
    }

    public void startTracking(String routeId){
        isTracking = true;
        realTimeTrafficEngine = new RealTimeTrafficEngine(mContext,routeId,sharedPreferencesHelper.retrieveStringPreferences(Constants.PREFERENCE_BUS));
    }

    public void stopTracking(){
        isTracking = false;
        realTimeTrafficEngine.endTransmitRealTimeTraffic();
        realTimeTrafficEngine = null;
    }

    public boolean checkTrackingStatus(){
        return isTracking ;
    }
    //region LifeCycle
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //endregion

    //Region Location Listener
    @Override
    public void onLocationChanged(Location location) {
        if (previousLocation == null) {
            previousLocation = location;
        } else {
            //Turn off tracking if a vehicle is not moving for 5 minutes
            if (location.distanceTo(previousLocation) > 5) {
                MainActivity.resetAutoStopTimer();
                previousLocation = location;
            }
        }

        if (isTracking) {
            if(location.hasSpeed())
            realTimeTrafficEngine.setRouteData(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    //endregion

    public boolean checkLocationServiceStatus(){
        boolean isGpsOn = false;

        if(!mlocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            ShellCommandHelper.executor(ShellCommandHelper.ShellCommand.ENABLE_GPS);
        }else{
            isGpsOn = true;
        }

        return isGpsOn;
    }


    public boolean checkNetworkConnectionStatus() {
        boolean isConnectedMobileData = false;

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] allNetworkInfos = cm.getAllNetworkInfo();

        for (NetworkInfo networkInFo : allNetworkInfos) {
            if (networkInFo.getType() == ConnectivityManager.TYPE_MOBILE)
                if (networkInFo.isConnected()){
                    isConnectedMobileData = true;
                }else{
                    ShellCommandHelper.executor(ShellCommandHelper.ShellCommand.ENABLE_DATACONNECTION);
                }

        }
        return isConnectedMobileData;
    }


}
