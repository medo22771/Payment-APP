package com.yelloco.payment.Download;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.yelloco.payment.MainActivity;
import com.yelloco.payment.R;
import com.yelloco.payment.fragment.DwnReceiptFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Desktop1 on 27-Mar-18.
 */

public class DownloadTask extends AppCompatActivity
{
    private static final String TAG = "Download Task";
    private Context context;
    private String downloadUrl = "";
    private String downloadFileName = "";
    private ProgressDialog pDialog;

    public DownloadTask(Context context, String downloadUrl)
    {
        this.context = context;
        this.downloadUrl = downloadUrl;

        //Create file name by picking download file name from URL
        downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf( '=' ) + 1,downloadUrl.length());

        //Start Downloading Task
        new DownloadingTask().execute();
    }

    public boolean isSDCardPresent()
    {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        return false;
    }

    private class DownloadingTask extends AsyncTask<Void, Void, Void>
    {
        File apkStorage = null;
        File outputFile = null;

        @Override
        protected void onPreExecute() {
            pDialog=new ProgressDialog(context);
            pDialog.setMessage("Downloading...");
            pDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            try
            {   pDialog.show(); }
            catch(Exception e)
            {   Log.i("Error" , "DwnldTask 0003: " + e.toString()); }
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (outputFile != null)
                {
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run() {
                            pDialog.dismiss();
                            Toast.makeText(context, "Downloaded Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }, 400);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DwnReceiptFragment.showReceiptBtn(downloadFileName);
                        }
                    },1200);
                }
                else
                    Toast.makeText(context, "Download Failed" , Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                Log.i("Error" , "DwnldTask 0001: " + e.toString());
            }
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            try
            {
                //Create Download URl
                URL url = new URL(downloadUrl);

                //Open Url Connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //Set Request Method to "GET" since we are grtting data
                conn.setRequestMethod("GET");

                //connect the URL Connection
                conn.connect();

                //If Connection response is not OK then show Logs
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + conn.getResponseCode()
                            + " " + conn.getResponseMessage());
                }


                //Get File if SD card is present
                if (isSDCardPresent())
                {
                    apkStorage = new File(Environment.getExternalStorageDirectory() + "/"
                            + "QRC Downloaded Files");

                    SharedPreferences sharedPref = context.getSharedPreferences("ImageDB", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("ImgName",downloadFileName);
                    editor.apply();
                }
                else
                    Toast.makeText(context, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();

                //If File is not present create directory
                if (!apkStorage.exists())
                {
                    apkStorage.mkdirs();
                    Log.e(TAG, "Directory Created.");
                }

                //Create Output file in Main File (Location, FileName)
                outputFile = new File(apkStorage, downloadFileName);

                //Create New File if not present
                if (!outputFile.exists())
                {
                    outputFile.createNewFile();
                    Log.e(TAG, "File Created");
                }

                //Get OutputStream for NewFile Location
                FileOutputStream DownloadedFile = new FileOutputStream(outputFile);

                //Get InputStream for connection
                InputStream ReturnedData = conn.getInputStream();

                byte[] buffer = new byte[1024];//Set buffer type
                int len1 = 0;//init length
                while ((len1 = ReturnedData.read(buffer)) != -1) {
                    DownloadedFile.write(buffer, 0, len1);//Write new file
                }

                //Close all connection after doing task
                DownloadedFile.close();
                ReturnedData.close();

            }
            catch (Exception e)
            {
                outputFile = null;
                Log.i("Error" , "DwnldTask 0002: " + e.toString());
            }

            return null;
        }
    }
}

