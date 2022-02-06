package gitlet;

import static gitlet.Repository.STAGE;
import static gitlet.Utils.writeContents;

/**
 * A class where Stage related static methods live.
 * Will never be instantiated.
 *
 * @author XIE Changyuan
 */
public class Stage {

    /**
     * Refresh the staging area.
     * By creating a new Tree and overwriting STAGE with the new Tree's ID.
     */
    static void refresh_Stage() {
        String newTreeID = Tree.newWrite_Tree();
        writeContents(STAGE, newTreeID);
    }
}
