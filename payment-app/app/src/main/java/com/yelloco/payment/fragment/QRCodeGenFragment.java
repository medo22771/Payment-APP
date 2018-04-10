package com.yelloco.payment.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.yelloco.payment.MainActivity;
import com.yelloco.payment.R;

import java.io.IOException;

import yogesh.firzen.mukkiasevaigal.S;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;


public class QRCodeGenFragment extends BaseFragment
{
    private static ImageView QRImage;
    private Button DwnFragBtn;
    //private MainActivity MainActObj; // init object in createView and use it's functions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.qrcodegen_fragment, container, false);

        SharedPreferences RetrieveData = getActivity().getSharedPreferences("TransactionIDDB", Context.MODE_PRIVATE);
        String QRCode = RetrieveData.getString("QRCode", "Empty No QRCode Sent");

        QRImage = (ImageView)view.findViewById(R.id.QRCodeImage);
        DwnFragBtn = (Button)view.findViewById(R.id.DwnBtn);

        try
        {
            if(QRCode.equals("Empty No QRCode Sent"))
                ;
            else
                generateImageCode(QRCode);
        }
        catch (Exception e)
        {}

        //Download Electronic Receipt Button
        DwnFragBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(QRImage.getTag().equals("Loaded"))
                {
                    QRImage.setTag("NotLoaded");

                    DwnReceiptFragment nextFrag = new DwnReceiptFragment();
                    MainActivity.switchFragment(nextFrag, false);
//                    DwnReceiptFragment nextFrag= new DwnReceiptFragment();
//                    getActivity().getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.main_container, nextFrag)
//                            .addToBackStack(null)
//                            .commit();
                }
                else
                    Toast.makeText(getActivity().getApplicationContext(), "QRCode Image Not Generated Yet, Can't Download...", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    public void generateImageCode(String ImgCode) throws IOException, WriterException
    {
        Bitmap bitmap = null;
        try
        {
            bitmap = encodeAsBitmap(ImgCode, 400, 300);
            QRImage.setImageBitmap(bitmap);
            QRImage.setTag("Loaded");

        } catch (WriterException e)
        {   e.printStackTrace();    }

    }

    Bitmap encodeAsBitmap(String str, int QRWidth, int QRHeight) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, QRWidth, QRHeight, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

}
