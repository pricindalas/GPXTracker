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
    public static final String UUID_HR_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static final String UUID_HR_CHARACTERISTIC = "00002a37-0000-1000-8000-00805f9b34fb";
    public static final String UUID_HR_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";
}
