package edu.rosehulman.mercury.mercurycontroller;

import android.os.Bundle;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import edu.rosehulman.me435.RobotActivity;

public class MainActivity extends RobotActivity {

    public static final String EMAIL = "cartersm@rose-hulman.edu";
    public static final String PASSWORD = "RoseHulmanMercury2016";
    private static final String LED_FORMAT_STRING = "LED %d %s";
    private Firebase mFirebaseRef;
    private long mBirthTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);

        mBirthTime = System.currentTimeMillis();

        mFirebaseRef = new Firebase("https://mercury-robotics-16.firebaseio.com/");
        if (mFirebaseRef.getAuth() == null) {
            mFirebaseRef.authWithPassword(EMAIL, PASSWORD,
                    new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            Toast.makeText(MainActivity.this, "login successful", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {
                            Toast.makeText(MainActivity.this, firebaseError.getMessage(), Toast.LENGTH_LONG).show();

                        }
                    });
        } else {
            mFirebaseRef.child("ledCommands").addChildEventListener(new LedCommandListener());
        }
    }

    private class LedCommandListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            LedCommand cmd = dataSnapshot.getValue(LedCommand.class);
            int ledNumber = cmd.getLedNumber();
            String status = cmd.getStatus();
            long timestamp = cmd.getTimestamp();

            if (timestamp < mBirthTime) {
                // ignore commands sent before we started up
                return;
            }
            String command = String.format(LED_FORMAT_STRING, ledNumber, status.toUpperCase());
            Toast.makeText(MainActivity.this, "Sending command \"" + command + "\"", Toast.LENGTH_SHORT).show();
            sendCommand(command);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            // unused
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            // unused
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            // unused
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            // unused
        }
    }
}
