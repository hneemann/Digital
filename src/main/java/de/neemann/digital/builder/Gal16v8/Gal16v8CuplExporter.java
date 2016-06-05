package de.neemann.digital.builder.Gal16v8;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionVisitor;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.builder.*;
import de.neemann.digital.builder.jedec.FuseMapFillerException;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

/**
 * Creates a CUPL file
 *
 * @author hneemann
 */
public class Gal16v8CuplExporter implements ExpressionExporter<Gal16v8CuplExporter> {
    private final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    private final String projectName;
    private final String username;
    private final Date date;
    private final BuilderCollector builder;
    private final PinMap pinMap;

    /**
     * Creates a new project name
     *
     * @param projectName the project name
     */
    public Gal16v8CuplExporter(String projectName) {
        this(projectName, System.getProperty("user.name"), new Date());
    }

    /**
     * Creates a new project name
     *
     * @param projectName the project name
     * @param username    user name
     * @param date        date
     */
    public Gal16v8CuplExporter(String projectName, String username, Date date) {
        this.projectName = projectName;
        this.username = username;
        this.date = date;
        builder = new CuplBuilder();
        pinMap = createPinMap();
    }

    protected PinMap createPinMap() {
        return new PinMap()
                .setAvailInputs(2, 3, 4, 5, 6, 7, 8, 9)
                .setAvailOutputs(12, 13, 14, 15, 16, 17, 18, 19);
    }


    @Override
    public BuilderCollector getBuilder() {
        return builder;
    }

    @Override
    public PinMap getPinMapping() {
        return pinMap;
    }

    /**
     * Writes code to given writer
     *
     * @param out the stream to write to
     * @throws IOException            IOException
     * @throws FuseMapFillerException FuseMapFillerException
     * @throws PinMapException        PinMapException
     */
    public void writeTo(Writer out) throws IOException, FuseMapFillerException, PinMapException {
        out
                .append("Name     ").append(projectName).append(" ;\r\n")
                .append("PartNo   00 ;\r\n")
                .append("Date     ").append(dateFormat.format(date)).append(" ;\r\n")
                .append("Revision 01 ;\r\n")
                .append("Designer ").append(username).append(" ;\r\n")
                .append("Company  unknown ;\r\n")
                .append("Assembly None ;\r\n")
                .append("Location unknown ;\r\n")
                .append("Device   " + "g16v8a ;\r\n");

        out.append("\r\n/* inputs */\r\n");
        if (!builder.getRegistered().isEmpty())
            out.append("PIN 1 = CLK;\r\n");

        for (String in : builder.getInputs())
            out.append("PIN ").append(Integer.toString(pinMap.getInputFor(in))).append(" = ").append(in).append(";\r\n");

        out.append("\r\n/* outputs */\r\n");

        for (String var : builder.getOutputs())
            out.append("PIN ").append(Integer.toString(pinMap.getOutputFor(var))).append(" = ").append(var).append(";\r\n");

        try {
            if (!builder.getRegistered().isEmpty()) {
                out.append("\r\n/* sequential logic */\r\n");
                for (Map.Entry<String, Expression> c : builder.getRegistered().entrySet())
                    out
                            .append(c.getKey())
                            .append(".D = ")
                            .append(FormatToExpression.FORMATTER_CUPL.format(c.getValue()))
                            .append(";\r\n");
            }

            if (!builder.getCombinatorial().isEmpty()) {
                out.append("\r\n/* combinatorial logic */\r\n");
                for (Map.Entry<String, Expression> c : builder.getCombinatorial().entrySet())
                    out
                            .append(c.getKey()).append(" = ")
                            .append(FormatToExpression.FORMATTER_CUPL.format(c.getValue()))
                            .append(";\r\n");

            }
        } catch (FormatterException e) {
            throw new IOException(e);
        }

        out.flush();
    }

    @Override
    public void writeTo(OutputStream out) throws FuseMapFillerException, IOException, PinMapException {
        writeTo(new OutputStreamWriter(out, "ISO-8859-1"));
    }

    private final class CuplBuilder extends BuilderCollector {
        private final NotAllowedVariablesVisitor notAllowedVariablesVisitor = new NotAllowedVariablesVisitor();

        @Override
        public BuilderCollector addCombinatorial(String name, Expression expression) throws BuilderException {
            if (pinMap.isSimpleAlias(name, expression))
                return this;  // ignore simple variables!

            expression.traverse(notAllowedVariablesVisitor);
            notAllowedVariablesVisitor.check(name);
            return super.addCombinatorial(name, expression);
        }

        @Override
        public BuilderCollector addSequential(String name, Expression expression) throws BuilderException {
            expression.traverse(notAllowedVariablesVisitor);
            notAllowedVariablesVisitor.check(name);
            return super.addSequential(name, expression);
        }
    }

    private static final class NotAllowedVariablesVisitor implements ExpressionVisitor {
        private static final String NOT_ALLOWED_CHARS = " &#()-+[]/:.*;,!'=@$^\"";
        private final HashSet<String> notAllowed;

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
            for (int i = 0; i < NOT_ALLOWED_CHARS.length(); i++)
                if (v.indexOf(NOT_ALLOWED_CHARS.charAt(i)) >= 0)
                    throw new RuntimeException(Lang.get("err_varNotAllowedInCUPL_N", v));
            if (notAllowed.contains(v))
                throw new RuntimeException(Lang.get("err_varNotAllowedInCUPL_N", v));
        }

    }

}
