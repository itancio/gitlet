package gitlet;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.*;
import static gitlet.Utils.*;
import static gitlet.Directory.*;
import static gitlet.Helper.*;
import static gitlet.Commit.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *  @author Irvin Tancioco
 */

public class Repository {
    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    public static void init() {

        // Return if .gitlet is already created
        if (GITLET_DIR.exists()) {
            return;
        }

        Directory.initDirectory();

        // Creates a new Commit object and save to a file
        Commit init = new Commit("initial commit");
        init.writeCommit();

        // Creates a new master branch
        Branch branch = new Branch("master", init.getUid());
        branch.writeBranch();

        // Creates a new vertex in graph
        graph.addVertex(init.getUid());
        writeObject(GRAPH_FILE, (Serializable) graph);

        // Update Head pointer pointing to the initial commit
        updatePointer(branch);

    }

    /** Adds a copy of the file as it currently exists in the STAGING AREA. */
    public static void add(String filename) throws IOException {
        File fileCWD = join(CWD, filename);

        /** If FILE does not exist, program will just exit */
        Validate.NoFileExists(fileCWD);

        /** Read contents of a file from Working Directory */
        String contents = readContentsAsString(fileCWD);
        String version = sha1(contents);

        /** Loading StageAdd hashmap */
        stageAdd = readObject(STAGE_ADD_FILE, HashMap.class);
        stageRmv = readObject(STAGE_REMOVE_FILE, HashMap.class);

        /** Copy the contents of a file from Working directory into
         * Staging Area hashmap, STAGE_ADD */
        File versionPath = join(STAGE_DIR, version);
        Files.copy(fileCWD.toPath(), versionPath.toPath());
        stageAdd.put(filename, version);

        /** If the CWD version is identical to a tracked version in the commit,
         *      remove from staging area. */
        String head = readHeadCommitUID();
        Commit commit = fromFile(head);
        if (isFileTracked(commit, filename) && fileCWD.exists()) {
            if (commit.blobs.containsValue(version)) {

                /** if file is in the stage for addition, remove that file from
                 *      stage for removal */
                boolean inStageAdd = stageAdd.containsValue(version);
                boolean inStageRmv = stageRmv.containsValue(version);
                if (inStageAdd) {
                    stageRmv.remove(filename);
                }
                stageAdd.remove(filename);
            }
        }

        /** Writes changes on stage_add to a file */
        writeObject(STAGE_ADD_FILE, (Serializable) stageAdd);
        writeObject(STAGE_REMOVE_FILE, (Serializable) stageRmv);

    }

    /** Saves a snapshot of certain files in the current commit and staging
     * area so they can be restored at a later time, creating a new commit.
     * */
    public static void commit(String msg) throws IOException {
        /** Load the Stage maps */
        stageAdd = readObject(STAGE_ADD_FILE, HashMap.class);
        stageRmv = readObject(STAGE_REMOVE_FILE, HashMap.class);
        Validate.noFilesStages();
        commit(msg, null);
    }


    /** Commit method for Merge cases */
    public static void commit(String msg, String branchName) throws IOException {
        /** Read the commit object's contents that HEAD is referring to. */
        String head = readHeadCommitUID();
        Commit parentCommit = fromFile(head);
        Commit currentCommit = parentCommit.clone(msg, branchName);

        /** Use the staging area in order to update the files tracked by the current commit */
        Set<String> keys = stageAdd.keySet();
        for (String key : keys) {
            String stageValue = stageAdd.get(key);
            currentCommit.blobs.put(key, stageValue);
        }

        /** Use the staging are to remove the files from blobs to be tracked */
        Set<String> rmvKeys = stageRmv.keySet();
        Set<String> blobsKeys = currentCommit.blobs.keySet();
        for (String key : rmvKeys) {
            if (blobsKeys.contains(key)) {
                currentCommit.blobs.remove(key);
            }
        }

        /** Write the new commit with secondParent */
        currentCommit.writeCommit();

        /** Update the active pointers */
        Branch branch = new Branch(readHeadBranch(), currentCommit.getUid());
        updatePointer(branch);

        /** Move the files from Staging area directory to Blob directory after the Commit */
        Collection<String> versions = stageAdd.values();
        for (String v : versions) {
            File source = join(STAGE_DIR, v);
            File target = join(BLOBS_DIR, v);
            Files.move(source.toPath(), target.toPath());
        }

        /** Clears the stage and write the changes to a file */
        clearStage();
    }

