# Gitlet Design Document

## Classes and Data Structures

### Main

Driver class for Gitlet, a subset of the Git version-control system.
Account for validating the number of arguments and invoking package-private methods according to received commands.
The cache write back method `Cache.writeBack()` which enabling the persistence of Gitlet is also invoked in this class.

#### Fields

This class contains only `static` methods since `Main` should not be instantiated.

1. `static final File localCWD  = new File(System.getProperty("user.dir"))`
   The current working directory `File` object.
2. `static String currCommand` A static variable that holds the current command. Used during `pull` command.
3. `public static void main(String[] args)` The main method of Gitlet.
4. `private static void assertArgsNum(String[] args, int n)`
   Throw a GitletException if args _don't have_ exactly n elements.
5. `private static void assertNotArgsNum(String[] args, int n)`
   Throw a GitletException if args _have_ exactly n elements.
6. `private static String[] getOperands(String[] args)`
   Strip the first element of the input array and return the rest.
7. `private static void assertString(String expected, String actual)`
   Assert two `String` are equal.

### Cache

This class is used to house static methods that facilitate lazy loading and caching of persistence.
This file will set up data structures for caching, load necessary objects,
and write back the cache at the very end of execution.
This class will never be instantiated.

This class defers all `HashObject` and its subclasses' logic to them.
For example, instead of deserialize and serialize objects directly,
`Cache` class will invoke methods from the corresponding class to do that.

On the other hand, the `Cache` class will do all the `getxxx()` methods which retrieving desired objects lazily
from the cache.

#### Fields

1. Caching `HashObject`
   1. `static final Map<String, HashObject> cachedHashObjects` A `Map` that stores cached ID and `HashObject` pairs.
   2. `static Map<String, HashObject> cachedRemoteHashObjects` The cache for the remote repository.
   3. `private static HashObject getHashObject(String id)`
      Lazy loading and caching of HashObjects.
      Being `private` because a `HashObject` will never be requested as `HashObject`
      (as `Commit` or `Tree` or `Blob` instead).
      Special case: return `null` if requesting a commit with `null` or `""`.
   4. `static Commit getCommit(String id)`
      A method that lazy-load a `Commit` with `id` utilizing `getHashObject(String id)`.
   5. `static Tree getTree(String id)`
      A method that lazy-load a `Tree` with `id` utilizing `getHashObject(String id)`.
   6. `static Blob getBlob(String id)`
      A method that lazy-load a `Blob` with `id` utilizing `getHashObject(String id)`.
   7. `static Commit getLatestCommit()` Get the `Commit` object of the latest commit utilizing `getCommit(String id)`.
   8. `static final Set<String> queuedForWriteHashObjects`
      New HashObjects' IDs that are queued for writing to filesystem.
   9. `static String cacheAndQueueForWriteHashObject(HashObject object)`
      Manually cache a `HashObject` by put a `HashObject` into the cache,
      and queue it for writing to filesystem. Return its ID.
   10. `static void writeBackAllQueuedHashObject()`
       Write back all queued-for-writing `HashObjects` to filesystem. Invoked upon exit.
   11. `static final Set<String> queuedForDeleteHashObject`
       Deprecated `HashObject`s' IDs that are queued for deletion from filesystem.
   12. `static void queueForDeleteHashObject(String id)` Given Qa `HashObject`'s ID, queue it for deletion.
   13. `static void deleteAllQueuedHashObject()` Delete all queued-for-deletion `HashObject`s. Invoked upon exit.
2. Caching Branches
   1. `static final Map<String, String> cachedBranches` A `Map` that stores cached branch name and commit ID pairs.
   2. `static Map<String, String> cachedRemoteBranches` The cache for the remote repository.
   3. `static String getBranch(String branchName)` Lazy loading and caching of branches.
   4. `static String getLatestCommitID()`
      A method that lazy-load the ID of the latest commit by `getBranch(getHEAD())`.
   5. `static void cacheBranch(String branchName, String commitID)`
      Manually cache a `Branch` by putting a `branchName` - `commitID` pair into the cache.
   6. `static void wipeBranch(String branchName)`
      Manually wipe the pointer of a designated branch.
   7. `static void writeBackAllBranches()`
      Write back (update) all branches to filesystem. Invoked upon exit.
      If a branch's pointer is wiped out, delete the branch file in the filesystem.
      Special case: ignore branch with empty name.
3. Caching `HEAD`
   1. `static String cachedHEAD` A `String` that stores cached `HEAD`, the current branch's name.
   2. `static String cachedRemoteHEAD` The cache for the remote repository.
   3. `static String getHEAD()` Lazy loading and caching of `HEAD`.
   4. `static void cacheHEAD(String branchName)`
      Manually cache the `HEAD` by assigning the `cachedHEAD` to a given `branchName`.
   5. `static void writeBackHEAD()` Write back (update) the `HEAD` file. Invoked upon exit.
4. Caching `STAGE` (Stage ID)
   1. `static String cachedStageID` A `String` that stores cached `STAGE`, the ID of the current staging area.
   2. `static String cachedRemoteStageID` The cache for the remote repository.
   3. `static String getStageID()`
      Lazy loading and caching of STAGE (the ID of the saved staging area).
      Notice: this DOES NOT point to the current staging area after the staging area is modified and before write back.
   4. `static void cacheStageID(String newStageID)`
      Manually cache the `STAGE` by assigning the `cachedStageID` to a given `stageID`.
   5. `static void writeBackStageID()` Write back STAGE file. Invoked upon exit.
5. Caching the Stage Area
   1. `static Tree cachedStage` A `Tree` that stores cached staging area.
   2. `static Tree cachedRemoteStage` The cache for the remote repository.
   3. `static Tree getStage()` Get the `Tree` object representing the staging area utilizing `getTree(getStageID())`.
   4. `static void cacheStage(Tree stage)`
      Queue the previous staging area for deletion and manually cache the passed-in Stage.
      Special case:
      queue the previous staging area for deletion only if
      there is a commit,
      and the previous staging area is different from the Tree of the latest commit,
      and the previous staging area is not empty.
6. MISC
   1. `static void writeBack()` Write back all caches. Invoked upon exit.
   2. `static void cleanCache()` Reset all caches. Used for testing proposes.
   3. `private static boolean inRemoteRepo()` Return `true` if currently operating on the remote repository.

### Repository

A class houses static methods related to the whole repository.
This class will handle all actual Gitlet commands by invoking methods in other classes correctly.
It also sets up persistence and do additional error checking.

#### Fields

