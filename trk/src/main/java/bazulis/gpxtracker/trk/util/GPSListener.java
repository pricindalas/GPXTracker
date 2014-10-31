package bazulis.gpxtracker.trk.util;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

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
    public double speed, avspeed;

    private long totalTime, prevTime, startTime, duration;

    public GPSListener(TrackService service, int minimumSpeed) {
        this.service = service;
        distance = 0;
        this.minimumSpeed = minimumSpeed;
        startTime = System.currentTimeMillis();
        prevTime = startTime;
        totalTime = 0;
        duration = totalTime;
    }
    public GPSListener(TrackService service, int minimumSpeed, long totalTime, double distance, long startTime, long prevTime) {
        this.service = service;
        this.minimumSpeed = minimumSpeed;
        this.totalTime = totalTime;
        this.distance = distance;
        this.startTime = startTime;
        this.prevTime = prevTime;
        avspeed = (distance/(totalTime/1000))*3.6;
        duration = totalTime;
        this.service.updateLocation(duration, distance / 1000, speed, avspeed);
    }

    @Override
    public void onLocationChanged(Location location) {
        service.gpsStatus = 2;
        long segTime = System.currentTimeMillis()-prevTime;
        prevTime = System.currentTimeMillis();
        speed = location.getSpeed() * 3.6;
        if(prevLocation!=null) distance += location.distanceTo(prevLocation);
        prevLocation = location;
        if(speed > minimumSpeed || minimumSpeed==0) {
            totalTime += segTime;
            avspeed = (distance/(totalTime/1000))*3.6;
            duration = totalTime;
            service.updateLocation(duration, distance / 1000, speed, avspeed);
            service.gpx.addSegment(location.getLatitude(), location.getLongitude(), location.getAltitude(), startTime+totalTime-BRActions.getTZOffset());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        switch (i) {
            case 0: {
                service.gpsStatus = 0;
            }
            case 1: {
                service.gpsStatus = 1;
            }
            case 2: {
                service.gpsStatus = 2;
            }
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        service.gpsStatus = 1;
    }

    @Override
    public void onProviderDisabled(String s) {
        service.gpsStatus = 0;
    }
}
