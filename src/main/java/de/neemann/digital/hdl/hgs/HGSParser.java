package de.neemann.digital.hdl.hgs;

import de.neemann.digital.hdl.hgs.ast.stmt.PrintfStatement;
import de.neemann.digital.hdl.hgs.ast.stmt.ForStatement;
import de.neemann.digital.hdl.hgs.ast.stmt.OutputStatement;
import de.neemann.digital.hdl.hgs.ast.stmt.IfStatement;
import de.neemann.digital.hdl.hgs.ast.stmt.Statement;
import de.neemann.digital.hdl.hgs.ast.stmt.AssignStatement;
import de.neemann.digital.hdl.hgs.ast.stmt.CompoundStatement;
import de.neemann.digital.hdl.hgs.ast.stmt.WhileStatement;
import de.neemann.digital.hdl.hgs.ast.stmt.PrintStatement;
import de.neemann.digital.hdl.hgs.ast.lvalue.LValue;
import de.neemann.digital.hdl.hgs.ast.lvalue.IndexedLValue;
import de.neemann.digital.hdl.hgs.ast.lvalue.IdentLValue;
import de.neemann.digital.hdl.hgs.ast.expr.LValueExpr;
import de.neemann.digital.hdl.hgs.ast.expr.UnaryExpr;
import de.neemann.digital.hdl.hgs.ast.expr.ConstantExpr;
import de.neemann.digital.hdl.hgs.ast.expr.Expr;
import de.neemann.digital.hdl.hgs.ast.expr.FunctionCallExpr;
import de.neemann.digital.hdl.hgs.ast.expr.BinaryExpr;
import de.neemann.digital.hdl.hgs.ast.expr.StringLiteralExpr;
import de.neemann.digital.hdl.hgs.ast.lvalue.FieldAccessLValue;
import de.neemann.digital.lang.Lang;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author ideras
 */
public class HGSParser {

    private final HGSLexer lexer;
    private Token currToken;

    /**
     * Creates a new instance
     *
     * @param lexer the lexer instance
     */
    public HGSParser(HGSLexer lexer) {
        this.lexer = lexer;
    }

    /**
     * Parse the input and generate an AST if no errors.
     *
     * @return The AST (Abstract Syntax Tree)
     *
     * @throws IOException      IOException
     * @throws HGSException  HDLGenException
     */
    public Statement parse() throws IOException, HGSException {
        currToken = lexer.getNextToken();

        Statement stmt = statementList();

        if (currToken.getId() != Token.EOF) {
            throw new HGSException(lexer.getLine(),
                    Lang.get("unexpectedStartOfStatement", currToken));
        }

        return stmt;
    }

    private Statement statementList() throws HGSException, IOException {
        Statement stmt;
        int[] firstOfStatement = {
            '{',
            Token.IDENTIFIER,
            Token.OUTPUT_LITERAL,
            Token.IF,
            Token.WHILE,
            Token.FOR,
            Token.PRINTEXPR,
            Token.PRINTF
        };

        if (!currToken.isOneOf(firstOfStatement)) {
            throw new HGSException(lexer.getLine(),
                    Lang.get("unexpectedStartOfStatement", currToken));
        } else {
            ArrayList<Statement> stmtList = new ArrayList<>();

            stmt = statement();
            stmtList.add(stmt);

            while (currToken.isOneOf(firstOfStatement)) {
                stmt = statement();
                stmtList.add(stmt);
            }

            return new CompoundStatement(stmtList);
        }
    }

    private void nextToken() throws IOException, HGSException {
        currToken = lexer.getNextToken();
    }

    private void matchToken(int tkID, String tkLexeme) throws HGSException, IOException {
        if (currToken.getId() != tkID) {
            throw new HGSException(lexer.getLine(), Lang.get("unexpectedToken", currToken, tkLexeme));
        }
        currToken = lexer.getNextToken();
    }

