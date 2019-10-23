package my.edu.tarc.tarucbustrackingsystem.Engine;

/**
 * Created by leewengyang on 1/19/16.
 * All Right Reserved
 */
public class ShellCommandHelper {

    public enum ShellCommand{
        ENABLE_DATACONNECTION("svc data enable"),
        ENABLE_GPS("settings put secure location_providers_allowed gps,network"),
        SHUTDOWN("reboot -p");
        //HIDESTATUSBAR("settings put secure user_setup_complete 0");

        private final String command;
        private ShellCommand(String string) {
            command = string;

        }
        public boolean equalsName(String otherName) {
            return (otherName == null) ? false : command.equals(otherName);
        }

        public String toString() {
            return this.command;
        }
    }

    public static void executor(ShellCommand shellCommand){
        try {
            Process proc = Runtime.getRuntime()
                    .exec(new String[]{ "su", "-c", shellCommand.toString() });

            proc.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
