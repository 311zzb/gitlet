package gitlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static gitlet.Branch.*;
import static gitlet.HashObject.*;
import static gitlet.Stage.*;

/**
 * This class is used to house static methods that facilitate lazy loading and caching of persistence.
 * This file will set up data structures for caching, load necessary objects,
 * and write back the cache at the very end of execution.
 * This class will never be instantiated.
 *
 * This class defers all HashObject and its subclasses' logic to them.
 * For example, instead of deserialize and serialize objects directly,
 * Cache class will invoke methods from the corresponding class to do that.
 *
 * On the other hand, the Cache class will do all the get xxx() methods which retrieving desired objects lazily
 * from the cache.
 *
 * @author XIE Changyuan
 */
public class Cache {

    /* CACHING OBJECT */

    /** A Map that stores cached ID and HashObject pairs. */
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
    /** Write back all queued-for-writing HashObjects to filesystem. Invoked upon exit. */
    static void writeBackAllQueuedHashObject() {
        for (String id : queuedForWriteHashObjects) {
            writeCachedHashObject(id);
        }
    }


    /** Deprecated HashObjects' IDs that are queued for deletion from filesystem. */
    static List<String> queuedForDeleteHashObject = new ArrayList<>();
    /** Given a HashObject's ID, queue it for deletion. */
    static void queueForDeleteHashObject(String id) {
        queuedForDeleteHashObject.add(id);
    }
    /** Delete all queued-for-deletion HashObjects. Invoked upon exit. */
    static void deleteAllQueuedHashObject() {
        for (String id : queuedForDeleteHashObject) {
            deleteHashObject(id);
        }
    }



    /* CACHING BRANCH */

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
    static String getLatestCommitRef() {
        return getBranch(getHEAD());
    }
    static void cacheBranch(String branchName, String commitID) {
        cachedBranches.put(branchName, commitID);
    }
    /** Write back (update) all branches to filesystem. Invoked upon exit. */
    static void writeBackAllBranches() {
        for (String branchName : cachedBranches.keySet()) {
            if (branchName.equals("")) {
                continue;
            } // Special case: don't write back empty branch
            writeBranch(branchName);
        }
    }


    /* CACHING HEAD */

    static String cachedHEAD = null;
    /** Lazy loading and caching of HEAD (the current branch's branch name).
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
    /** Write back HEAD file. Invoked upon exit. */
    static void writeBackHEAD() {
        writeHEAD();
    }


    /* CACHING STAGE */

    static String cachedStageID = null;
    /** Lazy loading and caching of STAGE (the ID of the current staging area). */
    static String getStageID() {
        if (cachedStageID == null) {
            cachedStageID = loadSTAGEID();
        }
        return cachedStageID;
    }
    static void cacheStageID(String stageID) {
        cachedStageID = stageID;
    }
    /** Write back STAGE file. Invoked upon exit. */
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