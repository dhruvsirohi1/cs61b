package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Dhruv Sirohi
 */
class Alphabet {

    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        chars = chars.replaceAll(" ", "");
        _alphabetList = chars;
        if (checkRepeat()) {
            throw new EnigmaException("Duplicate in alphabet.");
        } else if (this.contains('(')
                || this.contains(')') || this.contains('*')) {
            throw new EnigmaException("Alphabet is unacceptable.");
        }
    }

    /** Handles case of Ringstellung,
     * updating the alphabet of the particular rotor.
     * @param ch - Ringstellung setting supplied in.
     */
    void updateRing(char ch) {
        int index = _alphabetList.indexOf(ch);
        int length = _alphabetList.length();
        if (index > 0) {
            String first = _alphabetList.substring(0, index);
            _alphabetList = _alphabetList.substring(index, length);
            _alphabetList += first;
        }
    }

    /** Returns the String of alphabet
     * used.
     * @return String of alphabet.
     */
    String returnAlpha() {
        return _alphabetList;
    }

    /** Checks if any letter has been repeated in Alphabet.
     *  If returns true then ERROR.
     * @return Boolean value.
     */
    boolean checkRepeat() {
        for (int i = 0; i < _alphabetList.length(); i++) {
            for (int j = i + 1; j < _alphabetList.length(); j++) {
                if (_alphabetList.charAt(i) == _alphabetList.charAt(j)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _alphabetList.length();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        return (_alphabetList.indexOf(ch) >= 0);
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return _alphabetList.charAt(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        return _alphabetList.indexOf(ch);
    }

    /** String of letters used.*/
    private String _alphabetList;
}
