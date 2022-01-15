package com.suman.voice.voicerecorder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;

import suman.brose.voicerecorder.R;

/**
 * Created by hp on 6/26/2020.
 */

public class RespiratoryRate extends AppCompatActivity {
    double rr1;
    double rr2;
    double rr3;
    double rr4;
    String Name;
    String Age;
    String Sex;
    String Height;
    String Weight;
    double rrMax;
    double rrMin;
    ArrayList<String> RawStrings = new ArrayList<String>();
    public static final String OUTPUT_DIRECTORY = "SpiroRecorder";
    private static final String FILE_NAME1 = "RecordDataRR";
    private static final String FILE_NAME2 = "ReportRR";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.respiratory_rate);
        Bundle extra = getIntent().getExtras();
        rr1 = extra.getDouble("RR1");
        rr2 = extra.getDouble("RR2");
        rr3 = extra.getDouble("RR3");
        rr4 = extra.getDouble("RR4");
        RawStrings = extra.getStringArrayList("rawData");
        Name = extra.getString("Name");
        Age = extra.getString("Age");
        Sex = extra.getString("Sex");
        Height = extra.getString("Height");
        Weight = extra.getString("Weight");

        TextView rr1Text = (TextView) findViewById(R.id.rr1);
        TextView rr2Text = (TextView) findViewById(R.id.rr2);
        TextView rr3Text = (TextView)findViewById(R.id.rr3);
        TextView rr4Text = (TextView)findViewById(R.id.rr4);
        TextView maxrrText = (TextView) findViewById(R.id.maxrr);
        TextView minrrText = (TextView) findViewById(R.id.minrr);
        TextView warningText = (TextView)findViewById(R.id.warning);

        Double RR1 = rr1*60;
        Double RR2 = rr2*60;
        Double RR3 = rr3*60;
        Double RR4 = rr4*60;

        double[] dblArray = {RR1,RR2,RR3,RR4};
        rrMax = getMax(dblArray);
        rrMin = getMin(dblArray);

        String rr1String = " RR in 1st 15 Seconds : " + Double.toString(round(RR1,2))+" breaths/min";
        String rr2String = " RR in 2nd 15 Seconds : " + Double.toString(round(RR2,2))+" breaths/min";
        String rr3String = " RR in 1st 15 Seconds : " + Double.toString(round(RR3,2))+" breaths/min";
        String rr4String = " RR in 1st 15 Seconds : " + Double.toString(round(RR4,2))+" breaths/min";
        String rrMaxString = " Maximum RR : " + Double.toString(round(rrMax,2))+" breaths/min";
        String rrMinString = " Minimum RR : " + Double.toString(round(rrMin,2))+" breaths/min";

        if(rrMax>25||rrMax<12){
            SpannableString ss = new SpannableString(rrMaxString);
            SpannableStringBuilder ssb = new SpannableStringBuilder(rrMaxString);
            ForegroundColorSpan fcsRed = new ForegroundColorSpan(Color.RED);
            ssb.setSpan(fcsRed, 14, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            maxrrText.setText(ssb);
        }
        else{
            maxrrText.setText(rrMaxString);
        }
        if(rrMin>25||rrMin<12){
            SpannableString ss = new SpannableString(rrMaxString);
            SpannableStringBuilder ssb = new SpannableStringBuilder(rrMinString);
            ForegroundColorSpan fcsRed = new ForegroundColorSpan(Color.RED);
            ssb.setSpan(fcsRed, 14, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            minrrText.setText(ssb);
        }
        else{
            minrrText.setText(rrMinString);
        }

        rr1Text.setText(rr1String);
        rr2Text.setText(rr2String);
        rr3Text.setText(rr3String);
        rr4Text.setText(rr4String);

        if(rrMax>25||rrMin<12){
            warningText.setText(" Warning: Consult Doctor");
        }
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bigd = new BigDecimal(value);
        bigd = bigd.setScale(places, RoundingMode.HALF_UP);
        return bigd.doubleValue();
    }
    public static double getMax(double[] inputArray){
        double maxValue = inputArray[0];
        for(int i=1;i < inputArray.length;i++){
            if(inputArray[i] > maxValue){
                maxValue = inputArray[i];
            }
        }
        return maxValue;
    }
    public static double getMin(double[] inputArray){
        double minValue = inputArray[0];
        for(int i=1;i<inputArray.length;i++){
            if(inputArray[i] < minValue){
                minValue = inputArray[i];
            }
        }
        return minValue;
    }
    public void save(View v) {
        FileOutputStream fos = null;
        File sdcard = Environment.getExternalStorageDirectory();
        // to this path add a new directory path
        File dir = new File(sdcard.getAbsolutePath() + "/" + OUTPUT_DIRECTORY);
        // create this directory if not already created
        if(!dir.exists()) {
            dir.mkdir();
        }
        // create the file in which we will write the contents
        Date d = new Date();
        CharSequence seq  = DateFormat.format("MM-dd-yy hh-mm-ss", d.getTime());
        String record = FILE_NAME1+seq.toString()+".txt";
        File file = new File(dir, record);
        Log.d("output", "RawStrings " + RawStrings);
        StringBuilder csvBuilder = new StringBuilder();
        for(String s : RawStrings){
            csvBuilder.append(s);
            csvBuilder.append(",");
        }
        String csv = csvBuilder.toString();
        Log.d("output", "StoredData " + csv);
        try {
            fos = new FileOutputStream(file, true);
            fos.write(csv.getBytes());
            Toast.makeText(this, "Saved to " + OUTPUT_DIRECTORY + "/" + record,
                    Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String result = FILE_NAME2+seq.toString()+".txt";
        String strIncom;
        strIncom = "Results: "+"\r\n"+"Name: "+Name+"\r\n"+"Age: "+Age+" "+"Sex: "+Sex+"\r\n"+"Height: "+Height+"\r\n"+"Weight: "+Weight+"\r\n"+"Maximum Respiratory Rate : "+String.valueOf(rrMax)+" breaths/min"+"\r\n"+"Minimum Respiratory Rate : "+String.valueOf(rrMin)+" breaths/min";
        File file1 = new File(dir,result);
        try {
            FileWriter fw = new FileWriter(file1.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(strIncom);
            bw.close();
            Toast.makeText(getApplicationContext(),"Data saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Data saved_catch", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    public void toMonitor(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity1.class);
        startActivity(intent);
    }
}
