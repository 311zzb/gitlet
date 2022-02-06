package gitlet;

import java.io.File;
import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
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

    /** INIT COMMAND */

    /**
     * 1. Set up persistence directories.
     * 2. Create an initial commit.
     * 3. Create a master branch and make it point to the initial commit.
     * 4. Make the master branch the current branch.
     * 5. Don't forget the READ-MODIFY-WRITE paradigm!
     */
    public static void init() {

    }

    private static void setUpPersistence() {
        HEAD.mkdirs();
        STAGE.mkdirs();
        OBJECTS_DIR.mkdirs();
        BRANCHES_DIR.mkdirs();
    }
}
