package dev.byblos.chart.test;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utility for finding source path for the current sub project. This should be used sparingly.
 * If read-only is needed, then use getResource instead. The primary use-case is for tests
 * around images where there is an option to bless changes and update the golden files in the
 * source resources directory.
 */
public final class SrcPath {

    public static String forProject(String name) {
        var cwd = Paths.get(".").normalize().toAbsolutePath();
        var subProject = cwd.resolve(name);
        if (Files.exists(subProject)) {
            // If not forking for tests or running in the ide, the working directory is typically
            // the root for the overall project.
            return subProject.toString();
        } else if (name.equals(cwd.getFileName().toString())) {
            // If the tests are forked, then the working directory is expected to be the directory
            // for the sub-project.
            return ".";
        }
        // Otherwise, some other case we haven't seen, force the user to figure it out.
        throw new IllegalStateException(String.format("cannot determine source dir for %s, working dir: %s", name, cwd));
    }

    private SrcPath() {
        // Do not instantiate.
    }
}
