/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.graphics;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * Analyzes VGA signals
 */
public class VGA extends Node implements Element {

    private static final HashMap<VideoId, VideoMode> MODES = new HashMap<>();

    static {
        vm(new VideoMode(70, 25.175, 640, 16, 96, 48, 350, 37, 2, 60, false, true));
        vm(new VideoMode(85, 31.5, 640, 32, 64, 96, 350, 32, 3, 60, false, true));
        vm(new VideoMode(70, 25.175, 640, 16, 96, 48, 400, 12, 2, 35, true, false));
        vm(new VideoMode(85, 31.5, 640, 32, 64, 96, 400, 1, 3, 41, true, false));
        vm(new VideoMode(60, 25.175, 640, 16, 96, 48, 480, 10, 2, 33, true, true));
        vm(new VideoMode(73, 31.5, 640, 24, 40, 128, 480, 9, 2, 29, true, true));
        vm(new VideoMode(75, 31.5, 640, 16, 64, 120, 480, 1, 3, 16, true, true));
        vm(new VideoMode(85, 36, 640, 56, 56, 80, 480, 1, 3, 25, true, true));
        vm(new VideoMode(100, 43.16, 640, 40, 64, 104, 480, 1, 3, 25, true, false));
        vm(new VideoMode(85, 35.5, 720, 36, 72, 108, 400, 1, 3, 42, true, false));
        vm(new VideoMode(60, 34.96, 768, 24, 80, 104, 576, 1, 3, 17, true, false));
        vm(new VideoMode(72, 42.93, 768, 32, 80, 112, 576, 1, 3, 21, true, false));
        vm(new VideoMode(75, 45.51, 768, 40, 80, 120, 576, 1, 3, 22, true, false));
        vm(new VideoMode(85, 51.84, 768, 40, 80, 120, 576, 1, 3, 25, true, false));
        vm(new VideoMode(100, 62.57, 768, 48, 80, 128, 576, 1, 3, 31, true, false));
        vm(new VideoMode(56, 36, 800, 24, 72, 128, 600, 1, 2, 22, false, false));
        vm(new VideoMode(60, 40, 800, 40, 128, 88, 600, 1, 4, 23, false, false));
        vm(new VideoMode(75, 49.5, 800, 16, 80, 160, 600, 1, 3, 21, false, false));
        vm(new VideoMode(72, 50, 800, 56, 120, 64, 600, 37, 6, 23, false, false));
        vm(new VideoMode(85, 56.25, 800, 32, 64, 152, 600, 1, 3, 27, false, false));
        vm(new VideoMode(100, 68.18, 800, 48, 88, 136, 600, 1, 3, 32, true, false));
        vm(new VideoMode(43, 44.9, 1024, 8, 176, 56, 768, 0, 8, 41, false, false));
        vm(new VideoMode(60, 65, 1024, 24, 136, 160, 768, 3, 6, 29, true, true));
        vm(new VideoMode(70, 75, 1024, 24, 136, 144, 768, 3, 6, 29, true, true));
        vm(new VideoMode(75, 78.8, 1024, 16, 96, 176, 768, 1, 3, 28, false, false));
        vm(new VideoMode(85, 94.5, 1024, 48, 96, 208, 768, 1, 3, 36, false, false));
        vm(new VideoMode(100, 113.31, 1024, 72, 112, 184, 768, 1, 3, 42, true, false));
        vm(new VideoMode(75, 108, 1152, 64, 128, 256, 864, 1, 3, 32, false, false));
        vm(new VideoMode(85, 119.65, 1152, 72, 128, 200, 864, 1, 3, 39, true, false));
        vm(new VideoMode(100, 143.47, 1152, 80, 128, 208, 864, 1, 3, 47, true, false));
        vm(new VideoMode(60, 81.62, 1152, 64, 120, 184, 864, 1, 3, 27, true, false));
        vm(new VideoMode(60, 108, 1280, 48, 112, 248, 1024, 1, 3, 38, false, false));
        vm(new VideoMode(75, 135, 1280, 16, 144, 248, 1024, 1, 3, 38, false, false));
        vm(new VideoMode(85, 157.5, 1280, 64, 160, 224, 1024, 1, 3, 44, false, false));
        vm(new VideoMode(100, 190.96, 1280, 96, 144, 240, 1024, 1, 3, 57, true, false));
        vm(new VideoMode(60, 83.46, 1280, 64, 136, 200, 800, 1, 3, 24, true, false));
        vm(new VideoMode(60, 102.1, 1280, 80, 136, 216, 960, 1, 3, 30, true, false));
        vm(new VideoMode(72, 124.54, 1280, 88, 136, 224, 960, 1, 3, 37, true, false));
        vm(new VideoMode(75, 129.86, 1280, 88, 136, 224, 960, 1, 3, 38, true, false));
        vm(new VideoMode(85, 148.5, 1280, 64, 160, 224, 960, 1, 3, 47, false, false));
        vm(new VideoMode(100, 178.99, 1280, 96, 144, 240, 960, 1, 3, 53, true, false));
        vm(new VideoMode(60, 85.86, 1368, 72, 144, 216, 768, 1, 3, 23, true, false));
        vm(new VideoMode(60, 122.61, 1400, 88, 152, 240, 1050, 1, 3, 33, true, false));
        vm(new VideoMode(72, 149.34, 1400, 96, 152, 248, 1050, 1, 3, 40, true, false));
        vm(new VideoMode(75, 155.85, 1400, 96, 152, 248, 1050, 1, 3, 42, true, false));
        vm(new VideoMode(85, 179.26, 1400, 104, 152, 256, 1050, 1, 3, 49, true, false));
        vm(new VideoMode(100, 214.39, 1400, 112, 152, 264, 1050, 1, 3, 58, true, false));
        vm(new VideoMode(60, 106.47, 1440, 80, 152, 232, 900, 1, 3, 28, true, false));
        vm(new VideoMode(60, 162, 1600, 64, 192, 304, 1200, 1, 3, 46, false, false));
        vm(new VideoMode(100, 280.64, 1600, 128, 176, 304, 1200, 1, 3, 67, true, false));
        vm(new VideoMode(60, 147.14, 1680, 104, 184, 288, 1050, 1, 3, 33, true, false));
        vm(new VideoMode(60, 204.8, 1792, 128, 200, 328, 1344, 1, 3, 46, true, false));
        vm(new VideoMode(75, 261, 1792, 96, 216, 352, 1344, 1, 3, 69, true, false));
        vm(new VideoMode(60, 218.3, 1856, 96, 224, 352, 1392, 1, 3, 43, true, false));
        vm(new VideoMode(75, 288, 1856, 128, 224, 352, 1392, 1, 3, 104, true, false));
        vm(new VideoMode(60, 193.16, 1920, 128, 208, 336, 1200, 1, 3, 38, true, false));
        vm(new VideoMode(60, 234, 1920, 128, 208, 344, 1440, 1, 3, 56, true, false));
        vm(new VideoMode(75, 297, 1920, 144, 224, 352, 1440, 1, 3, 56, true, false));
    }

