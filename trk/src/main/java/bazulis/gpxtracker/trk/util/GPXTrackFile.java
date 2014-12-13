package bazulis.gpxtracker.trk.util;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import bazulis.gpxtracker.trk.GPXDetails;

/**
 * Created by bazulis on 14.8.13.
 * GPX failo objektas su pagrindinem funkcijom.
 */
public class GPXTrackFile {
    public final List<Float> lats = new ArrayList<>();
    public final List<Float> lons = new ArrayList<>();
    public final List<Float> eles = new ArrayList<>();
    private final List<String> times = new ArrayList<>();
    private final String filename;

    public double distance;
    public long duration, startTime, lastTime;

    private FileOutputStream outputStream;
    private final SimpleDateFormat dformatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private final SimpleDateFormat fformatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

    public GPXTrackFile(String filename, boolean resumeFile) {
        dformatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.filename = filename;
        checkFolder();
        if (resumeFile) {
            resumeGPX();
        } else {
            readGPX();
        }
    }

    public GPXTrackFile() {
        dformatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.filename = fformatter.format(new Date()) + ".gpx";
        newGPX(filename);
    }

    private void checkFolder() {
        File gpxFolder = new File(Config.GPX_PATH);
        if (!gpxFolder.mkdir() && !gpxFolder.exists()) {
            System.out.println("Can't create a gpx folder!");
        }
    }

    private void readGPX() {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(new FileInputStream(Config.GPX_PATH + filename)));
            int eventType = xpp.getEventType();
            float lat, lon, ele;
            String time;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("trkpt")) {
                        lat = Float.parseFloat(xpp.getAttributeValue(null, "lat"));
                        lon = Float.parseFloat(xpp.getAttributeValue(null, "lon"));
                        lats.add(lat);
                        lons.add(lon);
                        eventType = xpp.next();
                        continue;
                    }
                    if (xpp.getName().equals("time")) {
                        eventType = xpp.next();
                        time = xpp.getText();
                        times.add(time);
                        continue;
                    }
                    if (xpp.getName().equals("ele")) {
                        eventType = xpp.next();
                        ele = Float.parseFloat(xpp.getText());
                        eles.add(ele);
                        continue;
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    public void analyzeGPX(GPXDetails activity) {
        CounterThread counter = new CounterThread(activity, lons, lats, times, eles);
        counter.start();
    }

    public void analyzeGPX() {
        CounterThread counter = new CounterThread(lons, lats, times);
        counter.run();
        duration = counter.getDuration();
        distance = counter.getDistance();
        try {
            startTime = dformatter.parse(times.get(0)).getTime();
            lastTime = dformatter.parse(times.get(times.size() - 1)).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void newGPX(String filename) {
        try {
            File file = new File(Config.GPX_PATH + filename);
            outputStream = new FileOutputStream(file);
            String startTag = "<?xml version='1.0' standalone='yes' ?>\n<gpx>\n <metadata>\n  <time>"+dformatter.format(new Date())+"</time>\n </metadata>\n  <trk>\n    <trkseg>\n";
            outputStream.write(startTag.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resumeGPX() {
        readGPX();
        newGPX(fformatter.format(new Date()) + "-R.gpx");
        for (int i = 0; i < lats.size(); i++) {
            addSegment(lats.get(i), lons.get(i), eles.get(i), times.get(i));
        }
    }

    public void addSegment(double lat, double lon, double elevation, long time) {
        String trackTag = "      <trkpt lat=\"" + lat + "\" lon=\"" + lon + "\">\n        <ele>" + elevation + "</ele>\n        <time>" + dformatter.format(new Date(time)) + "</time>\n      </trkpt>\n";
        try {
            outputStream.write(trackTag.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addSegment(double lat, double lon, double elevation, String time) {
        String trackTag = "      <trkpt lat=\"" + lat + "\" lon=\"" + lon + "\">\n        <ele>" + elevation + "</ele>\n        <time>" + time + "</time>\n      </trkpt>\n";
        try {
            outputStream.write(trackTag.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveGPX() {
        String stopTag = "    </trkseg>\n  </trk>\n</gpx>\n";
        try {
            outputStream.write(stopTag.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isEmptyFile() {
        return eles.size() <= 0;
    }
}
