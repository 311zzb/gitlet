package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Cache.*;
import static gitlet.Repository.*;
import static gitlet.Utils.*;

/**
 * This class houses static methods that related to branch and HEAD.
 * It contains methods for loading and writing branch files and the HEAD file.
 * This class will never be instantiated since there are only static methods.
 *
 * @author XIE Changyuan
 */
public class Branch {

    /* STATIC METHODS ------------------------------------------------------------------------------------------------*/

    /**
     * Load a branch file from filesystem with designated name.
     * @param branchName the designated branch name
     * @return the pointed Commit ID, null if the branch name is "" (nothing) or there is no such branch.
     */
    static String loadBranch(String branchName) {
        if (Objects.equals(branchName, "")) {
            return null;
        } // Special case: loading a "no branch".
        File branchFile = join(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) {
            return null;
        } // Special case: loading a branch that do not exist.
        return readContentsAsString(branchFile);
    }

    /** Return true if a branch exists. */
    static boolean existBranch(String branchName) {
        return getBranch(branchName) != null;
    }

    /**
     * Load all branch files from the filesystem.
     * @return a List contains all commit IDs that are pointed by a branch.
     */
    static List<String> loadAllBranches() {
        List<String> branches = plainFilenamesIn(BRANCHES_DIR);
        assert branches != null;
        List<String> branchesCommitID = new ArrayList<>();
        for (String branch : branches) {
            branchesCommitID.add(loadBranch(branch));
        }
        return branchesCommitID;
    }

    /** Print the "Branches" status. */
    static void branchStatus() {
        List<String> branches = plainFilenamesIn(BRANCHES_DIR);
        assert branches != null;
        sortLexico(branches);
        System.out.println("=== Branches ===");
        for (String branch : branches) {
            if (branch.equals(getHEAD())) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.print("\n");
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
     * Delete the designated branch in the filesystem.
     * Invoked by the Cache class.
     * @param branchName the designated branch name.
     */
    static void deleteBranch(String branchName) {
        File branchFile = join(BRANCHES_DIR, branchName);
        branchFile.delete();
    }

    /**
     * Make a new branch with designated name at the latest commit.
     * @param branchName the designated branch name
     */
    static void mkNewBranch(String branchName) {
        cacheBranch(branchName, getLatestCommitID());
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
