package gitlet;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static gitlet.Cache.*;
import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static org.junit.Assert.*;

/**
 * This class contains JUnit tests for Gitlet.
 *
 * @author XIE Changyuan
 */
public class GitletTest {

    /* INIT COMMAND --------------------------------------------------------------------------------------------------*/

    /** Sanity test for init command. */
    @Test
    public void initCommandSanityTest() throws IOException {
        GitletExecute("init");
    }

    /* ADD COMMAND ---------------------------------------------------------------------------------------------------*/

    /** Sanity test for add command. */
    @Test
    public void addCommandSanityTest() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
    }

    /** Test using add command twice. */
    @Test
    public void addCommandTwiceTest() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");

        writeTestFile("_bye.txt", "bye");
        GitletExecute("add", "_bye.txt");
    }

    /* COMMIT COMMAND ------------------------------------------------------------------------------------------------*/

    /** Sanity test for commit command. */
    @Test
    public void commitSanityTest() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        writeTestFile("_bye.txt", "bye");
        GitletExecute("add", "_bye.txt");

        GitletExecute("commit", "added hello and bye");
    }

    /** Dummy commit test. */
    @Test
    public void dummyCommitTest() throws IOException {
        GitletExecute("init");

        GitletExecute("commit", "dummy commit");
    }

    /** Add a file, make a commit, and add another file. */
    @Test
    public void commitAndAddTest() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        GitletExecute("commit", "added hello");

        writeTestFile("_bye.txt", "bye");
        GitletExecute("add", "_bye.txt");
    }

    /**
     * Make a commit, change the file and add, then change back and add.
     * The staging area should be empty.
     */
    @Test
    public void addAndRestoreTest() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        GitletExecute("commit", "added hello");

        writeTestFile("_hello.txt", "hello world");
        GitletExecute("add", "_hello.txt");
        writeTestFile("_hello.txt", "hello");
        // Should remove _hello.txt from the staging area since it is now identical with the version in the latest commit
        GitletExecute("add", "_hello.txt");
        assertTrue(getStage().isEmpty());
    }

    /* RM COMMAND ----------------------------------------------------------------------------------------------------*/

    /**
     * The rm command should unstage the added file.
     */
    @Test
    public void rmUnstageTest() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        GitletExecute("rm", "_hello.txt");
    }

    /**
     * Add a file, commit, and rm it, commit again.
     * The latest commit should have an empty commit tree.
     * The file in the CWD should be deleted.
     */
    @Test
    public void rmCommitTest() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        GitletExecute("commit", "added hello");

        GitletExecute("rm", "_hello.txt");
        GitletExecute("commit", "removed hello");
    }


    /* LOG COMMAND ---------------------------------------------------------------------------------------------------*/

    /** Sanity test for log command. */
    @Test
    public void logSanityTest() throws IOException {
        GitletExecute("init");
        GitletExecute("log");
    }

    /** Simple test for log command. */
    @Test
    public void simpleLogTest() throws  IOException {
        GitletExecute("init");
        GitletExecute("commit", "dummy commit");
        GitletExecute("log");
    }

    /** Normal test for log command. */
    @Test
    public void normalLogTest() throws  IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        GitletExecute("commit", "added hello");

        writeTestFile("_bye.txt", "bye");
        GitletExecute("add", "_bye.txt");
        GitletExecute("commit", "added bye");

        GitletExecute("log");
    }


    /* GLOBAL-LOG COMMAND --------------------------------------------------------------------------------------------*/

    /** Sanity test for global-log command. */
    @Test
    public void globalLogSanityTest() throws  IOException {
        GitletExecute("init");

        GitletExecute("commit", "dummy 1");
        GitletExecute("commit", "dummy 2");
        GitletExecute("commit", "dummy 3");

        GitletExecute("global-log");
    }

    /** Test for global-log command with branching. */
    @Test
    public void globalLogBranchTest() throws  IOException {
        GitletExecute("init");
        GitletExecute("branch", "cool-bean");
        GitletExecute("commit", "dummy");
        GitletExecute("commit", "not dummy");
        GitletExecute("checkout", "cool-bean");
        GitletExecute("commit", "not dummy");
        GitletExecute("commit", "dummy");
        GitletExecute("global-log");
    }

    /* FIND COMMAND --------------------------------------------------------------------------------------------------*/

    /** Sanity test for find command. */
    @Test
    public void findSanityTest() throws IOException {
        GitletExecute("init");

        GitletExecute("commit", "dummy");
        GitletExecute("commit", "dummy");
        GitletExecute("commit", "not dummy");
        GitletExecute("log");

        GitletExecute("find", "dummy");
    }

    /** Test for find command with branching. */
    @Test
    public void findBranchTest() throws IOException {
        GitletExecute("init");
        GitletExecute("branch", "cool-bean");
        GitletExecute("commit", "dummy");
        GitletExecute("commit", "not dummy");
        GitletExecute("checkout", "cool-bean");
        GitletExecute("commit", "not dummy");
        GitletExecute("commit", "dummy");
        GitletExecute("global-log");

        GitletExecute("find", "dummy");
    }

    /* STATUS COMMAND ------------------------------------------------------------------------------------------------*/

    /** Basic test for status command. */
    @Test
    public void statusBasicTest() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        GitletExecute("commit", "added hello");
        writeTestFile("_bye.txt", "bye");
        GitletExecute("add", "_bye.txt");
        GitletExecute("rm", "_hello.txt");

        GitletExecute("status");
    }

    /** Test extra functions ("Modification Not Staged For Commit") condition 3 of status command. */
    @Test
    public void statusModification3Test() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");

        deleteTestFile("_hello.txt");
        GitletExecute("status");
    }

    /** Test extra functions ("Modification Not Staged For Commit") condition 4 of status command. */
    @Test
    public void statusModification4Test() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        GitletExecute("commit", "added hello");

        deleteTestFile("_hello.txt");
        GitletExecute("status");
    }

    /** Test extra functions ("Untracked Files") of status command. */
    @Test
    public void statusUntrackedTest() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        GitletExecute("commit", "added hello");
        writeTestFile("_bye.txt", "bye");
        GitletExecute("status");
    }

    /* CHECKOUT COMMAND ----------------------------------------------------------------------------------------------*/

    /** Sanity test for checkout usage 1. */
    @Test
    public void checkoutHeadFileSanityTest() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        GitletExecute("commit", "added hello");

        writeTestFile("_hello.txt", "hello world");
        assertEquals("hello world", readTestFile("_hello.txt"));
        GitletExecute("checkout", "--", "_hello.txt"); // java gitlet.Main checkout -- _hello.txt
        assertEquals("hello", readTestFile("_hello.txt"));
    }

    /** Sanity test for checkout usage 2. */
    @Test
    public void checkoutCommitFileSanityTest() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        GitletExecute("commit", "added hello");

        writeTestFile("_hello.txt", "hello world");
        assertEquals("hello world", readTestFile("_hello.txt"));
        GitletExecute("add", "_hello.txt");
        GitletExecute("commit", "changed hello");
        String commitID = Cache.getCommit(getLatestCommitID()).getParentCommitID();
        GitletExecute("checkout", commitID.substring(0, 6), "--", "_hello.txt"); // java gitlet.Main checkout [abbreviated commit id] -- _hello.txt
        assertEquals("hello", readTestFile("_hello.txt"));
    }

    /** Sanity test for checkout usage 3. */
    @Test
    public void checkoutBranchSanityTest() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        GitletExecute("commit", "added hello");

        GitletExecute("branch", "cool-bean");
        GitletExecute("checkout", "cool-bean");
        GitletExecute("status");
    }

    /* BRANCH COMMAND ------------------------------------------------------------------------------------------------*/

    /** Sanity test for branch command. */
    @Test
    public void branchSanityTest() throws IOException {
        GitletExecute("init");

        GitletExecute("branch", "cool-bean");
        GitletExecute("branch", "hot-bean");
        GitletExecute("status");
    }

    /* RM-BRANCH COMMAND ---------------------------------------------------------------------------------------------*/

    /** Sanity test for rm-branch command. */
    @Test
    public void rmBranchSanityTest() throws IOException {
        GitletExecute("init");

        GitletExecute("branch", "cool-bean");
        GitletExecute("branch", "hot-bean");
        GitletExecute("rm-branch", "cool-bean");
        GitletExecute("status");
    }

    /* RESET COMMAND -------------------------------------------------------------------------------------------------*/

    /** Sanity test for reset command. */
    @Test
    public void resetSanityTest() throws IOException {
        GitletExecute("init");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        GitletExecute("commit", "added hello");

        String commitID = Cache.getCommit(getLatestCommitID()).getParentCommitID();
        GitletExecute("reset", commitID);
        GitletExecute("log");
    }

    /* MERGE COMMAND -------------------------------------------------------------------------------------------------*/

    /** Test the lca method. */
    @Test
    public void lcaTest() throws IOException {
        GitletExecute("init");
        GitletExecute("branch", "hot-bean");
        GitletExecute("commit", "done something");
        GitletExecute("checkout", "hot-bean");
        GitletExecute("commit", "done something else");
        Commit commit1 = getCommit(getBranch("master"));
        Commit commit2 = getCommit(getBranch("hot-bean"));
        GitletExecute("global-log");
        System.out.println(Commit.lca(commit1, commit2).id()); // Should be the ID of the initial commit.
    }

    /** A sanity test for the merge command. */
    @Test
    public void mergeSanityTest() throws IOException {
        GitletExecute("init");

        GitletExecute("branch", "hot-bean");
        GitletExecute("checkout", "hot-bean");

        writeTestFile("_hello.txt", "hello");
        GitletExecute("add", "_hello.txt");
        GitletExecute("commit", "added hello");

        GitletExecute("checkout", "master");
        writeTestFile("_bye.txt", "bye");
        GitletExecute("add", "_bye.txt");
        GitletExecute("commit", "added bye");

        GitletExecute("merge", "hot-bean");
        GitletExecute("global-log");
    }

    /** Test merging two branches with conflict. */
    @Test
    public void mergeConflictTest() throws IOException {
        GitletExecute("init");
        GitletExecute("branch", "cool-bean");

        writeAndAdd("conflict", "CONFLICT\n");
        GitletExecute("commit", "CONFLICT commit");

        GitletExecute("checkout", "cool-bean");
        writeAndAdd("conflict", "!CONFLICT\n");
        GitletExecute("commit", "!CONFLICT commit");

        GitletExecute("merge", "master");
        GitletExecute("log");
    }

    /** A hard (and comprehensive) test for the merge command. */
    @Test
    public void mergeTest() throws IOException {
        GitletExecute("init");
        writeAndAdd("a", "A");
        writeAndAdd("b", "B");
        writeAndAdd("c", "C");
        writeAndAdd("d", "D");
        writeAndAdd("e", "E");
        writeAndAdd("conflict", "Hi mom");
        GitletExecute("commit", "split commit");
        GitletExecute("branch", "branch1");

        writeAndAdd("a", "!A");
        writeAndAdd("b", "B");
        GitletExecute("rm", "c");
        GitletExecute("rm", "d");
        writeAndAdd("e", "E");
        writeAndAdd("f", "!F");
        writeAndAdd("conflict", "CONFLICT\n");
        GitletExecute("commit", "master commit");

        GitletExecute("checkout", "branch1");
        writeAndAdd("a", "A");
        writeAndAdd("b", "!B");
        GitletExecute("rm", "c");
        writeAndAdd("d", "D");
        GitletExecute("rm", "e");
        writeAndAdd("g", "G");
        writeAndAdd("conflict", "!CONFLICT\n");
        GitletExecute("commit", "branch1 commit");

        GitletExecute("merge", "master");

        assertFile("a", "!A");
        assertFile("b", "!B");
        assertFile("c", null);
        assertFile("d", null);
        assertFile("e", null);
        assertFile("f", "!F");
        assertFile("g", "G");
        assertFile("conflict", "<<<<<<< HEAD\n!CONFLICT\n=======\nCONFLICT\n>>>>>>>\n");
    }

    /* AUTO GRADER DEBUGS */

    @Test
    public void test20_status_after_commit() throws IOException {
        GitletExecute("init");
        writeAndAdd("f.txt", "wug");
        writeAndAdd("g.txt", "not wug");
        GitletExecute("commit", "Two files");
        GitletExecute("status");

        GitletExecute("rm", "f.txt");
        GitletExecute("commit", "Removed f.txt");
        GitletExecute("status");
    }

    @Test
    public void test24_global_log_prev() throws IOException {
        GitletExecute("init");
        writeAndAdd("f.txt", "wug");
        writeAndAdd("g.txt", "not wug");
        GitletExecute("commit", "Two files");
        writeAndAdd("h.txt", "h");
        GitletExecute("commit", "Add h");
//        GitletExecute("log");

        String id = getLatestCommit().getParentCommitID();
        GitletExecute("reset", id);
        GitletExecute("global-log"); // Should print out all three commits
    }

    @Test
    public void test29_bad_checkouts_err() throws IOException {
        GitletExecute("init");
        writeAndAdd("wug.txt", "wug");
        GitletExecute("commit", "version 1 of wug.txt");
        writeAndAdd("wug.txt", "not wug");
        GitletExecute("commit", "version 2 of wug.txt");
        String version2ID = getLatestCommitID();
        String version1ID = getLatestCommit().getParentCommitID();
//        GitletExecute("checkout", "--", "warg.txt");
        GitletExecute("checkout", "5d0bc169a1737e955f9cb26b9e7aa21e4afd4d12", "--", "wug.txt");

    }

    /* MISC ----------------------------------------------------------------------------------------------------------*/

    /** Execute commands with Gitlet and clean the cache after execution. */
    private static void GitletExecute(String... command) throws IOException {
        if (command[0].equals("init")) {
            Repository.deleteCWDFiles();
            deleteDirectory(GITLET_DIR);
        } // Special case: make sure there is no .gitlet directory before init command. Implemented for testing purposes.
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
        GitletExecute("add", fileName);
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
}
