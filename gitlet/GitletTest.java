package gitlet;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static gitlet.Cache.cleanCache;

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
        GitletExecute("init");
    }

    /* ADD COMMAND */

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

    /* COMMIT COMMAND */

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

    /* LOG COMMAND */

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



    /* MISC */

    /** Execute commands with Gitlet and clean the cache after execution. */
    private static void GitletExecute(String... command) throws IOException {
        Main.main(command);
        cleanCache();
    }

    /** Write content into a designated file name. Overwriting or creating file as needed. */
    private static void writeTestFile(String fileName, String content) {
        File file = join(CWD, fileName);
        writeContents(file, content);
    }
}
