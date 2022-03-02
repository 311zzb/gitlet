package gitlet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static gitlet.Branch.moveCurrBranch;
import static gitlet.Cache.*;
import static gitlet.Repository.ALL_COMMITS_ID;
import static gitlet.Repository.printAndExit;
import static gitlet.Stage.mkNewStage;
import static gitlet.Tree.mkCommitTree;

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
    private final String _parentCommitID;
    /** The ID of the second parent (if any). */
    private final String _parentMergeCommitID;
    /** A time stamp of the commit been made. */
    private final Date _timeStamp;
    /** The ID of the associated Tree object. */
    private final String _treeID;

    /**
     * The constructor of the Commit class. This method is `private`
     * because no "naked" instantiation of `Commit` is allowed outside the `Commit` class.
     * Additionally, the time stamp is set to 1970.01.01 for initial commit.
     * @param parentCommitID the hash of parent commit
     * @param message the message come with the commit
     * @param treeRef the corresponding tree's hash
     */
    private Commit(String parentCommitID, String message, String treeRef) {
        this._parentCommitID = parentCommitID;
        this._parentMergeCommitID = null;
        this._message = message;
        this._treeID = treeRef;
        if (message.equals("initial commit")) {
            this._timeStamp = new Date(0);
            return;
        } // Special case: the time is set to 1970 for initial commit
        this._timeStamp = new Date(System.currentTimeMillis());
    }

    /** Constructor for merge commits. */
    private Commit(String firstCommitID, String secondCommitID, String message, String treeRef) {
        this._parentCommitID = firstCommitID;
        this._parentMergeCommitID = secondCommitID;
        this._message = message;
        this._treeID = treeRef;
        this._timeStamp = new Date(System.currentTimeMillis());
    }

    /**
     * Content-addressable toString() method.
     * @return {parentCommitRef}@{message}@{treeRef}@{timeStamp}
     */
    @Override
    public String toString() {
        return _parentCommitID
                + "@"
                + _message
                + "@"
                + _treeID
                + "@"
                + _timeStamp.toString();
    }

    /** Return the log information of this Commit. */
    String logString() {
        String pattern = "EEE MMM dd HH:mm:ss yyyy Z";
        DateFormat df = new SimpleDateFormat(pattern);
        String timeStampString = df.format(_timeStamp);
        if (_parentMergeCommitID == null) {
            return  "===\n"
                    + "commit "
                    + this.id()
                    + "\n"
                    + "Date: "
                    + timeStampString
                    + "\n"
                    + _message
                    + "\n";
        } else {
            return  "===\n"
                    + "commit "
                    + this.id()
                    + "\n"
                    + "Merge: "
                    + _parentCommitID.substring(0, 7)
                    + " "
                    + _parentMergeCommitID.substring(0, 7)
                    + "\n"
                    + "Date: "
                    + timeStampString
                    + "\n" + _message
                    + "\n";
        }
    }

    /**
     * Print information of this commit on System.out.
     */
    @Override
    public void dump() {
        super.dump();
        System.out.println("time: " + _timeStamp);
        System.out.println("message: " + _message);
        System.out.println("parent: " + _parentCommitID);
        System.out.println("treeRef: " + _treeID);
    }

    /** Get the message of this commit. */
    String getMessage() {
        return _message;
    }

    /** Get the ID of the parent commit. */
    String getParentCommitID() {
        return _parentCommitID;
    }

    /** Get the ID of the second parent commit. */
    String getParentMergeCommitID() {
        return _parentMergeCommitID;
    }

    /** Get the Commit object of the parent commit. */
    Commit getParentCommit() {
        return getCommit(getParentCommitID());
    }

    /** Get the Commit object ot the second parent commit. */
    Commit getParentMergeCommit() {
        return getCommit(getParentMergeCommitID());
    }

    /**
     * Get the ID of the associating Tree of this commit.
     */
    String getCommitTreeID() {
        return _treeID;
    }

    /** Get the associating Tree of this commit. */
    Tree getCommitTree() {
        String commitTreeRef = getCommitTreeID();
        return getTree(commitTreeRef);
    }

    /** Get the ID of the Blob of a designated file name in this commit. */
    String getBlobID(String fileName) {
        Tree commitTree = this.getCommitTree();
        return commitTree.getBlobID(fileName);
    }

    /** Return the content of a designated file name in this commit. */
    String getFileContent(String fileName) {
        Blob blob = getBlob(getBlobID(fileName));
        if (blob == null) {
            return "";
        } // Special case: return an empty String if there is no corresponding Blob.
        return blob.getContent();
    }

    /** Return whether this Commit tracks a file with fileName. */
    Boolean trackedFile(String fileName) {
        Tree commitTree = this.getCommitTree();
        return commitTree.containsFile(fileName);
    }

    /** Return a string Set of tracked files of this commit. */
    Set<String> trackedFiles() {
        Tree commitTree = this.getCommitTree();
        return new HashSet<>(commitTree.trackedFiles());
    }

    /* STATIC METHODS */

    /**
     * Factory method. Make a new Commit.
     * 1. Get the ID of the latest commit
     * 2. Make a commit tree by copying from the latest commit, and update it with the staging area
     * 3. Construct a new Commit object
     * 4. Cache the new Commit and queue it for write back
     * 5. Move the current branch pointing the new commit
     * 6. Make a new staging area
     * 7. Record the new commit's ID
     */
    static void mkCommit(String message) {
        if (!message.equals("initial commit") && getStage().isEmpty()) {
            printAndExit("No changes added to the commit.");
        } // Special case: abort if no change is made.
        String parentCommitID = getLatestCommitID();
        String treeRef = mkCommitTree();
        Commit newCommit = new Commit(parentCommitID, message, treeRef);
        String newCommitID = cacheAndQueueForWriteHashObject(newCommit);
        moveCurrBranch(newCommitID);
        mkNewStage();
        recordCommitID(newCommitID);
    }

    /** Factory method. Make a new merge Commit. */
    static void mkMergeCommit(String givenBranchName, Boolean conflicted) {
        if (getStage().isEmpty()) {
            printAndExit("No changes added to the commit.");
        } // Special case: abort if no change is made.
        String firstParentID = getLatestCommitID();
        String secondParentID = getBranch(givenBranchName);
        String currBranchName = getHEAD();
        String treeRef = mkCommitTree();
        String message = "Merged " + givenBranchName + " into " + currBranchName + ".";
        Commit newCommit = new Commit(firstParentID, secondParentID, message, treeRef);
        String newCommitID = cacheAndQueueForWriteHashObject(newCommit);
        moveCurrBranch(newCommitID);
        mkNewStage();
        recordCommitID(newCommitID);
        if (conflicted) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * Return the latest common ancestor (LCA) of two commits.
     * @param commit1 the first commit object.
     * @param commit2 the second commit object.
     * @return the commit ID of the LCA.
     */
    static Commit lca(Commit commit1, Commit commit2) {
        Set<String> set = new HashSet<>(ancestors(commit1));
        Stack<Commit> bfs = new Stack<>(); // the Stack for bfs
        bfs.push(commit2);
        while (!bfs.empty()) {
            Commit curr = bfs.pop();
            if (curr == null) {
                continue;
            } // Special case: skip null Commit.
            if (set.contains(curr.id())) {
                return curr;
            }
            bfs.push(curr.getParentCommit());
            bfs.push(curr.getParentMergeCommit());
        }
        return null;
    }

    /**
     * Recursively collect and return a Set of all ancestors' ID of the given Commit object,
     * including merge parents.
     */
    static Set<String> ancestors(Commit commit) {
        Set<String> set = new HashSet<>();
        if (commit == null) {
            return set;
        } // Special case: return an empty Set if the given Commit is null.
        set.add(commit.id());
        set.addAll(ancestors(commit.getParentMergeCommit()));
        set.addAll(ancestors(commit.getParentCommit()));
        return set;
    }

    /** Record a new commit's ID to the allCommitsID file. */
    static void recordCommitID(String commitID) {
        Tree allCommitsID = getAllCommitsID();
        allCommitsID.putBlobID(commitID, null);
        Utils.writeObject(ALL_COMMITS_ID, allCommitsID);
    }

    /** Return a Tree object that captures all IDs of commits ever made. */
    static Tree getAllCommitsID() {
        return Utils.readObject(ALL_COMMITS_ID, Tree.class);
    }
}
