package com.suman.voice.voicerecorder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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
import java.util.List;

import static java.lang.Math.abs;

import suman.brose.voicerecorder.R;

public class Plot extends AppCompatActivity {
    ArrayList<String> globalflowRates = new ArrayList<String>();
    ArrayList<String> globalinstantVolumes = new ArrayList<String>();
    ArrayList<String> globalinstantVolumesFFT = new ArrayList<String>();
    ArrayList<String> FFTPeaks = new ArrayList<String>();
    ArrayList<String> RawStrings = new ArrayList<String>();
    double[] FFTPeaksDouble;
    double FEV;
    double FVC;
    double ratio;
    double PEF;
    String Name;
    String Age;
    String Sex;
    String Height;
    String Weight;
    Double CF;
    SharedPreferences myPrefs;
    public static final String OUTPUT_DIRECTORY = "SpiroRecorder";
    private static final String FILE_NAME1 = "RecordData";
    private static final String FILE_NAME2 = "Report";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_activity);
        Bundle extra = getIntent().getExtras();
        globalinstantVolumesFFT = extra.getStringArrayList("flowVolumeFFT");
        FFTPeaks = extra.getStringArrayList("flowFFT");
        RawStrings = extra.getStringArrayList("rawData");
        Name = extra.getString("Name");
        Age = extra.getString("Age");
        Sex = extra.getString("Sex");
        Height = extra.getString("Height");
        Weight = extra.getString("Weight");
        CF = extra.getDouble("CF");

        if (CF == null) {
            CF = 1.0;
            Log.d("Correction Factor null", "CFnull " + String.valueOf(CF));
        }
        if (CF != 0.0) {
            //Log.d("Correction Factor ", "CFnotZero " + String.valueOf(CF));
            myPrefs = getSharedPreferences("prefID", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = myPrefs.edit();
            editor.putString("nameKey", String.valueOf(CF));
            editor.apply();
            editor.commit();
        }
        //ratio = FEV/(FVC+0.01)*100;
        updateUI();
        generateGraphs();

    }

    public void updateUI() {
        TextView fev1 = (TextView) findViewById(R.id.FEV);
        TextView fvc = (TextView) findViewById(R.id.FVC);
        TextView Ratio = (TextView) findViewById(R.id.Ratio);
        TextView pef = (TextView) findViewById(R.id.PEF);

        Log.d("FFT_Peaks", "FFT_Peaks " + FFTPeaks);
        FFTPeaksDouble = parseData(FFTPeaks);
        Log.d("FFT_Peaks", "FFT_Peaks " + convertStringArray(FFTPeaksDouble));
        //double[] FEV_FVC = calFEVandFVC(FFTPeaksDouble);
        myPrefs = getSharedPreferences("prefID", Context.MODE_PRIVATE);
        String name = myPrefs.getString("nameKey", " ");
        //Log.d("Correction Factor Name", "CFname " + name.length());
        if (!name.isEmpty() && name != null && name.length() > 1) {
            //Log.d("Correction Factor Name", "CFname " + name);
            CF = Double.parseDouble(name);
        } else {
            CF = 1.0;
            //Log.d("Correction Factor else", "CFelse " + String.valueOf(CF));
        }
        double cumulativeVolume = 0;
        int k = 1;
        double[] FEV_FVC_FFT = new double[2];
        Log.d("Correction Factor", "CF " + String.valueOf(CF));
        for (int i = 0; i < FFTPeaksDouble.length; i++) {
            if (CF != 0) {
                double flowRate = (FFTPeaksDouble[i]) * k * CF;
                cumulativeVolume += abs((flowRate * 0.064));
            } else {
                double flowRate = (FFTPeaksDouble[i]) * k;
                cumulativeVolume += abs((flowRate * 0.064));
            }
            if (i < FFTPeaksDouble.length * 0.50) {
                FEV_FVC_FFT[0] = cumulativeVolume;
            }
            FEV_FVC_FFT[1] = cumulativeVolume;
            globalinstantVolumes.add(String.valueOf(cumulativeVolume));
        }
        Log.d("output", "FFTPeaksVolume " + globalinstantVolumes);

        Double fev1Val = round(FEV_FVC_FFT[0], 2);//Double fev1Val = round(FEV, 2);
        Double fvcVal = round(FEV_FVC_FFT[1], 2);//round(FVC, 2);
        ratio = round(FEV_FVC_FFT[0] / FEV_FVC_FFT[1] + 0.00001, 2);//round(ratio,2);
        //double[] globalflowRatesDouble = parseData(globalflowRates);
        PEF = round(getMax(FFTPeaksDouble), 2);//round(getMax(globalflowRatesDouble), 2 );
        FEV = fev1Val;
        FVC = fvcVal;

        String fevText = "FEV1 : " + Double.toString(fev1Val);
        String fvcText = "FVC : " + Double.toString(fvcVal);
        String ratioText = "FEV1/FVC :" + Double.toString(ratio * 100);
        String pefText = "PEF :" + Double.toString(PEF);

        fev1.setText(fevText + " L");
        fvc.setText(fvcText + " L");
        Ratio.setText(ratioText + " %");
        pef.setText(pefText + " L");

        ////////////Predicted_Value/////////////////////////////////////////////////////////////////
        //TextView fev1PredText = (TextView) findViewById(R.id.FEVPred);
        TextView fvcPredText = (TextView) findViewById(R.id.FVCPred);
        TextView RatioPredText = (TextView) findViewById(R.id.RatioPred);

        Double fevPred = -3.682 - (Double.valueOf(Age) * 0.024) + (Double.valueOf(Height) * 0.046) - (1.645 * 0.402);
        Double fvcPred = -5.048 - (Double.valueOf(Age) * 0.014) + (Double.valueOf(Height) * 0.054) + (Double.valueOf(Weight) * 0.006) - (1.645 * 0.479);
        Double ratioPred = 102.56 - (Double.valueOf(Age) * 0.679) + (Double.valueOf(Age) * Double.valueOf(Age) * 0.00477) - (Double.valueOf(Weight) * 0.08) - (1.645 * 5.79);

        //String fevTextPred = "FEV1 : " + Double.toString(round(fevPred,2));
        String fvcTextPred = "Pred_FVC : " + Double.toString(round(fvcPred, 2)) + " L";
        String ratioTextPred = "FVC/Pred_FVC :" + Double.toString(round(fvcVal / (fvcPred), 2) * 100.0) + " %";

        //fev1PredText.setText(fevTextPred);
        fvcPredText.setText(fvcTextPred);
        RatioPredText.setText(ratioTextPred);
        ////////////////////////////////////////////////////////////////////////////////////////////
    }

    public boolean generateGraphs() {
        double zero = 0;
        String Zero = String.valueOf(zero);
        GraphView flowGraph = (GraphView) findViewById(R.id.rateTimeGraph);
        FFTPeaksDouble = parseData(FFTPeaks);
        if (FFTPeaksDouble != null && FFTPeaksDouble.length > 0) {
            smoothArray(FFTPeaksDouble, 2);
        }
        for (int i = 0; i < FFTPeaksDouble.length; i++) {
            globalflowRates.add(String.valueOf(FFTPeaksDouble[i]));
        }
        globalflowRates.add(String.valueOf(0));


        LineGraphSeries<DataPoint> flowSeries = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, FFTPeaksDouble[0]),
        });
        /*LineGraphSeries<DataPoint> flowSeries = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, Double.parseDouble(globalflowRates.get(0))),
        });
        if(globalflowRates != null) {
            for (int i = 1; i < globalflowRates.size(); i++) {
                if(globalflowRates.get(i)!=null) {
                    if(Double.parseDouble(globalflowRates.get(i))>0) {
                        flowSeries.appendData(new DataPoint(i*0.1, Double.parseDouble(globalflowRates.get(i))), true, globalflowRates.size());
                    }
                    if(i==globalflowRates.size()-1){
                        flowSeries.appendData(new DataPoint(i*0.1, Double.parseDouble(Zero)), true, globalflowRates.size());
                    }
                }
            }
        }*/
        if (globalflowRates != null) {
            for (int i = 0; i < globalflowRates.size(); i++) {
                if (globalflowRates.get(i) != null) {
                    Log.d("output", "FFTPeaksDouble " + String.valueOf(globalflowRates.get(i)));
                    flowSeries.appendData(new DataPoint(i * 0.25, Double.parseDouble(globalflowRates.get(i))), true, globalflowRates.size());
                }
            }
        }
        Viewport viewport1 = flowGraph.getViewport();
        flowGraph.addSeries(flowSeries);
        flowGraph.getGridLabelRenderer().setHorizontalAxisTitle("Time (s)");
        flowGraph.getGridLabelRenderer().setVerticalAxisTitle("Flow Rate (L/s)");
        flowGraph.getGridLabelRenderer().setLabelHorizontalHeight(40);
        viewport1.setYAxisBoundsManual(true);
        //viewport1.setMinY(0);
        //viewport1.setMaxY(6);
        viewport1.setXAxisBoundsManual(true);
        viewport1.setMinX(0);
        viewport1.setMaxX(8);
        viewport1.setScrollable(true);

        return true;
    }

    public void toMonitor(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void save(View v) {
        FileOutputStream fos = null;
        File sdcard = Environment.getExternalStorageDirectory();
        // to this path add a new directory path
        File dir = new File(sdcard.getAbsolutePath() + "/" + OUTPUT_DIRECTORY);
        // create this directory if not already created
        if (!dir.exists()) {
            dir.mkdir();
        }
        // create the file in which we will write the contents
        Date d = new Date();
        CharSequence seq = DateFormat.format("MM-dd-yy hh-mm-ss", d.getTime());
        String record = FILE_NAME1 + seq.toString() + ".txt";
        File file = new File(dir, record);
        Log.d("output", "RawStrings " + RawStrings);
        StringBuilder csvBuilder = new StringBuilder();
        for (String s : RawStrings) {
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
        String result = FILE_NAME2 + seq.toString() + ".txt";
        String strIncom;
        strIncom = "Results: " + "\r\n" + "Name: " + Name + "\r\n" + "Age: " + Age + " " + "Sex: " + Sex + "\r\n" + "Height: " + Height + "\r\n"
                + "Weight: " + Weight + "\r\n" + "FEV:" + String.valueOf(FEV) + "\r\n" + "FVC:" + String.valueOf(FVC) + "\r\n" + "Ratio:" +
                String.valueOf(ratio) + "\r\n" + "PEF:" + String.valueOf(PEF);
        File file1 = new File(dir, result);
        try {
            FileWriter fw = new FileWriter(file1.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(strIncom);
            bw.close();
            Toast.makeText(getApplicationContext(), "Data saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Data saved_catch", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public double[] parseData(List<String> globalflowRates) {
        double[] ret = new double[globalflowRates.size()];
        for (int j = 0; j < ret.length; j++) {
            if (globalflowRates.get(j) != null) {
                ret[j] = Double.parseDouble(globalflowRates.get(j)) / 5.2;//6.2
            }
        }
        return ret;
    }

    public ArrayList<String> convertStringArray(double[] doubleArray) {
        ArrayList<String> stringArray = new ArrayList<String>();
        if (doubleArray.length > 0) {
            for (int i = 0; i < doubleArray.length; i++) {
                stringArray.add(String.valueOf(doubleArray[i]));
            }
        }
        return stringArray;
    }

    public double[] smoothArray(double[] values, int smoothing) {
        double value = values[0]; // start with the first input
        int len = values.length;
        for (int i = 1; i < len; ++i) {
            double currentValue = values[i];
            value += (currentValue - value) / smoothing;
            values[i] = value;
        }
        return values;
    }

    public static double getMax(double[] inputArray) {
        double maxValue = inputArray[0];
        int index = 0;
        for (int i = 1; i < inputArray.length; i++) {
            if (inputArray[i] > maxValue) {
                maxValue = inputArray[i];
                index = i;
            }
        }
        return maxValue;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bigd = new BigDecimal(value);
        bigd = bigd.setScale(places, RoundingMode.HALF_UP);
        return bigd.doubleValue();
    }
    /*
    public double[] calFEVandFVC(double[] FFTPeaksDouble){
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
        return FEV_FVC_FFT;
    }*/
}
