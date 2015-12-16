package edu.rosehulman.mercury.mercurycontroller;

import android.app.Activity;
import android.content.Intent;
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
    }
}
