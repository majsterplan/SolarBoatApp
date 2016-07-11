package com.example.marcin.solarboatapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * BluetoothService Class created by Marcin on 2016-04-01.
 */
public class BluetoothService extends Service {
    private Context appContext;
    private BluetoothAdapter adapter;
    private String measuringUnitName = "wut_sb_measurements";
    private BluetoothDevice measuringUnit;
    private ConnectThread connectThread;
    private ConnectionThread connectionThread;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothService(Context context) {
        appContext = context;
    }

    //@Override
    public StartUpStatus start() {
        StartUpStatus startUpStatus;
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            status = false;
            startUpStatus = StartUpStatus.NO_SUPPORT;
        } else if (!adapter.isEnabled()) {
            status = false;
            startUpStatus = StartUpStatus.NEED_USER_INTERACTION;
        } else {
            status = true;
            startUpStatus = StartUpStatus.ON;
        }
        return startUpStatus;
    }

    public void discoverMeasuringUnitToConnect() {
        adapter.startDiscovery();
    }

    public void connectMeasuringUnit() {
        connectThread = new ConnectThread(measuringUnit);
        connectThread.start();
    }

    public void clearConnection() {
        if (connectionThread != null) {
            connectionThread.cancel();
            connectionThread = null;
        }
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (measuringUnit != null) {
            measuringUnit = null;
        }
    }

    public void manageMeasuringUnit(BluetoothSocket socket) {
        connectionThread = new ConnectionThread(socket);
        connectionThread.start();
    }

    public BluetoothAdapter getAdapter() {
        return adapter;
    }

    public String getMeasuringUnitName() {
        return measuringUnitName;
    }

    public void setMeasuringUnit(BluetoothDevice device) {
        measuringUnit = device;
    }

    public BluetoothDevice getMeasuringUnit() {
        return measuringUnit;
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice myDevice) {
            BluetoothSocket tmp = null;
            device = myDevice;

            try {
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                //e.printStackTrace();
            }

            socket = tmp;
        }

        public void run() {
            try {
                socket.connect();
            } catch (IOException e) {
                //e.printStackTrace();
                Intent intent = new Intent();
                intent.setAction("CONNECTION_PROBLEM");
                intent.putExtra("message", "Problem z połączeniem.");
                appContext.sendBroadcast(intent);
            }

            if (socket.isConnected()) {
                Intent intent = new Intent();
                intent.setAction("CONNECTION_SUCCESS");
                intent.putExtra("message", "Nawiązano połączenie.");
                appContext.sendBroadcast(intent);
                manageMeasuringUnit(socket);
            }
        }

        public void cancel() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    private class ConnectionThread extends Thread {
        private boolean isLoop = false;
        private BluetoothSocket socket;
        private InputStream inputStream;

        public ConnectionThread(BluetoothSocket mySocket) {
            socket = mySocket;
            InputStream tmp = null;

            try {
                tmp = this.socket.getInputStream();
            } catch (IOException e) {
                //e.printStackTrace();
            }

            inputStream = tmp;
            isLoop = true;
        }

        public void run() {
            byte[] buffer = new byte[128];
            int newBytes = 0;
            while (isLoop) {
                try {
                    newBytes += inputStream.read(buffer, newBytes, buffer.length - newBytes);
                    int lastBegin = 0;
                    for (int i = 0; i < newBytes; i++) {
                        if (buffer[i] == Frame.FRAME_CLOSE.getBytes()[0]) {
                            String frame = new String(buffer, lastBegin, i + 1 - lastBegin);
                            if (i + 1 < newBytes) {
                                lastBegin = i + 1;
                            }
                            Intent intent = new Intent();
                            intent.setAction("INCOMING_FRAME");
                            intent.putExtra("frame", frame);
                            appContext.sendBroadcast(intent);
                        }
                        if (i == newBytes - 1) {
                            byte[] tmp = buffer;
                            buffer = null;
                            buffer = new byte[128];
                            System.arraycopy(tmp, lastBegin, buffer, 0, i + 1 - lastBegin);
                            newBytes = i + 1 - lastBegin;
                        }
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                    isLoop = false;
                    Intent intent = new Intent();
                    intent.setAction("CONNECTION_PROBLEM");
                    intent.putExtra("message", "Problem z połączeniem.");
                    appContext.sendBroadcast(intent);
                }
            }
        }

        public void cancel() {
            if (isLoop) {
                isLoop = false;
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }

            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }
    }
}
