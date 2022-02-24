package gitlet;

import java.io.File;

import static gitlet.Cache.*;
import static gitlet.Repository.CWD;
import static gitlet.Utils.*;

/**
 *
 */
public class Blob extends HashObject {

    private final String _content;

    Blob(String content) {
        _content = content;
    }

    String getContent() {
        return this._content;
    }

    /**
     * Content-addressable toString() method.
     * @return the content of this Blob
     */
    @Override
    public String toString() {
        return this.getContent();
    }

    @Override
    public void dump() {
        super.dump();
        System.out.println(_content);
    }


    /* STATIC METHODS */

    /**
     * Factory method. Make a new Blob with a designated file.
     * Cache it and queue it for writing to filesystem.
     * @param fileName the designated file name
     * @return the ID of the new Blob
     */
    static String mkBlob(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            return "";
        } // Special case: adding a file that not exists means adding for removal
        String content = readContentsAsString(file);
        Blob blob = new Blob(content);
        return cacheAndQueueForWriteHashObject(blob);
    }

    /** Return the ID of a designated file's Blob without cache or saving a Blob. */
    static String currFileID(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            return "";
        }
        String content = readContentsAsString(file);
        Blob blob = new Blob(content);
        return blob.id();
    }
}