    private static void vm(VideoMode videoMode) {
        MODES.put(videoMode.id(), videoMode);
    }

    /**
     * The terminal description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(VGA.class,
            input("R"),
            input("G"),
            input("B"),
            input("H"),
            input("V"),
            input("C").setClock())
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL);

    private ObservableValue r;
    private ObservableValue g;
    private ObservableValue b;
    private ObservableValue hSync;
    private ObservableValue vSync;
    private ObservableValue clock;
    private boolean lastClock;
    private int xPos;
    private int yPos;
    private int lineCount;
    private int lineLen;
    private int lineLenStable;
    private int lineCountStable;
    private long maxCol;
    private BufferedImage image;
    private VideoMode mode;
    private SyncDetector hSyncDetection = new SyncDetector();
    private SyncDetector vSyncDetection = new SyncDetector();
    private VGADialog graphicDialog;
    private String label;

    /**
     * Creates a new instance
     *
     * @param attr the attributes
     */
    public VGA(ElementAttributes attr) {
        label = attr.getLabel();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        r = inputs.get(0);
        int bits = r.getBits();
        maxCol = Bits.up(1, bits) - 1;
        g = inputs.get(1).checkBits(bits, this);
        b = inputs.get(2).checkBits(bits, this);
        hSync = inputs.get(3).checkBits(1, this);
        vSync = inputs.get(4).checkBits(1, this);
        clock = inputs.get(5).checkBits(1, this).addObserverToValue(this);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean actClock = clock.getBool();
        if (actClock && !lastClock) {
            xPos++;
            if (hSyncDetection.add(hSync.getBool())) {
                setLineLen(xPos);
                xPos = 0;
                yPos++;
            }

            if (vSyncDetection.add(vSync.getBool())) {
                setLineCount(yPos);
                yPos = 0;
            }

            if (lineCountStable > 2 && lineLenStable > 100) {
                setPixel(xPos, yPos,
                        new Color(
                                col(r.getValue()),
                                col(g.getValue()),
                                col(b.getValue())));
            }
        }
        lastClock = actClock;
    }

    private void setPixel(int xPos, int yPos, Color color) {
        if (mode == null) {
            VideoId id = new VideoId(
                    lineLen, hSyncDetection.syncPulse(), hSyncDetection.isNegPolarity(),
                    lineCount, (vSyncDetection.syncPulse() + lineLen / 2) / lineLen, vSyncDetection.isNegPolarity());

            mode = MODES.get(id);
            if (mode == null)
                throw new RuntimeException(Lang.get("err_vgaModeNotDetected_N", id));
            image = mode.createImage();
        }
        mode.set(image, xPos, yPos, color);
        updateGraphic();
    }

    private int col(long value) {
        return (int) (value * 255 / maxCol);
    }

