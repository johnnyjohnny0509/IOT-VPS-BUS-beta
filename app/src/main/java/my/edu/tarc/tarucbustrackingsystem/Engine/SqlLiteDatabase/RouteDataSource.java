package my.edu.tarc.tarucbustrackingsystem.Engine.SqlLiteDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WenYang on 2/3/2016.
 */
public class RouteDataSource {
    private SQLiteDatabase database;
    private RouteSqlHelper dbHelper;
    private String[] allColumn = {
            RouteContract.Route.COLUMN_ROUTEID,
            RouteContract.Route.COLUMN_ROUTENAME};

    public RouteDataSource(Context context){
        dbHelper = new RouteSqlHelper(context);
    }

    public void open() throws SQLException{
        database = dbHelper.getWritableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public void insertRoute(RouteRecord userRecord){
        ContentValues values = new ContentValues();
        values.put(RouteContract.Route.COLUMN_ROUTEID, userRecord.getRouteId());
        values.put(RouteContract.Route.COLUMN_ROUTENAME, userRecord.getRouteName());
        database = dbHelper.getWritableDatabase();
        database.insert(RouteContract.Route.TABLE_NAME, null, values);
        database.close();
    }

    public List<RouteRecord> getAllRoutes(){
        List<RouteRecord> records = new ArrayList<RouteRecord>();
        Cursor cursor = database.query(RouteContract.Route.TABLE_NAME, allColumn , null,
                null, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            RouteRecord routeRecord = new RouteRecord();
            routeRecord.setRouteId(cursor.getString(0));
            routeRecord.setRouteName(cursor.getString(1));
            records.add(routeRecord);
            cursor.moveToNext();
        }
        cursor.close();
        return records;
    }

    public void reset(){
        database = dbHelper.getWritableDatabase ();
        dbHelper.onUpgrade(database,1,1);
        database.close ();
    }
}
