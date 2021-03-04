package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Stack;
import java.util.HashSet;
import java.util.Date;
import java.util.ArrayList;
import java.io.IOException;

/** A commit. Stores all the relevant info,
 * not in the form of pointers. Handles adding new
 * commits (by creating them). Generates the SHA here.
 *
 * @author Dhruv Sirohi
 */
public class Commit implements Serializable {

    /** Date.  */
    private Date date;
    /** The timestamp of this commit. */
    private String timestamp;
    /** Commit ID for this commit. */
    protected String commitID;
    /** Commit message. */
    protected String displayName;
    /** Branch this belongs to. */
    protected String branchName;
    /** If this is where they diverge. */
    private Boolean divergence;
    /** List of parent Commit IDs. */
    private ArrayList<String> parentSha;
    /** List of parents. Unused. */
    protected ArrayList<Commit> parent;
    /** List of branches this commit belongs to. */
    protected HashSet<String> branchNames;
    /** Data. Unused. */
    protected HashMap<String, byte[]> data;
    /** A single blob of file information. */
    private Blob blob;
    /** Merged message, if this is a merge commit. */
    private String merged;
    /** True if this is a result of a merge. */
    private boolean merger;
    /** File data mapped by their names. */
    private HashMap<String, Blob> blobs;


    /** Constructor. */
    Commit() {
        date = new Date();
        String s = String.format("%1$ta %1$tb %1$td %1$tT %1$tY %1$tz", date);
        timestamp = s;
        merger = false;
        merged = "";
        branchName = "master";
        parent = null;
        data = new HashMap<>();
        blob = new Blob();
        divergence = false;
        commitID = Utils.sha1("What");
        branchNames = new HashSet<>();
        branchNames.add("master");
        branchNames = new HashSet<>();
        blobs = new HashMap<>();
        parentSha = new ArrayList<>();
    }


    /** Copy constructor.
     *
     * @param commit - commit to be copied from
     */
    Commit(Commit commit) {
        timestamp = commit.getTime();
        commitID = commit.getSHA();
        branchNames = new HashSet<>();
        parentSha = new ArrayList<>();
        blobs = new HashMap<>();
        merger = false;
        merged = "";
        for (String name : commit.blobs.keySet()) {
            blobs.put(name, commit.blobs.get(name));
        }
    }

    /** Constructor for remote.
     *
     * @param commit - Copying commit
     * @param bool - addition for overloading
     */
    Commit(Commit commit, boolean bool) {
        timestamp = commit.getTime();
        commitID = commit.getSHA();
        branchNames = new HashSet<>();
        displayName = commit.getMsg();
        branchNames.addAll(commit.getBrancheNames());
        parentSha = new ArrayList<>();
        parentSha.addAll(commit.getParents());
        blobs = new HashMap<>();
        merger = false;
        merged = "";
        for (String name : commit.blobs.keySet()) {
            blobs.put(name, commit.blobs.get(name));
        }
    }

    /** Return timestamp.
     *
     * @return String
     */
    public String getTime() {
        return timestamp;
    }

    /** Return parents.
     *
     * @return Array
     */
    public ArrayList<String> getParents() {
        return parentSha;
    }

    /** Return branches.
     *
     * @return HashSet
     */
    public HashSet<String> getBrancheNames() {
        return branchNames;
    }

    /**
     * Initial commit settings.
     */
    void initDate() {
        date = new Date(0);
        String s = String.format("%1$ta %1$tb %1$td %1$tT %1$tY %1$tz", date);
        timestamp = s;
        commitID = Utils.sha1("initial commit");
        displayName = "initial commit";
        parentSha.add("");
        branchNames.add("master");
    }

    /** Remove a tracked file.
     *
     * @param name - name of removed file
     */
    public void untrack(String name) {
        blobs.remove(name);
    }

    /**
     * Add a commit.
     * @param currbranch - Current branch name
     * @param msg - commit message
     * @param parentId - parent SHA
     * @param stage - staging area
     */
    void updateCommit(Stage stage, String msg,
                      String parentId, String currbranch) {
        HashMap<String, Blob> filemap = stage.getAddedfiles();
        date = new Date();
        String s = String.format("%1$ta %1$tb %1$td %1$tT %1$tY %1$tz", date);
        timestamp = s;
        parentSha.add(parentId);
        displayName = msg;
        branchNames.add(currbranch);
        HashSet<String> newfiles = new HashSet<>();
        for (String name : filemap.keySet()) {
            if (blobs.containsKey(name)) {
                if (!(blobs.get(name).getFiledata().
                        equals(filemap.get(name).getFiledata()))) {
                    this.blobs.replace(name, filemap.get(name));
                    newfiles.add(filemap.get(name).getFiledata());
                }
            } else {
                this.blobs.put(name, filemap.get(name));
                newfiles.add(filemap.get(name).getFiledata());
            }
        }
        this.commitID = Utils.sha1((Object) Utils.serialize(this));
    }

