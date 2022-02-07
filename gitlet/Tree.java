package gitlet;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import static gitlet.Cache.*;

/**
 * Represent a Gitlet Tree, corresponding to UNIX directory entries.
 * A single tree object contains a TreeMap, containing zero or more entries.
 * Each of these entries is a fileName-BlobID pair.
 * This class also contains Tree related static methods.
 *
 * @author XIE Changyuan
 */
public class Tree extends HashObject implements Iterable<String> {
    /* A map that store the FILENAME - Blob ID pairs. */
    private final Map<String, String> _structure;

    /** Constructor */
    Tree() {
        _structure = new TreeMap<>();
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
     * Record a FILENAME - Blob ID pair
     * @param fileName the name of the recording file
     * @param blobRef the hash pointer to a Blob
     */
    void record(String fileName, String blobRef) {
        _structure.put(fileName, blobRef);
    }

    /**
     * Return the Blob ref according to a given fileName (if exists).
     */
    String retrieve(String fileName) {
        return _structure.get(fileName);
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
            this.record(key, updater.retrieve(key));
        }
    }


    /* STATIC METHODS */

    /**
     * Make a new empty Tree object and cache it.
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
     * Copy the Tree from the latest commit and update it with the staging area.
     * Cache it and queue it for writing.
     * @return the new Tree
     */
    static String mkCommitTree() {
        Tree tree = getLatestCommitTree();
        if (tree == null) {
            return mkNewEmptyTree();
        } // Special cases: make a new empty tree if there is no Tree for latest commit.
        tree.updateWith(getStage());
        return cacheAndQueueForWriteHashObject(tree);
    }
}
