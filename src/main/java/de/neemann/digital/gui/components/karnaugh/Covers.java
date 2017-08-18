package de.neemann.digital.gui.components.karnaugh;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Not;
import de.neemann.digital.analyse.expression.Operation;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Covers implements Iterable<Covers.Cover> {

    private final ArrayList<Cell> cells;
    private final List<Variable> vars;
    private final ArrayList<Cover> covers;

    public Covers(List<Variable> vars, Expression expr) throws KarnaughException {
        this.vars = vars;
        cells = new ArrayList<>();
        for (int row = 0; row < 4; row++)
            for (int col = 0; col < 4; col++)
                cells.add(new Cell(row, col));
        covers = new ArrayList<>();

        addToRow(0, 0, true);
        addToRow(1, 0, true);
        addToRow(2, 0, false);
        addToRow(3, 0, false);
        addToRow(0, 1, false);
        addToRow(1, 1, true);
        addToRow(2, 1, true);
        addToRow(3, 1, false);
        addToCol(0, 2, true);
        addToCol(1, 2, true);
        addToCol(2, 2, false);
        addToCol(3, 2, false);
        addToCol(0, 3, false);
        addToCol(1, 3, true);
        addToCol(2, 3, true);
        addToCol(3, 3, false);

        addExpression(expr);
    }

    private void addToRow(int row, int var, boolean invert) {
        for (int col = 0; col < 4; col++)
            getCell(row, col).add(new VarState(var, invert));
    }

    private void addToCol(int col, int var, boolean invert) {
        for (int row = 0; row < 4; row++)
            getCell(row, col).add(new VarState(var, invert));
    }

    private Cell getCell(int row, int col) {
        for (Cell cell : cells)
            if (cell.is(row, col)) return cell;
        throw new RuntimeException("cell not found");
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
        } else
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

    public int size() {
        return covers.size();
    }

    public static final class Cell {
        private final int row;
        private final int col;
        private ArrayList<VarState> impl;
        private ArrayList<Cover> covers;

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
        }

        public boolean contains(Cover cover) {
            return covers.contains(cover);
        }
    }

    public static final class VarState {
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

        public VarState not() {
            return new VarState(num, !invert);
        }
    }

    /**
     * a cover
     */
    public class Cover {
        private final ArrayList<VarState> varStates;
        private Pos pos;
        private int belongsTo;

        public Cover() {
            varStates = new ArrayList<>();
        }

        public Cover add(VarState varState) {
            varStates.add(varState);
            return this;
        }

        public boolean contains(VarState s) {
            return varStates.contains(s);
        }

        public Pos getPos() {
            if (pos==null) {
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
                pos = new Pos(rowMin, colMin, colMax - colMin + 1, rowMax - rowMin + 1);
            }
            return pos;
        }
    }

    /**
     * The position of the cover
     */
    public static class Pos {
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

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
