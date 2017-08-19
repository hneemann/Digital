package de.neemann.digital.gui.components.karnaugh;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
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
        this.vars = vars;
        cells = new ArrayList<>();
        covers = new ArrayList<>();
        switch (vars.size()) {
            case 2:
                for (int row = 0; row < 2; row++)
                    for (int col = 0; col < 2; col++)
                        cells.add(new Cell(row, col));
                addToRow(0, 2, 0, true);
                addToRow(1, 2, 0, false);
                addToCol(0, 2, 1, true);
                addToCol(1, 2, 1, false);
                headerLeft = new Header(0, true, false);
                headerTop = new Header(1, true, false);
                headerRight = null;
                headerBottom = null;
                break;
            case 3:
                for (int row = 0; row < 2; row++)
                    for (int col = 0; col < 4; col++)
                        cells.add(new Cell(row, col));
                addToRow(0, 4, 0, true);
                addToRow(1, 4, 0, false);
                addToCol(0, 2, 1, true);
                addToCol(1, 2, 1, true);
                addToCol(2, 2, 1, false);
                addToCol(3, 2, 1, false);
                addToCol(0, 2, 2, false);
                addToCol(1, 2, 2, true);
                addToCol(2, 2, 2, true);
                addToCol(3, 2, 2, false);
                headerLeft = new Header(0, true, false);
                headerTop = new Header(1, true, true, false, false);
                headerBottom = new Header(2, false, true, true, false);
                headerRight = null;
                break;
            case 4:
                for (int row = 0; row < 4; row++)
                    for (int col = 0; col < 4; col++)
                        cells.add(new Cell(row, col));

                addToRow(0, 4, 0, true);
                addToRow(1, 4, 0, true);
                addToRow(2, 4, 0, false);
                addToRow(3, 4, 0, false);
                addToRow(0, 4, 1, false);
                addToRow(1, 4, 1, true);
                addToRow(2, 4, 1, true);
                addToRow(3, 4, 1, false);
                addToCol(0, 4, 2, true);
                addToCol(1, 4, 2, true);
                addToCol(2, 4, 2, false);
                addToCol(3, 4, 2, false);
                addToCol(0, 4, 3, false);
                addToCol(1, 4, 3, true);
                addToCol(2, 4, 3, true);
                addToCol(3, 4, 3, false);
                headerLeft = new Header(0, true, true, false, false);
                headerRight = new Header(1, false, true, true, false);
                headerTop = new Header(2, true, true, false, false);
                headerBottom = new Header(3, false, true, true, false);
                break;
            default:
                throw new KarnaughException(Lang.get("err_toManyVars"));
        }
        createTruthTableIndex();

        addExpression(expr);
    }

    private void createTruthTableIndex() {
        for (Cell c : cells)
            c.createTableIndex();
    }

    private void addToRow(int row, int cols, int var, boolean invert) {
        for (int col = 0; col < cols; col++)
            getCell(row, col).add(new VarState(var, invert));
    }

    private void addToCol(int col, int rows, int var, boolean invert) {
        for (int row = 0; row < rows; row++)
            getCell(row, col).add(new VarState(var, invert));
    }

    /**
     * The given cell
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
                else
                    throw new KarnaughException(Lang.get("err_invalidExpression"));
        } else if (!(expr instanceof Constant))
            throw new KarnaughException(Lang.get("err_invalidExpression"));
    }

    private void addCover(Expression expr) throws KarnaughException {
        addCover(new Cover().add(getVarOf(expr)));
    }

    private void addCover(ArrayList<Expression> expressions) throws KarnaughException {
        Cover cover = new Cover();
        for (Expression expr : expressions)
            cover.add(getVarOf(expr));
        addCover(cover);
    }

    private void addCover(Cover cover) {
        covers.add(cover);
        for (Cell cell : cells)
            cell.check(cover);
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
     * a sigle cell in kv map
     */
    public static final class Cell {
        private final int row;
        private final int col;
        private ArrayList<VarState> impl;
        private ArrayList<Cover> covers;
        private int index;

        private Cell(int row, int col) {
            this.row = row;
            this.col = col;
            impl = new ArrayList<>();
            covers = new ArrayList<>();
        }

        private void add(VarState varState) {
            impl.add(varState);
        }

        private boolean is(int row, int col) {
            return (this.row == row) && (this.col == col);
        }

        private void check(Cover cover) {
            for (VarState s : impl)
                if (cover.contains(s.not()))
                    return;
            covers.add(cover);
            cover.incCellCount();
        }

        private boolean contains(Cover cover) {
            return covers.contains(cover);
        }

        private void createTableIndex() {
            int tableCols = impl.size();
            index = 0;
            for (VarState i : impl) {
                if (!i.invert)
                    index += (1 << (tableCols - i.num - 1));
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
        public int getIndex() {
            return index;
        }

        boolean hasImpl(int var, boolean invert) {
            return impl.contains(new VarState(var, invert));
        }
    }

    private static final class VarState {
        private int num;
        private boolean invert;

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
     * a cover
     */
    public final class Cover {
        private final ArrayList<VarState> varStates;
        private Pos pos;
        private int cellCount;

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
         * @return the position of the cover
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
                pos = new Pos(rowMin, colMin, width, height, cellCount);
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
    }

    /**
     * The position of the cover.
     * If a cover is wrapping around the borders the bounding box is returned!
     * Check the number of cells to detect that situation.
     */
    public static final class Pos {
        private final int row;
        private final int col;
        private final int width;
        private final int height;
        private final int cellCount;

        private Pos(int row, int col, int width, int height, int cellCount) {
            this.row = row;
            this.col = col;
            this.width = width;
            this.height = height;
            this.cellCount = cellCount;
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

        /**
         * @return true if cover is split
         */
        public boolean isSplit() {
            return width * height > cellCount;
        }

        public int inset() {
            return 0;
        }
    }

    /**
     * Defines the variables in the borders
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
         * Returns the variables state
         *
         * @param i the index of the row column
         * @return true if inverted variable
         */
        public boolean getInvert(int i) {
            return invert[i];
        }

    }
}
