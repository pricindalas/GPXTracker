package bazulis.gpxtracker.trk;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    public static final String SETTINGS_NAME = "gpx_settings";
    public static final String SETTINGS_REFRESH_INTERVAL = "refresh_interval";
    public static final String SETTINGS_MINIMUM_SPEED = "minimum_speed";
    public static final String SETTINGS_UPDATE_NOTIFICATION_BAR = "notification_bar_update";
    public static final String IS_SERVICE_RUNNING = "is_service_running";

    private static int REFRESH_INTERVAL;
    private static int MINIMUM_SPEED;
    private static boolean UPDATE_NOTIFICATION_BAR;

    private TextView interval, speed;
    private CheckBox update_status_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        interval = (TextView) findViewById(R.id.t_interval_value);
        speed = (TextView) findViewById(R.id.t_speed_value);
        update_status_bar = (CheckBox) findViewById(R.id.chk_notification_update);
        SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, 0);
        REFRESH_INTERVAL = settings.getInt(SETTINGS_REFRESH_INTERVAL, 2);
        MINIMUM_SPEED = settings.getInt(SETTINGS_MINIMUM_SPEED, 2);
        UPDATE_NOTIFICATION_BAR = settings.getBoolean(SETTINGS_UPDATE_NOTIFICATION_BAR, true);
        interval.setText(REFRESH_INTERVAL+" s");
        speed.setText(MINIMUM_SPEED+" m/s");
        update_status_bar.setChecked(UPDATE_NOTIFICATION_BAR);
    }
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(SETTINGS_REFRESH_INTERVAL, REFRESH_INTERVAL);
        editor.putInt(SETTINGS_MINIMUM_SPEED, MINIMUM_SPEED);
        editor.putBoolean(SETTINGS_UPDATE_NOTIFICATION_BAR, UPDATE_NOTIFICATION_BAR);
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
}
