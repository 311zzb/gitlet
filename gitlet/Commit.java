package gitlet;

import java.util.Date;

import static gitlet.Branch.*;
import static gitlet.Cache.*;
import static gitlet.Tree.*;

/**
 * This class represents a Commit in Gitlet, it extends the HashObject class.
 * Each instance of Commit have several instance variables such as commit message and time stamp.
 * This file also has helper methods that unlocks instance variable
 * as well as static method that carry out the procedure to make a new commit.
 *
 *  @author XIE Changyuan
 */
public class Commit extends HashObject {

    /** The commit message */
    private final String _message;
    /** The ID of the parent commit. */
    private final String _parentCommitRef;
    /** The ID of the other parent (if any). */
    private final String _parentCommitMergeRef; // TODO: merging of branches
    /** A time stamp of the commit been made. */
    private final Date _timeStamp;
    /** The ID of the associated Tree object. */
    private final String _treeRef;

    /**
     * The constructor of `Commit` class. This method is `private`
     * because no "naked" instantiation of `Commit` is allowed outside the `Commit` class.
     * Additionally, the time stamp is set to 1970.01.01 for initial commit.
     * @param parentCommitRef the hash of parent commit
     * @param message the message come with the commit
     * @param treeRef the corresponding tree's hash
     */
    private Commit(String parentCommitRef, String message, String treeRef) {
        this._parentCommitRef = parentCommitRef;
        this._parentCommitMergeRef = null; // TODO: merge command
        this._message = message;
        this._treeRef = treeRef;
        if (message.equals("initial commit")) {
            this._timeStamp = new Date(0); // Special case: the time is set to 1970 for initial commit
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
     * Get the ID of the associating Tree of this commit.
     */
    String getCommitTreeRef() {
        return _treeRef;
    }

    /** Get the associating Tree of this commit. */
    Tree getCommitTree() {
        String commitTreeRef = getCommitTreeRef();
        return getTree(commitTreeRef);
    }

    /** Get the ID of the Blob of a designated file name in this commit. */
    String getCommitTreeBlobID(String fileName) {
        Tree commitTree = this.getCommitTree();
        return commitTree.getBlobID(fileName);
    }

    /* STATIC METHODS */

    /**
     * Make a new Commit.
     * 1. Get the ID of the latest commit
     * 2. Make a commit tree by copying from the latest commit, and update it with the staging area
     * 3. Construct a new Commit object
     * 4. Cache the new Commit and queue it for write back
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
