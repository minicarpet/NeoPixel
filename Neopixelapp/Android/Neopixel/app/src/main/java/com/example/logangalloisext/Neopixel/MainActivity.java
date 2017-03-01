package com.example.logangalloisext.Neopixel;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.devadvance.ColorPicker.ColorCircle;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter BA;
    private BluetoothLeScanner bs;
    private BluetoothGatt mGatt;
    private BluetoothDevice bd;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic txCarac;

    private EditText editText;
    private TextView receiveText;
    private ColorCircle colorCircle;

    private static final int RequestLocationId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA != null)
        {
            BA.enable();
            bs = BA.getBluetoothLeScanner();
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        RequestLocationId);
            }
        } else {
            if (bs != null)
                bs.startScan(mScanCallback);
        }

        editText = (EditText) findViewById(R.id.editText);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });

        Button button = (Button) findViewById(R.id.send);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(editText.getText().toString());
            }
        });

        colorCircle = (ColorCircle)  findViewById(R.id.ColorCircle1);
        colorCircle.setOnColorPickerListener(new ColorCircle.OnColorCircleListener() {
            @Override
            public void onRelease(View view, int color, int luminosity) {
                Log.d("Main Red", Integer.toString(Color.red(color)));
                Log.d("Main Green", Integer.toString(Color.green(color)));
                Log.d("Main Blue", Integer.toString(Color.blue(color)));
                Log.d("Main Luminosity", Integer.toString(luminosity));
            }
        });

        SeekBar seekBar1 = (SeekBar)  findViewById(R.id.seekBarValue);
        seekBar1.setProgress(seekBar1.getMax());
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(colorCircle != null) {
                    colorCircle.setValue((float) progress / seekBar.getMax());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(colorCircle != null) {
                    Log.d("Main Red", Integer.toString(Color.red(colorCircle.getColor())));
                    Log.d("Main Green", Integer.toString(Color.green(colorCircle.getColor())));
                    Log.d("Main Blue", Integer.toString(Color.blue(colorCircle.getColor())));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case RequestLocationId: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (bs != null)
                        bs.startScan(mScanCallback);
                } else {
                    // permission denied
                }
                return;
            }
        }
    }

    private void sendData(String dataToSend) {
        if (txCarac != null) {
            txCarac.setValue(dataToSend);
            mGatt.writeCharacteristic(txCarac);
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);

            if (result != null) {
                if (result.getDevice() != null) {
                    Log.d("yolo", result.getDevice().getName().toString());
                    if (result.getDevice().getName() != null) {
                        if (result.getDevice().getName().equals(new String("OrbeLED"))) { /* Should be the name of device wanted */
                            bd = result.getDevice();
                            connectTo(bd);
                        }
                    }
                }
            }
        }
    };

    private void connectTo(BluetoothDevice device) {
        if(mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            bs.stopScan(mScanCallback); /* Once device connected, stop scan for save battery */
        }
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            /* See https://developer.android.com/reference/android/bluetooth/BluetoothProfile.html#STATE_CONNECTED for value */
            if (newState == BluetoothProfile.STATE_CONNECTED) { /* If connected */
                mGatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            for (int i=0 ; i<gatt.getServices().size() ; i++) {
                if (gatt.getServices().get(i).getUuid().toString().equals("19b10000-e8f2-537e-4f6c-d104768a1214")) {
                    service = gatt.getServices().get(i);
                }
            }
            if (service != null) {
                for (int i=0 ; i<service.getCharacteristics().size() ; i++) {
                    if ((service.getCharacteristics().get(i).getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0) {
                        txCarac = service.getCharacteristics().get(i);
                    } else if ((service.getCharacteristics().get(i).getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        mGatt.setCharacteristicNotification(service.getCharacteristics().get(i), true);
                    }
                }
            } else {
                //ERROR SERVICE NOT FOUND, CHECK ARDUINO PROGRAM AND GET SERVICES
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            receiveText.setText(characteristic.getValue().toString());
        }
    };

    @Override
    protected void onDestroy() {
        if(mGatt != null)
            mGatt.close();
        if (BA != null) {
            if (BA.isDiscovering())
                bs.stopScan(mScanCallback);
        }
        BA = null;
        bs = null;
        bd = null;
        service = null;
        txCarac = null;
        mGatt = null;
        super.onDestroy();
    }
}
