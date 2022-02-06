package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Branch.*;
import static gitlet.Stage.*;
import static gitlet.Commit.*;
import static gitlet.Tree.*;
import static gitlet.Utils.*;


/**
 * A class where Repository related static methods live.
 * Handling all commands to gitlet passed by the Main method.
 * Will never be instantiated.
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
    static final File OBJECTS_Blob_DIR = join(OBJECTS_DIR, "Blob");
    static final File OBJECTS_Tree_DIR = join(OBJECTS_DIR, "Tree");
    static final File OBJECTS_Commit_DIR = join(OBJECTS_DIR, "Commit");

    /* INIT COMMAND */

    /**
     * 1. Set up persistence directories
     * 2. Create an initial commit
     * 3. Create a master branch and make it point to the initial commit
     * 4. Make the HEAD points to master branch
     * Don't forget the READ-MODIFY-WRITE paradigm!
     */
    public static void init() throws IOException {
        setUpPersistence();
        String initialCommitID = mkInitialCommit();
        mkMasterBranch(initialCommitID);
        moveHeadTo("master");
    }

    /** Set up the persistence directories. */
    private static void setUpPersistence() throws IOException {
        BRANCHES_DIR.mkdirs();
        OBJECTS_Blob_DIR.mkdirs();
        OBJECTS_Tree_DIR.mkdirs();
        OBJECTS_Commit_DIR.mkdirs();
        HEAD.createNewFile();
        STAGE.createNewFile();
    }

    /* MISC */

    /** Move HEAD points to branchName. */
    private static void moveHeadTo(String branchName) {
        writeContents(HEAD, branchName);
    }


}
