package com.yelloco.payment.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.yelloco.payment.Download.DownloadTask;
import com.yelloco.payment.MainActivity;
import com.yelloco.payment.R;

/**
 * Created by Ahmed on 27-Mar-18.
 */

public class DwnReceiptFragment extends Fragment
{

    String GenerateFIleUrl = "http://41.32.255.229:9090/TMS0000707/myresource/receipt?ch1=1&ch2=1&p1=";
    String GenerateDownloadURL = "http://41.32.255.229:8080/Upload_Download_Service/services/download1?p3=";

    //TextView ErrorTextView;
    private ImageView ReceiptImg;
    private static Button ShowReceiptBtn;
    private Context currContext;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.dwnreceipt_fragment, container, false);

        while(currContext == null)
            currContext = getActivity().getApplicationContext();

        // Attatching Views
        SharedPreferences RetrieveData = getActivity().getSharedPreferences("TransactionIDDB", Context.MODE_PRIVATE);
        ReceiptImg = (ImageView)view.findViewById(R.id.ReceiptImg);
        ShowReceiptBtn = (Button)view.findViewById(R.id.ShowImgBtn);

        //Initialization
        ReceiptImg.setImageResource(R.mipmap.ic_launcher);
        String QRCode = RetrieveData.getString("QRCode", "Empty No QRCode Sent");
        if(QRCode.equals("Empty No QRCode Sent"))
            Log.i("Error", "DwnReceiptFragError 0001: Empty No QRCode" );
        else
            GenerateFIleUrl = GenerateFIleUrl + QRCode;

        //Actions (Listeners)
        ShowReceiptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowReceiptBtn.setVisibility(View.INVISIBLE);
                ShowReceiptBtn.setClickable(false);
                SharedPreferences sharedPref = getActivity().getSharedPreferences("ImageDB", Context.MODE_PRIVATE);
                String ImageName = sharedPref.getString("ImgName", "Image Doesn't Exist Check Your File Explorer");

                if(ImageName.equals("Image Doesn't Exist Check Your File Explorer"))
                    Log.i("Error", "DwnReceiptFragError 0002: Image Doesn't Exist" );
                else
                    ShowImage(ImageName);
            }
        });

        //Download
        DownloadFun(QRCode);
        return view;
    }
    public static void showReceiptBtn(String ReceiptName)
    {
        try
        {
            ShowReceiptBtn.setVisibility(View.VISIBLE);
        }
        catch (Exception e)
        {
            Log.i("Error", "DwnReceiptFragError 0003: " + e.toString());
        }
    }

    public void ShowImage(String ImageName)
    {
        try
        {
            Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + "/"
                    + "QRC Downloaded Files/" + ImageName);
//            if(bmp == null)
//                Log.i("HAMADA",Environment.getExternalStorageDirectory().toString() + "/"
//                        + "QRC Downloaded Files/" + ImageName);
            ReceiptImg.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), true));
        }
        catch(Exception e)
        {
            ReceiptImg.setImageResource(R.drawable.cast_ic_notification_disconnect);
            Log.i("Error", "DwnReceiptFragError 0002: " + e.toString());
        }
    }

    public void DownloadFun(String ImageCodeName)
    {
        final String finalImgCodeName = ImageCodeName;
        try
        {
            final RequestQueue ReqQ = Volley.newRequestQueue(getActivity().getApplicationContext());
            StringRequest StrReq = new StringRequest(Request.Method.GET, GenerateFIleUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response)
                        {
                            //Log.i("Tester", response);
                            ReqQ.stop();
                            new DownloadTask(currContext,GenerateDownloadURL + response + "&p4=" + finalImgCodeName +".png");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.i("Error", "DwnReceiptFragError 0003: " + error.toString());
                    ReqQ.stop();
                    String message = "Unknown Error";
                    if (error instanceof NetworkError) {
                        message = "Cannot connect to Internet...Please check your connection!";
                    } else if (error instanceof ServerError) {
                        message = "The server could not be found. Please try again after some time!!";
                    } else if (error instanceof AuthFailureError) {
                        message = "Cannot connect to Internet...Please check your connection!";
                    } else if (error instanceof ParseError) {
                        message = "Parsing error! Please try again after some time!!";
                    } else if (error instanceof NoConnectionError) {
                        message = "Cannot connect to Internet...Please check your connection!";
                    } else if (error instanceof TimeoutError) {
                        message = "Connection TimeOut! Please check your internet connection.";
                    }
                    Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();

                }
            });
            ReqQ.add(StrReq);
        }
        catch(Exception e)
        {
            Log.i("Error", "DwnReceiptFragError 0004: " + e.toString());
        }
    }

}
