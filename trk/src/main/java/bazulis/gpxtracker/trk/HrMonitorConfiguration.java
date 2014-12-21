package bazulis.gpxtracker.trk;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class HrMonitorConfiguration extends Activity {

    private BluetoothAdapter btadapter;

    private mLeScanCallback leScanCallback;

    private DeviceListAdapter dlAdapter;

    private TextView hrmName, hrmAddress;

    private String HRM_NAME, HRM_MAC;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK) startSearching();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr_monitor_configuration);

        leScanCallback = new mLeScanCallback();

        ListView deviceList = (ListView) findViewById(R.id.lw_hrm);
        hrmName = (TextView) findViewById(R.id.t_hrm_name);
        hrmAddress = (TextView) findViewById(R.id.t_hrm_mac);
        Button confirmButton = (Button) findViewById(R.id.b_hrm_confirm);
        dlAdapter = new DeviceListAdapter();

        deviceList.setAdapter(dlAdapter);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("hrmName", HRM_NAME);
                intent.putExtra("hrmAddress", HRM_MAC);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        hrmName.setText(getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0).getString(SettingsActivity.SETTINGS_HRMONITOR_NAME, "Not set"));
        hrmAddress.setText(getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0).getString(SettingsActivity.SETTINGS_HRMONITOR_MAC, "Notset"));

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        btadapter = manager.getAdapter();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hr_monitor_configuration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_scan_hrm) {
            if (btadapter == null || !btadapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 2);
            } else startSearching();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startSearching() {
        if (Build.VERSION.SDK_INT < 21) startSearchingOld();
    }

    private void startSearchingOld() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btadapter.stopLeScan(leScanCallback);
            }
        }, 10000);
        btadapter.startLeScan(leScanCallback);
    }

    public void setDevice(final String name, final String mac) {
        HRM_NAME = name;
        HRM_MAC = mac;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hrmName.setText(name);
                hrmAddress.setText(mac);
            }
        });
    }

    private class DeviceListAdapter extends BaseAdapter {

        private List<BluetoothDevice> devices;

        public DeviceListAdapter() {
            devices = new ArrayList<>();
        }

        public void addDevice(BluetoothDevice device) {
            devices.add(device);
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.hrm_list, parent, false);
            TextView name = (TextView) convertView.findViewById(R.id.t_hrm_name);
            TextView address = (TextView) convertView.findViewById(R.id.t_hrm_mac);
            name.setText(devices.get(position).getName());
            address.setText(devices.get(position).getAddress());
            convertView.setBackgroundResource(R.drawable.bcg_frame);
            final int pos = position;
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDevice(devices.get(pos).getName(), devices.get(pos).getAddress());
                }
            });
            return convertView;
        }
    }

    private class mLeScanCallback implements BluetoothAdapter.LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            dlAdapter.addDevice(device);
            dlAdapter.notifyDataSetChanged();
        }
    }
}
