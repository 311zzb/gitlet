package gitlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static gitlet.Branch.*;
import static gitlet.HashObject.*;
import static gitlet.Stage.*;

/**
 * Cache related methods.
 * Will never be instantiated.
 *
 * @author XIE Changyuan
 */
public class Cache {

    /* CACHING OBJECTS */

    /** Cached objects. */
    static Map<String, HashObject> cachedHashObjects = new TreeMap<>();
    /** Lazy loading and caching of HashObjects. */
    private static HashObject getHashObject(String id) {
        if (id == null) {
            return null;
        } // Special case: Get null HashObject
        if (!cachedHashObjects.containsKey(id)) {
            cachedHashObjects.put(id, loadHashObject(id));
        }
        return cachedHashObjects.get(id);
    }
    static Commit getCommit(String id) {
        return (Commit) getHashObject(id);
    }
    static Tree getTree(String id) {
        return (Tree) getHashObject(id);
    }
    // TODO: getBlob

    /** Get the Commit object of the latest commit. */
    static Commit getLatestCommit() {
        return getCommit(getLatestCommitRef());
    }
    static String getLatestCommitRef() {
        return getBranch(getHEAD());
    }
    /** Get the Tree object representing the staging area. */
    static Tree getStage() {
        return getTree(getStageID());
    }



    /** New HashObjects' IDs that are queued for writing to filesystem. */
    static List<String> queuedForWriteHashObjects = new ArrayList<>();
    /** Put a HashObject into the cache, and queue for writing to filesystem.
     * @return the ID of the HashObject
     */
    static String cacheAndQueueForWriteHashObject(HashObject object) {
        String id = object.id();
        cachedHashObjects.put(id, object);
        queuedForWriteHashObjects.add(id);
        return id;
    }
    /** Write back all queued-for-writing HashObjects to filesystem. */
    static void writeBackAllQueuedHashObject() {
        for (String id : queuedForWriteHashObjects) {
            writeCachedHashObject(id);
        }
    }


    /** Deprecated HashObjects' IDs that are queued for deletion from filesystem. */
    static List<String> queuedForDeleteHashObject = new ArrayList<>();
    /** Queue a HashObject's ID for deletion. */
    static void queueForDeleteHashObject(String id) {
        queuedForDeleteHashObject.add(id);
    }
    /** Delete all queued-for-deletion HashObjects. */
    static void deleteAllQueuedHashObject() {
        for (String id : queuedForDeleteHashObject) {
            deleteHashObject(id);
        }
    }


    /* CACHING BRANCHES */

    /** Cached branches. */
    static Map<String, String> cachedBranches = new TreeMap<>();
    /** Lazy loading and caching of branches.
     * @return the Commit ID pointed by branch branchName */
    static String getBranch(String branchName) {
        if (!cachedBranches.containsKey(branchName)) {
            cachedBranches.put(branchName, loadBranch(branchName));
        }
        return cachedBranches.get(branchName);
    }
    static void cacheBranch(String branchName, String commitID) {
        cachedBranches.put(branchName, commitID);
    }
    /** Write back (update) all branches to filesystem. */
    static void writeBackAllBranches() {
        for (String branchName : cachedBranches.keySet()) {
            if (branchName.equals("")) {
                continue;
            } // Special case: don't write back empty branch.
            writeBranch(branchName);
        }
    }


    /* CACHING HEAD */

    static String cachedHEAD = null;
    /** lazy loading and caching of HEAD (the current branch's branch name).
     * @return the current branch's name */
    static String getHEAD() {
        if (cachedHEAD == null) {
            cachedHEAD = loadHEAD();
        }
        return cachedHEAD;
    }
    static void cacheHEAD(String branchName) {
        cachedHEAD = branchName;
    }
    /** Write back HEAD file. */
    static void writeBackHEAD() {
        writeHEAD();
    }


    /* CACHING STAGE */

    static String cachedStageID = null;
    /** Lazy loading and caching of STAGE (a pointer to the staging area). */
    static String getStageID() {
        if (cachedStageID == null) {
            cachedStageID = loadSTAGEID();
        }
        return cachedStageID;
    }
    static void cacheStage(String newStageID) {
        cachedStageID = newStageID;
    }
    /** Write back STAGE file. */
    static void writeBackSTAGE() {
        writeSTAGE();
    }


    /* MISC */

    /**
     * Write back all caches. Invoked upon exit.
     */
    static void writeBack() {
        deleteAllQueuedHashObject();
        writeBackAllQueuedHashObject();
        writeBackAllBranches();
        writeBackHEAD();
        writeBackSTAGE();
    }
}
