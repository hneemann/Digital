/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.Gal16v8;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionVisitor;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.builder.*;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

/**
 * Creates a CUPL file.
 * The default setting is usable for GAL16V8 chips.
 */
public class CuplExporter implements ExpressionExporter<CuplExporter> {
    private final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    private final String username;
    private final Date date;
    private final BuilderCollector builder;
    private final CleanNameBuilder cleanNameBuilder;

    private final PinMap pinMap;
    private final String devName;

    private String projectName;
    private boolean createNodes = false;
    private int clockPin = 1;

    /**
     * Creates a new project name
     */
    public CuplExporter() {
        this(System.getProperty("user.name"), new Date());
    }

    /**
     * Creates a new project name
     *
     * @param username user name
     * @param date     date
     */
    public CuplExporter(String username, Date date) {
        this(username, date, "g16v8a");
        getPinMapping()
                .setAvailInputs(2, 3, 4, 5, 6, 7, 8, 9)
                .setAvailOutputs(12, 13, 14, 15, 16, 17, 18, 19);
    }

    /**
     * Sets the clock pin.
     *
     * @param clockPin the clock pin
     */
    protected void setClockPin(int clockPin) {
        this.clockPin = clockPin;
    }

    /**
     * Creates a new instance
     *
     * @param username user name
     * @param date     creation date
     * @param devName  device name
     */
    protected CuplExporter(String username, Date date, String devName) {
        this.username = username;
        this.date = date;
        this.devName = devName;
        cleanNameBuilder = new CleanNameBuilder(null);
        this.pinMap = cleanNameBuilder.createPinMap();
        builder = new CuplBuilder(pinMap);
        cleanNameBuilder.setParent(builder);
    }

    /**
     * Sets the project name
     *
     * @param projectName the project name
     * @return this for call chaining
     */
    public CuplExporter setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    /**
     * Set the create nodes flag.
     * If "create nodes" is enabled the CUPL file contains a buried value as a NODE and not as a PIN assignment.
     *
     * @param createNodes true if the exporter should create nodes.
     */
    public void setCreateNodes(boolean createNodes) {
        this.createNodes = createNodes;
    }

    @Override
    public BuilderInterface getBuilder() {
        return cleanNameBuilder;
    }

    @Override
    public PinMap getPinMapping() {
        return pinMap;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException, PinMapException {
        writeTo(new OutputStreamWriter(out, StandardCharsets.ISO_8859_1));
    }

    /**
     * Writes code to given writer
     *
     * @param out the stream to write to
     * @throws IOException     IOException
     * @throws PinMapException PinMapException
     */
    public void writeTo(Writer out) throws IOException, PinMapException {
        out
                .append("Name     ").append(projectName).append(" ;\r\n")
                .append("PartNo   00 ;\r\n")
                .append("Date     ").append(formatDate(date)).append(" ;\r\n")
                .append("Revision 01 ;\r\n")
                .append("Designer ").append(username).append(" ;\r\n")
                .append("Company  unknown ;\r\n")
                .append("Assembly None ;\r\n")
                .append("Location unknown ;\r\n")
                .append("Device   ").append(devName).append(" ;\r\n");


        headerWritten(out);

        out.append("\r\n/* inputs */\r\n");
        if (!builder.getRegistered().isEmpty())
            out.append("PIN ").append(String.valueOf(clockPin)).append(" = CLK;\r\n");

        for (String in : builder.getInputs())
            out.append("PIN ").append(Integer.toString(pinMap.getInputFor(in))).append(" = ").append(in).append(";\r\n");

        out.append("\r\n/* outputs */\r\n");

        for (String var : builder.getOutputs()) {
            if (createNodes) {
                int p = pinMap.isOutputAssigned(var);
                if (p >= 0)
                    out.append("PIN ").append(Integer.toString(p)).append(" = ").append(var).append(";\r\n");
                else
                    out.append("NODE ").append(var).append(";\r\n");
            } else {
                out.append("PIN ").append(Integer.toString(pinMap.getOutputFor(var))).append(" = ").append(var).append(";\r\n");
            }
        }

        if (!builder.getRegistered().isEmpty()) {
            out.append("\r\n/* sequential logic */\r\n");
            for (Map.Entry<String, Expression> c : builder.getRegistered().entrySet()) {
                out.append(c.getKey()).append(".D = ");
                breakLines(out, FormatToExpression.CUPL.format(c.getValue()));
                out.append(";\r\n");
                sequentialWritten(out, c.getKey());
            }
        }

        if (!builder.getCombinatorial().isEmpty()) {
            out.append("\r\n/* combinatorial logic */\r\n");
            for (Map.Entry<String, Expression> c : builder.getCombinatorial().entrySet()) {
                out.append(c.getKey()).append(" = ");
                breakLines(out, FormatToExpression.CUPL.format(c.getValue()));
                out.append(";\r\n");
            }
        }

        out.flush();
    }

    private String formatDate(Date date) {
        if (date == null)
            return "unknownDate";
        else
            return dateFormat.format(date);
    }

    private void breakLines(Writer out, String expression) throws IOException {
        int pos = 0;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (pos > 80 && c == '#') {
                out.append("\r\n     ");
                pos = 0;
            }
            out.append(c);
            pos++;
        }
    }

    /**
     * Is called if header is written
     *
     * @param out the writer
     * @throws IOException IOException
     */
    protected void headerWritten(Writer out) throws IOException {
    }

    /**
     * Called is a sequential expression is written to the CUPL file
     *
     * @param out  Writer
     * @param name name of variable
     * @throws IOException IOException
     */
    protected void sequentialWritten(Writer out, String name) throws IOException {
    }

    private static final class CuplBuilder extends BuilderCollectorGAL {
        private final NotAllowedVariablesVisitor notAllowedVariablesVisitor = new NotAllowedVariablesVisitor();

        private CuplBuilder(PinMap pinMap) {
            super(pinMap);
        }

        @Override
        public BuilderCollector addCombinatorial(String name, Expression expression) throws BuilderException {
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
