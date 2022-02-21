package com.suman.voice.voicerecorder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import suman.brose.voicerecorder.R;

/**
 * Created by hp on 4/7/2020.
 */

public class Calibration extends AppCompatActivity {
    int i = 0;
    String Name;
    String Age;
    String Sex;
    String Height;
    String Weight;
    double FVC;
    double FVCclinic;
    double CF;
    ArrayList<String> globalflowRates = new ArrayList<String>();
    ArrayList<String> FFTPeaks = new ArrayList<String>();
    ArrayList<String> RawStrings = new ArrayList<String>();
    SharedPreferences sfCal;
    final String preferencesCal = "pref";
    final String saveItCal = "saveFVC";
    final String saveItCF = "saveCF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // getting attached intent data
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibration);
        Bundle extra = getIntent().getExtras();
        globalflowRates = extra.getStringArrayList("flowRates");
        FFTPeaks = extra.getStringArrayList("flowFFT");
        RawStrings = extra.getStringArrayList("rawData");
        Name = extra.getString("Name");
        Age = extra.getString("Age");
        Sex = extra.getString("Sex");
        Height = extra.getString("Height");
        Weight = extra.getString("Weight");
        FVC = extra.getDouble("FVC");
        final EditText clinicFVC = (EditText) findViewById(R.id.clinicFVCvalue);
        //Button buttonCF = (Button)findViewById(R.id.CF);
        final EditText CFvalue = (EditText) findViewById(R.id.CFvalue);
        final TextView tvFVC, tvCF;
        tvFVC = (TextView) findViewById(R.id.textViewFVC);
        tvCF = (TextView) findViewById(R.id.textViewCF);
        Button plot = (Button) findViewById(R.id.plotCal);

        TextView mFVC = (TextView) findViewById(R.id.mFVC);
        String fvcText = "Calculated FVC : " + Double.toString(round(FVC, 2));
        mFVC.setText(fvcText);

        sfCal = getSharedPreferences(preferencesCal, Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = sfCal.edit();

        clinicFVC.setText(sfCal.getString(saveItCal, ""));
        CFvalue.setText(sfCal.getString(saveItCF, ""));
        tvFVC.setText(sfCal.getString(saveItCal, ""));
        tvCF.setText(sfCal.getString(saveItCF, ""));

        tvFVC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = 0;
            }
        });
        tvCF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = 1;
            }
        });
        plot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String storeCal = clinicFVC.getText().toString();
                sfCal = getSharedPreferences(preferencesCal, Context.MODE_PRIVATE);
                SharedPreferences.Editor editorCal = sfCal.edit();
                editorCal.putString(saveItCal, storeCal);
                String storeCF = CFvalue.getText().toString();
                editorCal.putString(saveItCF, storeCF);
                editorCal.apply();
                if (storeCF.equalsIgnoreCase(""))
                    CF = Double.parseDouble("0");
                else
                    CF = Double.parseDouble(storeCF);

                switch (i) {
                    case 0:
                        tvFVC.setText(sfCal.getString(saveItCal, ""));
                        clinicFVC.setText(" ");
                        break;
                    case 1:
                        tvCF.setText(sfCal.getString(saveItCF, ""));
                        CFvalue.setText(" ");
                        break;
                }
                Log.d("Correction Factor", "CF " + String.valueOf(CF));
                Intent intent = new Intent(getApplicationContext(), Plot.class);
                intent.putExtra("CF", CF);
                intent.putExtra("FVC", FVC);
                intent.putStringArrayListExtra("flowRates", globalflowRates);
                intent.putStringArrayListExtra("flowFFT", FFTPeaks);
                intent.putStringArrayListExtra("rawData", RawStrings);
                intent.putExtra("Name", Name);
                intent.putExtra("Age", Age);
                intent.putExtra("Sex", Sex);
                intent.putExtra("Height", Height);
                intent.putExtra("Weight", Weight);
                startActivity(intent);
            }
        });
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bigd = new BigDecimal(value);
        bigd = bigd.setScale(places, RoundingMode.HALF_UP);
        return bigd.doubleValue();
    }
}

