package gitlet;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import static gitlet.Utils.*;
import static gitlet.Repository.*;

/**
 * // TODO: make a new compact unit test class.
 * @deprecated
 */
public class InitCommandTest {
    /** Sanity test */
    @Test
    public void sanityTest() throws IOException {
        String[] initCommand = {"init"};
        Main.main(initCommand);
    }

    /** Use the init command.
     * Get the init commit ID by master branch file.
     * Dump the init commit file for checking.
     */
    @Test
    public void initCommitDumpTest() throws IOException {
        Repository.init();
        String initCommitID = readContentsAsString(join(BRANCHES_DIR, "master"));
        File initCommitFile = join(OBJECTS_DIR, initCommitID);
        System.out.println("initCommitID: " + initCommitID);
        DumpObj.main(initCommitFile);
    }
}