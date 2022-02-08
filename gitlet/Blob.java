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

    private Blob(String content) {
        _content = content;
    }

    String getContent() {
        return this._content;
    }

    @Override
    public void dump() {
        super.dump();
        System.out.println(_content);
    }


    /* STATIC METHODS */

    /**
     * Make a new Blob with a designated file.
     * Cache it and queue it for writing to filesystem.
     * @param fileName the designated file name
     * @return the ID of the new Blob
     */
    static String mkBlob(String fileName) {
        File file = join(CWD, fileName);
        String content = readContentsAsString(file);
        Blob blob = new Blob(content);
        return cacheAndQueueForWriteHashObject(blob);
    }
}
