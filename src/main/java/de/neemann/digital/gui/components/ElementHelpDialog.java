package de.neemann.digital.gui.components;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.*;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;

/**
 * Simple Dialog to show an elements help text.
 * <p/>
 * Created by hneemann on 25.10.16.
 */
public class ElementHelpDialog extends JDialog {

    private static final int MAX_WIDTH = 600;
    private static final int MAX_HEIGHT = 800;

    /**
     * Creates a new instance
     *
     * @param parent            the parents dialog
     * @param elementType       the type of the element
     * @param elementAttributes the attributes of this element
     */
    public ElementHelpDialog(JDialog parent, ElementTypeDescription elementType, ElementAttributes elementAttributes) {
        super(parent, Lang.get("attr_help"), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        String description = getDetailedDescription(elementType, elementAttributes);
        init(parent, description);
    }

    /**
     * Creates a new instance
     *
     * @param parent  the parents dialog
     * @param library the elements library
     */
    public ElementHelpDialog(JFrame parent, ElementLibrary library) {
        super(parent, Lang.get("attr_help"), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        ArrayList<String> chapter = new ArrayList<>();

        String actPath = null;
        StringBuilder content = new StringBuilder();
        for (ElementLibrary.ElementContainer e : library) {
            String p = e.getTreePath();
            if (!p.equals(actPath)) {
                actPath = p;
                chapter.add(actPath);
                content.append("<h2><a name=\"").append(actPath).append("\">").append(actPath).append("</a></h2>\n");
                content.append("<hr/>");
            }
            content.append("<center><img src=\"image:").append(e.getDescription().getName()).append(".png\"/></center>\n");
            addHTMLDescription(content, e.getDescription(), new ElementAttributes());
            content.append("<hr/>");
        }


        StringBuilder sb = new StringBuilder("<html><body>");
        sb.append("<h1>").append(Lang.get("digital")).append("</h1>\n");
        for (String chap : chapter) {
            sb.append("<a href=\"#").append(chap).append("\">").append(chap).append("</a><br/>\n");
        }
        sb.append(content);
        sb.append("</body></html>");

        init(parent, sb.toString());
    }

    private void init(Component parent, String description) {
        JEditorPane editorPane = new JEditorPane("text/html", description);
        editorPane.setEditable(false);
        editorPane.setCaretPosition(0);

        editorPane.addHyperlinkListener(hyperlinkEvent -> {
            if (HyperlinkEvent.EventType.ACTIVATED == hyperlinkEvent.getEventType()) {
                String desc = hyperlinkEvent.getDescription();
                if (desc == null || !desc.startsWith("#")) return;
                desc = desc.substring(1);
                editorPane.scrollToReference(desc);
            }
        });

        getContentPane().add(new JScrollPane(editorPane));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(new AbstractAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        }));
        getContentPane().add(buttons, BorderLayout.SOUTH);

        pack();
        Dimension r = getSize();
        if (r.width > MAX_WIDTH) r.width = MAX_WIDTH;
        if (r.height > MAX_HEIGHT) r.height = MAX_HEIGHT;
        setSize(r);
        setLocationRelativeTo(parent);
    }

    /**
     * Creates a detailed human readable description of this element
     *
     * @param et                the element to describe
     * @param elementAttributes the actual attributes of the element to describe
     * @return the human readable description of this element
     */
    private static String getDetailedDescription(ElementTypeDescription et, ElementAttributes elementAttributes) {
        StringBuilder sb = new StringBuilder("<html><body>");
        addHTMLDescription(sb, et, elementAttributes);
        return sb.append("</body></html>").toString();
    }

    /**
     * Adds the description of the given element to the given StringBuilder.
     *
     * @param sb                the StringBuilder to use
     * @param et                the element to describe
     * @param elementAttributes the actual attributes of the element to describe
     */
    public static void addHTMLDescription(StringBuilder sb, ElementTypeDescription et, ElementAttributes elementAttributes) {
        String translatedName = et.getTranslatedName();
        if (translatedName.endsWith(".dig"))
            translatedName = new File(translatedName).getName();
        sb.append("<h3>").append(translatedName).append("</h3>\n");
        String descr = et.getDescription(elementAttributes);
        if (!descr.equals(translatedName))
            sb.append("<p>").append(StringUtils.breakLines(et.getDescription(elementAttributes))).append("</p>\n");

        try {
            PinDescriptions inputs = et.getInputDescription(elementAttributes);
            sb.append("<h4>").append(Lang.get("elem_Help_inputs")).append(":</h4>\n<dl>\n");
            if (inputs != null && inputs.size() > 0) {
                for (PinDescription i : inputs)
                    addEntry(sb, i.getName(), i.getDescription());
            }
            sb.append("</dl>\n");
        } catch (NodeException e) {
            e.printStackTrace();
        }

        PinDescriptions outputs = et.getOutputDescriptions(elementAttributes);
        sb.append("<h4>").append(Lang.get("elem_Help_outputs")).append(":</h4>\n<dl>\n");
        if (outputs != null && outputs.size() > 0) {
            for (PinDescription i : outputs)
                addEntry(sb, i.getName(), i.getDescription());
        }
        sb.append("</dl>\n");

        if (et.getAttributeList().size() > 0) {
            sb.append("<h4>").append(Lang.get("elem_Help_attributes")).append(":</h4>\n<dl>\n");
            for (Key k : et.getAttributeList())
                addEntry(sb, k.getName(), k.getDescription());
            sb.append("</dl>\n");
        }
    }

    private static void addEntry(StringBuilder sb, String name, String description) {
        if (description == null || description.length() == 0 || name.equals(description))
            sb.append("<dt><i>").append(name).append("</i></dt>\n");
        else
            sb.append("<dt><i>").append(name).append("</i></dt><dd>").append(description).append("</dd>\n");
    }

    /**
     * @return factory which catches 'image' protocol requests to deliver images via an URL.
     */
    public static URLStreamHandlerFactory createURLStreamHandlerFactory() {
        return new MyURLStreamHandlerFactory();

    }

    /**
     * Sets the shapeFactory used to create the images.
     *
     * @param shapeFactory the ShapeFactory
     */
    public static void setShapeFactory(ShapeFactory shapeFactory) {
        MyURLStreamHandlerFactory.shapeFactory = shapeFactory;
    }

    private static class MyURLStreamHandlerFactory implements URLStreamHandlerFactory {

        private static ShapeFactory shapeFactory;

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if (protocol.equals("image"))
                return new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(URL u) throws IOException {
                        return new ImageConnection(shapeFactory, u);
                    }
                };
            else
                return null;
        }
    }

    private static class ImageConnection extends URLConnection {

        private final ShapeFactory shapeFactory;

        ImageConnection(ShapeFactory shapeFactory, URL url) {
            super(url);
            this.shapeFactory = shapeFactory;
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            String path = url.getPath();
            if (path.endsWith(".png"))
                path = path.substring(0, path.length() - 4);
            BufferedImage bi = new VisualElement(path).setShapeFactory(shapeFactory).getBufferedImage(0.75, 150);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", baos);
            return new ByteArrayInputStream(baos.toByteArray());
        }
    }
}
