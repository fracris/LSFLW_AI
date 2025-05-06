package it.unical.gui;

import it.unical.model.Fleet;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class FleetView {
    private Fleet fleet;
    private static final int FLEET_SIZE = 10;
    private static final Font FLEET_FONT = new Font("Arial", Font.BOLD, 10);

    public FleetView(Fleet fleet) {
        this.fleet = fleet;
    }

    public void draw(Graphics2D g2d) {
        // Ottieni la posizione corrente della flotta
        Point position = fleet.getCurrentPosition();
        Color fleetColor = fleet.getColor();

        // Calcola l'angolo di rotazione in base alla direzione di movimento
        double dx = fleet.getDestination().getPosition().x - fleet.getSource().getPosition().x;
        double dy = fleet.getDestination().getPosition().y - fleet.getSource().getPosition().y;
        double angle = Math.atan2(dy, dx);

        // Salva la trasformazione corrente
        AffineTransform oldTransform = g2d.getTransform();

        // Applica la rotazione
        g2d.translate(position.x, position.y);
        g2d.rotate(angle);

        // Disegna la navicella (triangolo)
        g2d.setColor(fleetColor);
        int[] xPoints = {FLEET_SIZE, -FLEET_SIZE/2, -FLEET_SIZE/2};
        int[] yPoints = {0, -FLEET_SIZE/2, FLEET_SIZE/2};
        g2d.fillPolygon(xPoints, yPoints, 3);

        // Disegna il bordo
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(xPoints, yPoints, 3);

        // Ripristina la trasformazione
        g2d.setTransform(oldTransform);

        // Disegna il numero di navi
        g2d.setFont(FLEET_FONT);
        String shipsText = String.valueOf(fleet.getShips());
        int shipsWidth = g2d.getFontMetrics().stringWidth(shipsText);
        g2d.setColor(Color.WHITE);
        g2d.drawString(shipsText, position.x - shipsWidth/2, position.y - FLEET_SIZE - 2);
    }

    // Getter
    public Fleet getFleet() {
        return fleet;
    }
}
