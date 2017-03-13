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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.Function.Functions;
import com.devadvance.ColorPicker.ColorCircle;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int RequestLocationId = 0;
    public static MainActivity mainActivity;
    public CustomAdapter listAdapter;
    public ListView listViewFunction;
    public Functions allFunctions = new Functions();
    private BluetoothAdapter BA;
    private BluetoothLeScanner bs;
    private BluetoothGatt mGatt;
    private BluetoothDevice bd;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic txCarac;
    private EditText editText;
    private TextView receiveText;
    private ColorCircle colorCircle;
    private boolean listFunctionEdit = false;
    private int buffer[] = {0,0,0,0,0};
    private int numData = 0;
    private int numFunction;
    private boolean sync = false;
    private String names[] = {
            "ColorWipe",
            "TheaterChase",
            "rainbowCycle"
    };
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) { /* If connected */
                mGatt.discoverServices();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Connected")
                        .setMessage("Arduino successfully connected")
                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.btn_star)
                        .show();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Conncetion lost")
                        .setMessage("Arduino disconnected")
                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.btn_star)
                        .show();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            for (int i = 0; i < gatt.getServices().size(); i++) {
                if (gatt.getServices().get(i).getUuid().toString().equals("19b10000-e8f2-537e-4f6c-d104768a1214")) {
                    service = gatt.getServices().get(i);
                }
            }
            if (service != null) {
                for (int i = 0; i < service.getCharacteristics().size(); i++) {
                    if ((service.getCharacteristics().get(i).getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0) {
                        txCarac = service.getCharacteristics().get(i);
                    } else if ((service.getCharacteristics().get(i).getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        mGatt.setCharacteristicNotification(service.getCharacteristics().get(i), true);
                    }
                }
            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Error")
                        .setMessage("Services not found")
                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.btn_star)
                        .show();
                //ERROR SERVICE NOT FOUND, CHECK ARDUINO PROGRAM AND GET SERVICES
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("Error BLE", "Error number : " + Integer.toString(status));
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Error")
                        .setMessage("Error number : " + Integer.toString(status))
                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.btn_star)
                        .show();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("Error BLE", "Error number : " + Integer.toString(status));
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Error")
                        .setMessage("Error number : " + Integer.toString(status))
                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.btn_star)
                        .show();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            receiveText.setText(characteristic.getValue().toString());
            /** \todo: add sync user information */
            int value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            buffer[0] = value;
            numData++;
            if (numData == 5) {
                numData = 0;
                if (buffer[0] == 0 && buffer[1] == 1 && buffer[2] == 0 && buffer[3] == 0 && buffer[4] != 0) {
                    numFunction = buffer[4];
                } else {
                    int actualColor = buffer[1];
                    actualColor += buffer[2];
                    actualColor += buffer[3];
                    allFunctions.addItem(Integer.toString(
                            allFunctions.ITEMS.size()),
                            names[buffer[0]],
                            actualColor,
                            buffer[4]);
                }
            }
        }
    };
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);

            if (result != null) {
                if (result.getDevice() != null) {
                    if (result.getDevice().getName() != null) {
                        if (result.getDevice().getName().equals(new String("NeoPixel"))) { /* Should be the name of device wanted */
                            bd = result.getDevice();
                            connectTo(bd);
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;

        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA != null) {
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

        receiveText = (TextView) findViewById(R.id.datareceive);
        final SeekBar seekBar1 = (SeekBar) findViewById(R.id.seekBarValue);
        colorCircle = (ColorCircle) findViewById(R.id.ColorCircle1);
        Button sendSpecificDataButton = (Button) findViewById(R.id.send);
        Button sendColorButton = (Button) findViewById(R.id.sendColor);

        Drawable drawable = seekBar1.getProgressDrawable();
        drawable.setColorFilter(new LightingColorFilter(0xFF000000, colorCircle.getColor()));

        sendSpecificDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(editText.getText().toString());
            }
        });

        colorCircle.setOnColorPickerListener(new ColorCircle.OnColorCircleListener() {
            @Override
            public void onRelease(View view, int color, int luminosity) {

            }

            @Override
            public void onValueChanged(View view, int color, int luminosity) {
                Drawable drawableProgress = seekBar1.getProgressDrawable();
                Drawable drawablePointer = seekBar1.getThumb();
                drawableProgress.setColorFilter(new LightingColorFilter(0xFF000000, color));
                drawablePointer.setColorFilter(new LightingColorFilter(0xFF000000, color));
                drawableProgress.setAlpha(luminosity);
            }
        });

        seekBar1.setProgress(seekBar1.getMax());
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (colorCircle != null) {
                    Drawable drawableProgress = seekBar.getProgressDrawable();
                    drawableProgress.setAlpha((int) (((float) progress / seekBar.getMax()) * 255.0));
                    colorCircle.setLuminosity((float) progress / seekBar.getMax());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("Color");
        tabSpec.setContent(R.id.tab1);
        tabSpec.setIndicator("Color");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("Programming");
        tabSpec.setContent(R.id.tab2);
        tabSpec.setIndicator("Programming");
        tabHost.addTab(tabSpec);

        listViewFunction = (ListView) findViewById(R.id.listView);
        listAdapter = new CustomAdapter(this.getBaseContext(), R.layout.simplerow, allFunctions.ITEMS);
        listAdapter.allFunctionAdapter = allFunctions;
        listAdapter.mainActivity = this;
        final LayoutInflater factory = getLayoutInflater();
        final View textEntryView = factory.inflate(R.layout.header_view, null);
        listViewFunction.addHeaderView(textEntryView);
        listViewFunction.setAdapter(listAdapter);

        Button addFunction = (Button) findViewById(R.id.addButton);
        Button syncFunctionsButton = (Button) findViewById(R.id.syncButton);
        Button editFunction = (Button) findViewById(R.id.editButton);

        addFunction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, addFunctionActivity.class);

                startActivity(intent);
            }
        });

        editFunction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listFunctionEdit = !listFunctionEdit;
                allFunctions.isEdit = listFunctionEdit;
                listAdapter.notifyDataSetChanged();
                if (listFunctionEdit) {
                    listViewFunction.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                } else {
                    listViewFunction.setChoiceMode(ListView.CHOICE_MODE_NONE);
                }
            }
        });

        sendColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("StartColor");
                sendData(Integer.toString(Color.red(colorCircle.getColor())));
                sendData(Integer.toString(Color.green(colorCircle.getColor())));
                sendData(Integer.toString(Color.blue(colorCircle.getColor())));
                sendData(Integer.toString((int) colorCircle.getLuminosity() * 200));
            }
        });

        syncFunctionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("Start");
                for (Functions.Function function:allFunctions.ITEMS) {
                    sendData(Integer.toString(Arrays.asList(names).indexOf(function.title)));
                    sendData(Integer.toString(Color.red(function.color)));
                    sendData(Integer.toString(Color.green(function.color)));
                    sendData(Integer.toString(Color.blue(function.color)));
                    sendData(Integer.toString(function.delay));
                }
                sendData("End");
            }
        });
    }

    public void checkFunctionsToDelete() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < allFunctions.ITEMS.size(); i++) {
                    if (allFunctions.ITEMS.get(i).willBeDeleted) {
                        allFunctions.deleteItem(i);
                        listAdapter.notifyDataSetChanged();
                    }
                }
            }
        }, 10);
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

    private void connectTo(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            bs.stopScan(mScanCallback); /* Once device connected, stop scan for save battery */
        }
    }

    @Override
    protected void onDestroy() {
        if (mGatt != null)
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
