package git.artdeell.dnbootstrap.input.editor;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
    private Button backgroundColorButton;
    private ImageView backgroundAssetSelector;
    private int editingKeycode;
    private final int[] controlButtonKeycodes = new int[2];
    private int selectedBackgroundColor;
    private String selectedBackgroundAsset;
    private String[] backgroundAssets;
    private static final String ASSET_PREFIX = "btn_icon_";

    private static final int[] BACKGROUND_COLORS = {
        0x80000000, // Transparent
        0x804CAF50, // Green
        0x802196F3, // Blue
        0x80FF9800, // Orange
        0x80F44336  // Red
    };

    public ButtonEditorDialog() {
        super(R.layout.dialog_button_setup);
        backgroundAssets = findDrawableNamesByPrefix(ASSET_PREFIX);
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
        backgroundColorButton = dialog.findViewById(R.id.editor_background_color_button);
        backgroundAssetSelector = dialog.findViewById(R.id.editor_background_asset_selector);
        for(int i = 0; i < keycodeViews.length; i++) {
            int keycodeIndex = i;
            keycodeViews[i].setOnClickListener((v)->openKeycodeSpinner(keycodeIndex));
        }

        listPopupWindow = new ListPopupWindow(dialog.getContext());
        listPopupWindow.setAdapter(createSpinner());
        listPopupWindow.setWidth(ListPopupWindow.WRAP_CONTENT);
        listPopupWindow.setAnchorView(anchorStub);
        listPopupWindow.setOnItemClickListener(new KeyCodeSelectedListener());

        backgroundColorButton.setOnClickListener(v -> openColorPicker());
        backgroundAssetSelector.setOnClickListener(v -> openAssetPickerDialog());
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
        selectedBackgroundColor = controlButtonData.backgroundColor;
        selectedBackgroundAsset = controlButtonData.backgroundAssetId;
        updateColorButtonAppearance();
        updateAssetSelectorAppearance();
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
        controlButtonData.backgroundColor = selectedBackgroundColor;
        controlButtonData.backgroundAssetId = selectedBackgroundAsset;
        editTarget.applyBackground();
    }

    private void openKeycodeSpinner(int index) {
        listPopupWindow.show();
        editingKeycode = -1;
        listPopupWindow.setSelection(DisplayKeyCode.findIndexByCode(controlButtonKeycodes[index]));
        editingKeycode = index;
    }

    private void openColorPicker() {
        Context context = backgroundColorButton.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Color");

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 5)); // 5 columns for colors

        // Convert int[] to List<GridItem>
        List<GridItem> items = new ArrayList<>();
        for (int color : BACKGROUND_COLORS) {
            items.add(new ColorItem(color));
        }

        GridPickerAdapter adapter = new GridPickerAdapter(
                context,
                items,
                selectedBackgroundColor, // Pass current selection
                (value) -> {
                    selectedBackgroundColor = (Integer) value;
                    updateColorButtonAppearance();
                }
        );
        recyclerView.setAdapter(adapter);

        builder.setView(recyclerView);
        AlertDialog dialog = builder.create();
        adapter.setDialog(dialog);
        dialog.show();
    }

    private void updateColorButtonAppearance() {
        backgroundColorButton.setBackgroundColor(selectedBackgroundColor);
        int textColor = ColorUtils.calculateLuminance(selectedBackgroundColor) > 0.5
                ? Color.BLACK
                : Color.WHITE;
        backgroundColorButton.setTextColor(textColor);
    }

    private void openAssetPickerDialog() {
        Context context = backgroundAssetSelector.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Icon");

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 3));

        // Convert String[] to List<GridItem>
        List<GridItem> items = new ArrayList<>();
        if (backgroundAssets != null) {
            for (String asset : backgroundAssets) {
                items.add(new IconItem(asset));
            }
        }

        GridPickerAdapter adapter = new GridPickerAdapter(
                context,
                items,
                selectedBackgroundAsset, // Pass current selection
                (value) -> {
                    selectedBackgroundAsset = (String) value;
                    updateAssetSelectorAppearance();
                }
        );
        recyclerView.setAdapter(adapter);

        builder.setView(recyclerView);
        AlertDialog dialog = builder.create();
        adapter.setDialog(dialog);
        dialog.show();
    }

    private void updateAssetSelectorAppearance() {
        String assetName = selectedBackgroundAsset;
        if("None".equals(assetName) || assetName == null || assetName.isEmpty()) {
            backgroundAssetSelector.setImageDrawable(null);
            backgroundAssetSelector.setBackgroundColor(0xff181818);
            // Optionally set a placeholder drawable for "None"
            // backgroundAssetSelector.setImageResource(R.drawable.ic_placeholder);
        } else {
            int drawableId = backgroundAssetSelector.getContext().getResources().getIdentifier(
                    assetName,
                    "drawable",
                    backgroundAssetSelector.getContext().getPackageName()
            );
            if(drawableId != 0) {
                backgroundAssetSelector.setImageResource(drawableId);
            } else {
                backgroundAssetSelector.setImageDrawable(null);
            }
        }
    }

    private ListAdapter createSpinner() {
        return new ArrayAdapter<>(getEditTarget().getContext(), R.layout.key_list_item, DisplayKeyCode.values());
    }

    private String[] findDrawableNamesByPrefix(String prefix) {
        List<String> names = new ArrayList<>();

        try {
            for (Field field : R.drawable.class.getFields()) {
                String name = field.getName();
                if (name.startsWith(prefix)) {
                    names.add(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return names.toArray(new String[0]);
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

    public interface GridItem {
        void bind(ImageView view, Context context);
        Object getValue();
    }
    private static class IconItem implements GridItem {
        private final String assetName;

        public IconItem(String assetName) {
            this.assetName = assetName;
        }

        @Override
        public void bind(ImageView view, Context context) {
            if ("None".equals(assetName)) {
                view.setImageDrawable(null);
                view.setBackgroundColor(Color.LTGRAY);
            } else {
                int drawableId = context.getResources().getIdentifier(assetName, "drawable", context.getPackageName());
                if (drawableId != 0) {
                    view.setImageResource(drawableId);
                } else {
                    view.setImageDrawable(null);
                }
                // Reset background to transparent so the icon shows clearly
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        @Override
        public Object getValue() {
            return assetName;
        }
    }

    private static class ColorItem implements GridItem {
        private final int color;

        public ColorItem(int color) {
            this.color = color;
        }

        @Override
        public void bind(ImageView view, Context context) {
            view.setImageDrawable(null); // Clear any icon
            view.setBackgroundColor(color);
        }

        @Override
        public Object getValue() {
            return color;
        }
    }

    private static class GridPickerAdapter extends RecyclerView.Adapter<GridPickerAdapter.ViewHolder> {
        private final List<GridItem> items;
        private final Context context;
        private Object currentSelection;
        private final OnItemSelectedListener listener;
        private AlertDialog dialog;

        public interface OnItemSelectedListener {
            void onSelected(Object value);
        }

        public GridPickerAdapter(Context context, List<GridItem> items, Object currentSelection, OnItemSelectedListener listener) {
            this.context = context;
            this.items = items;
            this.currentSelection = currentSelection;
            this.listener = listener;
        }

        public void setDialog(AlertDialog dialog) {
            this.dialog = dialog;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Create a FrameLayout to hold the content and the selection overlay
            FrameLayout layout = new FrameLayout(context);
            int size = (int) (80 * context.getResources().getDisplayMetrics().density); // 80dp
            int padding = (int) (4 * context.getResources().getDisplayMetrics().density); // 4dp

            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(size, size);
            layout.setLayoutParams(lp);
            layout.setPadding(padding, padding, padding, padding);

            // The content view (ImageView for both icons and colors)
            ImageView content = new ImageView(context);
            content.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            content.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // The selection overlay (a simple semi-transparent layer)
            View overlay = new View(context);
            overlay.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            overlay.setBackgroundColor(Color.parseColor("#80000000")); // Semi-transparent black
            overlay.setVisibility(View.GONE);

            layout.addView(content);
            layout.addView(overlay);

            return new ViewHolder(layout, content, overlay);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GridItem item = items.get(position);

            // Bind data (icon or color)
            item.bind(holder.content, context);

            // Show selection overlay if this item is selected
            if (item.getValue().equals(currentSelection)) {
                holder.selectionOverlay.setVisibility(View.VISIBLE);
            } else {
                holder.selectionOverlay.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSelected(item.getValue());
                }
                currentSelection = item.getValue();
                notifyDataSetChanged();
                if (dialog != null) {
                    dialog.dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView content;
            View selectionOverlay;

            ViewHolder(View itemView, ImageView content, View selectionOverlay) {
                super(itemView);
                this.content = content;
                this.selectionOverlay = selectionOverlay;
            }
        }
    }
}
