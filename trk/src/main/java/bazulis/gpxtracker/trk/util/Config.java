package bazulis.gpxtracker.trk.util;


import android.os.Environment;

/**
 * Created by bazulis on 14.8.14.
 *
 */
public class Config {
    public static final String GPX_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/gpx/";
    public static final String MAIN_RECEIVER = "bazulis.gpxtracker.trk.GETDATA";
    public static final String SERVICE_RECEIVER = "bazulis.gpxtracker.trk.PUSHCOMMANDS";
}
