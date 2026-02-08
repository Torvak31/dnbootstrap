package git.artdeell.dnbootstrap;

import static android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.InputStream;

import git.artdeell.dnbootstrap.assets.AppDirs;
import git.artdeell.dnbootstrap.glfw.GLFW;
import git.artdeell.dnbootstrap.glfw.KeyCodes;
import git.artdeell.dnbootstrap.input.SoftInputCallback;
import git.artdeell.dnbootstrap.input.TouchCharInput;
import org.apache.commons.io.FileUtils; // Already in your project via AssetsExtractor

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

        // --- NEW: Handle Zip Install on startup ---
        handleZipIntent(getIntent());
    }

    // --- NEW: Handle Zip Install if app is already running ---
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleZipIntent(intent);
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

    // Hide status bar
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API Level 30) and above
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // For older versions (Legacy approach)
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    // =========================================================================
    // MOD INSTALLATION LOGIC STARTS HERE
    // =========================================================================

    private void handleZipIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            Uri zipUri = intent.getData();
            installMod(zipUri);
        }
    }

    private void installMod(Uri uri) {
        new Thread(() -> {
            try {
                // 1. Construct the path you found
                // Root is getFilesDir(), then we go into the Linux-style home structure
                File modsDir = new File(getFilesDir(), "home/.config/VintagestoryData/Mods");

                // 2. Create the directories if they don't exist (just in case)
                if (!modsDir.exists()) {
                    modsDir.mkdirs();
                }

                // 3. Copy the file
                String fileName = getFileName(uri);
                File destFile = new File(modsDir, fileName);

                try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                    FileUtils.copyInputStreamToFile(inputStream, destFile);
                }

                // 4. Success Message
                runOnUiThread(() -> {
                    Toast.makeText(this, "Mod Installed successfully!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = "downloaded_mod.zip";
        }
        return result;
    }
}