package gitlet;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import static gitlet.Cache.*;

/**
 * Represent a Gitlet Tree, corresponding to UNIX directory entries.
 * An instance of Tree object contains a TreeMap as instance variable, which has zero or more entries.
 * Each of these entries is a fileName-BlobID pair.
 * This class also contains Tree related static methods.
 *
 * @author XIE Changyuan
 */
public class Tree extends HashObject implements Iterable<String> {
    /* The TreeMap that stores fileName - blobID pairs. */
    private final Map<String, String> _structure;

    /** Constructor */
    private Tree() {
        _structure = new TreeMap<>();
    }

    /** A constructor that deep-copy the passed-in Tree. */
    Tree(Tree another) {
        _structure = new TreeMap<>();
        for (String key : another) {
            this.putBlobID(key, another.getBlobID(key));
        }
    }

    /**
     * Content-addressable toString() method.
     * @return the TreeMap instance variable
     */
    @Override
    public String toString() {
        return _structure.toString();
    }

    /**
     * Print the treeMap in this object on System.out.
     */
    @Override
    public void dump(){
        super.dump();
        System.out.println(_structure);
    }

    /**
     * Return true if a Tree is empty.
     */
    boolean isEmpty() {
        return _structure.isEmpty();
    }

    /**
     * Record a fileName - Blob ID pair
     * @param fileName the name of the recording file
     * @param blobRef the hash pointer to a Blob
     */
    void putBlobID(String fileName, String blobRef) {
        _structure.put(fileName, blobRef);
    }

    /** Remove an entry with fileName as the key from this Tree. */
    void removeBlobID(String fileName) {
//        queueForDeleteHashObject(getBlobID(fileName)); // This may unintended delete Blob in previous commits
        _structure.remove(fileName);
    }

    /**
     * Return the ID of a Blob according to a given fileName (if exists).
     */
    String getBlobID(String fileName) {
        return _structure.get(fileName);
    }

    Blob getBlob(String fileName) {
        return Cache.getBlob(getBlobID(fileName));
    }

    @Override
    public Iterator<String> iterator() {
        return _structure.keySet().iterator();
    }

    /**
     * Update THIS Tree with entries in UPDATER.
     * @param updater the Tree has newer entries
     */
    void updateWith(Tree updater) {
        for (String key : updater) {
            this.putBlobID(key, updater.getBlobID(key));
        }
    }


    /* STATIC METHODS */

    /**
     * A packaged constructor for Tree.
     * Creates an empty Tree, cache it and return its ID.
     * @return the new tree's ID
     */
    static String mkNewEmptyTree() {
        Tree newTree = new Tree();
        return cacheAndQueueForWriteHashObject(newTree);
    }

    /**
     * Return the Tree of the latest commit.
     */
    static Tree getLatestCommitTree() {
        Commit latestCommit = getLatestCommit();
        if (latestCommit == null) {
            return null;
        } // Special case: return null if there is no latest commit.
        String latestCommitTreeRef = latestCommit.getCommitTreeRef();
        return getTree(latestCommitTreeRef);
    }

    /**
     * Return a Tree that capture the Tree from the latest commit as well as current addition and removal status.
     * 1. Get a copy of the Tree of the latest commit
     * 2. Get the staging area Tree
     * 3. Update that copy with the staging area
     * 4. Cache it and queue it for writing
     * @return the ID of the new Tree
     */
    static String mkCommitTree() {
        Tree tree = copyLatestCommitTree();
        if (tree == null) {
            return mkNewEmptyTree();
        } // Special cases: make a new empty tree if there is no Tree in the latest commit
        tree.updateWith(getStage());
        return cacheAndQueueForWriteHashObject(tree);
    }

    /** Return a deep-copy of the Tree in the latest commit. */
    private static Tree copyLatestCommitTree() {
        Tree latestCommitTree = getLatestCommitTree();
        if (latestCommitTree == null) {
            return null;
        } else {
            return new Tree(latestCommitTree);
        }
    }
}
