package gitlet;

import java.util.Date;

import static gitlet.Branch.*;
import static gitlet.Cache.*;
import static gitlet.Tree.*;

/** Represents a gitlet commit object.
 *
 *
 *  @author XIE Changyuan
 */
public class Commit extends HashObject {

    /** The message of this Commit. */
    private final String _message;
    /** A hash reference to the parent commit. */
    private final String _parentCommitRef;
    /** A hash reference to another parent (if any). */
    private final String _parentCommitMergeRef;
    /** A time stamp of the commit been made. */
    private final Date _timeStamp;
    /** A hash reference to a Tree object. */
    private final String _treeRef;

    /**
     * Constructor of Commit.
     * @param parentCommitRef the hash of parent commit
     * @param message the message come with the commit
     * @param treeRef the corresponding tree's hash
     */
    private Commit(String parentCommitRef, String message, String treeRef) {
        this._parentCommitRef = parentCommitRef;
        this._parentCommitMergeRef = null; // TODO: merge command
        this._message = message;
        this._treeRef = treeRef;
        if (message.equals("initial commit")) { // initial commit special case
            this._timeStamp = new Date(0);
        } else {
            this._timeStamp = new Date(System.currentTimeMillis());
        }
    }

    /**
     * Print information of this commit on System.out.
     */
    @Override
    public void dump(){
        super.dump();
        System.out.println("time: " + _timeStamp);
        System.out.println("message: " + _message);
        System.out.println("parent: " + _parentCommitRef);
        System.out.println("treeRef: " + _treeRef);
    }

    /**
     * Return the ID of the Tree of THIS.
     */
    public String getCommitTreeRef() {
        return _treeRef;
    }

    /* STATIC METHODS */

    /**
     * Make a new Commit.
     * 1. Get the ID of the latest commit
     * 2. Make a commit tree by copy from the latest commit, and update it with staging area
     * 3. Construct a new Commit object
     * 4. Cache the new Commit and queue it for write (back)
     * 5. Move the current branch pointing the new commit
     */
    static void mkCommit(String message) {
        String parentCommitRef = getLatestCommitRef();
        String treeRef = mkCommitTree();
        Commit newCommit = new Commit(parentCommitRef, message, treeRef);
        String newCommitID = cacheAndQueueForWriteHashObject(newCommit);
        moveCurrBranch(newCommitID);
    }


}
