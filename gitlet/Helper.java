package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.*;

import static gitlet.Directory.*;
import static gitlet.Utils.*;
import static gitlet.Commit.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Helper {
    /** Check if a file has already been created. If it doesn't, it will create the File */
    public static void createNewFile(File file) {
        if (!(file.exists())) {
            try {
                if (file.createNewFile()) {
                    return;
                }
            } catch (IOException ex) {
                System.out.println("Oops, something went wrong, check " + file.toString());
            }
        }
    }

    /** Checks if a file is begin tracked by a given commit */
    public static boolean isFileTracked(Commit commit, String filename) {
        Set<String> keys = commit.blobs.keySet();
        for (String key : keys) {
            if (key.equals(filename)) {
                return true;
            }
        }
        return false;
    }

    public static void checkoutHelper(String commitID, String filename) throws IOException {
        String fullID = getFullID(commitID);
        List<String> commitIDs = plainFilenamesIn(COMMIT_DIR);

        // Checks if commit ID does not exist in the directory,
        if (fullID == null || !commitIDs.contains(fullID)) {
            Validate.noCommitIDExists();
        }

        Commit commit = fromFile(fullID);
        String version = (String) commit.blobs.get(filename);

        if (version == null) {
            Validate.noFileExistsInCommit();
        }
        File target = join(CWD, filename);
        File source = join(BLOBS_DIR, version);

        if (restrictedDelete(target)) {
            Files.copy(source.toPath(), target.toPath());
        } else {
            Validate.fileNotInCommit();
        }
    }

    /** Return the full commit ID */
    public static String getFullID(String uid) {
        List<String> list = plainFilenamesIn(COMMIT_DIR);
        int size = uid.length();

        for (String elem : list) {
            if (elem.substring(0, size).equals(uid)) {
                return elem;
            }
        }
        return uid;
    }

    public static void deleteStageFiles() {
        for (File file : STAGE_DIR.listFiles()) {
            if (file.isFile() && file.getParentFile().equals(STAGE_DIR)) {
                file.delete();
            }
        }
    }

    /** If file is successfully overwritten, return true. */
    public static boolean canOverwrite(Commit other) throws IOException {
        // get all the blob on head
        String headID = readHeadCommitUID();
        Commit headCommit = fromFile(headID);

        // get all blob on other
        Set<String> otherKeys = other.blobs.keySet();
        // get all files on cwd
        List<String> filesCWD = plainFilenamesIn(CWD);

        for (String key : otherKeys) {
            boolean inHead = headCommit.blobs.containsKey(key);
            boolean inOther = other.blobs.containsKey(key);
            boolean inCWD = filesCWD.contains(key);

            if (inCWD && inOther && !inHead) {
                return false;
            }
        }

        /** Copy the contents of that version to the CWD */
        for (String key : otherKeys) {
            File source = join(BLOBS_DIR, (String) other.blobs.get(key));
            File target = join(CWD, key);
            Files.copy(source.toPath(), target.toPath(), REPLACE_EXISTING);
        }

        return true;
    }

    public static void clearStage() {
        /** Clears the stage and write the changes to a file */
        deleteStageFiles();
        stageAdd.clear();
        stageRmv.clear();
        writeObject(STAGE_ADD_FILE, (Serializable) stageAdd);
        writeObject(STAGE_REMOVE_FILE, (Serializable) stageRmv);
    }

    public static String readHeadBranch() {
        Branch branch = readObject(HEAD, Branch.class);
        return branch.getName();
    }

    public static String readHeadCommitUID() {
        Branch branch = readObject(HEAD, Branch.class);
        return branch.getCommitUID();
    }

    public static String getBranchID(String name) {
        File source = join(BRANCHES_DIR, name);
        if (source.exists()) {
            return readContentsAsString(source);
        }
        return null;
    }

    public static void updatePointer(Branch branch) {
        String branchName = branch.getName();
        String branchUID = branch.getCommitUID();

        // write the new commit to the active pointer
        File file = join(BRANCHES_DIR, branchName);
        if (!file.exists()) {
            createNewFile(file);
        }
        writeContents(file, branchUID);

        writeObject(HEAD, branch);
    }


    public static String splitPointFinder(String branchName) {
        // Find all the paths from branch to initial commit
        Deque<String> paths1 = paths(branchName);

        // Find all the paths from active branch to initial
        Deque<String> paths2 = paths(readHeadBranch());

        return getLatest(paths1, paths2);
    }

    public static String getLatest(Deque<String> paths1, Deque<String> paths2) {
        int size = min(paths1.size(), paths2.size());
        String latestCommitID = readHeadCommitUID();
        for (int i = 0; i < size; i++) {
            String lastPath1 = paths1.getLast();
            String lastPath2 = paths2.getLast();
            if (lastPath1.equals(lastPath2)) {
                latestCommitID = lastPath1;
                paths1.removeLast();
                paths2.removeLast();
            } else {
                return latestCommitID;
            }
        }
        return latestCommitID;
    }

//    public static Deque<String> paths(String branchName) {
//        File file = join(BRANCHES_DIR, branchName);
//        String commitID;
//        if (file.exists()) {
//            commitID = readContentsAsString(file);
//            Commit commit = fromFile(commitID);
//
//            Deque<String> pathsList = new LinkedList<>();
//
//            while (commit != null) {
//                pathsList.add(commit.getUid());
//
//                String parentUID = commit.getFirstParent();
//                commit = fromFile(parentUID);
//            }
//            return pathsList;
//        }
//        return null;
//    }

    public static Deque<String> paths(String branchName) {
        File file = join(BRANCHES_DIR, branchName);
        graph = readObject(GRAPH_FILE, Graph.class);

        String commitID = readContentsAsString(file);
        return graph.bfs(commitID);
    }

    public static int min(int size1, int size2) {
        if (size1 < size2) {
            return size1;
        } else {
            return size2;
        }
    }

    public static void mergeHelper(Map<String, String> splitBlobs,
                                   Map<String, String> headBlobs,
                                   Map<String, String> branchBlobs) throws IOException {

        Set<String> files = fileCollector(splitBlobs.keySet(),
                headBlobs.keySet(), branchBlobs.keySet());

        for (String filename : files) {
            boolean inSplit = splitBlobs.containsKey(filename);
            boolean inBranch = branchBlobs.containsKey(filename);
            boolean inHead = headBlobs.containsKey(filename);

            /** If the same version of a file is in split and other, but
             *      different version is in head */
            if (inSplit && inHead && inBranch) {
                String splitVersion = splitBlobs.get(filename);
                String branchVersion = branchBlobs.get(filename);
                String headVersion = headBlobs.get(filename);

                /** Rule # 2 */
                if (splitVersion.equals(branchVersion) && !splitVersion.equals(headVersion)) {
                    File source = join(BLOBS_DIR, headVersion);
                    File target = join(CWD, filename);
                    Files.copy(source.toPath(), target.toPath());
                    continue;
                }

                /** Rule # 1: */
                if (splitVersion.equals(headVersion) && !splitVersion.equals(branchVersion)) {
                    File source = join(BLOBS_DIR, branchVersion);
                    File target = join(CWD, filename);
                    Files.copy(source.toPath(), target.toPath());
                    continue;
                }

                /** Rule # 3a */
                if (!splitVersion.equals(branchVersion) && branchVersion.equals(headVersion)) {
                    continue;
                }

                /** Rule # 3b */
                if (!splitVersion.equals(branchVersion) && !branchVersion.equals(headVersion)) {
                    continue;
                }
            }

            /** Checks first if the file is in CWD before deleting */
            List<String> filesCWD = plainFilenamesIn(CWD);
            boolean inCWD = filesCWD.contains(filename);

            /** Rule # 5 */
            if (!inSplit && inBranch && !inHead) {
                String branchVersion = branchBlobs.get(filename);
                File source = join(BLOBS_DIR, branchVersion);
                File target = join(CWD, filename);
                Files.copy(source.toPath(), target.toPath());
                continue;
            }

            if (inCWD) {
                /** Rule # 6 and Rule # 7*/
                if ((inSplit && !inBranch && inHead)
                        || (inSplit && inBranch && !inHead)) {
                    File fileCWD = join(CWD, filename);
                    fileCWD.delete();
                    continue;
                }

                /** Rule # 4 */
                if (!inSplit && !inBranch && inHead) {
                    String headVersion = headBlobs.get(filename);
                    File source = join(BLOBS_DIR, headVersion);
                    File target = join(CWD, filename);
                    Files.copy(source.toPath(), target.toPath());
                    continue;
                }
            }

        }
    }

    public static Set<String> fileCollector(Set<String> splitKs,
                                            Set<String> headKs,
                                            Set<String> branchKs) {

        Set<String> fileSet = new HashSet<>();
        for (String file : splitKs) {
            fileSet.add(file);
        }
        for (String file : headKs) {
            fileSet.add(file);
        }
        for (String file : branchKs) {
            fileSet.add(file);
        }
        return fileSet;

    }


}