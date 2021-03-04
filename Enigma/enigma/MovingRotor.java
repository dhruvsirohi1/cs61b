package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Dhruv Sirohi
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initially in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
    }

    @Override
    void advance() {
        this._setting += 1;
        this._setting = permutation().wrap(this._setting);
    }

    @Override
    boolean rotates() {
        return true;
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return _notches.indexOf(_alpha.toChar(setting())) >= 0;
    }

    /** String of my notches. */
    private String _notches;
}
