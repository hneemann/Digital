/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Creates an image
 */
public final class GraphicsImage extends GraphicSwing implements Closeable {

    private final OutputStream out;
    private final String format;
    private final float scale;
    private BufferedImage bi;

    /**
     * Creates a new instance
     *
     * @param out    the output stream
     * @param format the format to write
     * @param scale  the scaling
     */
    public GraphicsImage(OutputStream out, String format, float scale) {
        super(null);
        this.out = out;
        this.format = format;
        this.scale = scale;
    }

    @Override
    public Graphic setBoundingBox(VectorInterface min, VectorInterface max) {
        int thickness = Style.MAXLINETHICK;
        bi = new BufferedImage(
                Math.round((max.getXFloat() - min.getXFloat() + thickness * 2) * scale),
                Math.round((max.getYFloat() - min.getYFloat() + thickness * 2) * scale),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = bi.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        gr.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        gr.setColor(new Color(255, 255, 255, 0));
        gr.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        gr.scale(scale, scale);
        gr.translate(thickness - min.getXFloat(), thickness - min.getYFloat());
        setGraphics2D(gr);
        return this;
    }

    @Override
    public void close() throws IOException {
        if (out != null) {
            if (bi != null)
                ImageIO.write(bi, format, out);
            out.close();
        }
    }

    /**
     * @return the created image
     */
    public BufferedImage getBufferedImage() {
        return bi;
    }
}
