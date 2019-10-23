package my.edu.tarc.tarucbustrackingsystem.Engine.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import my.edu.tarc.tarucbustrackingsystem.Engine.ShellCommandHelper;

/**
 * Created by leewengyang on 1/22/16.
 * All Right Reserved
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        Log.d("AlarmReceiver","Shutting dowm...");
        ShellCommandHelper.executor(ShellCommandHelper.ShellCommand.SHUTDOWN);

    }
}
