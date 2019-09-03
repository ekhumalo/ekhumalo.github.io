package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author E. Khumalo
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
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
        return _setting;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        _setting = posn;
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        _setting = cposn - num;
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        int newIndex = permutation().permute(permutation().wrap(p + setting()));
        return permutation().wrap(newIndex - setting());
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        int newIndex = permutation().invert(permutation().wrap(e + setting()));
        return permutation().wrap(newIndex - setting());
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /**
     * Can Advance.
     * @return boolean if it can advance, otherwise false.
     */
    boolean canAdvance() {
        return false;
    }

    /**
     * ON Pawl.
     */
    void onPawl() {
    }
    /**
     * OFF Pawl.
     */
    void offPawl() {
    }

    /**
     * ON Activated.
     */
    void onActivated() {
    }
    /**
     * OFF Activated.
     */
    void offActivated() {
    }

    /** My name. */
    private final String _name;

    /** The permutation implemnted by this rotor in its 0 position. */
    private Permutation _permutation;

    /**
     * Rotor Setting.
     */
    private int _setting = 0;

    /**
     * Magic number to adjust 65.
     */
    private final int num = 65;


}
