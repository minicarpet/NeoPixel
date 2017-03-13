package com.example.logangalloisext.Neopixel;

import android.content.DialogInterface;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.devadvance.ColorPicker.ColorCircle;

public class addFunctionActivity extends AppCompatActivity {

    MainActivity mainActivity;
    ColorCircle colorCircle;
    Spinner spinner;
    EditText delayText;

    String functionString[] = new String[]{
            "ColorWipe",
            "TheaterChase",
            "rainbowCycle"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_function);

        mainActivity = MainActivity.mainActivity;

        spinner = (Spinner) findViewById(R.id.spinnerFunction);
        colorCircle = (ColorCircle) findViewById(R.id.ColorCircleFunction);
        final Button validate = (Button) findViewById(R.id.validate);
        final Button cancel = (Button) findViewById(R.id.cancel);
        delayText = (EditText) findViewById(R.id.DelayText);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, functionString);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final SeekBar seekBar1 = (SeekBar) findViewById(R.id.luminosity);
        seekBar1.setProgress(seekBar1.getMax());
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (colorCircle != null) {
                    Drawable drawableProgress = seekBar.getProgressDrawable();
                    drawableProgress.setAlpha((int) (((float) progress / seekBar.getMax()) * 255.0));
                    colorCircle.setValue((float) progress / seekBar.getMax());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainActivity.allFunctions.ITEMS.size() < 255) {
                    try {
                        int value = Integer.parseInt(String.valueOf(delayText.getText()));
                        mainActivity.allFunctions.addItem(Integer.toString(mainActivity.allFunctions.ITEMS.size()), (String) spinner.getSelectedItem(), colorCircle.getColor(), value);
                        mainActivity.listAdapter.notifyDataSetChanged();
                        addFunctionActivity.this.finish();
                    } catch (NumberFormatException error) {
                        new AlertDialog.Builder(addFunctionActivity.this)
                                .setTitle("Error")
                                .setMessage("Please use number for delay")
                                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setIcon(android.R.drawable.btn_star)
                                .show();
                    }
                } else {
                    new AlertDialog.Builder(addFunctionActivity.this)
                            .setTitle("Error")
                            .setMessage("You already have the maximum function, please cancel and delete some if you want to change pattern")
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
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFunctionActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mainActivity.allFunctions.ITEMS.size() < 255) {
            try {
                int value = Integer.parseInt(String.valueOf(delayText.getText()));
                mainActivity.allFunctions.addItem(Integer.toString(mainActivity.allFunctions.ITEMS.size()), (String) spinner.getSelectedItem(), colorCircle.getColor(), value);
                mainActivity.listAdapter.notifyDataSetChanged();
                super.onBackPressed();
            } catch (NumberFormatException error) {
                new AlertDialog.Builder(addFunctionActivity.this)
                        .setTitle("Error")
                        .setMessage("Please use number for delay")
                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.btn_star)
                        .show();
            }
        } else {
            new AlertDialog.Builder(addFunctionActivity.this)
                    .setTitle("Error")
                    .setMessage("You already have the maximum function, please cancel and delete some if you want to change pattern")
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
}
