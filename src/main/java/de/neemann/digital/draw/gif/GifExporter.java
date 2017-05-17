package de.neemann.digital.draw.gif;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.draw.graphics.GraphicsImage;
import de.neemann.digital.draw.graphics.linemerger.GraphicLineCollector;
import de.neemann.digital.draw.graphics.linemerger.GraphicSkipLines;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Exporter which creates an animated GIF file.
 * Created by hneemann on 17.05.17.
 */
public class GifExporter {
    private final Model model;
    private final Circuit circuit;
    private final int frames;
    private final int delayMs;
    private final GraphicMinMax minMax;

    /**
     * Creates a new instance
     *
     * @param model   the mode to use
     * @param circuit the circuit to export
     * @param frames  then number of frames to write to the file
     * @param delayMs the delay between frames im milliseconds
     */
    public GifExporter(Model model, Circuit circuit, int frames, int delayMs) {
        this.model = model;
        this.circuit = circuit;
        this.frames = frames;
        this.delayMs = delayMs;
        minMax = new GraphicMinMax();
        circuit.drawTo(minMax);
    }

    /**
     * Exports the file
     *
     * @param file the file to write
     * @throws IOException   IOException
     * @throws NodeException NodeException
     */
    public void export(File file) throws IOException, NodeException {
        try (ImageOutputStream output = new FileImageOutputStream(file)) {
            try (GifSequenceWriter writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, delayMs, true)) {
                for (int i = 0; i < frames; i++) {
                    writer.writeToSequence(createBufferedImage());

                    Clock clock = model.getClocks().get(0);
                    ObservableValue o = clock.getClockOutput();
                    o.setBool(!o.getBool());
                    model.doStep();
                }
            }
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
