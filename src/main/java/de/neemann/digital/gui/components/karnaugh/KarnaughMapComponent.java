/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.karnaugh;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static de.neemann.digital.analyse.expression.Not.not;

/**
 * JComponent to show a kv map.
 */
public class KarnaughMapComponent extends JComponent {
    private static final int STROKE_WIDTH = 4;
    private static final Color[] COVER_COLORS = new Color[]{
            new Color(255, 0, 0, 128), new Color(0, 255, 0, 128),
            new Color(128, 0, 0, 128), new Color(0, 0, 128, 128),
            new Color(0, 0, 255, 128), new Color(255, 0, 255, 128),
            new Color(200, 200, 0, 128), new Color(0, 255, 255, 128)};
    private KarnaughMap kv;
    private BoolTable boolTable;
    private ArrayList<Variable> vars;
    private Graphics2D gr;
    private String message = Lang.get("msg_noKVMapAvailable");

    private int xOffs;
    private int yOffs;
    private int cellSize;

    /**
     * Creates a new instance
     *
     * @param modifier the modifier which is used to modify the truth table.
     */
    public KarnaughMapComponent(Modifier modifier) {
        setPreferredSize(Screen.getInstance().scale(new Dimension(400, 400)));
        if (modifier != null)
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    if (kv != null) {
                        int x = (mouseEvent.getX() - xOffs) / cellSize - 1;
                        int y = (mouseEvent.getY() - yOffs) / cellSize - 1;
                        if (x >= 0 && x < kv.getColumns() && y >= 0 && y < kv.getRows()) {
                            int row = kv.getCell(y, x).getBoolTableRow();
                            modifier.modify(boolTable, row);
                        }
                    }
                }
            });
    }

    /**
     * Sets a result to the table
     *
     * @param vars      the variables
     * @param boolTable the bool table
     * @param exp       the expression
     */
    public void setResult(ArrayList<Variable> vars, BoolTable boolTable, Expression exp) {
        this.vars = vars;
        this.boolTable = boolTable;
        try {
            kv = new KarnaughMap(vars, exp);
        } catch (KarnaughException e) {
            kv = null;
            message = e.getMessage();
        }
        repaint();
    }

    /**
     * Shows nothing
     */
    public void showNothing() {
        kv = null;
        message = Lang.get("msg_noKVMapAvailable");
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        gr = (Graphics2D) graphics;
        gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        int width = getWidth();
        int height = getHeight();
        gr.setColor(Color.WHITE);
        gr.fillRect(0, 0, width, height);
        gr.setColor(Color.BLACK);

        if (kv != null) {
            AffineTransform trans = gr.getTransform(); // store the old transform

            int kvWidth = kv.getColumns();
            int kvHeight = kv.getRows();
            cellSize = (int) Math.min(height / (kvHeight + 2.5f), width / (kvWidth + 2.5f));
            Font origFont = gr.getFont();
            Font valuesFont = origFont.deriveFont(cellSize * 0.5f);
            gr.setFont(valuesFont);

            Font headerFont = valuesFont;
            try {
                int maxHeaderStrWidth = 0;
                FontMetrics fontMetrics = gr.getFontMetrics();
                for (Variable v : vars) {
                    int w = fontMetrics.stringWidth(FormatToExpression.defaultFormat(not(v)));
                    if (w > maxHeaderStrWidth) maxHeaderStrWidth = w;
                }
                if (maxHeaderStrWidth > cellSize)
                    headerFont = origFont.deriveFont(cellSize * 0.5f * cellSize / maxHeaderStrWidth);
            } catch (FormatterException e) {
                // can not happen
            }

            xOffs = (width - (kvWidth + 2) * cellSize) / 2;
            yOffs = (height - (kvHeight + 2) * cellSize) / 2;
            gr.translate(xOffs, yOffs);   // center the kv map

            // draw table
            gr.setColor(Color.GRAY);
            gr.setStroke(new BasicStroke(STROKE_WIDTH / 2));
            for (int i = 0; i <= kvWidth; i++) {
                int dy1 = isNoHeaderLine(kv.getHeaderTop(), i - 1) ? cellSize : 0;
                int dy2 = isNoHeaderLine(kv.getHeaderBottom(), i - 1) ? cellSize : 0;
                gr.drawLine((i + 1) * cellSize, dy1, (i + 1) * cellSize, (kvHeight + 2) * cellSize - dy2);
            }

            for (int i = 0; i <= kvHeight; i++) {
                int dx1 = isNoHeaderLine(kv.getHeaderLeft(), i - 1) ? cellSize : 0;
                int dx2 = isNoHeaderLine(kv.getHeaderRight(), i - 1) ? cellSize : 0;
                gr.drawLine(dx1, (i + 1) * cellSize, (kvWidth + 2) * cellSize - dx2, (i + 1) * cellSize);
            }
            gr.setStroke(new BasicStroke(STROKE_WIDTH));

            // fill in bool table content
            for (KarnaughMap.Cell cell : kv.getCells()) {
                gr.setColor(Color.BLACK);
                gr.setFont(valuesFont);
                drawString(boolTable.get(cell.getBoolTableRow()).toString(), cell.getCol() + 1, cell.getRow() + 1);
                gr.setColor(Color.GRAY);
                gr.setFont(origFont);
                gr.drawString(Integer.toString(cell.getBoolTableRow()),
                        (cell.getCol() + 1) * cellSize + 1,
                        (cell.getRow() + 2) * cellSize - 1);
            }

            // draw the text in the borders
            gr.setColor(Color.BLACK);
            gr.setFont(headerFont);
            drawVerticalHeader(kv.getHeaderLeft(), 0);
            drawVerticalHeader(kv.getHeaderRight(), kvWidth + 1);
            drawHorizontalHeader(kv.getHeaderTop(), 0);
            drawHorizontalHeader(kv.getHeaderBottom(), kvHeight + 1);

            // draw the covers
            int color = 0;
            for (KarnaughMap.Cover c : kv) {
                gr.setColor(COVER_COLORS[color++]);
                KarnaughMap.Pos p = c.getPos();
                int frame = (c.getInset() + 1) * STROKE_WIDTH;
                int edgesRadius = cellSize - frame * 2;
                if (c.isDisconnected()) {
                    Rectangle clip = gr.getClipBounds();
                    gr.setClip(cellSize, cellSize, kvWidth * cellSize, kvHeight * cellSize);
                    if (c.onlyEdges()) {
                        gr.drawRoundRect(frame, frame, 2 * cellSize - frame * 2, 2 * cellSize - frame * 2, edgesRadius, edgesRadius);
                        gr.drawRoundRect(4 * cellSize + frame, frame, 2 * cellSize - frame * 2, 2 * cellSize - frame * 2, edgesRadius, edgesRadius);
                        gr.drawRoundRect(frame, 4 * cellSize + frame, 2 * cellSize - frame * 2, 2 * cellSize - frame * 2, edgesRadius, edgesRadius);
                        gr.drawRoundRect(4 * cellSize + frame, 4 * cellSize + frame, 2 * cellSize - frame * 2, 2 * cellSize - frame * 2, edgesRadius, edgesRadius);
                    } else { // draw the two parts of the cover
                        int xofs = 0;
                        int yOfs = 0;
                        if (c.isVerticalDivided())
                            xofs = cellSize * 3;
                        else
                            yOfs = cellSize * 3;

                        gr.drawRoundRect((p.getCol() + 1) * cellSize + frame + xofs, (p.getRow() + 1) * cellSize + frame + yOfs,
                                p.getWidth() * cellSize - frame * 2, p.getHeight() * cellSize - frame * 2,
                                edgesRadius, edgesRadius);
                        gr.drawRoundRect((p.getCol() + 1) * cellSize + frame - xofs, (p.getRow() + 1) * cellSize + frame - yOfs,
                                p.getWidth() * cellSize - frame * 2, p.getHeight() * cellSize - frame * 2,
                                edgesRadius, edgesRadius);

                    }
                    gr.setClip(clip.x, clip.y, clip.width, clip.height);
                } else
                    gr.drawRoundRect((p.getCol() + 1) * cellSize + frame, (p.getRow() + 1) * cellSize + frame,
                            p.getWidth() * cellSize - frame * 2, p.getHeight() * cellSize - frame * 2,
                            edgesRadius, edgesRadius);
            }
            gr.setTransform(trans);
        } else
            gr.drawString(message, 10, 20);
    }

    private boolean isNoHeaderLine(KarnaughMap.Header header, int i) {
        if (header == null) return false;
        if (i < 0 || i >= header.size() - 1) return false;
        return header.getInvert(i) == header.getInvert(i + 1);
    }

    //CHECKSTYLE.OFF: ModifiedControlVariable
    private void drawHorizontalHeader(KarnaughMap.Header header, int pos) {
        if (header != null)
            for (int i = 0; i < header.size(); i++) {
                int dx = 0;
                if (isNoHeaderLine(header, i)) {
                    i++;
                    dx = cellSize / 2;
                }
                drawString(getStr(header.getVar(), header.getInvert(i)), i + 1, pos, dx, 0);
            }
    }

    private void drawVerticalHeader(KarnaughMap.Header header, int pos) {
        if (header == null) return;
        for (int i = 0; i < header.size(); i++) {
            int dy = 0;
            if (isNoHeaderLine(header, i)) {
                i++;
                dy = cellSize / 2;
            }
            drawString(getStr(header.getVar(), header.getInvert(i)), pos, i + 1, 0, dy);
        }
    }
    //CHECKSTYLE.ON: ModifiedControlVariable

    private void drawString(String s, int row, int col) {
        drawString(s, row, col, 0, 0);
    }

    private void drawString(String s, int row, int col, int xOffs, int yOffs) {
        FontMetrics fontMetrics = gr.getFontMetrics();
        Rectangle2D bounds = fontMetrics.getStringBounds(s, gr);
        int xPos = (int) ((cellSize - bounds.getWidth()) / 2);
        int yPos = cellSize - (int) ((cellSize - bounds.getHeight()) / 2) - fontMetrics.getDescent();
        gr.drawString(s, row * cellSize + xPos - xOffs, col * cellSize + yPos - yOffs);
    }

    private String getStr(int var, boolean invert) {
        try {
            if (invert)
                return FormatToExpression.defaultFormat(not(vars.get(var)));
            else
                return FormatToExpression.defaultFormat(vars.get(var));
        } catch (FormatterException e) {
            // can not happen
            return "";
        }
    }

    /**
     * The modifier which is used to modify the bool table
     */
    public interface Modifier {

        /**
         * Is called if the k-map is clicked and the truth table needs to be changed.
         *
         * @param boolTable the table to modify
         * @param row       the row to modify
         */
        void modify(BoolTable boolTable, int row);
    }
}
