package it.unical.gui;

import it.unical.model.StarSystem;

import javax.imageio.ImageIO;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.image.BufferedImage;

public class StarSystemView {
    private StarSystem system;
    private BufferedImage image;
    private BufferedImage selectedOverlay;

    public StarSystemView(StarSystem system) {
        this.system = system;

        // Carica le immagini appropriate
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/images/star_system.png"));
            selectedOverlay = ImageIO.read(getClass().getResourceAsStream("/images/selected.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void draw(Graphics2D g2d, boolean isSelected) {
        Position pos = system.getPosition();
        int x = pos.getX();
        int y = pos.getY();

        // Disegna l'immagine del sistema
        g2d.drawImage(image, x - image.getWidth()/2, y - image.getHeight()/2, null);

        // Se selezionato, disegna l'overlay
        if (isSelected) {
            g2d.drawImage(selectedOverlay, x - selectedOverlay.getWidth()/2,
                    y - selectedOverlay.getHeight()/2, null);
        }

        // Disegna il nome del sistema
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(system.getName(), x - g2d.getFontMetrics().stringWidth(system.getName())/2,
                y + image.getHeight()/2 + 15);

        // Disegna il numero di navi
        g2d.setColor(system.getOwner() != null ? system.getOwner().getColor() : Color.GRAY);
        g2d.drawString(String.valueOf(system.getShips()), x, y);
    }

    public boolean contains(int mouseX, int mouseY) {
        Position pos = system.getPosition();
        int distance = (int) Math.sqrt(Math.pow(mouseX - pos.getX(), 2) +
                Math.pow(mouseY - pos.getY(), 2));
        return distance < image.getWidth() / 2;
    }
}
