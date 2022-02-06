package gitlet;

import org.junit.Test;

import java.io.File;

import static gitlet.Repository.CWD;
import static gitlet.Utils.*;
import static org.junit.Assert.*;

public class HashObjectTest {

    /**
     * Create a HashObject, save it, and check the dump file.
     */
    @Test
    public void dumpingTest() {
        HashObject test = new HashObject("test");
        String fileName = test.save();
        DumpObj.main(fileName);
    }

    /**
     * Try to find a way to get a content-addressable id of objects.
     * It seems impossible.
     */
    @Test
    public void saveAndHashTest() {
        // An object's SHA1 will change once it is serialized and deserialized.
        HashObject testHashObject = new HashObject("test");
        testHashObject.save();
        String fileID = testHashObject.id();
        File savedFile = join(CWD, fileID);

        String readHashObjectString = readContentsAsString(savedFile);
        String readHashObjectStringID = sha1(readHashObjectString);
        assertEquals(fileID, readHashObjectStringID);

        HashObject readHashObject = readObject(savedFile, HashObject.class);
        String readHashObjectID = readHashObject.id();
        assertEquals(fileID, readHashObjectID);
    }

    /** Sanity test for Tree functions. */
    @Test
    public void treeTest() {
        Tree testTree = new Tree();
        assertTrue(testTree.isEmpty());

        testTree.record("test.txt", "some hash");

        assertEquals("some hash", testTree.retrieve("test.txt"));
        assertNull(testTree.retrieve("notExist.txt"));
    }

    /** Dump test for Tree. */
    @Test
    public void treeDumpTest() {
        Tree testTree = new Tree();
        testTree.record("test.txt", "some hash");
        testTree.record("other.txt", "other hash");

        String testTreeID = testTree.save();
        DumpObj.main(testTreeID);
    }
}
