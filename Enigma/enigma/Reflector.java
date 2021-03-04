package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a reflector in the enigma.
 *  @author Dhruv Sirohi
 */
class Reflector extends FixedRotor {

    /** A non-moving rotor named NAME whose permutation at the 0 setting
     * is PERM. */
    Reflector(String name, Permutation perm) {
        super(name, perm);
    }


    /** Return true iff I reflect. */
    boolean reflecting() {
        return true;
    }

    /** Tells the world if I am a derangement,
     * as I should be.
      * @return Boolean value
     */
    public boolean isDerangement() {
        return this.permutation().derangement();
    }


}
