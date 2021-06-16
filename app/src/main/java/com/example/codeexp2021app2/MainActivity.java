package com.example.codeexp2021app2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.codeexp2021app2.bluetooth.BluetoothHelper;
import com.example.codeexp2021app2.listener.BluetoothListener;
import com.example.codeexp2021app2.utils.ToastUtils;

public class MainActivity extends AppCompatActivity implements BluetoothListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView tvSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvSubtitle = (TextView) findViewById(R.id.tv_subtitle);
        BluetoothHelper.getInstance().init(MainActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (BluetoothHelper.getInstance().bluetoothState()) {
            BluetoothHelper.getInstance().listen();
        }
    }

    @Override
    protected void onDestroy() {
        BluetoothHelper.getInstance().release();
        super.onDestroy();
    }

    @Override
    public void onListening() {
        Log.i(TAG, "onListening");
        ToastUtils.showShortSafe("Bluetooth Listening...");
    }

    @Override
    public void onConnecting() {
        Log.i(TAG, "onConnecting");
        ToastUtils.showShortSafe("Bluetooth Connecting...");
    }

    @Override
    public void onConnected() {
        Log.i(TAG, "onConnected");
        ToastUtils.showShortSafe("Bluetooth Connected...");
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "onDisconnected");
        ToastUtils.showShortSafe("Bluetooth Disconnected...");
    }

    @Override
    public void onConnectionFailed() {
        Log.i(TAG, "onConnectionFailed");
        ToastUtils.showShortSafe("Bluetooth Connection Failed...");
    }

    @Override
    public void onMessageReceived(String message) {
        Log.i(TAG, "onMessageReceived " + message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvSubtitle.setText(message);
            }
        });
    }
}

