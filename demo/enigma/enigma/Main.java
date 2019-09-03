package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;
import static java.lang.String.*;

/** Enigma simulator.
 *  @author E. K (Part)
 *  Acknowledgements, Some Regex expressions from Stackoverflow.
 *  Acknowledgements, PIAZZA Posts and SLACK group offered guide
 *  to design implementation
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
    public static void main(String... args) {
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
            _inputCopy = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
            _inputCopy = new Scanner(System.in);
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
    private void process() {
        _inputLine = _input.nextLine();
        Machine machine = readConfig();
        if (!_inputLine.startsWith("*")) {
            throw new EnigmaException("Message has no settings");
        }
        while (_inputLine.startsWith("*")) {
            setUp(machine, _inputLine);
            String input = "";
            String nowLine = "";
            if (_input.hasNextLine()) {
                nowLine = _input.nextLine();
            }
            while (!(nowLine.trim().startsWith("*")) && !nowLine.equals("#")) {
                input = input.concat(
                        nowLine.replaceAll("[^A-Za-z]+", "").toUpperCase());
                if (_input.hasNextLine()) {
                    nowLine = _input.nextLine();
                } else {
                    nowLine = "#";
                }
            }
            if (!input.equals("")) {
                printMessageLine(machine.convert(input));
            } else {
                printBlankmessage(input);
            }
            _endFile = true;
            _inputLine = nowLine;

        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _config.nextLine();
            int numRotors = _config.nextInt();
            int numPawls = _config.nextInt();
            _config.nextLine();
            _prevLine = _config.nextLine();
            while (_config.hasNextLine()) {
                _nextLine = _config.nextLine();
                allRotors.add(readRotor());
                _prevLine = _nextLine;
            }
            if (!_nextLine.trim().startsWith("(")) {
                _endConfig = true;
                allRotors.add(readRotor());
            }
            _alphabet = new UpperCaseAlphabet();
            return new Machine(_alphabet, numRotors, numPawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String trimRotor = _nextLine.trim();

            if (trimRotor.startsWith("(")) {
                _prevLine = _prevLine.concat(" " + trimRotor);
                if (_config.hasNextLine()) {
                    _nextLine = _config.nextLine();
                }
            }

            String[] testRotor = _prevLine.trim().split(" ");
            String name = testRotor[0].trim().toUpperCase();
            String type = testRotor[1].trim().substring(0, 1);
            String notches = testRotor[1].trim().substring(1);
            String cycles = _prevLine.substring(_prevLine.indexOf("("));
            Permutation perm = new Permutation(cycles, new UpperCaseAlphabet());

            switch (type) {
            case "M":
                MovingRotor returnRotor = new MovingRotor(name, perm, notches);
                return returnRotor;

            case "N":
                FixedRotor returnFixed = new FixedRotor(name, perm);
                return returnFixed;
            case "R":
                Reflector returnReflector = new Reflector(name, perm);
                return returnReflector;
            default:
                throw new EnigmaException("Bad Rotor Configuration");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] data = settings.split(" ");
        int start = settings.indexOf("(");
        String plugCycle = "";
        String checkSetting = "";
        if (start > 0) {
            plugCycle = settings.substring(start);
            checkSetting = settings.substring(0, start);
        } else {
            checkSetting = settings;
        }
        int givenNumRotors = checkSetting.split(" ").length - 2;
        if (givenNumRotors != M.numRotors()) {
            throw new EnigmaException("Incorrect number of rotors passed");
        }
        String[] rotors = new String[M.numRotors()];
        System.arraycopy(data, 1, rotors, 0, M.numRotors());
        M.insertRotors(rotors);
        M.setRotors(data[M.numRotors() + 1]);
        M.setPlugboard(new Permutation(plugCycle, new UpperCaseAlphabet()));
    }

    /**
     * Print Blank Message.
     * @param msg Blank message.
     */
    private void printBlankmessage(String msg) {
        _output.append(msg.trim());
        _output.flush();
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        String line = "";
        String see = "";
        if (_endFile) {
            _output.append("\r\n");
            see = _inputCopy.nextLine();
            if (!see.startsWith("*")) {
                line = see;
            }
        } else {
            see = _inputCopy.nextLine();
            if (_inputCopy.hasNextLine()) {
                line = _inputCopy.nextLine();
            }
        }
        while (line.length() == 0) {
            if (_inputCopy.hasNextLine()) {
                line = _inputCopy.nextLine();
                if (see.startsWith("*") && !_endFile) {
                    _output.append("\r\n");
                }
            }
        }
        int index = 0;

        while (line.replaceAll("[^A-Za-z]+", "").length() != 0) {
            if (line.startsWith("*")) {
                if (_inputCopy.hasNextLine()) {
                    line = _inputCopy.nextLine();
                } else {
                    throw new EnigmaException("Bad Input file");
                }
            }

            String[] lineMsg = msg.substring(
                    index, index
                            + line.replaceAll("[^A-Za-z]+", "").length(
                    )).split("(?<=\\G.{5})");
            String toOutput = "";
            for (String part : lineMsg) {
                toOutput = toOutput + part + " ";
            }
            _output.append(toOutput.trim());
            if (_inputCopy.hasNextLine()) {
                String temp = _inputCopy.nextLine();
                if (temp.startsWith("*")) {
                    line = "#";
                } else {
                    index = index + line.replaceAll("[^A-Za-z]+", "").length();
                    line = temp;
                    _output.append("\r\n");
                }
            } else {
                line = "#";
            }
        }
        _output.flush();

    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /**
     * Copy of input messages.
     */
    private Scanner _inputCopy;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /**
     * String for Previous _ConfigLine.
     */
    private String _prevLine;

    /**
     * String of NEXT _ConfigLine.
     */
    private String _nextLine;

    /**
     * String of INPUT SETTINGS.
     */
    private String _inputLine;

    /**
     * HashMap of Rotor Name to Rotor.
     */
    private ArrayList<Rotor> allRotors = new ArrayList<Rotor>();

    /**
     * Boolean to track EOF of PrintStream.
     */
    private boolean _endFile;

    /**
     * End Config File with helper boolean.
     */
    private boolean _endConfig;
}
