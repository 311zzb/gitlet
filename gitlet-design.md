# Gitlet Design Document

**Name**: XIE Changyuan

## Classes and Data Structures

### Main

Driver class for Gitlet, a subset of the Git version-control system.
Account for validating the number of arguments and invoking package-private methods according to received commands.
The cache write back method `Cache.writeBack()` which enabling the persistence of Gitlet is also invoked in this class.

#### Fields

This class contains only `static` methods since `Main` should not be instantiated.

1. `public static void main(String[] args)` The main method of Gitlet.
2. `private static void assertArgsNum(String cmd, String[] args, int n)`
   Throw a GitletException if args _don't have_ exactly n elements.
3. `private static void assertNotArgsNum(String cmd, String[] args, int n)`
   Throw a GitletException if args _have_ exactly n elements.
4. `private static String[] getOperands(String[] args)`
   Strip the first element of the input array and return the rest.

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
   2. `private static HashObject getHashObject(String id)`
      Lazy loading and caching of HashObjects.
      Being `private` because a `HashObject` will never be requested as `HashObject`
      (as `Commit` or `Tree` or `Blob` instead).
   3. `static Commit getCommit(String id)`
      A method that lazy-load a `Commit` with `id` utilizing `getHashObject(String id)`.
   4. `static Tree getTree(String id)`
      A method that lazy-load a `Tree` with `id` utilizing `getHashObject(String id)`.
   5. `static Blob getBlob(String id)`
      A method that lazy-load a `Blob` with `id` utilizing `getHashObject(String id)`.
   6. `static Commit getLatestCommit()` Get the `Commit` object of the latest commit utilizing `getCommit(String id)`.
   7. `static final Set<String> queuedForWriteHashObjects`
      New HashObjects' IDs that are queued for writing to filesystem.
   8. `static String cacheAndQueueForWriteHashObject(HashObject object)`
      Manually cache a `HashObject` by put a `HashObject` into the cache,
      and queue it for writing to filesystem. Return its ID.
   9. `static void writeBackAllQueuedHashObject()`
      Write back all queued-for-writing `HashObjects` to filesystem. Invoked upon exit.
   10. `static final Set<String> queuedForDeleteHashObject`
       Deprecated `HashObject`s' IDs that are queued for deletion from filesystem.
   11. `static void queueForDeleteHashObject(String id)` Given Qa `HashObject`'s ID, queue it for deletion.
   12. `static void deleteAllQueuedHashObject()` Delete all queued-for-deletion `HashObject`s. Invoked upon exit.
2. Caching `Branch`
   1. `static final Map<String, String> cachedBranches` A `Map` that stores cached branch name and commit ID pairs.
   2. `static String getBranch(String branchName)` Lazy loading and caching of branches.
   3. `static String getLatestCommitRef()`
      A method that lazy-load the ID of the latest commit by `getBranch(getHEAD())`.
   4. `static void cacheBranch(String branchName, String commitID)`
      Manually cache a `Branch` by putting a `branchName` - `commitID` pair into the cache.
   5. `static void writeBackAllBranches()`
      Write back (update) all branches to filesystem. Invoked upon exit.
3. Caching `HEAD`
   1. `static String cachedHEAD` A `String` that stores cached `HEAD`, the current branch's name.
   2. `static String getHEAD()` Lazy loading and caching of `HEAD`.
   3. `static void cacheHEAD(String branchName)`
      Manually cache the `HEAD` by assigning the `cachedHEAD` to a given `branchName`.
   4. `static void writeBackHEAD()` Write back (update) the `HEAD` file. Invoked upon exit.
4. Caching `STAGE` (Stage ID)
   1. `static String cachedStageID` A `String` that stores cached `STAGE`, the ID of the current staging area.
   2. `static String getStageID()`
      Lazy loading and caching of STAGE (the ID of the saved staging area).
      Notice: this DOES NOT point to the current staging area after the staging area is modified and before write back.
   3. `static void cacheStageID(String newStageID)`
      Manually cache the `STAGE` by assigning the `cachedStageID` to a given `stageID`.
   4. `static void writeBackStageID()` Write back STAGE file. Invoked upon exit.
5. Caching Stage
   1. `static Tree cachedStage` A `Tree` that stores cached staging area.
   2. `static Tree getStage()` Get the `Tree` object representing the staging area utilizing `getTree(getStageID())`.
   3. `static void cacheStage(Tree stage)`
      Queue the previous staging area for deletion and manually cache the passed-in Stage.
      Special case:
      queue the previous staging area for deletion only if
      there is a commit,
      and the previous staging area is different from the Tree of the latest commit,
      and the previous staging area is not empty.
