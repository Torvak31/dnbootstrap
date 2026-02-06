package git.artdeell.dnbootstrap.input;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import git.artdeell.dnbootstrap.R;
import git.artdeell.dnbootstrap.glfw.GLFW;
import git.artdeell.dnbootstrap.glfw.KeyCodes;

/**
 * A simple on-screen joystick that integrates with ControlLayout / LayoutTouchConsumer.
 * It maps directions to up to 4 keys (up, down, left, right), including diagonals.
 */
public class ControlJoystick extends View implements LayoutTouchConsumer {

    private final InputConfiguration inputConfiguration = new InputConfiguration();

    // Configurable keycodes (similar to ControlButton styleable approach)
    private int keyCodeUp = 0;
    private int keyCodeDown = 0;
    private int keyCodeLeft = 0;
    private int keyCodeRight = 0;

    // Visual state
    private float centerX;
    private float centerY;
    private float baseRadius;
    private float knobRadius;
    private float knobX;
    private float knobY;

    // Internal state: which directions are currently active
    private boolean pressedUp = false;
    private boolean pressedDown = false;
    private boolean pressedLeft = false;
    private boolean pressedRight = false;

    // Paints for simple rendering
    private final Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint knobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // Add these fields to ControlJoystick
    private boolean hasOrigin = false;
    private float originX;
    private float originY;


    public ControlJoystick(@NonNull Context context) {
        this(context, null);
    }

    public ControlJoystick(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlJoystick(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        // Default visual settings
        basePaint.setColor(Color.argb(160, 0, 0, 0));
        basePaint.setStyle(Paint.Style.FILL);

        knobPaint.setColor(Color.argb(220, 255, 255, 255));
        knobPaint.setStyle(Paint.Style.FILL);

        // Joystick is not sticky, does not move cursor by default
        inputConfiguration.sticky = true; // so ControlLayout will keep sending move updates while pressed
        inputConfiguration.movesCursor = false;

        if (attrs != null) {
            try (TypedArray a = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.ControlJoystick,
                    defStyleAttr,
                    R.style.ControlJoystick)) {

                keyCodeUp = a.getInt(R.styleable.ControlJoystick_joystickKeyUp, 0);
                keyCodeDown = a.getInt(R.styleable.ControlJoystick_joystickKeyDown, 0);
                keyCodeLeft = a.getInt(R.styleable.ControlJoystick_joystickKeyLeft, 0);
                keyCodeRight = a.getInt(R.styleable.ControlJoystick_joystickKeyRight, 0);

            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerX = w / 2f;
        centerY = h / 2f;
        baseRadius = Math.min(w, h) * 0.36f;
        knobRadius = baseRadius * 0.35f;
        resetKnob();
    }

    private void resetKnob() {
        knobX = centerX;
        knobY = centerY;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw base
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint);
        // Draw knob
        canvas.drawCircle(knobX, knobY, knobRadius, knobPaint);
    }

    // ------- LayoutTouchConsumer implementation -------

    @Override
    public void onTouchState(boolean isTouched) {
        if (!isTouched) {
            // finger up
            hasOrigin = false;
            originX = originY = 0f;

            setDirectionState(false, false, false, false);
            resetKnob();
        } else {
            // finger down
            hasOrigin = false; // will be set on first move
        }
    }

    @Override
    public void onTouchPosition(float x, float y) {
        // x,y are already local to this view (0..width / 0..height). [file:2]

        if (!hasOrigin) {
            // Remember the first contact point and treat it as "center"
            originX = x;
            originY = y;
            hasOrigin = true;
        }

        // Displacement from origin, not from absolute center
        float rawDx = x - originX;
        float rawDy = y - originY;

        // Map that displacement into joystick space relative to visual center
        float dx = rawDx;
        float dy = rawDy;

        // Clamp by baseRadius
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > baseRadius) {
            float scale = baseRadius / dist;
            dx *= scale;
            dy *= scale;
        }

        // Knob relative to visual center
        knobX = centerX + dx;
        knobY = centerY + dy;
        invalidate();

        // Normalize to -1..1 for direction
        float nx = dx / baseRadius;
        float ny = dy / baseRadius;

        float deadZone = 0.2f;
        boolean up = false, down = false, left = false, right = false;

        if (Math.abs(nx) > deadZone || Math.abs(ny) > deadZone) {
            double angle = Math.atan2(-ny, nx);
            double deg = Math.toDegrees(angle);

            if (deg >= -22.5 && deg < 22.5) {
                right = true;
            } else if (deg >= 22.5 && deg < 67.5) {
                right = true; up = true;
            } else if (deg >= 67.5 && deg < 112.5) {
                up = true;
            } else if (deg >= 112.5 && deg < 157.5) {
                up = true; left = true;
            } else if (deg >= 157.5 || deg < -157.5) {
                left = true;
            } else if (deg >= -157.5 && deg < -112.5) {
                left = true; down = true;
            } else if (deg >= -112.5 && deg < -67.5) {
                down = true;
            } else if (deg >= -67.5 && deg < -22.5) {
                down = true; right = true;
            }
        }

        setDirectionState(up, down, left, right);
    }

    @NonNull
    @Override
    public InputConfiguration getInputConfiguration() {
        return inputConfiguration;
    }

    // ------- Key event handling -------

    private void setDirectionState(boolean up, boolean down, boolean left, boolean right) {
        if (up != pressedUp) {
            sendKey(keyCodeUp, up);
            pressedUp = up;
        }
        if (down != pressedDown) {
            sendKey(keyCodeDown, down);
            pressedDown = down;
        }
        if (left != pressedLeft) {
            sendKey(keyCodeLeft, left);
            pressedLeft = left;
        }
        if (right != pressedRight) {
            sendKey(keyCodeRight, right);
            pressedRight = right;
        }
    }

    private void sendKey(int code, boolean pressed) {
        if (code == 0) return;
        int state = pressed ? KeyCodes.GLFW_PRESS : KeyCodes.GLFW_RELEASE;
        if (code == -1) {
            GLFW.sendMouseEvent(KeyCodes.GLFW_MOUSE_BUTTON_LEFT, state, 0);
        } else if (code == -2) {
            GLFW.sendMouseEvent(KeyCodes.GLFW_MOUSE_BUTTON_RIGHT, state, 0);
        } else if (code == -3) {
            GLFW.sendMouseEvent(KeyCodes.GLFW_MOUSE_BUTTON_MIDDLE, state, 0);
        } else if (code == -4) {
            // Soft keyboard toggle (same semantics as in ControlButton)
            Context context = getContext();
            if (context instanceof SoftInputCallback) {
                ((SoftInputCallback) context).requestSoftInput();
            }
        } else if (code <= KeyCodes.GLFW_KEY_LAST) {
            GLFW.sendKeyEvent(code, state, 0);
        }
    }
}
