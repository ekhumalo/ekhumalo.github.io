package ataxx;

import static ataxx.PieceColor.*;
import static java.lang.Math.min;
import static java.lang.Math.max;

/** A Player that computes its own moves.
 *  @author E. Khumalo
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 5;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * Start.
     */
    private int start = Board.START;

    /**
     * End.
     */
    private int end = Board.END;

    /**
     * Side of game.
     */
    private int side = Board.EXTENDED_SIDE;

    /** A new AI for GAME that will play MYCOLOR. */
    AI(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        if (!board().canMove(myColor())) {
            return Move.pass();
        }
        Move move = findMove();
        return move;
    }

    @Override
    boolean isAI() {
        return true;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (myColor() == RED) {
            findMove(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            findMove(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** Used to communicate best moves found by findMove, when asked for. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value >= BETA if SENSE==1,
     *  and minimal value or value <= ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels before using a static estimate. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        PieceColor pieceColor;
        Move goodSoFar = null;
        int bestSoFar;
        if (sense == 1) {
            pieceColor = RED;
            bestSoFar = alpha;
        } else {
            pieceColor = BLUE;
            bestSoFar = beta;
        }
        if (depth == 0 || board.gameOver()) {
            return simplefindMove(board, saveMove, sense, alpha, beta);
        } else {
            for (int i = start; i <= end; i++) {
                if (board.get(i).equals(pieceColor)) {
                    for (int j = -2; j <= 2; j++) {
                        for (int k = -2; k <= 2; k++) {
                            int to = Board.neighbor(i, j, k);
                            char c0 = (char) ('a' - 2 + (i % side));
                            char r0 = (char) ('1' - 2 + (i / side));
                            char c1 = (char) ('a' - 2 + (to % side));
                            char r1 = (char) ('1' - 2 + (to / side));
                            Move move = Move.move(c0, r0, c1, r1);
                            if (board.legalMove(move)) {
                                int res = findMove(
                                        board, depth - 1,
                                        false, -1 * sense, alpha, beta);
                                if (sense == 1) {
                                    if (res >= bestSoFar) {
                                        bestSoFar = res;
                                        goodSoFar = move;
                                        alpha = max(alpha, res);
                                    }
                                } else if (sense == -1) {
                                    if (res <= bestSoFar) {
                                        bestSoFar = res;
                                        goodSoFar = move;
                                        beta = min(beta, res);
                                    }
                                }
                                if (beta <= alpha) {
                                    break;
                                }
                                board.undo();
                            }
                        }

                    }
                }
            }
        }
        if (saveMove) {
            _lastFoundMove = goodSoFar;
        }
        return bestSoFar;
    }

    /**
     * Simple find Move.
     * @param board board
     * @param saveMove boolean
     * @param sense sense
     * @param alpha alpha
     * @param beta beta
     * @return int
     */
    private int simplefindMove(Board board, boolean saveMove,
                               int sense, int alpha, int beta) {
        Move goodSoFar = null;
        PieceColor pieceColor;
        int bestSoFar;
        if (sense == 1) {
            pieceColor = RED;
            bestSoFar = alpha;
        } else {
            pieceColor = BLUE;
            bestSoFar = beta;
        }
        if (board.gameOver()) {
            return staticScore(board);
        }
        for (int i = start; i <= end; i++) {
            if (board.get(i).equals(pieceColor)) {
                for (int j = -2; j <= 2; j++) {
                    for (int k = -2; k <= 2; k++) {
                        int to = Board.neighbor(i, j, k);
                        char c0 = (char) ('a' - 2 + (i % side));
                        char r0 = (char) ('1' - 2 + (i / side));
                        char c1 = (char) ('a' - 2 + (to % side));
                        char r1 = (char) ('1' - 2 + (to / side));
                        Move move = Move.move(c0, r0, c1, r1);
                        if (board.legalMove(move)) {
                            board.makeMove(move);
                            int score = staticScore(board);
                            if (sense == 1) {
                                if (score >= bestSoFar) {
                                    bestSoFar = score;
                                    goodSoFar = move;
                                    alpha = max(alpha, score);
                                    if (beta <= alpha) {
                                        break;
                                    }
                                }
                            } else if (sense == -1) {
                                if (score <= bestSoFar) {
                                    bestSoFar = score;
                                    goodSoFar = move;
                                    beta = min(beta, score);
                                    if (beta <= alpha) {
                                        break;
                                    }
                                }
                            }
                            board.undo();
                        }
                    }
                }
            }
        }
        if (saveMove) {
            _lastFoundMove = goodSoFar;
        }
        return bestSoFar;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        int score =  board.numPieces(board.whoseMove())
                - board.numPieces(board.whoseMove().opposite());
        if (board.numPieces(board.whoseMove())
                > (board.numPieces(board.whoseMove()))) {
            return score + WINNING_VALUE;
        } else {
            return score - WINNING_VALUE;
        }
    }
}
