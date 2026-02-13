package git.artdeell.dnbootstrap_test.input.model;

public class VisibilityConfiguration {
    public boolean showInGame;
    public boolean showInMenu;

    public VisibilityConfiguration() {}

    public VisibilityConfiguration(VisibilityConfiguration configuration) {
        this.showInGame = configuration.showInGame;
        this.showInMenu = configuration.showInMenu;
    }
}
