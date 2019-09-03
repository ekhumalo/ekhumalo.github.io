package ataxx;

/* Author: P. N. Hilfinger, (C) 2008. */

import java.util.Observable;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import static ataxx.PieceColor.*;
import static ataxx.GameException.error;

/** An Ataxx board.   The squares are labeled by column (a char value between
 *  'a' - 2 and 'g' + 2) and row (a char value between '1' - 2 and '7'
 *  + 2) or by linearized index, an integer described below.  Values of
 *  the column outside 'a' and 'g' and of the row outside '1' to '7' denote
 *  two layers of border squares, which are always blocked.
 *  This artificial border (which is never actually printed) is a common
 *  trick that allows one to avoid testing for edge conditions.
 *  For example, to look at all the possible moves from a square, sq,
 *  on the normal board (i.e., not in the border region), one can simply
 *  look at all squares within two rows and columns of sq without worrying
 *  about going off the board. Since squares in the border region are
 *  blocked, the normal logic that prevents moving to a blocked square
 *  will apply.
 *
 *  For some purposes, it is useful to refer to squares using a single
 *  integer, which we call its "linearized index".  This is simply the
 *  number of the square in row-major order (counting from 0).
 *
 *  Moves on this board are denoted by Moves.
 *  @author E. Khumalo
 */
class Board extends Observable {

    /** Number of squares on a side of the board. */
    static final int SIDE = 7;
    /** Length of a side + an artificial 2-deep border region. */
    static final int EXTENDED_SIDE = SIDE + 4;

    /** Number of non-extending moves before game ends. */
    static final int JUMP_LIMIT = 25;

    /** A new, cleared board at the start of the game. */
    Board() {
        _board = new PieceColor[EXTENDED_SIDE * EXTENDED_SIDE];

        for (int k = 0; k < _board.length; k++) {
            _board[k] = BLOCKED;
        }
        clear();
    }

    /** A copy of B. */
    Board(Board b) {
        _board = b._board.clone();
        _numMoves = b.numMoves();
        _allMoves = (LinkedList<Move>) b.allMoves();
        _numJumps = b.numJumps();

        _numPiecesStore = b._numPiecesStore;
        _whoseMove = b.whoseMove();
    }

    /** Return the linearized index of square COL ROW. */
    static int index(char col, char row) {
        return (row - '1' + 2) * EXTENDED_SIDE + (col - 'a' + 2);
    }

    /** Return the linearized index of the square that is DC columns and DR
     *  rows away from the square with index SQ. */
    static int neighbor(int sq, int dc, int dr) {
        return sq + dc + dr * EXTENDED_SIDE;
    }

    /** Clear me to my starting state, with pieces in their initial
     *  positions and no blocks. */
    void clear() {
        _whoseMove = RED;
        _allMoves = new LinkedList<Move>();
        _numPiecesStore = new HashMap<PieceColor, Integer>();

        for (char i = '1'; i <= '7'; i++) {
            for (char j = 'a'; j <= 'g'; j++) {
                set(j, i, EMPTY);
            }
        }
        set('a', '7', RED);
        set('g', '1', RED);
        set('g', '7', BLUE);
        set('a', '1', BLUE);
        incrPieces(RED, 2);
        incrPieces(BLUE, 2);


        setChanged();
        notifyObservers();
    }

    /** Return true iff the game is over: i.e., if neither side has
     *  any moves, if one side has no pieces, or if there have been
     *  MAX_JUMPS consecutive jumps without intervening extends. */
    boolean gameOver() {
        if (redPieces() == 0 || bluePieces() == 0) {
            return true;
        }
        return numJumps() >= JUMP_LIMIT
                || !(canMove(_whoseMove) || canMove(_whoseMove.opposite()));
    }

    /** Return number of red pieces on the board. */
    int redPieces() {
        return numPieces(RED);
    }

    /** Return number of blue pieces on the board. */
    int bluePieces() {
        return numPieces(BLUE);
    }

