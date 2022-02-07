package gitlet;

import java.io.File;
import java.util.Objects;

import static gitlet.Cache.*;
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
     * Load a branch file.
     * @param branchName the designated branch name
     * @return the pointed Commit ID
     */
    static String loadBranch(String branchName) {
        if (Objects.equals(branchName, "")) {
            return null;
        } // Special case: loading a "no branch"
        File branchFile = join(BRANCHES_DIR, branchName);
        return readContentsAsString(branchFile);
    }

    /**
     * Write branch branchName to filesystem.
     * @param branchName the designated branch name
     */
    static void writeBranch(String branchName) {
        File branchFile = join(BRANCHES_DIR, branchName);
        writeContents(branchFile, getBranch(branchName));
    }

    /**
     * Make a new branch at the latest commit.
     * @param branchName the designated branch name
     */
    static void mkNewBranch(String branchName, String commitID) {
        cacheBranch(branchName, commitID);
    }

    /**
     * Move the current branch to pointing to commitID.
     * @param commitID the designated commit ID
     */
    static void moveCurrBranch(String commitID) {
        cacheBranch(getHEAD(), commitID);
    }

    /**
     * Load the HEAD file.
     * @return the current branch (HEAD file content)
     */
    static String loadHEAD() {
        return readContentsAsString(HEAD);
    }

    /**
     * Write HEAD to filesystem.
     */
    static void writeHEAD() {
        writeContents(HEAD, getHEAD());
    }

    /**
     * Move HEAD to branchName
     */
    static void moveHEAD(String branchName) {
        cacheHEAD(branchName);
    }

}
