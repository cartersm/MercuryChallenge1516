package edu.rosehulman.mercury.mercurycontroller;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    public static final String TAG = "MERCC";
//    public static final String HANGOUT_URL = "https://plus.google.com/hangouts/_/event/cesf9en94g0bi69039k5bc9h890";
    public static final String SKYPE_URI = "skype:live:firstmate8445?call&video=true";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent thisIntent = getIntent();
        Intent passIntent = new Intent(this, MercuryFirebaseService.class);
        passIntent.putExtras(thisIntent);

        startService(passIntent);

        startSkypeCall();

        findViewById(R.id.video_call_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startSkypeCall();
                    }
                });
    }

    private void startSkypeCall() {
        Intent skypeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SKYPE_URI));
        skypeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(skypeIntent);
    }
}
