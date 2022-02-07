package gitlet;

import java.io.File;
import java.util.Objects;

import static gitlet.Cache.*;
import static gitlet.Repository.BRANCHES_DIR;
import static gitlet.Repository.HEAD;
import static gitlet.Utils.*;

/**
 * This class houses static methods that related to branch and HEAD.
 * It contains methods for loading and writing branch files and the HEAD file.
 * This class will never be instantiated since there are only static methods.
 *
 * @author XIE Changyuan
 */
public class Branch {

    /**
     * Load a branch file from filesystem with designated name.
     * @param branchName the designated branch name
     * @return the pointed Commit ID, null if the branch name is "" (nothing)
     */
    static String loadBranch(String branchName) {
        if (Objects.equals(branchName, "")) {
            return null;
        } // Special case: loading a "no branch"
        File branchFile = join(BRANCHES_DIR, branchName);
        return readContentsAsString(branchFile);
    }

    /**
     * Get the branch's information from cache and write it back to filesystem.
     * Invoked by the Cache class.
     * @param branchName the designated branch name
     */
    static void writeBranch(String branchName) {
        File branchFile = join(BRANCHES_DIR, branchName);
        writeContents(branchFile, getBranch(branchName));
    }

    /**
     * Make a new branch with designated name at the latest commit.
     * @param branchName the designated branch name
     */
    static void mkNewBranch(String branchName) {
        cacheBranch(branchName, getLatestCommitRef());
    }

    /**
     * Make the current branch pointing to a designated commit.
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
     * Get the HEAD from cache and write it back to filesystem. Invoked by the Cache class.
     */
    static void writeHEAD() {
        writeContents(HEAD, getHEAD());
    }

    /**
     * Make the HEAD pointing to a designated branch.
     */
    static void moveHEAD(String branchName) {
        cacheHEAD(branchName);
    }

}