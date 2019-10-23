package my.edu.tarc.tarucbustrackingsystem.Model;

/**
 * Created by CheeYew on 05/25/2016.
 */
public class Route {
   int routeID ;
    String routeName ;
    String loc_name;
    double lat ;


    double lon;
    int sequence;
    int isStation;
 int station_num;

    public Route() {

    }

    public int getLoc_id() {
        return loc_id;
    }

    public void setLoc_id(int loc_id) {
        this.loc_id = loc_id;
    }

    int loc_id;
    public Route(int routeID, String routeName, String loc_name, double lat, double lon, int sequence, int isStation, int station_num) {
        this.routeID = routeID;
        this.routeName = routeName;
        this.loc_name = loc_name;
        this.lat = lat;
        this.lon = lon;
        this.isStation = isStation;
        this.sequence = sequence;
      this.station_num=station_num;
    }

    public int getStation_num() {
        return station_num;
    }

    public void setStation_num(int station_num) {
        this.station_num = station_num;
    }

    public int getRouteID() {
        return routeID;
    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getLoc_name() {
        return loc_name;
    }

    public void setLoc_name(String loc_name) {
        this.loc_name = loc_name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getIsStation() {
        return isStation;
    }

    public void setIsStation(int isStation) {
        this.isStation = isStation;
    }


    @Override
    public String toString() {
        return "Route{" +
                "routeID='" + routeID + '\'' +
                ", routeName='" + routeName + '\'' +
                ", loc_name='" + loc_name + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", sequence=" + sequence +
                ", isStation=" + isStation +
                ", station_num=" + station_num +
                '}';
    }
}
