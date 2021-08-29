package gitlet;

import java.io.File;

import static gitlet.Directory.*;

public class Validate {
    public static void numArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands");
            System.exit(0);
        }
    }

    public static void numArgs(String[] args, int start, int end) {
        if (args.length < start || args.length > end) {
            System.out.println("Incorrect operands");
            System.exit(0);
        }

    }

    public static void command(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command");
            System.exit(0);
        }
    }

    public static void noExistingCmd() {
        System.out.println("No command with the name exists.");
        System.exit(0);
    }

    public static void initialization() {
        if (!(GITLET_DIR.exists())) {
            System.out.println("Not in an initialized Gitlet directory");
            System.exit(0);
        }
    }

    public static void reInit() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists"
                    + " " + "in the current directory.");
            System.exit(0);
        }
    }

    public static void NoFileExists(File file) {
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
    }

    public static void noFileExistsInCommit() {
        System.out.println("File does not exist in that commit.");
        System.exit(0);

    }

    public static void noFilesStages() {
        if (stageAdd.isEmpty() && stageRmv.isEmpty()) {
            System.out.println("No changes added to the commit");
            System.exit(0);
        }
    }

    public static void noCommitMessage(String[] args) {
        if (args.length == 1 || args[1].isEmpty()) {
            System.out.println("Please enter a commit message");
            System.exit(0);
        }
    }

    public static void withDash(String arg) {
        String dash = "--";
        if (!arg.equals(dash)) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void fileNotInCommit() {
        System.out.println("File does not exist in that commit");
        System.exit(0);
    }

    public static void noCommitIDExists() {
        System.out.println("No commit with that id exists.");
        System.exit(0);
    }

    public static void noBranchExists() {
        System.out.println("No such branch exists.");
        System.exit(0);
    }

    public static void noBranchNameExists() {
        System.out.println("A branch with that name does not exist.");
        System.exit(0);
    }

    public static void isCurrentBranch() {
        System.out.println("No need to checkout the current branch");
        System.exit(0);
    }

    public static void noMessage() {
        System.out.println("Found no commit with that message.");
        System.exit(0);
    }

    public static void notRemovable() {
        System.out.println("Cannot remove the current branch.");
        System.exit(0);
    }

    public static void noReasonToRmv() {
        System.out.println("No Reason to remove the file.");
        System.exit(0);
    }

    public static void branchExists() {
        System.out.println("A branch with that name already exists.");
    }

    public static void containsUntrackedFiles() {
        System.out.println("There is an untracked file in the way;"
                + " delete it, or add and commit it first.");
        System.exit(0);
    }

}



