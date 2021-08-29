package gitlet;

import java.io.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Directory.*;
import static gitlet.Helper.*;
import static gitlet.Graph.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Irvin Tancioco
 */
public class Commit<T> implements Serializable, Comparable<T> {

    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    private String[] parents;
    private String uid;

    /** METADATA of this commit: 1) message, 2) timestamp */
    private String message;
    private Date timestamp;

    /** Reference to a blob map that keeps track of the filename as the key, and version as val */
    Map<String, String> blobs;

    /** A Commit constructor. Initialize the timeStamp to Epoch */
    public Commit(String message) {
        this(message, null, null);
        this.timestamp = new Date(0);
    }

    /** A constructor that takes in message, and parent reference */
    public Commit(String message, String parent) {
        this(message, parent, null);
    }

    /** A constructor that takes in message, and 2 parents reference */
    public Commit(String message, String parent1, String parent2) {
        this.message = message;
        this.timestamp = new Date();
        this.parents = new String[]{parent1, parent2};
        this.uid = null;
        this.blobs = new TreeMap<>();
    }


    public String getMessage() {
        return this.message;
    }

    public Date getTimeStamp() {
        return this.timestamp;
    }

    public String getFirstParent() {
        return this.parents[0];
    }

    public String getSecondParent() {
        return this.parents[1];
    }

    public String getUid() {
        return this.uid;
    }

    public Map<String, String> getBlobs() {
        return blobs;
    }

    
    /** Reads in and deserializes a COMMIT from a file with name FILENAME in COMMIT_DIR. */
    public static Commit fromFile(String filename) {
        if (filename == null) {
            return null;
        }

        File path = join(COMMIT_DIR, filename);
        // Deserialize commit and read the contents of an existing file
        Commit c = readObject(path, Commit.class);
        return c;
    }

    /** Clones an existing commit */
    public Commit clone(String message) {
        return clone(message, null);
    }

    public Commit clone(String message, String parent2) {
        String parentUid = null;
        if (!(parent2 == null)) {
            String parentUiD = getBranchID(parent2);
        }
        Commit currCommit = new Commit(message, this.getUid(), parentUid);
        currCommit.blobs.putAll(this.blobs);

        return currCommit;
    }

    /** Writes the commit object and saves sha1 hashcode as filename */
    public void writeCommit() {
        // Check if file exists. If it does not, it creates the file
        String id = sha1(serialize(this));
        File path = join(COMMIT_DIR, id);
        if (!path.exists()) {
            createNewFile(path);
        }

        // Update this commit's UID
        this.uid = id;

        // Serialize commit and write in the existing COMMIT file
        writeObject(path, this);

        // Add the new commit to a graph and write to a file */
        for(String p : parents) {
            if (p != null) {
                graph.addVertex(this.uid);
                graph.addEdge(this.uid, p, BIDIRECTIONAL);
            }
        }

        writeObject(GRAPH_FILE, Graph.class);
    }

    @Override
    public String toString() {
        String contents = String.format("ID: %s   Timestamp: %s   message: %s     blobs: %s",
                uid, timestamp, message, blobs.toString());
        return contents;
    }

    @Override
    public int compareTo(T o) {
        Commit c = (Commit) o;
        boolean isSameMessage = this.message.equals(c.getMessage());
        boolean isSameTime = this.timestamp.equals(c.getTimeStamp());
        boolean isBlobs = this.blobs.values().equals(c.getBlobs().values());
        return ((isSameMessage && isSameTime && isBlobs) ? 0 : -5);
    }

    /****************************
     *
     * HELPER FUNCTIONS
     *
     ****************************/


    /** print the metadata of a commit */
    public void printCommit() {
        Date date = this.getTimeStamp();
        Format formatted = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        this.getTimeStamp();
        String contents = "===";
        contents = String.format(contents + "\n" + "commit %s" + "\n", this.getUid());

        /** Handles log history with merge */
        if (this.getSecondParent() != null) {
            contents = addMergeContent(this, contents);
        }

        contents = String.format(contents + "Date: %s" + "\n"
                + this.getMessage() + "\n", formatted.format(date));

        System.out.println(contents);

    }

    private String addMergeContent(Commit commit, String contents) {
        String firstBranch = subString(commit.getFirstParent());
        String secondBranch = subString(commit.getSecondParent());
        contents = String.format(contents + "Merge: %s %s" + "\n",
                firstBranch, secondBranch);

        return contents;
    }

    private String subString(String commitUID) {
        return commitUID.substring(0, 6);
    }
}
