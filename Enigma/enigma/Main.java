package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Dhruv Sirohi
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) throws IOException {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() throws IOException {
        Machine m = readConfig();
        Scanner processScan = _input;
        int configured = 0;
        while (processScan.hasNextLine()) {
            String line = processScan.nextLine();
            String strPat = "\\*([\\s\\t]*[\\w]+)+([\\s\\t]*"
                    + "\\([\\u0000-\\u007F[^\\*\\(\\)]]+\\))*";
            String pat2 = "\\*([\\s\\t]*[\\u0000-\\u007F[^\\*\\(\\)]]+)+";
            Pattern p = Pattern.compile(strPat), p2 = Pattern.compile(pat2);
            Matcher mat = p.matcher(line), mat2 = p2.matcher(line);
            if (mat.matches() || mat2.matches()) {
                setUp(m, line);
                configured = 1;
            } else if (configured == 1) {
                String msg = line;
                msg = msg.replaceAll("\\s+|\\t+", "");
                String out = m.convert(msg);
                printMessageLine(out);
            } else {
                throw new EnigmaException("Message without machine "
                        + "configuration/wrong setting line.");
            }
        }
    }

    /** Return this pattern.
     *
     * @return String of Regex pattern for a Rotor.
     */
    String getPat() {
        return "[\\s\\t]*([\\u0000-\\u007F[^\\*\\(\\)]])*[\\s\\t]+"
                + "([MNmnrR][\\w]*)[\\s\\t]*(\\s*\\([\\w\\.]+\\))+"
                + "[\\n\\r]?([\\s\\t]*\\([\\w]+\\)[\\s\\t]*)*[\\s\\t]*";
    }

    /** Check for redundancies in all the rotors
     * available. Code outsourced from
     * StackOverflow.
     */
    void checkRedundant() {
        for (int i = 0; i < allRotors.size(); i++) {
            for (int j = i + 1; j < allRotors.size(); j++) {
                if (allRotors.get(i).name().compareTo(allRotors.get(j).name())
                        == 0) {
                    throw new EnigmaException("Rotors cannot be repeated.");
                }
            }
        }
    }

    /** Fill the list provided.
     * @param l - a collection of integers.
     * @param s - scanner to get the values.*/
    void fillList(ArrayList<Integer> l, Scanner s) {
        while (s.hasNext()) {
            if (s.hasNextInt()) {
                l.add(s.nextInt());
            } else {
                throw new EnigmaException("Erroneous rotor nums.");
            }
        }
    }

    /** Check if rotor numbers provided are
     * valid.
     * @param l - List of rotor numbers.
     */
    void checkRotornums(ArrayList<Integer> l) {
        if (l.size() != 2 || l.get(1)
                >= l.get(0) || l.get(0) <= 0
                || l.get(1) < 0) {
            throw new EnigmaException("Bad rotors and pawls");
        }
    }

    /** Return an Enigma machine configured from
     * the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            ArrayList<Integer> rotors = new ArrayList<Integer>();
            int numPawls = 0;
            int totRotors = 0;
            String alpha = "([\\u0000-\\u007F[^\\*\\(\\)]])*";
            Pattern pAlpha = Pattern.compile(alpha);
            if (_config.hasNextLine()) {
                String inp = _config.nextLine();
                Matcher mat = pAlpha.matcher(inp);
                if (mat.matches()) {
                    _alphabet = new Alphabet(inp);
                } else {
                    throw new EnigmaException("Bad alphabet");
                }
            }
            if (_config.hasNextLine()) {
                _config.useDelimiter("\\s|\\t");
                fillList(rotors, new Scanner(_config.nextLine()));
                checkRotornums(rotors);
                totRotors = rotors.get(0);
                numPawls = rotors.get(1);
            }
            String rotorPat = getPat();
            String partialPat = "[\\s\\t]*([\\s\\t]*\\([\\w\\?\\-\\.]+\\))*";
            Pattern p1 = Pattern.compile(rotorPat);
            Pattern p2 = Pattern.compile(partialPat);
            ArrayList<String> rotorconfig = new ArrayList<String>();
            while (_config.hasNextLine()) {
                String rot = _config.nextLine();
                Matcher matRotor = p1.matcher(rot), mat2 = p2.matcher(rot);
                if (matRotor.matches()) {
                    rotorconfig.add(rot);
                } else if (mat2.matches()) {
                    String r = rotorconfig.get(rotorconfig.size() - 1);
                    r += rot;
                    rotorconfig.remove(rotorconfig.size() - 1);
                    rotorconfig.add(r);
                } else {
                    throw new EnigmaException("Bad rotor.");
                }
            }
            Iterator<String> rotIt = rotorconfig.iterator();
            while (rotIt.hasNext()) {
                Rotor r = readRotor(rotIt.next());
                if (r instanceof Reflector
                        && !((Reflector) r).isDerangement()) {
                    throw new EnigmaException(" Reflector not a derangement.");
                }
                allRotors.add(r);
            }
            checkRedundant();
            if (allRotors.size() < totRotors) {
                throw new EnigmaException("Bad rotor num.");
            }
            return new Machine(_alphabet, totRotors, numPawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config.
     * @param s - String of rotor with complete description.
     * */
    private Rotor readRotor(String s) {
        try {
            String name = "";
            StringBuilder info = new StringBuilder();
            StringBuilder perm = new StringBuilder();
            int i = 0;
            Scanner scanRot = new Scanner(s);
            scanRot.useDelimiter("\\s+|\\t+");
            while (scanRot.hasNext()) {
                switch (i) {
                case 0:
                    name = scanRot.next();
                    i++;
                    continue;
                case 1:
                    info.append(scanRot.next());
                    i++;
                    continue;
                default:
                    perm.append(" ").append(scanRot.next());
                }
            }
            String infoS = info.toString();
            String rotorPerm = perm.toString();
            if (info.charAt(0) == 'M' || info.charAt(0) == 'm') {
                return new MovingRotor(name,
                        new Permutation(rotorPerm, _alphabet),
                        info.substring(1));
            } else if (infoS.compareTo("R") == 0 || infoS.compareTo("r") == 0) {
                return new Reflector(name,
                        new Permutation(rotorPerm, _alphabet));
            } else if (infoS.compareTo("N") == 0 || infoS.compareTo("n") == 0) {
                return new FixedRotor(name,
                        new Permutation(rotorPerm, _alphabet));
            } else {
                throw new EnigmaException("Bad rotor type.");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String setting = "";
        String[] rotors = new String[M.numRotors()];
        if (true) {
            int i = 0;
            Scanner scanSet = new Scanner(settings);
            String first = scanSet.next();
            while (scanSet.hasNext() && i < M.numRotors()) {
                if (i == 0 && first.length() > 1) {
                    rotors[i] = first.substring(1);
                } else {
                    rotors[i] = scanSet.next();
                }
                i++;
            }
            M.insertRotors(rotors);
            if (scanSet.hasNext()) {
                String temp = scanSet.next();
                setting += temp;
                if (temp.length() != M.numRotors() - 1) {
                    throw new EnigmaException("Erroneous setting line.");
                }
                StringBuilder pb = new StringBuilder();
                String stellungPat = "[\\w]+";
                Pattern p = Pattern.compile(stellungPat);
                if (scanSet.hasNext()) {
                    String stellung = scanSet.next();
                    Matcher mat = p.matcher(stellung);
                    if (mat.matches() && stellung.length() <= M.numRotors()) {
                        M.stellungSet(stellung);
                    } else {
                        pb.append(stellung);
                    }
                }
                M.setRotors(setting);
                while (scanSet.hasNext()) {
                    pb.append(" ").append(scanSet.next());
                }
                String plugboard = pb.toString();
                M.setPlugboard(new Permutation(plugboard, _alphabet));
            }
        } else {
            throw new EnigmaException("Bad setting.");
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        int length = msg.length();
        int groups = length / 5;
        int point = 0;

        for (int i = 0; i <= groups; i++) {
            int j = 0;
            while (j < 5 && point < length) {

                _output.print(msg.charAt(point));
                point++;
                j++;
            }
            if (i < groups + 1) {

                _output.print(' ');
            }
        }

        _output.print("\n");
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** List of all rotors from config. */
    private ArrayList<Rotor> allRotors = new ArrayList<Rotor>();
}
