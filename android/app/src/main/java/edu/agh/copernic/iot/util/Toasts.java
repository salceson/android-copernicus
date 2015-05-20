package edu.agh.copernic.iot.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

public class Toasts {
    private Toasts() {
    }

    public static void show(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void show(Context context, @StringRes int res) {
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
    }
}
