package bazulis.gpxtracker.trk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import java.util.UUID;

import bazulis.gpxtracker.trk.util.Config;

public class HeartRateMonitorService extends Service {
    private int serviceID = 19891021;
    private Notification.Builder nbuilder;
    private NotificationManager nmanager;
    //private BroadcastReceiver receiver;

    private BluetoothGatt mBluetoothGatt;

    public HeartRateMonitorService() {
        nmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nbuilder = new Notification.Builder(getApplicationContext());
        nbuilder.setSmallIcon(R.drawable.ic_heart_rate);
        nbuilder.setContentTitle("Heart Rate Monitor status");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.HRM_SERVICE_RECEIVER);
        registerReceiver(receiver, filter);
        BluetoothManager btmanager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter btadapter = btmanager.getAdapter();
        String address = getSharedPreferences(SettingsActivity.SETTINGS_NAME, 0).getString(SettingsActivity.SETTINGS_HRMONITOR_MAC, "none");
        if (!address.equals("none")) {
            mBluetoothGatt = btadapter.getRemoteDevice(address).connectGatt(getApplicationContext(), true, mGattCallback);
        } else {
            Toast.makeText(getApplicationContext(), "Adress of the HRM is not set.", Toast.LENGTH_SHORT).show();
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("stop", false)) {
                stopForeground(true);
                stopSelf();
            }
        }
    };

    BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState) {
                case BluetoothGatt.STATE_CONNECTING : {
                    nbuilder.setContentText("Connecting...");
                    nmanager.notify(serviceID, nbuilder.build());
                    break;
                }
                case BluetoothGatt.STATE_CONNECTED : {
                    gatt.discoverServices();
                    nbuilder.setContentText("Connected, discovering services...");
                    nmanager.notify(serviceID, nbuilder.build());
                    break;
                }
                case BluetoothGatt.STATE_DISCONNECTING : {
                    nbuilder.setContentText("Disconnecting...");
                    nmanager.notify(serviceID, nbuilder.build());
                    break;
                }
                case BluetoothGatt.STATE_DISCONNECTED : {
                    nbuilder.setContentText("Disconnected.");
                    nmanager.notify(serviceID, nbuilder.build());
                    mBluetoothGatt.close();
                    break;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            nbuilder.setContentText("Services discovered, getting data...");
            nmanager.notify(serviceID, nbuilder.build());
            BluetoothGattService service = gatt.getService(UUID.fromString(Config.UUID_HR_SERVICE));
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(Config.UUID_HR_CHARACTERISTIC));
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(Config.UUID_HR_DESCRIPTOR));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
            gatt.setCharacteristicNotification(characteristic, true);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            int flag = characteristic.getProperties();
            int format;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
            }
            int heartrate = characteristic.getIntValue(format, 1);

        }
    };
}
