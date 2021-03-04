/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import ucb.gui2.Pad;

import java.awt.geom.Ellipse2D;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.List;
import java.util.ArrayList;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import static loa.Piece.BP;
import static loa.Piece.WP;
import static loa.Square.sq;

/** A widget that displays a Loa game.
 *  @author Dhruv Sirohi
 */
class BoardWidget extends Pad {



    /** Squares on each side of the board.
     * Parameters controlling sizes, speeds, colors, and fonts.*/
    static final int SIZE = Square.BOARD_SIZE;

    /** Colors of empty squares, pieces, boundaries, and markings. */
    static final Color
        BLACK_COLOR = new Color(0.765f, 0.0f, 0.0117f),
        WHITE_COLOR = Color.white,
        DARK_SQUARE_COLOR = new Color(0.816f, 0.543f, 0.277f),
        LIGHT_SQUARE_COLOR = new Color(1.0f, 0.805f, 0.617f),
        BORDER_COLOR = new Color(0.408f, 0.271f, 0.138f),
        GRID_LINE_COLOR = Color.black,
        PIECE_BOUNDARY_COLOR = Color.black;

    /** Width of border around board (pixels). */
    static final int BORDER_WIDTH = 6;
    /** Distance from edge of window to board (pixels). */
    static final int MARGIN = 16;
    /** Dimension of single square on the board (pixels). */
    static final int SQUARE_SIDE = 30;
    /** Dimension of component containing the board and margin (pixels). */
    static final int BOARD_SIDE =
        SQUARE_SIDE * SIZE + 2 * MARGIN + 2 * BORDER_WIDTH;
    /** Diameter of piece (pixels). */
    static final int PIECE_SIZE = (int) Math.round(0.8 * SQUARE_SIDE);
    /** Distance of edge of piece to edge of square it's on. */
    static final int PIECE_OFFSET =
        (int) Math.round(0.5 * (SQUARE_SIDE - PIECE_SIZE));

    /** Strokes to provide boundary around board and outline of piece. */
    static final BasicStroke
        BORDER_STROKE = new BasicStroke(BORDER_WIDTH, BasicStroke.CAP_ROUND,
                                        BasicStroke.JOIN_ROUND),
        PIECE_BOUNDARY_STROKE = new BasicStroke(1.0f);

    /**A list of Shapes. */
    private List<Ellipse2D> shapes = new ArrayList<Ellipse2D>();


    /** A graphical representation of a Loa board that sends commands
     *  derived from mouse clicks to COMMANDS.  */
    BoardWidget(ArrayBlockingQueue<String> commands) {

        nextColored = new HashSet<>();
        _commands = commands;
        shapes.add(new Ellipse2D.Double(newSize,
                newSize, doubleSize, thirdSize));
        setMouseHandler("press", this::mouseAction);
        setMouseHandler("release", this::mouseAction);
        setMouseHandler("drag", this::mouseAction);
        setPreferredSize(BOARD_SIDE, BOARD_SIDE);
        _acceptingMoves = false;
    }

    /** Added to avoid magic number error.
     * Used to create a new ellipse. */
    private final int newSize = 10;
    /** Added to avoid magic number error.
     * Used to create a new ellipse. */
    private final int doubleSize = 20;
    /** Added to avoid magic number error.
     * Used to create a new ellipse. */
    private final int thirdSize = 30;
    /** Draw the bare board G.  */
    private void drawGrid(Graphics2D g) {
        g.setColor(LIGHT_SQUARE_COLOR);
        g.fillRect(0, 0, BOARD_SIDE, BOARD_SIDE);

        g.setColor(DARK_SQUARE_COLOR);
        for (int y = 0; y < SIZE; y += 1) {
            for (int x = (y + 1) % 2; x < SIZE; x += 2) {
                g.fillRect(x * SQUARE_SIDE + MARGIN + BORDER_WIDTH,
                           y * SQUARE_SIDE + MARGIN + BORDER_WIDTH,
                           SQUARE_SIDE, SQUARE_SIDE);
            }
        }

        g.setColor(BORDER_COLOR);
        g.setStroke(BORDER_STROKE);
        g.drawRect(MARGIN + BORDER_WIDTH / 2, MARGIN + BORDER_WIDTH / 2,
                   BOARD_SIDE - 2 * MARGIN - BORDER_WIDTH,
                   BOARD_SIDE - 2 * MARGIN - BORDER_WIDTH);

    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        drawGrid(g);
        for (Square sq : Square.ALL_SQUARES) {
            if (nextColored.contains(sq)) {
                drawPiece(g, sq, true);
            } else {
                drawPiece(g, sq);
            }
        }
        if (lastMove != null) {
            g.drawLine(cx(lastMove.getFrom()) + SQUARE_SIDE / 2,
                    cy(lastMove.getFrom()) + SQUARE_SIDE / 2,
                    cx(lastMove.getTo()) + SQUARE_SIDE / 2,
                    cy(lastMove.getTo()) + SQUARE_SIDE / 2);
        }
    }

    /** Squares that are to be pointed out to the user. */
    private HashSet<Square> nextColored;

