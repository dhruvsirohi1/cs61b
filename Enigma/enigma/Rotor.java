package enigma;
import java.util.ArrayList;
import java.util.List;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Dhruv Sirohi
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;

        _alpha = new Alphabet(_permutation.getAlpha());
        _alphaList = new ArrayList<Character>(_alpha.size());
        _setting = 0;
        _pointer = this.setting();
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return _permutation.wrap(_setting);
    }

    /** Set setting() to POSN. */
    void set(int posn) {
        posn = _permutation.wrap(posn);
        _setting = posn;
        _pointer += _setting;
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        if (_alpha.contains(cposn)) {
            set(_alpha.toInt(cposn));
        } else {
            throw new EnigmaException("Invalid setting value.");
        }
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        int posn = _permutation.wrap(p);
        int input = (posn + setting()) % _alpha.size();
        int result = _permutation.permute(input);
        result = (result - setting()) % _alpha.size();
        if (result < 0) {
            return _alpha.size() + result;
        } else {
            return result;
        }
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        int posn = _permutation.wrap(e);
        int input = (posn + setting()) % _alpha.size();
        int result = _permutation.invert(input);
        result = (result - setting()) % _alpha.size();
        if (result < 0) {
            return _alpha.size() + result;
        } else {
            return result;
        }
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Updates Ring according to Ringstellung
     * settiing.
     * @param ch - new 0 posn
     */
    void updateRing(char ch) {
        this._alpha.updateRing(ch);
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** This rotors setting. */
    protected int _setting;

    /** This rotors personal Alphabet. */
    protected Alphabet _alpha;

    /** This rotors personal list of all letters in
     * alphabet.
     */
    protected List<Character> _alphaList;



    /** A useless variable to soothe my inner monster. */
    protected int _pointer;
}