1. Static Variables
   1. `static File CWD`
      The Current Working Directory. A package-private static variable.
   2. `static File GITLET_DIR`
      The `.gitlet` directory, where all the state of the repository will be stored. Package-private.
   3. `static File HEAD`
      The `.gitlet/HEAD` file. This file stores the name of the active branch.
   4. `static File STAGE`
      The `.gitlet/STAGE` file, where the ID of the current staging area is stored.
   5. `static File ALL_COMMITS_ID`
      The `.gitlet/allCommitsID` file, which is a serialized `Tree` that holds all the IDs of existing commits.
   6. `static File OBJECTS_DIR`
      The `.gilet/objects` directory. This is the object database where all `HashObject` live.
   7. `static File BRANCHES_DIR`
      The `.gitlet/branches` directory. Each branch is stored as a file under this directory.
   8. `static void assignStaticVariables(File cwd)`
      Assign the above static variables according to the given `CWD`.
      This is useful dealing with local and remote repositories.
      The current working directory is passed in as `CWD` for default, but the remote repository directory
      will be passed in when manipulating it.
2. `init` command
   1. `public static void init()`
      The method which handles the `init` command. Implementation details in the Algorithms section.
   2. `static void setUpPersistence()`
      A helper method of `init` command, set up the persistence directories.
      Implementation details in the Algorithms section.
      This method also checks if there is an existing `.gitlet` directory and abort the execution if so.
3. `add` command
   1. `public static void add(String fileName)`
      Execute the add command by adding a copy of the file as it currently exists to the staging area.
4. `commit` command
   1. `public static void commit(String message)` Execute the commit command.
5. `rm` command
   1. `public static void rm(String fileName)` Execute the rm command. Implementation details in the Algorithms section.
6. `log` command
   1. `public static void log()` Execute the log command. Implementation details in the Algorithms section.
   2. `private static void log(String CommitID)`
      Print log information recursively. Starting from the commit with the given commit ID, to the initial commit.
7. `global-log` command
   1. `public static void globalLog()`
      Print log information about all commits ever made. Implementation details in the Algorithms section.
8. `find` command
   1. `private static final List<String> foundCommitID` A list of commit IDs that have the designated commit message.
   2. `public static void find(String commitMessage)`
      Execute the `find` command. Implementation details in the Algorithms section.
   3. `private static void findCheck(String CommitID, String commitMessage)`
      Check if the designated commit has the designated commit message.
9. `status` command
   1. `public static void status()` Execute the status command. Implementation details in the Algorithms section.
   2. "Modifications Not Staged For Commit"
      1. `private static void modificationStatus()` Print the "Modifications Not Staged For Commit" status.
      2. `private static List<String> modifiedNotStagedFiles()`
         A private helper method that construct a list of "modified but not staged" files.
         Implementation details in the Algorithms section.
      3. `private static Set<String> modifiedStatusFocusFiles()`
         Return a string `Set` that contains all file names that should be checked 
         (file names in the `CWD` or the Stage or tracked by the head Commit).
      4. `private static boolean modifiedNotStagedFiles1(String fileName)`
         Return `true` if a file is tracked in the current commit, changed in the working directory, but not staged (modified).
      5. `private static boolean modifiedNotStagedFiles2(String fileName)`
         Return `true` if a file is staged for addition, but with different contents than in the working directory (modified).
      6. `private static boolean modifiedNotStagedFiles3(String fileName)`
         Return `true` if a file is staged for addition, but deleted in the working directory (deleted).
      7. `private static boolean modifiedNotStagedFiles4(String fileName)`
         Return `true` if a file is not staged for removal, 
         but tracked in the current commit and deleted from the working directory (deleted).
      8. `static boolean trackedInHeadCommit(String fileName)`
         Return `true` if a file is tracked in the head commit.
      9. `static boolean changedInCWD(String fileName)`
         Return `true` if a file is changed in the `CWD` (different from its version in the head commit).
      10. `static boolean addDiffContent(String fileName)`
          Return `true` if a file's version in the stage is different from the working one.
      11. `static boolean notInCWD(String fileName)`
          Return `true` if a file is not in the `CWD`.
   3. "Untracked Files"
      1. `private static void untrackedStatus()` Print the "Untracked Files" status.
      2. `private static List<String> untrackedFiles()`
         Return a list of files that is untracked (neither staged for addition nor tracked by the head commit).
10. `checkout` command
    1. `public static void checkout1(String fileName)`
       Execute checkout command usage 1 (checkout a file to the latest commit).
       Implementation details in the Algorithms section.
    2. `public static void checkout2(String commitID, String fileName)`
       Execute checkout command usage 2 (checkout a file to the given commit).
       Implementation details in the Algorithms section.
    3. `public static void checkout3(String branchName)`
       Execute checkout command usage 3 (checkout all files to the designated branch).
       Implementation details in the Algorithms section.
    4. `static void checkoutToCommit(String commitID)`
       A helper method that checkout to a `Commit` (with designated ID).
    5. `private static void checkoutAllCommitFile(String commitID)`
       A private helper method that checkout all files that a `Commit` (with designated ID) tracked.
    6. `private static void checkoutCommitFile(Commit commit, String fileName)`
       A private helper method that checkout a file with `fileName` from a given `Commit`.
11. `branch` command
    1. `public static void branch(String branchName)`
       Execute the branch command. Implementation details in the Algorithms section.
12. `rm-branch` command
    1. `public static void rmBranch(String branchName)`
       Execute the rm-branch command. Implementation details in the Algorithms section.
13. `reset` command
    1. `public static void reset(String commitID)` 
       Execute the reset command. Implementation details in the Algorithms section.
       Abbreviated commit ID will be handled, and branches will always point to full IDs.
14. `merge` command
    1. `public static void merge(String branchName)`
       Execute the merge command (merge files from the given branch into the current branch).
       Implementation details in the Algorithms section.
    2. `private static void mergeModifyCWD(Commit curr, Commit other, Map<String, Set<String>> mergeModifications)`
       Modify files in the `CWD` (either use the version in the other branch, or make a conflict file) accordingly.
    3. `private static void makeConflict(Set<String> files, Commit curr, Commit other)`
       Modify all conflict files and add them to the stage.
    4. `private static String makeConflictContent(String fileName, Commit curr, Commit other)`
       Return the right content for a conflict file after merging.
    5. `private static void useOther(Set<String> files, Commit other)`
       Modify files in `CWD` to their versions in the other commit, and stage the change (add or rm).
    6. `private static Map<String, Set<String>> mergeWillModify(Commit split, Commit curr, Commit other)`
       Perform the checks for the merge command and return a `Map` of necessary modifications.
    7. `private static Map<String, Set<String>> mergeLogic(Set<String> focusFiles, Commit split, Commit curr, Commit other)`
       A private helper method that captures the logic of the merge command.
    8. `private static void mergeChecks1(Commit curr, Commit other)` Perform checks for the merge command.
    9. `private static void fastForward(Commit other)`
       Fast-forward the current branch to the designated commit and print information.
       Only called when the split commit is the same as the current commit.
       Special case: do not print fast-forward info when pulling.
    10. `private static void mergeChecks2(Set<String> changingFiles)` Perform checks for the merge command.
