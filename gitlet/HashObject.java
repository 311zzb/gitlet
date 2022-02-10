package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Cache.*;
import static gitlet.Repository.*;
import static gitlet.Utils.*;

/**
 * This class represents a HashObject that will be serialized within .gitlet/objects, named after its SHA-1.
 * The HashObject class has helper methods that will return the SHA-1 of a HashObject,
 * or returning the HashObject object corresponding to its ID (SHA-1),
 * as well as static methods that write to or delete from the object database a HashObject.
 *
 * @author XIE Changyuan
 */
public class HashObject implements Serializable, Dumpable {

    /**
     * Get the SHA-1 of THIS.
     * @return the SHA-1 of THIS
     */
    String id() {
        // Caution: this.toString() should be content-addressable!
        return sha1(this.toString());
    }

    /**
     * Print the type of this object on System.out.
     */
    @Override
    public void dump() {
        System.out.println("This is a HashObject.");
    }

    /* STATIC METHODS */

    /**
     * Load a type object with its ID.
     * @param id the given id
     * @return the deserialized object
     */
    static HashObject loadHashObject(String id) {
        File folder = join(OBJECTS_DIR, id.substring(0, 2));
        File file = join(folder, id.substring(2));
        if (!folder.exists() || !file.exists()) {
            throw new GitletException("Failed to load HashObject " + id);
        } // Special case: throw an Exception if told to load an object that does not exist
        return readObject(file, HashObject.class);
    }

    /**
     * Write a cached HashObject with ID in cachedObjects to filesystem.
     * @param id the designated objects id
     */
    static void writeCachedHashObject(String id) {
        assert cachedHashObjects.containsKey(id);
        File folder = join(OBJECTS_DIR, id.substring(0, 2));
        if (!folder.exists()) {
            folder.mkdir();
        } // Special case: make the containing directory if it is not already there
        File file = join(folder, id.substring(2));
        writeObject(file, cachedHashObjects.get(id));
    }

    /**
     * Delete a HashObject from filesystem.
     * @param id the designated ID
     */
    static void deleteHashObject(String id) {
        File folder = join(OBJECTS_DIR, id.substring(0, 2));
        File file = join(folder, id.substring(2));
        file.delete();
    }
}
