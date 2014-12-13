package bazulis.gpxtracker.trk.util;

import android.location.Location;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bazulis on 14.12.13.
 *
 */
public class GPXRouteFile {
    private File file;
    private List<Location> points = new ArrayList<>();
    public GPXRouteFile(File file) {
        this.file = file;
        readFile();
    }
    public void readFile() {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(new FileInputStream(file)));
            int eventType = xpp.getEventType();
            while (eventType!=XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("trkpt")) {
                        Location point = new Location("");
                        point.setLatitude(Double.parseDouble(xpp.getAttributeValue(null, "lat")));
                        point.setLongitude(Double.parseDouble(xpp.getAttributeValue(null, "lon")));
                        points.add(point);
                        eventType = xpp.next();
                        continue;
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }
    public List<Location> getPoints() {
        return points;
    }
}
