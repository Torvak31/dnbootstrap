package git.artdeell.dnbootstrap_test.assets;

import java.io.File;

import git.artdeell.dnbootstrap_test.io.IOUtil;

public class AppDirs {
    public final File base;
    public final File vs;
    public final File runtime;
    public final File fontconfig;

    public AppDirs(File baseDir) {
        base = baseDir;
        vs = new File(baseDir, "vs");
        runtime = new File(baseDir, "dotnet-runtime");
        fontconfig = new File(baseDir, "fonts");
    }

    public boolean isGameInstalled() {
        return IOUtil.checkComponentInstalled(vs);
    }

    public boolean isFullyInstalled() {
        return isGameInstalled() && IOUtil.checkComponentInstalled(runtime) && IOUtil.checkComponentInstalled(fontconfig);
    }
}
