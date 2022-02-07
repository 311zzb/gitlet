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
   1. `static Map<String, HashObject> cachedHashObjects` A `Map` that stores cached ID and `HashObject` pairs.
   2. `private static HashObject getHashObject(String id)` 
      Lazy loading and caching of HashObjects. 
      Being `private` because a `HashObject` will never be requested as `HashObject` 
      (as `Commit` or `Tree` or `Blob` instead).
   3. `static Commit getCommit(String id)` 
      A method that lazy-load a `Commit` with `id` utilizing `getHashObject(String id)`.
   4. `static Tree getTree(String id)`
      A method that lazy-load a `Tree` with `id` utilizing `getHashObject(String id)`.
   5. `static Commit getLatestCommit()` Get the `Commit` object of the latest commit utilizing `getCommit(String id)`.
   6. `static Tree getStage()` Get the `Tree` object representing the staging area utilizing `getTree(String id)`.
   7. `static List<String> queuedForWriteHashObjects` New HashObjects' IDs that are queued for writing to filesystem.
   8. `static String cacheAndQueueForWriteHashObject(HashObject object)`
      Manually cache a `HashObject` by put a `HashObject` into the cache, 
      and queue it for writing to filesystem. Return its ID.
   9. `static void writeBackAllQueuedHashObject()` 
      Write back all queued-for-writing `HashObjects` to filesystem. Invoked upon exit.
   10. `static List<String> queuedForDeleteHashObject`
       Deprecated `HashObject`s' IDs that are queued for deletion from filesystem.
   11. `static void queueForDeleteHashObject(String id)` Given Qa `HashObject`'s ID, queue it for deletion.
   12. `static void deleteAllQueuedHashObject()` Delete all queued-for-deletion `HashObject`s. Invoked upon exit.
2. Caching `Branch`
   1. `static Map<String, String> cachedBranches` A `Map` that stores cached branch name and commit ID pairs.
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
4. Caching `STAGE`
   1. `static String cachedStageID` A `String` that stores cached `STAGE`, the ID of the current staging area.
   2. `static String getStageID()` Lazy loading and caching of `STAGE`.
   3. `static void cacheStageID(String newStageID)` 
      Manually cache the `STAGE` by assigning the `cachedStageID` to a given `stageID`.
   4. `static void writeBackSTAGE()` Write back STAGE file. Invoked upon exit.
5. MISC
   1. `static void writeBack()` Write back all caches. Invoked upon exit.

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
      This method also checks if there is an existing `.gitlet` directory and abort the execution if so.
3. `add` command
   1. 


### Branch
This class houses static methods that related to branch and HEAD. 
It contains methods for loading and writing branch files and the HEAD file.
This class will never be instantiated since there are only static methods.

#### Fields
1. `static String loadBranch(String branchName)`
   Load a branch file from filesystem with designated name. Return null if the branch name is "" (nothing).
   Invoked by the Cache class.
2. `static void writeBranch(String branchName)`
   Get a branch's information from cache and write it back to filesystem. Invoked by the Cache class.
3. `static void mkNewBranch(String branchName, String commitID)` 
   Make a new branch with designated name at the latest commit by caching it manually.
4. `static void moveCurrBranch(String commitID)` Make the current branch pointing to a designated commit.
5. `static String loadHEAD()`
   Load the `HEAD` file and return the current branch's name. Invoked by the Cache class.
6. `static void writeHEAD()`
   Get the `HEAD` from cache and write it back to filesystem. Invoked by the Cache class.
7. `static void moveHEAD(String branchName)` Make the `HEAD` pointing to a designated branch.


### Stage
This class houses static methods that related to Stage (the staging area).
It contains methods for loading and writing the `STAGE` file, as well as making a new staging area.
This class will never be instantiated since there are only static methods.

#### Fields
1. `static String loadSTAGEID()` Return the ID of the current staging area (a `Tree` object). Invoked by the Cache class.
2. `static void writeSTAGE()` Write the stage ID in cache back to filesystem. Invoked by the Cache class.
3. `static void mkNewStage()` Make a new stage (a `Tree` object) and cache its ID.


### HashObject 
This class represents a `HashObject` that will be serialized within `.gitlet/objects`, named after its SHA-1.
`HashObject` is an implementation of `Serializable` and `Dumpable`.
This file has helper methods that will return the SHA-1 (ID) of a `HashObject`.
As well as static methods that returning the `HashObject` object corresponding to its `ID` (SHA-1), 
and write to or delete from the object database a `HashObject`.