    /**
     * Committing after a merge.
     * @param stage - Staging area
     * @param branch - Current branch file
     * @param current - Merging branch file
     */
    void mergeCommit(Stage stage, File current, File branch) {
        String currid = Utils.readContentsAsString(current);
        String bid = Utils.readContentsAsString(branch);
        HashMap<String, Blob> filemap = stage.getAddedfiles();
        parentSha.add(currid);
        parentSha.add(bid);
        String bname = "";
        if (branch.getName().contains("-")) {
            bname = branch.getName().replaceAll("-", "/");
            displayName = "Merged " + bname + " into "
                    + current.getName() + ".";
        } else {
            displayName = "Merged " + branch.getName() + " into "
                    + current.getName() + ".";
        }
        merger = true;
        merged = currid.substring(0, 7) + " " + bid.substring(0, 7);
        branchNames.add(branch.getName());
        branchNames.add(current.getName());
        for (String name : filemap.keySet()) {
            if (blobs.containsKey(name)) {
                if (!blobs.get(name).getFiledata().
                        equals(filemap.get(name).getFiledata())) {
                    this.blobs.replace(name, filemap.get(name));
                }
            } else {
                this.blobs.put(name, filemap.get(name));
            }
        }
        this.commitID = Utils.sha1((Object)
                Utils.serialize(this));
    }


    /**
     * Print all ancestors of this commit. (log)
     */
    public void printLog() {
        Commit temp = this;
        while (!(temp.parentSha.get(0)).equals("")) {
            System.out.println("===");
            System.out.println("commit " + temp.commitID);
            if (temp.merger) {
                System.out.println("Merge: " + temp.merged);
            }
            System.out.println("Date: " + temp.timestamp);
            System.out.println(temp.displayName + "\n");
            File parentfile = Utils.join
                    (Main._logs.toString(), temp.parentSha.get(0));
            temp = Utils.readObject(parentfile, Commit.class);
        }
        System.out.println("===");
        System.out.println("commit " + temp.commitID);
        System.out.println("Date: " + temp.timestamp);
        System.out.println(temp.displayName + "\n");
    }

    /**
     * Used every time a commit is made to modify GLOBAL_LOG file.
     * @param file - GLOBAL_LOG
     */
    public void addLog(File file) {
        String s = Utils.readContentsAsString(file);
        if (merger) {
            Utils.writeContents(file, "===" + "\n"
                    + "commit " + commitID + "\n"
                    + "Merge: " + merged + "\n"
                    + "Date: " + timestamp + "\n"
                    + displayName + "\n\n" + s);
        } else {
            Utils.writeContents(file, "===" + "\n"
                    + "commit " + commitID + "\n"
                    + "Date: " + timestamp + "\n"
                    + displayName + "\n\n" + s);
        }
    }

    /**
     * Get the message of this commit. Used in find.
     * @return commit message
     */
    public String getMsg() {
        return displayName;
    }

    /**
     * Return the SHA of this commit.
     * @return the commit ID
     */
    String getSHA() {
        return commitID;
    }

    /**
     * Restore a file. (checkout)
     * @param filename - Name of the file to be restored.
     * @return A blob
     */
    Blob restoreFile(String filename) {
        if (blobs.containsKey(filename)) {
            return blobs.get(filename);
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
            return null;
        }
    }

    /**
     * Add a branch.
     * @param bname - branch name.
     */
    public void addBranch(String bname) {
        branchNames.add(bname);
    }


    /**
     * Check if file is being tracked in this commit.
     * @param filename - name of file being checked.
     * @return if this file is being tracked by this commit.
     */
    boolean tracking(String filename) {
        return blobs.containsKey(filename);
    }

    /**
     * Checks if files have identical content.
     * @param file - compared file
     * @return same or not
     */
    boolean isIdentical(File file) {
        return (blobs.get(file.toString()).
                getFiledata()).equals(Utils.readContentsAsString(file));
    }

    /**
     * Returns blobs.
     * @return A hashmap of blobs.
     */
    HashMap<String, Blob> getBlobs() {
        return blobs;
    }

    /**
     * Return contents of the branch.
     * @return A hashmap
     */
    HashMap<String, Blob> restoreBranch() {
        return blobs;
    }

