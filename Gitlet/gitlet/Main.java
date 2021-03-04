package gitlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Dhruv Sirohi
 */
public class Main {

    /** Main metadata folder. */
    private static File _cwd = new File(".");

    /** A file. */
    private static File _gitfolder = Utils.join(_cwd.toString(), ".gitlet");

    /** A file. */
    private static File _commitfile =
            Utils.join(_gitfolder.toString(), "commits");

    /** A file. */
    private static File _remotefolder =
            Utils.join(_gitfolder.toString(), "remotes");
    /** A file. */
    private static File _stagingarea =
            Utils.join(_gitfolder.toString(), "staging area");
    /** A file. */
    protected static File _globallog =
            Utils.join(_gitfolder.toString(), "GLOBAL LOG");
    /** A file. */
    private static File _master =
            Utils.join(_gitfolder.toString(), "master");
    /** A file. */
    private static File _logmap =
            Utils.join(_gitfolder.toString(), "log map");

    /** A file. */
    private static File _branches =
            Utils.join(_gitfolder.toString(), "_branches");
    /** A file. */
    private static File _initial =
            Utils.join(_gitfolder.toString(), "initial");

    /** A file. */
    private static File _head =
            Utils.join(_gitfolder.toString(), "_head");

    /** A file. */
    private static File _active =
            Utils.join(_gitfolder.toString(), "_active");

    /** A file. */
    private static File _current =
            Utils.join(_gitfolder.toString(), "current");

