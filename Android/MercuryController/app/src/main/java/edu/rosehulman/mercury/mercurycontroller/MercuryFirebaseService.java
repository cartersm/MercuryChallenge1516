package edu.rosehulman.mercury.mercurycontroller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.Process;
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

/**
 * Adapted from <a href="https://github.com/Rose-Hulman-ROBO4xx/1314-Mercury/blob/master/GCMADKservice/src/edu/rosehulman/gcmadkservice/GCMADKservice.java">
 * This Gist
 * </a>, which is in turn adapted from edu.rosehulman.me435.AccessoryActivity.
 */
public class MercuryFirebaseService extends Service {

    public static final String EMAIL = "cartersm@rose-hulman.edu";
    public static final String PASSWORD = "RoseHulmanMercury2016";
    public static final String CONNECTION_CHANGED_MSG = "CONNECTION_CHANGED_MSG";
    private static final String MOTOR_FORMAT_STRING = "MOTORS %d %d %b %b";
    private static final String GRIPPER_FORMAT_STRING = "GRIPPER %s %s %s";
    private static final String LED_FORMAT_STRING = "LED %s";
    // "MERC" - non-random so we use the same notification even if the service restarts
    private static final int NOTIF_ID = 0x4d657263;
    private long mBirthTime;
    private Firebase mFirebaseRef;

    /* fields from GCMADKService */
    private static final String ACTION_USB_PERMISSION = "edu.rosehulman.me435.action.USB_PERMISSION";
    private boolean mPermissionRequestPending;
    private UsbManager mUsbManager;
    private UsbAccessory mAccessory;
    private ParcelFileDescriptor mFileDescriptor;
    private FileInputStream mInputStream;
    private FileOutputStream mOutputStream;
    private PendingIntent mPermissionIntent;
    private Handler mHandler = new Handler();
    private final BroadcastReceiver mUsbReceiver = new UsbReceiver();
    private boolean mIsRunning = false;
    private MotorCommandListener mMotorCommandListener;
    private GripperCommandListener mGripperCommandListener;
    private LedCommandListener mLedCommandListener;
    private ConnectivityReceiver mConnectivityReceiver;
    private Camera mCamera;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* TODO: maybe: update the widget with the same data as the notification */

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(MainActivity.TAG, "In onCreate()");

        mBirthTime = System.currentTimeMillis();
        // Firebase stuff
        Firebase.setAndroidContext(getApplicationContext());

        mFirebaseRef = new Firebase("https://mercury-robotics-16.firebaseio.com/");
        if (mFirebaseRef.getAuth() == null) {
            auth();
        } else if (mFirebaseRef.getAuth().getExpires() < mBirthTime / 1000) {
            Log.w(MainActivity.TAG, "Auth expired, reauthenticating...");
            mFirebaseRef.unauth();
            auth();
        } else {
            Log.d(MainActivity.TAG, "Already logged in");
            postNotification("Successfully logged in!");
        }

        mMotorCommandListener = (MotorCommandListener) mFirebaseRef.child("motorCommands")
                .addChildEventListener(new MotorCommandListener());
        mGripperCommandListener = (GripperCommandListener) mFirebaseRef.child("gripperLauncherCommands")
                .addChildEventListener(new GripperCommandListener());
        mLedCommandListener = (LedCommandListener) mFirebaseRef.child("ledCommands")
                .addChildEventListener(new LedCommandListener());

        // Arduino Stuff
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        // Connection watcher
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mConnectivityReceiver = new ConnectivityReceiver();
        registerReceiver(mConnectivityReceiver, intentFilter);