    public static void rm(String filename) {
        /** Load the StageRemove map */
        stageAdd = readObject(STAGE_ADD_FILE, HashMap.class);
        stageRmv = readObject(STAGE_REMOVE_FILE, HashMap.class);
        boolean isTrackedOrStaged = false;

        /** Un-stage the file if file is staged for addition */
        if (stageAdd.containsKey(filename)) {
            isTrackedOrStaged = true;
            stageAdd.remove(filename);
        }

        /** If the file is tracked in the current commit, stage it for removal. */
        String head = readHeadCommitUID();
        Commit commit = fromFile(head);

        if (isFileTracked(commit, filename)) {
            isTrackedOrStaged = true;
            stageRmv.put(filename, commit.getUid());

            /** File will also be removed from the Working Directory if it exists */
            File file = join(CWD, filename);

            if (file.exists()) {
                file.delete();
            }
        }

        if (!isTrackedOrStaged) {
            Validate.noReasonToRmv();
        }

        /** Write changes to files */
        writeObject(STAGE_ADD_FILE, (Serializable) stageAdd);
        writeObject(STAGE_REMOVE_FILE, (Serializable) stageRmv);
    }

    public static void
        checkout(String[] args) throws IOException {
        String head = readHeadCommitUID();

        int numArgs = args.length;
        if (numArgs == 4) {
            String commitUID = args[1];
            String dashArg = args[2];
            String filename = args[3];

            Validate.withDash(dashArg);
            checkoutHelper(commitUID, filename);
        } else if (numArgs == 3) {
            String dashArg = args[1];
            String filename = args[2];

            Validate.withDash(dashArg);
            checkoutHelper(head, filename);
        } else if (numArgs == 2) {
            String branchName = args[1];
            List<String> branches = plainFilenamesIn(BRANCHES_DIR);

            if (!(branches.contains(branchName))) {
                Validate.noBranchExists();
            }
            if (branchName.equals(readHeadBranch())) {
                Validate.isCurrentBranch();
            }

            /** load stage maps */
            /** Load the StageRemove map */
            stageAdd = readObject(STAGE_ADD_FILE, HashMap.class);
            stageRmv = readObject(STAGE_REMOVE_FILE, HashMap.class);

            File branchFile = join(BRANCHES_DIR, branchName);
            String branchID = readContentsAsString(branchFile);
            Commit headCommit = fromFile(head);
            Commit branchCommit = fromFile(branchID);

            /** check if file can be overwritten. If any files are untracked,
             *      prints error and program exits */
            if (!canOverwrite(branchCommit)) {
                Validate.containsUntrackedFiles();
            } else {
                /** if files are not in checkout branch, delete it from CWD */
                List<String> filesCWD = plainFilenamesIn(CWD);
                Set<String> branchKeys = branchCommit.blobs.keySet();
                Set<String> headKeys = headCommit.blobs.keySet();

                for (String file : filesCWD) {
                    if (!branchKeys.contains(file)) {
                        File fileToDelete = join(CWD, file);
                        fileToDelete.delete();
                    }
                }

                /** if files are tracked in Head branch but are not in
                 *      in check out branch, delete them from CWD *?
                 */
                for (String file : headKeys) {
                    boolean inBranch = branchKeys.contains(file);
                    if (!inBranch) {
                        File fileToDelete = join(CWD, file);
                        fileToDelete.delete();
                    }
                }

                /** Update the active pointer and Head pointer */
                if (branchName.equals(Branch.SENTINEL)) {
                    branchName = "master";
                    rmBranch(Branch.SENTINEL);
                }
                Branch branch = new Branch(branchName, branchCommit.getUid());
                updatePointer(branch);

                /** If branch is not the Head branch, clear the stage */
                if (!branchName.equals(readHeadBranch())) {
                    clearStage();
                }
            }
        }

    }

    public static void log() {
        String commitUID = readHeadCommitUID();
        Commit commit = fromFile(commitUID);

        while (commit != null) {
            commit.printCommit();

            String parentUID = commit.getFirstParent();
            commit = fromFile(parentUID);
        }
    }

    public static void globalLog() {
        List<String> files = plainFilenamesIn(COMMIT_DIR);

        for (String file: files) {
            Commit commit = fromFile(file);
            commit.printCommit();
        }
    }