15. misc
    1. `private static void assertGITLET()` Assert the `CWD` contains a `.gitlet` directory.
    2. `private static void overwriteCWDFile(String fileName, Blob overwriteSrc)`
       Overwrite the file in `CWD` of designated file name with the content in the given `Blob` object.
    3. `static void sortLexico(List<String> list)` Sort a string `List` in lexicographical order in place.
    4. `static void deleteCWDFiles()` Delete all files in the `CWD`.
    5. `private static Set<String> CWDFilesSet()` Return a Set of all files' names in the `CWD`.
    6. `private static<T> Set<T> combineSets(Set<T>... sets)` Generic method to merge (union) multiple sets in Java.
    7. `static void printAndExit(String msg)` Print a message and exit the execution with status `0`.

### Branch

This class houses static methods that related to branch and HEAD.
It contains methods for loading and writing branch files and the HEAD file.
This class will never be instantiated since there are only static methods.

#### Fields

1. `static String loadBranch(String branchName)`
   Load a branch file from filesystem with designated name. 
   Return `null` if the branch name is `""` (nothing) or there is no branch.
   Invoked by the Cache class.
2. `static boolean existBranch(String branchName)` Return `true` if a branch exists.
3. `static List<String> loadAllBranches()`
   Load all branch files from the filesystem. Return a `List` contains all commit IDs that are pointed by a branch.
4. `static void branchStatus()` Print the "Branches" status. Implementation details in the Algorithms section.
5. `static void writeBranch(String branchName)`
   Get a branch's information from cache and write it back to filesystem. Invoked by the Cache class.
6. `static void deleteBranch(String branchName)`
   Delete the designated branch in the filesystem. Invoked by the Cache class.
7. `static void mkNewBranch(String branchName, String commitID)`
   Make a new branch with designated name at the latest commit by caching it manually.
8. `static void moveCurrBranch(String commitID)` Make the current branch pointing to a designated commit.
9. `static void moveBranch(String branchName, String commitID)`
   Move the designated branch to point to a commit with designated ID.
10. `static String loadHEAD()`
    Load the `HEAD` file and return the current branch's name. Invoked by the Cache class.
11. `static void writeHEAD()`
    Get the `HEAD` from cache and write it back to filesystem. Invoked by the Cache class.
12. `static void moveHEAD(String branchName)` Make the `HEAD` pointing to a designated branch.
13. `private static File branchFile(String branchName)` Get the `File` object of a branch with designated name.
14. `private static List<String> allBranches()` 
    Return a `List` of all branches' names. Support fetched remote branches.

### Stage

This class houses static methods that related to Stage (the staging area).
It contains methods for loading and writing the `STAGE` file, as well as making a new staging area.
This class will never be instantiated since there are only static methods.

#### Fields

1. `static String loadStageID()`
   Return the ID of the current staging area (a `Tree` object). Invoked by the Cache class.
2. `static void putInStage(String fileName, String BlobID)`
   Copy the staging area and add a `fileName` - `BlobID` pair.
   Mark the previous staging area `Tree` for deletion.
   This function should only be invoked once per run.
3. `static void removeFromStage(String fileName)`
   Copy the staging area and remove the entry with a specific `fileName` (if exists) from it.
   Mark the previous staging area `Tree` for deletion.
   This function should only be invoked once per run.
4. `static void writeStageID()` Write the stage ID in cache back to filesystem. Invoked by the Cache class.
5. `static void mkNewStage()` Make a new stage (a `Tree` object) and cache its ID.
6. `static void addToStage(String fileName)`
   Add a file to the current staging area. Implementation details in the Algorithms section.
7. `static void stageStatus()` Print the status information related with the staging area.
8. `private static List<String> stagedFiles()` Return a sorted List of file names in the current staging area.
9. `private static void stagedFilesStatus()`
   Print the "Staged Files" status. Implementation details in the Algorithms section.
10. `private static void removedFilesStatus()`
    Print the "Removed Files" status. Implementation details in the Algorithms section.
11. `static boolean isStagedForAdd(String fileName)` Return `true` if a designated file is staged for _addition_.
12. `static boolean isStagedForRemoval(String fileName)` Return `true` if a designated file is staged for _removal_.

### HashObject

This class represents a `HashObject` that will be serialized within `.gitlet/objects`, named after its SHA-1.
`HashObject` is an implementation of `Serializable` and `Dumpable`.
This file has helper methods that will return the SHA-1 (ID) of a `HashObject`.
As well as static methods that returning the `HashObject` object corresponding to its `ID` (SHA-1),
and write to or delete from the object database a `HashObject`.

#### Fields

1. `private static final Boolean OPTIMIZATION`
   Allow you to switch between flat `objects` directory (easy to debug) and HashTable `objects` directory (better performance).
   Notice: this should be consistence for a single Gitlet repository.
2. `String id()` Get the SHA-1 of `THIS`.
3. `public void dump()` Print the type of this object on System.out.
4. `static HashObject loadHashObject(String id)` Load a type object with its ID.
   Special case: return `null` if told to load an object that does not exist.
5. `static void writeCachedHashObject(String id)` Write a cached HashObject with ID in cachedObjects to filesystem.
6. `static void deleteHashObject(String id)` Delete a HashObject from filesystem.
7. `static private File optimizedObjectIDFolder(String id)`
   Helper method that returns the housing directory of a `HashObject` with the given ID.
   Used in the optimized object database.
8. `static private File optimizedObjectIDFile(String id)`
   Helper method that returns the file of a `HashObject` with the given ID.
   Used in the optimized object database.
9. `static private File optimizedObjectAbbrevIDFile(String id)`
   Helper method that return the file of a `HashObject` with the given abbreviated ID.
   Used in the optimized object database.

Despite `HashObject` should be instantiated very often, it has no constructor method(s).
Any `HashObject` is designed to be instantiated as a more specific subclass, namely `Commit`, `Tree`, or `Blob`.

### Commit

