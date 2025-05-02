package it.unical.utils;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ResourceLoader {
    public static BufferedImage loadImage(String path) {
        try {
            InputStream is = ResourceLoader.class.getClassLoader().getResourceAsStream(path);
            if (is != null) {
                return ImageIO.read(is);
            } else {
                System.err.println("Resource not found: " + path);
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error loading image: " + path);
            e.printStackTrace();
            return null;
        }
    }
}