    /** A file. */
    protected static File _logs =
            Utils.join(_gitfolder.toString(), "_logs");


    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        validateDirectory(args);
        checkCases(args);
    }

    /** Check the arguments for actions.
     *
     * @param args - arguments
     */
    public static void checkCases(String[] args) {
        switch (args[0]) {
        case "init":
            initialize(args);
            break;
        case "add":
            add(args);
            break;
        case "commit":
            commits(args);
            break;
        case "rm":
            remove(args);
            break;
        case "log":
            getLog(args);
            break;
        case "global-log":
            globalLog(args);
            break;
        case "find":
            gitFind(args);
            break;
        case "status":
            status(args);
            break;
        case "checkout":
            checkout(args);
            break;
        case "branch":
            branch(args);
            break;
        case "rm-branch":
            removeBranch(args);
            break;
        case "reset":
            reset(args);
            break;
        case "merge":
            merge(args);
            break;
        case "add-remote":
            remote(args);
            break;
        case "rm-remote":
            removeRemote(args);
            break;
        case "push":
            push(args);
            break;
        case "fetch":
            fetch(args);
            break;
        case "pull":
            pull(args);
            break;
        default:
            noCase();
        }
    }

    /** Check if an initialized Gitlet exists.
     * @param args -arguments
     */
    public static void validateDirectory(String[] args) {
        if (!args[0].equals("init") && !_gitfolder.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
    /** Initial exit.
     */
    public static void noCase() {
        System.out.println("No command with that name exists.");
        System.exit(0);
    }
    /** The common object of Commit used by
     * each method, if required.
     */
    private static Commit commit;

    /** Common object of Stage. */
    private static Stage staging;

    /** Initialize a Gitlet directory.
     *
     * @param args - arguments
     */
    public static void initialize(String[] args) {
        validateNumArgs("init", args, 1);
        if (!_gitfolder.exists()) {
            Boolean existence = _gitfolder.mkdir();
            try {
                Boolean[] commitence = {_commitfile.createNewFile(),
                        _globallog.createNewFile(),
                        _stagingarea.createNewFile(),
                        _master.createNewFile(),
                        _head.createNewFile(),
                        _initial.createNewFile(),
                        _remotefolder.mkdir(),
                        _branches.createNewFile(),
                        _logs.mkdir(),
                        _current.createNewFile(),
                        _active.createNewFile()};
                Utils.writeContents(_active, "master");
                Utils.writeContents(_branches, "master" + "\n");
            } catch (IOException e) {
                System.out.println("IO");
            }
            commit = new Commit();
            commit.initDate();
            Commit nextCommit = new Commit();
            staging = new Stage();
            commit.addLog(_globallog);
            Utils.writeContents(_initial, commit.getSHA());
            Utils.writeObject(_current, nextCommit);
            Utils.writeContents(_head, commit.getSHA());
            Utils.writeContents(_master, commit.getSHA());
            File ref1 = Utils.join(_logs, commit.getSHA());
            try {
                ref1.createNewFile();
            } catch (IOException e) {
                System.out.println("IOError init.");
            }
            Utils.writeObject(ref1, commit);
            Utils.writeObject(_stagingarea, staging);
        } else {
            System.out.println("A Gitlet version-control"
                    + " system already exists in the current directory.");
            System.exit(0);
        }
    }

    /** Add a file to the stage.
     *
     * @param args - arguments
     */
    public static void add(String[] args) {
        validateNumArgs("add", args, 2);
        String filename = args[1];
        File file = new File(filename);
        String id = Utils.readContentsAsString(_head);
        File headcommit = Utils.join(_logs.toString(), id);
        staging = Utils.readObject(_stagingarea, Stage.class);
        if (staging.containsRemoval(filename)) {
            staging.clear();
            Utils.writeObject(_stagingarea, staging);
            return;
        }
        if (file.exists()) {
            if (headcommit.exists()) {
                commit = Utils.readObject(headcommit, Commit.class);
                if (commit.tracking(filename) && commit.isIdentical(file)) {
                    staging.removefromAdded(filename);
                } else {
                    staging.addFile(file);
                }
            }
            Utils.writeObject(_stagingarea, staging);
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
    }

    /** Add a commit.
     *
     * @param args - arguments
     */
    public static void commits(String[] args) {
        validateCommitArgs("commit", args, 2);
        staging = Utils.readObject(_stagingarea, Stage.class);
        String headId = Utils.readContentsAsString(_head);
        Commit head = Utils.readObject(Utils.join(_logs.toString(), headId),
                Commit.class);
        commit = Utils.readObject(_current, Commit.class);
        if (!staging.isEmpty() || !staging.getRemovedfiles().isEmpty()
                || !head.getBlobs().equals(commit.getBlobs())) {
            commit.updateCommit(staging, args[1],
                    Utils.readContentsAsString(_head),
                    Utils.readContentsAsString(_active));
            Utils.writeContents(_head, commit.getSHA());
            String active = Utils.readContentsAsString(_active);
            File activeHead = Utils.join(_gitfolder.toString(), active);
            Utils.writeContents(activeHead, commit.getSHA());
            commit.addLog(_globallog);
            File thiscommit = Utils.join(_logs.toString(), commit.getSHA());
            try {
                thiscommit.createNewFile();
            } catch (IOException e) {
                System.out.println("IO commit.");
            }
            Utils.writeObject(thiscommit, commit);
            Commit nextCommit = new Commit(commit);
            Utils.writeObject(_current, nextCommit);
            staging.clear();
            Utils.writeContents(_head, commit.getSHA());
            Utils.writeObject(_stagingarea, staging);
        } else {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
    }

    /** Remove a file.
     *
     * @param args - arguments
     */
    public static void remove(String[] args) {
        validateNumArgs("rm", args, 2);
        staging = Utils.readObject(_stagingarea, Stage.class);
        File current = Utils.join(_logs.toString(),
                Utils.readContentsAsString(_head));
        commit = Utils.readObject(current, Commit.class);
        HashMap<String, Blob> filesmap = commit.getBlobs();
        if (staging.contains(args[1]) || filesmap.containsKey(args[1])) {
            File deletable = new File(args[1]);
            staging.removefromAdded(args[1]);
            Commit next = Utils.readObject(_current, Commit.class);
            next.untrack(args[1]);
            Utils.writeObject(_current, next);
            if (commit.tracking(args[1])) {
                staging.addRemoval(deletable);
                if (deletable.exists()) {
                    deletable.delete();
                }
            }
            Utils.writeObject(_stagingarea, staging);
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    /** Print the log/history of the current head commit.
     *
     * @param args - arguments
     */
    public static void getLog(String[] args) {
        validateNumArgs("log", args, 1);
        File current = Utils.join(_logs.toString(),
                Utils.readContentsAsString(_head));
        commit = Utils.readObject(current, Commit.class);
        commit.printLog();
    }

    /** Get the complete log of gitlet.
     *
     * @param args - arguments
     */
    public static void globalLog(String[] args) {
        validateNumArgs("global-log", args, 1);
        System.out.println(Utils.readContentsAsString(_globallog));
    }

    /** Find the commits with this commit msg.
     *
     * @param args - arguments
     */
    public static void gitFind(String[] args) {
        validateNumArgs("find", args, 2);
        File dir = new File(_logs.toString());
        boolean found = false;
        Commit temp;
        File[] allFiles = dir.listFiles();
        if (allFiles != null) {
            for (File file : allFiles) {
                temp = Utils.readObject(file, Commit.class);
                if (temp.getMsg().equals(args[1])) {
                    System.out.println(temp.getSHA());
                    found = true;
                }
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** Display the state of Gitlet.
     *
     * @param args - arguments
     */
    public static void status(String[] args) {
        validateNumArgs("status", args, 1);
        staging = Utils.readObject(_stagingarea, Stage.class);
        ArrayList<String> addedfiles = staging.getAddedfilenames();
        ArrayList<String> removedfilenames = staging.getRemovedfilenames();
        System.out.println("=== Branches ===");
        printBranches();
        System.out.println("=== Staged Files ===");
        Collections.sort(addedfiles);
        for (String s : addedfiles) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        Collections.sort(removedfilenames);
        for (String file : removedfilenames) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        String id = Utils.readContentsAsString(_head);
        File commitfile = Utils.join(_logs.toString(), id);
        Commit com = Utils.readObject(commitfile, Commit.class);
        HashSet<String> printed = new HashSet<>();
        HashMap<String, Blob> comfiles = com.getBlobs();
        for (String filename : comfiles.keySet()) {
            File checkfile = new File((filename));
            if (!(checkfile).exists()
                    && !staging.containsRemoval(filename)) {
                System.out.println(filename + " (deleted)");
                printed.add(filename);
            } else if (checkfile.exists()) {
                if (!staging.containsAdded(filename)
                        && !comfiles.get(filename).getFiledata().equals(Utils.
                                readContentsAsString(new File(filename)))) {
                    System.out.println(filename + " (modified)");
                    printed.add(filename);
                }
            }
        }
        HashMap<String, Blob> stagefiles = staging.getAddedfiles();
        for (String filename : stagefiles.keySet()) {
            File currstate = new File(filename);
            if (currstate.exists()) {
                if (!stagefiles.get(filename).getFiledata().
                        equals(Utils.readContentsAsString(currstate))) {
                    if (!printed.contains(filename)) {
                        System.out.println(filename + " (modified)");
                    }
                }
            } else {
                if (!printed.contains(filename)) {
                    System.out.println(filename + " (deleted)");
                }
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        printUntracked(staging, comfiles);
    }

    /** Print untracked Files.
     *
     * @param stage - Stage
     * @param com - commit map
     */
    private static void printUntracked(Stage stage, HashMap<String, Blob> com) {
        List<String> untracked = Utils.plainFilenamesIn(_cwd);
        HashMap<String, Blob> staged = stage.getAddedfiles();
        if (untracked != null) {
            for (String name : untracked) {
                if (!com.containsKey(name) && !staged.containsKey(name)) {
                    System.out.println(name);
                } else if (stage.containsRemoval(name)
                        && (new File(name)).exists()) {
                    System.out.println(name);
                }
            }
        }
        System.out.println();
    }

    /** Print out the branches for status.
     */
    private static void printBranches() {
        System.out.println("*" + Utils.readContentsAsString(_active));
        ArrayList<String> blist = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(_branches));
            String line = reader.readLine();
            while (line != null) {
                if (!line.equals(Utils.readContentsAsString(_active))
                        && !line.equals("")) {
                    blist.add(line);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println("IOError print Branch.");
        }
        Collections.sort(blist);
        for (String s : blist) {
            System.out.println(s);
        }
        System.out.println();
    }
    /** This method has been borrowed by stackOverflow user Sach,
     * edited by Felix Bohnacker.
     * @param cwd - current working directory
     * @return File names
     */
    public static ArrayList<String> getAllfiles(File cwd) {
        File[] filelist = cwd.listFiles();
        ArrayList<String> filenames = new ArrayList<>();
        for (File f : filelist) {
            filenames.add(f.toString());
        }
        return filenames;
    }

    /** Overwrite a file.
     *
     * @param file - File to be written.
     * @param restorefile - Restoring data.
     */
    private static void writeFile(File file, Blob restorefile) {
        if (file.exists()) {
            Utils.writeContents(file, (Object) restorefile.getFiledata());
        } else {
            try {
                file.createNewFile();
                Utils.writeContents(file, (Object) restorefile.getFiledata());
            } catch (IOException e) {
                System.out.println("Writing restore.");
            }
        }
    }

    /** Normal SHA length. */
    private static final int COMMIT_ID_LENGTH = 40;
    /** Checkout length for commit and file. */
    private static final int CHECKOUT_FROM_COMMIT = 4;

    /** Checkout data. Works in three ways.
     *      a) checkout -- [filename]:
     *      checkout file from head commit.
     *
     *      b) checkout [commit id] -- [filename]:
     *      checkout the file from that commit.
     *
     *      c) checkout [branch]:
     *      checks out the whole branch, and set's it as
     *      the current branch.
     * @param args - arguments
     */
    public static void checkout(String[] args) {
        validateCheckout(args);
        String head = Utils.readContentsAsString(_head);
        staging = Utils.readObject(_stagingarea, Stage.class);
        if (args[1].equals("--")) {
            File file = new File(args[2]);
            File commitplace = Utils.join(_logs.toString(), head);
            commit = Utils.readObject(commitplace, Commit.class);
            Blob restoring = commit.restoreFile(args[2]);
            writeFile(file, restoring);
        } else if (args.length == CHECKOUT_FROM_COMMIT
                && args[2].equals("--")) {
            String id = args[1];
            if (id.length() < COMMIT_ID_LENGTH) {
                commit = findCommit(id);
            } else {
                File commitplace = Utils.join(_logs.toString(), id);
                if (!commitplace.exists()) {
                    System.out.println("No commit with that id exists.");
                    System.exit(0);
                }
                commit = Utils.readObject(commitplace, Commit.class);
            }
            Blob restored = commit.restoreFile(args[3]);
            File file = new File(args[3]);
            writeFile(file, restored);
        } else {
            branchRestore(args[1]);
            staging.clear();
        }
        Utils.writeObject(_stagingarea, staging);
    }

    /** Restore the branch.
     *
     * @param branch - branch name
     */
    private static void branchRestore(String branch) {
        String branchname = branch.replace('/', '-');
        File branchfile = Utils.join(_gitfolder.toString(), branchname);
        File headfile = Utils.join(_logs.toString(),
                Utils.readContentsAsString(_head));
        Commit curr = Utils.readObject(headfile, Commit.class);
        if (!branchfile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        } else if (Utils.readContentsAsString(_active).equals(branchname)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        String branchid = Utils.readContentsAsString(branchfile);
        File commitfile = Utils.join(_logs.toString(), branchid);
        commit = Utils.readObject(commitfile, Commit.class);
        HashMap<String, Blob> restored = commit.restoreBranch();
        for (String name : restored.keySet()) {
            File file = new File(name);
            Blob data = restored.get(name);
            if (file.exists()) {
                if (!curr.getBlobs().containsKey(name)
                        && !Utils.
                        readContentsAsString(file).equals(data.getFiledata())) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    System.exit(0);
                }
                Utils.writeContents(file, (Object) data.getFiledata());
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    System.out.println("Could not replace the file.");
                    System.exit(0);
                }
                Utils.writeContents(file, (Object) data.getFiledata());
            }
        }

        Commit newCom = new Commit(commit);
        newCom.addBranch(branch);
        Utils.writeObject(_current, newCom);
        for (String files : curr.getBlobs().keySet()) {
            if (!restored.containsKey(files)) {
                File deleting = new File(files);
                if (deleting.exists()) {
                    deleting.delete();
                }
            }
        }
        Utils.writeContents(_head, branchid);
        Utils.writeContents(_active, branchname);
    }
    /** Find the commit from partial SHA.
     *
     * @param partialcommit - partial SHA
     * @return - Commit
     */
    static Commit findCommit(String partialcommit) {
        List<String> filenames = Utils.plainFilenamesIn(_logs);
        for (String name : filenames) {
            if (partialcommit.equals(name.substring(0,
                    partialcommit.length()))) {
                File returnfile = Utils.join(_logs.toString(), name);
                return Utils.readObject(returnfile, Commit.class);
            }
        }
        System.out.println("No commit with that id exists.");
        System.exit(0);
        return null;
    }

    /** Add a branch to the head commit.
     *
     * @param args - arguments
     */
    public static void branch(String[] args) {
        validateNumArgs("branch", args, 2);
        String head = (Utils.readContentsAsString(_head));
        File commitfile = Utils.join(_logs.toString(), head);
        commit = Utils.readObject(commitfile, Commit.class);
        File branch = Utils.join(_gitfolder.toString(), args[1]);
        if (branch.exists()) {
            System.out.println("A branch with this name already exists.");
            System.exit(0);
        } else {
            try {
                branch.createNewFile();
                commit.addBranch(args[1]);
                Utils.writeContents(branch, head);
                String addBranch = Utils.readContentsAsString(_branches);
                Utils.writeContents(_branches, addBranch + args[1] + "\n");
                Utils.writeObject(commitfile, commit);
            } catch (IOException e) {
                System.out.println("Couldn't create new branch");
                System.exit(0);
            }
        }
    }


    /** Add a branch to the head commit.
     *
     * @param args - arguments
     */
    public static void initBranch(String[] args) {
        validateNumArgs("branch", args, 2);
        String head = (Utils.readContentsAsString(_initial));
        File commitfile = Utils.join(_logs.toString(), head);
        commit = Utils.readObject(commitfile, Commit.class);
        File branch = Utils.join(_gitfolder.toString(), args[1]);
        if (branch.exists()) {
            System.out.println("A branch with this name already exists.");
            System.exit(0);
        } else {
            try {
                branch.createNewFile();
                commit.addBranch(args[1]);
                Utils.writeContents(branch, head);
                String addBranch = Utils.readContentsAsString(_branches);
                Utils.writeContents(_branches, addBranch + args[1] + "\n");
                Utils.writeObject(commitfile, commit);
            } catch (IOException e) {
                System.out.println("Couldn't create new branch");
                System.exit(0);
            }
        }
    }

    /** Remove a branch. Doesn't change ANYTHING else.
     *
     * @param args - arguments
     */
    public static void removeBranch(String[] args) {
        validateNumArgs("rm", args, 2);
        if (args[1].equals(Utils.readContentsAsString(_active))) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        String branches = Utils.readContentsAsString(_branches);
        File branchfile = Utils.join(_gitfolder.toString(), args[1]);
        if (branchfile.exists()) {
            String update = branches.replaceAll(args[1] + "\n", "");
            Utils.writeContents(_branches, update);
            branchfile.delete();
        } else {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    /** Used to pass style. Checks if file exists.
     * @param file - file
     */
    private static void checkThisFile(File file) {
        if (!file.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
    }
    /** Reset to the branch given.
     *
     * @param args - arguments
     */
    public static void reset(String[] args) {
        validateNumArgs("reset", args, 2);
        String id = args[1];
        staging = Utils.readObject(_stagingarea, Stage.class);
        if (id.length() < COMMIT_ID_LENGTH) {
            commit = findCommit(id);
        } else {
            File commitplace = Utils.join(_logs.toString(), id);
            checkThisFile(commitplace);
            commit = Utils.readObject(commitplace, Commit.class);
        }
        String fullId = commit.getSHA();
        HashMap<String, Blob> commitFiles = commit.getBlobs();
        File currhead = Utils.join(_logs.toString(),
                Utils.readContentsAsString(_head));
        Commit currCommit = Utils.readObject(currhead, Commit.class);
        HashMap<String, Blob> trackedFiles = currCommit.getBlobs();
        String currBranch = Utils.readContentsAsString(_active);
        File branchHead = Utils.join(_gitfolder.toString(), currBranch);
        commit.addBranch(currBranch);
        ArrayList<File> deletables = new ArrayList<>();
        for (String file : trackedFiles.keySet()) {
            if (!commitFiles.containsKey(file)) {
                File removal = new File(file);
                if (removal.exists()) {
                    deletables.add(removal);
                }
            }
        }
        for (String file : commitFiles.keySet()) {
            File restore = new File(file);
            if (restore.exists()) {
                if (!trackedFiles.containsKey(file)
                        && !Utils.readContentsAsString(restore).
                        equals(commitFiles.get(file).getFiledata())) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    System.exit(0);
                }
                Utils.writeContents(restore,
                        commitFiles.get(file).getFiledata());
            } else {
                try {
                    restore.createNewFile();
                    Utils.writeContents(restore,
                            commitFiles.get(file).getFiledata());
                } catch (IOException e) {
                    System.out.println("IOError reset.");
                }
            }
        }
        Utils.writeContents(branchHead, fullId);
        deleteReset(deletables);
        Utils.writeContents(_head, fullId);
        File newcomid = Utils.join(_logs.toString(), fullId);
        Utils.writeObject(newcomid, commit);
        staging.clear();
        Utils.writeObject(_stagingarea, staging);
    }

    /** Delete for reset.
     * @param list - file list
     */
    public static void deleteReset(ArrayList<File> list) {
        for (File file : list) {
            file.delete();
        }
    }
    /** Handle the event of merge.
     *
     * @param args - arguments
     */
    public static void merge(String[] args) {
        validateNumArgs("merge", args, 2);
        String head = Utils.readContentsAsString(_head);
        boolean conflict = false;
        File branchfile = Utils.join(_gitfolder.toString(), args[1]);
        File currBranch = Utils.join(_gitfolder.toString(),
                Utils.readContentsAsString(_active));
        staging = Utils.readObject(_stagingarea, Stage.class);
        checkStageEmpty();
        if (branchfile.exists()) {
            if (Utils.readContentsAsString(_active).equals(args[1])) {
                System.out.println("Cannot merge branch with itself.");
                System.exit(0);
            }
            File commitfile = Utils.join(_logs.toString(), head);
            commit = Utils.readObject(commitfile, Commit.class);
            File branch = Utils.join(_logs.toString(),
                    Utils.readContentsAsString(branchfile));
            Commit branchcommit = Utils.readObject(branch, Commit.class);
            HashSet<String> branchhistory = new HashSet<>();
            branchcommit.getHistory(branchhistory);
            Commit lca = commit.closestCommon(args[1], branchhistory);
            if (lca.getSHA().equals(Utils.readContentsAsString(branchfile))) {
                System.out.println("Given branch is an ancestor "
                        + "of the current branch.");
                System.exit(0);
            } else if (lca.getSHA().equals(Utils.
                    readContentsAsString(currBranch))) {
                checkout(args);
                System.out.println("Current branch fast-forwarded.");
                System.exit(0);
            }
            HashMap<String, Blob> mapBranch = branchcommit.getBlobs();
            HashMap<String, Blob> mapCurr = commit.getBlobs();
            HashMap<String, Blob> mapSplit = lca.getBlobs();
            conflict = resolveMerge(mapBranch, mapCurr, mapSplit);
            stageCheck();
            commit = Utils.readObject(_current, Commit.class);
            commit.mergeCommit(staging, currBranch, branchfile);
            commit.addLog(_globallog);
            staging.clear();
            File newlog = Utils.join(_logs.toString(), commit.getSHA());
            Utils.writeContents(currBranch, commit.getSHA());
            Utils.writeContents(_head, commit.getSHA());
            createLog(newlog);
            Utils.writeObject(newlog, commit);
            Utils.writeObject(_stagingarea, staging);
            Commit nextCommit = new Commit(commit);
            Utils.writeObject(_current, nextCommit);
            if (conflict) {
                System.out.println("Encountered a merge conflict.");
            }
        } else {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    /** Create the new log.
     *
     * @param file - file
     */
    public static void createLog(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("IOError merge.");
        }
    }

    /** Check if commit is redundant. */
    public static void stageCheck() {
        if (staging.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);

        }
    }

    /** Check if stage is empty. */
    private static void checkStageEmpty() {
        if (!staging.getAddedfiles().isEmpty()
                || !staging.getRemovedfiles().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }
    /** Resolve all the conditions of the merge.
     *
     * @param branchMap - map of merging branch.
     * @param currMap - map of current branch.
     * @param splitPoint - map of split point.
     * @return true if detected merge conflict.
     */
    private static boolean resolveMerge(HashMap<String, Blob> branchMap,
                                        HashMap<String, Blob> currMap,
                                        HashMap<String, Blob> splitPoint) {
        boolean conflicting = false;
        ArrayList<File> deletables = new ArrayList<>();
        for (String filename : splitPoint.keySet()) {
            if (currMap.containsKey(filename)) {
                if (branchMap.containsKey(filename)) {
                    if (currMap.get(filename).isSame(splitPoint.get(filename))
                            && !branchMap.get(filename).
                            isSame(splitPoint.get(filename))) {
                        restore(filename, branchMap.get(filename));
                        staging.addFile(new File(filename));
                    } else if (!currMap.get(filename).
                            isSame(branchMap.get(filename))
                            && !branchMap.get(filename).isSame(splitPoint.
                            get(filename))) {
                        File conflicted = new File(filename);
                        mergeDisplay(conflicted, currMap.get(filename),
                                branchMap.get(filename));
                        conflicting = true;
                        staging.addFile(conflicted);
                    }
                } else if (currMap.get(filename).
                        isSame(splitPoint.get(filename))) {
                    staging.removefromAdded(filename);
                    File deleting = new File(filename);
                    deletables.add(deleting);
                } else {
                    currConflict(new File(filename), currMap.get(filename));
                    conflicting = true;
                }
            } else if (branchMap.containsKey(filename) && !branchMap.
                    get(filename).isSame(splitPoint.get(filename))) {
                absentConflict(new File(filename), branchMap.get(filename));
                conflicting = true;
            }
        }
        for (String filename : branchMap.keySet()) {
            if (!currMap.containsKey(filename)
                    && !splitPoint.containsKey(filename)) {
                checkDelete(new File(filename), branchMap.get(filename));
                restore(filename, branchMap.get(filename));
                staging.addFile(new File(filename));
            } else if (!splitPoint.containsKey(filename)
                    && !currMap.get(filename).getFiledata().
                    equals(branchMap.get(filename).getFiledata())) {
                File conflicted = new File(filename);
                mergeDisplay(conflicted, currMap.get(filename),
                        branchMap.get(filename));
                conflicting = true;
                staging.addFile(conflicted);
            }
        }
        mergeDelete(deletables);
        return conflicting;
    }

    /** Delete the files after a merge.
     * @param list - file list
     */
    public static void mergeDelete(ArrayList<File> list) {
        for (File file : list) {
            mergeRemove(file.getName());
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /** Display merge conflict.
     *
     * @param file file
     * @param curr - current blob
     * @param branch - branch blob
     */
    public static void mergeDisplay(File file, Blob curr, Blob branch) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Merge display.");
            }
        }
        Utils.writeContents(file, "<<<<<<< HEAD" + "\n"
                + curr.getFiledata()
                + "=======" + "\n"
                + branch.getFiledata()
                + ">>>>>>>" + "\n");
    }
    /** Untracking and staging a file for removal.
     *
     * @param file - file name
     */
    public static void mergeRemove(String file) {
        staging.addRemoval(new File(file));
        Commit next = Utils.readObject(_current, Commit.class);
        next.untrack(file);
        Utils.writeObject(_current, next);
    }
    /** Conflict with branch file deleted.
     * @param file - file
     * @param blob - blob
     */
    public static void currConflict(File file, Blob blob) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("IOError delete display.");
            }
        }
        Utils.writeContents(file, "<<<<<<< HEAD" + "\n"
                + blob.getFiledata()
                + "=======" + "\n"
                + ""
                + ">>>>>>>" + "\n");
        staging.addFile(file);
    }

    /** Conflict with current file deleted.
     * @param file - file
     * @param blob - blob
     */
    public static void absentConflict(File file, Blob blob) {
        if (!file.exists()) {
            mergeDisplay(file, blob);
        } else {
            System.out.println("There is an untracked file"
                    + " in the way; delete it, or add and commit "
                    + "it first.");
            System.exit(0);
        }
        staging.addFile(file);
    }

    /** Display conflict.
     * @param file - file
     * @param branch - branch
     */
    public static void mergeDisplay(File file, Blob branch) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("IOError delete display.");
        }
        Utils.writeContents(file, "<<<<<<< HEAD" + "\n"
                + ""
                + "=======" + "\n"
                + branch.getFiledata()
                + ">>>>>>>" + "\n");
    }
    /** Check if the deleting file is a bad one.
     * @param file - file
     * @param blob - blob
     */
    private static void checkDelete(File file, Blob blob) {
        if (file.exists()) {
            if (!Utils.readContentsAsString(file).equals(blob.getFiledata())) {
                System.out.println("There is an untracked file"
                        + " in the way; delete it, or add and commit "
                        + "it first.");
                System.exit(0);
            }
        }
    }
    /** Restore a file.
     *
     * @param filename - name of file.
     * @param blob - data
     */
    static void restore(String filename, Blob blob) {
        File file = new File(filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("IOError restore.");
            }
        }
        Utils.writeContents(file, blob.getFiledata());
    }

    /** Handle remote directory.
     *
     * @param args - arguments
     */
    private static void remote(String[] args) {
        validateNumArgs("add-remote", args, 3);
        String rmname = args[1];
        File remotefile = Utils.join(_gitfolder.toString(), rmname);
        String path = args[2].replaceAll("/", File.separator);
        path = path.substring(0, path.length() - 7);
        if (!remotefile.exists()) {
            try {
                remotefile.createNewFile();
            } catch (IOException e) {
                System.out.println("Add remote.");
            }
            Utils.writeContents(remotefile, path);
        } else {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }
    }

    /** Remove the remote directory information.
     *
     * @param args - arguments
     */
    private static void removeRemote(String[] args) {
        validateNumArgs("rm-remote", args, 2);
        String remotename = args[1];
        File remotefile = Utils.join(_gitfolder.toString(), remotename);
        if (remotefile.exists()) {
            remotefile.delete();
        } else {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
    }

    /** Get location of remote directory.
     *
     * @param file - arguments
     * @return String location
     */
    static String getLocation(File file) {
        return Utils.readContentsAsString
                (file);
    }

    /** Push the current ahead commits to the remote branch.
     *
     * @param args - arguments
     */
    private static void push(String[] args) {
        validateNumArgs("push", args, 3);
        String remotename = args[1];
        String remotebranch = args[2];
        File remotefile = Utils.join(_gitfolder.toString(), remotename);
        if (!remotefile.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        String location = getLocation(remotefile);
        getRemoteFiles(location);
        String remotehead = Utils.readContentsAsString(_head);
        File branchfile = Utils.join(_gitfolder.toString(), remotebranch);
        boolean branchexists = branchfile.exists();
        if (branchexists) {
            String rmbranchhead = Utils.readContentsAsString(branchfile);
            reinitializeFiles();
            Stack<Commit> pushingcommits = new Stack<>();
            String localhead = Utils.readContentsAsString(_head);
            commit = Utils.readObject(Utils.join(_logs.toString(),
                    localhead), Commit.class);
            boolean found = commit.pushCommits(rmbranchhead, pushingcommits);
            if (!found) {
                System.out.println("Please pull down remote "
                        + "changes before pushing.");
                System.exit(0);
            }
            getRemoteFiles(location);
            commit = Utils.readObject(Utils.join(_logs.toString(),
                    rmbranchhead), Commit.class);
            Commit newBranchHead = commit.appendCommits(pushingcommits,
                    remotebranch);
            Commit nextcommit = new Commit(newBranchHead);
            Utils.writeContents(_head, newBranchHead.getSHA());
            Utils.writeContents(branchfile, newBranchHead.getSHA());
            Utils.writeObject(_current, nextcommit);
            reinitializeFiles();
        } else {
            reinitializeFiles();
            Stack<Commit> pushingcommits = new Stack<>();
            String localhead = Utils.readContentsAsString(_head);
            commit = Utils.readObject(Utils.join(_logs.toString(),
                    localhead), Commit.class);
            commit.getFullHistory(pushingcommits);
            getRemoteFiles(location);
            Commit remoteHead = Utils.readObject(Utils.join(_logs.toString(),
                    remotehead), Commit.class);
            remoteHead.addBranch(remotebranch);
            Commit branchhead = remoteHead.appendCommits(pushingcommits,
                    remotebranch);
            createRemBranch(remotebranch, branchhead);
            reinitializeFiles();
        }
    }

    /** Create the branch at the remote Gitlet.
     * @param branch - branch name
     * @param head - head commit
     */
    public static void createRemBranch(String branch, Commit head) {
        String branches = Utils.readContentsAsString(_branches);
        Utils.writeContents(_branches, branches + branch + "\n");
        File newbranch = Utils.join(_gitfolder.toString(), branch);
        try {
            newbranch.createNewFile();
        } catch (IOException ignored) {
            System.out.println("IOError push.");
        }
        Utils.writeContents(newbranch, head.getSHA());
    }

    /** Create the branch at the local Gitlet.
     * @param branch - branch name
     * @param remote - remote name
     */
    public static void addRemBranch(String remote, String branch) {
        File rem = Utils.join(_gitfolder.toString(), remote);
        File rembranch = Utils.join(rem.toString(), branch);
        if (!rembranch.exists()) {
            try {
                rembranch.createNewFile();
            } catch (IOException e) {
                System.out.println("Error push");
            }
        }
    }
    /** Fetch from remote repo.
     *
     * @param args - arguments
     */
    private static void fetch(String[] args) {
        validateNumArgs("fetch", args, 3);
        boolean localexistence = false;
        String localbhead = "";
        String remotename = args[1];
        String rbranch = args[2];
        String localbranch = remotename + "-" + rbranch;
        File remote = Utils.join(_gitfolder.toString(), remotename);
        if (!remote.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        File lbranch = Utils.join(_gitfolder.toString(), localbranch);
        if (lbranch.exists()) {
            localexistence = true;
            localbhead = Utils.readContentsAsString(lbranch);
        }
        String location = getLocation(remote);
        getRemoteFiles(location);
        File rmbranch = Utils.join(_gitfolder.toString(), rbranch);
        if (!rmbranch.exists()) {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }
        String branchid = Utils.readContentsAsString(rmbranch);
        File branchcommit = Utils.join(_logs.toString(), branchid);
        Commit rmCom = Utils.readObject(branchcommit, Commit.class);
        Stack<Commit> newcommits = new Stack<>();
        if (localexistence) {
            rmCom.pushCommits(localbhead, newcommits);
        } else {
            rmCom.getFullHistory(newcommits);
        }
        reinitializeFiles();
        if (!localexistence) {
            String[] branchargs = {"branch", localbranch};
            initBranch(branchargs);
            String localhead = Utils.readContentsAsString(_initial);
            Commit headCom = Utils.readObject(Utils.join(_logs.toString(),
                    localhead), Commit.class);
            Commit newHead = headCom.appendCommits(newcommits, localbranch);
            addRemoteBranch(lbranch, newHead.getSHA());
        } else {
            String comId = Utils.readContentsAsString(lbranch);
            Commit bHead = Utils.readObject(Utils.join(_logs.toString(), comId),
                    Commit.class);
            Commit newHead = bHead.appendCommits(newcommits, localbranch);
            if (Utils.readContentsAsString(_active).equals(localbranch)) {
                Utils.writeContents(_head, newHead.getSHA());
                Commit next = new Commit(newHead);
                Utils.writeObject(_current, next);
            }
            Utils.writeContents(lbranch, newHead.getSHA());
        }
    }

    /** Add the remote branch.
     * @param file - file
     * @param id - sha
     */
    private static void addRemoteBranch(File file, String id) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("Remote branch fetch.");
        }
        Utils.writeContents(file, id);
    }
    /** Update the head if necessary.
     *
     * @param branch - branch appended/added
     * @param com - head commit
     */
    public static void checkHead(String branch, Commit com) {
        String active = Utils.readContentsAsString(_active);
        if (active.equals(branch)) {
            Utils.writeContents(_head, com.getSHA());
            Commit next = new Commit(com);
            Utils.writeObject(_current, next);
        }
    }

    /** Write the commit sha to remote branch.
     *
     * @param branchname - branch
     * @param remote - remote dir
     * @param sha - id
     */
    private static void writeRemoteBranch(String branchname, File remote,
                                          String sha) {
        File remotebranch = Utils.join(remote.toString(), branchname);
        if (!remotebranch.exists()) {
            try {
                remotebranch.createNewFile();
            } catch (IOException ignore) {
                System.out.println("IOError write.");
            }
        }
        Utils.writeContents(remotebranch, sha);
    }

    /** Pull from remote.
     *
     * @param args - arguments
     */
    private static void pull(String[] args) {
        validateNumArgs("pull", args, 3);
        String remotename = args[1];
        String remotebranch = args[2];
        String localbranch = args[1] + "-" + args[2];
        String[] fetchargs = {"fetch", remotename, remotebranch};
        fetch(fetchargs);
        String[] mergeargs = {"merge", localbranch};
        merge(mergeargs);
    }

    /** Reinitialize files so remote data will be available.
     *
     * @param path - path of remote
     */
    private static void getRemoteFiles(String path) {

        _cwd = new File(path);

        if (!_cwd.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }


        _gitfolder = Utils.join(_cwd.toString(), ".gitlet");

        _commitfile =
                Utils.join(_gitfolder.toString(), "commits");

        _initial =
                Utils.join(_gitfolder.toString(), "initial");

        _stagingarea =
                Utils.join(_gitfolder.toString(), "staging area");

        _globallog =
                Utils.join(_gitfolder.toString(), "GLOBAL LOG");

        _remotefolder =
                Utils.join(_gitfolder.toString(), "remotes");

        _master =
                Utils.join(_gitfolder.toString(), "master");

        _branches =
                Utils.join(_gitfolder.toString(), "_branches");

        _head =
                Utils.join(_gitfolder.toString(), "_head");

        _active =
                Utils.join(_gitfolder.toString(), "_active");

        _current =
                Utils.join(_gitfolder.toString(), "current");

        _logs =
                Utils.join(_gitfolder.toString(), "_logs");
    }

    /** Reinitialize files to local.
     */
    public static void reinitializeFiles() {

        _cwd = new File(".");

        _gitfolder = Utils.join(_cwd.toString(), ".gitlet");

        _commitfile =
                Utils.join(_gitfolder.toString(), "commits");

        _stagingarea =
                Utils.join(_gitfolder.toString(), "staging area");

        _globallog =
                Utils.join(_gitfolder.toString(), "GLOBAL LOG");

        _initial =
                Utils.join(_gitfolder.toString(), "initial");

        _remotefolder =
                Utils.join(_gitfolder.toString(), "remotes");

        _master =
                Utils.join(_gitfolder.toString(), "master");

        _branches =
                Utils.join(_gitfolder.toString(), "_branches");

        _head =
                Utils.join(_gitfolder.toString(), "_head");

        _active =
                Utils.join(_gitfolder.toString(), "_active");

        _current =
                Utils.join(_gitfolder.toString(), "current");

        _logs =
                Utils.join(_gitfolder.toString(), "_logs");


    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param cmd Name of command you are validating
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /** Validate checkout arguments.
     *
     * @param args - arguments
     */
    public static void validateCheckout(String[] args) {
        if (args.length < 2 || args.length > 4) {
            System.out.println("Incorrect Operands");
            System.exit(0);
        }
        if (args[1].equals("--") && args.length < 3) {
            System.out.println("Incorrect Operands.");
            System.exit(0);
        }
        if (args.length == CHECKOUT_FROM_COMMIT && !args[2].equals("--")) {
            System.out.println("Incorrect Operands.");
            System.exit(0);
        }
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect Operands.");
                System.exit(0);
            }
        }
    }

    /** Validate arguments for Commit.
     *
     * @param cmd - command
     * @param args - arguments
     * @param n - number of arguments
     */
    public static void validateCommitArgs(String cmd, String[] args, int n) {
        if (args.length > n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else if (args[0].equals("commit") && args.length == 1
                || args[1].isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        } else if (args.length < n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }

    }
}
