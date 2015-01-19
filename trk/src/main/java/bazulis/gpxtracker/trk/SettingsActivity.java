package bazulis.gpxtracker.trk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    public static final String SETTINGS_NAME = "gpx_settings";
    public static final String SETTINGS_REFRESH_INTERVAL = "refresh_interval";
    public static final String SETTINGS_MINIMUM_SPEED = "minimum_speed";
    public static final String SETTINGS_UPDATE_NOTIFICATION_BAR = "notification_bar_update";
    public static final String SETTINGS_ENABLE_HR_MONITOR = "hrm_enable";
    public static final String SETTINGS_HRMONITOR_MAC = "heart_rate_monitor_mac";
    public static final String SETTINGS_HRMONITOR_NAME = "heart_rate_monitor_name";
    public static final String SETTINGS_DURATION_ENABLED = "duration_enabled";
    public static final String SETTINGS_DISTANCE_ENABLED = "distance_enabled";
    public static final String SETTINGS_SPEED_ENABLED = "speed_enabled";
    public static final String SETTINGS_AVSPEED_ENABLED = "avspeed_enabled";
    public static final String SETTINGS_PACE_ENABLED = "pace_enabled";
    public static final String SETTINGS_AVPACE_ENABLED = "avpace_enabled";
    public static final String SETTINGS_AVHEARTRATE_ENABLED = "avheartrate_enabled";

    private static int REFRESH_INTERVAL;
    private static int MINIMUM_SPEED;
    private static boolean UPDATE_NOTIFICATION_BAR;
    private static boolean ENABLE_HR_MONITOR;
    private static boolean ENABLE_DURATION;
    private static boolean ENABLE_DISTANCE;
    private static boolean ENABLE_SPEED;
    private static boolean ENABLE_AVSPEED;
    private static boolean ENABLE_PACE;
    private static boolean ENABLE_AVPACE;
    private static boolean ENABLE_AVHEARTRATE;

    private String HRM_NAME, HRM_MAC;

    private TextView interval, speed;
    private Button hrmConfigButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        interval = (TextView) findViewById(R.id.t_interval_value);
        speed = (TextView) findViewById(R.id.t_speed_value);
        CheckBox update_status_bar = (CheckBox) findViewById(R.id.chk_notification_update);
        CheckBox chk_enable_hrm = (CheckBox) findViewById(R.id.chk_enable_hrm);
        CheckBox durationCheck = (CheckBox) findViewById(R.id.chk_duration);
        CheckBox distanceCheck = (CheckBox) findViewById(R.id.chk_distance);
        CheckBox speedCheck = (CheckBox) findViewById(R.id.chk_speed);
        CheckBox avspeedCheck = (CheckBox) findViewById(R.id.chk_avspeed);
        CheckBox paceCheck = (CheckBox) findViewById(R.id.chk_pace);
        CheckBox avpaceCheck = (CheckBox) findViewById(R.id.chk_avpace);
        CheckBox avheartrateCheck = (CheckBox) findViewById(R.id.chk_avheartrate);
        SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, 0);
        REFRESH_INTERVAL = settings.getInt(SETTINGS_REFRESH_INTERVAL, 2);
        MINIMUM_SPEED = settings.getInt(SETTINGS_MINIMUM_SPEED, 2);
        UPDATE_NOTIFICATION_BAR = settings.getBoolean(SETTINGS_UPDATE_NOTIFICATION_BAR, true);
        ENABLE_HR_MONITOR = settings.getBoolean(SETTINGS_ENABLE_HR_MONITOR, false);
        ENABLE_DURATION = settings.getBoolean(SETTINGS_DURATION_ENABLED, true);
        ENABLE_DISTANCE = settings.getBoolean(SETTINGS_DISTANCE_ENABLED, true);
        ENABLE_SPEED = settings.getBoolean(SETTINGS_SPEED_ENABLED, true);
        ENABLE_AVSPEED = settings.getBoolean(SETTINGS_AVSPEED_ENABLED, true);
        ENABLE_PACE = settings.getBoolean(SETTINGS_PACE_ENABLED, true);
        ENABLE_AVPACE = settings.getBoolean(SETTINGS_AVPACE_ENABLED, true);
        ENABLE_AVHEARTRATE = settings.getBoolean(SETTINGS_AVHEARTRATE_ENABLED, false);
        interval.setText(REFRESH_INTERVAL+" s");
        speed.setText(MINIMUM_SPEED+" km/h");
        update_status_bar.setChecked(UPDATE_NOTIFICATION_BAR);
        update_status_bar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UPDATE_NOTIFICATION_BAR = isChecked;
            }
        });
        chk_enable_hrm.setChecked(ENABLE_HR_MONITOR);
        chk_enable_hrm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ENABLE_HR_MONITOR = isChecked;
                hrmConfigButton.setEnabled(isChecked);
            }
        });
        durationCheck.setChecked(ENABLE_DURATION);
        durationCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ENABLE_DURATION = isChecked;
            }
        });
        distanceCheck.setChecked(ENABLE_DISTANCE);
        distanceCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ENABLE_DISTANCE = isChecked;
            }
        });
        speedCheck.setChecked(ENABLE_SPEED);
        speedCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ENABLE_SPEED = isChecked;
            }
        });
        avspeedCheck.setChecked(ENABLE_AVSPEED);
        avspeedCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ENABLE_AVSPEED = isChecked;
            }
        });
        paceCheck.setChecked(ENABLE_PACE);
        paceCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ENABLE_PACE = isChecked;
            }
        });
        avpaceCheck.setChecked(ENABLE_AVPACE);
        avpaceCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ENABLE_AVPACE = isChecked;
            }
        });
        avheartrateCheck.setChecked(ENABLE_AVHEARTRATE);
        avheartrateCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ENABLE_AVHEARTRATE = isChecked;
            }
        });
        hrmConfigButton = (Button) findViewById(R.id.b_hrm_config);
        hrmConfigButton.setEnabled(ENABLE_HR_MONITOR);
        hrmConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HrMonitorConfiguration.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            HRM_MAC = data.getStringExtra("hrmAddress");
            HRM_NAME = data.getStringExtra("hrmName");
            Toast.makeText(getApplicationContext(), "Nustatytas irenginys "+HRM_NAME+"\n"+HRM_MAC, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(SETTINGS_REFRESH_INTERVAL, REFRESH_INTERVAL);
        editor.putInt(SETTINGS_MINIMUM_SPEED, MINIMUM_SPEED);
        editor.putBoolean(SETTINGS_UPDATE_NOTIFICATION_BAR, UPDATE_NOTIFICATION_BAR);
        editor.putBoolean(SETTINGS_ENABLE_HR_MONITOR, ENABLE_HR_MONITOR);
        editor.putBoolean(SETTINGS_DURATION_ENABLED, ENABLE_DURATION);
        editor.putBoolean(SETTINGS_DISTANCE_ENABLED, ENABLE_DISTANCE);
        editor.putBoolean(SETTINGS_SPEED_ENABLED, ENABLE_SPEED);
        editor.putBoolean(SETTINGS_AVSPEED_ENABLED, ENABLE_AVSPEED);
        editor.putBoolean(SETTINGS_PACE_ENABLED, ENABLE_PACE);
        editor.putBoolean(SETTINGS_AVPACE_ENABLED, ENABLE_AVPACE);
        editor.putBoolean(SETTINGS_AVHEARTRATE_ENABLED, ENABLE_AVHEARTRATE);
        if (HRM_NAME!=null && HRM_MAC!=null) {
            editor.putString(SETTINGS_HRMONITOR_NAME, HRM_NAME);
            editor.putString(SETTINGS_HRMONITOR_MAC, HRM_MAC);
        }
        editor.apply();
        Toast.makeText(this, getString(R.string.toast_settingssaved), Toast.LENGTH_SHORT).show();
    }

    public void decreaseInterval(View view) {
        if(REFRESH_INTERVAL>1) {
            REFRESH_INTERVAL--;
            interval.setText(REFRESH_INTERVAL+"s");
        } else {
            Toast.makeText(this, getString(R.string.toast_minimumupdateinterval), Toast.LENGTH_SHORT).show();
        }
    }
    public void increaseInterval(View view) {
        REFRESH_INTERVAL++;
        interval.setText(REFRESH_INTERVAL+" s");
    }
    public void decreaseMinSpeed(View view) {
        if(MINIMUM_SPEED>0) {
            MINIMUM_SPEED--;
            speed.setText(MINIMUM_SPEED+" km/h");
        } else {
            Toast.makeText(this, getString(R.string.toast_minimumspeed), Toast.LENGTH_SHORT).show();
        }
    }
    public void increaseMinSpeed(View view) {
        MINIMUM_SPEED++;
        speed.setText(MINIMUM_SPEED+" km/h");
    }
}
