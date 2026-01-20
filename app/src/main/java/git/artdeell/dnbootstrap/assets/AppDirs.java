package git.artdeell.dnbootstrap.assets;

import java.io.File;

import git.artdeell.dnbootstrap.io.IOUtil;

public class AppDirs {
    public final File vs;
    public final File runtime;

    public AppDirs(File baseDir) {
        vs = new File(baseDir, "vs");
        runtime = new File(baseDir, "dotnet-runtime");
    }

    public boolean isFullyInstalled() {
        return IOUtil.checkComponentInstalled(vs) && IOUtil.checkComponentInstalled(runtime);
    }
}
