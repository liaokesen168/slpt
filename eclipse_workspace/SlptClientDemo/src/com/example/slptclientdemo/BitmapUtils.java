package com.example.slptclientdemo;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtils {

    public static byte[] getbyteFromResource(Context context, int resId) {
        if (resId <= 0) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        return Bitmap2Bytes(bitmap);
    }

    
    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

}
