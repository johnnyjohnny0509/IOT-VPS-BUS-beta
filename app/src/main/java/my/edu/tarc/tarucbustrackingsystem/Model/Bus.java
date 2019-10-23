package my.edu.tarc.tarucbustrackingsystem.Model;

/**
 * Created by WenYang on 7/9/2015.
 */
public class Bus {
    private String busPlateNumber, busCode, busRegistrationStatus , busStatus;

    public Bus(String busPlateNumber, String busCode, String busRegistrationStatus, String busStatus) {
        this.busPlateNumber = busPlateNumber;
        this.busCode = busCode;
        this.busRegistrationStatus = busRegistrationStatus;
        this.busStatus = busStatus;
    }

    public String getBusPlateNumber() {
        return busPlateNumber;
    }

    public String getBusCode() {
        return busCode;
    }

    public String getBusRegistrationStatus() {
        return busRegistrationStatus;
    }

    public String getBusStatus() {
        return busStatus;
    }
}
