package bazulis.gpxtracker.trk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import bazulis.gpxtracker.trk.util.BRActions;


public class MainActivity extends Activity {

    private boolean fileResuming;
    private String filename;
    private BroadcastReceiver receiver;

    private TextView t_distance, t_duration, t_speed, t_avspeed, t_gps_status;
    private ImageView ic_gps_status;
    private Button b_start, b_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t_distance = (TextView) findViewById(R.id.t_distance);
        t_duration = (TextView) findViewById(R.id.t_duration);
        t_speed = (TextView) findViewById(R.id.t_speed);
        t_avspeed = (TextView) findViewById(R.id.t_avspeed);
        t_gps_status = (TextView) findViewById(R.id.t_gps_status);
        ic_gps_status = (ImageView) findViewById(R.id.ic_gps_status);

        b_start = (Button) findViewById(R.id.b_start);
        b_stop = (Button) findViewById(R.id.b_stop);

        if (isServiceRunning()) {
            b_start.setEnabled(false);
            b_stop.setEnabled(true);
        } else {
            b_start.setEnabled(true);
            b_stop.setEnabled(false);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BRActions.MAIN_RECEIVER);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // 0 - ateinantys duomenys is LocationListener
                // 1 - ateinantis GPS statuso pakeitimas
                switch (intent.getIntExtra("TYPE", 0)) {
                    case 0 : {
                        t_duration.setText(intent.getStringExtra("duration"));
                        t_distance.setText(new DecimalFormat("#.## km").format(intent.getDoubleExtra("distance", 0)));
                        t_speed.setText(new DecimalFormat("##.## km/h").format(intent.getDoubleExtra("speed", 0)));
                        t_avspeed.setText(new DecimalFormat("##.## km/h").format(intent.getDoubleExtra("avspeed", 0)));
                    }
                    case 1 : {
                        int color = intent.getIntExtra("color", Color.GRAY);
                        t_gps_status.setText(intent.getStringExtra("gps"));
                        t_gps_status.setTextColor(color);
                        switch (color) {
                            case Color.GREEN: {
                                ic_gps_status.setImageResource(R.drawable.ic_gps_receiving);
                                break;
                            }
                            case Color.YELLOW: {
                                ic_gps_status.setImageResource(R.drawable.gps_searching);
                                break;
                            }
                            case Color.RED: {
                                ic_gps_status.setImageResource(R.drawable.gps_disconected);
                                break;
                            }
                            case Color.GRAY: {
                                ic_gps_status.setImageResource(R.drawable.ic_gps_idle);
                                break;
                            }
                        }
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
            startActivityForResult(intent, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onStart() {

        // Siusti atsibudimo intenta.
        Intent intent = new Intent();
        intent.setAction(BRActions.SERVICE_RECEIVER);
        intent.putExtra("visible", true);
        sendBroadcast(intent);

        super.onStart();
    }

    @Override
    protected void onStop() {

        // Siusti uzmigimo intenta.
        Intent intent = new Intent();
        intent.setAction(BRActions.SERVICE_RECEIVER);
        intent.putExtra("visible", false);
        sendBroadcast(intent);

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
            filename = data.getStringExtra("filename");
            if(!isServiceRunning()) {
                b_start.setEnabled(false);
                b_stop.setEnabled(true);
                fileResuming = true;
                startService();
                Toast.makeText(this, getString(R.string.intent_resumeFileOK)+" "+filename, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.intent_resumeFileFail), Toast.LENGTH_SHORT).show();
            }

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void begin(View view) {
        b_start.setEnabled(false);
        b_stop.setEnabled(true);
        startService();
    }

    public void end(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_confirmStop));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                b_stop.setEnabled(false);
                b_start.setEnabled(true);
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
    private boolean isServiceRunning() {
        return getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0).getBoolean(SettingsActivity.IS_SERVICE_RUNNING, false);
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, TrackService.class);
        if (fileResuming) serviceIntent.putExtra("filename", filename);
        startService(serviceIntent);
    }

    private void stopService() {
        Intent stopIntent = new Intent();
        stopIntent.setAction(BRActions.SERVICE_RECEIVER);
        stopIntent.putExtra("stop", true);
        sendBroadcast(stopIntent);
        t_gps_status.setText(getString(R.string.gps_status));
        t_gps_status.setTextColor(Color.GRAY);
    }
}
