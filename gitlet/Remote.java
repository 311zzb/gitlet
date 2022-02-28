package gitlet;

import java.io.File;

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
//        File remoteGitlet = getRemoteGitlet(remoteName);
//        Remote remote = new Remote(remoteGitlet);
//        Commit remoteHeadCommit =
//        Commit localHeadCommit = Cache.getLatestCommit();
//
//
//
    }


}