This class represents a `Commit` in Gitlet, it extends the `HashObject` class.
Each instance of `Commit` have several instance variables such as commit message and time stamp.
This file also has helper methods that unlocks instance variable
as well as static method that carry out the procedure to make a new commit.

#### Fields

1. `private final String _message` The commit message.
2. `private final String _parentCommitID` The ID of the parent commit.
3. `private final String _parentMergeCommitID` The ID of the second parent (if any).
4. `private final Date _timeStamp` A time stamp of the commit been made.
5. `private final String _treeID` The ID of the associated `Tree` object.
6. `private Commit(String parentCommitID, String message, String treeRef)`
   The constructor of `Commit` class. This method is `private`
   because no "naked" instantiation of `Commit` is allowed outside the `Commit` class.
   Additionally, the time stamp is set to `1970.01.01 00:00:00` for initial commit.
7. `private Commit(String firstCommitID, String secondCommitID, String message, String treeRef)`
   Constructor for merge commits.
8. `public String toString()` Content-addressable overriding `toString()` method.
9. `String logString()` Return the log information of this `Commit`.
10. `public void dump()` Print information of this `Commit` on `System.out`.
11. `String getMessage()` Get the message of this `Commit`.
12. `String getParentCommitID()` Get the ID of the parent commit.
13. `String getParentMergeCommitID()` Get the ID of the second parent commit.
14. `Commit getParentCommit()` Get the `Commit` object of the parent commit.
15. `Commit getParentMergeCommit()` Get the `Commit` object ot the second parent commit.
16. `String getCommitTreeID()` Get the ID of the associating `Tree` of this commit.
17. `Tree getCommitTree()` Get the associating `Tree` of this commit.
18. `String getBlobID(String fileName)` Get the ID of the `Blob` of a designated file name in this commit.
19. `String getFileContent(String fileName)` Return the content of a designated file name in this commit.
    Special case: return an empty `String` if there is no corresponding `Blob`.
20. `Boolean trackedFile(String fileName)` Return whether this `Commit` contains a file with `fileName`.
21. `Set<String> trackedFiles()` Return a string `Set` of tracked files of this commit.
22. `static void mkCommit(String message)` Factory method. Make a new `Commit`.
    Implementation details in the Algorithm section.
23. `static void mkMergeCommit(String givenBranchName, Boolean conflicted)`
    Factory method. Make a new merge Commit.
24. `static Commit lca(Commit commit1, Commit commit2)` Return the latest common ancestor (LCA) of two `Commit`s.
25. `static Set<String> ancestors(Commit commit)`
    Recursively collect and return a `Set` of all ancestors' ID of the given `Commit` object, including merge parents.
    Special case: return an empty `Set` if the given `Commit` is `null`.
26. `static void recordCommitID(String commitID)` Record a new commit's ID to the `.gitlet/allCommitsID` file.
27. `static Tree getAllCommitsID()` Return a `Tree` object that captures all IDs of commits ever made.

### Tree

Represent a Gitlet `Tree`, corresponding to UNIX directory entries.
Implements `Iterable<String>`, extends `HashObject`.
An instance of `Tree` object contains a `TreeMap` as instance variable, which has zero or more entries.
Each of these entries is a `fileName` - `BlobID` pair.
This class also contains `Tree` related static methods.

#### Fields

1. `private final Map<String, String> _structure` The `TreeMap` that stores `fileName` - `blobID` pairs.
2. `Tree()` The constructor of `Tree` class.
3. `Tree(Tree another)` A constructor that deep-copy the passed-in `Tree`.
4. `public String toString()` Content-addressable overriding `toString()` method.
5. `public void dump()` Print information of this `Tree` on `System.out`.
6. `boolean isEmpty()` Return whether this `Tree` is empty.
7. `boolean containsFile(String fileName)` Return `true` if a `Tree` contains a file with `fileName`.
8. `List<String> trackedFiles()`
   Return the sorted list of file names in this `Tree` following a Java string-comparison order.
9. `void putBlobID(String fileName, String blobRef)` Record a `fileName` - `blobID` pairs.
10. `void removeBlobID(String fileName)` Remove an entry with `fileName` as the key from this `Tree`.
11. `String getBlobID(String fileName)` Return the ID of a `Blob` according to a given `fileName` (if exists).
12. `Blob getBlob(String fileName)` Return a `Blob` according to a given `fileName` (if exist).
13. `public Iterator<String> iterator()` Returns an `Iterator` of this `Tree`, namely the `keySet()` of its `TreeMap`.
14. `void updateWith(Tree updater)`
    Update this `Tree` with the entries in the given `Tree`.
    Special case: remove the corresponding pair from `this` if the value to a key in the updater is `null`.
15. `static String mkNewEmptyTree()` Factory method.
    Creates an empty `Tree`, cache it and return its ID.
16. `static Tree getLatestCommitTree()` Factory method. Return the copy of the `Tree` of the latest commit if exists.
    Special case: return `null` if there is no latest commit.
17. `static String mkCommitTree()`
    Factory method.
    Return a `Tree` that capture the `Tree` from the latest commit as well as current addition and removal status.
    Implementation details in the Algorithm section.
    Special cases: make a new empty tree if there is no `Tree` in the latest commit.
18. `private static Tree copyLatestCommitTree()` Factory method. Return a deep-copy of the `Tree` in the latest commit.
19. `static Teww CWDFiles()` Return a temporary `Tree` that capture information of files in `CWD`.

### Blob

Represent a Gitlet `Blob`, corresponding to UNIX files.
Extends `HashObject`.
`Blob` has one instance variable `_content`, which holding the content of a file.
This variable enables a `Blob` to represent a version of such file.
This class also has `Blob` related static methods.

#### Fields

1. `private final String _content` The instance variable that hold the content of a version of a file.
2. `Blob(String content)` The private constructor of `Blob`.
   No "naked" instantiation of `Blob` is allowed.
3. `String getContent()` Unlocks the content of a `Blob`.
4. `public String toString()` Content-addressable overriding `toString()` method.
5. `public void dump()` Print information of this `Tree` on `System.out`.
6. `static String mkBlob(String fileName)`
   Factory method. Make a new `Blob` with a designated file. Cache it and queue it for writing to filesystem.
   Special case: adding a file that not exists in the `CWD` means adding it for removal.
7. `static String currFileID(String fileName)`
   Return the `ID` of a designated file's `Blob` without cache or saving a `Blob`.

### Remote

Represent a remote Gitlet repository and accommodating remote commands related methods.

#### Fields

