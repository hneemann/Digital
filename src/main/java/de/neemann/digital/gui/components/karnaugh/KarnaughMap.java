/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.karnaugh;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Creates the covers needed to draw a kv map
 */
public class KarnaughMap implements Iterable<KarnaughMap.Cover> {

    private final ArrayList<Cell> cells;
    private final List<Variable> vars;
    private final ArrayList<Cover> covers;
    private final Header headerLeft;
    private final Header headerRight;
    private final Header headerBottom;
    private final Header headerTop;

    /**
     * Creates a new instance
     *
     * @param vars the variables used
     * @param expr the expression
     * @throws KarnaughException KarnaughException
     */
    public KarnaughMap(List<Variable> vars, Expression expr) throws KarnaughException {
        this(vars, expr, 0, null);
    }

    /**
     * Checks is the given swap list is valid (not null and of the correct size).
     * If so, the given list is returned, IF not, a simple, non swapping default swap
     * list is created.
     *
     * @param swap the old swap list
     * @param size the required size of the list
     * @return the valid swap list
     */
    public static int[] checkSwap(int[] swap, int size) {
        if (swap != null && swap.length == size)
            return swap;

        int[] s = new int[size];
        for (int i = 0; i < s.length; i++) s[i] = i;
        return s;
    }

    /**
     * Creates a new instance
     *
     * @param vars the variables used
     * @param expr the expression
     * @param mode the layout mode
     * @param swap describes, how to swap the variables
     * @throws KarnaughException KarnaughException
     */
    public KarnaughMap(List<Variable> vars, Expression expr, int mode, int[] swap) throws KarnaughException {
        swap = checkSwap(swap, vars.size());
        this.vars = vars;
        cells = new ArrayList<>();
        covers = new ArrayList<>();

        boolean leftMode = (mode & 1) != 0;
        boolean rightMode = (mode & 2) != 0;
        boolean topMode = (mode & 4) != 0;
        boolean bottomMode = (mode & 8) != 0;

        switch (vars.size()) {
            case 2:  // create the needed KV cells
                for (int row = 0; row < 2; row++)
                    for (int col = 0; col < 2; col++)
                        cells.add(new Cell(row, col));

                headerLeft = new Header(swap[0], !leftMode, leftMode).toRows(2, this);
                headerTop = new Header(swap[1], !topMode, topMode).toCols(2, this);
                headerRight = null;
                headerBottom = null;
                break;
            case 3:
                for (int row = 0; row < 2; row++)
                    for (int col = 0; col < 4; col++)
                        cells.add(new Cell(row, col));

                headerLeft = new Header(swap[0], !leftMode, leftMode).toRows(4, this);
                headerTop = new Header(swap[1], !topMode, !topMode, topMode, topMode).toCols(2, this);
                headerBottom = new Header(swap[2], !bottomMode, bottomMode, bottomMode, !bottomMode).toCols(2, this);
                headerRight = null;
                break;
            case 4:
                for (int row = 0; row < 4; row++)
                    for (int col = 0; col < 4; col++)
                        cells.add(new Cell(row, col));

                headerLeft = new Header(swap[0], !leftMode, !leftMode, leftMode, leftMode).toRows(4, this);
                headerRight = new Header(swap[1], !rightMode, rightMode, rightMode, !rightMode).toRows(4, this);
                headerTop = new Header(swap[2], !topMode, !topMode, topMode, topMode).toCols(4, this);
                headerBottom = new Header(swap[3], !bottomMode, bottomMode, bottomMode, !bottomMode).toCols(4, this);
                break;
            default:
                throw new KarnaughException(Lang.get("err_toManyVars"));
        }
        for (Cell c : cells)   // set the row index of the bool table to the cells
            c.createBoolTableRow();

        addExpression(expr);   // create the covers
    }

    /**
     * Returns the cell at the given position
     *
     * @param row the row
     * @param col the column
     * @return the cell at this position
     */
    public Cell getCell(int row, int col) {
        for (Cell cell : cells)
            if (cell.is(row, col)) return cell;
        throw new RuntimeException("cell not found");
    }

    /**
     * @return all cells
     */
    public ArrayList<Cell> getCells() {
        return cells;
    }

