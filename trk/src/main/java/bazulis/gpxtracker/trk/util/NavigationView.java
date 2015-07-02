package bazulis.gpxtracker.trk.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

/**
 * Created by bazulis on 14.12.13.
 *
 */
public class NavigationView extends View {
    private List<Location> points;
    public static Location currentLocation;
    private static final float SCALE = 0.5f;
    final double lonRadius = 6378.16;
    final double latRadius = 6357.715;
    final double lonAngle = 360 / ( 2 * Math.PI * lonRadius );
    final double latAngle = 360 / ( 2 * Math.PI * latRadius );
    Paint linePaint = new Paint();
    Paint objectPaint = new Paint();
    public NavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        objectPaint.setAntiAlias(true);
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.rgb(192, 192, 192));
        linePaint.setStrokeWidth(5);
    }

    public void setup(List<Location> points) {
        this.points = points;
        currentLocation = new Location("");
        currentLocation.setLongitude(this.points.get(0).getLongitude());
        currentLocation.setLatitude(this.points.get(0).getLatitude());
        this.invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private float getlonX(int i, int width) {
        double r = width / 2 + SCALE * (points.get(i).getLongitude() - currentLocation.getLongitude()) * width / lonAngle * Math.cos(Math.toRadians(points.get(i).getLatitude()));
        return (float) r;
    }
    private float getlatY(int i, int height) {
        double r = height / 2 + SCALE * (currentLocation.getLatitude() - points.get(i).getLatitude()) * height / latAngle;
        return (float) r;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!points.isEmpty() && currentLocation != null) {
            objectPaint.setColor(Color.rgb(255, 255, 128));
            for (int i = 0; i < points.size() - 1; i++) {
                if (points.get(i).distanceTo(currentLocation) < 1500) {
                    canvas.drawLine(getlonX(i, canvas.getWidth()), getlatY(i, canvas.getHeight()), getlonX(i + 1, canvas.getWidth()), getlatY(i + 1, canvas.getHeight()), linePaint);
                }
            }
        } else {
            objectPaint.setColor(Color.RED);
        }
        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, 8, objectPaint);
    }
}
