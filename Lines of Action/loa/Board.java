/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;


import java.util.Stack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Formatter;
import java.util.regex.Pattern;

import static loa.Piece.BP;
import static loa.Piece.EMP;
import static loa.Piece.WP;
import static loa.Square.BOARD_SIZE;
import static loa.Square.sq;

/** Represents the state of a game of Lines of Action.
 *  @author Dhruv Sirohi
 */
class Board {

    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 60;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row][col]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move.
     * You EDITED this.*/
    void initialize(Piece[][] contents, Piece side) {
        _moves.clear();
        _winner = null;
        _boardHistory = new Stack<>();
        sizeBlack = sizeWhite = 12;
        blackBoardScore = whiteBoardScore = 0;
        int count = _board.length - 1;
        int rw = BOARD_SIZE;
        _map = new HashMap<>();
        _playingSquares = new HashMap<>();
        for (int i = contents.length - 1; i >= 0; i--) {

            char s = 'h';
            for (int j = contents[i].length - 1; j >= 0; j--) {
                _board[count] = contents[i][j];
                String st = s + Integer.toString(rw);
                _map.put((s + Integer.toString(rw)), _board[count]);
                if (contents[i][j] != EMP) {
                    _playingSquares.put(sq((s
                            + Integer.toString(rw))), contents[i][j]);
                }
                count--;
                s--;
            }
            rw--;
        }
        _turn = side;
        _comBlack = _comWhite = sq("d4");
        _moveLimit = DEFAULT_MOVE_LIMIT;
        setMap();
        initCom();
        _comWhite = _comBlack = sq("d4");
        whiteComs = new Stack<>();
        blackComs = new Stack<>();
        whiteComs.push(_comWhite);
        blackComs.push(_comBlack);
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD.
     * You EDITED this. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        System.arraycopy(board._board, 0,
                _board, 0, 64);
        this._map = new HashMap<>(board._map);
        this._turn = board.turn();
        this.sizeWhite = board.getWhiteNum();
        this.sizeBlack = board.getBlackNum();
        this._comBlack = board.getCom(BP);
        this._comWhite = board.getCom(WP);
        this.blackRegions = board.getRegions(BP);
        this.whiteRegions = board.getRegions(WP);
        this._playingSquares = new HashMap<>(board.getPlayers());
        this.valMap = new HashMap<>(board.getValMap());
        this.blackBoardScore = board.getBlackBoardScore();
        this.whiteBoardScore = board.getWhiteBoardScore();
        this.whiteRow = board.getWhiteRow();
        this.blackRow = board.getBlackRow();
        this.whiteCol = board.getWhiteCol();
        this.blackCol = board.getBlackCol();
    }

    /** Sums of col and rows of each piece. */
    private int blackCol, blackRow, whiteCol, whiteRow;


    /** Return sum of specified thing.
     *
     * @return sum
     */
    int getBlackCol() {
        return blackCol;
    }


    /** Return sum of specified thing.
     *
     * @return sum
     */

    int getBlackRow() {
        return blackRow;
    }


    /** Return sum of specified thing.
     *
     * @return sum
     */
    int getWhiteCol() {
        return whiteCol;
    }


    /** Return sum of specified thing.
     *
     * @return sum
     */
    int getWhiteRow() {
        return whiteRow;
    }

    /** Initialize center or mass info. */
    void initCom() {
        blackCol = blackRow = whiteRow = whiteCol = 0;
        for (Square s : _playingSquares.keySet()) {
            if (_playingSquares.get(s) == BP) {
                blackCol += s.col();
                blackRow += s.row();
            } else {
                whiteCol += s.col();
                whiteRow += s.row();
            }
        }
    }
    /** Returns value map. */
    HashMap<Square, Integer> getValMap() {
        return valMap;
    }
    /** Returns the regions for a piece.
     *
     * @param p - piece
     * @return ArrayList
     */
    ArrayList<HashSet<Square>> getRegions(Piece p) {
        if (p == BP) {
            return blackRegions;
        } else {
            return whiteRegions;
        }
    }
    /** Return the contents of the square at SQ. */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /** Set the square at SQ to V and set the side that is to move next
     *  to NEXT, if NEXT is not null.You EDITED this.*/
    void set(Square sq, Piece v, Piece next) {
        _board[sq.index()] = v;
        if (next != null) {
            _turn = next;
        }
        _board[sq.index()] = v;
        _map.put(sq.toString(), v);
    }

    /** Set the square at SQ to V, without modifying the side that
     *  moves next. */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }

    /** Set limit on number of moves by each side that results in a tie to
     *  LIMIT, where 2 * LIMIT > movesMade(). */
    void setMoveLimit(int limit) {
        if (2 * limit <= movesMade()) {
            throw new IllegalArgumentException("move limit too small");
        }
        _moveLimit = 2 * limit;
    }

    /** Capture penalty for a move. */
    private static final int CAPTURE_PENALTY = 2000;
    /** Bonus for breaking a set. */
    private static final int BREAKING_BONUS = 0;
    /** Tells if this move slaps. */
    private boolean _captured;
    /** Assuming isLegal(MOVE), make MOVE. Assumes MOVE.isCapture()
     *  is false. You EDITED this. */
    void makeMove(Move move) {
        assert isLegal(move);
        _moves.add(move);
        updateCom(move);
        Square from = move.getFrom();
        Piece moving = _map.get(from.toString());

        Square to = move.getTo();
        _playingSquares.remove(from);
        if (_playingSquares.containsKey(to)) {
            _playingSquares.replace(to, _map.get(from.toString()));
        } else {
            _playingSquares.put(to, _map.get(from.toString()));
        }
        if (_map.get(move.getTo().toString()) == BP) {
            sizeBlack--;
            move = move.captureMove();
            _captured = true;
            blackBoardScore -= valMap.get(to);
        } else if (_map.get(move.getTo().toString()) == WP) {
            sizeWhite--;
            move = move.captureMove();
            _captured = true;
            whiteBoardScore -= valMap.get(to);
        }
        _boardHistory.push(_map.get(move.getTo().toString()));
        Piece temp = _board[from.index()];
        _map.put(from.toString(), EMP);
        _map.put(to.toString(), temp);
        _board[from.index()] = EMP;
        _board[to.index()] = temp;
        _turn = _turn.opposite();
        updateScores(from, to);
        _subsetsInitialized = false;
    }

    /** Updates center of mass.
     *  Changing For Satsfaction of Mind.
     * @param move - a move
     */
    void updateCom(Move move) {
        Square from = move.getFrom();
        Square to = move.getTo();

        if (_map.get(from.toString()) == BP) {
            blackCol = blackCol - from.col() + to.col();
            blackRow = blackRow - from.row() + to.row();
        } else {
            whiteCol = whiteCol - from.col() + to.col();
            whiteRow = whiteRow - from.row() + to.row();
        }
        if (_map.get(to.toString()) != EMP) {
            if (_map.get(to.toString()) == BP) {
                blackCol -= to.col();
                blackRow -= to.row();
            } else {
                whiteRow -= to.row();
                whiteCol -= to.col();
            }
        }


        _comBlack = sq(blackCol / sizeBlack, blackRow / sizeBlack);
        _comWhite = sq(whiteCol / sizeWhite, whiteRow / sizeWhite);
        whiteComs.push(_comWhite);
        blackComs.push(_comBlack);
    }

    /** Holders of previous CoMs. */
    private Stack<Square> whiteComs, blackComs;
    /** Position score of black and white pieces. */
    private int blackBoardScore, whiteBoardScore;

    /** Returns blacks board score.
     *
     * @return black score
     */
    int getBlackBoardScore() {
        return blackBoardScore;
    }

    /** Returns whites board score.
     *
     * @return white score
     */
    int getWhiteBoardScore() {
        return whiteBoardScore;
    }

    /** Checks if there is a lone Square for this player.
     *
     * @param p - piece
     * @return Square
     */
    Square loneSquare(Piece p) {
        if (movesMade() >= 0) {
            computeRegions();
            if (p == BP) {
                for (HashSet<Square> s : blackRegions) {
                    if (s.size() == 1) {
                        Iterator<Square> iter = s.iterator();
                        return iter.next();
                    }
                }
            } else {
                for (HashSet<Square> s : whiteRegions) {
                    if (s.size() == 1) {
                        Iterator<Square> iter = s.iterator();
                        return iter.next();
                    }
                }
            }
        }
        return null;
    }
    /** Update this boards score.
     *
     * @param from - square moving
     * @param to - where from is moving
     */
    void updateScores(Square from, Square to) {
        if (_playingSquares.get(from) == BP) {

            blackBoardScore = blackBoardScore - largestDist(BP);
            blocked(BP);

        } else {

            whiteBoardScore = whiteBoardScore - largestDist(WP);
            blocked(WP);

        }
    }

    /** Largest distance from CoM.
     *
     * @param p - piece
     * @return int distance
     */
    int largestDist(Piece p) {
        int sum = 0;
        for (Square sq : _playingSquares.keySet()) {
            for (Square sq2 : _playingSquares.keySet()) {
                if (sq != sq2 && _map.get(sq2.toString()) == p) {
                    if (sq2.distance(sq) > sum) {
                        sum = sq2.distance(sq);
                    }
                }
            }
        }
        return (sum * 100);
    }

    /** Returns true of this square is surrounded by a similar piece.
     *
     * @param s - square
     * @param p - piece type
     * @return true/false
     */
    boolean surrounded(Square s, Piece p) {
        for (int i = 0; i < 8; i++) {
            Square temp = s.moveDest(i, 1);
            if (temp != null) {
                if (_map.get(temp.toString()) == p) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Checks how many pieces are blocked/ blocking for
     *  each player.
     * @param p - Piece
     */
    void blocked(Piece p) {
        int found = -1;
        for (Square s : _playingSquares.keySet()) {
            if (notConnected(s)) {
                for (int i = 0; i < 8; i++) {
                    Square temp = s.moveDest(i, 1);
                    while (temp != null) {
                        if (_map.get(temp.toString()) == p.opposite()) {
                            break;
                        }
                        if (_map.get(temp.toString()) == _playingSquares.get(s)
                                || !surrounded(temp, p)) {
                            found = 1;
                            break;
                        }
                        temp = temp.moveDest(i, 1);
                    }
                    if (found == 1) {
                        break;
                    }
                }
                if (found == -1) {
                    if (p == BP) {
                        blackBoardScore -= blockedPenalty;
                        whiteBoardScore += blockingBonus;
                    } else if (p == WP) {
                        whiteBoardScore -= blockedPenalty;
                        blackBoardScore += blockingBonus;
                    }
                }
            }
        }
    }
    /** Bonus points. */
    private final int blockingBonus = 8000;
    /** Bonus points. */
    private final int blockedPenalty = 4000;

    /** Checks if a Square is not connected.
     *
     * @param s - square
     * @return true/false
     */
    boolean notConnected(Square s) {
        boolean answer = true;
        for (int i = 0; i < 8; i++) {
            Square temp = s.moveDest(i, 1);
            if (temp != null) {
                if (_map.get(s.toString()) == _map.get(temp.toString())) {
                    return false;
                }
            }
        }
        return answer;
    }

    /** My very own creation.
     *
     * @param move - a move
     * @param computer - a computer
     */
    void makeMove(Move move, boolean computer) {
        assert isLegal(move);
        _moves.add(move);
        updateCom(move);
        Square from = move.getFrom();
        Piece moving = _map.get(from.toString());
        Square to = move.getTo();
        _playingSquares.remove(from);
        if (_playingSquares.containsKey(to)) {
            _playingSquares.replace(to, _map.get(from.toString()));
        } else {
            _playingSquares.put(to, _map.get(from.toString()));
        }
        int initialBlack = blackRegions.size();
        int initialWhite = whiteRegions.size();
        if (_map.get(move.getTo().toString()) == BP) {
            sizeBlack--;
            _capturedPiece = BP;
            move = move.captureMove();
        } else if (_map.get(move.getTo().toString()) == WP) {
            sizeWhite--;
            _capturedPiece = WP;
            move = move.captureMove();
        }
        _boardHistory.push(_map.get(move.getTo().toString()));
        Piece temp = _board[from.index()];
        _map.put(from.toString(), EMP);
        _map.put(to.toString(), temp);
        _board[from.index()] = EMP;
        _board[to.index()] = temp;
        _turn = _turn.opposite();
        computeRegions();
        if (initialBlack < blackRegions.size()
                || initialWhite > whiteRegions.size()) {
            if (initialWhite > whiteRegions.size()) {
                whiteBonus = 10 * _captureBonus;
            } else {
                whiteBonus = 4 * _captureBonus;
            }
        } else if (initialWhite < whiteRegions.size()
                || initialBlack > blackRegions.size()) {
            if (initialBlack > blackRegions.size()) {
                blackBonus = 10 * _captureBonus;
            } else {
                blackBonus = 4 * _captureBonus;
            }
        } else {
            if (_capturedPiece == BP) {
                _whitePenalty +=  CAPTURE_PENALTY;
            } else {
                _blackPenalty +=  CAPTURE_PENALTY;
            }
        }
        updateScores(from, to);
        _subsetsInitialized = false;
    }

    /** Captured piece. */
    private Piece _capturedPiece;
    /** Penalty for worthless capture. */
    private int _blackPenalty, _whitePenalty;
    /** Fix. */
    private final int  _captureBonus = 1000;
    /** Black bonus points. */
    private int blackBonus = 0;
    /** White bonus points. */
    private int whiteBonus = 0;
    /** Retract (unmake) one move, returning to
     * the state immediately before
     *  that move.  Requires that movesMade () > 0.
     *  You EDITED this.*/
    void retract() {
        if (movesMade() > 0) {
            Piece restored = _boardHistory.pop();
            _lastMove = _moves.get(_moves.size() - 1);
            _moves.remove(_moves.size() - 1);
            Square s1 = _lastMove.getFrom();
            Square s2 = _lastMove.getTo();
            Piece temp = _board[s2.index()];
            restoreCom(_lastMove, restored, temp);
            restoreScores(temp, restored, s1, s2);
            _map.put(s2.toString(), restored);
            _map.put(s1.toString(), temp);
            _board[s2.index()] = restored;
            _board[s1.index()] = temp;
            _turn = _turn.opposite();
        }
    }

    /** Restores CoM.
     *
     * @param move - Move that WAS made.
     * @param restored - restored piece
     * @param retracted - retracted piece
     */
    void restoreCom(Move move, Piece restored, Piece retracted) {
        if (restored == BP && retracted == WP
                || restored == WP && retracted == BP) {
            _comBlack = blackComs.pop();
            _comWhite = whiteComs.pop();
        } else if (restored == BP || retracted == BP) {
            _comBlack = blackComs.pop();
        } else if (restored == WP || retracted == WP) {
            _comWhite = whiteComs.pop();
        }
    }

    /** Restores the scores.
     *
     * @param retracted - Piece
     * @param restored - Piece
     * @param from - Square
     * @param to - Square
     */
    void restoreScores(Piece retracted, Piece restored,
                       Square from, Square to) {
        if (restored != EMP) {
            if (restored == BP) {
                blackBoardScore += valMap.get(to);
            } else {
                whiteBoardScore += valMap.get(to);
            }
        }
        if (retracted == BP) {
            blackBoardScore = blackBoardScore
                    - valMap.get(to) + valMap.get(from);
        } else {
            whiteBoardScore = whiteBoardScore
                    - valMap.get(to) + valMap.get(from);
        }
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff FROM - TO is a legal move
     * for the player currently on
     *  move.
     *  You EDITED this.*/
    boolean isLegal(Square from, Square to) {
        if (from.isValidMove(to)) {
            int numSquares = checkSquares(from, to);
            return numSquares == from.distance(to)
                    && !blocked(from, to);
        }
        return false;
    }

    /** Check the number of square in the line of
     * direction from Square from, to Square to.
     * @param from - Square that contains the moving piece.
     * @param to - Square where the piece is being moved.
     * @return number of squares in the horizontal/ vertical/
     * diagonal line.
     */
    private int checkSquares(Square from, Square to) {
        int countSq = 1;
        int dir1 = from.direction(to);
        int dir2 = (dir1 + 4) % 8;
        Square temp1 = from.moveDest(dir1, 1);
        Square temp2 = from.moveDest(dir2, 1);
        while (temp1 != null) {
            String s1 = temp1.toString();
            if (_map.get(s1) != EMP) {
                countSq++;
            }

            temp1 = temp1.moveDest(dir1, 1);

        }
        while (temp2 != null) {
            if (_map.get(temp2.toString()) != EMP) {
                countSq++;
            }
            temp2 = temp2.moveDest(dir2, 1);
        }
        return countSq;
    }

    /** Return true iff MOVE is legal for the player currently on move.
     *  The isCapture() property is ignored. */
    boolean isLegal(Move move) {
        return isLegal(move.getFrom(), move.getTo());
    }

    /** Corner moves for first move. */
    private ArrayList<Move> cornerMoveblack, cornerMovewhite;

    /** Returns the corner moves.
     * @param player = player
     * @return corner moves
     */
    ArrayList<Move> getCornerMove(int player) {
        if (player < 0) {
            return cornerMoveblack;
        } else {
            return cornerMovewhite;
        }
    }
    /** Return a sequence of all legal moves from this position.
     * You MADE this.*/
    void legalMoves() {
        allBlackMoves = new Stack<>();
        allWhiteMoves = new Stack<>();
        cornerMoveblack = new ArrayList<>();
        cornerMovewhite = new ArrayList<>();
        for (Square sq : _playingSquares.keySet()) {
            for (int dir = 0; dir < 8; dir++) {
                Square next = sq.moveDest(dir, 1);
                while (next != null) {
                    if (isLegal(Move.mv(sq, next))) {
                        if (_map.get(sq.toString()) == BP) {
                            if (next.col() == 0 && next.row() == 0
                                    || next.col() == 0
                                    && next.row() == 7
                                    || next.col() == 7
                                    && next.row() == 0
                                    || next.col() == 7
                                    && next.row() == 7) {
                                cornerMoveblack.add(Move.mv(sq, next));
                            }
                            allBlackMoves.push(Move.mv(sq, next));
                        } else {
                            if (next.col() == 0 && next.row() == 0
                                    || next.col() == 0
                                    && next.row() == 7
                                    || next.col() == 7
                                    && next.row() == 0
                                    || next.col() == 7
                                    && next.row() == 7) {
                                cornerMovewhite.add(Move.mv(sq, next));
                            }
                            allWhiteMoves.push(Move.mv(sq, next));
                        }
                    }
                    next = next.moveDest(dir, 1);
                }
            }
        }
    }

    /** Legal Moves for this Square.
     *
     * @param s - square
     * @return Set of reachable squares.
     */
    HashSet<Square> squareMoves(Square s) {
        nextMoves = new HashSet<>();
        for (int dir = 0; dir < 8; dir++) {
            Square next = s.moveDest(dir, 1);
            while (next != null) {
                if (isLegal(s, next)) {
                    nextMoves.add(next);
                }
                next = next.moveDest(dir, 1);
            }
        }
        return nextMoves;
    }

    /** Next reachable Squares. */
    private HashSet<Square> nextMoves;


    /** Stacks for all black and white moves possible. */
    private Stack<Move> allBlackMoves = new Stack<>();

    /** Stack for all white moves possible. */
    private Stack<Move> allWhiteMoves = new Stack<>();

    /** Return the stack of moves for this piece.
     * @param p - unused*/
    Stack<Move> getStack(int p) {
        if (p < 0) {
            return allBlackMoves;
        } else {
            return allWhiteMoves;
        }
    }

    /** Return true iff the game is over (either player has all his
     *  pieces continguous or there is a tie). */
    boolean gameOver() {
        return winner() != null;
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }

    /** Return the winning side, if any.  If the game is not over, result is
     *  null.  If the game has ended in a tie, returns EMP.
     *  You EDITED this. */
    Piece winner() {
        if (!_winnerKnown) {
            if (piecesContiguous(BP)) {
                _winner =  BP;
            } else if (piecesContiguous(WP)) {
                _winner =  WP;
            } else if (_moves.size() >= _moveLimit) {
                return EMP;
            }
            if ((_whiteRegionSizes.size() == 0
                    && turn() == WP) && piecesContiguous(BP)) {
                _winner = BP;
            } else if ((_blackRegionSizes.size() == 0
                    && turn() == BP) && piecesContiguous(WP)) {
                _winner = WP;
            }
            if (piecesContiguous(BP) && piecesContiguous(WP)) {
                _winner = turn().opposite();
            }
        }
        return _winner;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return true if a move from FROM to TO is blocked by an opposing
     *  piece or by a friendly piece on the target square.
     *  You EDITED this. */
    private boolean blocked(Square from, Square to) {
        int dir = from.direction(to);
        if (_map.get(to.toString()) == _map.get(from.toString())) {
            return true;
        }
        Square temp = from.moveDest(dir, 1);
        while (temp != null && !temp.toString().equals(to.toString())) {
            if (_map.get(temp.toString())
                    == _map.get(from.toString()).opposite()) {
                return true;
            }
            temp = temp.moveDest(dir, 1);
        }
        return false;
    }

    /** Lists each of the regions of pieces. */
    private ArrayList<HashSet<Square>> blackRegions, whiteRegions;

    /** Container used to stop loop if number of squares checked
     * exceeds total dquares on board. */
    private int numTrue;

    /** Return the size of the as-yet unvisited cluster of squares
     *  containing P at and adjacent to SQ.  VISITED indicates squares that
     *  have already been processed or are in different clusters.  Update
     *  VISITED to reflect squares counted.
     *  You EDITED this.
     *  @param p piece
     *  @param sq square
     *  @param list list
     *  @param visited boolean array
     *  @return integer value*/
    private int numContig(Square sq, boolean[][] visited,
                          Piece p, HashSet<Square> list) {
        Square[] adjSquares = sq.adjacent();
        visited[sq.row()][sq.col()] = true;
        int count = 1;
        for (Square s : adjSquares) {

            if (!visited[s.row()][s.col()]
                    && _map.get(s.toString()) != p.opposite()) {
                numTrue++;
                visited[s.row()][s.col()] = true;
                if (_map.get(s.toString()) == p) {
                    list.add(s);
                    count += numContig(s, visited, p, list);
                }

            }

        }
        return count;
    }


    /** Set the values of _whiteRegionSizes and _blackRegionSizes.
     * You EDITED this.*/
    private void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }
        blackRegions = new ArrayList<>();
        whiteRegions = new ArrayList<>();
        HashSet<Square> blackSet;
        HashSet<Square> whiteSet;
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();
        boolean[][] visitHistory = new boolean[BOARD_SIZE][BOARD_SIZE];
        numTrue = 1;
        for (String key : _map.keySet()) {
            if (!visitHistory[sq(key).row()][sq(key).col()]) {
                if (_map.get(key) == WP) {
                    whiteSet = new HashSet<>();
                    whiteSet.add(sq(key));
                    _whiteRegionSizes.add(numContig(sq(key),
                            visitHistory, WP, whiteSet));
                    whiteRegions.add(whiteSet);
                } else if (_map.get(key) == BP) {
                    blackSet = new HashSet<>();
                    blackSet.add(sq(key));
                    _blackRegionSizes.add(numContig(sq(key),
                            visitHistory, BP, blackSet));
                    blackRegions.add(blackSet);
                }
                if (numTrue >= 64) {
                    break;
                }
            }
        }

        Collections.sort(_whiteRegionSizes, Collections.reverseOrder());
        Collections.sort(_blackRegionSizes, Collections.reverseOrder());
        _subsetsInitialized = true;



    }

    /** Return the sizes of all the regions in the current union-find
     *  structure for side S. */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {
            return _whiteRegionSizes;
        } else {
            return _blackRegionSizes;
        }
    }



    /** The standard initial configuration for Lines of Action (bottom row
     *  first). */
    static final Piece[][] INITIAL_PIECES = {
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };

    /** Current contents of the board.  Square S is at _board[S.index()]. */
    private final Piece[] _board = new Piece[BOARD_SIZE  * BOARD_SIZE];

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();

    /** Gets the moves made.
     * @return List of moves made.*/
    public ArrayList<Move> getMoves() {
        return _moves;
    }
    /** Current side on move. */
    private Piece _turn;
    /** Limit on number of moves before tie is declared.  */
    private int _moveLimit;
    /** True iff the value of _winner is known to be valid. */
    private boolean _winnerKnown;
    /** Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     *  in progress).  Use only if _winnerKnown. */
    private Piece _winner;

    /** True iff subsets computation is up-to-date. */
    private boolean _subsetsInitialized;

    /** The last move made.
     * YOU ADDED EVERYTHING BELOW THIS.
     */
    private Move _lastMove;
    /** List of the sizes of continguous clusters of pieces, by color. */
    private final ArrayList<Integer>
        _whiteRegionSizes = new ArrayList<>(),
        _blackRegionSizes = new ArrayList<>();

    /** The mapping of a square to the piece it contains. */
    private HashMap<String, Piece> _map;

     /** Array of alphabet chars used. */
    private char[] alpha = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};

    /** This histore of this board. */
    private  Stack<Piece> _boardHistory;

    /** Alphabet at the index.
     * @param index index
     * @return alphabet
     * */
    private char alphAt(int index) {
        return alpha[index - 1];
    }

    /** Number of black pieces on the board. */
    private int sizeBlack;
    /** number of white pieces on the board. */
    private int sizeWhite;

    /** Get the number of black pieces.
     * @return integer*/
    int getBlackNum() {
        return sizeBlack;
    }

    /** Get the number of white pieces.
     * @return integer*/
    int getWhiteNum() {
        return sizeWhite;
    }

    /** Center of mass for black. */
    private Square _comBlack;

    /** Center of mass for white. */
    private Square _comWhite;

    /** Get the sizes of all the regions of black.
     * @return list of blacks*/
    ArrayList<Integer> getBlackList() {
        return _blackRegionSizes;
    }

    /** Get the sizes of all the regions of white.
     * @return list of whites*/
    ArrayList<Integer> getWhiteList() {
        return _whiteRegionSizes;
    }

    /** Initially meant to store the board value after each move,
     * but unused due to efficiency concerns. */
    private int _boardVal;

    /** Stack of all white and black squares. */
    private Stack<Square> whiteSquares, blackSquares;

    /** Stack of all distances. UNUSED. */
    private Stack<Integer> whiteDist, blackDist;

    /** Sets the distance of each region
     * of pieces from their CoM. UNUSED.
     * @param s Square
     * @param list Arraylist
     * @return integer*/
    int setDistance(List<HashSet<Square>> list, Square s) {
        Iterator<HashSet<Square>> iterator = list.iterator();
        int distance = 0;
        HashSet<Square> sqSet;
        while (iterator.hasNext()) {
            sqSet = iterator.next();
            Square sq = getCoM(sqSet);
            distance += s.distance(sq) * 100;
        }
        return distance / list.size();
    }

    /** Gets the CoM of the set of squares.
     * Unused as of now.
     * @param set = set of squares
     * @return Square*/
    Square getCoM(Set<Square> set) {
        int col = 0;
        int row = 0;
        for (Square sq : set) {
            col += sq.col();
            row += sq.row();
        }
        return Square.sq(col / set.size(), row / set.size());
    }


    /** This also computes CoM distance.
     * @param p piece
     * @param dist stack of distances
     * @param sq square
     * @return double*/
    double comDist(Stack<Square> dist, Square sq, Piece p) {
        Square temp;
        int num;
        double totdist = 0.0;
        if (p == BP) {
            num = sizeBlack;
        } else {
            num = sizeWhite;
        }
        while (!dist.empty()) {
            temp = dist.pop();
            totdist += temp.distance(sq);
        }
        return totdist / num;
    }

    /** Returns avg distance.
     *
     * @return avg distance
     */
    private int avgDistance() {
        int blackdist = 0;
        int whitedist = 0;
        for (Square s : _playingSquares.keySet()) {
            if (_playingSquares.get(s) == BP) {
                blackdist += (s.distance(_comBlack)
                        * MULTIPLIER * _multip);
            } else {
                whitedist += (s.distance(_comWhite)
                        * MULTIPLIER * _multip);
            }
        }
        return (whitedist) - (blackdist);
    }

    /** Double multiplier. */
    private final int _multip = 150;

    /**
     * My evaluation of the worth of each position on the board.
     */
    private static final int[][] BOARD_VALUES = {
            {-1250, -750, -750, 0, 0, -750, -750, -1250},
            {-750, 550, 550, 550, 550, 550, 550, -750},
            {-750, 550, 1450, 1400, 1400, 1450, 550, 0},
            {0, 550, 1775, 2750, 2750, 1775, 550, 0},
            {0, 550, 1775, 2750, 2450, 1775, 550, 0},
            {-750, 550, 1450, 1400, 1400, 1450, 550, -750},
            {-750, 550, 550, 550, 550, 550, 550, -750},
            {-1250, -750, -750, 0, 0, -750, -750, -1250}
    };

    /** Winning value. */
    private static final int WHITE_VAL = Integer.MAX_VALUE - 20;

    /** Winning value. */
    private static final int BLACK_VAL = Integer.MIN_VALUE + 20;

    /** Mapping of a square with it's fixed value. */
    private HashMap<Square, Integer> valMap;

    /** Set the map mentioned above. */
    void setMap() {
        valMap = new HashMap<>();
        for (int i = 0; i < BOARD_VALUES.length; i++) {
            for (int j = 0; j < BOARD_VALUES[0].length; j++) {
                Square sq = sq(j, i);
                valMap.put(sq, BOARD_VALUES[i][j]);
            }

        }
    }
    /** Get the position score of the board.
     * Calculates the value of each piece's position,
     * and integrates the average distance of each piece from
     * it's collective center of mass.
     * @return integer*/
    int posnScore() {
        if (gameOver()) {
            if (_winner == BP) {
                return BLACK_VAL;
            } else {
                return WHITE_VAL;
            }
        }
        whiteBoardScore -= ((sizeWhite - sizeBlack) * posnMukltiplier);
        blackBoardScore -= ((sizeBlack - sizeWhite) * posnMukltiplier);
        return ((whiteBoardScore + whiteBonus - _whitePenalty)
                - (avgDistance())
                - (blackBoardScore + blackBonus - _blackPenalty));
    }

    /** Final multiplier. */
    private final int posnMukltiplier = 500;
    /** Map of squares on board (non empty). **/
    private HashMap<Square, Piece> _playingSquares;

    /** Return the mapping of squares with pieces.
     *
     * @return Map of Square -> piece
     */
    HashMap<Square, Piece> getPlayers() {
        return _playingSquares;
    }

    /** Multiplier for avg dist. */
    private static final int MULTIPLIER = 150;

    /** Gets the capture penalty.
     * @return penalty value */
    private int getPenalty() {
        if (_captured) {
            return CAPTURE_PENALTY;
        } else {
            return 0;
        }
    }
    /** Is P greater than the opp ?
     * @param p piece
     * @return boolean*/
    boolean greater(Piece p) {
        if (p == WP) {
            return sizeWhite > sizeBlack;
        } else {
            return sizeBlack > sizeWhite;
        }
    }

    /** Return the center of mass for the piece.
     * @param p - piece
     * @return square
     */
    Square getCom(Piece p) {
        if (p == WP) {
            return _comWhite;
        } else {
            return _comBlack;
        }
    }
}