    private void addExpression(Expression expr) throws KarnaughException {
        if (expr instanceof Not || expr instanceof Variable) {
            addCover(expr);
        } else if (expr instanceof Operation.And) {
            addCover(((Operation.And) expr).getExpressions());
        } else if (expr instanceof Operation.Or) {
            for (Expression and : ((Operation.Or) expr).getExpressions())
                if (and instanceof Operation.And)
                    addCover(((Operation.And) and).getExpressions());
                else if (and instanceof Not || and instanceof Variable)
                    addCover(and);
                else
                    throw new KarnaughException(Lang.get("err_invalidExpression"));
        } else if (!(expr instanceof Constant))
            throw new KarnaughException(Lang.get("err_invalidExpression"));
    }

    private void addCover(Expression expr) throws KarnaughException {
        addCoverToCells(new Cover().add(getVarOf(expr)));
    }

    private void addCover(ArrayList<Expression> expressions) throws KarnaughException {
        Cover cover = new Cover();
        for (Expression expr : expressions)
            cover.add(getVarOf(expr));
        addCoverToCells(cover);
    }

    private void addCoverToCells(Cover cover) {
        covers.add(cover);

        HashSet<Integer> insetsUsed = new HashSet<>();
        for (Cell cell : cells)
            cell.addCoverToCell(cover, insetsUsed);
        for (int i = 0; i < 8; i++)
            if (!insetsUsed.contains(i)) {
                cover.inset = i;
                break;
            }
    }

    private VarState getVarOf(Expression expression) throws KarnaughException {
        String name = null;
        boolean invert = false;
        if (expression instanceof Variable) {
            name = ((Variable) expression).getIdentifier();
            invert = false;
        } else if (expression instanceof Not) {
            Expression ex = ((Not) expression).getExpression();
            if (ex instanceof Variable) {
                name = ((Variable) ex).getIdentifier();
                invert = true;
            }
        }
        if (name == null) throw new KarnaughException(Lang.get("err_invalidExpression"));

        int var = vars.indexOf(new Variable(name));
        if (var < 0) throw new KarnaughException(Lang.get("err_invalidExpression"));

        return new VarState(var, invert);
    }

    @Override
    public Iterator<Cover> iterator() {
        return covers.iterator();
    }

    /**
     * @return the number of covers
     */
    public int size() {
        return covers.size();
    }

    /**
     * @return the rows of the table
     */
    public int getRows() {
        return headerLeft.size();
    }

    /**
     * @return the cols of the table
     */
    public int getColumns() {
        return headerTop.size();
    }

    /**
     * @return the left header
     */
    public Header getHeaderLeft() {
        return headerLeft;
    }

    /**
     * @return the right header
     */
    public Header getHeaderRight() {
        return headerRight;
    }

    /**
     * @return the bottom header
     */
    public Header getHeaderBottom() {
        return headerBottom;
    }

    /**
     * @return the top header
     */
    public Header getHeaderTop() {
        return headerTop;
    }

    /**
     * a single cell in the kv map
     */
    public static final class Cell {
        private final int row;
        private final int col;
        private final ArrayList<VarState> minTerm; // min term  of the cell
        private final ArrayList<Cover> covers;
        private int boolTableRow;

        private Cell(int row, int col) {
            this.row = row;
            this.col = col;
            minTerm = new ArrayList<>();
            covers = new ArrayList<>();
        }

        private void add(VarState varState) {
            minTerm.add(varState);
        }

        private boolean is(int row, int col) {
            return (this.row == row) && (this.col == col);
        }

        private void addCoverToCell(Cover cover, HashSet<Integer> insetsUsed) {
            for (VarState s : minTerm)
                if (cover.contains(s.not()))
                    return;

            for (Cover c : covers)
                insetsUsed.add(c.inset);

            covers.add(cover);
            cover.incCellCount();
        }

        private boolean contains(Cover cover) {
            return covers.contains(cover);
        }

        private void createBoolTableRow() {
            int tableCols = minTerm.size();
            boolTableRow = 0;
            for (VarState i : minTerm) {
                if (!i.invert)
                    boolTableRow += (1 << (tableCols - i.num - 1));
            }
        }

        /**
         * @return the row
         */
        public int getRow() {
            return row;
        }

        /**
         * @return the column
         */
        public int getCol() {
            return col;
        }

        /**
         * @return the row in the truth table this cell belongs to
         */
        public int getBoolTableRow() {
            return boolTableRow;
        }

        boolean isVarInMinTerm(int var, boolean invert) {
            return minTerm.contains(new VarState(var, invert));
        }
    }

    private static final class VarState {
        private final int num;        // number of the variable in the vars list
        private final boolean invert; // true if variable is inverted

        private VarState(int num, boolean invert) {
            this.num = num;
            this.invert = invert;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VarState varState = (VarState) o;

            if (num != varState.num) return false;
            return invert == varState.invert;
        }

