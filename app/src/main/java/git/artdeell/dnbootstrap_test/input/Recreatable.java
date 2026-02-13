package git.artdeell.dnbootstrap_test.input;

import androidx.annotation.NonNull;

import git.artdeell.dnbootstrap_test.input.model.ViewCreator;

public interface Recreatable {
    @NonNull
    ViewCreator getCreator();
}
