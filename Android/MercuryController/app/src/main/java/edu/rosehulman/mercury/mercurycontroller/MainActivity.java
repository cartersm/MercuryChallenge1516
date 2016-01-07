package edu.rosehulman.mercury.mercurycontroller;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class MainActivity extends Activity {
    public static final String TAG = "MERCC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        Intent thisIntent = getIntent();
        Intent passIntent = new Intent(this, MercuryFirebaseService.class);
        passIntent.putExtras(thisIntent);

        startService(passIntent);

        Intent hangoutsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/hangouts/_/event/cesf9en94g0bi69039k5bc9h890"));
        startActivity(hangoutsIntent);
    }
}
