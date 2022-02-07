package gitlet;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static gitlet.Repository.CWD;
import static gitlet.Repository.OBJECTS_DIR;
import static gitlet.Utils.*;
import static org.junit.Assert.*;

/**
 * @deprecated
 */
public class HashObjectTest {

    /**
     * Create a HashObject, save it, and check the dump file.
     */
    @Test
    public void dumpingTest() throws IOException {
//        Repository.setUpPersistence();
//        HashObject test = new HashObject();
//        String fileName = test.save();
//        DumpObj.main(join(OBJECTS_DIR, fileName));
    }

    /**
     * Try to find a way to get a content-addressable id of objects.
     * It seems impossible.
     * @deprecated
     */
    @Test
    public void saveAndHashTest() {
        // An object's SHA1 will change once it is serialized and deserialized.
//        HashObject testHashObject = new HashObject();
//        testHashObject.save();
//        String fileID = testHashObject.id(); // SHA-1
//        File savedFile = join(CWD, fileID);
//
//        HashObject readHashObject = readObject(savedFile, HashObject.class);
//        String readHashObjectID = readHashObject.id();
//        assertEquals(fileID, readHashObjectID);
    }

    /** Sanity test for Tree functions. */
    @Test
    public void treeTest() {
//        Tree testTree = new Tree();
//        assertTrue(testTree.isEmpty());
//
//        testTree.record("test.txt", "some hash");
//
//        assertEquals("some hash", testTree.retrieve("test.txt"));
//        assertNull(testTree.retrieve("notExist.txt"));
    }

    /** Dump test for Tree. */
    @Test
    public void treeDumpTest() {
//        Tree testTree = new Tree();
//        testTree.record("test.txt", "some hash");
//        testTree.record("other.txt", "other hash");

//        String testTreeID = testTree.save();
//        DumpObj.main(testTreeID);
    }
}
