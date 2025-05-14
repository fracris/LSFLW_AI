package it.unical.gui;

import it.unical.model.Fleet;
import it.unical.utils.ResourceLoader;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class FleetView {
    private Fleet fleet;
    private static final Font FLEET_FONT = new Font("Arial", Font.BOLD, 10);
    private static final int FLEET_IMAGE_SIZE = 20;

    private static BufferedImage playerShipImage;
    private static BufferedImage ai1ShipImage;
    private static BufferedImage ai2ShipImage;
    private static BufferedImage ai3ShipImage;

    public static void setPlayerShipImage(BufferedImage image) {
        playerShipImage=image;
    }

    public static void setAi1ShipImage(BufferedImage image) {
        ai1ShipImage=image;
    }

    public static void setAi2ShipImage(BufferedImage image) {
        ai2ShipImage=image;
    }

    public static void setAi3ShipImage(BufferedImage image) {
        ai3ShipImage=image;
    }

    public FleetView(Fleet fleet) {
        this.fleet = fleet;
    }

    public void draw(Graphics2D g2d) {
        // Ottieni la posizione corrente della flotta
        Point position = fleet.getCurrentPosition();
        BufferedImage fleetImage = (!fleet.getOwner().isAI()) ? playerShipImage : fleet.getOwner().getColor() == Color.RED ? ai1ShipImage : fleet.getOwner().getColor() == Color.GREEN ? ai2ShipImage : ai3ShipImage;

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
        g2d.drawImage(fleetImage, -FLEET_IMAGE_SIZE / 2, -FLEET_IMAGE_SIZE / 2,
                FLEET_IMAGE_SIZE, FLEET_IMAGE_SIZE, null);

        // Ripristina la trasformazione
        g2d.setTransform(oldTransform);

        // Disegna il numero di navi
        g2d.setFont(FLEET_FONT);
        String shipsText = String.valueOf(fleet.getShips());
        int shipsWidth = g2d.getFontMetrics().stringWidth(shipsText);
        g2d.setColor(Color.WHITE);
        g2d.drawString(shipsText, position.x - shipsWidth / 2, position.y - FLEET_IMAGE_SIZE / 2 - 2);
    }

    // Getter
    public Fleet getFleet() {
        return fleet;
    }
}
