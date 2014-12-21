package bazulis.gpxtracker.trk.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import bazulis.gpxtracker.trk.R;

/**
 * Created by bazulis on 14.7.12.
 * Zemelapio elementas
 */
public class MapView extends View {
    private static final int MAX_LINES = 450;
    private float maxlat;
    private float minlon;
    private float dAngle;
    private float dlatSeg;
    private float dlonSeg;
    private float dH;
    private int size, skipper;
    private final Paint mapLine;
    private final Context context;
    private List<Float> lats, lons, eles;
    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        lats = new ArrayList<>();
        lons = new ArrayList<>();

        mapLine = new Paint();
        mapLine.setColor(Color.rgb(255, 0, 0));
        mapLine.setStrokeWidth(2);
        mapLine.setAntiAlias(true);
    }
    @Override
    public void onDraw(Canvas canvas) {
        size = canvas.getHeight();
        if (!lats.isEmpty() && !lons.isEmpty()) {
            int x1, x2, y1, y2;
            float elev;
            for (int i = 0; i < lats.size() - skipper; i += skipper) {
                elev = (eles.get(i) - dH) / dH;
                mapLine.setColor(Color.rgb(red(elev), green(elev), 0));
                x1 = (int) getX(lons.get(i), lats.get(i));
                x2 = (int) getX(lons.get(i + skipper), lats.get(i + skipper));
                y1 = (int) getY(lats.get(i));
                y2 = (int) getY(lats.get(i + skipper));
                canvas.drawLine(x1, y1, x2, y2, mapLine);
            }

        }
    }
    /////////////////////////////////SETUP//////////////////////////////////////////////////////////
    private int red(float x) {
        if (x >= 0.5) {
            return 255;
        } else {
            return (int) (x * 510);
        }
    }
    private int green(float x) {
        if (x <= 0.5) {
            return 255;
        } else {
            return (int) ((1-x) * 510);
        }
    }
    public void setup(List<Float> lat, List<Float> lon, List<Float> ele) {
        lats = lat;
        lons = lon;
        eles = ele;
        skipper = lats.size() / MAX_LINES;
        if(skipper==0) skipper = 1;
        if (!lats.isEmpty() && !lons.isEmpty() && !eles.isEmpty()) {
            setBounds();
        } else {
            Toast.makeText(context, context.getString(R.string.toast_gpxempty), Toast.LENGTH_LONG).show();
        }
    }
    private double getX(double lon, double lat) {
        return 10 + (lon-minlon+dlonSeg) * (size-20) / (dAngle) * Math.cos(Math.toRadians(lat));
    }
    private double getY(double lat) {
        return 10 + (maxlat-lat+dlatSeg) * (size-20) / (dAngle);
    }
    private void setBounds() {
        float minlat, maxlon, minEle, maxEle;

        maxEle = minEle = eles.get(0);
        minlat = maxlat = lats.get(0);
        minlon = maxlon = lons.get(0);

        for(int i = 0; i < lats.size(); i++) {
            if (minlat >lats.get(i)) minlat = lats.get(i);
            if (maxlat<lats.get(i)) maxlat = lats.get(i);
            if (minlon>lons.get(i)) minlon = lons.get(i);
            if (maxlon <lons.get(i)) maxlon = lons.get(i);
            if (maxEle < eles.get(i)) maxEle = eles.get(i);
            if (minEle > eles.get(i)) minEle = eles.get(i);
            dH = maxEle - minEle;
        }
        float latseg = maxlat- minlat;
        float lonseg = (float) ((maxlon -minlon)*Math.cos(Math.toRadians(maxlat)));
        if (latseg>lonseg) {
            dAngle = latseg;
            dlonSeg = (latseg - lonseg) / 2;
            dlatSeg = 0;
        } else {
            dAngle = lonseg;
            dlatSeg = (lonseg - latseg) / 2;
            dlonSeg = 0;
        }
    }
}
