package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Dhruv Sirohi
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }


    @Test(expected = EnigmaException.class)
    public void testinvalidPermute() {
        Permutation p1 = new Permutation("(BACD)", new Alphabet("ABCD"));
        p1.permute('Q');
    }

    @Test
    public void testDerangement() {
        Permutation p1 = new Permutation("(HILFN)", new Alphabet("FLHINACS"));
        assertFalse("Cycles does not account "
                + "for each alphabet.", p1.derangement());

        Permutation p2 = new Permutation("(HILFN)", new Alphabet("FLHIN"));
        assertTrue("Permutation p2 is a derangement.", p2.derangement());

        Permutation p3 = new Permutation("", new Alphabet("FLHIN"));
        assertFalse("Every alphabet maps to itself.", p3.derangement());
    }

    @Test(expected = EnigmaException.class)
    public void testWrongCycles2() {
        Permutation p3 = new Permutation("AADESC", new Alphabet("ABCDES"));
        Permutation p4 = new Permutation("(ABD) (AES)", new Alphabet("ABCDES"));
    }

    @Test
    public void testInvert() {
        Permutation p1 = new Permutation("(HILFN)", new Alphabet("FLHIN"));
        char c = 'L';
        Permutation p2 = new Permutation("(MANUTD)", new Alphabet("ANMTDU"));
        char d = 'D';

        Permutation p3 = new Permutation("(ABC)", new Alphabet("ABCDE"));
        Permutation p4 = new Permutation("", new Alphabet("ABCDE"));

        assertEquals("Wrong invert of L", 'I', p1.invert('L'));
        assertEquals("Wrong invert od D", 'T', p2.invert('D'));
        assertEquals("Wrong invert for index -200 % size", 1, p1.invert(-200));
        assertEquals("Wrong invert for index -2 % size", 2, p1.invert(-2));
        assertEquals("Wrong invert for index 1 % size", 3, p1.invert(1));

        assertEquals("Wrong permute for index 3 % size", 3, p3.invert(3));
        assertEquals("Wrong permute for index 1 % size", 1, p4.permute(1));
        assertEquals("Wrong permute for index 4 % size", 4, p4.invert(4));
    }

    @Test(expected = EnigmaException.class)
    public void testWrongCycles() {
        Permutation p = new Permutation("(AB(CSD)E)", new Alphabet("ABCDES"));
    }

    @Test(expected = EnigmaException.class)
    public void testinvalidPermute2() {
        Permutation p1 = new Permutation("(BACD)", new Alphabet("ABCD"));
        p1.permute('/');
    }

    @Test(expected = EnigmaException.class)
    public void testNotInAlphabet() {
        Permutation p1 = new Permutation("(BACD)", new Alphabet("ABCD"));
        Permutation p2 = new Permutation("(QWER)", new Alphabet("RWEQ"));
        p1.invert('F');

    }

}
