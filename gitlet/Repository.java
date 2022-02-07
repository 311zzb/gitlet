package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Branch.*;
import static gitlet.Commit.*;
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
        mkNewBranch("master", null);
        moveHEAD("master");
        mkCommit("initial commit");
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

    /* MISC */


}
