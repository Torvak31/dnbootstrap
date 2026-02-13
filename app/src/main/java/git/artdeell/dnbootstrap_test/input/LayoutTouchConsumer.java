package git.artdeell.dnbootstrap_test.input;

import git.artdeell.dnbootstrap_test.input.model.InputConfiguration;
import git.artdeell.dnbootstrap_test.input.model.VisibilityConfiguration;

public interface LayoutTouchConsumer {
    void onTouchState(boolean isTouched);
    void onTouchPosition(float x, float y);
    void setVisibility(int visibility);
    int getLeft();
    int getTop();
    InputConfiguration getInputConfiguration();
    VisibilityConfiguration getVisibilityConfiguration();
}
