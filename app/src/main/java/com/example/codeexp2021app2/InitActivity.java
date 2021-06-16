package com.example.codeexp2021app2;

import android.content.Intent;
import android.os.Bundle;

import com.example.codeexp2021app2.utils.PermissionsUtils;

import pub.devrel.easypermissions.AfterPermissionGranted;

public class InitActivity extends BaseActivity {

    private static final int REC_PERMISSION = 100;
    private static final String[] PERMISSIONS = new String[]{
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.CHANGE_NETWORK_STATE,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.CHANGE_WIFI_STATE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        requestPermission();
    }

    @AfterPermissionGranted(REC_PERMISSION)
    private void requestPermission() {
        PermissionsUtils.doSomeThingWithPermission(this, () -> {
            Intent intent = new Intent(InitActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, PERMISSIONS, REC_PERMISSION, R.string.rationale_init);
    }
}