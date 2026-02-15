package git.artdeell.dnbootstrap.input.model;

import android.content.Context;
import android.view.View;

import java.util.Arrays;

import git.artdeell.dnbootstrap.input.Joystick;
import git.artdeell.dnbootstrap.input.LoadableButtonLayout;

public class JoystickData extends VisibilityConfiguration implements ViewCreator {
    public static final String TYPE = "joystick";
    public LoadableButtonLayout.LayoutParams layoutParams;
    public InputConfiguration inputConfiguration;
    public String label;
    public int[] axisCodes;  // For X and Y axes
    public int sensitivity;  // Joystick sensitivity (1-100)
    public boolean autoCenter;  // Auto-center the joystick

    public JoystickData() {}

    public JoystickData(JoystickData src) {
        this.layoutParams = new LoadableButtonLayout.LayoutParams(src.layoutParams);
        this.inputConfiguration = new InputConfiguration(src.inputConfiguration);
        this.label = src.label;
        this.axisCodes = Arrays.copyOf(src.axisCodes, src.axisCodes.length);
        this.sensitivity = src.sensitivity;
        this.autoCenter = src.autoCenter;
        this.showInGame = src.showInGame;
        this.showInMenu = src.showInMenu;
    }

    public static JoystickData createDefault() {
        JoystickData joystickData = new JoystickData();
        joystickData.axisCodes = new int[2];  // X and Y axes
        joystickData.label = "Joystick";
        joystickData.showInGame = true;
        joystickData.showInMenu = true;
        joystickData.layoutParams = new LoadableButtonLayout.LayoutParams(8, 8);
        joystickData.layoutParams.offsetHorizontal = 1;
        joystickData.layoutParams.offsetVertical = 6;
        joystickData.inputConfiguration = new InputConfiguration();
        joystickData.inputConfiguration.sticky = false;
        joystickData.sensitivity = 50;
        joystickData.autoCenter = true;
        return joystickData;
    }

    @Override
    public View createView(Context context) {
        return new Joystick(context, this);
    }

    @Override
    public String getType() {
        return JoystickData.TYPE;
    }
}
