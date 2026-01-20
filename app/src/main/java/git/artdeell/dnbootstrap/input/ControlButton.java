package git.artdeell.dnbootstrap.input;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import git.artdeell.dnbootstrap.R;
import git.artdeell.dnbootstrap.glfw.GLFW;
import git.artdeell.dnbootstrap.glfw.KeyCodes;

public class ControlButton extends androidx.appcompat.widget.AppCompatTextView implements LayoutTouchConsumer {
    private final InputConfiguration inputConfiguration = new InputConfiguration();
    private int keyCode = 0, keyCode2 = 0;

    public ControlButton(@NonNull Context context) {
        super(context);
        init(context, null, R.attr.controlButtonStyle);
    }

    public ControlButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, R.attr.controlButtonStyle);
        init(context, attrs, R.attr.controlButtonStyle);
    }

    public ControlButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if(defStyleAttr == 0) defStyleAttr = R.attr.controlButtonStyle;
        try(TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ControlButton, defStyleAttr, R.style.ControlButton)) {
            keyCode = attrArray.getInteger(R.styleable.ControlButton_button_keycode, 0);
            keyCode2 = attrArray.getInteger(R.styleable.ControlButton_button_keycode2, 0);
            inputConfiguration.sticky = attrArray.getBoolean(R.styleable.ControlButton_sticky, true);
            inputConfiguration.movesCursor = attrArray.getBoolean(R.styleable.ControlButton_cursorPassThru, false);
        }
    }

    private void executeKeyEvent(int code, int state) {
        if(code == 0) return;
        if(code < 0) {
            Context context = getContext();
            switch (code) {
                case -1:
                    GLFW.sendMouseEvent(KeyCodes.GLFW_MOUSE_BUTTON_LEFT, state, 0);
                    break;
                case -2:
                    GLFW.sendMouseEvent(KeyCodes.GLFW_MOUSE_BUTTON_RIGHT, state, 0);
                    break;
                case -3:
                    GLFW.sendMouseEvent(KeyCodes.GLFW_MOUSE_BUTTON_MIDDLE, state, 0);
                    break;
                case -4:
                    if(context instanceof SoftInputCallback) {
                        ((SoftInputCallback)context).requestSoftInput();
                    }
            }
        }else if(code <= KeyCodes.GLFW_KEY_LAST) {
            GLFW.sendKeyEvent(code, state, 0);
        }
    }

    @Override
    public void onTouchState(boolean isTouched) {
        int state = isTouched ? KeyCodes.GLFW_PRESS : KeyCodes.GLFW_RELEASE;
        executeKeyEvent(keyCode, state);
        executeKeyEvent(keyCode2, state);
    }

    @Override
    public void onTouchPosition(float x, float y) {

    }

    @NonNull
    @Override
    public InputConfiguration getInputConfiguration() {
        return inputConfiguration;
    }
}
