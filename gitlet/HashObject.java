package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Repository.CWD;
import static gitlet.Utils.*;

public class HashObject implements Serializable, Dumpable {

    private final String _type;

    public HashObject(String type) {
        this._type = type;
    }

    public String id() {
        return sha1(this.toString());
    }

    public String save() {
        String id = id();
        File dest = join(CWD, id); // FIXME
        writeObject(dest, this);
        return id;
    }

    @Override
    public void dump() {
        System.out.println("Object type: " + _type);
    }
}
