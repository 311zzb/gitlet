package gitlet;

import static gitlet.Cache.*;
import static gitlet.Repository.STAGE;
import static gitlet.Tree.*;
import static gitlet.Utils.*;
import static gitlet.Blob.*;

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
    static String loadStageID() {
        return readContentsAsString(STAGE);
    }

    /**
     * Simply add a fileName - BlobID pair into the cached staging area.
     * Mark the previous staging area Tree for deletion.
     * This function should only be invoked once per run.
     * @param fileName the designated file name
     * @param BlobID the designated ID
     */
    static void putInStage(String fileName, String BlobID) {
        Tree stage = getStage();
        stage.putBlobID(fileName, BlobID);
        cacheStage(stage);
    }

    /**
     * Simply remove an entry from the staging area with a specific fileName (if exists).
     * Mark the previous staging area Tree for deletion.
     * This function should only be invoked once per run.
     * @param fileName the designated file name
     */
    static void removeFromStage(String fileName) {
        Tree stage = getStage();
        stage.removeBlobID(fileName);
        cacheStage(stage);
    }

    /**
     * Write the stage ID in cache back to filesystem. Invoked by the Cache class.
     */
    static void writeStageID(String newStagID) {
        writeContents(STAGE, newStagID);
    }

    /**
     * Make a new stage (a Tree object) and cache its ID.
     */
    static void mkNewStage() {
        String newStageID = mkNewEmptyTree();
        cacheStageID(newStageID);
    }

    /**
     * Add a file to the current staging area.
     * 1. Get the file as its current version, cache it as a Blob (don't queue for write back yet)
     * 2. Get the version of the designated file from the latest commit
     * 3. Special case:
     *    If the current version of the file is identical to the version in the latest commit (by comparing IDs),
     *    do not stage it, and remove it from the staging area if it is already there. End the execution.
     * 4. Modify cached staging area
     *
     * @param fileName the designated file name
     */
    static void addToStage(String fileName) {
        String currVerBlobID = mkBlob(fileName);
        Commit latestCommit = getLatestCommit();
        String prevVerBlobID = latestCommit.getCommitTreeBlobID(fileName);
        if (currVerBlobID.equals(prevVerBlobID)) {
            removeFromStage(fileName);
            return;
        }
        putInStage(fileName, currVerBlobID);
    }
}