1. Non-static methods
   1. `File _remoteWD` Stores the working directory of this remote repository as a `File` object.
   2. `private Remote(File remoteGitlet)` Construct a remote repository representation.
   3. `private void remoteRunner()`
      Change the `CWD` in the Gitlet running environment to the remote repository's working directory.
      Must call before commanding the remote repository.
   4. `private void localRunner()`
      Change the `CWD` in Gitlet running environment to the local repository's working directory.
   5. Methods that simply calling `remoteRunner()` and `localRunner()` before and after 
      calling the method with the same signature from other classes.
      1. `private String getHEAD()`
      2. `private Commit getLatestCommit()`
      3. `private String getBranch(String branchName)`
      4. `private Commit getCommit(String id)`
      5. `private String cacheAndQueueForWriteHashObject(HashObject object)`
      6. `private void writeBack()`
      7. `private Set<String> commitAncestors(Commit commit)`
      8. `private void moveHEAD(String branchName)`
      9. `private void moveCurrBranch(String commitID)`
      10. `private void checkoutToCommit(String commitID)`
      11. `private boolean existBranch(String branchName)`
      12. `private void mkNewBranch(String branchName)`
      13. `private void mkNewStage()`
      14. `private Tree getCommitTree(Commit commit)`
      15. `private Blob getBlob(String blobID)` 
      16. `private void recordCommitID(String commitID)`
2. Static methods
   1. `add-remote` command
      1. `public static void addRemote(String remoteName, String path)`
         Execute the add-remote command by creating a reference to the remote repository.
      2. `private static void writeRemote(File remoteFile, String path)`
         Write a remote repository reference.
      3. `static File readRemote(String remoteName)`
         Get the `File` referencing the remote (in the local repository).
   2. `rm-remote` command
      1. `static File getRemoteGitlet(String remoteName)`
         Get the `File` of the remote `.gitlet` directory.
   3. `push` command
      1. `public static void push(String remoteName, String remoteBranchName)`
         Executing the `push` command.
      2. `private static void pushReset(Remote remote, String commitID, String remoteBranchName)`
         Fast-forward the remote repository.
      3. `private static Set<String> commitsToPush(Commit localC, Commit remoteC, Remote remote)`
         Return a `Set` of `String` containing the IDs of commits that should be pushed to the remote repo.
      4. `private static void pushCommits(Set<String> commitIDs, Remote remote)`
         Push all `Commit` with the designated ID in the `Set`, 
         and its associating `Tree` and `Blob` to the remote repository.
      5. `private static void pushCommit(Commit commit, Remote remote)`
         Push a single `Commit` and its associating `Tree` and `Blob` to the remote repository.
   4. `fetch` command
      1. `public static void fetch(String remoteName, String remoteBranchName)`
         Execute the `fetch` command. Implementation details in the Algorithms section.
      2. `private static Set<String> commitsToFetch(Commit localC, Commit remoteC, Remote remote)`
         Return a `Set` of String containing the IDs of commits that should be fetched from the remote repo.
      3. `private static void fetchCommits(Remote remote, Set<String> commitIDs)`
         Fetch commits that their IDs in the `Set` to the local repo.
      4. `private static void fetchCommit(Remote remote, String commitID)`
         Fetch a commit to the local repo.
   5. `pull` command
      1. `public static void pull(String remoteName, String remoteBranchName)`
         Execute the `pull` command. Implementation details in the Algorithms section.

### GitletTest

This class contains JUnit tests and some helper methods for Gitlet.

#### Fields

1. `init` command
   1. `public void initCommandSanityTest()` Sanity test for init command.
2. `add` command
   1. `public void addCommandSanityTest()` Sanity test for add command.
   2. `public void addCommandTwiceTest()` Test using add command twice.
3. `commit` command
   1. `public void commitSanityTest()` Sanity test for commit command.
   2. `public void dummyCommitTest()` Dummy commit test (commit without adding anything).
   3. `public void commitAndAddTest()` Add a file, make a commit, and add another file.
   4. `public void addAndRestoreTest()` Make a commit, change the file and add, then change back and add.
      The staging area should be empty.
4. `rm` command
   1. `public void rmUnstageTest()` The rm command should unstage the added file.
   2. `public void rmCommitTest()`
      Add a file, commit, and rm it, commit again.
      The latest commit should have an empty commit tree.
      The file in the `CWD` should be deleted.
5. `log` command
   1. `public void logSanityTest()` Sanity test for log command. Init and log.
   2. `public void simpleLogTest()` Simple test for log command. Init, commit, and log.
   3. `public void normalLogTest()` Normal test for log command. Init, commit, commit, and log.
6. `global-log` command
   1. `public void globalLogSanityTest()` Sanity test for global-log command.
   2. `public void globalLogBranchTest()` Test for global-log command with branching. Need implementation.
7. `find` command
   1. `public void findSanityTest()` Sanity test for find command.
   2. `public void findBranchTest()` Test for find command with branching. Need implementation.
8. `status` command
   1. `public void statusBasicTest()` Basic test for status command.
   2. `public void statusModification3Test()`
      Test extra functions ("Modification Not Staged For Commit") condition 3 of status command.
   3. `public void statusModification4Test()`
      Test extra functions ("Modification Not Staged For Commit") condition 4 of status command.
   4. `public void statusUntrackedTest()` Test extra functions ("Untracked Files") of status command.
9. `checkout` command
   1. `public void checkoutHeadFileSanityTest()`
      Sanity test for checkout usage 1 (checkout a file to the latest commit).
   2. `public void checkoutCommitFileSanityTest()`
      Sanity test for checkout usage 2 (checkout a file to the given commit).
   3. `public void checkoutBranchSanityTest()`
      Sanity test for checkout usage 3 (checkout to a branch).
10. `branch` command
    1. `public void branchSanityTest()` Sanity test for branch command.
11. `rm-branch` command
    1. `public void rmBranchSanityTest()` Sanity test for rm-branch command.
12. `reset` command
    1. `public void resetSanityTest()` Sanity test for reset command.
13. `merge` command
    1. `public void lcaTest()` Test the lca method.
    2. `public void mergeSanityTest()` A sanity test for the merge command.
    3. `public void mergeConflictTest()` Test merging two branches with conflict.
    4. `public void mergeTest()` A hard (and comprehensive) test for the merge command.
14. `add-remote` command
    1. `public void addRemoteTest()`A sanity test for `add-remote`.
15. `push` command
    1. `public void pushTest()` A sanity test for add-remote command.
16. `fetch` command
    1. `public void fetchTest()` A sanity test for fetch command.
17. `pull` command
    1. `public void pullTest()` A sanity test for pull command.
