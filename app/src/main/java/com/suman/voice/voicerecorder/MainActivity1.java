package com.suman.voice.voicerecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.suman.voice.graphviewlibrary.GraphView;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.*;

import suman.brose.voicerecorder.R;


public class MainActivity1 extends AppCompatActivity {

    public static final String SCALE = "scale";
    public static final String OUTPUT_DIRECTORY = "VoiceRecorder";
    public static final String OUTPUT_FILENAME = "recorder.mp3";
    private static final int MY_PERMISSIONS_REQUEST_CODE = 0;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    int scale = 8;
    private GraphView graphView;
    private VoiceRecorder recorder;
    private List samples;
    long[] lPeaks;
    double[] doubles;
    int cal = 0;
    String Name;
    String Age;
    String Sex;
    String Height;
    String Weight;
    ArrayList<String> globalflowRates = new ArrayList<String>();
    ArrayList<String> FFTPeaks = new ArrayList<String>();
    ArrayList<String> RawStrings = new ArrayList<String>();
    private Switch sw1;
    private  Button chng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        graphView = (GraphView) findViewById(R.id.graphView);
        graphView.setGraphColor(Color.rgb(0,0,255));
        graphView.setCanvasColor(Color.rgb(255,255,255));
        graphView.setTimeColor(Color.rgb(0, 0, 0));
       /* sw1 = (Switch)findViewById(R.id.switch1);
        chng = (Button)findViewById(R.id.chng);
        chng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sw1.isChecked()){
                    graphView.setGraphColor(Color.rgb(255,255,255));
                    graphView.setCanvasColor(Color.rgb(20,20,20));
                    graphView.setTimeColor(Color.rgb(255, 255, 255));
                }
            }
        });
*/
        recorder = VoiceRecorder.getInstance();
        if (recorder.isRecording()) {
            ((Button) findViewById(R.id.control)).setText(getResources().getString(R.string.stop));
            recorder.startPlotting(graphView);
        }
        if (savedInstanceState != null) {
            scale = savedInstanceState.getInt(SCALE);
            graphView.setWaveLengthPX(scale);
            if (!recorder.isRecording()) {
                samples = recorder.getSamples();
                //Log.d("output","Size " + String.valueOf(samples.size()));
                graphView.showFullGraph(samples);
            }
        }
        Intent intent=getIntent();
        Name = intent.getStringExtra("Name");
        Age = intent.getStringExtra("Age");
        Sex = intent.getStringExtra("Sex");
        Height = intent.getStringExtra("Height");
        Weight = intent.getStringExtra("Weight");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SCALE, scale);
        super.onSaveInstanceState(outState);
    }

    public void zoomIn(View v) {
        scale = scale + 1;
        if (scale > 15) {
            scale = 15;
        }
        graphView.setWaveLengthPX(scale);
        if (!recorder.isRecording()) {
            graphView.showFullGraph(samples);
        }
    }

    public void zoomOut(View v) {
        scale = scale - 1;
        if (scale < 2) {
            scale = 2;
        }
        graphView.setWaveLengthPX(scale);
        if (!recorder.isRecording()) {
            graphView.showFullGraph(samples);
        }
    }

    public void controlClick(View v) {
        if (recorder.isRecording()) {
            ((Button) findViewById(R.id.control)).setText(this.getResources().getString(R.string.record));
            graphView.stopPlotting();
            samples = recorder.stopRecording();
            Log.d("output","WaveSize " + String.valueOf(samples.size()));
            /*for(int i = 0; i<samples.size(); i++){
                String s = samples.get(i).toString();
                Log.d("output","Wave " + s);
            }*/
            graphView.showFullGraph(samples);
        } else if(checkRecordPermission()&&checkStoragePermission()){
            graphView.reset();
            String filepath = Environment.getExternalStorageDirectory().getPath();
            File file = new File(filepath, OUTPUT_DIRECTORY);
            if (!file.exists()) {
                file.mkdirs();
            }
            recorder.setOutputFilePath(file.getAbsoluteFile() + "/" + OUTPUT_FILENAME);
            recorder.startRecording();
            recorder.startPlotting(graphView);
            ((Button) findViewById(R.id.control)).setText(this.getResources().getString(R.string.stop));
        }else{
            requestPermissions();
        }
    }
    public void Cal1(View view){
        double[] doublesCal = new double[0];
        double FVC = 0;
        double FEV = 0;
        double[] FFTPeaksDouble;
        ArrayList<String> globalflowRatesCal = new ArrayList<String>();
        ArrayList<String> FFTPeaksCal = new ArrayList<String>();
        ArrayList<String> globalinstantVolumes = new ArrayList<String>();
        ArrayList<String> valuesBeforeFilter = new ArrayList<String>();
        ArrayList<String> valuesAfterFilter = new ArrayList<String>();
        long[] l = recorder.plotSpiro();
        double[] rawData = longtoDoubleCalibrate(l);
        RawStrings = doubleToString(rawData);
        if(l!=null) {
            //lPeaks = peakDetection(l);
            for (int i = 0; i < l.length; i++) {
                valuesBeforeFilter.add(String.valueOf(l[i]));
            }
            Log.d("output", "BeforeFilter " + valuesBeforeFilter);
            long[] filterOutput = smoothArray(l,5);// filterOutput is not referred then also you will get filter output---go through smoothArray
            for (int i = 0; i < l.length; i++) {
                valuesAfterFilter.add(String.valueOf(l[i]));
            }
            Log.d("output", "AfterFilter " + valuesAfterFilter);
            doublesCal = longtoDoubleCalibrate(l);
            if(doublesCal!=null) {
                for (int i = 0; i < doublesCal.length; i++) {
                    globalflowRatesCal.add(String.valueOf(doublesCal[i]));
                }
            }
            Log.d("output", "LongValue " + globalflowRatesCal);
        }
        FFTPeaksCal = FFT(doublesCal);
        FFTPeaksDouble = parseData(FFTPeaksCal);
        Log.d("FFT_Peaks", "FFT_Peaks " + convertStringArray(FFTPeaksDouble));
        double cumulativeVolume =0;
        int k =1;
        double[] FEV_FVC_FFT = new double[2];
        for (int i = 0; i < FFTPeaksDouble.length; i++) {
            double flowRate = (FFTPeaksDouble[i])*k;
            cumulativeVolume += abs((flowRate * 0.064));
            if(i<FFTPeaksDouble.length*0.50){
                FEV_FVC_FFT[0]=cumulativeVolume;
            }
            FEV_FVC_FFT[1]=cumulativeVolume;
            globalinstantVolumes.add(String.valueOf(cumulativeVolume));
        }
        Log.d("output", "FFTPeaksVolume " + globalinstantVolumes);
        Log.d("FFT_Peaks", "FFT_Peaks " + FFTPeaksCal);
        FVC = round(FEV_FVC_FFT[1],2);
        Intent intent = new Intent(getApplicationContext(), Calibration.class);
        intent.putExtra("FEV", FEV);
        intent.putExtra("FVC", FVC);
        intent.putStringArrayListExtra("flowRates", globalflowRatesCal);
        intent.putStringArrayListExtra("flowFFT", FFTPeaksCal);
        intent.putStringArrayListExtra("rawData", RawStrings);
        intent.putExtra("Name", Name);
        intent.putExtra("Age", Age);
        intent.putExtra("Sex", Sex);
        intent.putExtra("Height", Height);
        intent.putExtra("Weight", Weight);
        startActivity(intent);
    }
    /*public void Cal2(View view){
        cal = 2;
    }
    public void Cal3(View view){
        cal = 3;
    }
    public void Cal4(View view){
        cal = 4;
    }*/
    public void Plot(View view) {
        double cumulativeVolume =0;
        double cumulativeVolumeFFT =0;
        double k =1;
        double FVC = 0;
        double FEV = 0;
        ArrayList<String> valuesBeforeFilter = new ArrayList<String>();
        ArrayList<String> valuesAfterFilter = new ArrayList<String>();
        long[] l = recorder.plotSpiro();
        double[] rawData = longtoDoubleCalibrate(l);
        RawStrings = doubleToString(rawData);
        if(l!=null) {
            lPeaks = peakDetection(l);
            for (int i = 0; i < l.length; i++) {
                valuesBeforeFilter.add(String.valueOf(l[i]));
            }
            Log.d("output", "BeforeFilter " + valuesBeforeFilter);
            long[] filterOutput = smoothArray(l,5);// filterOutput is not referred then also you will get filter output---go through smoothArray
            for (int i = 0; i < l.length; i++) {
                valuesAfterFilter.add(String.valueOf(l[i]));
            }
            Log.d("output", "AfterFilter " + valuesAfterFilter);
            doubles = longtoDoubleCalibrate(l);
            //////////////////////////Calibration////////////////////////////
            /*switch (cal){
                case 0:
                    for(int i=1;i<doubles.length;i++){
                        doubles[i]= doubles[i];//2244
                        //Log.d("output", "dblValue " + String.valueOf(dbl[i]));
                    }
                case 1:
                    for(int i=1;i<doubles.length;i++){
                        doubles[i]= doubles[i] + 0.5;//2244
                        //Log.d("output", "dblValue " + String.valueOf(dbl[i]));
                    }
                    break;
                case 2:
                    for(int i=1;i<doubles.length;i++){
                        doubles[i]= doubles[i] + 1.0;//2244
                        //Log.d("output", "dblValue " + String.valueOf(dbl[i]));
                    }
                    break;
                case 3:
                    for(int i=1;i<doubles.length;i++){
                        doubles[i]= doubles[i] - 0.5;//2244
                        //Log.d("output", "dblValue " + String.valueOf(dbl[i]));
                    }
                    break;
                case 4:
                    for(int i=1;i<doubles.length;i++){
                        doubles[i]= doubles[i] - 1.0;//2244
                        //Log.d("output", "dblValue " + String.valueOf(dbl[i]));
                    }
                    break;
            }*/
            ////////////////////////////////////////////////////////////////
            if(doubles!=null) {
                for (int i = 0; i < doubles.length; i++) {
                    globalflowRates.add(String.valueOf(doubles[i]));
                }
            }
            Log.d("output", "LongValue " + globalflowRates);
        }
        FFTPeaks = FFT(doubles);

        Log.d("output", "FEV " + String.valueOf(FEV)+"FVC "+String.valueOf(FVC));
        Intent intent = new Intent(getApplicationContext(), Plot.class);
        intent.putExtra("FEV", FEV);
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
    public void RR(View view){
        long[] l = recorder.plotSpiro();
        //smoothArray(l,5);
        double[] rawData = longtoDoubleCalibrate(l);
        //double[] rawData = {0.0,0.01445466491458607,0.17017082785808146,0.5170827858081471,0.5216819973718791,0.1616294349540079,0.1425755584756899,0.11695137976346912,0.06767411300919843,1.6819973718791064,5.067674113009199,8.493429697766096,8.412614980289094,6.622864651773981,3.61892247043364,1.1432325886990802,0.03810775295663601,0.009198423127463863,0.010512483574244415,0.00985545335085414,0.011169513797634692,0.01971090670170828,0.00985545335085414,0.00985545335085414,0.011169513797634692,0.010512483574244415,0.011826544021024968,0.011169513797634692,0.013797634691195795,0.07752956636005257,0.5926412614980289,2.272010512483574,3.40473061760841,4.228646517739816,4.081471747700395,2.538764783180026,2.647831800262812,2.5532194480946124,1.3022339027595269,0.05584756898817345,0.013797634691195795,0.00985545335085414,0.009198423127463863,0.00985545335085414,0.00985545335085414,0.009198423127463863,0.009198423127463863,0.00985545335085414,0.015111695137976347,0.013140604467805518,0.01971090670170828,0.6990801576872536,2.8002628120893562,4.059789750328515,6.1879106438896185,5.466491458607096,3.0985545335085414,2.533508541392904,3.440210249671485,0.11892247043363995,0.02102496714848883,0.009198423127463863,0.010512483574244415,0.008541392904073587,0.00985545335085414,0.00985545335085414,0.00985545335085414,0.00985545335085414,0.00985545335085414,0.012483574244415242,0.009198423127463863,0.009198423127463863,0.017739816031537452,0.2752956636005256,1.6136662286465178,3.0302233902759528,7.592641261498029,8.503285151116952,4.351511169513798,4.434296977660972,3.4132720105124834,0.5269382391590013,0.03482260183968462,0.01576872536136662,0.012483574244415242,0.012483574244415242,0.012483574244415242,0.022339027595269383,0.011826544021024968,0.008541392904073587,0.013140604467805518,0.011169513797634692,0.008541392904073587,0.00788436268068331,0.16951379763469118,1.4914586070959264,4.814060446780552,2.9934296977660972,3.3344283837056503,2.9914586070959266,3.131406044678055,0.8909329829172142,0.18396846254927726,0.009198423127463863,0.00788436268068331,0.00788436268068331,0.00985545335085414,0.008541392904073587,0.010512483574244415,0.008541392904073587,0.008541392904073587,0.008541392904073587,0.00788436268068331,0.011169513797634692,0.1754270696452037,1.8488830486202366,1.3935611038107754,3.7417871222076213,3.530880420499343,2.776609724047306,2.4152431011826545,2.1057818659658345,0.7996057818659659,0.08935611038107753,0.00985545335085414,0.008541392904073587,0.00788436268068331,0.00985545335085414,0.012483574244415242,0.008541392904073587,0.009198423127463863,0.00985545335085414,0.009198423127463863,0.1583442838370565,1.230617608409987,5.624835742444152,5.1005256241787125,3.921156373193167,2.7424441524310117,2.0170827858081473,0.8160315374507228,0.035479632063074903,0.009198423127463863,0.015111695137976347,0.15703022339027595,0.1176084099868594,0.00985545335085414,0.008541392904073587,0.009198423127463863,0.011826544021024968,0.00788436268068331,0.008541392904073587,0.009198423127463863,0.008541392904073587,0.01445466491458607,0.5387647831800263,1.647831800262812,2.609067017082786,4.887647831800263,1.9487516425755584,2.126149802890933,2.0420499342969776,0.43495400788436267,0.012483574244415242,0.010512483574244415,0.00788436268068331,0.009198423127463863,0.011826544021024968,0.009198423127463863,0.009198423127463863,0.007227332457293035,0.00788436268068331,0.00788436268068331,0.00788436268068331,0.007227332457293035,0.00985545335085414,0.00985545335085414,0.11366622864651774,0.702365308804205,2.2897503285151117,2.6018396846254928,2.4526938239159,2.8134034165571618,3.202365308804205,1.4704336399474376,0.5821287779237845,0.040735873850197106,0.00985545335085414,0.008541392904073587,0.00788436268068331,0.008541392904073587,0.008541392904073587,0.013140604467805518,0.011169513797634692,0.010512483574244415,0.008541392904073587,0.00788436268068331,0.007227332457293035,0.02890932982917214,0.7490144546649146,2.854796320630749,3.3679369250985545,3.7207621550591328,3.6110381077529565,3.0965834428383707,1.1681997371879107,0.6241787122207622,0.009198423127463863,0.010512483574244415,0.012483574244415242,0.011169513797634692,0.012483574244415242,0.01576872536136662,0.010512483574244415,0.01445466491458607,0.0164257555847569,0.012483574244415242,0.008541392904073587,0.00788436268068331,0.009198423127463863,0.04730617608409987,0.7319316688567674,2.442181340341656,5.646517739816032,3.995400788436268,3.883705650459921,2.1471747700394217,2.378449408672799,0.24704336399474375,0.011169513797634692,0.00985545335085414,0.011169513797634692,0.011169513797634692,0.013140604467805518,0.011826544021024968,0.011169513797634692,0.011169513797634692,0.011826544021024968,0.008541392904073587,0.008541392904073587,0.007227332457293035,0.009198423127463863,0.010512483574244415,0.022339027595269383,0.15374507227332457,2.3679369250985545,2.185939553219448,4.425755584756899,3.3751642575558476,3.0867279894875166,1.8830486202365309,0.5565045992115637,0.1498028909329829,0.011826544021024968,0.013797634691195795,0.011169513797634692,0.009198423127463863,0.009198423127463863,0.02890932982917214,0.017082785808147174,0.011169513797634692,0.010512483574244415,0.02102496714848883,0.00788436268068331,0.03088042049934297,0.03745072273324573,1.0216819973718791,1.3469119579500657,2.0939553219448093,1.8731931668856767,1.2529566360052562,1.564388961892247,1.7227332457293036,0.5328515111695138,0.020367936925098553,0.013140604467805518,0.008541392904073587,0.00985545335085414,0.00788436268068331,0.008541392904073587,0.00985545335085414,0.011169513797634692,0.010512483574244415,0.008541392904073587,0.009198423127463863,0.009198423127463863,0.14060446780551905,0.7910643889618922,5.295663600525624,3.3442838370565044,4.805519053876479,3.1911957950065704,2.533508541392904,1.5006570302233904,0.13797634691195795,0.011169513797634692,0.00985545335085414,0.009198423127463863,0.008541392904073587,0.013140604467805518,0.010512483574244415,0.010512483574244415,0.009198423127463863,0.009198423127463863,0.008541392904073587,0.009198423127463863,0.008541392904073587,0.009198423127463863,0.013140604467805518,1.2628120893561103,7.438896189224704,3.159658344283837,4.64388961892247,2.444809461235217,3.9651773981603156,0.5374507227332457,0.02956636005256242,0.024967148488830485,0.03350854139290407,0.01971090670170828,0.013797634691195795,0.02890932982917214,0.018396846254927726,0.01971090670170828,0.017082785808147174,0.02102496714848883,0.021681997371879105,0.017082785808147174,0.023653088042049936,0.05519053876478318,0.6701708278580815,2.73784494086728,4.936925098554534,3.6149802890932983,3.73850197109067,1.8048620236530881,0.8554533508541393,0.0657030223390276,0.018396846254927726,0.012483574244415242,0.015111695137976347,0.0164257555847569,0.015111695137976347,0.019053876478318004,0.022339027595269383,0.015111695137976347,0.010512483574244415,0.012483574244415242,0.011826544021024968,0.00985545335085414,0.00985545335085414,0.018396846254927726,0.010512483574244415,0.019053876478318004,0.04664914586070959,0.29697766097240474,1.7884362680683312,3.5275952693823918,3.678712220762155,5.2247043363994745,2.8515111695137976,2.07161629434954,0.48751642575558474,0.013140604467805518,0.018396846254927726,0.022339027595269383,0.028252299605781867,0.032194480946123524,0.04862023653088042,0.04402102496714849,0.04533508541392904,0.054533508541392904,0.047963206307490146,0.06373193166885677,0.047963206307490146,0.021681997371879105,0.021681997371879105,0.025624178712220762,0.015111695137976347,0.020367936925098553,0.0519053876478318,0.6202365308804205,1.4986859395532195,1.633377135348226,1.4842312746386335,1.266754270696452,1.1044678055190538,0.07818659658344283,0.011826544021024968,0.013140604467805518,0.010512483574244415,0.011826544021024968,0.00985545335085414,0.013140604467805518,0.012483574244415242,0.010512483574244415,0.012483574244415242,0.009198423127463863,0.013140604467805518,0.013797634691195795,0.019053876478318004,0.1636005256241787,0.985545335085414,1.4178712220762155,1.4296977660972405,1.6281208935611038,1.0459921156373193,0.17871222076215507,5.469776609724048,14.052562417871222,2.0670170827858083,0.08541392904073587,0.23653088042049936,0.12417871222076216,0.051248357424441525,0.05387647831800263};
        //double[] rawData = {0.0,0.015111695137976347,0.051248357424441525,0.4756898817345598,0.4671484888304862,0.19250985545335086,0.16491458607095927,0.16557161629434955,0.13009198423127463,0.030223390275952694,0.02431011826544021,0.30814717477003944,0.022339027595269383,0.023653088042049936,0.02431011826544021,0.024967148488830485,13.969776609724047,6.819316688567674,7.511169513797634,1.028252299605782,0.03810775295663601,0.049277266754270695,13.708935611038108,8.131406044678055,4.54664914586071,0.3968462549277267,0.04862023653088042,2.967148488830486,12.448094612352168,5.752956636005257,0.5019710906701709,0.03942181340341656,0.05059132720105125,12.38239159001314,8.982260183968462,5.9973718791064385,0.7759526938239159,0.03745072273324573,0.03350854139290407,13.227332457293036,6.459921156373193,6.398817345597897,0.5775295663600526,0.03416557161629435,4.576872536136662,13.458607095926412,7.8942181340341655,2.283180026281209,0.05059132720105125,0.032194480946123524,12.681997371879106,6.4204993429697765,5.802233902759527,0.06964520367936924,0.03416557161629435,12.949408672798949,7.416557161629435,6.170170827858081,1.1130091984231274,0.03942181340341656,8.28186596583443,13.003285151116952,5.526281208935611,0.8278580814717477,0.04533508541392904,0.0519053876478318,13.868593955321945,6.427069645203679,1.9395532194480947,0.1583442838370565,0.4480946123521682,12.773981603153745,5.664914586070959,1.2582128777923784,0.11366622864651774,0.02956636005256242,14.022339027595269,5.990801576872536,5.001314060446781,0.2943495400788436,0.05059132720105125,13.741787122207622,5.716162943495401,4.505256241787122,0.2233902759526938,0.051248357424441525,14.032851511169515,6.2910643889618925,4.604467805519054,0.19710906701708278,0.03482260183968462,12.766097240473062,5.788436268068331,3.751642575558476,0.8705650459921156,0.028252299605781867,11.676741130091985,8.705650459921156,5.390932982917215,0.7424441524310118,0.022996057818659658,13.674113009198424,6.639290407358739,6.519053876478318,0.5105124835742444,0.04007884362680683,0.12812089356110382,13.363337713534822,5.434954007884363,0.5289093298291722,0.017739816031537452,13.268068331143233,12.576215505913272,4.442838370565046,0.4099868593955322,0.0164257555847569,7.124835742444152,13.040735873850197,5.807490144546649,0.580814717477004,0.026938239159001315,7.637976346911958,13.330486202365309,6.019053876478318,0.43626806833114323,0.025624178712220762,0.01576872536136662,13.1419185282523,5.3521681997371875,0.938239159001314,0.15243101182654403,0.01445466491458607,13.149145860709593,5.624835742444152,2.1412614980289093,0.4454664914586071,0.026281208935611037,11.942838370565045,13.235216819973719,4.913929040735874,1.2936925098554533,0.024967148488830485,0.013797634691195795,12.294349540078844,5.798948751642576,1.3626806833114322,0.202365308804205,0.025624178712220762,14.181340341655716,7.375164257555848,3.2910643889618925,0.2726675427069645,0.021681997371879105,13.720762155059132,8.461892247043364,3.6938239159001314,1.016425755584757,0.020367936925098553,7.83311432325887,13.371222076215506,5.885676741130092,0.9651773981603153,0.015111695137976347,0.010512483574244415,13.1688567674113,5.172798948751643,1.8771353482260185,0.3508541392904074,0.03745072273324573,13.521681997371878,6.948751642575559,2.7910643889618925,1.6162943495400788,0.024967148488830485,0.012483574244415242,13.88173455978975,5.6215505913272015,1.4651773981603153,0.02956636005256242,0.017739816031537452,13.942838370565045,5.279237844940868,1.7398160315374507,0.5197109067017083,0.04402102496714849,14.421813403416557,5.302890932982917,2.1346911957950065,0.9178712220762155,0.022996057818659658,0.01445466491458607,13.338370565045992,5.28515111695138,1.3534822601839684,0.43035479632063073,0.030223390275952694,13.961892247043364,5.47634691195795,2.1971090670170828,0.6471747700394218,0.017082785808147174,0.009198423127463863,0.00985545335085414,0.015111695137976347,1.4520367936925098,0.8659658344283837,0.1366622864651774,0.042706964520367936,0.023653088042049936,0.02956636005256242};
        RawStrings = doubleToString(rawData);
        Log.d("RR", "RR_check_length " + rawData.length);
        double[] doubles = Arrays.copyOf(rawData,512);
        double[] doubles1 = new double[128],doubles2= new double[128],doubles3= new double[128],doubles4= new double[128];
        for(int i=0;i<doubles.length;i++) {
            if (i < 128) {
                doubles1[i] = doubles[i];
            }
            if (i >= 128 && i < 256) {
                doubles2[i - 128] = doubles[i];
            }
            if (i >= 256 && i < 384) {
                doubles3[i - 256] = doubles[i];
            }
            if (i >= 384 && i < 512) {
                doubles[i - 384] = doubles[i];
            }
        }
        Complex[] cmplxFFT1 = new Complex[128],cmplxFFT2= new Complex[128],cmplxFFT3= new Complex[128],cmplxFFT4= new Complex[128];
        //Complex[] complexFFT = new Complex[doubles.length];
        //Log.d("RR", "RR_check_length " + doubles.length);
        if(doubles1.length>0) {
            for (int i = 0; i < doubles1.length; i++) {
                cmplxFFT1[i] = new Complex(doubles1[i], 0.0);
                //Log.d("output", "FFT db1 " + db1FFT[i].toString());
            }
        }
        if(doubles2.length>0) {
            for (int i = 0; i < doubles2.length; i++) {
                cmplxFFT2[i] = new Complex(doubles2[i], 0.0);
                //Log.d("output", "FFT db1 " + db1FFT[i].toString());
            }
        }
        if(doubles3.length>0) {
            for (int i = 0; i < doubles3.length; i++) {
                cmplxFFT3[i] = new Complex(doubles3[i], 0.0);
                //Log.d("output", "FFT db1 " + db1FFT[i].toString());
            }
        }
        if(doubles4.length>0) {
            for (int i = 0; i < doubles4.length; i++) {
                cmplxFFT4[i] = new Complex(doubles4[i], 0.0);
                //Log.d("output", "FFT db1 " + db1FFT[i].toString());
            }
        }
        int fs = 5;//5
        ArrayList<String> stringFFT1 = new ArrayList<String>();
        ArrayList<String> stringFFT2 = new ArrayList<String>();
        ArrayList<String> stringFFT3 = new ArrayList<String>();
        ArrayList<String> stringFFT4 = new ArrayList<String>();
        if(cmplxFFT1!=null && cmplxFFT1.length>0){
            fft(cmplxFFT1);
            for (Complex c : cmplxFFT1) {

                stringFFT1.add(String.valueOf(c.abs()));
            }
            Log.d("RR", "RR_FFT1 " + stringFFT1);
        }
        if(cmplxFFT2!=null && cmplxFFT2.length>0){
            fft(cmplxFFT2);
            for (Complex c : cmplxFFT2) {

                stringFFT2.add(String.valueOf(c.abs()));
            }
            Log.d("RR", "RR_FFT2 " + stringFFT2);
        }
        if(cmplxFFT3!=null && cmplxFFT3.length>0){
            fft(cmplxFFT3);
            for (Complex c : cmplxFFT3) {

                stringFFT3.add(String.valueOf(c.abs()));
            }
            Log.d("RR", "RR_FFT3 " + stringFFT3);
        }
        if(cmplxFFT4!=null && cmplxFFT4.length>0){
            fft(cmplxFFT4);
            for (Complex c : cmplxFFT3) {

                stringFFT4.add(String.valueOf(c.abs()));
            }
            Log.d("RR", "RR_FFT4 " + stringFFT4);
        }
        /////////////////////////////////for first 15 seconds//////////////////////////////////
        double[] absFFT1 = stringToDouble(stringFFT1);
        double[] freq1 = new double[absFFT1.length];
        int n1,n2,n3,n4;
        n1= absFFT1.length;
        Log.d("RR", "RR_length " + n1);
        for(int i=0;i<absFFT1.length;i++){
            freq1[i]= (double)i*((double)fs/(double)n1);
            //Log.d("RR", "RR_Freq " + String.valueOf(freq1[i]));
        }
        double[][] dblArray1 = new double[n1][n1];
        for(int i=0; i<n1;i++){
            dblArray1[i][0]=freq1[i];
            dblArray1[i][1]= absFFT1[i];
        }
        double maxPower1 = dblArray1[2][1];
        double maxFreq1 = dblArray1[2][0];
        int index=0;
        for(int i=2;i < dblArray1.length;i++){
            if(dblArray1[i][1] > maxPower1 && dblArray1[i][0]<=2){
                maxPower1 = dblArray1[i][1];
                maxFreq1 = dblArray1[i][0];
            }
        }
        Log.d("RR", "RR_check_freqPower1 " +String.valueOf(maxPower1)+" "+String.valueOf(maxFreq1));
        /////////////////////////////////for second 15 seconds///////////////////////////////////////////////////////////
        double[] absFFT2 = stringToDouble(stringFFT2);
        double[] freq2 = new double[absFFT2.length];
        n2=absFFT2.length;
        Log.d("RR", "RR_length " + n2);
        for(int i=0;i<absFFT2.length;i++){
            freq2[i]= (double)i*((double)fs/(double)n2);
            //Log.d("RR", "RR_Freq " + String.valueOf(freq2[i]));
        }
        double[][] dblArray2 = new double[n2][n2];
        for(int i=0; i<n2;i++){
            dblArray2[i][0]=freq2[i];
            dblArray2[i][1]= absFFT2[i];
        }
        double maxPower2 = dblArray2[2][1];
        double maxFreq2 = dblArray2[2][0];
        for(int i=2;i < dblArray2.length;i++){
            if(dblArray2[i][1] > maxPower2 && dblArray2[i][0]<=2){
                maxPower2 = dblArray2[i][1];
                maxFreq2 = dblArray2[i][0];
            }
        }
        Log.d("RR", "RR_check_freqPower2 " +String.valueOf(maxPower2)+" "+String.valueOf(maxFreq2));
        /////////////////////////////////for third 15 seconds/////////////////////////////////////////////////////////////////
        double[] absFFT3 = stringToDouble(stringFFT3);
        double[] freq3 = new double[absFFT3.length];
        n3=absFFT3.length;
        Log.d("RR", "RR_length " + n3);
        for(int i=0;i<absFFT3.length;i++){
            freq3[i]= (double)i*((double)fs/(double)n3);
            //Log.d("RR", "RR_length " + n3);
        }
        double[][] dblArray3 = new double[n3][n3];
        for(int i=0; i<n3;i++){
            dblArray3[i][0]=freq3[i];
            dblArray3[i][1]= absFFT3[i];
        }
        double maxPower3 = dblArray3[2][1];
        double maxFreq3 = dblArray3[2][0];
        for(int i=2;i < dblArray3.length;i++){
            if(dblArray3[i][1] > maxPower3 && dblArray3[i][0]<=2){
                maxPower3 = dblArray3[i][1];
                maxFreq3 = dblArray3[i][0];
            }
        }
        Log.d("RR", "RR_check_freqPower3 " +String.valueOf(maxPower3)+" "+String.valueOf(maxFreq3));
        /////////////////////////////////for fourth 15 seconds/////////////////////////////////////////////////////////////////
        double[] absFFT4 = stringToDouble(stringFFT4);
        double[] freq4 = new double[absFFT4.length];
        n4=absFFT4.length;
        Log.d("RR", "RR_length " + n4);
        for(int i=0;i<absFFT4.length;i++){
            freq4[i]= (double)i*((double)fs/(double)n4);
            //Log.d("RR", "RR_length " + n3);
        }
        double[][] dblArray4 = new double[n4][n4];
        for(int i=0; i<n4;i++){
            dblArray4[i][0]=freq4[i];
            dblArray4[i][1]= absFFT4[i];
        }
        double maxPower4 = dblArray4[2][1];
        double maxFreq4 = dblArray4[2][0];
        for(int i=2;i < dblArray4.length;i++){
            if(dblArray4[i][1] > maxPower4 && dblArray4[i][0]<=2){
                maxPower4 = dblArray4[i][1];
                maxFreq4 = dblArray4[i][0];
            }
        }
        Log.d("RR", "RR_check_freqPower4 " +String.valueOf(maxPower4)+" "+String.valueOf(maxFreq4));
        Intent intent = new Intent(getApplicationContext(), RespiratoryRate.class);
        intent.putExtra("RR1", maxFreq1);
        intent.putExtra("RR2", maxFreq2);
        intent.putExtra("RR3", maxFreq3);
        intent.putExtra("RR4", maxFreq4);
        intent.putStringArrayListExtra("rawData", RawStrings);
        intent.putExtra("Name", Name);
        intent.putExtra("Age", Age);
        intent.putExtra("Sex", Sex);
        intent.putExtra("Height", Height);
        intent.putExtra("Weight", Weight);
        startActivity(intent);

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        graphView.stopPlotting();
        if (recorder.isRecording()) {
            recorder.stopRecording();
        }
        if (recorder != null) {
            recorder.release();
        }
    }
    public void requestPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);//MY_PERMISSIONS_REQUEST_CODE

        } else {
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);//MY_PERMISSIONS_REQUEST_CODE
            // MY_PERMISSIONS_REQUEST_CODE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    private boolean checkRecordPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }
    public long[] peakDetection(long[] arr){
        long longPeak[];
        longPeak = new long[arr.length];
        for (int i = 1; i < arr.length-1; i++){
            if (arr[i - 1] <= arr[i] && arr[i] >= arr[i + 1]){
                longPeak[i] = arr[i];
            }
        }
        return longPeak;
    }
    public long[] smoothArray(long[] values,int smoothing ){
        long value = values[0]; // start with the first input
        int len=values.length;
        for (int i=1;i<len; ++i){
            long currentValue = values[i];
            value += (currentValue - value) / smoothing;
            values[i] = value;
        }
        return values;
    }
    public double[] longtoDoubleCalibrate(long[] arr){
        double[] dbl = new double[arr.length];
        for(int i=1;i<arr.length;i++){
            dbl[i]=(double)arr[i]/1522;//2244
            Log.d("output", "dblValue " + String.valueOf(dbl[i]));
        }
        return dbl;
    }
    public double[] stringToDouble(ArrayList<String> String){
        double[] ret = new double[String.size()];
        for(int j = 0; j < String.size(); j++) {
            if(String.get(j) != null) {
                ret[j] = Double.parseDouble(String.get(j));
            }
        }
        return ret;
    }
    public String stringToDoubleMax(List<String> globalflowRates){
        double[] ret = new double[globalflowRates.size()];
        for(int j = 0; j < ret.length; j++) {
            if(globalflowRates.get(j) != null) {
                ret[j] = Double.parseDouble(globalflowRates.get(j));
            }
        }
        double max = ret[0];
        for(int i=1;i < ret.length;i++){
            if(ret[i] > max){
                max = ret[i];
            }
        }
        return String.valueOf(max);
    }

    public ArrayList<String> doubleToString(double[] doubles) {
        ArrayList<String> strings = new ArrayList<String>();
        if(doubles!=null) {
            for (int i = 0; i < doubles.length; i++) {
                strings.add(String.valueOf(doubles[i]));
            }
        }
        return strings;
    }

    static void fft(Complex[] buffer) {

        int bits = (int) (log(buffer.length) / log(2));
        for (int j = 1; j < buffer.length / 2; j++) {

            int swapPos = bitReverse(j, bits);
            Complex temp = buffer[j];
            buffer[j] = buffer[swapPos];
            buffer[swapPos] = temp;
        }

        for (int N = 2; N <= buffer.length; N <<= 1) {
            for (int i = 0; i < buffer.length; i += N) {
                for (int k = 0; k < N / 2; k++) {

                    int evenIndex = i + k;
                    int oddIndex = i + k + (N / 2);
                    Complex even = buffer[evenIndex];
                    Complex odd = buffer[oddIndex];

                    double term = (-2 * PI * k) / (double) N;
                    if(odd!=null) {
                        Complex exp = (new Complex(cos(term), sin(term)).mult(odd));
                        Log.d("output", "exp " + String.valueOf(exp));
                        if(exp!=null) {
                            buffer[evenIndex] = even.add(exp);
                            buffer[oddIndex] = even.sub(exp);
                        }
                    }
                }
            }
        }
    }
    public static int bitReverse(int n, int bits) {
        int reversedN = n;
        int count = bits - 1;

        n >>= 1;
        while (n > 0) {
            reversedN = (reversedN << 1) | (n & 1);
            count--;
            n >>= 1;
        }

        return ((reversedN << count) & ((1 << bits) - 1));
    }
    public double[] parseData(List<String> globalflowRates) {
        double[] ret = new double[globalflowRates.size()];
        for(int j = 0; j < ret.length; j++) {
            if(globalflowRates.get(j) != null) {
                ret[j] = Double.parseDouble(globalflowRates.get(j))/5.2;
            }
        }
        return ret;
    }
    public ArrayList<String> convertStringArray(double[] doubleArray){
        ArrayList<String> stringArray = new ArrayList<String>();
        if(doubleArray.length>0) {
            for (int i = 0; i < doubleArray.length; i++) {
                stringArray.add(String.valueOf(doubleArray[i]));
            }
        }
        return stringArray;
    }
    public ArrayList<String> FFT(double[] doublesVal){
        ArrayList<String> FFTPeaksVal = new ArrayList<String>();
        ////////////doing FFT/////////////////
        Log.d("output", "checkLength " + doublesVal.length);
        double[] db1 = new double[8],db2= new double[8],db3= new double[8],db4= new double[8],db5= new double[8],db6= new double[8],db7= new double[8],db8= new double[8],db9= new double[8],db10= new double[8];
        double[] db11 = new double[8],db12 = new double[8],db13 = new double[8],db14 = new double[8];
        Complex[] db1FFT = new Complex[8],db2FFT= new Complex[8],db3FFT= new Complex[8],db4FFT= new Complex[8],db5FFT= new Complex[8],db6FFT= new Complex[8],db7FFT= new Complex[8],db8FFT= new Complex[8],db9FFT= new Complex[8],db10FFT= new Complex[8];
        Complex[] db11FFT = new Complex[8],db12FFT= new Complex[8],db13FFT= new Complex[8],db14FFT= new Complex[8];
        //double[] doublesFFT = Arrays.copyOf(doublesVal,44);
        double[] doublesFFT = Arrays.copyOf(doublesVal,60);
        double db1Peak = 0,db2Peak=0,db3Peak=0,db4Peak=0,db5Peak=0,db6Peak=0,db7Peak=0,db8Peak=0,db9Peak=0,db10Peak=0;
        ArrayList<String> flowFFT1 = new ArrayList<String>();
        ArrayList<String> flowFFT2 = new ArrayList<String>();
        ArrayList<String> flowFFT3 = new ArrayList<String>();
        ArrayList<String> flowFFT4 = new ArrayList<String>();
        ArrayList<String> flowFFT5 = new ArrayList<String>();
        ArrayList<String> flowFFT6 = new ArrayList<String>();
        ArrayList<String> flowFFT7 = new ArrayList<String>();
        ArrayList<String> flowFFT8 = new ArrayList<String>();
        ArrayList<String> flowFFT9 = new ArrayList<String>();
        ArrayList<String> flowFFT10 = new ArrayList<String>();

        ArrayList<String> flowFFT11 = new ArrayList<String>();
        ArrayList<String> flowFFT12 = new ArrayList<String>();
        ArrayList<String> flowFFT13 = new ArrayList<String>();
        ArrayList<String> flowFFT14 = new ArrayList<String>();

        for(int i=0;i<doublesFFT.length;i++){
            if (i < 8) {
                db1[i] = doublesFFT[i];
            }
            if(i>=4 && i<12){
                db2[i-4] = doublesFFT[i];
            }
            if(i>=8 && i<16){
                db3[i-8] = doublesFFT[i];
            }
            if(i>=12 && i<20){
                db4[i-12] = doublesFFT[i];
            }
            if(i>=16 && i<24){
                db5[i-16] = doublesFFT[i];
            }
            if(i>=20 && i<28){
                db6[i-20] = doublesFFT[i];
            }
            if(i>=24 && i<32){
                db7[i-24] = doublesFFT[i];
            }
            if(i>=28 && i<36){
                db8[i-28] = doublesFFT[i];
            }
            if(i>=32 && i<40){
                db9[i-32] = doublesFFT[i];
            }
            if(i>=36 && i<44){
                db10[i-36] = doublesFFT[i];
            }
            if(i>=40 && i<48){
                db11[i-40] = doublesFFT[i];
            }
            if(i>=44 && i<52){
                db12[i-44] = doublesFFT[i];
            }
            if(i>=48 && i<56){
                db13[i-48] = doublesFFT[i];
            }
            if(i>=52 && i<60){
                db14[i-52] = doublesFFT[i];
            }
        }
        if(db1.length>0) {
            for (int i = 0; i < db1.length; i++) {
                db1FFT[i] = new Complex(db1[i], 0.0);
                Log.d("output", "FFT db1 " + db1FFT[i].toString());
            }
        }
        if(db2.length>0) {
            for (int i = 0; i < db2.length; i++) {
                db2FFT[i] = new Complex(db2[i], 0.0);
                Log.d("output", "FFT db2 " + db2FFT[i].toString());
            }
        }
        if(db3.length>0) {
            for (int i = 0; i < db3.length; i++) {
                db3FFT[i] = new Complex(db3[i], 0.0);
                Log.d("output", "FFT db3 " + db3FFT[i].toString());
            }
        }
        if(db4.length>0) {
            for (int i = 0; i < db4.length; i++) {
                db4FFT[i] = new Complex(db4[i], 0.0);
                Log.d("output", "FFT db4 " + db4FFT[i].toString());
            }
        }
        if(db5.length>0) {
            for (int i = 0; i < db5.length; i++) {
                db5FFT[i] = new Complex(db5[i], 0.0);
                Log.d("output", "FFT db5 " + db5FFT[i].toString());
            }
        }
        if(db6.length>0) {
            for (int i = 0; i < db6.length; i++) {
                db6FFT[i] = new Complex(db6[i], 0.0);
                Log.d("output", "FFT db6 " + db6FFT[i].toString());
            }
        }
        if(db7.length>0) {
            for (int i = 0; i < db7.length; i++) {
                db7FFT[i] = new Complex(db7[i], 0.0);
                Log.d("output", "FFT db7 " + db7FFT[i].toString());
            }
        }
        if(db8.length>0) {
            for (int i = 0; i < db8.length; i++) {
                db8FFT[i] = new Complex(db8[i], 0.0);
                Log.d("output", "FFT db8 " + db8FFT[i].toString());
            }
        }
        if(db9.length>0) {
            for (int i = 0; i < db9.length; i++) {
                db9FFT[i] = new Complex(db9[i], 0.0);
                Log.d("output", "FFT db9 " + db9FFT[i].toString());
            }
        }
        if(db10.length>0) {
            for (int i = 0; i < db10.length; i++) {
                db10FFT[i] = new Complex(db10[i], 0.0);
                Log.d("output", "FFT db10 " + db10FFT[i].toString());
            }
        }
        if(db11.length>0) {
            for (int i = 0; i < db11.length; i++) {
                db11FFT[i] = new Complex(db11[i], 0.0);
                Log.d("output", "FFT db11 " + db11FFT[i].toString());
            }
        }
        if(db12.length>0) {
            for (int i = 0; i < db12.length; i++) {
                db12FFT[i] = new Complex(db12[i], 0.0);
                Log.d("output", "FFT db12 " + db12FFT[i].toString());
            }
        }
        if(db13.length>0) {
            for (int i = 0; i < db13.length; i++) {
                db13FFT[i] = new Complex(db13[i], 0.0);
                Log.d("output", "FFT db13 " + db13FFT[i].toString());
            }
        }
        if(db14.length>0) {
            for (int i = 0; i < db14.length; i++) {
                db14FFT[i] = new Complex(db14[i], 0.0);
                Log.d("output", "FFT db14 " + db14FFT[i].toString());
            }
        }
        //////////////////////////////////////////////////////////////////////////////////
        if(db1FFT!=null && db1FFT.length>0){
            fft(db1FFT);
            for (Complex c : db1FFT) {
                Log.d("output", "FFT1 " + c);
                flowFFT1.add(String.valueOf(c.abs()));
            }
            //db1Peak = stringToDoubleMax(flowFFT1);
        }
        if(db2FFT!=null && db2FFT.length>0){
            fft(db2FFT);
            for (Complex c : db2FFT) {
                Log.d("output", "FFT2 " + c);
                flowFFT2.add(String.valueOf(c.abs()));
            }
            //db2Peak = stringToDoubleMax(flowFFT2);
        }
        if(db3FFT!=null && db3FFT.length>0){
            fft(db3FFT);
            for (Complex c : db3FFT) {
                Log.d("output", "FFT3 " + c);
                flowFFT3.add(String.valueOf(c.abs()));
            }
            //db3Peak = stringToDoubleMax(flowFFT3);
        }
        if(db4FFT!=null && db4FFT.length>0){
            fft(db4FFT);
            for (Complex c : db4FFT) {
                Log.d("output", "FFT4 " + c);
                flowFFT4.add(String.valueOf(c.abs()));
            }
            //db4Peak = stringToDoubleMax(flowFFT4);
        }
        if(db5FFT!=null && db5FFT.length>0){
            fft(db5FFT);
            for (Complex c : db5FFT) {
                Log.d("output", "FFT5 " + c);
                flowFFT5.add(String.valueOf(c.abs()));
            }
            //db5Peak = stringToDoubleMax(flowFFT5);
        }
        if(db6FFT!=null && db6FFT.length>0){
            fft(db6FFT);
            for (Complex c : db6FFT) {
                Log.d("output", "FFT6 " + c);
                flowFFT6.add(String.valueOf(c.abs()));
            }
            //db6Peak = stringToDoubleMax(flowFFT6);
        }
        if(db7FFT!=null && db7FFT.length>0){
            fft(db7FFT);
            for (Complex c : db7FFT) {
                Log.d("output", "FFT7 " + c);
                flowFFT7.add(String.valueOf(c.abs()));
            }
            //db7Peak = stringToDoubleMax(flowFFT7);
        }
        if(db8FFT!=null && db8FFT.length>0){
            fft(db1FFT);
            for (Complex c : db8FFT) {
                Log.d("output", "FFT8 " + c);
                flowFFT8.add(String.valueOf(c.abs()));
            }
            //db8Peak = stringToDoubleMax(flowFFT8);
        }
        if(db9FFT!=null && db9FFT.length>0){
            fft(db9FFT);
            for (Complex c : db9FFT) {
                Log.d("output", "FFT9 " + c);
                flowFFT9.add(String.valueOf(c.abs()));
            }
            //db9Peak = stringToDoubleMax(flowFFT9);
        }
        if(db10FFT!=null && db10FFT.length>0){
            fft(db10FFT);
            for (Complex c : db10FFT) {
                Log.d("output", "FFT10 " + c);
                flowFFT10.add(String.valueOf(c.abs()));
            }
            //db10Peak = stringToDoubleMax(flowFFT10);
        }
        if(db11FFT!=null && db11FFT.length>0){
            fft(db11FFT);
            for (Complex c : db11FFT) {
                Log.d("output", "FFT11 " + c);
                flowFFT11.add(String.valueOf(c.abs()));
            }
            //db10Peak = stringToDoubleMax(flowFFT10);
        }
        if(db12FFT!=null && db12FFT.length>0){
            fft(db12FFT);
            for (Complex c : db12FFT) {
                Log.d("output", "FFT12 " + c);
                flowFFT12.add(String.valueOf(c.abs()));
            }
            //db10Peak = stringToDoubleMax(flowFFT10);
        }
        if(db13FFT!=null && db13FFT.length>0){
            fft(db13FFT);
            for (Complex c : db13FFT) {
                Log.d("output", "FFT13 " + c);
                flowFFT13.add(String.valueOf(c.abs()));
            }
            //db10Peak = stringToDoubleMax(flowFFT10);
        }
        if(db14FFT!=null && db14FFT.length>0){
            fft(db14FFT);
            for (Complex c : db14FFT) {
                Log.d("output", "FFT14 " + c);
                flowFFT14.add(String.valueOf(c.abs()));
            }
            //db10Peak = stringToDoubleMax(flowFFT10);
        }
        List<String> namesList = Arrays.asList( String.valueOf(0),stringToDoubleMax(flowFFT1),stringToDoubleMax(flowFFT2),stringToDoubleMax(flowFFT2),stringToDoubleMax(flowFFT3),stringToDoubleMax(flowFFT4),stringToDoubleMax(flowFFT5),stringToDoubleMax(flowFFT6),stringToDoubleMax(flowFFT7),stringToDoubleMax(flowFFT8),stringToDoubleMax(flowFFT9),stringToDoubleMax(flowFFT10),stringToDoubleMax(flowFFT11),stringToDoubleMax(flowFFT12),stringToDoubleMax(flowFFT13),stringToDoubleMax(flowFFT14),String.valueOf(0));
        FFTPeaksVal.addAll(namesList);
        Log.d("output", "FFTPeaksVal " + FFTPeaksVal);

        return FFTPeaksVal;
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bigd = new BigDecimal(value);
        bigd = bigd.setScale(places, RoundingMode.HALF_UP);
        return bigd.doubleValue();
    }

}