#### Fields

1. `String id()` Get the SHA-1 of `THIS`.
2. `public void dump()` Print the type of this object on System.out.
3. `static HashObject loadHashObject(String id)` Load a type object with its ID.
4. `static void writeCachedHashObject(String id)` Write a cached HashObject with ID in cachedObjects to filesystem.
5. `static void deleteHashObject(String id)` Delete a HashObject from filesystem.

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
7. `public void dump()` Print information of this `Commit` on `System.out`.
8. `public String getCommitTreeRef()` Get the associating `Tree` of this commit.
9. `static void mkCommit(String message)` A packaged constructor for `Commit`. 
   Implementation details in the Algorithm section.


### Tree
Represent a Gitlet `Tree`, corresponding to UNIX directory entries.
Implements `Iterable<String>`.
An instance of `Tree` object contains a `TreeMap` as instance variable, which has zero or more entries.
Each of these entries is a `fileName` - `BlobID` pair.
This class also contains `Tree` related static methods.

#### Fields
1. `private final Map<String, String> _structure` The `TreeMap` that stores `fileName` - `blobID` pairs.
2. `private Tree() {_structure = new TreeMap<>()` The constructor of `Tree` class. 
   This method is `private` because no "naked" instantiation of `Tree` should be allowed outside the `Tree` class.
3. `public void dump()` Print information of this `Tree` on `System.out`.
4. `boolean isEmpty()` Return whether this `Tree` is empty.
5. `void record(String fileName, String blobRef)` Record a `fileName` - `blobID` pairs.
6. `String retrieve(String fileName)` Return the `blobID` corresponding to a given `fileName`.
7. `public Iterator<String> iterator()` Returns an `Iterator` of this `Tree`, namely the `keySet()` of its `TreeMap`.
8. `void updateWith(Tree updater)` Update this `Tree` with the entries in the given `Tree`.
9. `static String mkNewEmptyTree()` A packaged constructor for `Tree`. 
   Creates an empty `Tree`, cache it and return its ID.
10. `static Tree getLatestCommitTree()` Return the associated `Tree` of the latest commit if exists.
    Return `null` if there is no latest commit.
11. `static String mkCommitTree()` Return the `Tree` that snapshots the current add and remove status. 
    Implementation details in the Algorithm section.


### Blob

#### Fields

1. xxx


### GitletTest

#### Fields

1. xxx



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

### init command
1. Set up persistence directories
2. Make the default branch "master" which is pointing null for now (no pun intended)
3. Make the `HEAD` pointing to the master branch
4. Make a new staging area
5. Create an initial commit (branch master will be moved in this method)

### Make a `Commit`
1. Get the ID of the latest commit
2. Make a commit `Tree` by copying from the latest commit, and update it with the staging area
3. Construct a new `Commit` object with the `private` constructor
4. Cache the new Commit and queue it for write back
5. Move the current branch pointing the new commit

### Make a commit `Tree`
A commit `Tree` is a `Tree` that every commit uses to record the associated file names and file versions (Blobs).
1. Copy the `Tree` from the latest commit
2. Overwrite it with the current staging area
3. Cache it and queue it for write back
4. Return the ID of overwritten `Tree`


## Persistence
The directory structure looks like this:
```
CWD                                                      <==== Whatever the current working directory is
└── .gitlet                                              <==== All persistant data is stored within here
    ├── HEAD                                             <==== The name of the current branch
    ├── STAGE                                            <==== A hash pointer to the serialized staging area Tree
    ├── objects                                          <==== The object database (all HashObject lives here)
    │   ├── d991f6cad12cc1bfb64791e893fa01ac5bf8358e     <==== A saved HashObject
    │   └── ...                                         
    └── branches                                         <==== Store all the branch references
        ├── master                                       <==== The default branch. Contains a hash pointer to a commit
        └── ...
```
### Command
#### init command
The `Repository.setUpPersistence()` method will set up all persistence. It will:
1. Abort if there is already a Gitlet version-control system in the current directory
2. Create `.gitlet/branches` and `.gitlet/objects` folders
3. Create `.gitlet/HEAD` and `.gitlet/STAGE` files

After setting up all persistence, the `init` command will do its jobs.
Finally, all changes that should be persistent (including branching, HEAD, new commit, and Tree for that commit) 
will be written to filesystem automatically by the `Cache.writeBack` method invoked in `Main.main(String[] args)`.

#### add command
