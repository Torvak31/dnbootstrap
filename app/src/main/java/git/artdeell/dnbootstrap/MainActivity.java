package git.artdeell.dnbootstrap;

import static android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import androidx.annotation.RequiresApi;

import java.io.File;

import git.artdeell.dnbootstrap.glfw.GLFW;
import git.artdeell.dnbootstrap.glfw.KeyCodes;
import git.artdeell.dnbootstrap.input.SoftInputCallback;
import git.artdeell.dnbootstrap.input.TouchCharInput;

public class MainActivity extends Activity implements SoftInputCallback {
    static {
        System.loadLibrary("glfw");
        GLFW.initialize();
        System.loadLibrary("dnbootstrap");
    }

    private TouchCharInput touchCharInput;

    private static boolean isRunning = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SurfaceView surfaceView = findViewById(R.id.surface_view);
        touchCharInput = findViewById(R.id.touch_char_input);
        surfaceView.getHolder().addCallback(new NativeSurfaceListener());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerOnBackInvoked();
        }
        if(!isRunning) {
            isRunning = true;
            new Thread(this::kickstart).start();
        }
    }

    @Override
    public void requestSoftInput() {
        touchCharInput.requestKeyboard();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void registerOnBackInvoked() {
        getOnBackInvokedDispatcher().registerOnBackInvokedCallback(PRIORITY_DEFAULT, this::onBackPressed);
    }

    public void kickstart() {
        try {
            DotnetStarter.kickstart(getFilesDir(), new File(getApplicationInfo().nativeLibraryDir));
        }catch (Throwable t) {
            showErrorDialog(t);
        }
    }

    private void showErrorDialog(Throwable t) {
        Log.w("MainActivity", "Showing error", t);
        runOnUiThread(()->{
            new AlertDialog.Builder(this)
                    .setTitle(R.string.error)
                    .setMessage(ThrowableUtil.printStackTrace(t))
                    .setPositiveButton(android.R.string.ok, (d, v)->finish())
                    .show();
        });
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        GLFW.sendKeyEvent(KeyCodes.GLFW_KEY_ESCAPE, 1, 0);
        GLFW.sendKeyEvent(KeyCodes.GLFW_KEY_ESCAPE, 0, 0);
    }

    public static native void runDotnet(String dotnetRoot, String vsDir);
}