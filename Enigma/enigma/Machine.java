package enigma;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

/** Class that represents a complete enigma machine.
 * What the heck is happening.
 *  @author Dhruv Sirohi
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) throws EnigmaException {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors;
        Iterator<Rotor> iter = _allRotors.iterator();
        _rotorMap = new HashMap<String, Rotor>();
        while (iter.hasNext()) {
            Rotor temp = iter.next();
            _rotorMap.put(temp.name(), temp);
        }
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        int numRotors = rotors.length;
        _usedRotors = new Rotor[numRotors];

        if (!(_rotorMap.containsKey(rotors[0]))) {
            throw new EnigmaException("Reflector not in config.");
        } else {
            _usedRotors[0] = _rotorMap.get(rotors[0]);
        }
        if (!(_usedRotors[0] instanceof  Reflector)) {
            throw new EnigmaException(" First rotor must be a reflector.");
        } else if (!_usedRotors[0].permutation().derangement()) {
            throw new EnigmaException("Reflector should be a derangement.");
        }

        for (int i = 1; i < numRotors; i++) {
            if (i >= numRotors() - numPawls()) {
                if (!(_rotorMap.containsKey(rotors[i]))) {
                    throw new EnigmaException("Rotor not in configuration");
                }
                _usedRotors[i] =  _rotorMap.get(rotors[i]);
                if (!(_usedRotors[i] instanceof MovingRotor)) {
                    throw new EnigmaException("Rotor number " + i
                            + " must be a Moving Rotor.");
                }
            } else {
                if (!(_rotorMap.containsKey(rotors[i]))) {
                    throw new EnigmaException("Rotor not in configuration");
                }
                _usedRotors[i] =  _rotorMap.get(rotors[i]);
                if (!(_usedRotors[i] instanceof FixedRotor)) {
                    throw new EnigmaException("Rotor number "
                            + i + " must be a Fixed Rotor.");
                }
            }

        }

        for (int i = 0; i < _usedRotors.length; i++) {
            for (int j = i + 1; j < _usedRotors.length; j++) {
                if (_usedRotors[i].name().compareTo(_usedRotors[j].name())
                        == 0) {
                    throw new EnigmaException("Rotors"
                            + " cannot be repeated in the machine.");
                }
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != numRotors() - 1) {
            throw new EnigmaException("Invalid number of settings.");
        } else {
            for (int i = 1; i <= setting.length(); i++) {
                Alphabet alpha = _usedRotors[i]._alpha;
                _usedRotors[i].set(alpha.toInt(setting.charAt(i - 1)));
            }
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugBoard = plugboard;
    }

    /** Checks if the conditions are satisfied
     * for a  moving rotor to rotate.
     * Then rotates it.
     * @param r - list of rotors in machine.
     */
    void checkRotate(Rotor[] r) {
        int[] moveCheck = new int[r.length];
        moveCheck[r.length - 1] = 1;
        for (int k = r.length - 2; k >= numRotors() - numPawls(); k--) {
            if (r[k + 1].atNotch() && r[k].rotates()) {
                moveCheck[k] = 1;
            } else {
                moveCheck[k] = 0;
            }
        }
        for (int k = 1; k < r.length - 1; k++) {
            if (moveCheck[k - 1] == 1) {
                moveCheck[k] = 1;
            }
        }
        for (int j = r.length - 1; j > 0; j--) {
            if (moveCheck[j] == 1) {
                r[j].advance();
            }
        }
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine.
     * @param c - index of letter to convert.
     *  */
    int convert(int c) {
        List<Rotor> rotorList = Arrays.asList(_usedRotors);
        int j = 0;
        int nextVal = _plugBoard.permute(c);
        checkRotate(_usedRotors);
        while (j < 2) {
            Collections.reverse(Arrays.asList(_usedRotors));
            Iterator<Rotor> iter = rotorList.iterator();
            if (j == 1) {
                iter.next();
            }

            while (iter.hasNext()) {

                Rotor rotor = iter.next();

                if (j == 0) {
                    nextVal = rotor.convertForward(nextVal);
                } else {
                    nextVal = rotor.convertBackward(nextVal);
                }
            }
            j++;
        }
        return _plugBoard.permute(nextVal);
    }

    /** Sets the ring setting according to Ringstellung input.
     * @param s - String of settigs.
     * */
    void stellungSet(String s) {
        int i = 0;
        while (i < s.length()) {
            _usedRotors[i + 1].updateRing(s.charAt(i));
            i++;
        }
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) throws IOException {
        _readMsg = new StringReader(msg);
        String result = "";
        while (true) {
            int c = _readMsg.read();
            if (c == -1) {
                break;
            }
            char ch = (char) c;

            if (_alphabet.contains(ch)) {
                int convertedVal = convert(_alphabet.toInt(ch));
                result += _alphabet.toChar(convertedVal);
            } else {
                throw new EnigmaException("'" + ch + "'" + " not in alphabet.");
            }
        }
        return result;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Numer of rotors in this machine. */
    private int _numRotors;

    /** Number of pawls AKA moving rotors in this machine. */
    private int _pawls;

    /** A collection of all available rotors, acquired
     * from config file.
     */
    private Collection<Rotor> _allRotors;

    /** An array of all used rotors. Acts as an independent
     * part of the whole.
     */
    private Rotor[] _usedRotors;

    /** Mapping of Name(String) --> Rotor(Rotor). */
    private Map<String, Rotor> _rotorMap;

    /** Permutation of the plugboard. */
    private Permutation _plugBoard;

    /** Reader for converting the message one
     * character at a time.
     */
    private Reader _readMsg;
}
