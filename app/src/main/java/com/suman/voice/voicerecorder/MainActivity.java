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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import suman.brose.voicerecorder.R;

/**
 * Created by hp on 4/1/2020.
 */

public class MainActivity extends AppCompatActivity {
    int i = 0;
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
        final TextView tv1,tv2,tv3,tv4,tv5;
       /* tv1 = (TextView)findViewById(R.id.textView);
        tv2 = (TextView)findViewById(R.id.textView2);
        tv3 = (TextView)findViewById(R.id.textView3);
        tv4 = (TextView)findViewById(R.id.textView4);
        tv5 = (TextView)findViewById(R.id.textView5);*/
        final EditText et = (EditText)findViewById(R.id.Name);
        final EditText et1 = (EditText)findViewById(R.id.Age);
      //  final EditText et2 = (EditText)findViewById(R.id.Sex);
        final Spinner genderSpinner = findViewById(R.id.spinnerGender);
        final EditText et3 = (EditText)findViewById(R.id.Height);
        final EditText et4 = (EditText)findViewById(R.id.Weight);
        final Spinner smokingSpinner = findViewById(R.id.smokingSpinner);
        Button button = (Button)findViewById(R.id.button);
        sf = getSharedPreferences(preferences, Context.MODE_PRIVATE);
        Log.d("flowRates","Login"+String.valueOf(sf.getString(saveIt,"")));

//            et.setText(sf.getString(saveIt,""));
//            et1.setText(sf.getString(saveIt1,""));
//            et2.setText(sf.getString(saveIt2,""));
//            et3.setText(sf.getString(saveIt3, ""));
//            et4.setText(sf.getString(saveIt4,""));

           /* tv1.setText(sf.getString(saveIt, ""));
            tv2.setText(sf.getString(saveIt1, ""));
            tv3.setText(sf.getString(saveIt2, ""));
            tv4.setText(sf.getString(saveIt3, ""));
            tv5.setText(sf.getString(saveIt4,""));*/

       /* tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = 0;
            }
        });
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = 1;
            }
        });
        tv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = 2;
            }
        });
        tv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = 3;
            }
        });
        tv5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = 4;
            }
        });*/

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(et.getText().toString().equalsIgnoreCase("") ||
                        et1.getText().toString().equalsIgnoreCase("") ||
                        genderSpinner.getSelectedItem().toString().equalsIgnoreCase("") ||
                        et3.getText().toString().equalsIgnoreCase("") ||
                        et4.getText().toString().equalsIgnoreCase("") ||
                        smokingSpinner.getSelectedItem().toString().equalsIgnoreCase(""))
                {
                    //name
                    if(et.getText().toString().equalsIgnoreCase("")) {
                        et.setError(getString(R.string.this_field_required));
                    }
                    else {
                        et.setError(null);
                    }

                    //age
                    if(et1.getText().toString().equalsIgnoreCase("")) {
                        et1.setError(getString(R.string.this_field_required));
                    }
                    else {
                        et1.setError(null);
                    }

                    //height
                    if(et3.getText().toString().equalsIgnoreCase("")) {
                        et3.setError(getString(R.string.this_field_required));
                    }
                    else {
                        et3.setError(null);
                    }

                    //weight
                    if(et4.getText().toString().equalsIgnoreCase("")) {
                        et4.setError(getString(R.string.this_field_required));
                    }
                    else {
                        et4.setError(null);
                    }

                    Toast.makeText(MainActivity.this, "Please enter details", Toast.LENGTH_SHORT).show();
                    return;
                }


                final String store = et.getText().toString();
                final String store1 = et1.getText().toString();
               // final String store2 = et2.getText().toString();
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
             //   Sex = store2;
                Height = et3.getText().toString();
                Weight = et4.getText().toString();

                Log.d("Login Info","LoginInfo"+Name+" "+Age+" "+Sex);
                Intent intent = new Intent(getApplicationContext(), MainActivity1.class);
                //intent.putExtra("selval", selval);
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
