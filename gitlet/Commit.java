package gitlet;

import java.util.Date;

/** Represents a gitlet commit object.
 *
 *
 *  @author XIE Changyuan
 */
public class Commit extends HashObject {

    /** The message of this Commit. */
    private String _message;
    /** A hash reference to the parent commit. */
    private String _parentCommitRef;
    /** A hash reference to another parent (if any). */
    private String _parentCommitMergeRef;
    /** A time stamp of the commit been made. */
    private Date _timeStamp;
    /** A hash reference to a Tree object. */
    private String _treeRef;

    /**
     * Constructor of Commit.
     * @param parentCommitRef the hash of parent commit
     * @param message the message come with the commit
     * @param treeRef the corresponding tree's hash
     */
    private Commit(String parentCommitRef, String message, String treeRef) {
        super("Commit");
        this._parentCommitRef = parentCommitRef;
        this._parentCommitMergeRef = null; // TODO: merge command
        this._message = message;
        this._treeRef = treeRef;
        if (message.equals("initial commit")) {
            this._timeStamp = new Date(0); // initial commit special case
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
        System.out.println("message: " + _message);
        System.out.println("parent: " + _parentCommitRef);
        System.out.println("treeRef: " + _treeRef);
    }

    /* STATIC METHODS */

    /**
     * Create a new Commit object and write it
     * @return the new commit's ID
     */
    public static String newWrite_Commit(String parentCommitRef, String message, String treeRef) {
        Commit newCommit = new Commit(parentCommitRef, message, treeRef);
        return newCommit.save();
    }

    public static String readModifyWrite_Commit() {
        return null;
    }

    public static Commit read_Commit(String commitID) {
        return null;
    }

    /**
     * Make an initial commit
     * Refresh the staging area
     * @return the ID of the initial commit
     */
    static String mkInitialCommit() {
        String emptyTreeRef = Tree.newWrite_Tree();
        String initialCommitID = Commit.newWrite_Commit(null, "initial commit", emptyTreeRef);
        Stage.refresh_Stage();
        return initialCommitID;
    }

    /**
     * Load a commit with id
     * @param id the given ID
     * @return the deserialized commit object
     */
    static Commit loadCommit(String id) {
        return (Commit) HashObject.load("Commit", id);
    }
}
