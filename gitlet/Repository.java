package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Blob.*;
import static gitlet.Branch.*;
import static gitlet.Cache.*;
import static gitlet.Commit.*;
import static gitlet.Stage.*;
import static gitlet.Utils.*;


/**
 * A class houses static methods related to the whole repository.
 * This class will handle all actual Gitlet commands by invoking methods in other classes correctly.
 * It also sets up persistence and do additional error checking.
 *
 *  @author XIE Changyuan
 */
public class Repository {

    /** The current working directory. */
    static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The HEAD file. */
    static final File HEAD = join(GITLET_DIR, "HEAD");
    /** The STAGE file. */
    static final File STAGE = join(GITLET_DIR, "STAGE");
    /** The allCommitsID file. */
    static final File ALL_COMMITS_ID = join(GITLET_DIR, "allCommitsID");
    /** The objects directory. */
    static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    /** The branches directory. */
    static final File BRANCHES_DIR = join(GITLET_DIR, "branches");
    // A list of files that should be ignored when counting untracked files or delete all CWD files.
    static final List<String> debugCWDFiles = Arrays.asList("gitlet-design.md", "Makefile", "pom.xml");

    /* INIT COMMAND --------------------------------------------------------------------------------------------------*/

    /**
     * Execute the init command.
     * 1. Set up the repository
     * 2. Create an initial commit
     */
    public static void init() {
        try {
            setUpRepository();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mkCommit("initial commit");
    }

    /**
     * Set up the repository
     * 1. Set up persistence directories
     * 2. Make the default branch "master" which is pointing null for now (no pun intended)
     * 3. Make the HEAD pointing to the master branch
     * 4. Make a new staging area
     * 5. Initialize and serialize a Tree to allCommitsID
     */
    private static void setUpRepository() throws IOException {
        setUpPersistence();
        mkNewBranch("master");
        moveHEAD("master");
        mkNewStage();
        writeObject(ALL_COMMITS_ID, new Tree());
    }

    /** Set up the persistence directories. */
    static void setUpPersistence() throws IOException {
        if (GITLET_DIR.exists()) {
            printAndExit("A Gitlet version-control system already exists in the current directory.");
        } // Special case: Abort if there is already a Gitlet version-control system in the current directory.
        BRANCHES_DIR.mkdirs();
        OBJECTS_DIR.mkdirs();
        HEAD.createNewFile();
        STAGE.createNewFile();
        ALL_COMMITS_ID.createNewFile();
    }

    /* ADD COMMAND ---------------------------------------------------------------------------------------------------*/

    /**
     * Execute the add command.
     * 1. Add a copy of the file as it currently exists to the staging area
     * 2. Remove the file from staging for removal if it was at the time of the command
     * @param fileName the designated file name
     */
    public static void add(String fileName) {
        assertGITLET();
        File targetFile = join(CWD, fileName);
        if (!targetFile.exists()) {
            printAndExit("File does not exist.");
        } // Special case: abort if such file does not exist
        addToStage(fileName);
    }

    /* COMMIT COMMAND */

    /**
     * Execute the commit command.
     * @param message the commit message
     */
    public static void commit(String message) {
        assertGITLET();
        if (message.equals("")) {
            printAndExit("Please enter a commit message.");
        } // Special case: abort if message is blank.
        mkCommit(message);
    }

    /* RM COMMAND ----------------------------------------------------------------------------------------------------*/

    /**
     * Execute the rm command.
     * 1. Abort if the file is neither staged nor tracked by the head commit.
     * 2. If the file is currently staged for addition, unstage it.
     * 3. If the file is tracked in the current commit, stage it for removal and remove it from the CWD.
     * Stage for removal: add a {fileName : null} pair into the staging area.
     * @param fileName the designated file name.
     */
    public static void rm(String fileName) {
        assertGITLET();
        Tree stage = getStage();
        Commit headCommit = getLatestCommit();
        if (!stage.containsFile(fileName) && !headCommit.trackedFile(fileName)) {
            printAndExit("No reason to remove the file.");
        } // Special case: if the file is neither staged nor tracked by the head commit, print the error message
        if (stage.containsFile(fileName)) {
            removeFromStage(fileName);
        }
        if (headCommit.trackedFile(fileName)) {
            restrictedDelete(fileName); // Remove file from CWD
            addToStage(fileName); // Add {fileName - ""} pair to the stage (sign for stage for removal)
        }
    }

    /* LOG COMMAND ---------------------------------------------------------------------------------------------------*/

    /**
     * Execute the log command.
     * 1. Get the ID of the latest commit
     * 2. Print log information starting from that commit to the initial commit recursively
     */
    public static void log() {
        assertGITLET();
        log(getLatestCommitID());
    }

    /**
     * Print log information starting from a given commit ID.
     * 1. Get the Commit object with the given CommitID
     * 2. Print its log information
     * 3. Recursively print its ascendants' log information
     * @param CommitID the given commit ID
     */
    private static void log(String CommitID) {
        if (CommitID == null) {
            return;
        } // Special case: return if the CommitID is null or already printed (useful for global-log command)
        Commit commit = getCommit(CommitID);
        System.out.println(commit.logString());
        log(commit.getParentCommitID());
    }

    /* GLOBAL-LOG COMMAND --------------------------------------------------------------------------------------------*/

    /**
     * Print log information about all commits ever made.
     * 1. Get the allCommitsID Tree which holds all commits' IDs.
     * 2. Print log information for each of the IDs.
     */
    public static void globalLog() {
        assertGITLET();
        Tree allCommitsID = getAllCommitsID();
        for (String commitID : allCommitsID) {
            Commit commit = getCommit(commitID);
            System.out.println(commit.logString());
        }
    }

    /* FIND COMMAND --------------------------------------------------------------------------------------------------*/

    /** A list of commit IDs that have the designated commit message. */
    private static final List<String> foundCommitID = new ArrayList<>();

    /**
     * Execute the find command.
     * 1. Get the allCommitsID Tree which holds all commits' IDs.
     * 2. Check each commit whether it has the designated commit message.
     * @param commitMessage the designated commit message.
     */
    public static void find(String commitMessage) {
        assertGITLET();
        Tree allCommitsID = getAllCommitsID();
        for (String commitID : allCommitsID) {
            findCheck(commitID, commitMessage);
        }
        if (foundCommitID.isEmpty()) {
            printAndExit("Found no commit with that message.");
        } // Special case: no such commit exists.
        for (String CommitID : foundCommitID) {
            System.out.println(CommitID);
        }
    }

    /**
     * Check if the designated commit has the designated commit message.
     * @param commitID the designated commit ID.
     * @param commitMessage the matching commit message.
     */
    private static void findCheck(String commitID, String commitMessage) {
        Commit commit = getCommit(commitID);
        if (commit.getMessage().equals(commitMessage)) {
            foundCommitID.add(commitID);
        }
    }

    /* STATUS COMMAND ------------------------------------------------------------------------------------------------*/

    /** Execute the status command. */
    public static void status() {
        assertGITLET();
        branchStatus();
        stageStatus();
        modificationStatus();
        untrackedStatus();
    }

    /** Print the "Modifications Not Staged For Commit" status. */
    private static void modificationStatus() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName : modifiedNotStagedFiles()) {
            System.out.println(fileName);
        }
        System.out.print("\n");
    }

    /**
     * A private helper method that construct a list of "modified but not staged" files.
     * 1. Get a Set of all file names that should be checked.
     * 2. Check each file name and fill a List for "modified but not staged files".
     * 3. Return the list.
     */
    private static List<String> modifiedNotStagedFiles() {
        Set<String> files = modifiedStatusFocusFiles();
        List<String> modifiedFiles = new ArrayList<>();
        for (String fileName : files) {
            boolean modifiedNotStagedFiles1 = modifiedNotStagedFiles1(fileName);
            boolean modifiedNotStagedFiles2 = modifiedNotStagedFiles2(fileName);
            boolean modifiedNotStagedFiles3 = modifiedNotStagedFiles3(fileName);
            boolean modifiedNotStagedFiles4 = modifiedNotStagedFiles4(fileName);
            if (modifiedNotStagedFiles1 || modifiedNotStagedFiles2) {
                modifiedFiles.add(fileName + " (modified)");
            } else if (modifiedNotStagedFiles3 || modifiedNotStagedFiles4) {
                modifiedFiles.add(fileName + " (deleted)");
            }
        }
        sortLexico(modifiedFiles);
        return modifiedFiles;
    }
    // Return a string Set that contains all file names that should be checked (CWD + Stage + Head Commit).
    private static Set<String> modifiedStatusFocusFiles() {
        Set<String> CWDFilesSet = CWDFilesSet();
        Set<String> stageFilesSet = new TreeSet<>(stagedFiles());
        Set<String> headCommitFilesSet = new TreeSet<>(getLatestCommit().trackedFiles());
        return combineSets(CWDFilesSet, stageFilesSet, headCommitFilesSet);
    }
    // Tracked in the current commit, changed in the working directory, but not staged (modified).
    private static boolean modifiedNotStagedFiles1(String fileName) {
        return trackedInHeadCommit(fileName) &&
                !notInCWD(fileName) &&
                changedInCWD(fileName) &&
                !isStagedForAdd(fileName);
    }
    // Staged for addition, but with different contents than in the working directory (modified).
    private static boolean modifiedNotStagedFiles2(String fileName) {
        return isStagedForAdd(fileName) &&
                !notInCWD(fileName) &&
                addDiffContent(fileName);
    }
    // Staged for addition, but deleted in the working directory (deleted).
    private static boolean modifiedNotStagedFiles3(String fileName) {
        return isStagedForAdd(fileName) &&
                notInCWD(fileName);
    }
    // Not staged for removal, but tracked in the current commit and deleted from the working directory (deleted).
    private static boolean modifiedNotStagedFiles4(String fileName) {
        return !isStagedForRemoval(fileName) &&
                trackedInHeadCommit(fileName) &&
                notInCWD(fileName);
    }

    // Return true if a file is tracked in the head commit.
    static boolean trackedInHeadCommit(String fileName) {
        Commit headCommit = getLatestCommit();
        return headCommit.trackedFile(fileName);
    }
    // Return true if a file is changed in CWD (different from its version in the head commit).
    static boolean changedInCWD(String fileName) {
        Commit headCommit = getLatestCommit();
        return !headCommit.getBlobID(fileName).equals(currFileID(fileName));
    }
    // Return true if a file's version in the stage is different from the working one.
    static boolean addDiffContent(String fileName) {
        Tree stage = getStage();
        return !stage.getBlobID(fileName).equals(currFileID(fileName));
    }
    // Return true if a file is not in CWD.
    static boolean notInCWD(String fileName) {
        return !join(CWD, fileName).exists();
    }


    /** Print the "Untracked Files" status. */
    private static void untrackedStatus() {
        System.out.println("=== Untracked Files ===");
        for (String fileName : untrackedFiles()) {
            System.out.println(fileName);
        }
        System.out.print("\n");
    }

    /** Return a list of files that is untracked (neither staged for addition nor tracked by the head commit). */
    private static List<String> untrackedFiles() {
        Set<String> CWDFilesList = CWDFilesSet();
        Commit headCommit = getLatestCommit();
        List<String> list = new ArrayList<>();
        for (String fileName : CWDFilesList) {
            if (debugCWDFiles.contains(fileName)) {
                continue;
            } // Ignore development files
            if (!isStagedForAdd(fileName) && !headCommit.trackedFile(fileName)) {
                list.add(fileName);
            }
        }
        sortLexico(list);
        return list;
    }

    /* CHECKOUT COMMAND ----------------------------------------------------------------------------------------------*/

    /**
     * Execute checkout command usage 1 (checkout a file to the latest commit).
     * 1. Get the ID of the latest commit
     * 2. Invoke checkout2 method with the ID of the latest commit.
     * @param fileName the designated file name
     */
    public static void checkout1(String fileName) {
        String latestCommitRef = getLatestCommitID();
        checkout2(latestCommitRef, fileName);
    }

    /**
     * Execute checkout command usage 2 (checkout a file to the given commit).
     * 1. Get the Commit object with the designated commit ID
     * 2. Get the designated file's Blob object form that commit
     * 3. Overwrite the file with that name in the CWD
     * @param commitID the designated commit ID
     * @param fileName the designated file name
     */
    public static void checkout2(String commitID, String fileName) {
        assertGITLET();
        Commit commit = getCommit(commitID);
        if (commit == null) {
            printAndExit("No commit with that id exists.");
        } // Special case: abort if there is no commit with the given commit ID
        checkoutCommitFile(commit, fileName);
    }

    /**
     * Execute checkout command usage 3 (checkout all files to the designated branch).
     * 1. Perform checks
     * 2. Move the HEAD to that branch
     * 3. Checkout to the commit that the branch is pointing to
     * @param branchName the designated branch name
     */
    public static void checkout3(String branchName) {
        if (!existBranch(branchName)) {
            printAndExit("No such branch exists.");
        } // Special case: abort if no branch with that name exists.
        if (branchName.equals(getHEAD())) {
            printAndExit("No need to checkout the current branch.");
        } // Special case: abort if that branch is the current branch.
        if (!untrackedFiles().isEmpty()) {
            printAndExit("There is an untracked file in the way; delete it, or add and commit it first.");
        } // Special case: abort if a working file is untracked.
        String commitID = getBranch(branchName);
        checkoutToCommit(commitID);
        moveHEAD(branchName);
    }

    /**
     * A private helper method that checkout to a Commit (with designated ID).
     * 1. Delete all files in the CWD
     * 2. Checkout all files tracked by that commit
     * 3. Clean the staging area
     */
    private static void checkoutToCommit(String commitID) {
        deleteCWDFiles();
        checkoutAllCommitFile(commitID);
        mkNewStage();
    }

    /** A private helper method that checkout all files that a Commit (with designated ID) tracked. */
    private static void checkoutAllCommitFile(String commitID) {
        Commit commit = getCommit(commitID);
        if (commit == null) {
            printAndExit("No commit with that id exists.");
        } // Special case: print and exit if requested a Commit that does not exist.
        for (String fileName : commit.trackedFiles()) {
            checkoutCommitFile(commit, fileName);
        }
    }

    /** A private helper method that checkout a file with fileName from a given Commit. */
    private static void checkoutCommitFile(Commit commit, String fileName) {
        Blob blob = getBlob(commit.getBlobID(fileName));
        if (blob == null) {
            printAndExit("File does not exist in that commit.");
        } // Special case: abort if such file does not exist in that commit
        overwriteCWDFile(fileName, blob);
    }

    /* BRANCH COMMAND ------------------------------------------------------------------------------------------------*/

    /**
     * Execute the branch command.
     * Create a new branch with the given name, and points it at the current head commit.
     * @param branchName the designated branch name.
     */
    public static void branch(String branchName) {
        assertGITLET();
        if (existBranch(branchName)) {
            printAndExit("A branch with that name already exists.");
        } // Special case: abort if a branch with the given name already exists
        mkNewBranch(branchName);
    }

    /* RM-BRANCH COMMAND ---------------------------------------------------------------------------------------------*/

    /**
     * Execute the rm-branch command.
     * Deletes the branch with the given name. This only means to delete the pointer associated with the branch.
     * @param branchName the designated branch name.
     */
    public static void rmBranch(String branchName) {
        assertGITLET();
        File targetBranch = join(BRANCHES_DIR, branchName);
        if (!targetBranch.exists()) {
            printAndExit("A branch with that name does not exist.");
        } // Special case: abort if a branch with the given name does not exist
        if (branchName.equals(getHEAD())) {
            printAndExit("Cannot remove the current branch.");
        } // Special case: abort if try to remove the current branch
        wipeBranch(branchName);
    }

    /* RESET COMMAND -------------------------------------------------------------------------------------------------*/

    /**
     * Execute the reset command.
     * 1. Perform the checks: the commit with the designated ID exists, and there is no working untracked file
     * 2. Checkout to the designated commit
     * 3. Move the current branch to that commit
     */
    public static void reset(String commitID) {
        assertGITLET();
        Commit commit = getCommit(commitID);
        if (commit == null) {
            printAndExit("No commit with that id exists.");
        } // Special case: abort if no commit with the given id exists.
        if (!untrackedFiles().isEmpty()) {
            printAndExit("There is an untracked file in the way; delete it, or add and commit it first.");
        } // Special case: abort if a working file is untracked.
        checkoutToCommit(commitID);
        String fullCommitID = commit.id(); // This prevents an abbreviated ID been written to branch files
        moveCurrBranch(fullCommitID);
    }

    /* MERGE COMMAND -------------------------------------------------------------------------------------------------*/

    /**
     * Execute the merge command (merge files from the given branch into the current branch).
     * 1. Get the latest Commit object of the current branch, the given branch, and the common ancestors (split commit).
     * 2. Calculate which files will be changed in what manners, and perform checks.
     * 3. Modify the CWD following the result from step 2, staging for addition or removal as we go.
     * 4. Make a merge commit.
     * @param branchName the designated branch.
     */
    public static void merge(String branchName) {
        assertGITLET();
        String currCommitID = getLatestCommitID();
        String otherCommitID = getBranch(branchName);
        Commit currCommit = getCommit(currCommitID);
        Commit otherCommit = getCommit(otherCommitID);
        Commit splitCommit = lca(currCommit, otherCommit);
        Map<String, Set<String>> mergeModifications = mergeWillModify(splitCommit, currCommit, otherCommit);
        mergeModifyCWD(currCommit, otherCommit, mergeModifications);
        Boolean conflicted = !mergeModifications.get("conflict").isEmpty();
        mkMergeCommit(branchName, conflicted);
    }

    /** Modify files in the CWD (either use the version in the other branch, or make a conflict file) accordingly. */
    private static void mergeModifyCWD(Commit curr,
                                       Commit other,
                                       Map<String, Set<String>> mergeModifications) {
        Set<String> useOtherFiles = mergeModifications.get("other");
        Set<String> conflictFiles = mergeModifications.get("conflict");
        useOther(useOtherFiles, other);
        makeConflict(conflictFiles, curr, other);
    }

    /** Modify all conflict files and add them to the stage. */
    private static void makeConflict(Set<String> files, Commit curr, Commit other) {
        for (String fileName : files) {
            File file = join(CWD, fileName);
            String conflictContent = makeConflictContent(fileName, curr, other);
            writeContents(file, conflictContent);
            add(fileName);
        }
    }

    /** Return the right content for a conflict file after merging. */
    private static String makeConflictContent(String fileName, Commit curr, Commit other) {
        String currContent = curr.getFileContent(fileName);
        String otherContent = other.getFileContent(fileName);
        return "<<<<<<< HEAD\n" + currContent + "\n=======\n" + otherContent + ">>>>>>>";
    }

    /**
     * Modify files in CWD to their versions in the other commit, and stage the change (add or rm).
     * @param files a Set of file names that should be modified.
     * @param other the other commit.
     */
    private static void useOther(Set<String> files, Commit other) {
        for (String fileName : files) {
            File file = join(CWD, fileName);
            Blob otherFileBlob = getBlob(other.getBlobID(fileName));
            if (otherFileBlob == null) { // the file is removed in the other branch.
                rm(fileName);
            }
            else {
                String otherContent = otherFileBlob.getContent();
                writeContents(file, otherContent);
                add(fileName);
            }
        }
    }

    /**
     * Perform the checks for the merge command and return a Map of necessary modifications.
     * @return a Map that guides merging.
     * "other" maps to a List of file names that should use the version in the other branch.
     * "conflict" maps to a List of files that is conflicted.
     */
    private static Map<String, Set<String>> mergeWillModify(Commit split, Commit curr, Commit other) {
        mergeChecks1(split, curr, other);
        Set<String> mergeFocusFiles = combineSets(
                split.trackedFiles(),
                curr.trackedFiles(),
                other.trackedFiles());
        Map<String, Set<String>> mergeWillModify = mergeLogic(mergeFocusFiles, split, curr, other);
        Set<String> changingFiles = combineSets(mergeWillModify.get("other"), mergeWillModify.get("conflict"));
        mergeChecks2(changingFiles);
        return mergeWillModify;
    }

    /** A private helper method that captures the logic of the merge command. */
    private static Map<String, Set<String>> mergeLogic(Set<String> focusFiles, Commit split, Commit curr, Commit other) {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("other", new HashSet<>());
        map.put("conflict", new HashSet<>());
        for (String file : focusFiles) {
            String splitVer = split.getBlobID(file);
            String currVer = curr.getBlobID(file);
            String otherVer = other.getBlobID(file);
            if (Objects.equals(splitVer, currVer)) {
                map.get("other").add(file);
            } else if (Objects.equals(splitVer, otherVer)) { // do nothing
            } else if (Objects.equals(currVer, otherVer)) { // do nothing
            } else {
                map.get("conflict").add(file);
            }
        }
        return map;
    }

    /** Perform checks for the merge command. */
    private static void mergeChecks1(Commit split, Commit curr, Commit other) {
        if (other == null) {
            printAndExit("A branch with that name does not exist.");
        } // Abort merging if a branch with the given name does not exist.
        if (!stagedFiles().isEmpty()) {
            printAndExit("You have uncommitted changes.");
        } // Abort merging if there are staged additions or removals present.
        if (Objects.equals(curr.id(), other.id())) {
            printAndExit("Cannot merge a branch with itself.");
        } // Abort merging if attempting to merge a branch with itself.
        if (Objects.equals(split.id(), other.id())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } // Exit if the split point is the same commit as the given branch. The merge is complete.
        if (Objects.equals(split.id(), curr.id())) {
            fastForward(other);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        } // Fast-forward and exit if the split point is the same commit as the current branch.
    }

    /**
     * Fast-forward the current branch to the designated commit.
     * Only called when the split commit is the same as the current commit.
     * */
    private static void fastForward(Commit other) {
        String commitID = other.id();
        checkoutToCommit(commitID);
        moveCurrBranch(commitID);
    }

    /** Perform checks for the merge command. */
    private static void mergeChecks2(Set<String> changingFiles) {
        List<String> untrackedFiles = untrackedFiles();
        List<String> modifiedNotStagedFiles = modifiedNotStagedFiles();
        for (String file: changingFiles) {
            if (untrackedFiles.contains(file)) {
                printAndExit(
                        "There is an untracked file in the way; delete it, or add and commit it first.");
            } // Abort merging if an untracked file in the current commit would be overwritten or deleted by the merge.
            if (modifiedNotStagedFiles.contains(file)) {
                printAndExit(
                        "There is an unstaged change in the way; revoke it, or add and commit it first.");
            } // Abort merging if there are unstaged changes to file that would be changed by the merge.
        }
    }

    /* MISC ----------------------------------------------------------------------------------------------------------*/

    /** Assert the CWD contains a .gitlet directory. */
    private static void assertGITLET() {
        if (!GITLET_DIR.exists()) {
            printAndExit("Not in an initialized Gitlet directory.");
        }
    }

    /**
     * Overwrite the file in CWD of designated file name with the content in the given Blob object.
     * @param fileName the designated file name
     * @param overwriteSrc the given Blob object
     */
    private static void overwriteCWDFile(String fileName, Blob overwriteSrc) {
        String overwriteContent = overwriteSrc.getContent();
        File file = join(CWD, fileName);
        writeContents(file, overwriteContent);
    }

    /**
     * Sort a string List in lexicographical order in place.
     * @param list the given list
     */
    static void sortLexico(List<String> list) {
        list.sort(Comparator.comparing((String x) -> x));
    }

    /** Delete all files in the CWD. */
    static void deleteCWDFiles() {
        Set<String> files = CWDFilesSet();
        for (String fileName : files) {
            if (debugCWDFiles.contains(fileName)) {
                continue;
            } // Ignore development files
            File file = join(CWD, fileName);
            file.delete();
        }
    }

    /** Return a Set of all files' names in the CWD. */
    private static Set<String> CWDFilesSet() {
        List<String> CWDFilesList = plainFilenamesIn(CWD);
        if (CWDFilesList == null) {
            return new TreeSet<>();
        }
        return new TreeSet<>(CWDFilesList);
    }

    /** Generic method to merge (union) multiple sets in Java. */
    @SafeVarargs
    private static<T> Set<T> combineSets(Set<T>... sets) {
        Set<T> collection = new HashSet<>();
        for (Set<T> e: sets) {
            collection.addAll(e);
        }
        return collection;
    }

    /** Print a message and exit the execution with status 0. */
    static void printAndExit(String msg) {
        System.out.println(msg);
        System.exit(0);
    }
}