        mCamera = null;
    }

    private void postNotification(String notifString) {
        Context context = getApplicationContext();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        setIcon(builder);
        builder.setContentTitle("Mercury");
        builder.setContentText(notifString);

        Intent notificationIntent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notificationManager.notify(NOTIF_ID, notification);
    }

    private void setIcon(NotificationCompat.Builder builder) {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        builder.setSmallIcon(useWhiteIcon ? R.drawable.ic_notif_silhouette : R.mipmap.ic_launcher);
        if (useWhiteIcon) {
            builder.setColor(getResources().getColor(R.color.mercury_orange));
        }
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
                        postNotification("Error logging in: " + firebaseError.getMessage());
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFirebaseRef.child("motorCommands").removeEventListener(mMotorCommandListener);
        mFirebaseRef.child("gripperLauncherCommands").removeEventListener(mGripperCommandListener);
        mFirebaseRef.child("ledCommands").removeEventListener(mLedCommandListener);

        mFirebaseRef.unauth();
        closeAccessory();
        unregisterReceiver(mUsbReceiver);
        unregisterReceiver(mConnectivityReceiver);
        Log.d(MainActivity.TAG, "Service destroyed");
        postNotification("Service Destroyed");

        // FIXME: Force killing the service to circumvent Firebase not listening
        Process.killProcess(Process.myPid());
    }

    /* AccessoryActivity methods */
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
                    // onCommandReceived(receivedCommand);
                    mHandler.post(new Runnable() {
                        public void run() {
                            onCommandReceived(receivedCommand);
                        }
                    });
                }
            }
        }
    };

    protected void onCommandReceived(final String receivedCommand) {
        Log.d(MainActivity.TAG, "Received command = " + receivedCommand);
        postNotification("Received command: " + receivedCommand);
    }

    protected void sendCommand(final String commandString) {
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
                String msg = "Sending command \"" + commandString + "\"";
                Log.d(MainActivity.TAG, msg);
                postNotification(msg);
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
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            Thread thread = new Thread(null, mRxRunnable, MainActivity.TAG);
            thread.start();
            Log.d(MainActivity.TAG, "accessory opened");
            postNotification("Accessory opened");
        } else {
            Log.w(MainActivity.TAG, "accessory open fail");
            postNotification("Could not open accessory");
        }
    }

    private void closeAccessory() {
        Log.d(MainActivity.TAG, "Close accessory called.");
        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Exception closing accessory", e);
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
            mInputStream = null;
            mOutputStream = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // intent from ConnectivityReceiver
//        Log.d(MainActivity.TAG, "made it here");
        String message = intent.getStringExtra(CONNECTION_CHANGED_MSG);
        if (message != null) {
            Log.d(MainActivity.TAG, "message is \"" + message + "\"");
            sendCommand(message);
            return START_REDELIVER_INTENT;
        }

        // first-time startup
        if (mInputStream != null && mOutputStream != null) {
            return -1;
        }

        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            if (mUsbManager.hasPermission(accessory)) {
                Log.d(MainActivity.TAG, "Permission ready.");
                openAccessory(accessory);
            } else {
                Log.d(MainActivity.TAG, "Requesting permission.");
                postNotification("Requesting accessory permission...");
                synchronized (mUsbReceiver) {
                    if (!mPermissionRequestPending) {
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            Log.d(MainActivity.TAG, "accessory is null.");
            postNotification("Accessory is null");
        }
        return START_REDELIVER_INTENT;
    }

    /* Firebase Listeners */
    private class MotorCommandListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            MotorCommand cmd = dataSnapshot.getValue(MotorCommand.class);
            int distance = cmd.getDistance();
            int angle = cmd.getAngle();
            boolean isSerpentine = cmd.isSerpentine();
            boolean isSeesaw = cmd.isSeesaw();
            long timestamp = cmd.getTimestamp();

            if (timestamp < mBirthTime) {
                // ignore commands sent before we started up
                return;
            }
            String command = String.format(MOTOR_FORMAT_STRING, distance, angle, isSerpentine, isSeesaw);
            Log.d(MainActivity.TAG, "Sending command \"" + command + "\"");
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
            Log.e(MainActivity.TAG, "Fireabse Error: ", firebaseError.toException());
        }
    }

    private class GripperCommandListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            GripperLauncherCommand cmd = dataSnapshot.getValue(GripperLauncherCommand.class);
            boolean launch = cmd.getLaunch();
            String position = cmd.getPosition();
            String location = cmd.getLocation();
            long timestamp = cmd.getTimestamp();

            if (timestamp < mBirthTime) {
                // ignore commands sent before we started up
                return;
            }
            String command = String.format(GRIPPER_FORMAT_STRING, launch, location, position.toUpperCase());
            Log.d(MainActivity.TAG, "Sending command \"" + command + "\"");
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
            Log.e(MainActivity.TAG, "Fireabse Error: ", firebaseError.toException());
        }
    }

    private class LedCommandListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            LedCommand cmd = dataSnapshot.getValue(LedCommand.class);
            String status = cmd.getStatus();
            long timestamp = cmd.getTimestamp();

            if (timestamp < mBirthTime) {
                // ignore commands sent before we started up
                return;
            }
            String command = String.format(LED_FORMAT_STRING, status.toUpperCase());
            String message = "Received Firebase Command \"" + command + "\"";
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
            Log.e(MainActivity.TAG, "Firebase Error: ", firebaseError.toException());
        }
    }

    private class UsbReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = intent
                            .getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessory(accessory);
                    } else {
                        Log.d(MainActivity.TAG, "permission denied for accessory " + accessory);
                    }
                    mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = intent
                        .getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null && accessory.equals(mAccessory)) {
                    closeAccessory();
                }
            }
        }
    }

}