    /**Draw empty pieces on square where the users selection can move to.
     *
     * @param g - Graphics2d object
     * @param s - Square where piece is drawn
     * @param selected - To override the method
     */
    private void drawPiece(Graphics2D g, Square s, Boolean selected) {
        Piece p = _board.get(s);
        switch (p) {
        case EMP:
            g.drawOval(cx(s) + PIECE_OFFSET,
                    cy(s) + PIECE_OFFSET,
                    PIECE_SIZE, PIECE_SIZE);
            return;
        case WP:
            g.setColor(WHITE_COLOR);
            break;
        case BP:
            g.setColor(BLACK_COLOR);
            break;
        default:
            assert false;
        }
        g.fillOval(cx(s) + PIECE_OFFSET, cy(s)
                + PIECE_OFFSET, PIECE_SIZE, PIECE_SIZE);
        if (p == WP || p == BP) {
            g.setColor(Color.RED);
        }
        g.setStroke(new BasicStroke(3.0f));
        g.drawOval(cx(s) + PIECE_OFFSET,
                cy(s) + PIECE_OFFSET,
                PIECE_SIZE, PIECE_SIZE);
    }
    /** Draw the contents of S on G. */
    private void drawPiece(Graphics2D g, Square s) {
        Piece p = _board.get(s);
        switch (p) {
        case EMP:
            return;
        case WP:
            g.setColor(WHITE_COLOR);
            break;
        case BP:
            g.setColor(BLACK_COLOR);
            break;
        default:
            assert false;
        }
        g.fillOval(cx(s) + PIECE_OFFSET, cy(s) + PIECE_OFFSET,
                   PIECE_SIZE, PIECE_SIZE);
        g.setColor(PIECE_BOUNDARY_COLOR);
        g.setStroke(PIECE_BOUNDARY_STROKE);
        g.drawOval(cx(s) + PIECE_OFFSET, cy(s) + PIECE_OFFSET,
                   PIECE_SIZE, PIECE_SIZE);
    }


    /** Square to and form. */
    private Square to, from;
    /** Handle a mouse-button push on S.
     * You FIXED this.*/
    private void mousePressed(Square s) {
        lastMove = null;
        nextColored = new HashSet<>();
        if (from == null) {
            if (_board.get(s) == BP && _board.turn() == BP) {
                from = s;
                nextColored = _board.squareMoves(s);
            } else if (_board.get(s) == WP && _board.turn() == WP) {
                from = s;
                nextColored = _board.squareMoves(s);
            }
        }
        repaint();
    }

    /** Handle a mouse-button release on S.
     * Maybe FIX THIS?  */
    private void mouseReleased(Square s) {
        if (from != null) {
            to = s;
            if (_board.isLegal(from, to)) {
                Move move = Move.mv(from, to);
                _board.makeMove(move);
                _commands.add(move.toString());
                lastMove = Move.mv(from, to);
                from = to = null;
            } else {
                from = null;
                to = null;
            }
        }
        nextColored = new HashSet<>();
        repaint();
    }

    /** Storage to draw line. */
    private Move lastMove;

    /** Handle mouse click event E. */
    private synchronized void mouseAction(String unused, MouseEvent e) {
        int xpos = e.getX(), ypos = e.getY();
        int x = (xpos - cx(0)) / SQUARE_SIDE,
            y = (cy(SIZE - 1) - ypos) / SQUARE_SIDE + SIZE - 1;
        if (_acceptingMoves
            && x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
            Square s = sq(x, y);
            switch (e.getID()) {
            case MouseEvent.MOUSE_PRESSED:
                mousePressed(s);
                break;
            case MouseEvent.MOUSE_RELEASED:
                mouseReleased(s);
                break;
            default:
                break;
            }
        }
    }

    /** Revise the displayed board according to BOARD.
     * You FIX this! (MAYBE)*/
    synchronized void update(Board board) {
        _board.copyFrom(board);
        repaint();
    }

    /** Turn on move collection iff COLLECTING, and clear any current
     *  partial selection.  When move collection is off, ignore clicks on
     *  the board.
     *  Maybe FIX THIS?*/
    void setMoveCollection(boolean collecting) {
        _acceptingMoves = collecting;
        repaint();
    }

    /** Return x-pixel coordinate of the left corners of column X
     *  relative to the upper-left corner of the board. */
    private int cx(int x) {
        return x * SQUARE_SIDE + MARGIN + BORDER_WIDTH;
    }

    /** Return y-pixel coordinate of the upper corners of row Y
     *  relative to the upper-left corner of the board. */
    private int cy(int y) {
        return (SIZE - y - 1) * SQUARE_SIDE + MARGIN + BORDER_WIDTH;
    }

    /** Return x-pixel coordinate of the left corner of S
     *  relative to the upper-left corner of the board. */
    private int cx(Square s) {
        return cx(s.col());
    }

    /** Return y-pixel coordinate of the upper corner of S
     *  relative to the upper-left corner of the board. */
    private int cy(Square s) {
        return cy(s.row());
    }

    /** Queue on which to post move commands (from mouse clicks). */
    private ArrayBlockingQueue<String> _commands;
    /** Board being displayed. */
    private final Board _board = new Board();

    /** True iff accepting moves from user. */
    private boolean _acceptingMoves;

}