    /** Return number of COLOR pieces on the board. */
    int numPieces(PieceColor color) {
        int val = 0;
        for (int i = START; i <= END; i++) {
            if (get(i).equals(color)) {
                val++;
            }
        }

        return val;
    }

    /** Increment numPieces(COLOR) by K. */
    private void incrPieces(PieceColor color, int k) {
        if (_numPiecesStore.containsKey(color)) {
            int count = _numPiecesStore.get(color);
            _numPiecesStore.put(color, count + k);
        } else {
            _numPiecesStore.put(color, k);
        }
    }

    /** The current contents of square CR, where 'a'-2 <= C <= 'g'+2, and
     *  '1'-2 <= R <= '7'+2.  Squares outside the range a1-g7 are all
     *  BLOCKED.  Returns the same value as get(index(C, R)). */
    PieceColor get(char c, char r) {
        return _board[index(c, r)];
    }

    /** Return the current contents of square with linearized index SQ. */
    PieceColor get(int sq) {
        return _board[sq];
    }

    /** Set get(C, R) to V, where 'a' <= C <= 'g', and
     *  '1' <= R <= '7'. */
    private void set(char c, char r, PieceColor v) {
        set(index(c, r), v);
    }

    /** Set square with linearized index SQ to V.  This operation is
     *  undoable. */
    private void set(int sq, PieceColor v) {
        _board[sq] = v;
    }

    /** Set square at C R to V (not undoable). */
    private void unrecordedSet(char c, char r, PieceColor v) {
        _board[index(c, r)] = v;
    }

    /** Set square at linearized index SQ to V (not undoable). */
    private void unrecordedSet(int sq, PieceColor v) {
        _board[sq] = v;
    }

    /** Return true iff MOVE is legal on the current board. */
    boolean legalMove(Move move) {
        if (move == null) {
            return false;
        }
        if (move.isPass()) {
            return true;
        }
        if (get(move.fromIndex()).equals(_whoseMove)) {
            if (get(index(move.col1(), move.row1())) != EMPTY) {
                return false;
            }
            if (Math.abs(move.col1() - move.col0()) > 2
                    && Math.abs(move.row1() - move.row0()) > 2) {
                return false;
            }
            return true;
        }
        return false;
    }

    /** Return true iff player WHO can move, ignoring whether it is
     *  that player's move and whether the game is over. */
    boolean canMove(PieceColor who) {
        boolean canMove = false;
        for (int i = 0; i < _board.length; i++) {
            if (get(i) == who) {
                for (int j = -2; j <= 2; j++) {
                    for (int k = -2; k <= 2; k++) {
                        canMove = canMove || (get(neighbor(i, j, k)) == EMPTY);
                    }
                }
            }
        }
        return canMove;
    }

    /** Return the color of the player who has the next move.  The
     *  value is arbitrary if gameOver(). */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /** Return total number of moves and passes since the last
     *  clear or the creation of the board. */
    int numMoves() {
        return _numMoves;
    }

    /** Return number of non-pass moves made in the current game since the
     *  last extend move added a piece to the board (or since the
     *  start of the game). Used to detect end-of-game. */
    int numJumps() {
        return _numJumps;
    }

    /** Perform the move C0R0-C1R1, or pass if C0 is '-'.  For moves
     *  other than pass, assumes that legalMove(C0, R0, C1, R1). */
    void makeMove(char c0, char r0, char c1, char r1) {
        if (c0 == '-') {
            makeMove(Move.pass());
        } else {
            makeMove(Move.move(c0, r0, c1, r1));
        }
    }

