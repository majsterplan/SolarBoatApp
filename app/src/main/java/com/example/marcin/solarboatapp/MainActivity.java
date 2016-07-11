package com.example.marcin.solarboatapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private final int BLUETOOTH_REQUEST_CODE = 1;

    private Context appContext;
    private BluetoothService bluetoothService;
    private GpsService gpsService;
    private BoatStatus boatStatus;

    private BroadcastReceiver bluetoothServiceStatusReceiver;
    private BroadcastReceiver gpsServiceStatusReceiver;
    private BroadcastReceiver discoveringProcessReceiver;
    private BroadcastReceiver connectionStatusReceiver;
    private BroadcastReceiver uiChangesReceiver;

    private ListView logsList;
    private List<String> logs;

    private TextView speedValue;
    private TextView rotationalSpeedValue;
    private TextView voltageValue;
    private TextView currentValue;
    private TextView temperatureValue;
    private TextView temperatureValue2;
    private TextView temperatureValue3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appContext = getApplicationContext();

        speedValue = (TextView) findViewById(R.id.speed_1);
        rotationalSpeedValue = (TextView) findViewById(R.id.speed_2);
        voltageValue = (TextView) findViewById(R.id.voltage_1);
        currentValue = (TextView) findViewById(R.id.current_1);
        temperatureValue = (TextView) findViewById(R.id.temperature_1);
        temperatureValue2 = (TextView) findViewById(R.id.temperature_2);
        temperatureValue3 = (TextView) findViewById(R.id.temperature_3);

        logs = new ArrayList<String>();
        logsList = (ListView) findViewById(R.id.logs);

        displayLog("Start aplikacji.");

        bluetoothServiceStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (BluetoothAdapter.STATE_ON == state) {
                        displayLog("Bluetooth został włączony.");
                        bluetoothService.setStatus(true);
                        bluetoothService.discoverMeasuringUnitToConnect();
                    } else if (BluetoothAdapter.STATE_OFF == state) {
                        displayLog("Bluetooth został wyłączony.");
                        bluetoothService.setStatus(false);
                        enableBluetoothService();
                    }
                }
            }
        };

        gpsServiceStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("GPS_STATE_CHANGED")) {
                    int state = intent.getIntExtra("state", GpsService.GPS_ERROR);
                    if (GpsService.GPS_ON == state) {
                        displayLog("GPS został włączony");
                        gpsService.setStatus(true);
                    } else if (GpsService.GPS_OFF == state) {
                        displayLog("GPS został wyłączony");
                        gpsService.setStatus(false);
                        enableGPSservice();
                    }
                }
            }
        };

        discoveringProcessReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                    displayLog("Szukam jednostki pomiarowej.");
                } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getName().equals(bluetoothService.getMeasuringUnitName())) {
                        displayLog("Jednostka pomiarowa odnaleziona.");
                        bluetoothService.setMeasuringUnit(device);
                        bluetoothService.getAdapter().cancelDiscovery();
                    }
                } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                    if (null == bluetoothService.getMeasuringUnit()) {
                        displayLog("Nie odnalazłem jednostki pomiarowej. Szukam ponownie.");
                        bluetoothService.getAdapter().startDiscovery();
                    } else {
                        displayLog("Nawiązuje połączenie z jednostką pomiarową.");
                        bluetoothService.connectMeasuringUnit();
                    }
                }
            }
        };

        connectionStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("CONNECTION_PROBLEM")) {
                    displayLog(intent.getStringExtra("message"));
                    displayLog("Czyszczę połączenie.");
                    bluetoothService.clearConnection();
                    bluetoothService.getAdapter().startDiscovery();
                } else if (action.equals("CONNECTION_SUCCESS")) {
                    displayLog(intent.getStringExtra("message"));
                }
            }
        };

        uiChangesReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("SHOW_MEASUREMENT")) {
                    Set<String> keys = intent.getExtras().keySet();
                    for (String key :
                            keys) {
                        if (intent.getStringExtra("1.1") != null) {
                            speedValue.setText(intent.getStringExtra("1.1"));
                            break;
                        } else if (intent.getStringExtra("2.1") != null) {
                            rotationalSpeedValue.setText(intent.getStringExtra("2.1"));
                            break;
                        } else if (intent.getStringExtra("3.1") != null) {
                            currentValue.setText(intent.getStringExtra("3.1"));
                            break;
                        } else if (intent.getStringExtra("4.1") != null) {
                            voltageValue.setText(intent.getStringExtra("4.1"));
                            break;
                        } else if (intent.getStringExtra("5.1") != null) {
                            temperatureValue.setText(intent.getStringExtra("5.1"));
                            break;
                        } else if (intent.getStringExtra("5.2") != null) {
                            temperatureValue2.setText(intent.getStringExtra("5.2"));
                            break;
                        } else if (intent.getStringExtra("5.3") != null) {
                            temperatureValue3.setText(intent.getStringExtra("5.3"));
                            break;
                        } else if (intent.getStringExtra(key) != null) {
                            displayLog("Kategoria.numer: " + key + ", wartość: " + intent.getStringExtra(key) + ".");
                        }
                    }
                } else if (action.equals("SHOW_LOG")) {
                    displayLog(intent.getStringExtra("invalidFrame"));
                }
            }
        };

        registerReceiver(bluetoothServiceStatusReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(gpsServiceStatusReceiver, new IntentFilter("GPS_STATE_CHANGED"));
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter1.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter1.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveringProcessReceiver, filter1);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("CONNECTION_PROBLEM");
        filter2.addAction("CONNECTION_SUCCESS");
        registerReceiver(connectionStatusReceiver, filter2);
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction("SHOW_MEASUREMENT");
        filter3.addAction("SHOW_LOG");
        registerReceiver(uiChangesReceiver, filter3);

        boatStatus = new BoatStatus(appContext);

        bluetoothService = new BluetoothService(appContext);
        Service.StartUpStatus BluetoothStartUpStatus = bluetoothService.start();
        if (Service.StartUpStatus.NO_SUPPORT == BluetoothStartUpStatus) {
            displayLog("Urządzenie nie wspiera Bluetootha.");
        }
        if (Service.StartUpStatus.NEED_USER_INTERACTION == BluetoothStartUpStatus) {
            enableBluetoothService();
        }
        if (Service.StartUpStatus.ON == BluetoothStartUpStatus || bluetoothService.getStatus()) {
            displayLog("Bluetooth włączony.");
            bluetoothService.discoverMeasuringUnitToConnect();
        }

        gpsService = new GpsService(appContext);
        Service.StartUpStatus gpsStartUpStatus = gpsService.start();
        if (Service.StartUpStatus.NO_SUPPORT == gpsStartUpStatus) {
            displayLog("Urządzenie nie wspiera GPS");
        }
        if (Service.StartUpStatus.NEED_USER_INTERACTION == gpsStartUpStatus) {
            enableGPSservice();
        }
        if (Service.StartUpStatus.ON == gpsStartUpStatus) {
            displayLog("GPS włączony.");
        }
    }

    public void displayLog(final String text) {
        logs.add(0, DateFormat.getDateTimeInstance().format(new Date()) + " - " + text);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, logs) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);
                return view;
            }
        };
        logsList.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bluetoothService.clearConnection();

        unregisterReceiver(bluetoothServiceStatusReceiver);
        unregisterReceiver(discoveringProcessReceiver);
        unregisterReceiver(connectionStatusReceiver);
        unregisterReceiver(uiChangesReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (BLUETOOTH_REQUEST_CODE == requestCode) {
            if (resultCode == Activity.RESULT_CANCELED) {
                enableBluetoothService();
            }
        }
    }

    public void enableBluetoothService() {
        if (!bluetoothService.getStatus()) {
            displayLog("Uruchamiam Bluetooth.");
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, BLUETOOTH_REQUEST_CODE);
        }
    }

    public void enableGPSservice() {
        if (!gpsService.getStatus()) {
            displayLog("Uruchamiam GPS.");
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this, 4);
            alert.setMessage("Aplikacja wymaga usług lokalizacyjnych.");
            alert.setPositiveButton("Ustawienia", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent enableGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(enableGPS);
                }
            });
            alert.setNegativeButton("Odmów", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    enableGPSservice();
                }
            });
            alert.show();
        }
    }
}