6. MISC
   1. `static void writeBack()` Write back all caches. Invoked upon exit.
   2. `static void cleanCache()` Reset all caches. Used for testing proposes.

### Repository

A class houses static methods related to the whole repository.
This class will handle all actual Gitlet commands by invoking methods in other classes correctly.
It also sets up persistence and do additional error checking.

#### Fields

1. Static Variables
   1. `static final File CWD = new File(System.getProperty("user.dir"))`
      The Current Working Directory. A package-private static variable.
   2. `static final File GITLET_DIR = join(CWD, ".gitlet")`
      The `.gitlet` directory, where all the state of the repository will be stored. Package-private.
   3. `static final File HEAD = join(GITLET_DIR, "HEAD")`
      The `.gitlet/HEAD` file. This file stores the name of the active branch.
   4. `static final File STAGE = join(GITLET_DIR, "STAGE")`
      The `.gitlet/STAGE` file, where the ID of the current staging area is stored.
   5. `static final File OBJECTS_DIR = join(GITLET_DIR, "objects")`
      The `.gilet/objects` directory. This is the object database where all `HashObject` live.
   6. `static final File BRANCHES_DIR = join(GITLET_DIR, "branches")`
      The `.gitlet/branches` directory. Each branch is stored as a file under this directory.
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
   1. `private static final Set<String> loggedCommitID` 
      A Set that record the visited commits' IDs. No need to be persistent.
   2. `public static void globalLog()` 
      Print log information about all commits ever made. Implementation details in the Algorithms section.
8. `checkout` command
   1. `public static void checkout1(String fileName)` 
      Execute checkout command usage 1 (checkout a file to the latest commit). 
      Implementation details in the Algorithms section.
   2. `public static void checkout2(String commitID, String fileName)`
      Execute checkout command usage 2 (checkout a file to the given commit).
      Implementation details in the Algorithms section.
   3. `public static void checkout3(String branchName)`
      Execute checkout command usage 3 (checkout all files to the designated branch). TODO.
9. misc
   1. `private static void assertGITLET()` Assert the `CWD` contains a `.gitlet` directory.
   2. `private static void overwriteCWDFile(String fileName, Blob overwriteSrc)`
      Overwrite the file in `CWD` of designated file name with the content in the given `Blob` object.

### Branch

This class houses static methods that related to branch and HEAD.
It contains methods for loading and writing branch files and the HEAD file.
This class will never be instantiated since there are only static methods.

#### Fields

1. `static String loadBranch(String branchName)`
   Load a branch file from filesystem with designated name. Return null if the branch name is "" (nothing).
   Invoked by the Cache class.
2. `static List<String> loadAllBranches()`
   Load all branch files from the filesystem. Return a `List` contains all commit IDs that are pointed by a branch.
3. `static void writeBranch(String branchName)`
   Get a branch's information from cache and write it back to filesystem. Invoked by the Cache class.
4. `static void mkNewBranch(String branchName, String commitID)`
   Make a new branch with designated name at the latest commit by caching it manually.
5. `static void moveCurrBranch(String commitID)` Make the current branch pointing to a designated commit.
6. `static String loadHEAD()`
   Load the `HEAD` file and return the current branch's name. Invoked by the Cache class.
7. `static void writeHEAD()`
   Get the `HEAD` from cache and write it back to filesystem. Invoked by the Cache class.
8. `static void moveHEAD(String branchName)` Make the `HEAD` pointing to a designated branch.

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
2. `private final String _parentCommitRef` The ID of the parent commit.
3. `private final String _parentCommitMergeRef` The ID of the other parent (if any). Not implemented yet.
4. `private final Date _timeStamp` A time stamp of the commit been made.
5. `private final String _treeRef` The ID of the associated `Tree` object.
6. `private Commit(String parentCommitRef, String message, String treeRef)`
   The constructor of `Commit` class. This method is `private`
   because no "naked" instantiation of `Commit` is allowed outside the `Commit` class.
   Additionally, the time stamp is set to 1970.01.01 for initial commit.
7. `public String toString()` Content-addressable overriding `toString()` method.
8. `String logString()` Return the log information of this `Commit`.
9. `public void dump()` Print information of this `Commit` on `System.out`.
10. `String getParentCommitRef()` Get the ID of the parent commit.
11. `String getCommitTreeRef()` Get the ID of the associating `Tree` of this commit.
12. `Tree getCommitTree()` Get the associating `Tree` of this commit.
13. `String getCommitTreeBlobID(String fileName)` Get the ID of the `Blob` of a designated file name in this commit.
14. `Boolean containsFile(String fileName)` Return whether this `Commit` contains a file with `fileName`.
15. `static void mkCommit(String message)` Factory method. Make a new `Commit`.
    Implementation details in the Algorithm section.