    /** Make the MOVE on this Board, assuming it is legal. */
    void makeMove(Move move) {
        if (move.isPass()) {
            pass();
            _numMoves++;
            _whoseMove = _whoseMove.opposite();
            return;
        }
        if (legalMove(move)) {
            _numMoves++;
            if (move.isPass()) {
                pass();
                return;
            }
            set(move.col1(), move.row1(), _whoseMove);
            if (move.isJump()) {
                set(move.col0(), move.row0(), EMPTY);
                _prevJumps = _numJumps;
                _numJumps++;
            }
            if (move.isExtend()) {
                if (_numJumps == 0) {
                    _prevJumps = 0;
                }
                _numJumps = 0;
                if (_numPiecesStore.containsKey(_whoseMove)) {
                    int count = _numPiecesStore.get(_whoseMove);
                    _numPiecesStore.put(_whoseMove, count + 1);
                } else {
                    _numPiecesStore.put(_whoseMove, 1);
                }
            }
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int index = neighbor(index(move.col1(), move.row1()), i, j);
                    PieceColor piece = get(index);
                    if (piece.isPiece() && piece != _whoseMove) {
                        set(index, piece.opposite());
                        if (piece.isPiece()) {
                            move.addChange(index);
                            if (_numPiecesStore.containsKey(piece)) {
                                int count = _numPiecesStore.get(piece);
                                _numPiecesStore.put(piece, count + 1);
                            } else {
                                _numPiecesStore.put(piece, 1);
                            }
                        }
                    }
                }
            }
            _allMoves.add(move);
            PieceColor opponent = _whoseMove.opposite();
            _whoseMove = opponent;

