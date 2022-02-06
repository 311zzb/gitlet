package gitlet;

import java.io.File;

import static gitlet.Repository.BRANCHES_DIR;
import static gitlet.Repository.HEAD;
import static gitlet.Utils.*;

/**
 * A class where Branch related static methods live.
 * Will never be instantiated.
 *
 * @author XIE Changyuan
 */
public class Branch {

    /**
     * Create a master branch and make it point to the initial commit.
     * @param initialCommitID the ID of the initial commit
     */
    static void mkMasterBranch(String initialCommitID) {
        File masterBranchFile = join(BRANCHES_DIR, "master");
        writeContents(masterBranchFile, initialCommitID);
    }

    static String loadLatestCommitID() {
        return readContentsAsString(join(BRANCHES_DIR, loadCurrBranchName()));
    }

    static String loadCurrBranchName() {
        return readContentsAsString(HEAD);
    }
}
