package my.edu.tarc.tarucbustrackingsystem.Engine.SqlLiteDatabase;

import android.provider.BaseColumns;

/**
 * Created by WenYang on 2/3/2016.
 */
public class RouteContract {
    public RouteContract(){}
    public static abstract class Route implements BaseColumns {
        public static final String TABLE_NAME ="route";
        public static final String COLUMN_ROUTEID ="routeId";
        public static final String COLUMN_ROUTENAME ="routeName";
    }
}
