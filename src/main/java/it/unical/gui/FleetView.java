package it.unical.gui;

import it.unical.model.Fleet;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class FleetView {
    private final Fleet fleet;
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

        Point position = fleet.getCurrentPosition();
        BufferedImage fleetImage = (!fleet.getOwner().isAI()) ? playerShipImage : fleet.getOwner().getColor() == Color.RED ? ai1ShipImage : fleet.getOwner().getColor() == Color.GREEN ? ai2ShipImage : ai3ShipImage;

        double dx = fleet.getDestination().getPosition().x - fleet.getSource().getPosition().x;
        double dy = fleet.getDestination().getPosition().y - fleet.getSource().getPosition().y;
        double angle = Math.atan2(dy, dx);

        AffineTransform oldTransform = g2d.getTransform();

        g2d.translate(position.x, position.y);
        g2d.rotate(angle);

        g2d.drawImage(fleetImage, -FLEET_IMAGE_SIZE / 2, -FLEET_IMAGE_SIZE / 2,
                FLEET_IMAGE_SIZE, FLEET_IMAGE_SIZE, null);

        g2d.setTransform(oldTransform);

        g2d.setFont(FLEET_FONT);
        String shipsText = String.valueOf(fleet.getShips());
        int shipsWidth = g2d.getFontMetrics().stringWidth(shipsText);
        g2d.setColor(Color.WHITE);
        g2d.drawString(shipsText, position.x - shipsWidth / 2, position.y - FLEET_IMAGE_SIZE / 2 - 2);
    }

}
