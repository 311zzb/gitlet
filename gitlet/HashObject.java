package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Repository.CWD;
import static gitlet.Utils.*;

public class HashObject implements Serializable, Dumpable {

    String _msg;

    public HashObject(String msg) {
        this._msg = msg;
    }

    public String id() {
        return sha1(this.toString());
    }

    public String save() {
        String id = id();
        File dest = join(CWD, id);
        writeObject(dest, this);
        return id;
    }

    @Override
    public void dump() {
        System.out.println(_msg);
    }
}
