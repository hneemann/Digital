package de.neemann.digital.gui.components.karnaugh;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.gui.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static de.neemann.digital.analyse.expression.Not.not;

/**
 * shows the kv map
 */
public class KarnaughMapComponent extends JComponent {
    private static final int STROKE_WIDTH = 3;
    private KarnaughMap kv;
    private BoolTable boolTable;
    private ArrayList<Variable> vars;
    private Graphics2D gr;
    private int cellSize;
    private FontMetrics fontMetrics;

    /**
     * creates a new instance
     */
    public KarnaughMapComponent() {
        setPreferredSize(Screen.getInstance().scale(new Dimension(300, 300)));
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
            kv = null; // ToDo show massage
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        gr = (Graphics2D) graphics;
        int width = getWidth();
        int height = getHeight();
        gr.setColor(Color.WHITE);
        gr.fillRect(0, 0, width, height);
        AffineTransform trans = gr.getTransform();
        gr.setFont(getFont().deriveFont(cellSize * 0.5f));

        if (kv != null) {
            int kvWidth = kv.getHeaderTop().size();
            int kvHeight = kv.getHeaderLeft().size();
            cellSize = Math.min(height / (kvHeight + 3), width / (kvWidth + 3));

            gr.translate((width - (kvWidth + 2) * cellSize) / 2,
                    (height - (kvHeight + 2) * cellSize) / 2);
            gr.setColor(Color.BLACK);
            gr.setStroke(new BasicStroke(STROKE_WIDTH));

            // draw table
            for (int i = 0; i <= kvWidth; i++)
                gr.drawLine((i + 1) * cellSize, 0, (i + 1) * cellSize, (kvHeight + 2) * cellSize);

            for (int i = 0; i <= kvHeight; i++)
                gr.drawLine(0, (i + 1) * cellSize, (kvWidth + 2) * cellSize, (i + 1) * cellSize);

            // fill in content
            for (KarnaughMap.Cell cell : kv.getCells())
                drawString(boolTable.get(cell.getIndex()).toString(), cell.getCol() + 1, cell.getRow() + 1);

            // left header
            KarnaughMap.Header header = kv.getHeaderLeft();
            for (int i = 0; i < kvHeight; i++)
                drawString(getStr(header.getVar(), header.getInvert(i)), 0, i + 1);

            // top header
            header = kv.getHeaderTop();
            for (int i = 0; i < kvWidth; i++)
                drawString(getStr(header.getVar(), header.getInvert(i)), (i + 1), 0);

            // right header
            header = kv.getHeaderRight();
            if (header != null)
                for (int i = 0; i < kvHeight; i++)
                    drawString(getStr(header.getVar(), header.getInvert(i)), kvWidth + 1, i + 1);

            // bottom header
            header = kv.getHeaderBottom();
            if (header != null)
                for (int i = 0; i < kvWidth; i++)
                    drawString(getStr(header.getVar(), header.getInvert(i)), i + 1, kvWidth + 1);

            // draw covers
            for (KarnaughMap.Cover c : kv) {
                KarnaughMap.Pos p = c.getPos();
                int frame = 4;
                if (p.isSplit()) {
                    Rectangle clip = gr.getClipBounds();
                    gr.setClip((p.getCol() + 1) * cellSize + frame - STROKE_WIDTH / 2, (p.getRow() + 1) * cellSize + frame - STROKE_WIDTH / 2,
                            p.getWidth() * cellSize - frame * 2 + STROKE_WIDTH, p.getHeight() * cellSize - frame * 2 + STROKE_WIDTH);

                    int xofs = 0;
                    int yOfs = 0;
                    if (p.getWidth() > p.getHeight())
                        xofs = cellSize * 3;
                    else
                        yOfs = cellSize * 3;

                    gr.drawRoundRect((p.getCol() + 1) * cellSize + frame + xofs, (p.getRow() + 1) * cellSize + frame + yOfs,
                            p.getWidth() * cellSize - frame * 2, p.getHeight() * cellSize - frame * 2,
                            cellSize, cellSize);
                    gr.drawRoundRect((p.getCol() + 1) * cellSize + frame - xofs, (p.getRow() + 1) * cellSize + frame - yOfs,
                            p.getWidth() * cellSize - frame * 2, p.getHeight() * cellSize - frame * 2,
                            cellSize, cellSize);

                    gr.setClip(clip.x, clip.y, clip.width, clip.height);
                } else
                    gr.drawRoundRect((p.getCol() + 1) * cellSize + frame, (p.getRow() + 1) * cellSize + frame,
                            p.getWidth() * cellSize - frame * 2, p.getHeight() * cellSize - frame * 2,
                            cellSize, cellSize);
            }
            gr.setTransform(trans);
        }
    }

    private void drawString(String s, int row, int col) {
        fontMetrics = gr.getFontMetrics();
        Rectangle2D bounds = fontMetrics.getStringBounds(s, gr);
        int xPos = (int) ((cellSize - bounds.getWidth()) / 2);
        int yPos = cellSize - (int) ((cellSize - bounds.getHeight()) / 2) - fontMetrics.getDescent();
        gr.drawString(s, row * cellSize + xPos, col * cellSize + yPos);
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

}
