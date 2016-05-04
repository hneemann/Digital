package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;

/**
 * @author hneemann
 */
public class BoolTableExpression implements BoolTable {
    private final Expression expression;
    private final ContextFiller context;

    public BoolTableExpression(Expression expression, ContextFiller context) {
        this.expression = expression;
        this.context = context;
    }

    @Override
    public int size() {
        return 1 << context.getVarCount();
    }

    @Override
    public ThreeStateValue get(int i) throws ExpressionException {
        context.setContextTo(i);
        return ThreeStateValue.value(expression.calculate(context));
    }
}
