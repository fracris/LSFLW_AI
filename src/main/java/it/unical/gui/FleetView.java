package it.unical.gui;

import it.unical.model.Fleet;
import it.unical.utils.ResourceLoader;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class FleetView {
    private Fleet fleet;
    private static final Font FLEET_FONT = new Font("Arial", Font.BOLD, 10);
    private static final int FLEET_IMAGE_WIDTH = 30;
    private static final int FLEET_IMAGE_HEIGHT = 30;

    private static BufferedImage playerShipImage;
    private static BufferedImage aiShipImage;

    static {
        playerShipImage = ResourceLoader.loadImage("images/blue_ship.png"); // Replace with actual path
        aiShipImage = ResourceLoader.loadImage("images/red_ship.png"); // Replace with actual path
        System.out.println(playerShipImage);
        System.out.println(aiShipImage);
    }

    public FleetView(Fleet fleet) {
        this.fleet = fleet;
    }

    public void draw(Graphics2D g2d) {
        // Ottieni la posizione corrente della flotta
        Point position = fleet.getCurrentPosition();
        BufferedImage fleetImage = (!fleet.getOwner().isAI()) ? playerShipImage : aiShipImage;

        // Calcola l'angolo di rotazione in base alla direzione di movimento
        double dx = fleet.getDestination().getPosition().x - fleet.getSource().getPosition().x;
        double dy = fleet.getDestination().getPosition().y - fleet.getSource().getPosition().y;
        double angle = Math.atan2(dy, dx);

        // Salva la trasformazione corrente
        AffineTransform oldTransform = g2d.getTransform();

        // Applica la rotazione e traslazione
        g2d.translate(position.x, position.y);
        g2d.rotate(angle);

        // Disegna l'immagine della flotta con dimensioni fissate
        g2d.drawImage(fleetImage, -FLEET_IMAGE_WIDTH / 2, -FLEET_IMAGE_HEIGHT / 2,
                FLEET_IMAGE_WIDTH, FLEET_IMAGE_HEIGHT, null);

        // Ripristina la trasformazione
        g2d.setTransform(oldTransform);

        // Disegna il numero di navi
        g2d.setFont(FLEET_FONT);
        String shipsText = String.valueOf(fleet.getShips());
        int shipsWidth = g2d.getFontMetrics().stringWidth(shipsText);
        g2d.setColor(Color.WHITE);
        g2d.drawString(shipsText, position.x - shipsWidth / 2, position.y - FLEET_IMAGE_HEIGHT / 2 - 2);
    }

    // Getter
    public Fleet getFleet() {
        return fleet;
    }
}
