package de.neemann.digital.draw.gif;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.core.NodeException;
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
 * Created by hneemann on 17.05.17.
 */
public class GifExporter extends JDialog implements ModelStateObserver, ModelModifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(GifExporter.class);
    private final Circuit circuit;
    private final int delayMs;
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
     */
    public GifExporter(JFrame parent, Circuit circuit, int delayMs) {
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
        this.delayMs = delayMs;
        minMax = new GraphicMinMax();
        circuit.drawTo(minMax);

        pack();
        setLocation(parent.getLocation());
    }

    /**
     * Exports the file
     *
     * @param file the file to write
     * @return this for chained calls
     * @throws IOException   IOException
     * @throws NodeException NodeException
     */
    public GifExporter export(File file) throws IOException, NodeException {
        output = new FileImageOutputStream(file);
        writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, delayMs, true);
        LOGGER.debug("open GIF file");
        return this;
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
        if (event.equals(ModelEvent.STEP)) {
            writeImage();
        }
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
        GraphicsImage gri = GraphicsImage.create(null, minMax.getMin(), minMax.getMax(), "gif", 1);
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
