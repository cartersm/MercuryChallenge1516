package edu.rosehulman.mercury.mercurycontroller;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Adapted in part from {@link edu.rosehulman.me435.AccessoryActivity}, and in part from <a
 * href="https://gist.github.com/vikrum/6170193#file-firebasebackgroundservice-java">This Gist</a>
 */
public class MercuryFirebaseService extends Service {

    public static final String EMAIL = "cartersm@rose-hulman.edu";
    public static final String PASSWORD = "RoseHulmanMercury2016";
    private static final String LED_FORMAT_STRING = "LED %d %s";
    private static final int ID = new Random().nextInt();
    private long mBirthTime;
    private Firebase mFirebaseRef;

    /* fields from AccessoryActivity */
    private static final String ACTION_USB_PERMISSION = "edu.rosehulman.me435.action.USB_PERMISSION";
    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;
    private UsbManager mUsbManager;
    private ParcelFileDescriptor mFileDescriptor;
    protected FileInputStream mInputStream;
    protected FileOutputStream mOutputStream;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Log.d(MainActivity.TAG, "got here");
        mBirthTime = System.currentTimeMillis();

        mFirebaseRef = new Firebase("https://mercury-robotics-16.firebaseio.com/");
        if (mFirebaseRef.getAuth() == null) {
            auth();
        } else if (mFirebaseRef.getAuth().getExpires() < mBirthTime / 1000) {
            Log.w(MainActivity.TAG, "Auth expired, reauthenticating...");
            mFirebaseRef.unauth();
            auth();
        } else {
            Log.d(MainActivity.TAG, "Already logged in");
        }
        Log.d(MainActivity.TAG, "got here 2");
        mFirebaseRef.child("motorCommands").addChildEventListener(new MotorCommandListener());
        mFirebaseRef.child("gripperLauncherCommands").addChildEventListener(new GripperCommandListener());
        mFirebaseRef.child("ledCommands").addChildEventListener(new LedCommandListener());

        Log.d(MainActivity.TAG, "got here 3");

        // CONSIDER: may need to merge this onCreate() and onCreate() of AccessoryActivity somehow
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        Log.d(MainActivity.TAG, "got here 4");
    }

    private void postNotification(String notifString) {
        Context context = getApplicationContext();
        int icon = R.mipmap.ic_launcher;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(icon);
        builder.setContentTitle("Mercury");
        builder.setContentText(notifString);

        Intent notificationIntent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID, builder.build());
    }

    private void auth() {
        mFirebaseRef.authWithPassword(EMAIL, PASSWORD,
                new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        Log.d(MainActivity.TAG, "login succeeded");
                        postNotification("login success!");
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        Log.e(MainActivity.TAG, "login failed: " + firebaseError.getMessage());
                    }
                });
    }

    /* AccessoryActivity methods */
    public void sendCommand(String commandString) {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                String command = params[0];
                char[] buffer = new char[command.length() + 1];
                byte[] byteBuffer = new byte[command.length() + 1];
                command.getChars(0, command.length(), buffer, 0);
                buffer[command.length()] = '\n';
                for (int i = 0; i < command.length() + 1; i++) {
                    byteBuffer[i] = (byte) buffer[i];
                }
                if (mOutputStream != null) {
                    try {
                        mOutputStream.write(byteBuffer);
                    } catch (IOException e) {
                        Log.e(MainActivity.TAG, "write failed", e);
                    }
                }
                return null;
            }
        }.execute(commandString);
    }

    private void openAccessory(UsbAccessory accessory) {
        Log.d(MainActivity.TAG, "Open accessory called.");
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            Log.d(MainActivity.TAG, "accessory opened");
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            // CONSIDER: this needs to be uncommented if we use onCommandReceived
//            Thread thread = new Thread(null, mRxRunnable, MainActivity.TAG);
//            thread.start();
        } else {
            Log.d(MainActivity.TAG, "accessory open fail");
        }
    }

    private void closeAccessory() {
        Log.d(MainActivity.TAG, "Close accessory called.");
        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Exception when closing Accessory: ", e);
        } finally {
            mFileDescriptor = null;
            mInputStream = null;
            mOutputStream = null;
        }
    }

    /*
    // CONSIDER: this may not be usable, nor necessary
    // CONSIDER: getApplicationContext()
    // Rx runnable.
    private Runnable mRxRunnable = new Runnable() {
        public void run() {
            int ret = 0;
            byte[] buffer = new byte[255];
            // Loop that runs forever (or until a -1 error state).
            while (ret >= 0) {
                try {
                    ret = mInputStream.read(buffer);
                } catch (IOException e) {
                    break;
                }
                if (ret > 0) {
                    // Convert the bytes into a string.
                    String received = new String(buffer, 0, ret);
                    final String receivedCommand = received.trim();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            onCommandReceived(receivedCommand);
                        }
                    });
                }
            }
        }
    };

    protected void onCommandReceived(final String receivedCommand) {
        //Toast.makeText(this, "Received command = " + receivedCommand, Toast.LENGTH_SHORT).show();
        Log.d(MainActivity.TAG, "Received command = " + receivedCommand);
    }
    */

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (accessory != null) {
                            openAccessory(accessory);
                        }
                    } else {
                        Log.d(MainActivity.TAG, "permission denied for accessory " + accessory);
                    }
                    mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null) {
                    closeAccessory();
                }
            }
        }
    };

    /* Firebase Listeners */
    private class MotorCommandListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            MotorCommand cmd = dataSnapshot.getValue(MotorCommand.class);
            int distance = cmd.getDistance();
            int angle = cmd.getAngle();
            long timestamp = cmd.getTimestamp();

            if (timestamp < mBirthTime) {
                // ignore commands sent before we started up
                Log.d(MainActivity.TAG, "Ignoring old command");
                return;
            }
//            String command = String.format(LED_FORMAT_STRING, distance, angle);
//            Toast.makeText(MainActivity.this, "Sending command \"" + command + "\"", Toast.LENGTH_SHORT).show();
//            Log.d(MainActivity.TAG, "Sending command \"" + command + "\"");
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
            Log.e(MainActivity.TAG, firebaseError.getMessage());
        }
    }

    private class GripperCommandListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            GripperLauncherCommand cmd = dataSnapshot.getValue(GripperLauncherCommand.class);
            int angle = cmd.getAngle();
            String position = cmd.getPosition();
            long timestamp = cmd.getTimestamp();

            if (timestamp < mBirthTime) {
                // ignore commands sent before we started up
                Log.d(MainActivity.TAG, "Ignoring old command");
                return;
            }
//            String command = String.format(LED_FORMAT_STRING, angle, position.toUpperCase());
//            Toast.makeText(MainActivity.this, "Sending command \"" + command + "\"", Toast.LENGTH_SHORT).show();
//            Log.d(MainActivity.TAG, "Sending command \"" + command + "\"");
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
            Log.e(MainActivity.TAG, firebaseError.getMessage());
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
                Log.d(MainActivity.TAG, "Ignoring old command");
                return;
            }
            String command = String.format(LED_FORMAT_STRING, ledNumber, status.toUpperCase());
            String message = "Sending command \"" + command + "\"";
            Log.d(MainActivity.TAG, message);
            postNotification(message);
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
            Log.e(MainActivity.TAG, firebaseError.getMessage());
        }
    }
}