    /** The lowest common ancestor. */
    private static Commit lowestCommonAncestor;

    /** Get the history of the given branch. Used
     * in merge.
     * @param history - HashSet to be filled.
     */
    void getHistory(HashSet<String> history) {
        if (history.contains(this.commitID)) {
            return;
        }

        history.add(this.commitID);
        for (String s : this.parentSha) {
            if (!s.equals("")) {
                File parentfile = Utils.join(Main._logs.toString(), s);
                Commit temp = Utils.readObject(parentfile, Commit.class);
                temp.getHistory(history);
            }
        }
    }

    /**
     * Find latest common ancestor. (merge)
     * @param branchhistory - commits to be compared with
     * @param branchname  - name of the branch
     * @return The best commit found
     */
    public Commit closestCommon(String branchname,
                                HashSet<String> branchhistory) {
        int ignore = this.findlowestCommonAncestor(branchname,
                branchhistory, 0, Integer.MAX_VALUE);
        return lowestCommonAncestor;
    }

    /** Find the lowestCommonAncestor for the merge.
     *
     * @param bname - branch name
     * @param branchcommits - all commits that have branch path
     * @param dist - distance from current branch head
     * @param prevbest - best distance yet
     * @return integer disyance
     */
    public int findlowestCommonAncestor(String bname,
                                        HashSet<String> branchcommits,
                       int dist, int prevbest) {
        if (branchcommits.contains(this.commitID) && dist < prevbest) {
            lowestCommonAncestor = this;
            return dist;
        }
        int newdistance = dist;
        for (String parentId : this.parentSha) {
            if (!parentId.equals("")) {
                File parentCommit = Utils.join(Main._logs.toString(), parentId);
                Commit temp = Utils.readObject(parentCommit, Commit.class);
                newdistance = temp.findlowestCommonAncestor(bname,
                        branchcommits, dist + 1, prevbest);
                if (newdistance < prevbest) {
                    prevbest = newdistance;
                }
            }
        }
        return newdistance;
    }

    /** Find all the commits to be pushed to remote, iff
     * the remote head is in the current branch.
     * @param id - remote head commit SHA
     * @param list - Stack
     * @return found id in log
     */
    public boolean pushCommits(String id, Stack<Commit> list) {
        Commit temp = new Commit(this, true);
        boolean found = false;
        while (!temp.parentSha.get(0).equals("")) {
            if (temp.commitID.equals(id)) {
                found = true;
                break;
            }
            list.push(temp);
            File parentfile = Utils.join
                    (Main._logs.toString(), temp.parentSha.get(0));
            temp = new Commit(Utils.readObject(parentfile, Commit.class),
                    true);
        }
        if (!found) {
            if (temp.commitID.equals(id)) {
                found = true;
            }
        }
        return found;
    }

    /** Appends all the commits to the remote after pushing.
     *
     * @param list - Stack of commits
     * @param branch  - branch name
     * @return Head Commit
     */
    public Commit appendCommits(Stack<Commit> list, String branch) {
        Commit temp = this;
        HashMap<String, Blob> initialtrack = temp.getBlobs();
        while (!list.empty()) {
            Commit next = list.pop();
            String sha = next.getSHA();
            File newlog = Utils.join(Main._logs.toString(), sha);
            try {
                newlog.createNewFile();
            } catch (IOException ignored) {
                System.out.println("IOError append.");
            }
            next.parentSha = new ArrayList<>();
            next.branchNames = new HashSet<>();
            String previouscommit = temp.getSHA();
            next.parentSha.add(0, previouscommit);
            next.branchNames.add(branch);
            Utils.writeObject(newlog, next);
            next.addLog(Main._globallog);
            temp = next;
        }
        boolean changed = false;
        for (String name : initialtrack.keySet()) {
            if (!temp.getBlobs().containsKey(name)) {
                temp.blobs.put(name, initialtrack.get(name));
                changed = true;
            }
        }
        if (changed) {
            File logplace = Utils.join(Main._logs.toString(), temp.getSHA());
            Utils.writeObject(logplace, temp);
        }
        return temp;
    }

    /** Get the full history of this commit.
     *
     * @param stack - fill this up.
     */
    public void getFullHistory(Stack<Commit> stack) {
        Commit temp = new Commit(this, true);
        while (!temp.parentSha.get(0).equals("")) {
            stack.push(temp);
            File parentfile = Utils.join
                    (Main._logs.toString(), temp.parentSha.get(0));
            temp = new Commit(Utils.readObject(parentfile, Commit.class),
                    true);
        }
    }

}





