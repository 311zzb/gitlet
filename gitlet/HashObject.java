package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;

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

    /** Allow you to switch between flat OBJECTS directory and HashTable OBJECTS directory. */
    private static final Boolean OPTIMIZATION = true; // true -> better performance; false -> easier debug

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

    /* STATIC METHODS ------------------------------------------------------------------------------------------------*/

    /**
     * Load a type object with its ID.
     * @param id the given id
     * @return the deserialized object
     */
    static HashObject loadHashObject(String id) {
        if (OPTIMIZATION) {
            File folder = optimizedObjectIDFolder(id);
            File file = optimizedObjectIDFile(id);
            if (id.length() < 40) {
                file = optimizedObjectAbbrevIDFile(id);
            } // Special case: if the given ID is abbreviated, use helper method to get the right file.
            if (!folder.exists() || file == null || !file.exists()) {
                return null;
            } // Special case: return null if told to load an object that does not exist.
            return readObject(file, HashObject.class);
        } else {
            File dest = join(OBJECTS_DIR, id);
            if (!dest.exists()) {
                return null;
            } // Special case: return null if told to load an object that does not exist.
            return readObject(dest, HashObject.class);
        }

    }

    /**
     * Write a cached HashObject with ID in cachedObjects to filesystem.
     * @param id the designated objects id
     */
    static void writeCachedHashObject(String id) {
        assert cachedHashObjects.containsKey(id);
        if (OPTIMIZATION) {
            File folder = optimizedObjectIDFolder(id);
            if (!folder.exists()) {
                folder.mkdir();
            } // Special case: make the containing directory if it is not already there
            File file = optimizedObjectIDFile(id);
            writeObject(file, cachedHashObjects.get(id));
        } else {
            File dest = join(OBJECTS_DIR, id);
            writeObject(dest, cachedHashObjects.get(id));
        }
    }

    /**
     * Delete a HashObject from filesystem.
     * @param id the designated ID
     */
    static void deleteHashObject(String id) {
        if (OPTIMIZATION) {
            File folder = optimizedObjectIDFolder(id);
            File file = optimizedObjectIDFile(id);
            file.delete();
        } else {
            File dest = join(OBJECTS_DIR, id);
            dest.delete();
        }
    }

    /**
     * Helper method that returns the housing directory of a HashObject with the given ID.
     * Used in the optimized object database.
     */
    static private File optimizedObjectIDFolder(String id) {
        return join(OBJECTS_DIR, id.substring(0, 2));
    }

    /**
     * Helper method that returns the file of a HashObject with the given ID.
     * Used in the optimized object database.
     */
    static private File optimizedObjectIDFile(String id) {
        return join(optimizedObjectIDFolder(id), id.substring(2));
    }

    /**
     * Helper method that return the file of a HashObject with the given abbreviated ID.
     * Used in the optimized object database.
     */
    static private File optimizedObjectAbbrevIDFile(String id) {
        File folder = optimizedObjectIDFolder(id);
        String preFix = id.substring(2);
        assert preFix.length() >= 4;
        List<String> fileList = plainFilenamesIn(folder);
        if (fileList == null) {
            return null;
        } // Special case: return null if the folder is empty
        for (String fileName : fileList) {
            if (fileName.startsWith(preFix)) {
                return join(folder, fileName);
            }
        }
        return null;
    }
}
