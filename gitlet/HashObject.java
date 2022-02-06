package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Repository.CWD;
import static gitlet.Repository.OBJECTS_DIR;
import static gitlet.Utils.*;

/**
 *
 * @author XIE Changyuan
 */
public class HashObject implements Serializable, Dumpable {

    /**
     * The type of THIS.
     * Either Commit, or Tree, or Blob.
     */
    private final String _type;

    /**
     * Constructor.
     * @param type the type of THIS, designated by invoking
     */
    public HashObject(String type) {
        this._type = type;
    }

    /**
     * Get the SHA-1 of THIS
     * @return the SHA-1 of THIS
     */
    public String id() {
        return sha1(this.toString());
    }

    /**
     * Save THIS to one of the subdirectories of OBJECTS_DIR
     * (according to the type of THIS)
     * @return the file name (ID) of this
     */
    public String save() {
        String id = id();
        File dest = join(OBJECTS_DIR, _type, id);
        writeObject(dest, this);
        return id;
    }

    /**
     * Print the type of this object on System.out.
     */
    @Override
    public void dump() {
        System.out.println("Object type: " + _type);
    }
}
