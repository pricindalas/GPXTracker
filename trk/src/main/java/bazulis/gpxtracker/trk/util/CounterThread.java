package bazulis.gpxtracker.trk.util;

import android.location.Location;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import bazulis.gpxtracker.trk.GPXDetails;

/**
 * Created by bazulis on 14.7.12.
 * GPX failo skaiciuotuvas
 */
public class CounterThread extends Thread {
    private boolean countEles;
    private GPXDetails activity;
    private List<Float> lats, lons, eles;
    private List<String> times;
    private long duration;
    private double distance;
    private double avspeed;
    private double maxspeed;
    private double efficiency;
    private double uphill, downhill, maxheight, minheight;
    public CounterThread(GPXDetails activity, List<Float> lons, List<Float> lats, List<String> times, List<Float> eles) {
        this.activity = activity;
        this.lons = lons;
        this.lats = lats;
        this.eles = eles;
        this.times = times;
        countEles = true;
        distance = 0;
        duration = 0;
        avspeed = 0;
        maxspeed = 0;
    }
    public CounterThread(List<Float> lons, List<Float> lats, List<String> times) {
        this.lons = lons;
        this.lats = lats;
        this.times = times;
        countEles = false;
        distance = 0;
        duration = 0;
        avspeed = 0;
        maxspeed = 0;
    }
    @Override
    public void run() {
        final Location loc1 = new Location("");
        final Location loc2 = new Location("");
        SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS");
        Date t1, t2;
        double time = 1;
        double dist, tmax;
        uphill = 0;
        downhill = 0;
        if (countEles && !eles.isEmpty()) {
            maxheight = eles.get(0);
            minheight = eles.get(0);
        }
        for(int i = 0; i < lons.size()-1; i++) {
            try {
                t1 = dformat.parse(times.get(i));
                t2 = dformat.parse(times.get(i+1));
                time = t2.getTime() - t1.getTime();
                duration += time;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            loc1.setLatitude(lats.get(i));
            loc1.setLongitude(lons.get(i));
            loc2.setLatitude(lats.get(i + 1));
            loc2.setLongitude(lons.get(i + 1));
            dist = loc1.distanceTo(loc2);
            distance += dist;
            tmax = dist/(time/3600);
            if(maxspeed<tmax) maxspeed = tmax;
            if(countEles) {
                if (maxheight<eles.get(i)) maxheight = eles.get(i);
                if (minheight>eles.get(i)) minheight = eles.get(i);
                double ele = eles.get(i + 1) - eles.get(i);
                if(ele>0) {
                    uphill += ele;
                } else {
                    downhill += -ele;
                }
            }
        }
        if(lats.size()>1) {
            Location l1 = new Location("");
            Location l2 = new Location("");
            l1.setLatitude(lats.get(0));
            l1.setLongitude(lons.get(0));
            l2.setLatitude(lats.get(lats.size() - 1));
            l2.setLongitude(lons.get(lons.size() - 1));
            efficiency = l1.distanceTo(l2) / distance;
        }
        avspeed = distance / (duration / 3600);
        if (activity!=null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.updateData(distance, duration, avspeed, maxspeed, efficiency, uphill, downhill, maxheight, minheight);
                    activity.map.invalidate();
                }
            });
        }
    }
    public double getDistance() {
        return distance;
    }
    public long getDuration() {
        return duration;
    }
    public double getAvspeed() {
        return avspeed;
    }
}