18. Auto grader debug tests
    1. `public void test20_status_after_commit()`
    2. `public void test24_global_log_prev()`
    3. `public void test29_bad_checkouts_err()`
    4. `public void test35_merge_rm_conflicts()`
    5. `public void test36a_merge_parent2()`
19. misc
    1. `static final File CWD` The local repository's working directory.
    2. `private static void GitletExecute(String... command)`
       Execute commands with Gitlet and clean the cache after execution.
       Special case: make sure there is no `.gitlet` directory before the init command. Implemented for testing purposes.
    3. `private static void writeTestFile(String fileName, String content)`
       Write content into a designated file name. Overwriting or creating file as needed.
    4. `private static void deleteTestFile(String fileName)` Delete the file with the designated name.
    5. `private static String readTestFile(String fileName)` Read the designated file as String and return it.
    6. `private static void deleteDirectory(File directoryToBeDeleted)` Delete a directory recursively.
    7. `private static void writeAndAdd(String fileName, String content)`
       Write a test file with the designated file name and content, then add it to the stage.
    8. `private static void assertFile(String fileName, String content)`
       Assert a designated file has the designated content.

## Algorithms

### Lazy Loading and Caching

**Lazy Loading:** Only retrieve information from your file system when you need it, not all at once in the beginning.

**Caching:** Once you load something from your file system, save it in your Java program, so you don???t need to load it
again. (E.g. as an attribute or an entry in a Map.)

**Writing back:** If you cached something and then modified it, make sure at the end of your Java program, you write
the changes to your file system.