            setChanged();
            notifyObservers();
        }
    }

    /** Update to indicate that the current player passes, assuming it
     *  is legal to do so.  The only effect is to change whoseMove(). */
    void pass() {
        if (canMove(_whoseMove)) {
            _whoseMove = _whoseMove.opposite();
            _numMoves++;
            setChanged();
            notifyObservers();
        }
    }

    /** Undo the last move. */
    void undo() {
        startUndo();
        setChanged();
        notifyObservers();
    }

    /** Indicate beginning of a move in the undo stack. */
    private void startUndo() {
        Move move = _allMoves.removeLast();
        PieceColor prev = _whoseMove.opposite();
        ArrayList<Integer> neig = move.getChangedSquares();

        if (move.isPass()) {
            _numMoves--;
            _whoseMove = _whoseMove.opposite();
            return;
        }
        if (move.isJump()) {
            if (_numJumps == 0) {
                _numJumps = _prevJumps;
                if (_prevJumps != 0) {
                    _prevJumps--;
                }
            }
        }

        if (move.isExtend()) {
            if (_numPiecesStore.containsKey(_whoseMove)) {
                int count = _numPiecesStore.get(_whoseMove);
                _numPiecesStore.put(_whoseMove, count - 1);
            }
        }

        _numMoves--;
        addUndo(index(move.col0(), move.row0()), prev);
        addUndo(index(move.col1(), move.row1()), EMPTY);

        for (int i : neig) {
            if (get(i).isPiece()) {
                addUndo(i, get(i).opposite());
                if (_numPiecesStore.containsKey(get(i))) {
                    int count = _numPiecesStore.get(get(i));
                    _numPiecesStore.put(get(i), count - 1);
                }
            }
        }

        _whoseMove = _whoseMove.opposite();
    }

    /** Add an undo action for changing SQ to NEWCOLOR on current
     *  board. */
    private void addUndo(int sq, PieceColor newColor) {
        set(sq, newColor);
    }

    /** Return true iff it is legal to place a block at C R. */
    boolean legalBlock(char c, char r) {
        if (r > '4') {
            r = (char) ('7' - r + 1);
        }

        if (c > 'd') {
            c = (char) ('g' - c + 1);
        }
        int first = index(c, r);
        int second = index((char) ('a' + 'g' - c), r);
        int third = index(c, (char) ('1' + '7' - r));
        int fourth = index((char) ('a' + 'g' - c), (char) ('1' + '7' - r));

        return !(get(first).isPiece())
                && !(get(second).isPiece())
                && !(get(third).isPiece())
                && !(get(fourth).isPiece());
    }

    /** Return true iff it is legal to place a block at CR. */
    boolean legalBlock(String cr) {
        return legalBlock(cr.charAt(0), cr.charAt(1));
    }

    /** Set a block on the square C R and its reflections across the middle
     *  row and/or column, if that square is unoccupied and not
     *  in one of the corners. Has no effect if any of the squares is
     *  already occupied by a block.  It is an error to place a block on a
     *  piece. */
    void setBlock(char c, char r) {
        if (!legalBlock(c, r)) {
            throw error("illegal block placement");
        }
        if (r > '4') {
            r = (char) ('7' - r + 1);
        }

        if (c > 'd') {
            c = (char) ('g' - c + 1);
        }
        int first = index(c, r);
        int second = index((char) ('a' + 'g' - c), r);
        int third = index(c, (char) ('1' + '7' - r));
        int fourth = index((char) ('a' + 'g' - c), (char) ('1' + '7' - r));

        set(first, BLOCKED);
        set(second, BLOCKED);
        set(third, BLOCKED);
        set(fourth, BLOCKED);
        setChanged();
        notifyObservers();
    }

    /** Place a block at CR. */
    void setBlock(String cr) {
        setBlock(cr.charAt(0), cr.charAt(1));
    }

    /** Return a list of all moves made since the last clear (or start of
     *  game). */
    List<Move> allMoves() {
        return _allMoves;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /* .equals used only for testing purposes. */
    @Override
    public boolean equals(Object obj) {
        Board other = (Board) obj;
        return Arrays.equals(_board, other._board)
                && _allMoves.equals(other._allMoves)
                && _whoseMove.equals(other._whoseMove);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_board);
    }

    /** Return a text depiction of the board (not a dump).  If LEGEND,
     *  supply row and column numbers around the edges. */
    String toString(boolean legend) {
        String returnString = "===" + "\r\n";
        for (char i = '7'; i >= '1'; i--) {
            returnString += " ";
            for (char j = 'a'; j <= 'g'; j++) {
                returnString += " " + getLetter(get(j, i));
            }
            returnString.trim();
            returnString += "\r\n";
        }
        returnString += "===" + "\r\n";

        return returnString;

    }

    /**
     * Get Letter helper.
     * @param piece pieceColor.
     * @return piece.
     */
    private String getLetter(PieceColor piece) {
        switch (piece) {
        case EMPTY:
            return "-";
        case RED:
            return "r";
        case BLUE:
            return "b";
        default:
            return "X";
        }
    }

    /** For reasons of efficiency in copying the board,
     *  we use a 1D array to represent it, using the usual access
     *  algorithm: row r, column c => index(r, c).
     *
     *  Next, instead of using a 7x7 board, we use an 11x11 board in
     *  which the outer two rows and columns are blocks, and
     *  row 2, column 2 actually represents row 0, column 0
     *  of the real board.  As a result of this trick, there is no
     *  need to special-case being near the edge: we don't move
     *  off the edge because it looks blocked.
     *
     *  Using characters as indices, it follows that if 'a' <= c <= 'g'
     *  and '1' <= r <= '7', then row c, column r of the board corresponds
     *  to board[(c -'a' + 2) + 11 (r - '1' + 2) ], or by a little
     *  re-grouping of terms, board[c + 11 * r + SQUARE_CORRECTION]. */
    private PieceColor[] _board;


    /** Player that is on move. */
    private PieceColor _whoseMove;



    /**
     * Piece to NUM of Pieces.
     */
    private HashMap<PieceColor, Integer> _numPiecesStore;

    /**
     * Number of moves.
     */
    private int _numMoves;

    /**
     * Number of jumps.
     */
    private int _numJumps;

    /**
     * A magnitude smaller than a normal value.
     */
    static final int START = 24;

    /**
     * End.
     */
    static final int END = 96;


    /**
     * All moves.
     */
    private LinkedList<Move> _allMoves = new LinkedList<Move>();

    /**
     *  Previous numJumps.
     */
    private int _prevJumps;
}
