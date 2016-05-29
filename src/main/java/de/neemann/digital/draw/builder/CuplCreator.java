package de.neemann.digital.draw.builder;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionVisitor;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.VariableVisitor;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author hneemann
 */
public class CuplCreator implements BuilderInterface<CuplCreator> {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static final NotAllowedVariablesVisitor NOT_ALLOWED_VARIABLES_VISITOR = new NotAllowedVariablesVisitor();

    private final StringBuilder expressions;
    private final String projectName;
    private final TreeSet<String> outVars;
    private final VariableVisitor vars;
    private final String username;
    private final Date date;
    private boolean sequential = false;

    /**
     * Creates a new project name
     *
     * @param projectName the project name
     */
    public CuplCreator(String projectName) {
        this(projectName, System.getProperty("user.name"), new Date());
    }

    /**
     * Creates a new project name
     *
     * @param projectName the project name
     * @param username    user name
     * @param date        date
     */
    public CuplCreator(String projectName, String username, Date date) {
        this.projectName = projectName;
        this.username = username;
        this.date = date;
        this.expressions = new StringBuilder();
        outVars = new TreeSet<>();
        vars = new VariableVisitor();
    }

    @Override
    public CuplCreator addExpression(String name, Expression expression) throws BuilderException {
        addOutVar(name);
        addToStr(name, expression);
        return this;
    }

    @Override
    public CuplCreator addState(String name, Expression expression) throws BuilderException {
        sequential = true;
        addOutVar(name);
        addToStr(name + ".D", expression);
        return this;
    }

    private void addOutVar(String name) {
        NOT_ALLOWED_VARIABLES_VISITOR.check(name);
        outVars.add(name);
    }

    private void addToStr(String name, Expression expression) throws BuilderException {
        expression.traverse(vars);
        expression.traverse(NOT_ALLOWED_VARIABLES_VISITOR);
        try {
            expressions
                    .append(name)
                    .append(" = ")
                    .append(FormatToExpression.FORMATTER_CUPL.format(expression))
                    .append(" ;\r\n");
        } catch (FormatterException e) {
            throw new BuilderException(e.getMessage());
        }
    }

    /**
     * Writes code to given writer
     *
     * @param out the stream to write to
     * @throws IOException IOException
     */
    public void writeTo(Writer out) throws IOException {
        out
                .append("Name     ").append(projectName).append(" ;\r\n")
                .append("PartNo   00 ;\r\n")
                .append("Date     ").append(DATE_FORMAT.format(date)).append(" ;\r\n")
                .append("Revision 01 ;\r\n")
                .append("Designer ").append(username).append(" ;\r\n")
                .append("Company  unknown ;\r\n")
                .append("Assembly None ;\r\n")
                .append("Location unknown ;\r\n")
                .append("Device   g16v8a ;\r\n");

        ArrayList<String> inputs = new ArrayList<>();
        for (Variable v : vars.getVariables())
            if (!outVars.contains(v.getIdentifier()))
                inputs.add(v.getIdentifier());

        out.append("\r\n/* inputs */\r\n");
        if (sequential)
            out.append("PIN 1 = CLK;\r\n");
        int inNum = 2;
        for (String in : inputs)
            out.append("PIN ").append(Integer.toString(inNum++)).append(" = ").append(in).append(";\r\n");

        out.append("\r\n/* outputs */\r\n");
        int outNum = 12;
        for (String var : outVars)
            out.append("PIN ").append(Integer.toString(outNum++)).append(" = ").append(var).append(";\r\n");

        out.append("\r\n/* logic */\r\n");

        out.append(expressions);
        out.flush();
    }

    /**
     * Writes code to given stream
     *
     * @param out the stream to write to
     * @throws IOException IOException
     */
    public void writeTo(OutputStream out) throws IOException {
        writeTo(new OutputStreamWriter(out, "ISO-8859-1"));
    }

    private static final class NotAllowedVariablesVisitor implements ExpressionVisitor {
        private final HashSet<String> notAllowed;
        private final String notAllowedChars = " &#()-+[]/:.*;,!'=@$^\"";

        NotAllowedVariablesVisitor() {
            notAllowed = new HashSet<>();
            add("APPEND", "FUNCTION", "PARTNO", "ASSEMBLY", "FUSE", "PIN",
                    "ASSY", "GROUP", "PINNNODE", "COMPANY", "IF", "PRESENT",
                    "CONDITION", "JUMP", "REV", "DATE", "LOC", "REVISION",
                    "DEFAULT", "LOCATION", "SEQUENCE", "DESIGNER", "MACRO", "SEQUENCED",
                    "DEVICE", "MIN", "SEQUENCEJK", "ELSE", "NAME", "SEQUENCERS",
                    "FIELD", "NODE", "SEQUENCET", "FLD", "OUT", "TABLE", "FORMAT");
            add("D");
        }

        private void add(String... names) {
            Collections.addAll(notAllowed, names);
        }

        @Override
        public boolean visit(Expression expression) {
            if (expression instanceof Variable) {
                Variable v = (Variable) expression;
                check(v.getIdentifier());
            }
            return true;
        }

        private void check(String v) {
            for (int i = 0; i < notAllowedChars.length(); i++)
                if (v.indexOf(notAllowedChars.charAt(i)) >= 0)
                    throw new RuntimeException(Lang.get("err_varNotAllowedInCUPL_N", v));
            if (notAllowed.contains(v))
                throw new RuntimeException(Lang.get("err_varNotAllowedInCUPL_N", v));
        }

    }
}
