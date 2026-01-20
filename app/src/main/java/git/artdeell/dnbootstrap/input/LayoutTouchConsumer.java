package git.artdeell.dnbootstrap.input;

import androidx.annotation.NonNull;

public interface LayoutTouchConsumer {
    void onTouchState(boolean isTouched);
    void onTouchPosition(float x, float y);
    int getLeft();
    int getTop();
    @NonNull InputConfiguration getInputConfiguration();
}
