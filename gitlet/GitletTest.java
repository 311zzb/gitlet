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
    }

    /** Test using add command many times. */
    @Test
    public void addCommandManyTimesTest() throws IOException {
        File _hello = join(CWD, "_hello.txt");
        writeContents(_hello, "hello");
        File _bye = join(CWD, "_bye.txt");
        writeContents(_bye, "bye");
        File _mystery = join(CWD, "_mystery.txt");
        writeContents(_mystery, "mystery");

        String[] initCommand = {"init"};
        Main.main(initCommand);
        String[] addCommand1 = {"add", "_hello.txt"};
        String[] addCommand2 = {"add", "_bye.txt"};
        String[] addCommand3 = {"add", "_mystery.txt"};
        Main.main(addCommand1);
        Main.main(addCommand2);
        Main.main(addCommand3);
    }

}
