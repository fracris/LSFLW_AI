package it.unical.gui;

import it.unical.model.StarSystem;
import it.unical.utils.ResourceLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class StarSystemView {
    private StarSystem system;
    private static final int SYSTEM_RADIUS = 20;
    private static final Font SYSTEM_FONT = new Font("Arial", Font.BOLD, 12);
    private static final Font SHIPS_FONT = new Font("Arial", Font.PLAIN, 10);

    private static BufferedImage neutralStarImage;
    private static BufferedImage playerStarImage;
    private static BufferedImage aiStarImage;

    static {
        neutralStarImage = ResourceLoader.loadImage("images/star.png"); // Replace with actual path
        playerStarImage = ResourceLoader.loadImage("images/blue_star.png"); // Replace with actual path
        aiStarImage = ResourceLoader.loadImage("images/red_star.png"); // Replace with actual path
    }

    public StarSystemView(StarSystem system) {
        this.system = system;
    }

    public void draw(Graphics2D g2d) {
        Point position = system.getPosition();
        BufferedImage starImage;

        // Select image based on system owner
        if (system.getOwner() == null) {
            starImage = neutralStarImage;
        } else if (system.getOwner().isAI()) {
            starImage = aiStarImage;
        } else {
            starImage = playerStarImage;
        }

        // Draw the star system image
        g2d.drawImage(starImage, position.x - SYSTEM_RADIUS, position.y - SYSTEM_RADIUS,
                SYSTEM_RADIUS * 2, SYSTEM_RADIUS * 2, null);

        // Draw the name of the system
        g2d.setFont(SYSTEM_FONT);
        int nameWidth = g2d.getFontMetrics().stringWidth(system.getName());
        g2d.drawString(system.getName(), position.x - nameWidth / 2, position.y - SYSTEM_RADIUS - 5);

        // Draw the number of ships
        if (system.getOwner() != null) {
            g2d.setFont(SHIPS_FONT);
            String shipsText = String.valueOf(system.getShips());
            int shipsWidth = g2d.getFontMetrics().stringWidth(shipsText);
            g2d.setColor(Color.WHITE);
            g2d.drawString(shipsText, position.x - shipsWidth / 2, position.y + 5);
        }

        // Draw the production rate
        String prodText = "+" + system.getProductionRate();
        int prodWidth = g2d.getFontMetrics().stringWidth(prodText);
        g2d.setColor(Color.YELLOW);
        g2d.drawString(prodText, position.x - prodWidth / 2, position.y + SYSTEM_RADIUS + 15);
    }

    // Controlla se il punto dato è all'interno del sistema
    public boolean contains(Point point) {
        Point position = system.getPosition();
        double distance = position.distance(point);
        return distance <= SYSTEM_RADIUS;
    }

    // Getter
    public StarSystem getSystem() {
        return system;
    }
}