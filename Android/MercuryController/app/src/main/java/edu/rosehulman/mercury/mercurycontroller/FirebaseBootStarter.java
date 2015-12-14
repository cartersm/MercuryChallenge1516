package edu.rosehulman.mercury.mercurycontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Borrowed from <a href="https://gist.github.com/vikrum/6170193#file-startfirebaseatboot-java">This Gist</a>
 */
public class FirebaseBootStarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, MercuryFirebaseService.class));
    }
}