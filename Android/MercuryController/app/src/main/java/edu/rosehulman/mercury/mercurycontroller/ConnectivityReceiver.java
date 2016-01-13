package edu.rosehulman.mercury.mercurycontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectivityReceiver extends BroadcastReceiver {

    public static final String CONNECTED = "CONNECTED";
    public static final String DISCONNECTED = "DISCONNECTED";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            Intent connectionChangedIntent = new Intent(context, MercuryFirebaseService.class);
            String message;
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if (isConnected) {
                message = CONNECTED;
            } else {
                message = DISCONNECTED;
            }
            connectionChangedIntent.putExtra(MercuryFirebaseService.CONNECTION_CHANGED_MSG, message);
            Log.d(MainActivity.TAG, "Running startService() with message \"" + message + "\"");
            context.startService(connectionChangedIntent);
        }
    }
}