### Tree

Represent a Gitlet `Tree`, corresponding to UNIX directory entries.
Implements `Iterable<String>`, extends `HashObject`.
An instance of `Tree` object contains a `TreeMap` as instance variable, which has zero or more entries.
Each of these entries is a `fileName` - `BlobID` pair.
This class also contains `Tree` related static methods.

#### Fields

1. `private final Map<String, String> _structure` The `TreeMap` that stores `fileName` - `blobID` pairs.
2. `private Tree() {_structure = new TreeMap<>()` The constructor of `Tree` class.
   This method is `private` because no "naked" instantiation of `Tree` should be allowed outside the `Tree` class.
3. `Tree(Tree another)` A constructor that deep-copy the passed-in `Tree`.
4. `public String toString()` Content-addressable overriding `toString()` method.
5. `public void dump()` Print information of this `Tree` on `System.out`.
6. `boolean isEmpty()` Return whether this `Tree` is empty.
7. `boolean containsFile(String fileName)` Return `true` if a `Tree` contains a file with `fileName`.
8. `void putBlobID(String fileName, String blobRef)` Record a `fileName` - `blobID` pairs.
9. `void removeBlobID(String fileName)` Remove an entry with `fileName` as the key from this `Tree`.
10. `String getBlobID(String fileName)` Return the ID of a `Blob` according to a given `fileName` (if exists).
11. `Blob getBlob(String fileName)` Return a `Blob` according to a given `fileName` (if exist).
12. `public Iterator<String> iterator()` Returns an `Iterator` of this `Tree`, namely the `keySet()` of its `TreeMap`.
13. `void updateWith(Tree updater)` 
    Update this `Tree` with the entries in the given `Tree`.
    Special case: remove the corresponding pair from `this` if the value to a key in the updater is `null`.
14. `static String mkNewEmptyTree()` Factory method.
    Creates an empty `Tree`, cache it and return its ID.
15. `static Tree getLatestCommitTree()` Factory method. Return the copy of the `Tree` of the latest commit if exists.
    Special case: return `null` if there is no latest commit.
16. `static String mkCommitTree()`
    Factory method.
    Return a `Tree` that capture the `Tree` from the latest commit as well as current addition and removal status.
    Implementation details in the Algorithm section.
    Special cases: make a new empty tree if there is no `Tree` in the latest commit.
17. `private static Tree copyLatestCommitTree()` Factory method. Return a deep-copy of the `Tree` in the latest commit.

### Blob

Represent a Gitlet `Blob`, corresponding to UNIX files.
Extends `HashObject`.
`Blob` has one instance variable `_content`, which holding the content of a file.
This variable enables a `Blob` to represent a version of such file.
This class also has `Blob` related static methods.

#### Fields

1. `private final String _content` The instance variable that hold the content of a version of a file.
2. `private Blob(String content)` The private constructor of `Blob`.
   No "naked" instantiation of `Blob` is allowed.
3. `String getContent()` Unlocks the content of a `Blob`.
4. `public String toString()` Content-addressable overriding `toString()` method.
5. `public void dump()` Print information of this `Tree` on `System.out`.
6. `static String mkBlob(String fileName)`
   Factory method. Make a new `Blob` with a designated file. Cache it and queue it for writing to filesystem.
   Special case: adding a file that not exists in the `CWD` means adding it for removal.

### GitletTest

This class contains JUnit tests for Gitlet.

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
   2. `public void globalLogTest()` Test for global-log command with branching. Need implementation.
7. `checkout` command
   1. `public void checkoutHeadFileSanityTest()` 
      Sanity test for checkout usage 1 (checkout a file to the latest commit).
   2. `public void checkoutCommitFileSanityTest()` 
      Sanity test for checkout usage 2 (checkout a file to the given commit).
8. misc
   1. `private static void GitletExecute(String... command)` 
      Execute commands with Gitlet and clean the cache after execution.
      Special case: make sure there is no `.gitlet` directory before the init command. Implemented for testing purposes.
   2. `private static void writeTestFile(String fileName, String content)`
      Write content into a designated file name. Overwriting or creating file as needed.
   3. `private static String readTestFile(String fileName)` Read the designated file as String and return it.
   4. `private static void deleteDirectory(File directoryToBeDeleted)` Delete a directory recursively.


## Algorithms

