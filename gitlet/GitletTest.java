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
    /** Sanity test for init command. */
    @Test
    public void initCommandSanityTest() throws IOException {
        String[] initCommand = {"init"};
        Main.main(initCommand);
    }

    /** Sanity test for add command. */
    @Test
    public void addCommandSanityTest() throws IOException {
        File _hello = join(CWD, "_hello.txt");
        writeContents(_hello, "hello");

        String[] initCommand = {"init"};
        Main.main(initCommand);
        cleanCache();
        String[] addCommand = {"add", "_hello.txt"};
        Main.main(addCommand);
    }

    /** Test using add command twice. */
    @Test
    public void addCommandTwiceTest() throws IOException {
        File _hello = join(CWD, "_hello.txt");
        writeContents(_hello, "hello");
        File _bye = join(CWD, "_bye.txt");
        writeContents(_bye, "bye");

        String[] initCommand = {"init"};
        Main.main(initCommand);
        cleanCache();
        String[] addCommand1 = {"add", "_hello.txt"};
        String[] addCommand2 = {"add", "_bye.txt"};
        Main.main(addCommand1);
        cleanCache();
        Main.main(addCommand2);
    }

}
