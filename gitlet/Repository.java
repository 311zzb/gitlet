package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Branch.*;
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
     * 1. Set up persistence directories
     * 2. Make the default branch "master" which is pointing null for now (no pun intended)
     * 3. Make the HEAD pointing to the master branch
     * 4. Make a new staging area
     * 5. Create an initial commit (branch master will be moved in this method)
     */
    public static void init() throws IOException {
        setUpPersistence();
        mkNewBranch("master");
        moveHEAD("master");
        mkNewStage();
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


    /* ADD COMMAND */

    /**
     * Execute the add command.
     * 1. Add a copy of the file as it currently exists to the staging area
     *
     * @param fileName the designated file name
     */
    public static void add(String fileName) {
        File targetFile = join(CWD, fileName);
        if (!targetFile.exists()) {
            throw new GitletException("File does not exist.");
        } // Special case: abort if such file does not exist
        addToStage(fileName);
    }

    /* MISC */


}
