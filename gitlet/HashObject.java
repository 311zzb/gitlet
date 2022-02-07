package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Cache.*;
import static gitlet.Repository.*;
import static gitlet.Utils.*;

/**
 *
 * @author XIE Changyuan
 */
public class HashObject implements Serializable, Dumpable {

    /**
     * Get the SHA-1 of THIS
     * @return the SHA-1 of THIS
     */
    String id() {
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
     * Load a type object with id.
     * @param id the given id
     * @return the deserialized object
     */
    static HashObject loadHashObject(String id) {
        File dest = join(OBJECTS_DIR, id);
        return readObject(dest, HashObject.class);
    }

    /**
     * Write a cached HashObject with id in cachedObjects.
     * @param id the designated objects id
     */
    static void writeCachedHashObject(String id) {
        assert cachedHashObjects.containsKey(id);
        File dest = join(OBJECTS_DIR, id);
        writeObject(dest, cachedHashObjects.get(id));
    }

    /**
     * Delete a HashObject on the filesystem.
     * @param id the designated ID
     */
    static void deleteHashObject(String id) {
        File dest = join(OBJECTS_DIR, id);
        dest.delete();
    }
}
