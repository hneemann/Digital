package de.neemann.digital.draw.builder.jedec;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Not;
import de.neemann.digital.analyse.expression.Operation;
import de.neemann.digital.analyse.expression.Variable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Fills a equation in a fuse map
 * Assumes that all product terms follow each other derectly in the fuse map.
 * Assumes that the fuse and not fuse follow each other.
 *
 * @author hneemann
 */
public class FuseMapFiller {

    private final FuseMap fuseMap;
    private final int varsConnectedToMap;
    private final int productTerms;
    private final HashMap<Variable, Integer> varMap;

    /**
     * Creates a new instance
     * The given vars list needs to contain all variables connected to the matrix in the correct order.
     * If a variable in the matrix is not used, you have to add a null to the variables list.
     *
     * @param fuseMap            the fuse map to fill
     * @param varsConnectedToMap the number variables available in matrix
     * @param productTerms       the number of product terms available
     */
    public FuseMapFiller(FuseMap fuseMap, int varsConnectedToMap, int productTerms) {
        this.fuseMap = fuseMap;
        this.varsConnectedToMap = varsConnectedToMap;
        this.productTerms = productTerms;
        varMap = new HashMap<>();
    }

    /**
     * Adds a variable to the matrix
     *
     * @param index number in matrix
     * @param var   the variable
     * @return this for chained calls
     */
    public FuseMapFiller addVariable(int index, Variable var) {
        varMap.put(var, index);
        return this;
    }


    /**
     * Fills an expression to the fuse map
     *
     * @param offs number of first fuse of first product term to use
     * @param exp  the expression
     * @throws FuseMapFillerException EquationHandlerException
     */
    public void fillExpression(int offs, Expression exp) throws FuseMapFillerException {
        if (!(exp instanceof Operation.Or))
            throw new FuseMapFillerException("only OR terms are supported!");

        Operation.Or or = (Operation.Or) exp;
        ArrayList<Expression> terms = or.getExpressions();
        if (terms.size() > productTerms)
            throw new FuseMapFillerException("only " + productTerms + " product terms supported!");

        int fusesInTerm = varsConnectedToMap * 2;

        for (Expression e : terms) {

            for (int i = 0; i < fusesInTerm; i++)
                fuseMap.setFuse(offs + i, true);

            if (!(e instanceof Operation.And))
                throw new FuseMapFillerException("only OR must contain AND terms!");

            Operation.And and = (Operation.And) e;

            for (Expression v : and.getExpressions()) {

                Variable var;
                boolean invert = false;

                if (v instanceof Variable)
                    var = (Variable) v;
                else if (v instanceof Not) {
                    Expression n = ((Not) v).getExpression();
                    if (n instanceof Variable) {
                        var = (Variable) n;
                        invert = true;
                    } else {
                        throw new FuseMapFillerException("NOT does not contain a variable!");
                    }
                } else
                    throw new FuseMapFillerException("only VAR or NOT VAR allowed!");

                Integer i = varMap.get(var);

                if (i == null)
                    throw new FuseMapFillerException("VAR " + var + " not found in term list!");

                int fuse = i * 2;
                if (invert) fuse++;

                fuseMap.setFuse(offs + fuse, false);
            }
            offs += fusesInTerm;
        }


    }

}
