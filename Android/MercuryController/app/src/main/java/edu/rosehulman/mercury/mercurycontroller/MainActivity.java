package edu.rosehulman.mercury.mercurycontroller;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.rosehulman.me435.AccessoryActivity;

public class MainActivity extends AccessoryActivity {

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
            auth();
        } else if (mFirebaseRef.getAuth().getExpires() < mBirthTime / 1000) {
            Log.w("MERCC", "Auth expired, reauthenticating...");
            mFirebaseRef.unauth();
            auth();
        } else {
            Toast.makeText(MainActivity.this, "Already logged in", Toast.LENGTH_SHORT).show();
            Log.d("MERCC", "Already logged in");
        }
        mFirebaseRef.child("motorCommands").addChildEventListener(new MotorCommandListener());
        mFirebaseRef.child("gripperLauncherCommands").addChildEventListener(new GripperCommandListener());
        mFirebaseRef.child("ledCommands").addChildEventListener(new LedCommandListener());
    }

    private void auth() {
        mFirebaseRef.authWithPassword(EMAIL, PASSWORD,
                new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        Toast.makeText(MainActivity.this, "login successful", Toast.LENGTH_SHORT).show();
                        Log.d("MERCC", "login succeeded");
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        Toast.makeText(MainActivity.this, firebaseError.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("MERCC", "login failed: " + firebaseError.getMessage());
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFirebaseRef.unauth();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseRef.unauth();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mFirebaseRef.getAuth() == null) {
            auth();
        } else {
            mFirebaseRef.unauth();
            auth();
        }
    }

    private class MotorCommandListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            MotorCommand cmd = dataSnapshot.getValue(MotorCommand.class);
            int distance = cmd.getDistance();
            int angle = cmd.getAngle();
            long timestamp = cmd.getTimestamp();

            ((TextView) findViewById(R.id.motor_distance_text)).setText("" + distance);
            ((TextView) findViewById(R.id.motor_angle_text)).setText("" + angle);
            ((TextView) findViewById(R.id.motor_timestamp_text)).setText("" + new SimpleDateFormat().format(new Date(timestamp)));

            if (timestamp < mBirthTime) {
                // ignore commands sent before we started up
                Log.d("MERCC", "Ignoring old command");
                return;
            }
//            String command = String.format(LED_FORMAT_STRING, distance, angle);
//            Toast.makeText(MainActivity.this, "Sending command \"" + command + "\"", Toast.LENGTH_SHORT).show();
//            Log.d("MERCC", "Sending command \"" + command + "\"");
//            sendCommand(command);
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
            Log.e("MERCC", firebaseError.getMessage());
        }
    }

    private class GripperCommandListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            GripperLauncherCommand cmd = dataSnapshot.getValue(GripperLauncherCommand.class);
            int angle = cmd.getAngle();
            String position = cmd.getPosition();
            long timestamp = cmd.getTimestamp();

            ((TextView) findViewById(R.id.gripper_angle_text)).setText("" + angle);
            ((TextView) findViewById(R.id.gripper_position_text)).setText(position);
            ((TextView) findViewById(R.id.gripper_timestamp_text)).setText("" + new SimpleDateFormat().format(new Date(timestamp)));

            if (timestamp < mBirthTime) {
                // ignore commands sent before we started up
                Log.d("MERCC", "Ignoring old command");
                return;
            }
//            String command = String.format(LED_FORMAT_STRING, angle, position.toUpperCase());
//            Toast.makeText(MainActivity.this, "Sending command \"" + command + "\"", Toast.LENGTH_SHORT).show();
//            Log.d("MERCC", "Sending command \"" + command + "\"");
//            sendCommand(command);
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
            Log.e("MERCC", firebaseError.getMessage());
        }
    }

    private class LedCommandListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            LedCommand cmd = dataSnapshot.getValue(LedCommand.class);
            int ledNumber = cmd.getLedNumber();
            String status = cmd.getStatus();
            long timestamp = cmd.getTimestamp();

            ((TextView) findViewById(R.id.led_number_text)).setText("" + ledNumber);
            ((TextView) findViewById(R.id.led_status_text)).setText(status);
            ((TextView) findViewById(R.id.led_timestamp_text)).setText("" + new SimpleDateFormat().format(new Date(timestamp)));

            if (timestamp < mBirthTime) {
                // ignore commands sent before we started up
                Log.d("MERCC", "Ignoring old command");
                return;
            }
            String command = String.format(LED_FORMAT_STRING, ledNumber, status.toUpperCase());
            Toast.makeText(MainActivity.this, "Sending command \"" + command + "\"", Toast.LENGTH_SHORT).show();
            Log.d("MERCC", "Sending command \"" + command + "\"");
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
            Log.e("MERCC", firebaseError.getMessage());
        }
    }


}
