package git.artdeell.dnbootstrap.input.editor;

import android.app.Dialog;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.CallSuper;

import git.artdeell.dnbootstrap.R;
import git.artdeell.dnbootstrap.input.Joystick;
import git.artdeell.dnbootstrap.input.model.JoystickData;

public class JoystickEditorDialog extends InputConfigurationEditorDialog {
    private CheckBox showInGameCheckbox;
    private CheckBox showInMenuCheckbox;
    private CheckBox autoCenterCheckbox;

    public JoystickEditorDialog() {
        super(R.layout.dialog_joystick_setup);
    }

    @CallSuper
    @Override
    protected void inflate(Dialog dialog) {
        super.inflate(dialog);
        showInGameCheckbox = dialog.findViewById(R.id.editor_joystick_show_in_game);
        showInMenuCheckbox = dialog.findViewById(R.id.editor_joystick_show_in_menu);
        autoCenterCheckbox = dialog.findViewById(R.id.editor_joystick_auto_center);
    }

    @CallSuper
    @Override
    protected void loadSettings() {
        super.loadSettings();
        Joystick joystick = (Joystick) getEditTarget();
        JoystickData joystickData = joystick.joystickData;
        showInGameCheckbox.setChecked(joystickData.showInGame);
        showInMenuCheckbox.setChecked(joystickData.showInMenu);
        autoCenterCheckbox.setChecked(joystickData.autoCenter);
    }

    @CallSuper
    @Override
    protected void saveSettings() {
        super.saveSettings();
        Joystick joystick = (Joystick) getEditTarget();
        JoystickData joystickData = joystick.joystickData;
        joystickData.showInGame = showInGameCheckbox.isChecked();
        joystickData.showInMenu = showInMenuCheckbox.isChecked();
        joystickData.autoCenter = autoCenterCheckbox.isChecked();
    }
}
