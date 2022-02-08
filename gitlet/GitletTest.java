package gitlet;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

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
        String[] addCommand = {"add", "_hello.txt"};
        Main.main(addCommand);

        // check persistence
    }
}
