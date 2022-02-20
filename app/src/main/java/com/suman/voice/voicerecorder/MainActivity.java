package com.suman.voice.voicerecorder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import suman.brose.voicerecorder.R;

/**
 * Created by hp on 4/1/2020.
 */

public class MainActivity extends AppCompatActivity {
    SharedPreferences sf;
    final String preferences = "pref";
    final String saveIt = "saveName";
    final String saveIt1 = "saveAge";
    final String saveIt2 = "saveSex";
    final String saveIt3 = "saveHeigt";
    final String saveIt4 = "saveWeight";
    String Name = "";
    String Age = "";
    String Sex = "";
    String Height = "";
    String Weight = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // getting attached intent data
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_info);
        final EditText et = (EditText)findViewById(R.id.Name);

        final EditText et1 = (EditText)findViewById(R.id.Age);
        et1.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "150")}); // Range

        final Spinner genderSpinner = findViewById(R.id.spinnerGender);
        final Spinner smokingSpinner = findViewById(R.id.smokingSpinner);
        final Spinner chestSpinner = findViewById(R.id.chestSpinner);

        final EditText et3 = (EditText)findViewById(R.id.Height);
        et3.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "220")}); // Range

        final EditText et4 = (EditText)findViewById(R.id.Weight);
        et4.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "300")}); // Range



        Button button = (Button)findViewById(R.id.button);

        sf = getSharedPreferences(preferences, Context.MODE_PRIVATE);
        Log.d("flowRates","Login"+String.valueOf(sf.getString(saveIt,"")));


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(et.getText().toString().equalsIgnoreCase("") ||
                        et1.getText().toString().equalsIgnoreCase("") ||
                        genderSpinner.getSelectedItem().toString().equalsIgnoreCase("") ||
                        et3.getText().toString().equalsIgnoreCase("") ||
                        et4.getText().toString().equalsIgnoreCase("") ||
                        smokingSpinner.getSelectedItem().toString().equalsIgnoreCase("") ||
                        chestSpinner.getSelectedItem().toString().equalsIgnoreCase(""))
                {

                    //age
                    if(et1.getText().toString().equalsIgnoreCase("")) {
                        et1.requestFocus();
                        et1.setError(getString(R.string.this_field_required));
                        et1.setFocusable(true);
                        et1.setFocusableInTouchMode(true);
                    }
                    else {
                        et1.setError(null);
                    }

                    //gender
                    if(genderSpinner.getSelectedItemPosition() == 0) {
                        TextView errorText = (TextView)genderSpinner.getSelectedView();
                        errorText.setError("");
                        errorText.setTextColor(Color.RED);//just to highlight that this is an error
                    }


                    //height
                    if(et3.getText().toString().equalsIgnoreCase("")) {
                        et3.requestFocus();
                        et3.setError(getString(R.string.this_field_required));
                        et3.setFocusable(true);
                        et3.setFocusableInTouchMode(true);
                    }
                    else {
                        et3.setError(null);
                    }

                    //weight
                    if(et4.getText().toString().equalsIgnoreCase("")) {
                        et4.requestFocus();
                        et4.setError(getString(R.string.this_field_required));
                        et4.setFocusable(true);
                        et4.setFocusableInTouchMode(true);
                    }
                    else {
                        et4.setError(null);
                    }

                    //name
                    if(et.getText().toString().equalsIgnoreCase("")) {
                        et.setError(getString(R.string.this_field_required));
                        et.requestFocus();
                        et.setFocusable(true);
                        et.setFocusableInTouchMode(true);
                    }
                    else {
                        et.setError(null);
                    }

                    //smoking
                    if(smokingSpinner.getSelectedItemPosition() == 0) {
                        TextView errorText = (TextView)smokingSpinner.getSelectedView();
                        errorText.setError("");
                        errorText.setTextColor(Color.RED);//just to highlight that this is an error
                    }

                    //chest
                    if(chestSpinner.getSelectedItemPosition() == 0) {
                        TextView errorText = (TextView)chestSpinner.getSelectedView();
                        errorText.setError("");
                        errorText.setTextColor(Color.RED);//just to highlight that this is an error
                    }

                    return;
                }

                genderSpinner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                final String store = et.getText().toString();
                final String store1 = et1.getText().toString();
                final String store3 = et3.getText().toString();
                final String store4 = et4.getText().toString();

                sf = getSharedPreferences(preferences, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sf.edit();

                editor.putString(saveIt, store);
                editor.putString(saveIt1, store1);

                editor.putString(saveIt3, store3);
                editor.putString(saveIt4, store4);
                editor.apply();

                Name = et.getText().toString();
                Age = et1.getText().toString();
                Height = et3.getText().toString();
                Weight = et4.getText().toString();

                Log.d("Login Info","LoginInfo"+Name+" "+Age+" "+Sex);
                Intent intent = new Intent(getApplicationContext(), MainActivity1.class);
                intent.putExtra("Name", Name);
                intent.putExtra("Age",Age);
                intent.putExtra("Sex",Sex);
                intent.putExtra("Height",Height);
                intent.putExtra("Weight", Weight);
                startActivity(intent);
            }
        });

    }
}
