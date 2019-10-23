package my.edu.tarc.tarucbustrackingsystem.Engine;

import android.content.Context;

import my.edu.tarc.tarucbustrackingsystem.Constants.Constants;
import my.edu.tarc.tarucbustrackingsystem.Engine.SqlLiteDatabase.RouteDataSource;
import my.edu.tarc.tarucbustrackingsystem.Engine.SqlLiteDatabase.RouteRecord;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by WenYang on 2/3/2016.
 */
public class SqlEngine {

    RouteDataSource routeDataSource;

    public SqlEngine(Context context){
        routeDataSource = new RouteDataSource(context);
    }
    public void insertRoute(ArrayList<HashMap<String, String>> routesArrayList){
        for(HashMap<String, String> route : routesArrayList) {
            RouteRecord routeRecord = new RouteRecord();
            routeRecord.setRouteId(route.get(Constants.JSON_TAG_ROUTEID));
            routeRecord.setRouteName(route.get(Constants.JSON_TAG_ROUTENAME));
            routeDataSource.insertRoute(routeRecord);
        }
    }

    public ArrayList<HashMap<String, String>> retrieveRoutes(){
        ArrayList<HashMap<String, String>> routesArrayList = new ArrayList<>();
        try {
            routeDataSource.open();
            final List<RouteRecord> values = routeDataSource.getAllRoutes();

            for(RouteRecord routeRecord : values){
                HashMap<String, String> map = new HashMap<>();

                map.put(Constants.JSON_TAG_ROUTEID, routeRecord.getRouteId());
                map.put(Constants.JSON_TAG_ROUTENAME, routeRecord.getRouteName());

                routesArrayList.add(map);
            }

        }catch (SQLException ex){
            ex.printStackTrace();
        }

        return routesArrayList;
    }

    public void removedAllRecords(){
        routeDataSource.reset();
    }
}
