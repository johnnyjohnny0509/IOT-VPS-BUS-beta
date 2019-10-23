package my.edu.tarc.tarucbustrackingsystem.Engine.SqlLiteDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by WenYang on 2/3/2016.
 */
public class RouteSqlHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "route.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + RouteContract.Route.TABLE_NAME + "(" +
                    RouteContract.Route.COLUMN_ROUTEID + " TEXT," +
                    RouteContract.Route.COLUMN_ROUTENAME + " TEXT)";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RouteContract.Route.TABLE_NAME;

    public RouteSqlHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade
        // policy is to simply to discard the data and start over

        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }

}