[Reference: Gitlet Persistence](https://paper.dropbox.com/doc/Gitlet-Persistence-zEnTGJhtUMtGr8ILYhoab)

In this implementation of Gitlet, I used a standalone java Class `Cache.java` to accommodate code for lazy-loading and
caching.
During lazy loading, the `load****()` method for specific Class is invoked to retrieve an instance of that Class by
specifying the object's `ID`. Additionally, `Commit`, `Tree`, and `Blob` don't have standalone `load****()` methods
because they are subclasses of `HashObject`.

After loading, the cached object is saved into the corresponding static variable.
Namely, a `TreeMap` `cachedHashObjects` will store `ID` to `HashObject` pairs,
another `TreeMap` `cachedBranches` will store `branchName` to `commitID` pairs,
a `String` `cachedHEAD` will store the content of `.gitlet/HEAD` (the current branch's name),
and a `String` `cachedStageID` will store the content of `.gitlet/STAGE` (the ID of the staging area `Tree`).

Additionally, there is `List` `queuedForWriteHashObjects` and `queuedForDeleteHashObject` that hold IDs that should be
(re)write to or delete from the filesystem. These `List`s are updated along the course of execution by
`cacheAndQueueForWriteHashObject(HashObject object)` and `queueForDeleteHashObject(String id)`.
Note that a `HashObject` will never be modified after its creation.
Therefore, no modification of existing `HashObject`s will be carried out
thus there is no such `queuedForModifyHashObjects` data structure.

At the very end of execution, caches will be written back to filesystem.
Entries in `cachedHashObjects`, will be written to or delete from filesystem based on the IDs contained in
`queuedForWriteHashObjects` and `queuedForDeleteHashObject`.
Additionally, `cachedBranches`, `cachedHEAD`, `cachedStageID` will be rewritten anyway since the size of related
persistence are trivial for the most time.

### Get the ID of a `HashObject`

Every `HashObject` need to be serialized and saved in filesystem, thus a unique file name (ID) is indispensable.
We use SHA-1 (Secure Hashcode Algorithm 1) hashcode as a content-addressable ID of every `HashOject`.
In order to achieve content-addressability, the following two characteristics is necessary:

1. Different `HashObject`s with identical contents should have the same ID.
2. A `HashObject`'s ID should change after it is modified in terms of its contents.

To accomplish such requirements, ID of a `HashObject` is generated from applying SHA-1 on its string representation.
And subclasses of the `HashObject` class overrides the default `toString()` method to make it content-addressable.

### Saving, loading, or deleting a `HashObject`

If the static variable `OPTIMIZATION` in `HashObject` class is set to `true`,
Gitlet will construct a `HashTable`-liked structure in the `.gitlet/objects` directory.
That is, all `HashObject` (with a 40-character ID) will be stored under the `.gitlet/objects/xx` directory
and named after `xxx`,
where `xx` is the leading two characters of its ID and `xxx` is the left `38` characters.
The point of this optimization is speeding up retrieving `Commit`
when the user abbreviate commit ID with a unique prefix.
The real Git is also utilizing this technique.
When the user provide an abbreviated commit ID, Gitlet will go to the corresponding `.gitlet/objects/xx` directory
and iterate through a list of file names in that directory in order to figure out the comprehensive commit ID.

On the other hand, if `OPTIMIZATION` is set to `false`,
all `HashObject` will be stored flatly under the `.gitlet/objects` directory and named after the corresponding ID.
This set up is might be more convenient when digging into the object database for debugging purposes.
Due to performance concerns, referring commits with abbreviated IDs is not allowed when `OPTIMIZATION` is set to `false`.

### Initialize the repository

1. Set up the repository
2. Create an initial commit

#### Set up the repository

1. Set up persistence directories
2. Make the default branch "master" which is pointing null for now (no pun intended)
3. Make the HEAD pointing to the master branch
4. Make a new staging area
5. Initialize and serialize a `Tree` to `.gitlet/allCommitsID`

### Make a `Commit`

1. Get the ID of the latest commit
2. Make a commit `Tree` by copying from the latest commit, and update it with the staging area
3. Construct a new `Commit` object with the `private` constructor
4. Cache the new Commit and queue it for write back
5. Move the current branch pointing the new commit
6. Make a new staging area
7. Record the new commit's ID

#### Make a commit `Tree`

A commit `Tree` is a `Tree` that every commit uses to record the associated file names and file versions (`Blob`).

1. Get a copy of the `Tree` of the latest commit
2. Get the staging area `Tree`
3. Update that copy with the staging area
   (Special case: remove the corresponding pair from that copy if the value to a key in the staging area is `null`,
   i.e., staged for removal)
4. Cache it and queue it for writing

### Stage a file for addition

1. Get the file as its current version, cache it as a Blob (don't queue for write back yet)
2. Get the version of the designated file from the latest commit
3. Special case:
   If the current version of the file is identical to the version in the latest commit (by comparing IDs),
   do not stage it, and remove it from the staging area if it is already there. End the execution.
4. Modify cached staging area

### Stage a file for removal

1. Abort if the file is neither staged nor tracked by the head commit.
2. If the file is currently staged for addition, unstage it.
3. If the file is tracked in the current commit, stage it for removal and remove it from the `CWD`.

When it comes to the design decision of representing "staged for removal",
the chosen solution is to treat pairs in the staging tree with `""` (an empty `String`) value as staged for removal.
That is, when a file is staged for removal:

1. It is deleted from the `CWD` if the user haven't done that.
2. It is "added" to the staging area.
   Given the fact that there is no such file in the `CWD`,
   a {`fileName` - `""`} pair will be written into the staging area.
3. When making a commit `Tree`,
   staged for removal file will be handled and the new commit `Tree` will not include the staged-for-removal files.

In this manner, problems with naive approaches (such as introduce a "staging area for removal" `Tree`)
is avoided, and the amount of codes to implement the `rm` command is trivial.

### Print log

1. Get the ID of the latest commit
2. Print log information starting from that commit to the initial commit recursively
   1. Get the Commit object with the given CommitID
   2. Print its log information
   3. Recursively print its ascendants' log information

### Print global log

1. Get the allCommitsID `Tree` which holds all commits' IDs.
2. Print log information for each of the IDs.

### The `find` command

This command has similar algorithm with the `global-log` command.
Both of these commands cover all commits ever made by the same manner.

1. Get the allCommitsID `Tree` which holds all commits' IDs.
2. Check each commit whether it has the designated commit message.

### Print repository status

The status information is consist of the following five parts.

1. "Branches"
   1. Get a list of all branches by reading the filenames in the `.gitlet/branches` directory.
   2. Sort the list in lexicographical order.
   3. Print the header and all branches, print an asterisk before printing the current branch.
2. "Staged Files" and "Removed Files"
   1. Get the current staging area, and get a lexicographical sorted list of filenames it currently holds.
   2. If a `filename` has an empty corresponding `BlobID` in the staging area, print it under "Removed Files";
      if a filename has a valid corresponding `BlobID` in the staging area, print it under "Staged Files".
3. "Modifications Not Staged For Commit"
   1. Get a Set of all file names that should be checked (file names in the `CWD`, the staging area, and the head commit).
   2. Check each file name and fill a List for "modified but not staged files". 
      Conditions are described below.
      1. Record the file name concatenates ` (modified)` if it satisfies condition 1 or 2.
      2. Record the file name concatenates ` (deleted)` if it satisfies condition 3 or 4.
         A file name is either marked as modified or marked as deleted or not marked.
   3. Print the List.
4. "Untracked Files"
   1. Get a list of all untracked files. A file is untracked if it is neither staged for addition nor tracked by the head commit.
   2. Print the file names.

Conditions for "Modifications Not Staged For Commit":
1. Tracked in the current commit, changed in the working directory, but not staged.
2. Staged for addition, but with different contents than in the working directory.
3. Staged for addition, but deleted in the working directory.
4. Not staged for removal, but tracked in the current commit and deleted from the working directory.

#### Get a list of untracked files

1. Get the information of files in the `CWD` as a `Tree` object.
2. Get the head `Commit` object.
3. Iterate through the file names in the `CWD`, add it to a list of untracked files if it:
   1. is not staged for addition (not contained in the staging area or its corresponding `Blob` ID is empty)
   2. is not tracked by the head commit
4. Sort the untracked files list in lexicographical order

### The `checkout` command

#### Checkout a file to `HEAD` commit

1. Get the ID of the latest commit
2. Invoke `checkout2(String commitID, String fileName)` method with the ID of the latest commit.

#### Checkout a file to the commit with the designated ID

1. Get the `Commit` object with the designated commit ID
2. Get the designated file's `Blob` object form that commit
3. Overwrite the file with that name in the `CWD`

#### Checkout to a designated branch

1. Perform checks: Gitlet will abort if no branch with the given name exists, or that branch is the current branch,
   or a working file is untracked.
2. Move the `HEAD` to that branch.
3. Checkout to the commit that the branch is pointing to.

##### Checkout to a designated commit

1. Delete all files in the `CWD`.
2. Checkout all files tracked by that commit.
3. Clean the staging area.

### Create a new branch

Creating new branches is carried out when `branch` or `init` command is given.
When creating new branches, the operation under the hood is no more than writing a `branchName` - `CommitID` pair
into the `cachedBranches` which is then written back to the filesystem upon exit.
The `CommitID` assigned to the new branch is always the latest commit (head commit) if there is one.
For the default "master" branch which is created right before the initial commit,
its corresponding is `null` at the very first (but pointed to the initial commit after the initial commit is created).

### Remove a branch

This command delete the branch with the given name. It does not delete any commits under that branch.

1. Abort if the designated branch does not exist
2. Abort if the designated branch is the current branch
3. Wipe the branch's pointer in the cache and delete the branch file upon exit

### Reset to a designated commit

1. Perform the checks: the commit with the designated ID exists, and there is no working untracked file.
2. Checkout to the designated commit.
3. Move the current branch to that commit (The biggest difference between `reset` and `checkout [branch name]` command). 

### Merge files from the given branch into the current branch

The `merge` command is one of the most complicated commands in Gitlet. 
Therefore, the execution of this command is divided to multiple helper methods.
Generally, the following procedure is followed to execute this command.

1. Get the latest `Commit` object of the current branch, the given branch, and the common ancestors (split commit).
2. Perform checks.
3. Calculate which files will be changed in what manners, and perform checks.
4. Modify the `CWD` following the result from step 2, staging for addition or removal as we go.
5. Make a merge commit.

#### Get the latest common ancestor (split commit) of two commits

1. Get a `Set` of all ancestors' ID of a commit. 
   This is accomplished by recursively collect all parent commit(s)' ID(s) and their parent(s)' ID(s), 
   like breadth-first-search.
   Note that a merge commit has two parents, both will be collected as its ancestor.
2. Starting from the other commit, breadth-first-search the first commit that its ID is in the `Set`.

#### Determine which files will be changed in what manners

1. Get a Set of file names that should be checked 
   by combining sets of file names tracked by the split, current, and the other branch's head commits.
2. Construct a Map of String to Set of String `Map<String, Set<String>>`, 
   where `"other"` is mapped to a Set of files' file names that should use the version in the other (given) branch's head commit, 
   and `"conflict"` is mapped to a Set of files' file names that conflict after merging.
3. For file names in the Set need to be checked,
   1. Add to `"other"`'s value Set 
      if the version of such file in the split commit is the same of it in the current commit 
      (has the same content or both not exists).
   2. Add to `"conflictt"`'s value Set
      if the version in the split commit, the current commit, 
      and the other branch's head commit is all different from each other.
   
#### Perform checks for the merge command

* Abort merging if a branch with the given name does not exist.
* Abort merging if there are staged additions or removals present.
* Abort merging if attempting to merge a branch with itself.
* Exit if the split point is the same commit as the given branch's head commit. The merge is complete.
* Fast-forward and exit if the split point is the same commit as the current branch.
* Abort merging if an untracked file in the current commit would be overwritten or deleted by the merge.
  This is checked after which files will be changed is determined.
* Abort merging if there are unstaged changes to file that would be changed by the merge.
  This is checked after which files will be changed is determined.

#### Modify the `CWD` for merging, make merge Commit

The Map recoding necessary modification is disassembled into two `Set`s 
(one holding file names that should be changed to their version in the given branch's head commit, 
and one holding file names that resulting in conflicts).
Two helper methods are utilized to carry out the modifications. 
The changes will be staged immediately.

Making a merge `Commit` is not so different from making a normal commit,
despite the new commit will have two parent commit IDs, 
the first is the current commit ID and the second is the ID of the given branch's head commit.
Lastly, Gitlet will print a message to the console if any conflict is made.


### Remote commands

Commands related with remote repository requires reuse of existing code for the local repository.
To fulfill this demand, static variables (such as `CWD`, or `GITLET_DIR`)
in the `Repository` class are no longer final and will be assigned each time.
That is, when executing commands on the local repository, static variables will be assigned normally;
while executing commands on the remote repository, these static variables will be assigned according to the remote
working directory, thus reuse the existing code to manipulating the remote repository.


#### add and remove a remote

This command only involved manipulation to the local repository (creating path reference to the remote repository).

#### `push` command

1. Get the `Commit` object of the local repository's head commit 
   and the front commit of the remote repository's given branch. 
   Create a new branch at the remote repository if such branch does not exist.
2. Calculate the commits need to be pushed by contracting the ancestors of the two commits.
3. Push the `Commit`s (and their associating `Tree` and `Blob`) to the remote repository.
   Specifically, using the caching and writing back mechanisms developed for the local repository.
   Commit's IDs are added to the remote `allCommitsID` file upon pushing.
4. Reset the remote repository (change it to the given branch and fast-forward that branch).

#### `fetch` command

1. Get the `Commit` object of the local repository's fetched branch's head commit
   and the head commit of the remote repository's designated branch.
2. Calculate the commits need to be fetched by contracting the ancestors of the two commits.
3. Fetch the `Commit`s (and their associating `Tree` and `Blob`) to the local repository.
   Specifically, using the caching and writing back mechanisms of the local repository.
   Commit's IDs are added to the local `allCommitsID` file upon fetching.

#### `pull` command

This command is executed simply fetch the designated remote branch using the `fetch` command,
and then merge the fetched branch into the current branch using the `merge` command.

## Persistence

The directory structure looks like this:

```
CWD                                                      <==== Whatever the current working directory is
????????? .gitlet                                              <==== All persistant data is stored within here
    ????????? HEAD                                             <==== The name of the current branch
    ????????? STAGE                                            <==== A hash pointer to the serialized staging area Tree
    ????????? allCommitsID                                     <==== A serialized Tree that contains all commits' IDs
    ????????? objects                                          <==== The object database (all HashObject lives here)
    ???   ????????? d9                                           <==== Saves all HashObject with ID stating with "d9"
    ???   ???   ????????? 91f6cad12cc1bfb64791e893fa01ac5bf8358e   <==== A saved HashObject, named after its ID without the first two letters
    ???   ???   ????????? ...                                    
    ???   ????????? ...                                        
    ????????? branches                                         <==== Store all the branch references
    ???   ????????? R1                                           <==== Directory of branches fetched from a remote repository
    ???   ???   ????????? master                                   <==== A branch fetched from a remote, will be displayed as "R1/master"
    ???   ???   ????????? ...
    ???   ????????? master                                       <==== The default branch. Contains a hash pointer to a commit
    ???   ????????? ...
    ????????? remotes                                          <==== Store all the remote references
        ????????? R1                                           <==== A remote references in String, e.g. [remote directory]/.gitlet
        ????????? ...
```

### Commands

#### `init` command

The `Repository.setUpPersistence()` method will set up all persistence. It will:

1. Abort if there is already a Gitlet version-control system in the current directory
2. Create `.gitlet/branches` and `.gitlet/objects` folders
3. Create `.gitlet/HEAD`, `.gitlet/STAGE`, and `.gitlet/allCommitsID`

After setting up all persistence, the `init` command will do its jobs.
Finally, all changes that should be persistent (including branching, HEAD, new commit, and Tree for that commit)
will be written to filesystem automatically by the `Cache.writeBack` method invoked in `Main.main(String[] args)`.

#### `add` command

This command will modify persistence in the following two cases:

1. If the current version of the added file is identical to the version in the latest commit,
   that file will be removed from the staging area if it is already there.
   (as can happen when a file is changed, added, and then changed back to its original version)
2. If case 1 isn't happening and the added file is different from the version that is already in the staging area
   (if it exists),
   a new staging area containing the added file is saved to filesystem.

#### `commit` command

The `commit` command will modify persistence following the following rules (no pun intended):

1. Save a serialized `Commit` object in the object database
2. Overwrite the current branch's file, make it contains the new commit's ID
3. Make a new staging area and overwrite the `STAGE` file
4. Record the new commits' ID to `.gitlet/allCommitsID`
5. ~~Delete the previous staging area if it is not empty, and there is a commit already _(subtle bug may exist)_~~

#### `rm` command

The `rm` command will change the current staging area `Tree`
if the designated file is added (removing from the staging area)
or exists in the head commit (staging for removal).

#### `checkout` command

This command will write the current working directory, but only read persistence.
An exception is that when checking out to a branch, the staging area will be cleared.

#### `branch` command

When a branch is created, a `branchName` - `CommitID` pair will be written into the `cachedBranches` data structure.
Upon exit, the `cachedBranches` will be written back to the filesystem, i.e. the persistence will be modified
according to cached information.

#### `rm-branch` command

When a branch is removed, the corresponding file under the `.gitlet/branches` directory will be deleted.

#### `reset` command

This command will write the current working directory and clear the staging area.

#### `merge` command

If the merging is carried out successfully, this command will change the persistence just like the `commit` command.

#### `add-remote` and `rm-remote` command

These two commands will create/delete files in `.gitlet/remotes/` directory.

#### `push`, `fetch`, and `pull` command

These two commands will add serialized `HashObject` to the object database, 
as well as the branch files, the `.gitlet/HEAD` file, and the `.gitlet/allCommitsID` file.
