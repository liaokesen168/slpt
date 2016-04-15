package com.example.remotesearchtest.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtil {

    public static void show(Context context, String info) {
        Toast t = Toast.makeText(context, info, Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }

    public static void show(Context context, int info) {
        Toast t = Toast.makeText(context, info, Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }
}
