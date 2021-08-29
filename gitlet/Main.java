package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Irvin Tancioco
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        Validate.command(args);

        String firstArg = args[0];
        switch(firstArg) {
            /** 1. Calls the repository set up initializing method which creates a .gitlet directory */
            case "init":
                Validate.numArgs(args, 1);
                Validate.reInit();
                Repository.init();
                break;
            case "add":
                Validate.numArgs(args, 2);
                Validate.initialization();
                String filename = args[1];
                Repository.add(filename);
                break;
            case "commit":
                Validate.noCommitMessage(args);
                Validate.initialization();
                String message = args[1];
                Repository.commit(message);
                break;
            case "rm":
                Validate.numArgs(args, 2);
                Validate.initialization();
                filename = args[1];
                Repository.rm(filename);
                break;
            case "log":
                Validate.numArgs(args, 1);
                Validate.initialization();
                Repository.log();
                break;
            case "global-log":
                Validate.numArgs(args, 1);
                Validate.initialization();
                Repository.globalLog();
                break;
            case "find":
                Validate.numArgs(args, 2);
                Validate.initialization();
                message = args[1];
                Repository.find(message);
                break;
            case "status":
                Validate.numArgs(args, 1);
                Validate.initialization();
                Repository.status();
                break;
            case "checkout":
                Validate.numArgs(args, 2,4);
                Validate.initialization();
                Repository.checkout(args);
                break;
            case "branch":
                Validate.numArgs(args, 2);
                Validate.initialization();
                String name = args[1];
                Repository.branch(name);
                break;
            case "rm-branch":
                Validate.numArgs(args, 2);
                Validate.initialization();
                name = args[1];
                Repository.rmBranch(name);
                break;
            case "reset":
                Validate.numArgs(args, 2);
                Validate.initialization();
                name = args[1];
                Repository.reset(name);
                break;
            case "merge":
                Validate.numArgs(args, 2);
                Validate.initialization();
                name = args[1];
                Repository.merge(name);
                break;
            default:
                Validate.noExistingCmd();
        }
    }
}
