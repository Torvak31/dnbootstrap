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

public class JoystickEditorDialog extends InputConfigurationEditorDialog implements SeekBar.OnSeekBarChangeListener {
    private EditText labelEdit;
    private CheckBox showInGameCheckbox;
    private CheckBox showInMenuCheckbox;
    private CheckBox autoCenterCheckbox;
    private SeekBar sensitivitySeek;
    private TextView sensitivityLabel;
    private int sensitivity;

    public JoystickEditorDialog() {
        super(R.layout.dialog_joystick_setup);
    }

    @CallSuper
    @Override
    protected void inflate(Dialog dialog) {
        super.inflate(dialog);
        labelEdit = dialog.findViewById(R.id.editor_joystick_label_text);
        showInGameCheckbox = dialog.findViewById(R.id.editor_joystick_show_in_game);
        showInMenuCheckbox = dialog.findViewById(R.id.editor_joystick_show_in_menu);
        autoCenterCheckbox = dialog.findViewById(R.id.editor_joystick_auto_center);
        sensitivitySeek = dialog.findViewById(R.id.editor_joystick_sensitivity_seek);
        sensitivityLabel = dialog.findViewById(R.id.editor_joystick_sensitivity_label);
        sensitivitySeek.setOnSeekBarChangeListener(this);
    }

    @CallSuper
    @Override
    protected void loadSettings() {
        super.loadSettings();
        Joystick joystick = (Joystick) getEditTarget();
        JoystickData joystickData = joystick.joystickData;
        labelEdit.setText(joystickData.label);
        showInGameCheckbox.setChecked(joystickData.showInGame);
        showInMenuCheckbox.setChecked(joystickData.showInMenu);
        autoCenterCheckbox.setChecked(joystickData.autoCenter);
        sensitivity = joystickData.sensitivity;
        sensitivitySeek.setProgress(sensitivity);
        sensitivityLabel.setText(sensitivity + "%");
    }

    @CallSuper
    @Override
    protected void saveSettings() {
        super.saveSettings();
        Joystick joystick = (Joystick) getEditTarget();
        JoystickData joystickData = joystick.joystickData;
        joystickData.label = labelEdit.getText().toString();
        joystickData.showInGame = showInGameCheckbox.isChecked();
        joystickData.showInMenu = showInMenuCheckbox.isChecked();
        joystickData.autoCenter = autoCenterCheckbox.isChecked();
        joystickData.sensitivity = sensitivity;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        sensitivity = progress;
        sensitivityLabel.setText(progress + "%");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
