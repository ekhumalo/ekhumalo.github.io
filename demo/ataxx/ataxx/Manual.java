package ataxx;

/** A Player that receives its moves from its Game's getMoveCmnd method.
 *  @author E . Khumalo
 */
class Manual extends Player {

    /** A Player that will play MYCOLOR on GAME, taking its moves from
     *  GAME. */
    Manual(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        try {
            Command cmd = game().getMoveCmnd(myColor().toString()
                    + " enter move : ");
            String[] s = cmd.operands();
            Move move = Move.move(s[0].charAt(0), s[1].charAt(0),
                    s[2].charAt(0), s[3].charAt(0));
            if (game().board().legalMove(move)) {
                if (move.isPass()
                        && game().board().canMove(board().whoseMove())) {
                    return myMove();
                }
                return move;
            } else {
                return myMove();
            }
        } catch (NullPointerException excp) {
            return null;
        }
    }
}

