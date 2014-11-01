package bazulis.gpxtracker.trk;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import bazulis.gpxtracker.trk.util.GPXFile;
import bazulis.gpxtracker.trk.util.MapView;

public class GPXDetails extends Activity {
    private TextView t_duration;
    private TextView t_distance;
    private TextView t_avspeed;
    private TextView t_maxspeed;
    private TextView t_efficiency;
    private TextView t_uphill;
    private TextView t_downhill;
    private TextView t_maxheight, t_minheight, t_deltaheight;
    public MapView map;
    private String filename;
    private boolean fileIsEmpty = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpxdetails);
        Intent intent = getIntent();

        filename = intent.getStringExtra("filename");
        TextView t_filename = (TextView) findViewById(R.id.t_filename);
        t_filename.setText(filename);

        map = (MapView) findViewById(R.id.mapView);
        t_avspeed = (TextView) findViewById(R.id.t_avspeed);
        t_distance = (TextView) findViewById(R.id.t_distance);
        t_duration = (TextView) findViewById(R.id.t_duration);
        t_maxspeed = (TextView) findViewById(R.id.t_maxspeed);
        t_efficiency = (TextView) findViewById(R.id.t_efficiency);
        t_uphill = (TextView) findViewById(R.id.t_uphill);
        t_downhill = (TextView) findViewById(R.id.t_downhill);
        t_maxheight = (TextView) findViewById(R.id.t_maxheight);
        t_minheight = (TextView) findViewById(R.id.t_minheight);
        t_deltaheight = (TextView) findViewById(R.id.t_deltaheight);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int w = size.x;
        map.setLayoutParams(new LinearLayout.LayoutParams(w, w));

        GPXFile gpx = new GPXFile(filename, false);
        if (!gpx.isEmptyFile()) {
            gpx.analyzeGPX(this);
            map.setup(gpx.lats, gpx.lons, gpx.eles);
        } else {
            fileIsEmpty = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gpxdetails, menu);
        if (fileIsEmpty) {
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_share) {
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath() + "/gpx/" + filename)));
            share.setType("application/xml");
            startActivity(share);
            return true;
        }
        if (id == R.id.action_delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_delete));
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    File file = new File(Environment.getExternalStorageDirectory().getPath()+"/gpx/"+filename);
                    boolean del = file.delete();
                    if (del) {
                        Toast.makeText(getApplicationContext(), filename+" "+getString(R.string.toast_wasdeleted), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), getString(R.string.confirm_no), Toast.LENGTH_SHORT).show();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        if(id == R.id.action_resume) {
            Intent intent = new Intent();
            intent.putExtra("filename", filename);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    public void updateData(double distance, long duration, double avspeed, double maxspeed, double efficiency, double uphill, double downhill, double maxheight, double minheight) {
        t_distance.setText(new DecimalFormat("##.## km").format(distance/1000));
        t_duration.setText(new SimpleDateFormat("HH:mm:ss").format(new Date(duration-getTZOffset())));
        t_avspeed.setText(new DecimalFormat("##.## km/h").format(avspeed));
        t_maxspeed.setText(new DecimalFormat("##.## km/h").format(maxspeed));
        t_efficiency.setText(new DecimalFormat("##.## %").format(efficiency));
        t_uphill.setText(new DecimalFormat("### m").format(uphill));
        t_downhill.setText(new DecimalFormat("### m").format(downhill));
        t_maxheight.setText(new DecimalFormat("### m").format(maxheight));
        t_minheight.setText(new DecimalFormat("### m").format(minheight));
        t_deltaheight.setText(new DecimalFormat("### m").format(maxheight-minheight));
    }
    private long getTZOffset() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getRawOffset()+tz.getDSTSavings();
    }
}
