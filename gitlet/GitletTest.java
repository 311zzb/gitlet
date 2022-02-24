package gitlet;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static gitlet.Cache.getLatestCommitRef;
import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static gitlet.Cache.cleanCache;
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
        // TODO
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
        // TODO
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
        String commitID = Cache.getCommit(getLatestCommitRef()).getParentCommitRef();
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

        String commitID = Cache.getCommit(getLatestCommitRef()).getParentCommitRef();
        GitletExecute("reset", commitID);
        GitletExecute("log");
    }


    /* MISC ----------------------------------------------------------------------------------------------------------*/

    /** Execute commands with Gitlet and clean the cache after execution. */
    private static void GitletExecute(String... command) throws IOException {
        if (command[0].equals("init")) {
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
}