    private void setLineCount(int lc) {
        if (lc == lineCount) {
            lineCountStable++;
        } else {
            lineCountStable = 0;
        }
        lineCount = lc;
    }

    private void setLineLen(int ll) {
        if (ll == lineLen) {
            lineLenStable++;
        } else {
            lineLenStable = 0;
        }
        lineLen = ll;
    }

    @Override
    public void writeOutputs() throws NodeException {
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void init(Model model) {
    }

    private final AtomicBoolean paintPending = new AtomicBoolean();

    private void updateGraphic() {
        if (paintPending.compareAndSet(false, true)) {
            SwingUtilities.invokeLater(() -> {
                if (graphicDialog == null || !graphicDialog.isVisible()) {
                    graphicDialog = new VGADialog(getModel().getWindowPosManager().getMainFrame(), mode.toString(), image);
                    getModel().getWindowPosManager().register("VGA_" + label, graphicDialog);
                }
                paintPending.set(false);
                graphicDialog.updateGraphic();
            });
        }
    }

    static class SyncDetector {
        private boolean lastS;
        private int high;
        private int highLen;
        private int low;
        private int lowLen;

        boolean add(boolean s) {
            if (s) {
                high++;
                if (!lastS) {
                    lowLen = low;
                    low = 0;
                }
            } else {
                low++;
                if (lastS) {
                    highLen = high;
                    high = 0;
                }
            }

            boolean result = false;
            if (lowLen > 0 && highLen > 0) {
                if (lowLen > highLen) {
                    //positive;
                    result = lastS & !s;
                } else {
                    //negative
                    result = !lastS & s;
                }
            }
            lastS = s;
            return result;
        }

        boolean isNegPolarity() {
            return lowLen < highLen;
        }

        int syncPulse() {
            if (isNegPolarity())
                return lowLen;
            else
                return highLen;
        }

    }

    private static final class VideoMode {
        private final int refresh;
        private final double pixClock;
        private final int hDisplay;
        private final int hFrontPorch;
        private final int hSync;
        private final int hBackPorch;
        private final int vDisplay;
        private final int vFrontPorch;
        private final int vSync;
        private final int vBackPorch;
        private final boolean hNegative;
        private final boolean vNegative;

        //CHECKSTYLE.OFF: ParameterNumber
        private VideoMode(int refresh, double pixClock,
                          int hDisplay, int hFrontPorch, int hSync, int hBackPorch,
                          int vDisplay, int vFrontPorch, int vSync, int vBackPorch,
                          boolean hNegative, boolean vNegative) {
            this.refresh = refresh;
            this.pixClock = pixClock;
            this.hDisplay = hDisplay;
            this.hFrontPorch = hFrontPorch;
            this.hSync = hSync;
            this.hBackPorch = hBackPorch;
            this.vDisplay = vDisplay;
            this.vFrontPorch = vFrontPorch;
            this.vSync = vSync;
            this.vBackPorch = vBackPorch;
            this.hNegative = hNegative;
            this.vNegative = vNegative;
        }
        //CHECKSTYLE.ON: ParameterNumber

        @Override
        public String toString() {
            return hDisplay + "x" + vDisplay + "x" + refresh + "Hz, " + pixClock + "MHz";
        }

        public VideoId id() {
            return new VideoId(
                    hDisplay + hFrontPorch + hSync + hBackPorch, hSync, hNegative,
                    vDisplay + vFrontPorch + vSync + vBackPorch, vSync, vNegative);
        }

        private BufferedImage createImage() {
            return new BufferedImage(hDisplay, vDisplay, BufferedImage.TYPE_INT_RGB);
        }

        private void set(BufferedImage image, int xPos, int yPos, Color color) {
            xPos -= hBackPorch;
            yPos -= vBackPorch;
            if (xPos >= 0 && xPos < hDisplay && yPos >= 0 && yPos < vDisplay)
                image.setRGB(xPos, yPos, color.getRGB());
        }

    }

    private static final class VideoId {
        private final int width;
        private final int hSync;
        private final boolean hNegative;
        private final int height;
        private final int vSync;
        private final boolean vNegative;

        private VideoId(int width, int hSync, boolean hNegative, int height, int vSync, boolean vNegative) {
            this.width = width;
            this.hSync = hSync;
            this.hNegative = hNegative;
            this.height = height;
            this.vSync = vSync;
            this.vNegative = vNegative;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VideoId videoId = (VideoId) o;
            return width == videoId.width
                    && hSync == videoId.hSync
                    && hNegative == videoId.hNegative
                    && height == videoId.height
                    && vSync == videoId.vSync
                    && vNegative == videoId.vNegative;
        }

        @Override
        public int hashCode() {
            return Objects.hash(width, hSync, hNegative, height, vSync, vNegative);
        }

        @Override
        public String toString() {
            return "line len=" + width
                    + ", hSync len=" + hSync
                    + ", hNegative=" + hNegative
                    + ", line count=" + height
                    + ", vSync len=" + vSync
                    + ", vNegative=" + vNegative;
        }
    }
}
