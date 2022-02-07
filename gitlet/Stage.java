package gitlet;

import static gitlet.Cache.*;
import static gitlet.Repository.STAGE;
import static gitlet.Tree.mkNewEmptyTree;
import static gitlet.Utils.*;

/**
 * A class where Stage related static methods live.
 * Will never be instantiated.
 *
 * @author XIE Changyuan
 */
public class Stage {

    /**
     * Return the ID of staging area (a Tree object).
     */
    static String loadSTAGEID() {
        return readContentsAsString(STAGE);
    }

    /**
     * Write the stage ID in cache to filesystem.
     */
    static void writeSTAGE() {
        writeContents(STAGE, getStageID());
    }

    /**
     * Make a new stage (Tree object) and cache its ID.
     */
    static void mkNewStage() {
        String newStageID = mkNewEmptyTree();
        cacheStage(newStageID);
    }
}
