package git.artdeell.dnbootstrap_test.input.editor;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public interface LayoutEditable {
    Context getContext();
    ViewGroup.LayoutParams getLayoutParams();
    void setLayoutParams(ViewGroup.LayoutParams params);
    ViewParent getParent();
    View fullClone();

    LayoutEditorDialog createEditor();
    boolean isCompatibleEditor(LayoutEditorDialog editorDialog);
}