    private Statement statement() throws HGSException, IOException {
        Statement stmt;

        switch (currToken.getId()) {
            case '{':
                stmt = compoundStatement();
                break;
            case Token.IDENTIFIER:
                stmt = changeStatement();
                matchToken(';', ";");
                break;
            case Token.IF:
                stmt = ifStatement();
                break;
            case Token.FOR:
                stmt = forStatement();
                break;
            case Token.WHILE:
                stmt = whileStatement();
                break;
            case Token.PRINTF:
                stmt = printfStatement();
                matchToken(';', ";");
                break;
            case Token.OUTPUT_LITERAL:
                stmt = new OutputStatement(currToken.getLexeme());
                nextToken();
                break;
            case Token.PRINTEXPR:
                nextToken();
                Expr expr = expression();
                matchToken(Token.ENDOFEXPR, "end of expression");
                stmt = new PrintStatement(lexer.getLine(), expr);
                break;
            default:
                throw new HGSException(lexer.getLine(), Lang.get("unexpectedStartOfStatement", currToken));
        }

        return stmt;
    }

    private Statement compoundStatement() throws HGSException, IOException {
        matchToken('{', "{");
        Statement stmt = statementList();
        matchToken('}', "}");

        return stmt;
    }

    private Statement changeStatement() throws HGSException, IOException {
        String ident = currToken.getLexeme();

        nextToken();
        LValue lvalue = lvalueExpr(ident);

        Statement stmt;

        switch (currToken.getId()) {
            case '=':
                matchToken('=', "=");

                Expr expr = expression();
                stmt = new AssignStatement(lexer.getLine(), lvalue, expr);
                break;
            case Token.INC_OP:
                matchToken(Token.INC_OP, "++");
                stmt = new AssignStatement(lexer.getLine(),
                        lvalue,
                        new BinaryExpr(lexer.getLine(),
                                new LValueExpr(lexer.getLine(), lvalue),
                                new ConstantExpr(1),
                                BinaryExpr.Operator.ADD));
                break;
            case Token.DEC_OP:
                matchToken(Token.DEC_OP, "--");
                stmt = new AssignStatement(lexer.getLine(),
                        lvalue,
                        new BinaryExpr(lexer.getLine(),
                                new LValueExpr(lexer.getLine(), lvalue),
                                new ConstantExpr(1),
                                BinaryExpr.Operator.SUB));
                break;
            default:
                throw new HGSException(lexer.getLine(), Lang.get("unpextedTokenInChangeStmt", currToken));
        }

        return stmt;
    }

    private Statement ifStatement() throws HGSException, IOException {
        matchToken(Token.IF, "if");
        matchToken('(', "(");
        Expr cond = expression();
        matchToken(')', ")");
        Statement stmt1 = statement();
        Statement stmt2 = null;

        if (currToken.getId() == Token.ELSE) {
            matchToken(Token.ELSE, "else");
            stmt2 = statement();
        }

        return new IfStatement(lexer.getLine(), cond, stmt1, stmt2);
    }

    private Statement forStatement() throws HGSException, IOException {
        matchToken(Token.FOR, "for");
        matchToken('(', "(");
        Statement assignStmt = changeStatement();
        matchToken(';', ";");
        Expr cond = expression();
        matchToken(';', ";");
        Statement incStmt = changeStatement();
        matchToken(')', ")");
        Statement block = statement();

        return new ForStatement(lexer.getLine(), assignStmt, cond, incStmt, block);
    }

    private Statement whileStatement() throws HGSException, IOException {
        matchToken(Token.WHILE, "while");
        matchToken('(', "(");
        Expr cond = expression();
        matchToken(')', ")");
        Statement stmt = statement();

        return new WhileStatement(lexer.getLine(), cond, stmt);
    }

    private Statement printfStatement() throws IOException, HGSException {
        nextToken();
        matchToken('(', "(");
        String formatStr = currToken.getLexeme();
        matchToken(Token.STRING_LITERAL, "string literal");

        ArrayList<Expr> arguments = null;

        if (currToken.getId() == ',') {
            arguments = new ArrayList<>();
            while (currToken.getId() == ',') {
                nextToken();
                Expr argExpr = expression();
                arguments.add(argExpr);
            }
        }
        matchToken(')', ")");
        return new PrintfStatement(lexer.getLine(), formatStr, arguments);
    }

