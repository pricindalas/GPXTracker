package bazulis.gpxtracker.trk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import bazulis.gpxtracker.trk.util.Config;
import bazulis.gpxtracker.trk.util.GPXRouteFile;


public class MainActivity extends Activity {

    private BroadcastReceiver receiver;
    private UITicker ticker;
    private boolean serviceRunning = false;
    private boolean isHrmEnabled;
    private SimpleDateFormat durationFormat;

    private TextView t_distance, t_duration, t_speed, t_avspeed, t_pace, t_avpace, t_gps_status, t_heartrate, t_avheartrate;
    private ImageView ic_gps_status;
    private Button b_start, b_stop;

    private NavigationFragment navFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        durationFormat = new SimpleDateFormat("HH:mm:ss");
        durationFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        final SimpleDateFormat paceFormat = new SimpleDateFormat("mm:ss");
        paceFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        t_distance = (TextView) findViewById(R.id.t_distance);
        t_duration = (TextView) findViewById(R.id.t_duration);
        t_speed = (TextView) findViewById(R.id.t_speed);
        t_avspeed = (TextView) findViewById(R.id.t_avspeed);
        t_pace = (TextView) findViewById(R.id.t_pace);
        t_avpace = (TextView) findViewById(R.id.t_avpace);
        t_gps_status = (TextView) findViewById(R.id.t_gps_status);
        t_heartrate = (TextView) findViewById(R.id.t_heartrate);
        t_avheartrate = (TextView) findViewById(R.id.t_avheartrate);
        ic_gps_status = (ImageView) findViewById(R.id.ic_gps_status);

        SharedPreferences preferences = getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0);
        if (!preferences.getBoolean(SettingsActivity.SETTINGS_DISTANCE_ENABLED, true)) t_distance.setVisibility(View.GONE);
        if (!preferences.getBoolean(SettingsActivity.SETTINGS_DURATION_ENABLED, true)) t_duration.setVisibility(View.GONE);
        if (!preferences.getBoolean(SettingsActivity.SETTINGS_SPEED_ENABLED, true)) t_speed.setVisibility(View.GONE);
        if (!preferences.getBoolean(SettingsActivity.SETTINGS_AVSPEED_ENABLED, true)) t_avspeed.setVisibility(View.GONE);
        if (!preferences.getBoolean(SettingsActivity.SETTINGS_PACE_ENABLED, true)) t_pace.setVisibility(View.GONE);
        if (!preferences.getBoolean(SettingsActivity.SETTINGS_AVPACE_ENABLED, true)) t_avpace.setVisibility(View.GONE);


        b_start = (Button) findViewById(R.id.b_start);
        b_stop = (Button) findViewById(R.id.b_stop);

        b_start.setEnabled(true);
        b_stop.setEnabled(false);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.MAIN_RECEIVER);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                serviceRunning = true;
                b_start.setEnabled(false);
                b_stop.setEnabled(true);
                t_duration.setText(durationFormat.format(new Date(intent.getLongExtra("duration", 0))));
                t_distance.setText(new DecimalFormat("#.## km").format(intent.getDoubleExtra("distance", 0)));
                t_speed.setText(new DecimalFormat("##.## km/h").format(intent.getDoubleExtra("speed", 0)));


                t_avspeed.setText(new DecimalFormat("##.## km/h").format(intent.getDoubleExtra("avspeed", 0)));
                double pace = intent.getDoubleExtra("pace", 0);
                double avpace = intent.getDoubleExtra("avpace", 0);
                t_pace.setText(paceFormat.format(new Date((long) pace))+" min./km");
                t_avpace.setText(paceFormat.format(new Date((long) avpace))+" min./km");
                if (isHrmEnabled) {
                    t_heartrate.setText(intent.getIntExtra("heartrate", 0)+getString(R.string.t_bpm));
                    t_avheartrate.setText((int) intent.getDoubleExtra("avheartrate", 0)+getString(R.string.t_bpm));
                }
                switch (intent.getIntExtra("gps", 0)) {
                    case 0 : {
                        t_gps_status.setText(getString(R.string.gps_isdisabled));
                        ic_gps_status.setImageResource(R.drawable.gps_disconected);
                        break;
                    }
                    case 1 : {
                        t_gps_status.setText(getString(R.string.gps_searching));
                        ic_gps_status.setImageResource(R.drawable.gps_searching);
                        break;
                    }
                    case 2 : {
                        t_gps_status.setText(getString(R.string.gps_connected));
                        ic_gps_status.setImageResource(R.drawable.ic_gps_receiving);
                        break;
                    }
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        if (id == R.id.action_recordList) {
            Intent intent = new Intent(this, RecordList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("requestCode", 0);
            startActivityForResult(intent, 0);
            return true;
        }
        if (id == R.id.action_follow_route) {
            Intent intent = new Intent(this, RecordList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("requestCode", 1);
            startActivityForResult(intent, 1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onStart() {
        ticker = new UITicker();
        ticker.start();
        isHrmEnabled = getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0).getBoolean(SettingsActivity.SETTINGS_ENABLE_HR_MONITOR, false);
        if (!isHrmEnabled) {
            t_heartrate.setVisibility(View.GONE);
            t_avheartrate.setVisibility(View.GONE);
        } else {
            if (getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0).getBoolean(SettingsActivity.SETTINGS_AVHEARTRATE_ENABLED, false))
                t_avheartrate.setVisibility(View.VISIBLE);
            else t_avheartrate.setVisibility(View.GONE);
            t_heartrate.setVisibility(View.VISIBLE);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        ticker.running = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if(!serviceRunning) {
                b_start.setEnabled(false);
                b_stop.setEnabled(true);
                startService(true, data.getStringExtra("filename"));
                Toast.makeText(this, getString(R.string.intent_resumeFileOK)+" "+data.getStringExtra("filename"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.intent_resumeFileFail), Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            navFragment = new NavigationFragment();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.navigation_container, navFragment);
            ft.commit();
            GPXRouteFile route = new GPXRouteFile(new File(data.getStringExtra("filename")));
            navFragment.setPoints(route.getPoints());
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void begin(View view) {
        b_start.setEnabled(false);
        b_stop.setEnabled(true);
        startService(false, null);
    }

    public void end(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_confirmStop));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                b_stop.setEnabled(false);
                b_start.setEnabled(true);
                serviceRunning = false;
                stopService();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), getString(R.string.confirm_no), Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void startService(boolean resume, String filename) {
        Intent serviceIntent = new Intent(this, TrackService.class);
        if (resume) serviceIntent.putExtra("filename", filename);
        startService(serviceIntent);
    }

    private void stopService() {
        Intent stopIntent = new Intent();
        stopIntent.setAction(Config.TRACK_SERVICE_RECEIVER);
        stopIntent.putExtra("stop", true);
        sendBroadcast(stopIntent);
        t_gps_status.setText(getString(R.string.gps_status));
        t_gps_status.setTextColor(Color.GRAY);
        ic_gps_status.setImageResource(R.drawable.ic_gps_idle);
    }

    public void closeNavigation() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(navFragment);
        ft.commit();
    }

    private class UITicker extends Thread {
        public boolean running = true;
        private int refreshInterval = getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0).getInt(SettingsActivity.SETTINGS_REFRESH_INTERVAL, 2) * 1000;
        @Override
        public void run() {
            super.run();
            while (running) {
                try {
                    Intent request = new Intent();
                    request.setAction(Config.TRACK_SERVICE_RECEIVER);
                    request.putExtra("getdata", true);
                    sendBroadcast(request);
                    Thread.sleep(refreshInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
