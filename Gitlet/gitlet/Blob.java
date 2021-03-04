package gitlet;
import java.io.File;
import java.io.Serializable;

/** A blob of a file.
 * Represents the contents of the file
 * as String and stores their name.
 *
 * @author Dhruv Sirohi
 */
public class Blob implements Serializable {

    /** Contents of the file. */
    private String filedata;
    /** Name of the file. */
    private String name;

    /** Empty constructor. */
    Blob() {

    }

    /** Constructor using file.
     *
     * @param file Reference file.
     */
    Blob(File file) {
        filedata = Utils.readContentsAsString(file);
        name = file.toString();
    }

    /** Return the contents of this file.
     *
     * @return File contents
     */
    String getFiledata() {
        return filedata;
    }

    /** Return the name of the file.
     *
     * @return name
     */
    String getFile() {
        return name;
    }

    /** Check if the given file is identical.
     *
     * @param blob - provided blob of file
     * @return true/false
     */
    boolean isSame(Blob blob) {
        return blob.getFiledata().equals(this.filedata);
    }
}
