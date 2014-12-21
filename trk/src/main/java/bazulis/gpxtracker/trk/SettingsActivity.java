package bazulis.gpxtracker.trk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

    private static int REFRESH_INTERVAL;
    private static int MINIMUM_SPEED;
    private static boolean UPDATE_NOTIFICATION_BAR;
    private static boolean ENABLE_HR_MONITOR;

    private String HRM_NAME, HRM_MAC;

    private TextView interval, speed;
    private CheckBox update_status_bar, chk_enable_hrm;
    private Button hrmConfigButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        interval = (TextView) findViewById(R.id.t_interval_value);
        speed = (TextView) findViewById(R.id.t_speed_value);
        update_status_bar = (CheckBox) findViewById(R.id.chk_notification_update);
        chk_enable_hrm = (CheckBox) findViewById(R.id.chk_enable_hrm);
        SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, 0);
        REFRESH_INTERVAL = settings.getInt(SETTINGS_REFRESH_INTERVAL, 2);
        MINIMUM_SPEED = settings.getInt(SETTINGS_MINIMUM_SPEED, 2);
        UPDATE_NOTIFICATION_BAR = settings.getBoolean(SETTINGS_UPDATE_NOTIFICATION_BAR, true);
        ENABLE_HR_MONITOR = settings.getBoolean(SETTINGS_ENABLE_HR_MONITOR, false);
        interval.setText(REFRESH_INTERVAL+" s");
        speed.setText(MINIMUM_SPEED+" km/h");
        update_status_bar.setChecked(UPDATE_NOTIFICATION_BAR);
        chk_enable_hrm.setChecked(ENABLE_HR_MONITOR);
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
    public void chkBoxUpdateNotifications(View view) {
        UPDATE_NOTIFICATION_BAR = update_status_bar.isChecked();
    }
    public void chkBoxEnableHRM(View view) {
        ENABLE_HR_MONITOR = chk_enable_hrm.isChecked();
        hrmConfigButton.setEnabled(ENABLE_HR_MONITOR);
    }
}
