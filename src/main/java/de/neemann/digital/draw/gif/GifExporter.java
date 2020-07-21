/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.gif;

import de.neemann.digital.core.*;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.draw.graphics.GraphicsImage;
import de.neemann.digital.draw.graphics.linemerger.GraphicLineCollector;
import de.neemann.digital.draw.graphics.linemerger.GraphicSkipLines;
import de.neemann.digital.gui.ModelModifier;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.Screen;
import de.neemann.gui.ToolTipAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Exporter which creates an animated GIF file.
 * You can attach it to a model and then every modification
 * of the running circuit is reordered as a new frame in the
 * GIF file.
 */
public class GifExporter extends JDialog implements ModelStateObserverTyped, ModelModifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(GifExporter.class);
    private final Circuit circuit;
    private final GraphicMinMax minMax;
    private final JLabel frameLabel;
    private int frames;
    private FileImageOutputStream output;
    private GifSequenceWriter writer;
    private boolean closed = false;

    /**
     * Creates a new instance
     *
     * @param parent  the parent frame
     * @param circuit the circuit to export
     * @param delayMs the delay between frames im milliseconds
     * @param file    the file to write
     * @throws IOException IOException
     */
    public GifExporter(JFrame parent, Circuit circuit, int delayMs, File file) throws IOException {
        super(parent, Lang.get("msg_gifExport"), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        frameLabel = new JLabel(Lang.get("msg_framesWritten_N", frames));
        frameLabel.setFont(Screen.getInstance().getFont(1.5f));
        frameLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(frameLabel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                close();
            }
        });

        getContentPane().add(new ToolTipAction(Lang.get("btn_gifComplete")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                close();
            }
        }.setToolTip(Lang.get("btn_gifComplete_tt")).createJButton(), BorderLayout.SOUTH);

        this.circuit = circuit;
        minMax = new GraphicMinMax();
        circuit.drawTo(minMax);

        LOGGER.debug("open GIF file");
        output = new FileImageOutputStream(file);
        writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, delayMs, true);

        pack();
        setLocation(parent.getLocation());
    }

    private void close() {
        if (!closed) {
            try {
                writer.close();
                output.close();
                LOGGER.debug("closed GIF file");
                closed = true;
            } catch (IOException e) {
                SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorWritingGif")).addCause(e));
            }
        }
        dispose();
    }

    @Override
    public void preInit(Model model) throws NodeException {
        SwingUtilities.invokeLater(() -> setVisible(true));
        model.addObserver(this);
    }

    @Override
    public void handleEvent(ModelEvent event) {
        if (event == ModelEvent.STEP)
            writeImage();
    }

    @Override
    public ModelEventType[] getEvents() {
        return new ModelEventType[]{ModelEventType.STEP};
    }

    private void writeImage() {
        if (!closed) {
            try {
                writer.writeToSequence(createBufferedImage());
            } catch (IOException e) {
                SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorWritingGif")).addCause(e));
            }
            frames++;
            frameLabel.setText(Lang.get("msg_framesWritten_N", frames));
            LOGGER.debug("frame written to GIF file");
        }
    }

    private BufferedImage createBufferedImage() throws IOException {
        GraphicsImage gri = new GraphicsImage(null, "gif", 1);
        gri.setBoundingBox(minMax.getMin(), minMax.getMax());
        BufferedImage bi = gri.getBufferedImage();
        Graphics gr = bi.getGraphics();
        gr.setColor(Color.WHITE);
        gr.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        GraphicLineCollector glc = new GraphicLineCollector();
        circuit.drawTo(glc);
        glc.drawTo(gri);

        circuit.drawTo(new GraphicSkipLines(gri));

        return gri.getBufferedImage();
    }

}
