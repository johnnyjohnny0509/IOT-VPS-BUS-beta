package my.edu.tarc.tarucbustrackingsystem.Model;

/**
 * Created by CheeYew on 05/30/2016.
 */
public class RealTimeTraffic {
    private int routeId;

    private String busPlateNumber;
    private String status;
    private double lat;
    private double lon;
    private int orderNumber;
    private String trafDateTime;
    private double speed;

    public RealTimeTraffic(int routeId, String busPlateNumber, double lat, double lon, int orderNumber, String trafDateTime, String status, double speed) {
        this.routeId = routeId;
        this.busPlateNumber = busPlateNumber;
        this.orderNumber = orderNumber;
        this.trafDateTime = trafDateTime;
        this.status = status;
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
    }

    public RealTimeTraffic(int routeid, String status, String busPlateNumber) {
        this.busPlateNumber = busPlateNumber;
        this.routeId = routeid;
        this.status = status;

    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTrafDateTime() {
        return trafDateTime;
    }

    public void setTrafDateTime(String trafDateTime) {
        this.trafDateTime = trafDateTime;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public String getBusPlateNumber() {
        return busPlateNumber;
    }

    public void setBusPlateNumber(String busPlateNumber) {
        this.busPlateNumber = busPlateNumber;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Override
    public String toString() {
        return "RealTimeTraffic{" +
                "routeId=" + routeId +
                ", busPlateNumber='" + busPlateNumber + '\'' +
                ", status='" + status + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", orderNumber=" + orderNumber +
                ", trafDateTime='" + trafDateTime + '\'' +
                ", speed=" + speed +
                '}';
    }
}
