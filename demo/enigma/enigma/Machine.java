package enigma;

import java.util.ArrayList;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author E. Khumalo
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _numPawls = pawls;
        _allRotors = allRotors;
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numPawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        chosenRotors = new ArrayList<Rotor>();
        for (String name : rotors) {
            for (Rotor rotor : _allRotors) {
                if (name.equals(rotor.name())) {
                    chosenRotors.add(rotor);
                    break;
                }
            }
        }
        if (!chosenRotors.get(0).reflecting()) {
            throw new EnigmaException("First Rotor should be a REFLECTOR.");
        }
        int moving = 0;
        for (Rotor rotor : chosenRotors) {
            if (rotor.rotates()) {
                moving += 1;
            }
        }
        if (moving != numPawls()) {
            throw new EnigmaException("Rotors not equal to number of Pawls.");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of four
     *  upper-case letters. The first letter refers to the leftmost
     *  rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        int i = chosenRotors.size() - 1;
        if (setting.length() != numRotors() - 1) {
            throw new EnigmaException("Wrong number of rotors in setting");
        }

        while (i > 0) {
            try {
                chosenRotors.get(i).set(setting.charAt(i - 1));
                i--;
            } catch (StringIndexOutOfBoundsException exp) {
                throw new EnigmaException("Wrong number of rotors");
            }
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        if (chosenRotors.get(numRotors() - 1).atNotch()) {
            if (chosenRotors.get(numRotors() - 2).rotates()) {
                chosenRotors.get(numRotors() - 2).onPawl();
            }
        }
        chosenRotors.get(numRotors() - 1).advance();

        for (int i = numRotors() - 2; i >= 0; i--) {
            if (chosenRotors.get(i).atNotch()) {
                if (chosenRotors.get(i - 1).rotates()) {
                    chosenRotors.get(i - 1).onPawl();
                    chosenRotors.get(i).onActivated();
                }
            }
            chosenRotors.get(i).advance();
        }

        int index = _plugboard.permute(c);
        for (int i = numRotors() - 1; i >= 0; i--) {
            index = chosenRotors.get(i).convertForward(index);
        }
        for (int i = 1; i <= numRotors() - 1; i++) {
            index = chosenRotors.get(i).convertBackward(index);
        }
        return _plugboard.permute(index);
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String returnString = "";
        String[] message = msg.split("(?!^)");

        for (String str : message) {
            chosenRotors.get(numRotors() - 1).onPawl();
            chosenRotors.get(numRotors() - 1).onActivated();
            char result = _alphabet.toChar(convert(str.charAt(0) - num));
            returnString = returnString + result;
        }
        return returnString;
    }

    /**
     *  Common alphabet of my rotors.
     *  */
    private final Alphabet _alphabet;

    /**
     * All available rotors.
     */
    private Collection<Rotor> _allRotors;

    /**
     * Number of Rotors.
     */
    private int _numRotors;

    /**
     * Number of Pawls.
     */
    private int _numPawls;

    /**
     * HashMap of NAME to ROTOR.
     */
    private ArrayList<Rotor> chosenRotors = new ArrayList<Rotor>();

    /**
     * Plugboard permutation.
     */
    private Permutation _plugboard;

    /**
     * Magic number at 65.
     */
    private final int num = 65;
}
