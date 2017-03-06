package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelector;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelectorDefault;
import de.neemann.digital.lang.Lang;

import java.util.*;

import static de.neemann.digital.analyse.expression.Operation.or;

/**
 * The algorithm from Quine and McCluskey
 *
 * @author hneemann
 */
public class QuineMcCluskey {

    private final TableRows rows;
    private final List<Variable> variables;
    private final ArrayList<TableRow> primes;

    /**
     * Creates a new instance
     *
     * @param variables the variables to use
     */
    public QuineMcCluskey(List<Variable> variables) {
        this.variables = variables;
        this.rows = new TableRows();
        this.primes = new ArrayList<>();
    }

    private QuineMcCluskey(List<Variable> variables, TableRows rows, ArrayList<TableRow> primes) {
        this.variables = variables;
        this.rows = rows;
        this.primes = primes;
    }

    /**
     * Creates a new instance.
     * The Bool table is build using the given expression
     *
     * @param expression the expression used to build the table
     * @throws ExpressionException ExpressionException
     */
    public QuineMcCluskey(Expression expression) throws ExpressionException {
        ContextFiller context = new ContextFiller(expression);
        variables = context.getVariables();
        rows = new TableRows();
        fillTableWith(new BoolTableExpression(expression, context));
        primes = new ArrayList<>();
    }

    /**
     * Fills the instance with the given values
     *
     * @param values the values
     * @return this for chained calls
     * @throws ExpressionException ExpressionException
     */
    public QuineMcCluskey fillTableWith(BoolTable values) throws ExpressionException {
        int n = 1 << variables.size();
        if (n != values.size())
            throw new ExpressionException(Lang.get("err_exact_N0_valuesNecessaryNot_N1", n, values.size()));
        for (int i = 0; i < n; i++) {
            ThreeStateValue value = values.get(i);
            if (!value.equals(ThreeStateValue.zero)) {
                add(i, value.equals(ThreeStateValue.dontCare));
            }
        }
        return this;
    }


    private void add(int i, boolean dontCare) {
        rows.add(new TableRow(variables.size(), i, rows.size() + 1, dontCare));
    }

    /**
     * Simplifies the given expression.
     * If no simplification was found, the original expression is returned unchanged.
     *
     * @param expression the expression to simplify
     * @return the simplified expression
     * @throws ExpressionException ExpressionException
     */
    public static Expression simplify(Expression expression) throws ExpressionException {
        int initialCplx = expression.traverse(new ComplexityInclNotVisitor()).getComplexity();

        Expression newExp = new QuineMcCluskey(expression)
                .simplify()
                .getExpression();

        int newCplx = newExp.traverse(new ComplexityInclNotVisitor()).getComplexity();

        if (newCplx < initialCplx)
            return newExp;
        else
            return expression;
    }

    /**
     * Simplifies the table the the default {@link PrimeSelector}
     *
     * @return the simplified QMC instance
     */
    public QuineMcCluskey simplify() {
        return simplify(new PrimeSelectorDefault());
    }

    /**
     * Simplifies the table the the given {@link PrimeSelector}
     *
     * @param ps the prome selector
     * @return the simplified QMC instance
     */
    public QuineMcCluskey simplify(PrimeSelector ps) {
        QuineMcCluskey t = this;
        while (!t.isFinished())
            t = t.simplifyStep();
        return t.simplifyPrimes(ps);
    }


