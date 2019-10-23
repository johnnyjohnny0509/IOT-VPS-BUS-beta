package my.edu.tarc.tarucbustrackingsystem.Engine.SqlLiteDatabase;

/**
 * Created by WenYang on 2/3/2016.
 */
public class RouteRecord {
    private String routeId;
    private String routeName;

    public String getRouteId() {
        return routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    @Override
    public String toString(){
        return RouteContract.Route.COLUMN_ROUTEID + ":" + this.getRouteId() +
                "," + RouteContract.Route.COLUMN_ROUTENAME + ":" + this.routeName;
    }
}