### Lazy Loading and Caching

**Lazy Loading:** Only retrieve information from your file system when you need it, not all at once in the beginning.

**Caching:** Once you load something from your file system, save it in your Java program, so you don’t need to load it
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

### Set up the repository

1. Set up persistence directories
2. Make the default branch "master" which is pointing null for now (no pun intended)
3. Make the HEAD pointing to the master branch
4. Make a new staging area

### Make a `Commit`

1. Get the ID of the latest commit
2. Make a commit `Tree` by copying from the latest commit, and update it with the staging area
3. Construct a new `Commit` object with the `private` constructor
4. Cache the new Commit and queue it for write back
5. Move the current branch pointing the new commit
6. Make a new staging area

### Make a commit `Tree`

A commit `Tree` is a `Tree` that every commit uses to record the associated file names and file versions (`Blob`).

1. Get a copy of the `Tree` of the latest commit
2. Get the staging area `Tree`
3. Update that copy with the staging area 
   (Special case: remove the corresponding pair from that copy if the value to a key in the staging area is `null`,
   i.e., staged for removal)
4. Cache it and queue it for writing

### Add a file to the staging area

1. Get the file as its current version, cache it as a Blob (don't queue for write back yet)
2. Get the version of the designated file from the latest commit
3. Special case:
   If the current version of the file is identical to the version in the latest commit (by comparing IDs),
   do not stage it, and remove it from the staging area if it is already there. End the execution.
4. Modify cached staging area

### The `rm` command

1. Abort if the file is neither staged nor tracked by the head commit.
2. If the file is currently staged for addition, unstage it.
3. If the file is tracked in the current commit, stage it for removal and remove it from the `CWD`.

When it comes to the design decision of representing "staged for removal", 
the chosen solution is to treat pairs in the staging tree with `null` value as staged for removal.
That is, when a file is staged for removal:
1. It is deleted from the `CWD` if the user haven't done that.
2. It is "added" to the staging area. 
   Given the fact that there is no such file in the `CWD`, 
   a {`fileName`, `null`} pair will be written into the staging area.
3. When making a commit `Tree`, 
   staged for removal file will be handled and the new commit `Tree` will not include the staged-for-removal files.

In this manner, problems with naive approaches (such as introduce a "staging area for removal" `Tree`)
is avoided, and the amount of codes to implement the `rm` command is trivial. 

### Print commit log

1. Get the ID of the latest commit
2. Print log information starting from that commit to the initial commit recursively
   1. Get the Commit object with the given CommitID
   2. Print its log information
   3. Recursively print its ascendants' log information

### Print global-log

1. Get a list of commit IDs that are pointed by any branch
2. Print log information starting form each of the ID (ignore those commits that have been visited base on their IDs)

### Checkout a file to `HEAD` commit

1. Get the ID of the latest commit
2. Invoke `checkout2(String commitID, String fileName)` method with the ID of the latest commit.

### Checkout a file to the commit with the designated ID

1. Get the `Commit` object with the designated commit ID
2. Get the designated file's `Blob` object form that commit
3. Overwrite the file with that name in the `CWD`

### Checkout all files to a designated branch

1. xxx

### 



## Persistence

The directory structure looks like this:

```
CWD                                                      <==== Whatever the current working directory is
└── .gitlet                                              <==== All persistant data is stored within here
    ├── HEAD                                             <==== The name of the current branch
    ├── STAGE                                            <==== A hash pointer to the serialized staging area Tree
    ├── objects                                          <==== The object database (all HashObject lives here)
    │   ├── d9                                           <==== Saves all HashObject with ID stating with "d9"
    │   │   ├── 91f6cad12cc1bfb64791e893fa01ac5bf8358e   <==== A saved HashObject, named after its ID without the first two letters
    │   │   └── ...                                      
    │   └── ...                                          
    └── branches                                         <==== Store all the branch references
        ├── master                                       <==== The default branch. Contains a hash pointer to a commit
        └── ...
```

### Command

#### `init` command

The `Repository.setUpPersistence()` method will set up all persistence. It will:

1. Abort if there is already a Gitlet version-control system in the current directory
2. Create `.gitlet/branches` and `.gitlet/objects` folders
3. Create `.gitlet/HEAD` and `.gitlet/STAGE` files

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
4. Delete the previous staging area if it is not empty, and there is a commit already _(subtle bug may exist)_

#### `rm` command

The `rm` command will change the current staging area `Tree` 
if the designated file is added (removing from the staging area)
or exists in the head commit (staging for removal).

#### `checkout` command

This command will write the current working directory, but only read persistence.