package bazulis.gpxtracker.trk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.widget.Toast;

import java.text.DecimalFormat;

import bazulis.gpxtracker.trk.util.BRActions;
import bazulis.gpxtracker.trk.util.GPSListener;
import bazulis.gpxtracker.trk.util.GPXFile;

public class TrackService extends Service {

    private static final int serviceID = 19891020;
    private Notification.Builder nbuilder;
    private BroadcastReceiver receiver;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///Trackerio duomenys, skirti interfeisui
    private double distance;
    private double speed;
    private double avspeed;
    private long duration;

    ///
    private boolean updateNotif;
    public int gpsStatus = 0;
    // GPS status int : 0 - gps isjungtas
    //                  1 - ieskoma gps signalo
    //                  2 - gps veikia, prisijungta
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private NotificationManager nmanager;
    private LocationListener locationListener;
    private LocationManager locationManager;
    public GPXFile gpx;

    public TrackService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        makeForeground();
        String filename = intent.getStringExtra("filename");
        if (filename != null) {
            Toast.makeText(getApplicationContext(), intent.getStringExtra("filename"), Toast.LENGTH_SHORT).show();
            gpx = new GPXFile(intent.getStringExtra("filename"), true);
            gpx.analyzeGPX();
            locationListener = new GPSListener(this, getMinimumSpeed(), gpx.duration, gpx.distance, gpx.startTime, gpx.lastTime);
        } else {
            gpx = new GPXFile();
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
        setServiceRunning(true);
        Toast.makeText(this, getString(R.string.toast_servicestarted), Toast.LENGTH_SHORT).show();
        nmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        updateNotif = isNotifBarEnabled();
        nbuilder = new Notification.Builder(this);

        distance = 0;
        speed = 0;
        avspeed = 0;
        duration = 0 - BRActions.getTZOffset();

////////////////////////////////////////////////////////////////////////////////////////////////////
        IntentFilter filter = new IntentFilter();
        filter.addAction(BRActions.SERVICE_RECEIVER);
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
                    broadcast.setAction(BRActions.MAIN_RECEIVER);
                    broadcast.putExtra("duration", duration);
                    broadcast.putExtra("distance", distance);
                    broadcast.putExtra("speed", speed);
                    broadcast.putExtra("avspeed", avspeed);
                    broadcast.putExtra("gps", gpsStatus);
                    sendBroadcast(broadcast);
                }
            }
        };
        registerReceiver(receiver, filter);
////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locationListener);
        gpx.saveGPX();
        stopForeground(true);
        setServiceRunning(false);
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
        this.duration = duration - BRActions.getTZOffset();
        this.distance = distance;
        this.speed = speed;
        this.avspeed = avspeed;

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

    void setServiceRunning(boolean run) {
        SharedPreferences settings = getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(SettingsActivity.IS_SERVICE_RUNNING, run);
        editor.apply();
    }
}