        @Override
        public int hashCode() {
            int result = num;
            result = 31 * result + (invert ? 1 : 0);
            return result;
        }

        private VarState not() {
            return new VarState(num, !invert);
        }
    }

    /**
     * a cover in the kv table
     */
    public final class Cover {
        private final ArrayList<VarState> varStates;
        private Pos pos;
        private int cellCount;
        private int inset = 0;

        private Cover() {
            varStates = new ArrayList<>();
        }

        private Cover add(VarState varState) {
            varStates.add(varState);
            return this;
        }

        private boolean contains(VarState s) {
            return varStates.contains(s);
        }

        /**
         * @return the position of the cover. Caution: Returns a bounding box!
         */
        public Pos getPos() {
            if (pos == null) {
                int rowMin = Integer.MAX_VALUE;
                int rowMax = Integer.MIN_VALUE;
                int colMin = Integer.MAX_VALUE;
                int colMax = Integer.MIN_VALUE;
                for (Cell c : cells) {
                    if (c.contains(this)) {
                        if (c.row > rowMax) rowMax = c.row;
                        if (c.row < rowMin) rowMin = c.row;
                        if (c.col > colMax) colMax = c.col;
                        if (c.col < colMin) colMin = c.col;
                    }
                }
                int width = colMax - colMin + 1;
                int height = rowMax - rowMin + 1;
                pos = new Pos(rowMin, colMin, width, height);
            }
            return pos;
        }

        private void incCellCount() {
            cellCount++;
        }

        /**
         * @return the size of a cover
         */
        public int getSize() {
            return cellCount;
        }

        /**
         * @return the inset of this cover;
         */
        public int getInset() {
            return inset;
        }

        /**
         * @return true if cover is split, thus the cover is wrapping around the border
         */
        public boolean isDisconnected() {
            return getPos().width * getPos().height > cellCount;
        }

        /**
         * @return covers only the edges
         */
        public boolean onlyEdges() {
            return getPos().width * getPos().height == 16 && cellCount == 4;
        }

        /**
         * @return true if disconnected cover is vertical divided
         */
        public boolean isVerticalDivided() {
            Pos p = getPos();
            if (p.width * p.height == 16 && cellCount == 8)
                return getCell(1, 0).contains(this);
            else
                return p.getWidth() > p.getHeight();
        }
    }

    /**
     * The position of the cover.
     * If a cover is wrapping around the borders the bounding box is returned!
     */
    public static final class Pos {
        private final int row;
        private final int col;
        private final int width;
        private final int height;

        private Pos(int row, int col, int width, int height) {
            this.row = row;
            this.col = col;
            this.width = width;
            this.height = height;
        }


        /**
         * @return the row
         */
        public int getRow() {
            return row;
        }

        /**
         * @return the column
         */
        public int getCol() {
            return col;
        }

        /**
         * @return the width of the cover
         */
        public int getWidth() {
            return width;
        }

        /**
         * @return the height of the cover
         */
        public int getHeight() {
            return height;
        }

    }

    /**
     * Defines the variables in the borders of the KV map
     */
    public static final class Header {
        private final int var;
        private final boolean[] invert;

        private Header(int var, boolean... invert) {
            this.var = var;
            this.invert = invert;
        }

        /**
         * @return the variable
         */
        public int getVar() {
            return var;
        }

        /**
         * @return the size
         */
        public int size() {
            return invert.length;
        }

        /**
         * Returns the variable state
         *
         * @param i the index of the row column
         * @return true if inverted variable
         */
        public boolean getInvert(int i) {
            return invert[i];
        }

        /**
         * Initializes the table according to the selected header.
         *
         * @param cols the number columns in the table
         * @param kmap the k-map to use
         * @return this for chained calls
         */
        public Header toRows(int cols, KarnaughMap kmap) {
            for (int row = 0; row < invert.length; row++)
                for (int col = 0; col < cols; col++)
                    kmap.getCell(row, col).add(new VarState(var, invert[row]));
            return this;
        }

        /**
         * Initializes the table according to the selected header.
         *
         * @param rows the number rows in the table
         * @param kmap the k-map to use
         * @return this for chained calls
         */
        public Header toCols(int rows, KarnaughMap kmap) {
            for (int col = 0; col < invert.length; col++)
                for (int row = 0; row < rows; row++)
                    kmap.getCell(row, col).add(new VarState(var, invert[col]));
            return this;
        }

    }
}
