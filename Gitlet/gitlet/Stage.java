package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** The staging area of Gitlet.
 * Contains information of all files that have been
 * added for committing, and removed.
 *
 * @author Dhruv Sirohi
 */
public class Stage implements Serializable {

    /** Map of files added for addition. */
    private HashMap<String, Blob> filesAdded;

    /** Map of files added for removal. */
    private HashSet<String> filesRemoved;

    /** Constructor. */
    Stage() {
        filesAdded = new HashMap<>();
        filesRemoved = new HashSet<>();
    }

    /** Add file for removal.
     *
     * @param file File being removed.
     */
    void addRemoval(File file) {
        filesRemoved.add(file.toString());
    }

    /** Add file for addition.
     *
     * @param name file being added.
     */
    void addFile(File name) {
        Blob blob = new Blob(name);
        filesAdded.put(name.toString(), blob);
    }

    /** Check if the stage contains this file.
     *
     * @param filename - name of the file
     * @return true/false
     */
    boolean contains(String filename) {
        return filesAdded.containsKey(filename)
                || filesRemoved.contains(filename);
    }

    /** Check if file with name name is added.
     *
     * @param name name of file
     * @return true/false
     */
    boolean containsAdded(String name) {
        return filesAdded.containsKey(name);
    }

    /** Check if file is added for removal.
     *
     * @param name - name of file
     * @return true/false
     */
    boolean containsRemoval(String name) {
        return filesRemoved.contains(name);
    }

    /** Remove the file from added list.
     *
     * @param filename name of file.
     */
    void removefromAdded(String filename) {
        if (filesAdded.containsKey(filename)) {
            filesAdded.remove(filename);
        }

    }

    /** Remove from list of removal.
     *
     * @param filename - removed file
     */
    void removefromRemoved(String filename) {
        filesRemoved.remove(filename);
    }

    /** Return added files.
     *
     * @return HashMap
     */
    HashMap<String, Blob> getAddedfiles() {
        return filesAdded;
    }

    /** Return removed files.
     *
     * @return HashMap
     */
    HashMap<String, Blob> getRemovedfiles() {
        return filesAdded;
    }
    /** Return removed files.
     *
     * @return HashMap
     */
    ArrayList<String> getRemovedfilenames() {
        ArrayList<String> p = new ArrayList<>();
        for (String s : filesRemoved) {
            p.add(s);
        }
        return p;
    }

    /** Return added filenames.
     *
     * @return ArrayList
     */
    ArrayList<String> getAddedfilenames() {
        return new ArrayList<String>(filesAdded.keySet());
    }

    /** Return some files.
     *
     * @return blobs
     */
    HashSet<Blob> getFiles() {
        HashSet<Blob> names = new HashSet<>();
        for (String f : filesAdded.keySet()) {
            names.add(filesAdded.get(f));
        }
        return names;
    }

    /** Check is stage is empty.
     *
     * @return boolean
     */
    boolean isEmpty() {
        return filesAdded.isEmpty();
    }

    /** Get file map.
     *
     * @return map
     */
    public HashMap<String, Blob> getFilemap() {
        return filesAdded;
    }

    /** Clear the staging area.
     */
    public void clear() {
        filesAdded = new HashMap<>();
        filesRemoved = new HashSet<>();
    }
}
