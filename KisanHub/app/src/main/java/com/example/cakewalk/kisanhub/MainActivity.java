package com.example.cakewalk.kisanhub;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;


public class MainActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    // button to show progress dialog
    Button btnShowProgress;
    private static final int REQUEST_WRITE_PERMISSION = 99;
    // Progress Dialog
    private ProgressDialog pDialog;
    private TextView textView;
    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;

    CsvWriterSettings settings = new CsvWriterSettings(); //many options here. Check the documentation
    CsvWriter writer;


    // File url to download
    private static String[] file_url = {"http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Tmax/ranked/UK.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Tmin/ranked/UK.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Tmean/ranked/UK.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Sunshine/ranked/UK.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Rainfall/ranked/UK.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Tmax/ranked/England.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Tmin/ranked/England.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Tmean/ranked/England.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Sunshine/ranked/England.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Rainfall/ranked/England.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Tmax/ranked/Wales.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Tmin/ranked/Wales.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Tmean/ranked/Wales.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Sunshine/ranked/Wales.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Rainfall/ranked/Wales.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Tmax/ranked/Scotland.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Tmin/ranked/Scotland.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Tmean/ranked/Scotland.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Sunshine/ranked/Scotland.txt",
            "http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/Rainfall/ranked/Scotland.txt"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();


        // show progress bar button
        btnShowProgress = (Button) findViewById(R.id.btnProgressBar);
        textView = (TextView) findViewById(R.id.textView);
        textView.setVisibility(View.INVISIBLE);

        /**
         * Show Progress bar click event
         * */
        btnShowProgress.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // starting new Async Task
                File myDirectory = new File(Environment.getExternalStorageDirectory(), "KisanHub");

                if (!myDirectory.exists()) {
                    myDirectory.mkdirs();
                }

                try {
                    settings.getFormat().setDelimiter('|');
                    writer = new CsvWriter(new FileWriter(new File(Environment.getExternalStorageDirectory().toString() + "/KisanHub/weather.csv")), settings);
                    writer.writeHeaders("region_code", "weather_param", "year", "key", "value");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                new DownloadFileFromURL().execute(file_url);
            }
        });
    }

    /**
     * Showing Dialog
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    /**
     * Background Async Task to download file
     */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;

            try {
                for (int i = 0; i < f_url.length; i++) {

                    URL url = new URL(f_url[i]);
                    URLConnection conection = url.openConnection();
                    conection.connect();
                    // getting file length
                    int lenghtOfFile = conection.getContentLength();

                    // input stream to read file - with 8k buffer
                    InputStream input = new BufferedInputStream(url.openStream(), 8192);

                    // Output stream to write file
                    OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + setFileName(f_url[i]));
                    //Log.e("File URL", "/KisanHub/" + f_url[i].substring(f_url[i].lastIndexOf('/') + 1, f_url[i].length()));

                    byte data[] = new byte[5 * 1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();
                }

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);


            getFilesFromFolder();
            textView.setVisibility(View.VISIBLE);
        }

    }

    private void getFilesFromFolder() {
        String path = Environment.getExternalStorageDirectory().toString() + "/KisanHub";

        //Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        //Log.d("Files", "Size: " + files.length);
//
        //readFile(files[0]);
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".txt")) {
                //Log.e("Files", "FileName:" + files[i].getName());
                //return files[i].getName();
                File file = files[i];
                readFile(file);

            }
        }
        writer.close();

    }


    private String setFileName(String url) {
        String file = null;
        if (url.matches("(?i).*Tmax.*")) {
            file = "Max_temp";
        } else if (url.matches("(?i).*Tmin.*")) {
            file = "Min_temp";
        } else if (url.matches("(?i).*Tmean.*")) {
            file = "Mean_temp";
        } else if (url.matches("(?i).*Sunshine.*")) {
            file = "Sunshine";
        } else if (url.matches("(?i).*Rainfall.*")) {
            file = "Rainfall";
        }

        String fileName = "/KisanHub/" + file + url.substring(url.lastIndexOf('/') + 1, url.length());
        //Log.e("File Name ", fileName);
        return fileName;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //openFilePicker();
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        } else {

        }
    }


    private void readFile(File file) {
        int line = 8;
        String[] months = null;
        String regionCode = "N/A";
        String weather_param = "N/A";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            for (int i = 1; i < 7; i++) {
                if (i < 2) {
                    String arr[] = reader.readLine().split(" ", 2);
                    //Log.e("First Line", reader.readLine());
                    regionCode = arr[0].toString();
                    Log.e("Region Code", regionCode);
                    String secondWord = arr[1].toString();
                    Log.e("Second Word", secondWord);

                    if (secondWord.startsWith("Maximum")) {
                        weather_param = "Max Temp";
                        Log.e("Weather Param", weather_param);
                    } else if (secondWord.startsWith("Minimum")) {
                        weather_param = "Min Temp";
                        Log.e("Weather Param", weather_param);
                    } else if (secondWord.startsWith("Mean")) {
                        weather_param = "Mean Temp";
                        Log.e("Weather Param", weather_param);
                    } else if (secondWord.startsWith("Sunshine")) {
                        weather_param = "Sunshine";
                        Log.e("Weather Param", weather_param);
                    } else if (secondWord.startsWith("Rainfall")) {
                        weather_param = "Rainfall";
                        Log.e("Weather Param", weather_param);
                    }
                }
                Log.d("Data", reader.readLine());
            }

            String data = null;
            if ((data = reader.readLine()) != null) {
//              String monthData = data.replaceAll("Year", "").trim();
                String monthData = data.trim();
                months = monthData.split("\\s+");
                Log.d("Data", "Months : " + Arrays.toString(months));
            } else {
                pDialog.dismiss();
                Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show();
            }

            while ((data = reader.readLine()) != null) { //loop will run from 8th line
                String month = "N/A";
                data = data.trim();
                String[] tt = data.split("\\s+");
                Log.d("Length : " + tt.length + " Data", "S : " + Arrays.toString(tt));
                for (int i = 0; i < tt.length; i += 2) {
                    if (i >= 24)
                        break;
                    //Log.d("Data", "D " + i + ": " + regionCode + " , " + weather_param + " , " + tt[i + 1] + ", " + tt[i]);
                    //region_code,weather_param,year,key,value
                    if (i < 2) {
                        month = "JAN";
                    } else if (i < 4) {
                        month = "FEB";
                    } else if (i < 6) {
                        month = "MAR";
                    } else if (i < 8) {
                        month = "APR";
                    } else if (i < 10) {
                        month = "MAY";
                    } else if (i < 12) {
                        month = "JUN";
                    } else if (i < 14) {
                        month = "JUL";
                    } else if (i < 16) {
                        month = "AUG";
                    } else if (i < 18) {
                        month = "SEP";
                    } else if (i < 20) {
                        month = "OCT";
                    } else if (i < 22) {
                        month = "NOV";
                    } else if (i < 24) {
                        month = "DEC";
                    }
                    writer.writeRow(regionCode, weather_param, tt[i + 1], month, tt[i]);

                }
            }
        } catch (Exception e) {
            Log.e("---ERROR----", "Line Number : " + line);
            e.printStackTrace();
        }
    }

}