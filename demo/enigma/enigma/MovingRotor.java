package enigma;

import java.util.HashSet;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author E. Khumalo
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
        _activePawl = false;
        _hasActivated = false;
        toNotchSet();
    }

    @Override
    boolean rotates() {
        return true;
    }

    /**
     * Can Advance.
     * @return Boolean if can advance
     */
    boolean canAdvance() {
        return (_hasActivated && (atNotch()) || _activePawl);
    }

    @Override
    boolean atNotch() {
        return _notchSet.contains(alphabet().toChar(
                setting() % alphabet().size()));
    }


    @Override
    void advance() {
        if (canAdvance()) {
            set(setting() + 1);
            offActivated();
            offPawl();
        }
    }

    /**
     * Adds to _NOTCHSET.
     */
    private void toNotchSet() {
        if (!_notches.equals("")) {
            String[] notchArray = _notches.split("(?!^)");

            for (String notch : notchArray) {
                _notchSet.add(notch.charAt(0));
            }
        }
    }

    @Override
    void onPawl() {
        _activePawl = true;
    }

    @Override
    void offPawl() {
        _activePawl = false;
    }

    @Override
    void onActivated() {
        _hasActivated = true;
    }

    @Override
    void offActivated() {
        _hasActivated = false;
    }

    /**
     * Notches of Moving Rotor.
     */
    private String _notches;

    /**
     * HashSet of CHAR NOTCHES.
     */
    private HashSet<Character>  _notchSet = new HashSet<Character>();

    /**
     * Active PAWL.
     */
    private boolean _activePawl;

    /**
     * Has activated neighbor.
     */
    private boolean _hasActivated;

}
