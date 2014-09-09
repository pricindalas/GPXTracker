package bazulis.gpxtracker.trk.util;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import bazulis.gpxtracker.trk.R;
import bazulis.gpxtracker.trk.TrackService;

/**
 * Created by bazulis on 14.6.11.
 * GPS listeneris
 */
public class GPSListener implements LocationListener {
    private TrackService service;
    private Location prevLocation;
    private double distance;
    private int minimumSpeed;
    public String duration;
    public double speed, avspeed;

    private boolean firstOnChangeTicked;

    private long totalTime, prevTime, startTime;
    private DateFormat dFormat;

    public GPSListener(TrackService service, int minimumSpeed) {
        firstOnChangeTicked = false;
        dFormat = new SimpleDateFormat("HH:mm:ss");
        this.service = service;
        distance = 0;
        this.minimumSpeed = minimumSpeed;
        startTime = System.currentTimeMillis();
        prevTime = startTime;
        totalTime = 0;
    }
    public GPSListener(TrackService service, int minimumSpeed, long totalTime, double distance, long startTime, long prevTime) {
        firstOnChangeTicked = false;
        dFormat = new SimpleDateFormat("HH:mm:ss");
        this.service = service;
        this.minimumSpeed = minimumSpeed;
        this.totalTime = totalTime;
        this.distance = distance;
        this.startTime = startTime + getTZOffset();
        this.prevTime = prevTime;
        avspeed = (distance/(totalTime/1000))*3.6;
        duration = dFormat.format(new Date(totalTime-getTZOffset()));
        this.service.updateLocation(duration, distance / 1000, speed, avspeed);
    }
    /////////////////////////////////////////UTIL FUNKCIJOS/////////////////////////////////////////
    private long getTZOffset() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getRawOffset()+tz.getDSTSavings();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onLocationChanged(Location location) {
        if (!firstOnChangeTicked) {
            service.gpsStatus(service.getString(R.string.gps_connected), Color.GREEN);
            firstOnChangeTicked = true;
            Toast.makeText(service, service.getString(R.string.toast_trackstarted), Toast.LENGTH_SHORT).show();
        }
        long segTime = System.currentTimeMillis()-prevTime;
        prevTime = System.currentTimeMillis();
        speed = location.getSpeed() * 3.6;
        if(prevLocation!=null) distance += location.distanceTo(prevLocation);
        prevLocation = location;
        if(speed > minimumSpeed || minimumSpeed==0) {
            totalTime += segTime;
            avspeed = (distance/(totalTime/1000))*3.6;
            duration = dFormat.format(new Date(totalTime-getTZOffset()));
            service.updateLocation(duration, distance / 1000, speed, avspeed);
            service.gpx.addSegment(location.getLatitude(), location.getLongitude(), location.getAltitude(), startTime+totalTime-getTZOffset());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        switch (i) {
            case 0: {
                service.gpsStatus(service.getString(R.string.gps_problem), Color.RED);
                firstOnChangeTicked = false;
            }
            case 1: {
                service.gpsStatus(service.getString(R.string.gps_searching), Color.YELLOW);
                firstOnChangeTicked = false;
            }
            case 2: service.gpsStatus(service.getString(R.string.gps_connected), Color.GREEN);
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        firstOnChangeTicked = false;
        service.gpsStatus(service.getString(R.string.gps_isenabled), Color.YELLOW);
    }

    @Override
    public void onProviderDisabled(String s) {
        firstOnChangeTicked = false;
        service.gpsStatus(service.getString(R.string.gps_isdisabled), Color.YELLOW);
    }
}
