package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    /* STATIC METHODS */

    /**
     * Load a branch file from filesystem with designated name.
     * @param branchName the designated branch name
     * @return the pointed Commit ID,
     * null if the branch name is "" (nothing) or there is no such branch.
     */
    static String loadBranch(String branchName) {
        if (Objects.equals(branchName, "")) {
            return null;
        } // Special case: loading a "no branch".
        File branchFile = branchFile(branchName);
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
        List<String> branches = allBranches();
        assert branches != null;
        List<String> branchesCommitID = new ArrayList<>();
        for (String branch : branches) {
            branchesCommitID.add(loadBranch(branch));
        }
        return branchesCommitID;
    }

    /** Print the "Branches" status. */
    static void branchStatus() {
        List<String> branches = allBranches();
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
        File branchFile = branchFile(branchName);
        writeContents(branchFile, getBranch(branchName));
    }

    /**
     * Delete the designated branch in the filesystem.
     * Invoked by the Cache class.
     * @param branchName the designated branch name.
     */
    static void deleteBranch(String branchName) {
        File branchFile = branchFile(branchName);
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
     * Move the designated branch to point to a commit with designated ID.
     * @param branchName the designated branch's name.
     * @param commitID the designated commit's ID.
     */
    static void moveBranch(String branchName, String commitID) {
        cacheBranch(branchName, commitID);
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

    /**
     * Get the File object of a branch with designated name.
     * @param branchName the designated branch name.
     * @return the File object of that branch.
     */
    private static File branchFile(String branchName) {
        File branchFile;
        if (branchName.contains("/")) {
            String folder = branchName.split("/")[0];
            String file = branchName.split("/")[1];
            join(BRANCHES_DIR, folder).mkdir();
            branchFile = join(BRANCHES_DIR, folder, file);
        } else {
            branchFile = join(BRANCHES_DIR, branchName);
        }
        return branchFile;
    }

    /** Return a List of all branches' names. Support fetched remote branches. */
    private static List<String> allBranches() {
        List<String> branches = new ArrayList<>();
        File[] files = BRANCHES_DIR.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                String remoteName = file.getName();
                List<String> remoteBranchNames = plainFilenamesIn(file);
                if (remoteBranchNames == null) {
                    continue;
                }
                for (String remoteBranchName : remoteBranchNames) {
                    String branchName = remoteName + "/" + remoteBranchName;
                    branches.add(branchName);
                }
            } else {
                branches.add(file.getName());
            }
        }
        sortLexico(branches);
        return branches;
    }

}
