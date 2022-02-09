package gitlet;

import java.io.File;
import java.io.IOException;

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
    public static void init() throws IOException {
        setUpRepository();
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
        if (CommitID == null) {
            return;
        }
        Commit commit = getCommit(CommitID);
        System.out.println(commit.logString());
        log(commit.getParentCommitRef());
    }

    /* MISC */

    /** Assert the CWD contains a .gitlet directory. */
    private static void assertGITLET() {
        if (!GITLET_DIR.exists()) {
            throw new GitletException("A Gitlet version-control system have not initialized in the current directory.");
        }
    }

}