    public static void find(String message) {
        List<String> files = plainFilenamesIn(COMMIT_DIR);
        int match = 0;

        for (String file: files) {
            Commit commit = fromFile(file);
            int cmp = commit.getMessage().compareTo(message);
            if (cmp == 0) {
                System.out.println(commit.getUid());
                match++;
            }
        }
        if (match == 0) {
            Validate.noMessage();
        }
    }

    public static void status() {
        /** Prints Branches */
        System.out.println("=== Branches ===");
        List<String> files = plainFilenamesIn(BRANCHES_DIR);
        for (String file : files) {
            String active = readHeadBranch();
            if (file.equals(active)) {
                System.out.print("*");
            }
            System.out.println(file);
        }

        System.out.println();

        /** Prints Staged Files */
        System.out.println("=== Staged Files ===");

        stageAdd = readObject(STAGE_ADD_FILE, HashMap.class);
        Set<String> keys = stageAdd.keySet();

        for (String key : keys) {
            System.out.println(key);
        }

        System.out.println();

        /** Prints Removed Files */
        System.out.println("=== Removed Files ===");

        stageRmv = readObject(STAGE_REMOVE_FILE, HashMap.class);
        keys = stageRmv.keySet();

        for (String key : keys) {
            System.out.println(key);
        }

        System.out.println();

        /** Prints Removed Files */
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        /** Prints Untracked Files */
        System.out.println("=== Untracked Files ===");
        System.out.println();

    }

    public static void branch(String name) {
        File branchFile = join(BRANCHES_DIR, name);
        if (branchFile.exists()) {
            Validate.branchExists();
        }
        Branch branch = new Branch(name, readHeadCommitUID());
        branch.writeBranch();
    }

    public static void rmBranch(String name) {
        List<String> branches = plainFilenamesIn(BRANCHES_DIR);
        String currentBranch = readHeadBranch();

        //System.out.println(currentBranch);

        boolean notExist = true;

        for (String branch : branches) {
            int cmp = currentBranch.compareTo(name);
            if (cmp == 0) {
                Validate.notRemovable();
            }

            cmp = branch.compareTo(name);
            if (cmp == 0) {
                notExist = false;
                File file = join(BRANCHES_DIR, name);
                file.delete();
                break;
            }
        }

        if (notExist) {
            Validate.noBranchNameExists();
        }
    }

    public static void reset(String commitUID) throws IOException {
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        String fullID = getFullID(commitUID);
        if (!commits.contains(fullID)) {
            Validate.noCommitIDExists();
        }

        Branch temp = new Branch(Branch.SENTINEL, commitUID);
        temp.writeBranch();
        String[] args = {"checkout", temp.getName()};

        clearStage();
        checkout(args);
    }

    public static void merge(String branchName) throws IOException {

        // Validate if there are staged additions or removals
        stageAdd = readObject(STAGE_ADD_FILE, HashMap.class);
        stageRmv = readObject(STAGE_REMOVE_FILE, HashMap.class);

        if (!stageAdd.isEmpty() || !stageRmv.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        List<String> branches = plainFilenamesIn(BRANCHES_DIR);
        if (!branches.contains(branchName)) {
            Validate.noBranchExists();
        }

        if (branchName.equals(readHeadBranch())) {
            System.out.println("Cannot merge a branch with itself.");
        }


        // Find a splitpoint(), to get the blobs at the splitpoint
        File branchFile = join(BRANCHES_DIR, branchName);
        String splitPoint = splitPointFinder(branchName);

        // If splitPoint is the same commit as the given branch, do nothing
        String branchID = readContentsAsString(join(BRANCHES_DIR, branchName));
        if (splitPoint.equals(branchID)) {
            System.out.println("Given branch is an ancestor of the current branch");
            return;
        }

        // If split point is the current branch, checkout the given branch
        if (splitPoint.equals(readHeadCommitUID())) {
            String[] args = {"checkout", branchID};
            checkout(args);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        Commit commitSplit = fromFile(splitPoint);
        Commit commitHead = fromFile(readHeadCommitUID());
        Commit commitBranch = fromFile(readContentsAsString(branchFile));
        Map<String, String> splitBlobs = commitSplit.blobs;
        Map<String, String> headBlobs = commitHead.blobs;
        Map<String, String> branchBlobs = commitBranch.blobs;

        mergeHelper(splitBlobs, headBlobs, branchBlobs);

        String contents = String.format("Merged %s into %s.",
                branchName, readHeadBranch());
        commit(contents, branchName);
    }
}