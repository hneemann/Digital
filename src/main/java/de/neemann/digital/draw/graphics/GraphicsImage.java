package de.neemann.digital.draw.graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Creates an image
 *
 * @author hneemann
 */
public final class GraphicsImage extends GraphicSwing implements Closeable {

    /**
     * Creates a new instance of this class
     *
     * @param out    the OutputStream to write the image to
     * @param min    upper left corner
     * @param max    lower right corner
     * @param format the format to write
     * @return the {@link Graphic} instance
     */
    public static GraphicsImage create(OutputStream out, Vector min, Vector max, String format, float scale) {
        int thickness = Style.NORMAL.getThickness();
        BufferedImage bi
                = new BufferedImage(
                Math.round((max.x - min.x + thickness * 2) * scale),
                Math.round((max.y - min.y + thickness * 2) * scale),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = bi.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        gr.setColor(new Color(255, 255, 255, 0));
        gr.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        gr.scale(scale, scale);
        gr.translate(thickness - min.x, thickness - min.y);

        return new GraphicsImage(out, gr, bi, format);
    }

    private final OutputStream out;
    private final BufferedImage bi;
    private final String format;

    private GraphicsImage(OutputStream out, Graphics2D gr, BufferedImage bi, String format) {
        super(gr);
        this.out = out;
        this.bi = bi;
        this.format = format;
    }

    @Override
    public void close() throws IOException {
        ImageIO.write(bi, format, out);
    }
}
