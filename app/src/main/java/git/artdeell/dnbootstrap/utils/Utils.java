package git.artdeell.dnbootstrap.utils;

import android.app.Activity;
import android.app.AlertDialog;

import java.lang.ref.WeakReference;

import git.artdeell.dnbootstrap.R;

public class Utils {
    public static <T> T getWeakReference(WeakReference<T> reference) {
        if(reference == null) return null;
        return reference.get();
    }

    public static void showErrorDialog(Activity activity, Throwable t, boolean exit) {
        activity.runOnUiThread(()-> new AlertDialog.Builder(activity)
                .setTitle(R.string.error)
                .setMessage(ThrowableUtil.printStackTrace(t))
                .setPositiveButton(android.R.string.ok, (d, v)-> {
                    if(exit) activity.finish();
                })
                .show());
    }
}
