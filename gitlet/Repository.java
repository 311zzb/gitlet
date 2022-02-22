package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gitlet.Branch.*;
import static gitlet.Cache.*;
import static gitlet.Commit.*;
import static gitlet.Stage.*;
import static gitlet.Utils.*;


/**
 * A class houses static methods related to the whole repository.
 * This class will handle all actual Gitlet commands by invoking methods in other classes correctly.
 * It also sets up persistence and do additional error checking.
 *
 *  @author XIE Changyuan
 */
public class Repository {

    /** The current working directory. */
    static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The HEAD file. */
    static final File HEAD = join(GITLET_DIR, "HEAD");
    /** The STAGE file. */
    static final File STAGE = join(GITLET_DIR, "STAGE");
    /** The objects directory. */
    static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    /** The branches directory. */
    static final File BRANCHES_DIR = join(GITLET_DIR, "branches");


    /* INIT COMMAND */

    /**
     * Execute the init command.
     * 1. Set up the repository
     * 2. Create an initial commit
     */
    public static void init() {
        try {
            setUpRepository();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mkCommit("initial commit");
    }

    /**
     * Set up the repository
     * 1. Set up persistence directories
     * 2. Make the default branch "master" which is pointing null for now (no pun intended)
     * 3. Make the HEAD pointing to the master branch
     * 4. Make a new staging area
     */
    private static void setUpRepository() throws IOException {
        setUpPersistence();
        mkNewBranch("master");
        moveHEAD("master");
        mkNewStage();
    }

    /** Set up the persistence directories. */
    static void setUpPersistence() throws IOException {
        if (GITLET_DIR.exists()) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        } // Special case: Abort if there is already a Gitlet version-control system in the current directory.
        BRANCHES_DIR.mkdirs();
        OBJECTS_DIR.mkdirs();
        HEAD.createNewFile();
        STAGE.createNewFile();
    }


    /* ADD COMMAND */

    /**
     * Execute the add command.
     * 1. Add a copy of the file as it currently exists to the staging area
     * 2. Remove the file from staging for removal if it was at the time of the command // TODO
     * @param fileName the designated file name
     */
    public static void add(String fileName) {
        assertGITLET();
        File targetFile = join(CWD, fileName);
        if (!targetFile.exists()) {
            throw new GitletException("File does not exist.");
        } // Special case: abort if such file does not exist
        addToStage(fileName);
    }

    /* COMMIT COMMAND */

    /**
     * Execute the commit command.
     * @param message the commit message
     */
    public static void commit(String message) {
        assertGITLET();
        // TODO: assert the staging area is not empty. This is not implemented for testing convenience
        if (message.equals("")) {
            throw new GitletException("Please enter a commit message.");
        } // Special case: abort if message is blank
        mkCommit(message);
    }

    /* RM COMMAND */

    /**
     * Execute the rm command.
     * 1. Abort if the file is neither staged nor tracked by the head commit.
     * 2. If the file is currently staged for addition, unstage it.
     * 3. If the file is tracked in the current commit, stage it for removal and remove it from the CWD.
     * Stage for removal: add a {fileName : null} pair into the staging area.
     * @param fileName the designated file name.
     */
    public static void rm(String fileName) {
        assertGITLET();
        Tree stage = getStage();
        Commit head = getLatestCommit();
        if (!stage.containsFile(fileName) && !head.containsFile(fileName)) {
            throw new GitletException("No reason to remove the file.");
        } // Special case: if the file is neither staged nor tracked by the head commit, print the error message
        if (stage.containsFile(fileName)) {
            removeFromStage(fileName);
        }
        if (head.containsFile(fileName)) {
            restrictedDelete(fileName); // Remove file from CWD
            addToStage(fileName); // Add {fileName - null} pair to the stage (sign for stage for removal)
        }
    }

    /* LOG COMMAND */

    /**
     * Execute the log command.
     * 1. Get the ID of the latest commit
     * 2. Print log information starting from that commit to the initial commit recursively
     */
    public static void log() {
        assertGITLET();
        log(getLatestCommitRef());
    }

    /**
     * Print log information starting from a given commit ID.
     * 1. Get the Commit object with the given CommitID
     * 2. Print its log information
     * 3. Recursively print its ascendants' log information
     * @param CommitID the given commit ID
     */
    private static void log(String CommitID) {
        if (CommitID == null || loggedCommitID.contains(CommitID)) {
            return;
        } // Special case: return if the CommitID is null or already printed (useful for global-log command)
        loggedCommitID.add(CommitID);
        Commit commit = getCommit(CommitID);
        System.out.println(commit.logString());
        log(commit.getParentCommitRef());
    }

    /* GLOBAL-LOG COMMAND */

    /** A Set that record the visited commits' IDs. No need to be persistent. */
    private static final Set<String> loggedCommitID = new HashSet<>();

