package com.example.codeexp2021app2.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.example.codeexp2021app2.R;
import com.example.codeexp2021app2.listener.BluetoothListener;
import com.example.codeexp2021app2.utils.ContextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothHelper {

    private final static String TAG = "BluetoothHelper";
    private final static UUID BT_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");
    private final static int MAX_ERROR_TIMES = 3;

    private static final int STATE_LISTENING = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_CONNECTED = 3;
    private static final int STATE_DISCONNECTED = 4;
    private static final int STATE_CONNECTION_FAILED = 5;
    private static final int STATE_MESSAGE_RECEIVED = 6;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothListener bluetoothListener;
    private BluetoothDevice bluetoothDevice;

    private Client client;
    private Server server;
    private SendReceive sendReceive;

    private int countReadError = 0;
    // flag = 0 => client; flag = 1 => server;
    private int flag = 0;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case STATE_LISTENING:
                    if (bluetoothListener != null) {
                        bluetoothListener.onListening();
                    }
                    break;
                case STATE_CONNECTING:
                    if (bluetoothListener != null) {
                        bluetoothListener.onConnecting();
                    }
                    break;
                case STATE_CONNECTED:
                    if (bluetoothListener != null) {
                        bluetoothListener.onConnected();
                    }
                    break;
                case STATE_DISCONNECTED:
                    if (bluetoothListener != null) {
                        bluetoothListener.onDisconnected();
                    }
                    break;
                case STATE_CONNECTION_FAILED:
                    if (bluetoothListener != null) {
                        bluetoothListener.onConnectionFailed();
                    }
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    if (bluetoothListener != null) {
                        bluetoothListener.onMessageReceived(tempMsg);
                    }
                    break;
            }
            return true;
        }
    });

    private BluetoothHelper() {
    }

    private static class SingleInstance {
        private static BluetoothHelper INSTANCE = new BluetoothHelper();
    }

    public static BluetoothHelper getInstance() {
        return SingleInstance.INSTANCE;
    }

    public void init(BluetoothListener bluetoothListener) {
        this.bluetoothListener = bluetoothListener;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void release() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        if (sendReceive != null && !sendReceive.isInterrupted()) {
            sendReceive.interrupt();
            sendReceive = null;
        }
        if (client != null && !client.isInterrupted()) {
            client.interrupt();
            client = null;
        }
        if (server != null && !server.isInterrupted()) {
            server.interrupt();
            server = null;
        }
        bluetoothListener = null;
        bluetoothAdapter = null;
    }

    public boolean bluetoothState() {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.isEnabled();
        }
        return false;
    }

    public List<BluetoothDevice> scan() {
        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> bluetoothDevices = new ArrayList<>(bt.size());
        if (bt.size() > 0) {
            bluetoothDevices.addAll(bt);
        }
        return bluetoothDevices;
    }

    public void connect(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
        client = new Client(this.bluetoothDevice);
        client.start();
        Message message = Message.obtain();
        message.what = STATE_CONNECTING;
        handler.sendMessage(message);
        flag = 0;
    }

    public void disconnect() {
        if (sendReceive != null && !sendReceive.isInterrupted()) {
            sendReceive.interrupt();
            sendReceive = null;
        }
        if (client != null && !client.isInterrupted()) {
            client.interrupt();
            client = null;
        }
        Message message = Message.obtain();
        message.what = STATE_DISCONNECTED;
        handler.sendMessage(message);
    }

    public void send(String message) {
        if (sendReceive != null && !sendReceive.isInterrupted()) {
            String string = String.valueOf(message);
            sendReceive.write(string.getBytes());
        }
    }

    public void listen() {
        Server server = new Server();
        server.start();
        flag = 1;
    }

    public void refuse() {
        if (sendReceive != null && !sendReceive.isInterrupted()) {
            sendReceive.interrupt();
            sendReceive = null;
        }
        if (server != null && !server.isInterrupted()) {
            server.interrupt();
            server = null;
        }
    }

    private class Client extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public Client(BluetoothDevice device) {
            this.device = device;
            try {
                socket = this.device.createRfcommSocketToServiceRecord(BT_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run() {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Message message=Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendReceive extends Thread {

        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;
            try {
                tempIn = this.socket.getInputStream();
                tempOut = this.socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes,-1, buffer).sendToTarget();
                    countReadError = 0;
                } catch (IOException e) {
                    e.printStackTrace();
                    countReadError++;
                }
                if (countReadError >= MAX_ERROR_TIMES) {
                    break;
                }
            }
            countReadError = 0;
            if (flag == 0) {
                disconnect();
                if (bluetoothDevice != null) {
                    connect(bluetoothDevice);
                }
            } else {
                refuse();
                listen();
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Server extends Thread {

        private BluetoothServerSocket serverSocket;

        public Server() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        ContextUtils.getContext().getResources().getString(R.string.app_name), BT_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket = null;
            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                    socket = null;
                }
                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                    break;
                }
            }
        }
    }
}
