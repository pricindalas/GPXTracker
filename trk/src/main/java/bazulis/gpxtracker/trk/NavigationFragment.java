package bazulis.gpxtracker.trk;

import android.app.Fragment;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.List;

import bazulis.gpxtracker.trk.util.NavigationView;

/**
 * Created by bazulis on 14.12.13.
 *
 */
public class NavigationFragment extends Fragment {
    private NavigationView navMap;

    private List<Location> points;

    @Override
    public void onDetach() {
        super.onStop();
        tickeris.interrupt();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);
        navMap = (NavigationView) view.findViewById(R.id.navigation_map);
        Button closeButton = (Button) view.findViewById(R.id.b_close_navigation);
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        int w = size.x;
        navMap.setLayoutParams(new LinearLayout.LayoutParams(w, w));
        navMap.setup(points);
        tickeris.start();
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.closeNavigation();
            }
        });
        return view;
    }
    public void setPoints(List<Location> points) {
        this.points = points;
    }

    private Thread tickeris = new Thread() {
        public boolean isRunning = true;
        @Override
        public void run() {
            super.run();
            while (isRunning) {
                System.out.println("Tick...");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        navMap.invalidate();
                    }
                });
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    isRunning = false;
                    e.printStackTrace();
                }
            }
        }
    };
}
