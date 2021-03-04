/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Stack;

import static loa.Piece.BP;
import static loa.Piece.WP;

/** An automated Player.
 *  @author Dhruv Sirohi
 */
class MachinePlayer extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new MachinePlayer with no piece or controller (intended to produce
     *  a template). Switching it up. Once more. */
    MachinePlayer() {
        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
        history = new HashSet<>();
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert side() == work.turn();
        _foundMove = null;
        if (side() == WP) {
            findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove.
     *  You EDITED this.*/
    private void findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (getBoard().movesMade() < 2) {
            board.legalMoves();
            ArrayList<Move> temp = new ArrayList<>(board.getCornerMove(sense));
            Random r = new Random();
            _foundMove = temp.get(r.nextInt(temp.size()));
        } else {
            moveTree = new GameTree(new Board(board), sense, depth);
            moveTree.setValue(moveTree.setEval(moveTree, 0,
                    depth, alpha, beta));
            if (saveMove) {
                _foundMove = moveTree.bestMove();
            }
            int changed = -1;
            if (history.contains(_foundMove)) {
                if (sense > 0) {
                    int initScore = Integer.MIN_VALUE;
                    Square s = board.loneSquare(WP);
                    if (s != null) {
                        HashSet<Square> set = board.squareMoves(s);
                        for (Square to : set) {
                            Board temp = new Board(board);
                            Move nextmove = Move.mv(s, to);
                            temp.makeMove(nextmove);
                            if (temp.posnScore() > initScore) {
                                _foundMove = nextmove;
                                changed = 1;
                            }
                        }
                    }
                } else {
                    int initScore = Integer.MAX_VALUE;
                    Square s = board.loneSquare(BP);
                    if (s != null) {
                        HashSet<Square> set = board.squareMoves(s);
                        for (Square to : set) {
                            Board temp = new Board(board);
                            Move nextmove = Move.mv(s, to);
                            temp.makeMove(nextmove);
                            if (temp.posnScore() < initScore) {
                                _foundMove = nextmove;
                                changed = 1;
                            }
                        }
                    }
                }
                if (changed < 0) {
                    board.legalMoves();
                    Stack<Move> next = board.getStack(sense);
                    _foundMove = next.pop();
                }
            }
            if (history.size() < 4) {
                history.add(_foundMove);
            } else {
                history = new HashSet<>();
            }
        }
    }

    /** Makes sure move is not repeated. */
    private HashSet<Move> history;

    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return 3;

    }

    /** Limiting number. */
    private final int _limiter = 4;
    /** The game tree. */
    private GameTree moveTree;

    /** Used to convey moves discovered by findMove.
     * You DID NOT add any variables here. */
    private Move _foundMove;
}