    /**
     * Print log information about all commits ever made.
     * 1. Get a list of commit IDs that are pointed by any branch
     * 2. Print log information starting form each of the ID
     *    (ignore those commits that have been visited base on their IDs)
     * TODO: test this against branched repository
     */
    public static void globalLog() {
        List<String> branchesCommitID = loadAllBranches();
        for (String CommitID : branchesCommitID) {
            log(CommitID);
        }
    }

    /* FIND COMMAND */

    /** A list of commit IDs that have the designated commit message. */
    private static final List<String> foundCommitID = new ArrayList<>();
    /** A list of commit IDs that are already visited. */
    private static final List<String> visitedFindCommitID = new ArrayList<>();

    /**
     * Execute the find command.
     * 1. Get a list of commit IDs that are pointed by any branch
     * 2. Recursively check the commits and their ascendants whether they have the designated commit message
     *    (ignore those commits that have been visited base on their IDs)
     * @param commitMessage the designated commit message.
     */
    public static void find(String commitMessage) {
        List<String> branchesCommitID = loadAllBranches();
        for (String CommitID : branchesCommitID) {
            findCheck(CommitID, commitMessage);
        }
        if (foundCommitID.isEmpty()) {
            throw new GitletException("Found no commit with that message.");
        } // Special case: no such commit exists.
        System.out.println("Commit ID(s) that match(es) the given message \"" + commitMessage + "\":");
        for (String CommitID : foundCommitID) {
            System.out.println(CommitID);
        }
    }

    /**
     * Recursively check if commit with CommitID and its ascendants have the designated commit message.
     * @param CommitID the designated commit ID.
     * @param commitMessage the matching commit message.
     * TODO: test against branched repository
     */
    private static void findCheck(String CommitID, String commitMessage) {
        if (CommitID == null || visitedFindCommitID.contains(CommitID)) {
            return;
        } // Special case: return if CommitID is null or already visited.
        visitedFindCommitID.add(CommitID);
        Commit commit = getCommit(CommitID);
        if (commit.getMessage().equals(commitMessage)) {
            foundCommitID.add(CommitID);
        }
        findCheck(commit.getParentCommitRef(), commitMessage);
    }

    /* STATUS COMMAND */

    /** Execute the status command. */
    public static void status() {
        branchStatus();
        stageStatus();
        modificationStatus();
        untrackedStatus();
    }

    /** Print the "Modifications Not Staged For Commit" status. */
    private static void modificationStatus() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        // TODO (extra credit)
        System.out.print("\n");
    }

    /** Print the "Untracked Files" status. */
    private static void untrackedStatus() {
        System.out.println("=== Untracked Files ===");
        // TODO (extra credit)
        System.out.print("\n");
    }

    /* CHECKOUT COMMAND */

    /**
     * Execute checkout command usage 1 (checkout a file to the latest commit).
     * 1. Get the ID of the latest commit
     * 2. Invoke checkout2 method with the ID of the latest commit.
     * @param fileName the designated file name
     */
    public static void checkout1(String fileName) {
        String latestCommitRef = getLatestCommitRef();
        checkout2(latestCommitRef, fileName);
    }

    /**
     * Execute checkout command usage 2 (checkout a file to the given commit).
     * 1. Get the Commit object with the designated commit ID
     * 2. Get the designated file's Blob object form that commit
     * 3. Overwrite the file with that name in the CWD
     * @param commitID the designated commit ID
     * @param fileName the designated file name
     */
    public static void checkout2(String commitID, String fileName) {
        assertGITLET();
        Commit commit = getCommit(commitID);
        if (commit == null) {
            throw new GitletException("No commit with that id exists.");
        } // Special case: abort if there is no commit with the given commit ID
        Blob blob = getBlob(commit.getCommitTreeBlobID(fileName));
        if (blob == null) {
            throw new GitletException("File does not exist in that commit.");
        } // Special case: abort if such file does not exist in that commit
        overwriteCWDFile(fileName, blob);
    }

    /**
     * Execute checkout command usage 3 (checkout all files to the designated branch).
     * @param branchName the designated branch name
     */
    public static void checkout3(String branchName) {
        // TODO: checkout command usage 3
    }

    /* MISC */

    /** Assert the CWD contains a .gitlet directory. */
    private static void assertGITLET() {
        if (!GITLET_DIR.exists()) {
            throw new GitletException("A Gitlet version-control system have not initialized in the current directory.");
        }
    }

    /**
     * Overwrite the file in CWD of designated file name with the content in the given Blob object.
     * @param fileName the designated file name
     * @param overwriteSrc the given Blob object
     */
    private static void overwriteCWDFile(String fileName, Blob overwriteSrc) {
        String overwriteContent = overwriteSrc.getContent();
        File file = join(CWD, fileName);
        writeContents(file, overwriteContent);
    }
}
