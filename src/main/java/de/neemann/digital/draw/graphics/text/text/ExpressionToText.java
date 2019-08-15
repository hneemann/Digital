/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.text.text;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.draw.graphics.text.ParseException;
import de.neemann.digital.draw.graphics.text.Parser;

/**
 * Used to create a text structure from an expression.
 */
public class ExpressionToText {

    private boolean formatIdentifiers = true;

    /**
     * Creates a new instance
     */
    public ExpressionToText() {
    }

    /**
     * If set to false, the identifiers are unchanged
     *
     * @param formatIdentifiers set to false to keep identifiers unchanged
     * @return this for chained calls
     */
    public ExpressionToText setFormatIdentifiers(boolean formatIdentifiers) {
        this.formatIdentifiers = formatIdentifiers;
        return this;
    }

    /**
     * Creates a text structure from an expression.
     * Uses the default format
     *
     * @param expression the expression
     * @return the text
     */
    public Text createText(Expression expression) {
        return createText(expression, FormatToExpression.getDefaultFormat());
    }

    /**
     * Creates a text structure from an expression
     *
     * @param expression the expression
     * @param format     the format to use
     * @return the text
     */
    public Text createText(Expression expression, FormatToExpression format) {
        if (expression instanceof Variable) {
            String ident = ((Variable) expression).getIdentifier();
            return formatIdent(ident);
        } else if (expression instanceof Constant) {
            String value = format.constant(((Constant) expression).getValue());
            return new Simple(value);
        } else if (expression instanceof Operation.And) {
            return createOperationText((Operation) expression, format.getAndString(), format);
        } else if (expression instanceof Operation.Or) {
            return createOperationText((Operation) expression, format.getOrString(), format);
        } else if (expression instanceof Operation.XOr) {
            return createOperationText((Operation) expression, format.getXorString(), format);
        } else if (expression instanceof Not) {
            return new Decorate(createText(((Not) expression).getExpression(), format), Decorate.Style.OVERLINE);
        } else if (expression instanceof NamedExpression) {
            NamedExpression ne = (NamedExpression) expression;
            Sentence s = new Sentence();
            s.add(formatIdent(ne.getName()));
            s.add(Blank.BLANK);
            s.add(new Simple(format.getEqual()));
            s.add(Blank.BLANK);
            s.add(createText(ne.getExpression(), format));
            return s;
        } else
            return new Simple(expression.toString());
    }

    private Text formatIdent(String ident) {
        if (formatIdentifiers) {
            try {
                return new Parser(ident).parse();
            } catch (ParseException e) {
                return new Simple(ident);
            }
        } else
            return new Simple(ident);
    }

    private Text createOperationText(Operation op, String opString, FormatToExpression format) {
        Sentence s = new Sentence();
        for (Expression e : op.getExpressions()) {
            if (s.size() > 0) {
                s.add(Blank.BLANK);
                if (!opString.isEmpty()) {
                    s.add(new Simple(opString));
                    s.add(Blank.BLANK);
                }
            }
            if (e instanceof Operation) {
                s.add(new Simple("("));
                s.add(createText(e, format));
                s.add(new Simple(")"));
            } else {
                s.add(createText(e, format));
            }
        }
        return s;
    }

}
