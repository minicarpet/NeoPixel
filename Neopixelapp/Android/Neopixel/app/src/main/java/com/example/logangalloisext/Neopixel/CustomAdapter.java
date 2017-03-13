package com.example.logangalloisext.Neopixel;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.Function.Functions;

import java.util.List;

/**
 * Created by Minicarpet on 02/03/2017.
 */

public class CustomAdapter extends ArrayAdapter {

    private final List<Functions.Function> mValues;
    public MainActivity mainActivity;
    public Functions allFunctionAdapter;

    public CustomAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        mValues = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.simplerow, null);
        }
        Functions.Function function = mValues.get(position);
        if (view != null)
            view.setBackgroundColor(function.color);
        TextView textViewFunction = (TextView) view.findViewById(R.id.rowTextViewFunction);
        textViewFunction.setText(function.title);
        if (function.color != Color.BLACK) {
            textViewFunction.setTextColor(Color.BLACK);
        } else {
            textViewFunction.setTextColor(Color.WHITE);
        }

        TextView textViewDelay = (TextView) view.findViewById(R.id.rowTextViewDelay);
        textViewDelay.setText("avec un delai de : " + Integer.toString(function.delay));
        if (function.color != Color.BLACK) {
            textViewDelay.setTextColor(Color.BLACK);
        } else {
            textViewDelay.setTextColor(Color.WHITE);
        }

        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        if (allFunctionAdapter.isEdit) {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setChecked(false);
        } else {
            checkBox.setVisibility(View.GONE);
            if (checkBox.isChecked()) {
                checkBox.setChecked(false);
                function.willBeDeleted = true;
            }
        }

        if (position == allFunctionAdapter.ITEMS.size() - 1) {
            mainActivity.checkFunctionsToDelete();
        }

        return view;
    }
}
