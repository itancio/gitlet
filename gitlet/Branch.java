package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Directory.*;
import static gitlet.Helper.*;
import static gitlet.Utils.*;

public class Branch implements Serializable {

    public static final String SENTINEL = "sentinel";

    private String name;
    private String id;

    /** A Constructor accepts a name of the branch and writes to a file */
    public Branch(String name, String id) {
        String cName = name.toLowerCase();
        this.name = cName;
        this.id = id;
    }

    public String getCommitUID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    /** Write to a file the commit UID of the active pointer */
    public void writeBranch() {
        // Write the branch to a file
        File filename = join(BRANCHES_DIR, this.name);
        createNewFile(filename);
        writeContents(filename, id);
    }
}
