package gitlet;

import java.io.File;
import java.util.Set;

import static gitlet.Commit.recordCommitID;
import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static java.io.File.separator;

public class Remote {

    File _remoteWD;

    /**
     * Construct a remote repository representation.
     * @param remoteGitlet the .gitlet directory of the remote repository.
     */
    private Remote(File remoteGitlet) {
        this._remoteWD = remoteGitlet.getParentFile();
    }

    /**
     * Change the CWD in Gitlet running environment to the remote repository's working directory.
     * Must call before commanding the remote repository.
     */
    private void remoteRunner() {
        Repository.assignStaticVariables(this._remoteWD);
    }
    /**
     * Change the CWD in Gitlet running environment to the local repository's working directory.
     */
    private void localRunner() {
        Repository.assignStaticVariables(Main.localCWD);
    }

    private String getHEAD() {
        remoteRunner();
        String x = Cache.getHEAD();
        localRunner();
        return x;
    }
    private Commit getLatestCommit() {
        remoteRunner();
        Commit x = Cache.getLatestCommit();
        localRunner();
        return x;
    }
    private String getBranch(String branchName) {
        remoteRunner();
        String x = Cache.getBranch(branchName);
        localRunner();
        return x;
    }
    private Commit getCommit(String id) {
        remoteRunner();
        Commit x = Cache.getCommit(id);
        localRunner();
        return x;
    }
    private String cacheAndQueueForWriteHashObject(HashObject object) {
        remoteRunner();
        String x = Cache.cacheAndQueueForWriteHashObject(object);
        localRunner();
        return x;
    }
    private void writeBack() {
        remoteRunner();
        Cache.writeBack();
        localRunner();
    }
    private Set<String> commitAncestors(Commit commit) {
        remoteRunner();
        Set<String> x = Commit.ancestors(commit);
        localRunner();
        return x;
    }
    private void moveHEAD(String branchName) {
        remoteRunner();
        Branch.moveHEAD(branchName);
        localRunner();
    }
    private void moveCurrBranch(String commitID) {
        remoteRunner();
        Branch.moveCurrBranch(commitID);
        localRunner();
    }
    private void checkoutToCommit(String commitID) {
        remoteRunner();
        Repository.checkoutToCommit(commitID);
        localRunner();
    }
    private boolean existBranch(String branchName) {
        remoteRunner();
        boolean x = Branch.existBranch(branchName);
        localRunner();
        return x;
    }
    private void mkNewBranch(String branchName) {
        remoteRunner();
        Branch.mkNewBranch(branchName);
        localRunner();
    }
    private void mkNewStage() {
        remoteRunner();
        Stage.mkNewStage();
        localRunner();
    }
    private Tree getCommitTree(Commit commit) {
        remoteRunner();
        Tree x = commit.getCommitTree();
        localRunner();
        return x;
    }
    private Blob getBlob(String blobID) {
        remoteRunner();
        Blob x = Cache.getBlob(blobID);
        localRunner();
        return x;
    }
    private void recordCommitID(String commitID) {
        remoteRunner();
        Commit.recordCommitID(commitID);
        localRunner();
    }




    /* STATIC METHODS */

    /* ADD-REMOTE COMMAND */

    /**
     * Execute the add-remote command.
     * @param remoteName the designated remote name.
     * @param path the given path to the remote.
     */
    public static void addRemote(String remoteName, String path) {
        if (!path.endsWith("/.gitlet") || path.contains("\\")) {
            printAndExit("Bad remote repository syntax.");
        } // Special case: abort if invalid path.
        File remote = join(REMOTES_DIR, remoteName);
        if (remote.exists()) {
            printAndExit("A remote with that name already exists.");
        } // Special case: abort if a remote with the given name already exists.
        writeRemote(remote, path);
    }
    private static void writeRemote(File remoteFile, String path) {
        path = path.replace("/", separator);
        writeContents(remoteFile, path);
    }

    /**
     * Get the file referencing the remote.
     * @param remoteName the given remote name.
     * @return the remote reference file.
     */
    static File readRemote(String remoteName) {
        File remoteFile = join(REMOTES_DIR, remoteName);
        if (!remoteFile.exists()) {
            printAndExit("A remote with that name does not exist.");
        } // Special case: abort if a remoteFile with the given name not exists.
        return remoteFile;
    }

    /**
     * Get the file of the remote .gitlet directory.
     * @param remoteName the given remote name.
     * @return the .gitlet directory file of the remote.
     */
    static File getRemoteGitlet(String remoteName) {
        File remoteFile = readRemote(remoteName);
        String path = readContentsAsString(remoteFile);
        File remoteGitlet = join(path);
        if (!remoteGitlet.exists()) {
            printAndExit("Remote directory not found.");
        } // Special case: abort if the remote .gitlet directory does not exist.
        return remoteGitlet;
    }

    /* RM-REMOTE COMMAND */

