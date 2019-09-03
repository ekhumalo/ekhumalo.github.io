package enigma;

import java.util.HashMap;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author E. Khumalo
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters not
     *  included in any cycle map to themselves. Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = cycles;

        if (_cycles.equals("")) {
            identity();
        } else {
            toPermutemap();
        }
    }

    /**
     * Stores the cycle letters in a HASHMAP.
     */
    private void toPermutemap() {
        int i = 0;
        int j = 1;
        String checkedCycle = _cycles;
        while (j < checkedCycle.length()) {
            if (checkedCycle.charAt(i) == ")".charAt(0)
                    && checkedCycle.charAt(j) == "(".charAt(0)) {
                checkedCycle = checkedCycle.substring(0, j)
                        + " " + checkedCycle.substring(j);
                i = 0;
                j = 1;
            } else {
                i = i + 1;
                j = j + 1;
            }
        }

        String[] splitCycles = checkedCycle.split(" ");

        for (String cycle : splitCycles) {
            addCycle(cycle);
        }
    }

    /**
     * Empty cycle, character maps to themselves.
     */
    private void identity() {
        String[] identityArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("(?!^)");

        for (String letter : identityArray) {
            permuteMap.put(letter.charAt(0), letter.charAt(0));
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        if (!(cycle.startsWith("(") && cycle.endsWith(")"))) {
            throw new EnigmaException("Bad Configuration: Bad cycle");
        }
        String preparedCycle = cycle.substring(1, cycle.length() - 1);
        String[] cycleArray = preparedCycle.split("(?!^)");

        int i = 0;
        int j = 1;

        while (i < cycleArray.length - 1) {
            permuteMap.put(cycleArray[i].charAt(0), cycleArray[j].charAt(0));
            i = j;
            j = j + 1;
        }
        permuteMap.put(cycleArray[i].charAt(0), cycleArray[0].charAt(0));

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
        return alphabet().size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        if (permuteMap.keySet().contains(alphabet().toChar(p))) {
            return permuteMap.get((alphabet().toChar(p))) - num;
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        return invert(alphabet().toChar(c)) - num;
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return permuteMap.get(p);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    int invert(char c) {
        for (char x : permuteMap.keySet()) {
            if (permuteMap.get(x).equals(c)) {
                return ((int) x);
            }
        }
        return c;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /**
     * Cycles of this permutation.
     */
    private String _cycles;

    /**
     * HashMap to Store permutations.
     */
    private HashMap<Character, Character> permuteMap =
            new HashMap<Character, Character>();

    /**
     * Number of char at 65.
     */
    private final int num = 65;
}

