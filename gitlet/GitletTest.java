package gitlet;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static gitlet.Cache.*;
import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;
import static org.junit.Assert.*;

/**
 * This class contains JUnit tests for Gitlet.
 *
 * @author XIE Changyuan
 */
public class GitletTest {

    /* INIT COMMAND */

    /** Sanity test for init command. */
    @Test
    public void initCommandSanityTest() throws IOException {
        gitletExecute("init");
    }

    /* ADD COMMAND */

    /** Sanity test for add command. */
    @Test
    public void addCommandSanityTest() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
    }

    /** Test using add command twice. */
    @Test
    public void addCommandTwiceTest() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");

        writeTestFile("_bye.txt", "bye");
        gitletExecute("add", "_bye.txt");
    }

    /* COMMIT COMMAND */

    /** Sanity test for commit command. */
    @Test
    public void commitSanityTest() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        writeTestFile("_bye.txt", "bye");
        gitletExecute("add", "_bye.txt");

        gitletExecute("commit", "added hello and bye");
    }

    /** Dummy commit test. */
    @Test
    public void dummyCommitTest() throws IOException {
        gitletExecute("init");

        gitletExecute("commit", "dummy commit");
    }

    /** Add a file, make a commit, and add another file. */
    @Test
    public void commitAndAddTest() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        gitletExecute("commit", "added hello");

        writeTestFile("_bye.txt", "bye");
        gitletExecute("add", "_bye.txt");
    }

    /**
     * Make a commit, change the file and add, then change back and add.
     * The staging area should be empty.
     */
    @Test
    public void addAndRestoreTest() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        gitletExecute("commit", "added hello");

        writeTestFile("_hello.txt", "hello world");
        gitletExecute("add", "_hello.txt");
        writeTestFile("_hello.txt", "hello");
        // Should remove _hello.txt from the staging area
        // since it is now identical with the version in the latest commit
        gitletExecute("add", "_hello.txt");
        assertTrue(getStage().isEmpty());
    }

    /* RM COMMAND */

    /**
     * The rm command should unstage the added file.
     */
    @Test
    public void rmUnstageTest() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        gitletExecute("rm", "_hello.txt");
    }

    /**
     * Add a file, commit, and rm it, commit again.
     * The latest commit should have an empty commit tree.
     * The file in the CWD should be deleted.
     */
    @Test
    public void rmCommitTest() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        gitletExecute("commit", "added hello");

        gitletExecute("rm", "_hello.txt");
        gitletExecute("commit", "removed hello");
    }


    /* LOG COMMAND */

    /** Sanity test for log command. */
    @Test
    public void logSanityTest() throws IOException {
        gitletExecute("init");
        gitletExecute("log");
    }

    /** Simple test for log command. */
    @Test
    public void simpleLogTest() throws  IOException {
        gitletExecute("init");
        gitletExecute("commit", "dummy commit");
        gitletExecute("log");
    }

    /** Normal test for log command. */
    @Test
    public void normalLogTest() throws  IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        gitletExecute("commit", "added hello");

        writeTestFile("_bye.txt", "bye");
        gitletExecute("add", "_bye.txt");
        gitletExecute("commit", "added bye");

        gitletExecute("log");
    }


    /* GLOBAL-LOG COMMAND */

    /** Sanity test for global-log command. */
    @Test
    public void globalLogSanityTest() throws  IOException {
        gitletExecute("init");

        gitletExecute("commit", "dummy 1");
        gitletExecute("commit", "dummy 2");
        gitletExecute("commit", "dummy 3");

        gitletExecute("global-log");
    }

    /** Test for global-log command with branching. */
    @Test
    public void globalLogBranchTest() throws  IOException {
        gitletExecute("init");
        gitletExecute("branch", "cool-bean");
        gitletExecute("commit", "dummy");
        gitletExecute("commit", "not dummy");
        gitletExecute("checkout", "cool-bean");
        gitletExecute("commit", "not dummy");
        gitletExecute("commit", "dummy");
        gitletExecute("global-log");
    }

    /* FIND COMMAND */

    /** Sanity test for find command. */
    @Test
    public void findSanityTest() throws IOException {
        gitletExecute("init");

        gitletExecute("commit", "dummy");
        gitletExecute("commit", "dummy");
        gitletExecute("commit", "not dummy");
        gitletExecute("log");

        gitletExecute("find", "dummy");
    }

    /** Test for find command with branching. */
    @Test
    public void findBranchTest() throws IOException {
        gitletExecute("init");
        gitletExecute("branch", "cool-bean");
        gitletExecute("commit", "dummy");
        gitletExecute("commit", "not dummy");
        gitletExecute("checkout", "cool-bean");
        gitletExecute("commit", "not dummy");
        gitletExecute("commit", "dummy");
        gitletExecute("global-log");

        gitletExecute("find", "dummy");
    }

    /* STATUS COMMAND */

    /** Basic test for status command. */
    @Test
    public void statusBasicTest() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        gitletExecute("commit", "added hello");
        writeTestFile("_bye.txt", "bye");
        gitletExecute("add", "_bye.txt");
        gitletExecute("rm", "_hello.txt");

        gitletExecute("status");
    }

    /**
     * Test extra functions ("Modification Not Staged For Commit")
     * condition 3 of status command.
     */
    @Test
    public void statusModification3Test() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");

        deleteTestFile("_hello.txt");
        gitletExecute("status");
    }

    /**
     * Test extra functions ("Modification Not Staged For Commit")
     * condition 4 of status command.
     */
    @Test
    public void statusModification4Test() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        gitletExecute("commit", "added hello");

        deleteTestFile("_hello.txt");
        gitletExecute("status");
    }

    /** Test extra functions ("Untracked Files") of status command. */
    @Test
    public void statusUntrackedTest() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        gitletExecute("commit", "added hello");
        writeTestFile("_bye.txt", "bye");
        gitletExecute("status");
    }

    /* CHECKOUT COMMAND */

    /** Sanity test for checkout usage 1. */
    @Test
    public void checkoutHeadFileSanityTest() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        gitletExecute("commit", "added hello");

        writeTestFile("_hello.txt", "hello world");
        assertEquals("hello world", readTestFile("_hello.txt"));
        gitletExecute("checkout", "--", "_hello.txt"); // java gitlet.Main checkout -- _hello.txt
        assertEquals("hello", readTestFile("_hello.txt"));
    }

    /** Sanity test for checkout usage 2. */
    @Test
    public void checkoutCommitFileSanityTest() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        gitletExecute("commit", "added hello");

        writeTestFile("_hello.txt", "hello world");
        assertEquals("hello world", readTestFile("_hello.txt"));
        gitletExecute("add", "_hello.txt");
        gitletExecute("commit", "changed hello");
        String commitID = Cache.getCommit(getLatestCommitID()).getParentCommitID();
        // java gitlet.Main checkout [abbreviated commit id] -- _hello.txt
        gitletExecute("checkout", commitID.substring(0, 6), "--", "_hello.txt");
        assertEquals("hello", readTestFile("_hello.txt"));
    }

    /** Sanity test for checkout usage 3. */
    @Test
    public void checkoutBranchSanityTest() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        gitletExecute("commit", "added hello");

        gitletExecute("branch", "cool-bean");
        gitletExecute("checkout", "cool-bean");
        gitletExecute("status");
    }

    /* BRANCH COMMAND */

    /** Sanity test for branch command. */
    @Test
    public void branchSanityTest() throws IOException {
        gitletExecute("init");

        gitletExecute("branch", "cool-bean");
        gitletExecute("branch", "hot-bean");
        gitletExecute("status");
    }

    /* RM-BRANCH COMMAND */

    /** Sanity test for rm-branch command. */
    @Test
    public void rmBranchSanityTest() throws IOException {
        gitletExecute("init");

        gitletExecute("branch", "cool-bean");
        gitletExecute("branch", "hot-bean");
        gitletExecute("rm-branch", "cool-bean");
        gitletExecute("status");
    }

    /* RESET COMMAND */

    /** Sanity test for reset command. */
    @Test
    public void resetSanityTest() throws IOException {
        gitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        gitletExecute("commit", "added hello");

        String commitID = Cache.getCommit(getLatestCommitID()).getParentCommitID();
        gitletExecute("reset", commitID);
        gitletExecute("log");
    }

    /* MERGE COMMAND */

    /** Test the lca method. */
    @Test
    public void lcaTest() throws IOException {
        gitletExecute("init");
        gitletExecute("branch", "hot-bean");
        gitletExecute("commit", "done something");
        gitletExecute("checkout", "hot-bean");
        gitletExecute("commit", "done something else");
        Commit commit1 = getCommit(getBranch("master"));
        Commit commit2 = getCommit(getBranch("hot-bean"));
        gitletExecute("global-log");
        // Should be the ID of the initial commit.
        System.out.println(Commit.lca(commit1, commit2).id());
    }

    /** A sanity test for the merge command. */
    @Test
    public void mergeSanityTest() throws IOException {
        gitletExecute("init");

        gitletExecute("branch", "hot-bean");
        gitletExecute("checkout", "hot-bean");

        writeTestFile("_hello.txt", "hello");
        gitletExecute("add", "_hello.txt");
        gitletExecute("commit", "added hello");

        gitletExecute("checkout", "master");
        writeTestFile("_bye.txt", "bye");
        gitletExecute("add", "_bye.txt");
        gitletExecute("commit", "added bye");

        gitletExecute("merge", "hot-bean");
        gitletExecute("global-log");
    }

    /** Test merging two branches with conflict. */
    @Test
    public void mergeConflictTest() throws IOException {
        gitletExecute("init");
        gitletExecute("branch", "cool-bean");

        writeAndAdd("conflict", "CONFLICT\n");
        gitletExecute("commit", "CONFLICT commit");

        gitletExecute("checkout", "cool-bean");
        writeAndAdd("conflict", "!CONFLICT\n");
        gitletExecute("commit", "!CONFLICT commit");

        gitletExecute("merge", "master");
        gitletExecute("log");
    }

    /** A hard (and comprehensive) test for the merge command. */
    @Test
    public void mergeTest() throws IOException {
        gitletExecute("init");
        writeAndAdd("a", "A");
        writeAndAdd("b", "B");
        writeAndAdd("c", "C");
        writeAndAdd("d", "D");
        writeAndAdd("e", "E");
        writeAndAdd("conflict", "Hi mom");
        gitletExecute("commit", "split commit");
        gitletExecute("branch", "branch1");

        writeAndAdd("a", "!A");
        writeAndAdd("b", "B");
        gitletExecute("rm", "c");
        gitletExecute("rm", "d");
        writeAndAdd("e", "E");
        writeAndAdd("f", "!F");
        writeAndAdd("conflict", "CONFLICT\n");
        gitletExecute("commit", "master commit");

        gitletExecute("checkout", "branch1");
        writeAndAdd("a", "A");
        writeAndAdd("b", "!B");
        gitletExecute("rm", "c");
        writeAndAdd("d", "D");
        gitletExecute("rm", "e");
        writeAndAdd("g", "G");
        writeAndAdd("conflict", "!CONFLICT\n");
        gitletExecute("commit", "branch1 commit");

        gitletExecute("merge", "master");

        assertFile("a", "!A");
        assertFile("b", "!B");
        assertFile("c", null);
        assertFile("d", null);
        assertFile("e", null);
        assertFile("f", "!F");
        assertFile("g", "G");
        assertFile("conflict",
                "<<<<<<< HEAD\n!CONFLICT\n=======\nCONFLICT\n>>>>>>>\n");
    }

    /* ADD-REMOTE COMMAND */

    /** A sanity test for add-remote command. */
    @Test
    public void addRemoteTest() throws IOException {
        gitletExecute("init");
        gitletExecute("add-remote", "test", "D:/_SDE/cs61b/PlayGround/.gitlet");
        Remote.readRemote("test");
    }

    /* PUSH COMMAND */

    static final String REMOTE_WD = "D:/_SDE/cs61b/PlayGround2/.gitlet";

    /** A sanity test for push command. */
    @Test
    public void pushTest() throws IOException {
        gitletExecute("init");
        writeAndAdd("a", "a");
        gitletExecute("commit", "random commit");
        writeAndAdd("b", "b");
        writeAndAdd("c", "c");
        gitletExecute("commit", "yet another random commit");
        gitletExecute("add-remote", "PlayGround2", REMOTE_WD);
        gitletExecute("push", "PlayGround2", "cool-bean");
    }

    /* FETCH COMMAND */

    /** A sanity test for fetch command. */
    @Test
    public void fetchTest() throws IOException {
        gitletExecute("init");
        gitletExecute("add-remote", "PlayGround2", REMOTE_WD);
        gitletExecute("fetch", "PlayGround2", "master");
        gitletExecute("status");
        gitletExecute("global-log");
    }

    /* PULL COMMAND */

    /** A sanity test for pull command. */
    @Test
    public void pullTest() throws IOException {
        gitletExecute("init");
        gitletExecute("add-remote", "PlayGround2", REMOTE_WD);
        gitletExecute("pull", "PlayGround2", "master");
        gitletExecute("status");
        gitletExecute("log");
    }


    /* AUTO GRADER DEBUGS */

    @Test
    public void test20StatusAfterCommit() throws IOException {
        gitletExecute("init");
        writeAndAdd("f.txt", "wug");
        writeAndAdd("g.txt", "not wug");
        gitletExecute("commit", "Two files");
        gitletExecute("status");

        gitletExecute("rm", "f.txt");
        gitletExecute("commit", "Removed f.txt");
        gitletExecute("status");
    }

    @Test
    public void test24GlobalLogPrev() throws IOException {
        gitletExecute("init");
        writeAndAdd("f.txt", "wug");
        writeAndAdd("g.txt", "not wug");
        gitletExecute("commit", "Two files");
        writeAndAdd("h.txt", "h");
        gitletExecute("commit", "Add h");
//        GitletExecute("log");

        String id = getLatestCommit().getParentCommitID();
        gitletExecute("reset", id);
        gitletExecute("global-log"); // Should print out all three commits
    }

    @Test
    public void test29BadCheckoutsErr() throws IOException {
        gitletExecute("init");
        writeAndAdd("wug.txt", "wug");
        gitletExecute("commit", "version 1 of wug.txt");
        writeAndAdd("wug.txt", "not wug");
        gitletExecute("commit", "version 2 of wug.txt");
        String version2ID = getLatestCommitID();
        String version1ID = getLatestCommit().getParentCommitID();
        gitletExecute("checkout",
                "5d0bc169a1737e955f9cb26b9e7aa21e4afd4d12", "--", "wug.txt");

    }

    @Test
    public void test35MergeRmConflicts() throws IOException {
        gitletExecute("init");
        writeAndAdd("f.txt", "This is a wug.\n");
        writeAndAdd("g.txt", "This is not a wug.\n");
        gitletExecute("commit", "Two files");

        gitletExecute("branch", "other");
        writeAndAdd("h.txt", "Another wug.\n");
        gitletExecute("rm", "g.txt");
        writeAndAdd("f.txt", "Another wug.\n");
        gitletExecute("commit", "Add h.txt, remove g.txt, and change f.txt");

        gitletExecute("checkout", "other");
        gitletExecute("rm", "f.txt");
        writeAndAdd("k.txt", "And yet another wug.\n");
        gitletExecute("commit", "Add k.txt and remove f.txt");

        gitletExecute("checkout", "master");
        String masterHead = getBranch("master");

        gitletExecute("merge", "other");

        assertFileNotExist("g.txt");
        assertFile("h.txt", "Another wug.\n");
        assertFile("k.txt", "And yet another wug.\n");
        assertFile("f.txt",
                "<<<<<<< HEAD\nAnother wug.\n=======\n>>>>>>>\n");

        gitletExecute("log");
        gitletExecute("status");
    }

    @Test
    public void test36AMergeParent2() throws IOException {
        gitletExecute("init");
        gitletExecute("branch", "B1");
        gitletExecute("branch", "B2");

        gitletExecute("checkout", "B1");
        writeAndAdd("h.txt", "This is a wug.\n");
        gitletExecute("commit", "Add h.txt");

        gitletExecute("checkout", "B2");
        writeAndAdd("f.txt", "This is a wug.\n");
        gitletExecute("commit", "f.txt added");

        gitletExecute("branch", "C1");
        writeAndAdd("g.txt", "This is not a wug.\n");
        gitletExecute("rm", "f.txt");
        gitletExecute("commit", "g.txt added, f.txt removed");

        gitletExecute("checkout", "B1");
        assertFile("h.txt", "This is a wug.\n");
        assertFileNotExist("f.txt");
        assertFileNotExist("g.txt");

        gitletExecute("merge", "C1");
        assertFile("f.txt", "This is a wug.\n");
        assertFile("h.txt", "This is a wug.\n");
        assertFileNotExist("g.txt");

        gitletExecute("merge", "B2");
        assertFileNotExist("f.txt");
        assertFile("g.txt", "This is not a wug.\n");
        assertFile("h.txt", "This is a wug.\n");
    }

    /* MISC */

    static File CWD = new File(System.getProperty("user.dir"));

    /** Execute commands with Gitlet and clean the cache after execution. */
    private static void gitletExecute(String... command) throws IOException {
        if (command[0].equals("init")) {
            Repository.assignStaticVariables(CWD);
            Repository.deleteCWDFiles();
            deleteDirectory(GITLET_DIR);
        } // Special case: make sure there is no .gitlet directory before init command.
          // Implemented for testing purposes.
        Main.main(command);
        cleanCache();
    }

    /** Write content into a designated file name. Overwriting or creating file as needed. */
    private static void writeTestFile(String fileName, String content) {
        File file = join(CWD, fileName);
        writeContents(file, content);
    }

    /** Delete the file with the designated name. */
    private static void deleteTestFile(String fileName) {
        File file = join(CWD, fileName);
        file.delete();
    }

    /** Read the designated file as String and return it. */
    private static String readTestFile(String fileName) {
        File file = join(CWD, fileName);
        return readContentsAsString(file);
    }

    /** Delete a directory recursively. */
    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    /** Write a test file with the designated file name and content, then add it to the stage. */
    private static void writeAndAdd(String fileName, String content) throws IOException {
        writeTestFile(fileName, content);
        gitletExecute("add", fileName);
    }

    /** Assert a designated file has the designated content. */
    private static void assertFile(String fileName, String content) {
        File file = join(CWD, fileName);
        if (!file.exists() && content == null) {
            return;
        }
        String fileContent = readContentsAsString(file);
        assertEquals(content, fileContent);
    }

    /** Assert a designated file does not exist. */
    private static void assertFileNotExist(String fileName) {
        File file = join(CWD, fileName);
        assertFalse(file.exists());
    }
}