    /**
     * Execute the rm-remote command.
     * @param remoteName the designated remote's name.
     */
    public static void rmRemote(String remoteName) {
        File remoteFile = readRemote(remoteName);
        remoteFile.delete();
    }

    /* PUSH COMMAND */

    public static void push(String remoteName, String remoteBranchName) {
        File remoteGitlet = getRemoteGitlet(remoteName);
        Remote remote = new Remote(remoteGitlet);
        if (!remote.existBranch(remoteBranchName)) {
            remote.mkNewBranch(remoteBranchName);
        } // Special case: create a new branch if the remote does not have the input branch.
        String remoteHeadCommitID = remote.getBranch(remoteBranchName);
        Commit remoteHeadCommit = remote.getCommit(remoteHeadCommitID);
        Commit localHeadCommit = Cache.getLatestCommit();

        Set<String> commitsToPush = commitsToPush(localHeadCommit, remoteHeadCommit, remote);
        pushCommits(commitsToPush, remote);
        pushReset(remote, localHeadCommit.id(), remoteBranchName);
    }

    private static void pushReset(Remote remote, String commitID, String remoteBranchName) {
        remote.moveHEAD(remoteBranchName);
        remote.moveCurrBranch(commitID);
        remote.checkoutToCommit(commitID);
        remote.mkNewStage();
        remote.writeBack();
    }

    /** Return a Set of String containing the IDs of commits that should be pushed to the remote repo. */
    private static Set<String> commitsToPush(Commit localC, Commit remoteC, Remote remote) {
        Set<String> localCommitAncestors = Commit.ancestors(localC);
        if (!localCommitAncestors.contains(remoteC.id())) {
            printAndExit("Please pull down remote changes before pushing.");
        } // Special case: abort if the remote branch’s head is not in the history of the current local head.
        Set<String> remoteCommitAncestors = remote.commitAncestors(remoteC);
        localCommitAncestors.removeAll(remoteCommitAncestors);
        return localCommitAncestors;
    }

    private static void pushCommits(Set<String> commitIDs, Remote remote) {
        for (String commitID : commitIDs) {
            Commit commit = Cache.getCommit(commitID);
            pushCommit(commit, remote);
        }
        remote.writeBack();
    }

    private static void pushCommit(Commit commit, Remote remote) {
        remote.cacheAndQueueForWriteHashObject(commit);
        Tree tree = commit.getCommitTree();
        remote.cacheAndQueueForWriteHashObject(tree);
        for (String fileName : tree) {
            Blob blob = tree.getBlob(fileName);
            remote.cacheAndQueueForWriteHashObject(blob);
        }
        remote.recordCommitID(commit.id());
    }

    /* FETCH COMMAND */

    public static void fetch(String remoteName, String remoteBranchName) {
        File remoteGitlet = getRemoteGitlet(remoteName);
        Remote remote = new Remote(remoteGitlet);
        if (!remote.existBranch(remoteBranchName)) {
            printAndExit("That remote does not have that branch.");
        } // Special case:abort if the remote repo does not have a branch with the given name.
        String remoteHeadCommitID = remote.getBranch(remoteBranchName);
        Commit remoteHeadCommit = remote.getCommit(remoteHeadCommitID);
        Commit localHeadCommit;
        String localNewBranchName = remoteName + "/" + remoteBranchName;
        if (!Branch.existBranch(localNewBranchName)) {
            Branch.mkNewBranch(localNewBranchName);
            localHeadCommit = null;
        } else {
            localHeadCommit = Cache.getCommit(Cache.getBranch(localNewBranchName));
        }
        Set<String> commitsToFetch = commitsToFetch(localHeadCommit, remoteHeadCommit, remote);
        fetchCommits(remote, commitsToFetch);
        Branch.moveBranch(localNewBranchName, remoteHeadCommitID);
    }

    /** Return a Set of String containing the IDs of commits that should be fetched from the remote repo. */
    private static Set<String> commitsToFetch(Commit localC, Commit remoteC, Remote remote) {
        Set<String> localCommitAncestors = Commit.ancestors(localC);
        Set<String> remoteCommitAncestors = remote.commitAncestors(remoteC);
        remoteCommitAncestors.removeAll(localCommitAncestors);
        return remoteCommitAncestors;
    }

    private static void fetchCommits(Remote remote, Set<String> commitIDs) {
        for (String commitID : commitIDs) {
            fetchCommit(remote, commitID);
        }
    }

    private static void fetchCommit(Remote remote, String commitID) {
        Commit commit = remote.getCommit(commitID);

        Cache.cacheAndQueueForWriteHashObject(commit);
        Tree tree = remote.getCommitTree(commit);
        Cache.cacheAndQueueForWriteHashObject(tree);
        for (String fileName : tree) {
            Blob blob = remote.getBlob(tree.getBlobID(fileName));
            Cache.cacheAndQueueForWriteHashObject(blob);
        }
        Commit.recordCommitID(commitID);
    }

}