    private Expr binaryExprParse(TokenMatcher tokenMatcher, ExprProducer exprProducer) throws IOException, HGSException {
        Expr expr1 = exprProducer.get();
        BinaryExpr.Operator oper;

        while (tokenMatcher.accept(currToken)) {
            oper = mapTokenToOperator(currToken);

            currToken = lexer.getNextToken();
            Expr expr2 = exprProducer.get();
            expr1 = new BinaryExpr(lexer.getLine(), expr1, expr2, oper);
        }
        return expr1;
    }

    private Expr expression() throws HGSException, IOException {
        return binaryExprParse(tk -> (tk.getId() == Token.OR_OP), () -> logicalAndExpr());
    }

    private Expr logicalAndExpr() throws IOException, HGSException {
        return binaryExprParse(tk -> (tk.getId() == Token.AND_OP), () -> inclusiveOrExpr());
    }

    private Expr inclusiveOrExpr() throws IOException, HGSException {
        return binaryExprParse(tk -> (tk.getId() == '|'), () -> exclusiveOrExpr());
    }

    private Expr exclusiveOrExpr() throws IOException, HGSException {
        return binaryExprParse(tk -> (tk.getId() == '^'), () -> andExpr());
    }

    private Expr andExpr() throws IOException, HGSException {
        return binaryExprParse(tk -> (tk.getId() == '&'), () -> relationalExpr());
    }

    private Expr relationalExpr() throws HGSException, IOException {
        Expr expr1 = shiftExpr();
        if (currToken.isOneOf('<', '>', Token.LE_OP, Token.GE_OP, Token.EQ_OP, Token.NE_OP)) {
            BinaryExpr.Operator oper = null;

            switch (currToken.getId()) {
                case '<':
                    oper = BinaryExpr.Operator.LT;
                    break;
                case '>':
                    oper = BinaryExpr.Operator.GT;
                    break;
                case Token.GE_OP:
                    oper = BinaryExpr.Operator.GE;
                    break;
                case Token.LE_OP:
                    oper = BinaryExpr.Operator.LE;
                    break;
                case Token.EQ_OP:
                    oper = BinaryExpr.Operator.EQ;
                    break;
                case Token.NE_OP:
                    oper = BinaryExpr.Operator.NE;
                    break;
            }
            nextToken();
            Expr expr2 = shiftExpr();
            expr1 = new BinaryExpr(lexer.getLine(), expr1, expr2, oper);
        }

        return expr1;
    }

    private Expr shiftExpr() throws IOException, HGSException {
        return binaryExprParse(tk -> (tk.isOneOf(Token.SL_OP, Token.SR_OP)), () -> additiveExpr());
    }

    private Expr additiveExpr() throws IOException, HGSException {
        return binaryExprParse(tk -> (tk.isOneOf('+', '-')), () -> multiplicativeExpr());
    }

    private Expr multiplicativeExpr() throws IOException, HGSException {
        return binaryExprParse(tk -> (tk.isOneOf('*', '/', '%')), () -> primaryExpr());
    }

    private Expr primaryExpr() throws HGSException, IOException {
        Expr expr;

        switch (currToken.getId()) {
            case Token.IDENTIFIER:
                String ident = currToken.getLexeme();
                nextToken();

                if (currToken.getId() == '(') {
                    nextToken();
                    ArrayList<Expr> exprList = new ArrayList<>();
                    Expr arg = expression();
                    exprList.add(arg);

                    while (currToken.is(',')) {
                        nextToken();
                        arg = expression();
                        exprList.add(arg);
                    }
                    matchToken(')', ")");
                    expr = new FunctionCallExpr(lexer.getLine(), ident, exprList);
                } else {
                    LValue lval = lvalueExpr(ident);
                    expr = new LValueExpr(lexer.getLine(), lval);
                }
                break;
            case Token.STRING_LITERAL:
                String strlit = currToken.getLexeme();

                expr = new StringLiteralExpr(strlit);
                nextToken();
                break;
            case Token.CONSTANT:
                int value = Integer.parseInt(currToken.getLexeme());

                expr = new ConstantExpr(value);
                nextToken();
                break;
            case Token.HEX_CONSTANT:
                long longVal = Long.parseLong(currToken.getLexeme(), 16);

                expr = new ConstantExpr((int) longVal);
                nextToken();
                break;
            case '(':
                nextToken();
                expr = expression();
                matchToken(')', ")");
                break;
            case '+':
            case '-':
            case '~':
            case '!':
                UnaryExpr.Operator oper = mapTokenToUnaryOperator(currToken);
                nextToken();
                Expr pexpr = primaryExpr();

                expr = new UnaryExpr(lexer.getLine(), pexpr, oper);
                break;
            default:
                throw new HGSException(lexer.getLine(), Lang.get("unexpectedTokenInPrimaryExpr", currToken));
        }

        return expr;
    }

