package git.artdeell.dnbootstrap.input.editor;

import android.app.Dialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import git.artdeell.dnbootstrap.R;
import git.artdeell.dnbootstrap.input.ControlButton;
import git.artdeell.dnbootstrap.input.model.ControlButtonData;

public class ButtonEditorDialog extends InputConfigurationEditorDialog {
    private ListPopupWindow listPopupWindow;
    private final TextView[] keycodeViews = new TextView[2];
    private EditText labelEdit;
    //TODO: move visibility configuration into an abstract dialog class (or into LayoutEditorDialog?)
    private CheckBox showInGameCheckbox;
    private CheckBox showInMenuCheckbox;
    private int editingKeycode;
    private final int[] controlButtonKeycodes = new int[2];

    public ButtonEditorDialog() {
        super(R.layout.dialog_button_setup);
    }

    @Override
    protected void inflate(Dialog dialog) {
        super.inflate(dialog);
        View anchorStub = dialog.findViewById(R.id.editor_keycode_select_anchor);
        keycodeViews[0] = dialog.findViewById(R.id.editor_keycode_select_1);
        keycodeViews[1] = dialog.findViewById(R.id.editor_keycode_select_2);
        labelEdit = dialog.findViewById(R.id.editor_label_text);
        showInGameCheckbox = dialog.findViewById(R.id.editor_show_in_game);
        showInMenuCheckbox = dialog.findViewById(R.id.editor_show_in_menu);
        for(int i = 0; i < keycodeViews.length; i++) {
            int keycodeIndex = i;
            keycodeViews[i].setOnClickListener((v)->openKeycodeSpinner(keycodeIndex));
        }

        listPopupWindow = new ListPopupWindow(dialog.getContext());
        listPopupWindow.setAdapter(createSpinner());
        listPopupWindow.setWidth(ListPopupWindow.WRAP_CONTENT);
        listPopupWindow.setAnchorView(anchorStub);
        listPopupWindow.setOnItemClickListener(new KeyCodeSelectedListener());
    }

    @Override
    protected void loadSettings() {
        super.loadSettings();
        ControlButtonData controlButtonData = ((ControlButton) getEditTarget()).controlButtonData;
        for(int i = 0; i < keycodeViews.length; i++) {
            controlButtonKeycodes[i] = controlButtonData.keyCodes[i];
            DisplayKeyCode displayKeyCode = DisplayKeyCode.findEntryByCode(controlButtonKeycodes[i]);
            keycodeViews[i].setText(displayKeyCode.name());
        }
        labelEdit.setText(controlButtonData.label);
        showInGameCheckbox.setChecked(controlButtonData.showInGame);
        showInMenuCheckbox.setChecked(controlButtonData.showInMenu);
    }

    @Override
    protected void saveSettings() {
        super.saveSettings();
        ControlButton editTarget = (ControlButton) getEditTarget();
        ControlButtonData controlButtonData = editTarget.controlButtonData;
        System.arraycopy(controlButtonKeycodes, 0, controlButtonData.keyCodes, 0, controlButtonKeycodes.length);
        controlButtonData.label = labelEdit.getText().toString();
        editTarget.setText(controlButtonData.label);
        controlButtonData.showInGame = showInGameCheckbox.isChecked();
        controlButtonData.showInMenu = showInMenuCheckbox.isChecked();
    }

    private void openKeycodeSpinner(int index) {
        listPopupWindow.show();
        editingKeycode = -1;
        listPopupWindow.setSelection(DisplayKeyCode.findIndexByCode(controlButtonKeycodes[index]));
        editingKeycode = index;
    }

    private ListAdapter createSpinner() {
        return new ArrayAdapter<>(getEditTarget().getContext(), R.layout.key_list_item, DisplayKeyCode.values());
    }

    private class KeyCodeSelectedListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(editingKeycode == -1) return;
            DisplayKeyCode keyCode = DisplayKeyCode.values()[i];
            keycodeViews[editingKeycode].setText(keyCode.name());
            controlButtonKeycodes[editingKeycode] = keyCode.code;
            listPopupWindow.dismiss();
        }
    }
}
