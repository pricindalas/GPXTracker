package bazulis.gpxtracker.trk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.UUID;

import bazulis.gpxtracker.trk.util.Config;
import bazulis.gpxtracker.trk.util.GPSListener;
import bazulis.gpxtracker.trk.util.GPXTrackFile;

public class TrackService extends Service {

    private static final int serviceID = 19891020;
    private Notification.Builder nbuilder;
    private BroadcastReceiver receiver;
    private BluetoothGatt mBluetoothGatt;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///Trackerio duomenys, skirti interfeisui
    private double distance;
    private double speed;
    private double avspeed;
    private double pace;
    private double avpace;
    private long duration;
    private int heartrate;
    private double avheartrateSum;
    private double avheartrate;
    private double avheartrateCount;
    private boolean updateNotif;
    public boolean isHrmEnabled;
    ///
    public int gpsStatus = 1;
    // GPS status int : 0 - gps isjungtas
    //                  1 - ieskoma gps signalo
    //                  2 - gps veikia, prisijungta
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private NotificationManager nmanager;
    private LocationListener locationListener;
    private LocationManager locationManager;
    public GPXTrackFile gpx;

    public TrackService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        makeForeground();

        if (isHrmEnabled && !getHRmonitorMAC().equals("none")) {
            BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter mBluetoothAdapter = manager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                isHrmEnabled = false;
                Toast.makeText(getApplicationContext(), getString(R.string.service_enable_bt), Toast.LENGTH_LONG).show();
            } else {
                BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        if (newState == BluetoothGatt.STATE_CONNECTED) gatt.discoverServices();
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        BluetoothGattService service = gatt.getService(UUID.fromString(Config.UUID_HR_SERVICE));
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(Config.UUID_HR_CHARACTERISTIC));
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(Config.UUID_HR_DESCRIPTOR));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                        gatt.setCharacteristicNotification(characteristic, true);
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                        int flag = characteristic.getProperties();
                        int format;
                        if ((flag & 0x01) != 0) {
                            format = BluetoothGattCharacteristic.FORMAT_UINT16;
                            //System.out.println("UINT16 formatas");
                        } else {
                            format = BluetoothGattCharacteristic.FORMAT_UINT8;
                            //System.out.println("UINT8 formatas");
                        }
                        heartrate = characteristic.getIntValue(format, 1);
                        avheartrateSum += heartrate;
                        avheartrateCount++;
                        avheartrate = avheartrateSum / avheartrateCount;
                    }
                };
                BluetoothDevice targetDevice = mBluetoothAdapter.getRemoteDevice(getHRmonitorMAC());
                mBluetoothGatt = targetDevice.connectGatt(getApplicationContext(), true, mGattCallback);
            }
        }

        String filename = intent.getStringExtra("filename");
        if (filename != null) {
            Toast.makeText(getApplicationContext(), intent.getStringExtra("filename"), Toast.LENGTH_SHORT).show();
            gpx = new GPXTrackFile(intent.getStringExtra("filename"), true);
            gpx.analyzeGPX();
            locationListener = new GPSListener(this, getMinimumSpeed(), gpx.duration, gpx.distance, gpx.startTime, gpx.lastTime);
        } else {
            gpx = new GPXTrackFile();
            locationListener = new GPSListener(this, getMinimumSpeed());
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, getRefreshInterval(), 0, locationListener);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, getString(R.string.toast_servicestarted), Toast.LENGTH_SHORT).show();
        nmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        nbuilder = new Notification.Builder(this);
        updateNotif = isNotifBarEnabled();
        isHrmEnabled = getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0).getBoolean(SettingsActivity.SETTINGS_ENABLE_HR_MONITOR, false);

        distance = 0;
        speed = 0;
        avspeed = 0;
        duration = 0;
        avheartrateCount = 0;
        avheartrate = 0;
        avheartrateSum = 0;
        pace = 0;
        avpace = 0;

////////////////////////////////////////////////////////////////////////////////////////////////////
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.TRACK_SERVICE_RECEIVER);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean stop = intent.getBooleanExtra("stop", false);
                if (stop) {
                    stopForeground(true);
                    stopSelf();
                }
                boolean data = intent.getBooleanExtra("getdata", false);
                if (data) {
                    Intent broadcast = new Intent();
                    broadcast.setAction(Config.MAIN_RECEIVER);
                    broadcast.putExtra("duration", duration);
                    broadcast.putExtra("distance", distance);
                    broadcast.putExtra("speed", speed);
                    broadcast.putExtra("avspeed", avspeed);
                    broadcast.putExtra("pace", pace);
                    broadcast.putExtra("avpace", avpace);
                    broadcast.putExtra("gps", gpsStatus);
                    broadcast.putExtra("heartrate", heartrate);
                    broadcast.putExtra("avheartrate", avheartrate);
                    sendBroadcast(broadcast);
                }
            }
        };
        registerReceiver(receiver, filter);
////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    public int getHeartrate() {
        return heartrate;
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locationListener);
        gpx.saveGPX();
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
        stopForeground(true);
        unregisterReceiver(receiver);
        Toast.makeText(this, getString(R.string.toast_trackstopped), Toast.LENGTH_SHORT).show();
    }

    void makeForeground() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        nbuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker(getString(R.string.toast_trackstarted))
                .setContentTitle(getString(R.string.service_running))
                .setContentText(getString(R.string.service_waitinggps))
                .setContentIntent(pendIntent);
        startForeground(serviceID, nbuilder.build());
    }

    public void updateLocation(long duration, double distance, double speed, double avspeed) {
        this.duration = duration;
        this.distance = distance;
        this.speed = speed;
        this.avspeed = avspeed;
        this.pace = 60 / speed * 60000;
        this.avpace = 60 / avspeed * 60000;

        if (updateNotif) {
            nbuilder.setContentText("T: " + duration + "; " + new DecimalFormat("##.# km").format(distance) + "; AVS" + new DecimalFormat("##.# km/h").format(avspeed));
            nmanager.notify(serviceID, nbuilder.build());
        }
    }

    ///SETTINGS///
    int getMinimumSpeed() {
        return getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0).getInt(SettingsActivity.SETTINGS_MINIMUM_SPEED, 2);
    }

    int getRefreshInterval() {
        return getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0).getInt(SettingsActivity.SETTINGS_REFRESH_INTERVAL, 2) * 1000;
    }

    boolean isNotifBarEnabled() {
        return getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0).getBoolean(SettingsActivity.SETTINGS_UPDATE_NOTIFICATION_BAR, true);
    }

    String getHRmonitorMAC() {
        return getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0).getString(SettingsActivity.SETTINGS_HRMONITOR_MAC, "none");
    }
}
