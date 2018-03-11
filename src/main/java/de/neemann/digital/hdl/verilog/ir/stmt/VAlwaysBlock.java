/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.stmt;

import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import java.io.IOException;

/**
 * Represents a verilog always block.
 *
 * @author ideras
 */
 public class VAlwaysBlock extends VStatement {
    private final Event event;
    private final VExpr eventExpr;
    private final VStatement statement;

    /**
     * always block events
     */
    public enum Event {
        /**
         * positive edge
         */
        POSEDGE,
        /**
         * negative edge
         */
        NEGEDGE,
        /**
         * star (*)
         */
        STAR
    };

    /**
     * Initialize a new instance
     *
     * @param place the signal computed by the block
     * @param eventExpr the event expression
     * @param event the type of event
     * @param statement the statement
     */
    public VAlwaysBlock(VStatementPlace place, VExpr eventExpr, Event event, VStatement statement) {
        super(place);
        this.event = event;
        this.eventExpr = eventExpr;
        this.statement = statement;
    }

    /**
     * Creates a new instance
     *
     * @param place the signal computed by the block
     * @param statement the statement
     */
    public VAlwaysBlock(VStatementPlace place, VStatement statement) {
        this(place, null, Event.STAR, statement);
    }

    /**
     * Returns the always block event
     *
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Returns the statement
     *
     * @return the statement
     */
    public VStatement getStatement() {
        return statement;
    }

    @Override
    public void writeSourceCode(VerilogCodeBuilder vcBuilder, CodePrinter out) throws IOException {
        String eventStr = eventExpr.getSourceCode(vcBuilder);

        out.print("always @ (");
        switch (event) {
            case POSEDGE: out.print("posedge ").print(eventStr); break;
            case NEGEDGE: out.print("negedge ").print(eventStr); break;
            default:
               out.print("*"); break;
        }
        out.println(") begin").inc();
        statement.writeSourceCode(vcBuilder, out);
        out.dec().println().println("end");
    }

    @Override
    public VExpr resolveToExpr(VerilogCodeBuilder vcBuilder) {
        throw new RuntimeException("BUG in the machine: Calling VAlwaysStatement.resolveToExpr is invalid.");
    }
}
