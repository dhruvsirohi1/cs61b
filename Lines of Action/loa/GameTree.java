package loa;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/** Represents a Game Tree.
 *  Which is used to retrieve the
 *  best move the machine player should make.
 * @author Dhruv Sirohi
 */
public class GameTree {
    /** Move that should be made after this board state. */
    private Move move;
    /** Current depth. */
    private int depth;
    /** Current state of board.*/
    private Board board;
    /** Represents the type of chooser.
     * +1 for maximizer
     * -1 for minimizer
     */
    private int chooser;
    /** Value of this board state. */
    private int value;
    /** Represents a list of all the possible
     * boards after making all the possible moves.
     */
    private List<GameTree> node;
    /** Stack of all moves for this player. */
    private Stack<Move> _allMoves;
    /** Max depth of the tree. */
    private int maxDepth;

    /** Constructor.
     *
     * @param b - board
     * @param maxmin - sense
     * @param maxD - max depth
     */
    GameTree(Board b, int maxmin, int maxD) {
        board = new Board(b);
        board.legalMoves();
        depth = 0;
        chooser = maxmin;
        _allMoves = getMoves();
        node = new ArrayList<>();
        maxDepth = maxD;
        parentPlayer = maxmin;
        if (maxmin < 0) {
            currentBest = Integer.MAX_VALUE;
        } else {
            currentBest = Integer.MIN_VALUE;
        }
    }

    /** Constructor.
     *
     * @param b - board
     * @param maxmin - sense
     * @param d - this depth
     * @param m - move
     * @param parentSense parent sense
     */
    GameTree(Board b, int maxmin, int parentSense, int d, Move m) {
        board = b;
        board.legalMoves();
        depth = d;
        chooser = maxmin;
        _allMoves = getMoves();
        node = new ArrayList<>();
        move = m;
        parentPlayer = parentSense;
        currentBest = board.posnScore();
    }

    /** Return the all possible moves for this player.
     *
     * @return Stack of moves.
     */
    Stack<Move> getMoves() {
        return board.getStack(chooser);
    }

    /** Sense of parent. */
    private int parentPlayer;
    /** Set the value of this node.
     *
     * @param i = integer value
     */
    void setValue(int i) {
        value = i;
    }

    /** Found the winning value. */
    private static boolean winningVal;

    /** returns the best move found.
     *
     * @return Move
     */
    Move bestMove() {
        return move;
    }

    /** Winning value for white. */
    private static final int WHITE_WINNING = Integer.MAX_VALUE - 20;

    /** Winning Value for black. */
    private static final int BLACK_WINNING = Integer.MIN_VALUE + 20;


    /** Current best score. */
    private int currentBest;
    /** Set the evaluation of all possible future boards,
     * returning the best value.
     *
     * @param child = gametree of this move
     * @param d = depth of this player
     * @param maxD = max depth of the tree
     * @param alpha = Maximum value found till now
     * @param beta = Minimum value found till now
     * @return Value
     */
    int setEval(GameTree child, int d, int maxD, int alpha, int beta) {
        if (d == maxD || board.gameOver() || (winningVal && depth == 1)) {
            return board.posnScore();
        }
        if (chooser > 0) {
            int maxEval = Integer.MIN_VALUE;
            while (!_allMoves.empty()) {
                Move tempMove = _allMoves.pop();
                Board temp = new Board(child.board());
                temp.makeMove(tempMove, true);
                if (temp.posnScore() <= currentBest) {
                    continue;
                }
                GameTree tempNode = new GameTree(temp,
                        chooser * -1, parentPlayer, d + 1, tempMove);
                child.value = tempNode.setEval(tempNode,
                        d + 1, maxD, alpha, beta);
                maxEval = Math.max(child.value, maxEval);
                moveSetter(child, maxEval, tempMove, temp);
                alpha = Math.max(alpha, maxEval);
                if ((maxEval == WHITE_WINNING)) {
                    winningVal = true;
                    if (depth != 0) {
                        break;
                    }
                }
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            while (!_allMoves.empty()) {
                Move tempMove = _allMoves.pop();
                Board temp = new Board(child.board);
                temp.makeMove(tempMove, true);
                if (temp.posnScore() >= currentBest) {
                    continue;
                }
                GameTree tempNode = new GameTree(temp,
                        chooser * -1, parentPlayer, d + 1, tempMove);
                child.value = tempNode.setEval(tempNode,
                        d + 1, maxD, alpha, beta);
                minEval = Math.min(child.value, minEval);
                moveSetter(child, minEval, tempMove, temp);
                beta = Math.min(beta, minEval);
                if ((minEval == BLACK_WINNING)) {
                    winningVal = true;
                    if (depth != 0) {
                        break;
                    }
                }
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    /** Set the move of the root.
     *
     * @param root - root
     * @param moveValue - value
     * @param finalmove - finalmove
     * @param tempboard - temp board
     */
    void moveSetter(GameTree root, int moveValue,
                    Move finalmove, Board tempboard) {
        if (root.value == moveValue && root.depth == 0) {
            root.move = finalmove;
            root.currentBest = tempboard.posnScore();
        }
    }

    /** Returns this board.
     *
     * @return Board.
     */
    Board board() {
        return board;
    }

}
