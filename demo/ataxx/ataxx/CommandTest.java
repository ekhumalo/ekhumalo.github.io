/* Author: Paul N. Hilfinger.  (C) 2008. */

package ataxx;

import org.junit.Test;
import static org.junit.Assert.*;

import ataxx.Command.Type;
import static ataxx.Command.Type.*;

/** Test command parsing.  */
public class CommandTest {

    void check(String cmnd, Type type, String... operands) {
        Command c = Command.parseCommand(cmnd);
        assertEquals("Wrong type of command identified", type,
                     c.commandType());
        if (operands.length == 0) {
            assertEquals("Command has wrong number of operands", 0,
                         c.operands() == null ? 0 : c.operands().length);
        } else {
            assertArrayEquals("Operands extracted incorrectly",
                              operands, c.operands());
        }
    }

    void checkError(String cmnd) {
        check(cmnd, ERROR);
    }

    @Test public void testAUTO() {
        check("auto red", AUTO, "red");
        check("auto blue", AUTO, "blue");
        checkError("auto green");
        checkError("auto");
        checkError("auto red foo");
    }

    @Test public void testBLOCK() {
        check("block b3", BLOCK, "b3");
        check("block d3", BLOCK, "d3");
        checkError("block");
    }

    @Test public void testMANUAL() {
        check("manual red", MANUAL, "red");
        check("manual blue", MANUAL, "blue");
        checkError("manual green");
        checkError("manual");
        checkError("manual red foo");
    }

    @Test public void testSEED() {
        check("seed 142", SEED, "142");
        checkError("seed");
        checkError("seed 14x");
        checkError("seed 142 foo");
    }

    @Test public void testSTART() {
        check("start", START);
        checkError("start foo");
    }

    @Test public void testPASS() {
        check("pass", PASS);
        check("-", PASS);
        checkError("pass foo");
    }

    @Test public void testQUIT() {
        check("quit", QUIT);
        checkError("quit foo");
    }

    @Test public void testCLEAR() {
        check("clear", CLEAR);
        checkError("clear foo");
    }

    @Test public void testMOVE() {
        check("a3-b3", PIECEMOVE, "a", "3", "b", "3");
        check("g7-f6", PIECEMOVE, "g", "7", "f", "6");
        checkError("a3b3");
        checkError("a3-b3 foo");
        checkError("3a-3b");
        checkError("h3-g3");
        checkError("a0-a1");
        checkError("a7-a8");
    }

    @Test public void testLOAD() {
        check("load testing/test01.inp", LOAD, "testing/test01.inp");
        check("load boy/01.inp", LOAD, "boy/01.inp");
        check("load staff-src/test08.in", LOAD, "staff-src/test08.in");
        checkError("load test0$%");
    }
}
