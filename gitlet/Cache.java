package gitlet;

import java.util.*;

import static gitlet.Branch.*;
import static gitlet.HashObject.*;
import static gitlet.Repository.CWD;
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

    /* CACHING OBJECT ------------------------------------------------------------------------------------------------*/

    /** A Map that stores cached ID and HashObject pairs. */
    static Map<String, HashObject> cachedHashObjects = new TreeMap<>();
    static Map<String, HashObject> cachedRemoteHashObjects = new TreeMap<>();
    /** Lazy loading and caching of HashObjects. */
    private static HashObject getHashObject(String id) {
        Map<String, HashObject> currCachedHashObjects = inRemoteRepo()? cachedRemoteHashObjects : cachedHashObjects;
        if (id == null || id.equals("")) {
            return null;
        } // Special case: Get null or "" HashObject
        if (!currCachedHashObjects.containsKey(id)) {
            currCachedHashObjects.put(id, loadHashObject(id));
        }
        return currCachedHashObjects.get(id);
    }
    static Commit getCommit(String id) {
        return (Commit) getHashObject(id);
    }
    static Tree getTree(String id) {
        return (Tree) getHashObject(id);
    }
    static Blob getBlob(String id) {
        return (Blob) getHashObject(id);
    }

    /** Get the Commit object of the latest commit. */
    static Commit getLatestCommit() {
        return getCommit(getLatestCommitID());
    }


    /** New HashObjects' IDs that are queued for writing to filesystem. */
    static final Set<String> queuedForWriteHashObjects = new TreeSet<>();
    /** Put a HashObject into the cache, and queue for writing to filesystem.
     * @return the ID of the HashObject
     */
    static String cacheAndQueueForWriteHashObject(HashObject object) {
        Map<String, HashObject> currCachedHashObjects = inRemoteRepo()? cachedRemoteHashObjects : cachedHashObjects;
        String id = object.id();
        currCachedHashObjects.put(id, object);
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
    static final Set<String> queuedForDeleteHashObject = new TreeSet<>();
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

    /* CACHING BRANCH ------------------------------------------------------------------------------------------------*/

    /** Cached branches. */
    static Map<String, String> cachedBranches = new TreeMap<>();
    static Map<String, String> cachedRemoteBranches = new TreeMap<>();
    /** Lazy loading and caching of branches.
     * @return the Commit ID pointed by branch branchName */
    static String getBranch(String branchName) {
        Map<String, String> currCachedBranches = inRemoteRepo()? cachedRemoteBranches : cachedBranches;
        if (!currCachedBranches.containsKey(branchName)) {
            currCachedBranches.put(branchName, loadBranch(branchName));
        }
        return currCachedBranches.get(branchName);
    }
    static String getLatestCommitID() {
        return getBranch(getHEAD());
    }
    static void cacheBranch(String branchName, String commitID) {
        Map<String, String> currCachedBranches = inRemoteRepo()? cachedRemoteBranches : cachedBranches;
        currCachedBranches.put(branchName, commitID);
    }
    static void wipeBranch(String branchName) {
        Map<String, String> currCachedBranches = inRemoteRepo()? cachedRemoteBranches : cachedBranches;
        currCachedBranches.put(branchName, "");
    }
    /**
     * Write back (update) all branches to filesystem. Invoked upon exit.
     * If a branch's pointer is wiped out, delete the branch file in the filesystem.
     */
    static void writeBackAllBranches() {
        Map<String, String> currCachedBranches = inRemoteRepo()? cachedRemoteBranches : cachedBranches;
        for (String branchName : currCachedBranches.keySet()) {
            if (branchName.equals("")) {
                continue;
            } // Special case: ignore branch with empty name.
            if (currCachedBranches.get(branchName).equals("")) { // wiped branches
                deleteBranch(branchName);
            } else {
                writeBranch(branchName);
            }
        }
    }

    /* CACHING HEAD --------------------------------------------------------------------------------------------------*/

    static String cachedHEAD = null;
    static String cachedRemoteHEAD = null;
    /** Lazy loading and caching of HEAD (the current branch's branch name).
     * @return the current branch's name */
    static String getHEAD() {
        if (inRemoteRepo()) {
            if (cachedRemoteHEAD == null) {
                cachedRemoteHEAD = loadHEAD();
            }
            return cachedRemoteHEAD;
        } else {
            if (cachedHEAD == null) {
                cachedHEAD = loadHEAD();
            }
            return cachedHEAD;
        }
    }
    static void cacheHEAD(String branchName) {
        if (inRemoteRepo()) {
            cachedRemoteHEAD = branchName;
        } else {
            cachedHEAD = branchName;
        }
    }
    /** Write back HEAD file. Invoked upon exit. */
    static void writeBackHEAD() {
        writeHEAD();
    }

    /* CACHING STAGE ID ----------------------------------------------------------------------------------------------*/

    static String cachedStageID = null;
    static String cachedRemoteStageID = null;
    /**
     * Lazy loading and caching of STAGE (the ID of the saved staging area).
     * Notice: this DOES NOT point to the current staging area after the staging area is modified and before write back.
     */
    static String getStageID() {
        if (inRemoteRepo()) {
            if (cachedRemoteStageID == null) {
                cachedRemoteStageID = loadStageID();
            }
            return cachedRemoteStageID;
        } else {
            if (cachedStageID == null) {
                cachedStageID = loadStageID();
            }
            return cachedStageID;
        }
    }
    static void cacheStageID(String stageID) {
        if (inRemoteRepo()) {
            cachedRemoteStageID = stageID;
        } else {
            cachedStageID = stageID;
        }
    }
    /** Write back STAGE file. Invoked upon exit. */
    static void writeBackStageID() {
        writeStageID(getStageID());
    }

    /* CACHING STAGE -------------------------------------------------------------------------------------------------*/

    /** Cached staging area. */
    static Tree cachedStage = null;
    static Tree cachedRemoteStage = null;
    /** Get the Tree object representing the staging area. */
    static Tree getStage() {
        if (inRemoteRepo()) {
            if (cachedRemoteStage == null) {
                cachedRemoteStage = getTree(getStageID());
            }
            return cachedRemoteStage;
        } else {
            if (cachedStage == null) {
                cachedStage = getTree(getStageID());
            }
            return cachedStage;
        }
    }
    /**
     * Queue the previous staging area for deletion and manually cache the passed-in Stage.
     * @param stage the new stage to be cached
     */
    static void cacheStage(Tree stage) {
//        String prevStageID = getStageID();
//        if (
//                getLatestCommit() != null &&
//                !prevStageID.equals(getLatestCommit().getCommitTreeID()) &&
//                !getStage().isEmpty()) {
//            queueForDeleteHashObject(getStageID());
//        } /*Special case:
//            queue the previous staging area for deletion only if
//            there is a commit,
//            and the previous staging area is different from the Tree of the latest commit,
//            and the previous staging area is not empty.
//           */
        if (inRemoteRepo()) {
            cachedRemoteStage = stage;
        } else {
            cachedStage = stage;
        }
        String newStageID = cacheAndQueueForWriteHashObject(stage);
        cacheStageID(newStageID);
    }
    /** Write the new staging area. Invoked upon exit. */
    static void writeBackStage() {
        // Stage is written to file system as a HashObject.
        // There for no need for a standalone write-back method.
    }


    /* MISC ----------------------------------------------------------------------------------------------------------*/

    /**
     * Write back all caches. Invoked upon exit.
     */
    static void writeBack() {
        deleteAllQueuedHashObject();
        writeBackAllQueuedHashObject();
        writeBackAllBranches();
        writeBackHEAD();
        writeBackStageID();
    }

    /** Reset all caches. Used for testing proposes. */
    static void cleanCache() {
        cachedHashObjects.clear();
        queuedForWriteHashObjects.clear();
        queuedForDeleteHashObject.clear();
        cachedBranches.clear();
        cachedHEAD = null;
        cachedStageID = null;
        cachedStage = null;
    }

    /** Return true if currently operating on the remote repository. */
    static boolean inRemoteRepo() {
        return !CWD.equals(Main.localCWD);
    }
}
