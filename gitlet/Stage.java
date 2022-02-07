package gitlet;

import static gitlet.Cache.*;
import static gitlet.Repository.STAGE;
import static gitlet.Tree.mkNewEmptyTree;
import static gitlet.Utils.*;

/**
 * This class houses static methods that related to Stage (the staging area).
 * It contains methods for loading and writing the STAGE file, as well as making a new staging area.
 * This class will never be instantiated since there are only static methods.
 *
 * @author XIE Changyuan
 */
public class Stage {

    /**
     * Return the ID of the current staging area (a Tree object). Invoked by the Cache class.
     */
    static String loadSTAGEID() {
        return readContentsAsString(STAGE);
    }

    /**
     * Write the stage ID in cache back to filesystem. Invoked by the Cache class.
     */
    static void writeSTAGE() {
        writeContents(STAGE, getStageID());
    }

    /**
     * Make a new stage (a Tree object) and cache its ID.
     */
    static void mkNewStage() {
        String newStageID = mkNewEmptyTree();
        cacheStageID(newStageID);
    }
}
