package enigma;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Dhruv Sirohi
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _map = new HashMap<Character, Character>();
        _invMap = new HashMap<Character, Character>();
        _scanCycles = new Scanner(cycles);
        _scanCycles.useDelimiter("\\)");
        while (_scanCycles.hasNext()) {
            String temp = _scanCycles.next();
            String holder = trimCycle(temp);
            addCycle(holder);
        }
    }

    /** "Trims" cycle, or removes the parentheses
     * for ease of reading.
     * @param cycle - string containing parentheses.
     * @return Cycle without ()
     */
    private String trimCycle(String cycle) {
        String temp = cycle;
        int teller = 0;
        int j = 0;
        while (j < cycle.length()) {
            char ch = cycle.charAt(j);
            if (ch == '(' && teller != 0) {
                throw new EnigmaException("Invalid permutation");
            } else if (ch == '(' && teller == 0) {
                teller++;
            }
            if (ch == ')') {
                teller = 0;
            }
            j++;
        }

        temp = temp.replaceAll("[\\[\\](){}]|\\s*", "");
        return temp;
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        for (int i = 0; i < cycle.length(); i++) {
            char temp = cycle.charAt(i);
            if (_map.containsKey(cycle.charAt(i))) {
                throw new EnigmaException("Cycle letter repeated.");
            }
            if (!_alphabet.contains(cycle.charAt(i))
                    || cycle.chars().filter(ch -> ch == temp).count() > 1) {
                throw new EnigmaException("Permutation"
                        + " letter not in Alphabet.");
            }
            if (i == cycle.length() - 1) {
                _map.put(cycle.charAt(i), cycle.charAt(0));
            } else {
                _map.put(cycle.charAt(i), cycle.charAt(i + 1));
            }
        }
        int k = cycle.length() - 1;
        int j;
        for (j = k; j >= 0; j--) {
            if (j == 0) {
                _invMap.put(cycle.charAt(j), cycle.charAt(k));
            } else {
                _invMap.put(cycle.charAt(j), cycle.charAt(j - 1));
            }
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char result;
        int posn = p % _alphabet.size();
        posn = wrap(posn);
        if (_map.containsKey(_alphabet.toChar(posn))) {
            result = _map.get(_alphabet.toChar(posn));
            return _alphabet.toInt(result);
        } else {
            return posn;
        }
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        int posn = c % _alphabet.size();
        posn = wrap(posn);
        if (_invMap.containsKey(_alphabet.toChar(posn))) {
            char result = _invMap.get(_alphabet.toChar(posn));
            return _alphabet.toInt(result);
        } else {
            return posn;
        }
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        if (_map.containsKey(p)) {
            return _map.get(p);
        } else if (_alphabet.contains(p)) {
            return p;
        } else {
            throw new EnigmaException("Input value not valid.");
        }
    }

    /** Returns the string of alphabet.
     *
     * @return string pf alphabet.
     */
    String getAlpha() {
        return _alphabet.returnAlpha();
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        if (_invMap.containsKey(c)) {
            return _invMap.get(c);
        } else if (_alphabet.contains(c)) {
            return c;
        } else {
            throw new EnigmaException("Input value not valid.");
        }
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        boolean answer = true;
        if (_alphabet.size() > _map.size()) {
            return false;
        }
        for (Map.Entry<Character, Character> entry : _map.entrySet()) {
            if (entry.getKey() == entry.getValue()) {
                answer = false;
            }
        }
        return answer;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Map of permutations for each letter in alphabet. */
    protected Map<Character, Character> _map;

    /** Map of invert permutation for each letter in alphabet. */
    protected Map<Character, Character> _invMap;

    /** Scanner to read each block of cycles. */
    private Scanner _scanCycles;

}
