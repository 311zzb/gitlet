package gitlet;

import java.util.List;

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

    /* STATIC METHODS ------------------------------------------------------------------------------------------------*/


    /**
     * Return the ID of the current staging area (a Tree object). Invoked by the Cache class.
     */
    static String loadStageID() {
        return readContentsAsString(STAGE);
    }

    /**
     * Copy the staging area and add a fileName - BlobID pair.
     * Mark the previous staging area Tree for deletion.
     * This function should only be invoked once per run.
     * @param fileName the designated file name
     * @param BlobID the designated ID
     */
    static void putInStage(String fileName, String BlobID) {
        Tree stage = new Tree(getStage());
        stage.putBlobID(fileName, BlobID);
        cacheStage(stage);
    }

    /**
     * Copy the staging area and remove the entry with a specific fileName (if exists) from it.
     * Mark the previous staging area Tree for deletion.
     * This function should only be invoked once per run.
     * @param fileName the designated file name
     */
    static void removeFromStage(String fileName) {
        Tree stage = new Tree(getStage());
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
        Tree newStage = getTree(newStageID);
        cacheStage(newStage);
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
        String prevVerBlobID = latestCommit.getBlobID(fileName);
        if (currVerBlobID != null && currVerBlobID.equals(prevVerBlobID)) { // TODO: test this
            removeFromStage(fileName);
            return;
        }
        putInStage(fileName, currVerBlobID);
    }

    /** Print the status information related with the staging area. */
    static void stageStatus() {
        stagedFilesStatus();
        removedFilesStatus();
    }

    /** Return a sorted List of file names in the current staging area. */
    static List<String> stagedFiles() {
        Tree stage = getStage();
        return stage.trackedFiles();
    }

    /** Print the "Staged Files" status. */
    private static void stagedFilesStatus() {
        System.out.println("=== Staged Files ===");
        for (String fileName : stagedFiles()) {
            if (!getStage().getBlobID(fileName).equals("")) {
                System.out.println(fileName);
            }
        }
        System.out.print("\n");
    }

    /** Print the "Removed Files" status. */
    private static void removedFilesStatus() {
        System.out.println("=== Removed Files ===");
        for (String fileName : stagedFiles()) {
            if (getStage().getBlobID(fileName).equals("")) {
                System.out.println(fileName);
            }
        }
        System.out.print("\n");
    }

    /** Return true if a designated file is staged for addition. */
    static boolean isStagedForAdd(String fileName) {
        Tree stage = getStage();
        return stage.containsFile(fileName) && !stage.getBlobID(fileName).equals("");
    }

    /** Return true if a designated file is staged for removal. */
    static boolean isStagedForRemoval(String fileName) {
        Tree stage = getStage();
        return stage.containsFile(fileName) && stage.getBlobID(fileName).equals("");
    }
}
