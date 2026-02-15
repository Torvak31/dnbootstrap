package git.artdeell.dnbootstrap.input;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import git.artdeell.dnbootstrap.glfw.GLFW;
import git.artdeell.dnbootstrap.glfw.KeyCodes;
import git.artdeell.dnbootstrap.glfw.GrabListener;
import git.artdeell.dnbootstrap.input.model.InputConfiguration;
import git.artdeell.dnbootstrap.input.model.VisibilityConfiguration;

public class ControlLayout extends LoadableButtonLayout implements GrabListener {
    private final Rect hitTestRect = new Rect();
    private final HashMap<Integer, HitTarget> lastHitTargets = new HashMap<>();
    private final Set<HitTarget> allHitTargets = new HashSet<>();
    private final HitTarget defaultHitTarget = new HitTarget(new DefaultConsumer());
    private Context cont = getContext();
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ControlLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        GLFW.addGrabListener(this);
    }

    public ControlLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ControlLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlLayout(@NonNull Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    private HitTarget hitTest(int x, int y) {
        for(HitTarget hitTarget : allHitTargets) {
            View child = ((View)hitTarget.consumer);
            hitTestRect.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
            if(hitTestRect.contains(x,y)) return hitTarget;
        }
        return defaultHitTarget;
    }

    @Override
    public void onViewAdded(View view) {
        super.onViewAdded(view);
        if(!(view instanceof LayoutTouchConsumer)) return;
        LayoutTouchConsumer layoutTouchConsumer = (LayoutTouchConsumer)view;
        allHitTargets.add(new HitTarget(layoutTouchConsumer));
        updateVisibility(layoutTouchConsumer);
    }

    @Override
    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        if(allHitTargets.isEmpty()) return;
        if(!(view instanceof LayoutTouchConsumer)) return;
        Iterator<HitTarget> iter = allHitTargets.iterator();
        while(iter.hasNext()) {
            if(iter.next().consumer != view) continue;
            iter.remove();
            break;
        }
    }

    @Override
    protected void onRemoveAllViews() {
        super.onRemoveAllViews();
        allHitTargets.clear();
    }

    private void processPointer(MotionEvent event, int pointer, int action) {
        int pointerId = event.getPointerId(pointer);
        HitTarget lastHit = lastHitTargets.get(pointerId);
        float x = event.getX(pointer), y = event.getY(pointer);

        if(action == MotionEvent.ACTION_MOVE) {
            // Always update position for the INITIAL target only (no re-hit-testing)
            if (lastHit != null && lastHit.isInitialTarget) {
                lastHit.onTouchPosition(pointerId, x - lastHit.consumer.getLeft(),
                        y - lastHit.consumer.getTop());
            }
            return;  // Skip all other logic
        }else if(action == MotionEvent.ACTION_POINTER_UP) {
            if(lastHit != null) lastHit.onTouchState(pointerId, false);
            lastHitTargets.remove(pointerId);
        }else if(action == MotionEvent.ACTION_POINTER_DOWN) {
            HitTarget hit = hitTest((int) x, (int) y);
            if(hit != null) {
                hit.isInitialTarget = true;  // Mark as owning this pointer
                hit.onTouchState(pointerId, true);
            }
            lastHitTargets.put(pointerId, hit);
        }
    }

    private void releaseAllPointers() {
        for(HitTarget target : lastHitTargets.values()) {
            if(target == null) continue;
            target.reset();
        }
        lastHitTargets.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int affectedPointer = -1;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                releaseAllPointers();
                processPointer(event, 0, MotionEvent.ACTION_POINTER_DOWN);
                break;
            case MotionEvent.ACTION_UP:
                releaseAllPointers();
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_POINTER_DOWN:
                affectedPointer = event.getActionIndex();
            case MotionEvent.ACTION_MOVE:
                for(int i = 0; i < event.getPointerCount(); i++) {
                    int reportedAction = MotionEvent.ACTION_MOVE;
                    if(affectedPointer == i) reportedAction = action;
                    processPointer(event, i, reportedAction);
                }
                break;
        }
        return true;
    }

    @Override
    public void onGrabState(boolean isGrabbing) {
        post(this::updateVisibility);
    }

    private void updateVisibility(LayoutTouchConsumer layoutTouchConsumer) {
        boolean isGrabbing = GLFW.isGrabbing();
        VisibilityConfiguration visibilityConfiguration = layoutTouchConsumer.getVisibilityConfiguration();
        boolean visible = visibilityConfiguration.showInGame && isGrabbing;
        visible |= visibilityConfiguration.showInMenu && !isGrabbing;
        layoutTouchConsumer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void updateVisibility() {
        for(HitTarget hitTarget : allHitTargets) {
            if(hitTarget == defaultHitTarget) continue;
            updateVisibility(hitTarget.consumer);
        }
    }

    private class HitTarget {
        public final @NonNull LayoutTouchConsumer consumer;
        private int firstTouchedPointer;
        private boolean lastState;
        private boolean isInitialTarget = false;

        private HitTarget(@NonNull LayoutTouchConsumer consumer) {
            this.consumer = consumer;
            this.isInitialTarget = false;
        }

        public void onTouchState(int pointerId, boolean isTouched) {
            if(pointerId != firstTouchedPointer && firstTouchedPointer != -1) return;
            if(!isTouched) firstTouchedPointer = -1;
            if(isTouched && firstTouchedPointer == -1) firstTouchedPointer = pointerId;
            if(isTouched != lastState) {
                lastState = isTouched;
                consumer.onTouchState(isTouched);
            }
        }

        public void onTouchPosition(int pointerId, float x, float y) {
            if(pointerId != firstTouchedPointer) return;
            consumer.onTouchPosition(x - consumer.getLeft(), y - consumer.getTop());
        }

        public void reset() {
            firstTouchedPointer = -1;
            isInitialTarget = false;
            if(lastState) consumer.onTouchState(false);
            lastState = false;
        }
    }

    public class DefaultConsumer implements LayoutTouchConsumer {
        private final InputConfiguration defaultConfiguration = new InputConfiguration();
        private boolean deltaReady = false;
        private float lastX, lastY;

        private static final long LONG_PRESS_THRESHOLD_MS = 150; // Time required to trigger "Break" (Left Click)
        private static final float MOVE_TOLERANCE_PX = 20.0f;    // Max movement allowed to still count as a "Place" (Right Click)

        private long touchStartTime;
        private float touchStartX, touchStartY;
        private boolean isLeftClickActive = false;
        private boolean hasMovedSignificantly = false;

        Runnable r = new Runnable() {
            @Override
            public void run(){
                GLFW.sendMouseEvent(KeyCodes.GLFW_MOUSE_BUTTON_RIGHT, KeyCodes.GLFW_RELEASE, 0);
            }
        };

        @Override
        public void onTouchState(boolean isTouched) {
            if (!isTouched) {
                // Action Up (Finger Lifted)
                if (isLeftClickActive) {
                    // If we were breaking (Left Click held), release it.
                    GLFW.sendMouseEvent(KeyCodes.GLFW_MOUSE_BUTTON_LEFT, KeyCodes.GLFW_RELEASE, 0);
                } else {
                    // If we were not breaking, check if it was a short tap.
                    // Conditions: Short duration AND didn't move far.
                    long duration = System.currentTimeMillis() - touchStartTime;
                    if (duration < LONG_PRESS_THRESHOLD_MS && !hasMovedSignificantly) {
                        // Trigger Right Click (Place)
                        GLFW.sendMouseEvent(KeyCodes.GLFW_MOUSE_BUTTON_RIGHT, KeyCodes.GLFW_PRESS, 0);
                        // Release after short time else game not registering
                        scheduler.schedule(new Runnable() {
                            @Override
                            public void run() {
                                GLFW.sendMouseEvent(KeyCodes.GLFW_MOUSE_BUTTON_RIGHT, KeyCodes.GLFW_RELEASE, 0);
                            }
                        }, 10, TimeUnit.MILLISECONDS);
                        //Toast.makeText(cont, "Mouse right click (release)", Toast.LENGTH_SHORT).show();
                    }
                }

                // Reset standard tracking
                lastX = lastY = 0;
                deltaReady = false;
            } else {
                // Action Down (Finger Touched)
                // Reset logic flags
                isLeftClickActive = false;
                hasMovedSignificantly = false;
                // Record start time
                touchStartTime = System.currentTimeMillis();
            }
        }

        @Override
        public void onTouchPosition(float x, float y) {
            if (!deltaReady) {
                lastX = x;
                lastY = y;
                // Capture the initial start position on the first movement event
                touchStartX = x;
                touchStartY = y;
                deltaReady = true;
                return;
            }

            // --- Original Cursor Movement Logic ---
            float deltaX = x - lastX;
            float deltaY = y - lastY;
            GLFW.cursorX += deltaX / getWidth();
            GLFW.cursorY += deltaY / getHeight();
            GLFW.sendMousePos();
            lastX = x;
            lastY = y;

            // --- New Break/Drag Logic ---
            // We only check for "Long Press to Break" if we haven't already started breaking
            if (!isLeftClickActive) {
                // Calculate total distance from the initial touch point
                float totalDeltaX = x - touchStartX;
                float totalDeltaY = y - touchStartY;

                // Check if we moved outside the tolerance (Pythagorean theorem)
                // We compare squared distance to avoid expensive Math.sqrt calls
                if ((totalDeltaX * totalDeltaX + totalDeltaY * totalDeltaY) > (MOVE_TOLERANCE_PX * MOVE_TOLERANCE_PX)) {
                    hasMovedSignificantly = true;
                }

                // If we haven't moved too much, check if enough time has passed to trigger "Break"
                if (!hasMovedSignificantly) {
                    long duration = System.currentTimeMillis() - touchStartTime;
                    if (duration >= LONG_PRESS_THRESHOLD_MS) {
                        // Trigger Left Click (Break)
                        GLFW.sendMouseEvent(KeyCodes.GLFW_MOUSE_BUTTON_LEFT, KeyCodes.GLFW_PRESS, 0);
                        isLeftClickActive = true;
                        // Once active, movement is handled by the game logic (cursor updates),
                        // and the left click state remains held until onTouchState(false).
                    }
                }
            }
        }

        @Override
        public void setVisibility(int visibility) {}

        @Override
        public int getLeft() {
            return 0;
        }

        @Override
        public int getTop() {
            return 0;
        }

        @NonNull
        @Override
        public InputConfiguration getInputConfiguration() {
            return defaultConfiguration;
        }

        @Override
        public VisibilityConfiguration getVisibilityConfiguration() {
            return null;
        }
    }
}