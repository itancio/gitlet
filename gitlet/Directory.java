package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Helper.*;

public class Directory {
    /** The current working directory. */
    protected static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    protected static final File GITLET_DIR = join(CWD, ".gitlet");
    protected static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    protected static final File STAGE_DIR = join(GITLET_DIR, "stage");
    protected static final File REFS_DIR = join(GITLET_DIR, "refs");
    protected static final File BRANCHES_DIR = join(GITLET_DIR, "branches");

    /** Folder that commit objects live in. */
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");

    /** HashMap to keep track of the files in the Staging Area */
    protected static Map<String, String> stageAdd = new HashMap<>();
    protected static Map<String, String> stageRmv = new HashMap<>();

    protected static Graph<String> graph = new Graph<>();

    /** File that HEAD pointer contains */
    protected static File HEAD = join(GITLET_DIR, "HEAD");

    /** File that stage Add and stage remove contain */
    protected static File STAGE_ADD_FILE = join(REFS_DIR, "Map_Stage_Add");
    protected static File STAGE_REMOVE_FILE = join(REFS_DIR, "Map_Stage_Remove");

    /** File that the graph of commitsIDs contain */
    protected static File GRAPH_FILE = join(REFS_DIR, "Graph");

    public static void initDirectory() {

        // Creates a new .gitlet directory
        GITLET_DIR.mkdir();

        // Creates subdirectory
        BLOBS_DIR.mkdir();
        STAGE_DIR.mkdir();
        REFS_DIR.mkdir();
        BRANCHES_DIR.mkdir();

        // Creates commit subdirectory called OBJECT
        COMMIT_DIR.mkdir();

        // Creates a file for the HEAD pointer
        createNewFile(HEAD);

        // Creates new file that contains the Map of StageAdd
        createNewFile(STAGE_ADD_FILE);
        writeObject(STAGE_ADD_FILE, (Serializable) stageAdd);

        // Creates new file that contains the Map of StageRemove
        createNewFile(STAGE_REMOVE_FILE);
        writeObject(STAGE_REMOVE_FILE, (Serializable) stageRmv);

        // Creates new file that contains the graph
        createNewFile(GRAPH_FILE);
        writeObject(GRAPH_FILE, (Serializable) graph);

    }

}
