package bazulis.gpxtracker.trk.util;

import java.util.TimeZone;

/**
 * Created by bazulis on 14.8.14.
 *
 */
public class BRActions {
    public static String MAIN_RECEIVER = "bazulis.gpxtracker.trk.GETDATA";
    public static String SERVICE_RECEIVER = "bazulis.gpxtracker.trk.PUSHCOMMANDS";

    public static long getTZOffset() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getRawOffset()+tz.getDSTSavings();
    }
}
