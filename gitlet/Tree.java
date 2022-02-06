package gitlet;

import java.util.Map;
import java.util.TreeMap;

/**
 * Represent a Gitlet Tree, corresponding to UNIX directory entries.
 * A single tree object contains a TreeMap, containing zero or more entries.
 * Each of these entries is a fileName-BlobID pair.
 * This class also contains Tree related static methods.
 *
 * @author XIE Changyuan
 */
public class Tree extends HashObject{
    /* A map that store the FILENAME - Blob ID pairs. */
    private final Map<String, String> _structure;

    /** Constructor */
    public Tree() {
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
    public boolean isEmpty() {
        return _structure.isEmpty();
    }

    /**
     * Record a FILENAME - Blob ID pair
     * @param fileName the name of the recording file
     * @param blobRef the hash pointer to a Blob
     */
    public void record(String fileName, String blobRef) {
        _structure.put(fileName, blobRef);
    }

    /**
     * Return the Blob ref according to a given fileName (if exists).
     */
    public String retrieve(String fileName) {
        return _structure.get(fileName);
    }

    /* STATIC METHODS */

    /**
     * Create a new empty Tree object and save it
     * @return the new tree's ID
     */
    public static String newWrite_Tree() {
        Tree newTree = new Tree();
        return newTree.save();
    }
}