    QuineMcCluskey simplifyStep() {
        TableRows newRows = new TableRows();

        for (ArrayList<TableRow> list : rows.listIterable())
            for (int i = 0; i < list.size() - 1; i++)
                for (int j = i + 1; j < list.size(); j++) {

                    TableRow r1 = list.get(i);
                    TableRow r2 = list.get(j);

                    int index = checkCompatible(r1, r2);
                    if (index >= 0) {
                        // can optimize;
                        TableRow newRow = new TableRow(r1);
                        newRow.setToOptimized(index);

                        TableRow r = newRows.findRow(newRow);
                        if (r==null) {
                            newRow.addSource(r1.getSource());
                            newRow.addSource(r2.getSource());
                            newRows.add(newRow);
                        } else {
                            r.addSource(r1.getSource());
                            r.addSource(r2.getSource());
                        }
                        r1.setUsed();
                        r2.setUsed();
                    }
                }

        ArrayList<TableRow> np = new ArrayList<TableRow>();
        np.addAll(primes);
        for (TableRow row : rows)
            if (!row.isUsed() && row.getSource().size() > 0)
                np.add(row);

        return new QuineMcCluskey(variables, newRows, np);
    }

    /**
     * @return true id simplification is complete
     */
    public boolean isFinished() {
        return rows.isEmpty();
    }

    private int checkCompatible(TableRow r1, TableRow r2) {
        if (r1.getOptimizedFlags() != r2.getOptimizedFlags())
            return -1;

        int difIndex = -1;
        for (int i = 0; i < r1.size(); i++) {
            if (!r1.get(i).equals(r2.get(i))) {
                if (difIndex >= 0)
                    return -1;
                difIndex = i;
            }
        }
        return difIndex;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ArrayList<TableRow> newList = new ArrayList<TableRow>();
        for (TableRow r : rows) {
            newList.add(r);
        }
        Collections.sort(newList);
        for (TableRow r : newList) {
            sb.append(r.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * @return the final primes
     */
    public ArrayList<TableRow> getPrimes() {
        return primes;
    }

    /**
     * @return the simplified expression which represent this table
     */
    public Expression getExpression() {
        if (primes.isEmpty() && rows.isEmpty())
            return Constant.ZERO;

        Expression e = addAnd(null, primes, variables);
        return addAnd(e, rows, variables);
    }

    /**
     * Creates the final expression
     *
     * @param e         the expression to complete
     * @param rows      the rows to add
     * @param variables the variables to use to build the expression
     * @return the expression
     */
    public static Expression addAnd(Expression e, Iterable<TableRow> rows, List<Variable> variables) {
        for (TableRow r : rows) {
            Expression n = r.getExpression(variables);
            if (e == null)
                e = n;
            else
                e = or(e, n);
        }
        return e;
    }

    /**
     * Simplify the primes
     *
     * @param primeSelector the prome selector to use
     * @return this for call chaning
     */
    public QuineMcCluskey simplifyPrimes(PrimeSelector primeSelector) {
        ArrayList<TableRow> primesAvail = new ArrayList<TableRow>(primes);
        primes.clear();

        TreeSet<Integer> termIndices = new TreeSet<>();
        for (TableRow r : primesAvail)
            termIndices.addAll(r.getSource());

        // Nach primtermen suchen, welche einen index exclusiv enthalten
        // Diese müssen in jedem Falle enthalten sein!
        for (int pr : termIndices) {

            TableRow foundPrime = null;
            for (TableRow tr : primesAvail) {
                if (tr.getSource().contains(pr)) {
                    if (foundPrime == null) {
                        foundPrime = tr;
                    } else {
                        foundPrime = null;
                        break;
                    }
                }
            }

            if (foundPrime != null) {
                if (!primes.contains(foundPrime))
                    primes.add(foundPrime);
            }
        }
        primesAvail.removeAll(primes);

        // Die, Indices die wir schon haben können raus;
        for (TableRow pr : primes) {
            termIndices.removeAll(pr.getSource());
        }

        if (!termIndices.isEmpty()) {

            //Die noch übrigen Terme durchsuchen ob sie schon komplett dabei sind;
            Iterator<TableRow> it = primesAvail.iterator();
            while (it.hasNext()) {
                TableRow tr = it.next();
                boolean needed = false;
                for (int i : tr.getSource()) {
                    if (termIndices.contains(i)) {
                        needed = true;
                        break;
                    }
                }
                if (!needed) {
                    it.remove();
                }
            }

            primeSelector.select(primes, primesAvail, termIndices);
        }

        return this;
    }

}
