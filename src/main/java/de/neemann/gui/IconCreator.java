package de.neemann.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Helper to create Images and Icons
 * Created by hneemann on 11.02.14.
 */
public final class IconCreator {

    private IconCreator() {
    }

    /**
     * Creates an icon from a resource
     *
     * @param name name of the resource
     * @return the icon
     */
    public static Icon create(String name) {
        return new ImageIcon(createImage(name));
    }

    /**
     * Creates an image from a resource
     *
     * @param name name of the resource
     * @return the image
     */
    public static Image createImage(String name) {
        try {
            URL systemResource = ClassLoader.getSystemResource(name);
            if (systemResource == null) {
                throw new NullPointerException("recource " + name + " not found!");
            }
            return ImageIO.read(systemResource);
        } catch (IOException e) {
            throw new RuntimeException("Image " + name + " not found");
        }
    }

    /**
     * Creates an image list from a resource
     *
     * @param names names of the resource
     * @return the image
     */
    public static ArrayList<Image> createImages(String... names) {
        ArrayList<Image> list = new ArrayList<Image>(names.length);
        for (String name : names) {
            list.add(createImage(name));
        }
        return list;
    }

}