    private LValue lvalueExpr(String ident) throws IOException, HGSException {
        LValue lval = new IdentLValue(lexer.getLine(), ident);

        while (currToken.isOneOf('.', '[')) {
            switch (currToken.getId()) {
                case '.':
                    nextToken();
                    String fieldName = currToken.getLexeme();
                    matchToken(Token.IDENTIFIER, Lang.get("tkIdentDesc"));
                    lval = new FieldAccessLValue(lexer.getLine(), lval, fieldName);
                    break;
                case '[':
                    nextToken();
                    Expr indexExpr = expression();
                    matchToken(']', "]");
                    lval = new IndexedLValue(lexer.getLine(), lval, indexExpr);
                default:
                    /* Nothing */
            }
        }

        return lval;
    }

    private ArrayList<Expr> argumentList() throws HGSException, IOException {
        matchToken('(', "(");
        ArrayList<Expr> arguments = new ArrayList<>();

        if (currToken.getId() != ')') {
            Expr argExpr = expression();
            arguments.add(argExpr);

            if (currToken.getId() == ',') {
                while (currToken.getId() == ',') {
                    nextToken();
                    argExpr = expression();
                    arguments.add(argExpr);
                }
            }
        }
        matchToken(')', ")");

        return arguments;
    }

    private BinaryExpr.Operator mapTokenToOperator(Token tk) throws HGSException {
        switch (tk.getId()) {
            case '<':
                return BinaryExpr.Operator.LT;
            case '>':
                return BinaryExpr.Operator.GT;
            case Token.GE_OP:
                return BinaryExpr.Operator.GE;
            case Token.LE_OP:
                return BinaryExpr.Operator.LE;
            case Token.EQ_OP:
                return BinaryExpr.Operator.EQ;
            case Token.NE_OP:
                return BinaryExpr.Operator.NE;
            case Token.SL_OP:
                return BinaryExpr.Operator.SL;
            case Token.SR_OP:
                return BinaryExpr.Operator.SR;
            case Token.AND_OP:
                return BinaryExpr.Operator.LAND;
            case Token.OR_OP:
                return BinaryExpr.Operator.LOR;
            case '&':
                return BinaryExpr.Operator.AND;
            case '|':
                return BinaryExpr.Operator.OR;
            case '^':
                return BinaryExpr.Operator.XOR;
            case '+':
                return BinaryExpr.Operator.ADD;
            case '-':
                return BinaryExpr.Operator.SUB;
            case '*':
                return BinaryExpr.Operator.MULT;
            case '/':
                return BinaryExpr.Operator.DIV;
            case '%':
                return BinaryExpr.Operator.MOD;
        }
        throw new HGSException(lexer.getLine(), Lang.get("invalidBinaryOperator", tk));
    }

    private UnaryExpr.Operator mapTokenToUnaryOperator(Token currToken) {
        switch (currToken.getId()) {
            case '+':
                return UnaryExpr.Operator.ADD;
            case '-':
                return UnaryExpr.Operator.SUB;
            case '~':
                return UnaryExpr.Operator.NOT;
            case '!':
                return UnaryExpr.Operator.LNOT;
            default:
                throw new RuntimeException("Invalid unary operator '" + currToken.getLexeme() + "'");
        }
    }

    private interface ExprProducer {

        Expr get() throws HGSException, IOException;
    }

    private interface TokenMatcher {

        boolean accept(Token tk);
    }
}
