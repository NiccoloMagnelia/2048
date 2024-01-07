package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author Niccolo A. Magnelia
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board _board;
    /** Current score. */
    private int _score;
    /** Maximum score so far.  Updated when game ends. */
    private int _maxScore;
    /** True iff game is ended. */
    private boolean _gameOver;
    private boolean changer;
    private boolean changerChanger;
    private boolean scoreTurn;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        int[][] size_model = new int[size][size];
        _board = new Board(size_model);
        _score = 0;
        _maxScore = 0;
        _gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        _board = new Board(rawValues);
        _score = score;
        _maxScore = maxScore;
        _gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     */
    public Tile tile(int col, int row) {
        return _board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return _board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (_gameOver) {
            _maxScore = Math.max(_score, _maxScore);
        }
        return _gameOver;
    }

    /** Return the current score. */
    public int score() {
        return _score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return _maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        _score = 0;
        _gameOver = false;
        _board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        _board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     */
    public int colNorthHelper(int colNum, Board b) {
        changer = false;
        boolean prevMerge = false;
        int score_tracker = 0;
        int topSpot = b.size() - 1;

        for (int i = b.size() - 2; i >= 0; i--) {
            if (b.tile(colNum, i) != null) {
                // no top spot
                if (b.tile(colNum, topSpot) == null) {
                    b.move(colNum, topSpot, b.tile(colNum, i));
                    changer = true;
                    // merge top spot
                } else if (b.tile(colNum, topSpot).value() == b.tile(colNum, i).value()) {
                    if (!prevMerge) {
                        b.move(colNum, topSpot, b.tile(colNum, i));
                        changer = true;
                        prevMerge = true;
                        score_tracker += b.tile(colNum, topSpot).value();
                    } else {
                        b.move(colNum, topSpot - 1, b.tile(colNum, i));
                        changer = true;
                        System.out.println("no merge called");
                        topSpot -= 1;
                        prevMerge = false;
                    }
                } else {
                    b.move(colNum, topSpot - 1, b.tile(colNum, i));
                    if (topSpot - 1 != i) { changer = true; }
                    topSpot -= 1;
                    prevMerge = false;
                }
            }
        }
        return score_tracker;
    }

    public int colSouthHelper(int colNum, Board b) {
        changer = false;
        boolean prevMerge = false;
        int topSpot = 0;
        int score_tracker = 0;

        for (int i = 1; i < b.size(); i ++) {
            if (b.tile(colNum, i) != null) {
                // no top spot
                if (b.tile(colNum, topSpot) == null) {
                    b.move(colNum, topSpot, b.tile(colNum, i));
                    changer = true;
                    // merge top spot
                } else if (b.tile(colNum, topSpot).value() == b.tile(colNum, i).value()) {
                    if (!prevMerge) {
                        b.move(colNum, topSpot, b.tile(colNum, i));
                        changer = true;
                        prevMerge = true;
                        score_tracker += b.tile(colNum, topSpot).value();
                    } else {
                        b.move(colNum, topSpot + 1, b.tile(colNum, i));
                        changer = true;
                        topSpot += 1;
                        prevMerge = false;

                    }
                } else {
                    b.move(colNum, topSpot + 1, b.tile(colNum, i));
                    if (topSpot + 1 != i) { changer = true; }
                    topSpot += 1;
                    prevMerge = false;
                }
            }
        }
        return score_tracker;
    }

    public int colEastHelper(int rowNum, Board b) {
        changer = false;
        boolean prevMerge = false;
        int topSpot = b.size() - 1;
        int score_tracker = 0;

        for (int i = b.size() - 2; i >= 0; i--) {
            if (b.tile(i, rowNum) != null) {
                // no top spot
                if (b.tile(topSpot, rowNum) == null) {
                    b.move(topSpot, rowNum, b.tile(i, rowNum));
                    changer = true;
                    // merge top spot
                } else if (b.tile(topSpot, rowNum).value() == b.tile(i, rowNum).value()) {
                    if (!prevMerge) {
                        b.move(topSpot, rowNum, b.tile(i, rowNum));
                        changer = true;
                        prevMerge = true;
                        score_tracker += b.tile(topSpot, rowNum).value();
                    } else {
                        b.move(topSpot - 1, rowNum, b.tile(i, rowNum));
                        changer = true;
                        topSpot -= 1;
                        prevMerge = false;
                    }
                } else {
                    b.move(topSpot - 1, rowNum, b.tile(i, rowNum));
                    if (topSpot - 1 != i) { changer = true; }
                    topSpot -= 1;
                    prevMerge = false;
                }
            }
        }
        return score_tracker;
    }

    public int colWestHelper(int rowNum, Board b) {
        changer = false;
        boolean prevMerge = false;
        int topSpot = 0;
        int score_tracker = 0;

        for (int i = 1; i < b.size(); i++) {
            if (b.tile(i, rowNum) != null) {
                // no top spot
                if (b.tile(topSpot, rowNum) == null) {
                    b.move(topSpot, rowNum, b.tile(i, rowNum));
                    changer = true;
                    // merge top spot
                } else if (b.tile(topSpot, rowNum).value() == b.tile(i, rowNum).value()) {
                    if (!prevMerge) {
                        b.move(topSpot, rowNum, b.tile(i, rowNum));
                        changer = true;
                        prevMerge = true;
                        score_tracker += b.tile(topSpot, rowNum).value();
                    } else {
                        b.move(topSpot + 1, rowNum, b.tile(i, rowNum));
                        changer = true;
                        topSpot += 1;
                        prevMerge = false;
                    }
                } else {
                    b.move(topSpot + 1, rowNum, b.tile(i, rowNum));
                    if (topSpot + 1 != i) { changer = true; }
                    topSpot += 1;
                    prevMerge = false;
                }
            }
        }
        return score_tracker;
    }





    public boolean tilt(Side side) {
        boolean changed;
        changed = false;
        changerChanger = false;
        // tilt NORTH
        if (side == Side.NORTH) {
            for (int col = 0; col < _board.size(); col++) {
                _score += colNorthHelper(col, _board);
                if (changer) {
                    changerChanger = true;
                }
            }
            changed = changerChanger;
        }

        if (side == Side.SOUTH) {
            for (int col = 0; col < _board.size(); col++) {
                _score += colSouthHelper(col, _board);
                if (changer) {
                    changerChanger = true;
                }
            }
            changed = changerChanger;
        }

        if (side == Side.EAST) {
            for (int row = 0; row < _board.size(); row++) {
                _score += colEastHelper(row, _board);
                if (changer) {
                    changerChanger = true;
                }
            }
            changed = changerChanger;
        }

        if (side == Side.WEST) {
            for (int row = 0; row < _board.size(); row++) {
                _score += colWestHelper(row, _board);
                if (changer) {
                    changerChanger = true;
                }
            }
            changed = changerChanger;
        }


        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        _gameOver = checkGameOver(_board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     *  b(col, row)
     */
    public static boolean emptySpaceExists(Board b) {
        for (int col = 0; col < b.size(); col++) {
            for (int row = 0; row < b.size(); row ++) {
                if (b.tile(col, row) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        for (int col = 0; col < b.size(); col++) {
            for (int row = 0; row < b.size(); row ++) {
                if (b.tile(col, row) != null && b.tile(col, row).value() == MAX_PIECE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        boolean firstTile = true;
        int adjacentTileVal = 0;
        if (emptySpaceExists(b)) {
            return true;
        } else {
            for (int col = 0; col < b.size(); col++) {
                for (int row = 0; row < b.size(); row++) {
                    if (firstTile) {
                        if (b.tile(col, row) != null) {
                            adjacentTileVal = b.tile(col, row).value();
                            firstTile = false;
                        }
                    } else {
                        if (b.tile(col, row).value() == adjacentTileVal) {
                            return true;
                        } else {
                            adjacentTileVal = b.tile(col, row).value();
                        }
                    }
                }
                firstTile = true;
            }
            for (int row = 0; row < b.size(); row++) {
                for (int col = 0; col < b.size(); col++) {
                    if (firstTile) {
                        if (b.tile(col, row) != null) {
                            adjacentTileVal = b.tile(col, row).value();
                            firstTile = false;
                        }
                    } else {
                        if (b.tile(col, row).value() == adjacentTileVal) {
                            return true;
                        } else {
                            adjacentTileVal = b.tile(col, row).value();
                        }
                    }
                }
                firstTile = true;
            }
        }
        return false;
    }

    /** Returns the model as a string, used for debugging. */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    /** Returns whether two models are equal. */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    /** Returns hash code of Model’s string. */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
