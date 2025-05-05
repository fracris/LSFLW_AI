package it.unical.gui;

import it.unical.model.StarSystem;

import java.awt.*;

public class StarSystemView {
    private StarSystem system;
    private static final int SYSTEM_RADIUS = 20;
    private static final Font SYSTEM_FONT = new Font("Arial", Font.BOLD, 12);
    private static final Font SHIPS_FONT = new Font("Arial", Font.PLAIN, 10);

    public StarSystemView(StarSystem system) {
        this.system = system;
    }

    public void draw(Graphics2D g2d) {
        Point position = system.getPosition();
        Color systemColor = system.getColor();

        // Disegna l'alone attorno al sistema (effetto glow)
        g2d.setColor(new Color(systemColor.getRed(), systemColor.getGreen(), systemColor.getBlue(), 50));
        g2d.fillOval(position.x - SYSTEM_RADIUS - 5, position.y - SYSTEM_RADIUS - 5,
                (SYSTEM_RADIUS + 5) * 2, (SYSTEM_RADIUS + 5) * 2);

        // Disegna il sistema
        g2d.setColor(systemColor);
        g2d.fillOval(position.x - SYSTEM_RADIUS, position.y - SYSTEM_RADIUS,
                SYSTEM_RADIUS * 2, SYSTEM_RADIUS * 2);

        // Disegna il bordo
        g2d.setColor(Color.WHITE);
        g2d.drawOval(position.x - SYSTEM_RADIUS, position.y - SYSTEM_RADIUS,
                SYSTEM_RADIUS * 2, SYSTEM_RADIUS * 2);

        // Disegna il nome del sistema
        g2d.setFont(SYSTEM_FONT);
        int nameWidth = g2d.getFontMetrics().stringWidth(system.getName());
        g2d.drawString(system.getName(), position.x - nameWidth/2, position.y - SYSTEM_RADIUS - 5);

        // Disegna il numero di navi
        g2d.setFont(SHIPS_FONT);
        String shipsText = String.valueOf(system.getShips());
        int shipsWidth = g2d.getFontMetrics().stringWidth(shipsText);
        g2d.setColor(Color.WHITE);
        g2d.drawString(shipsText, position.x - shipsWidth/2, position.y + 5);

        // Disegna il tasso di produzione
        String prodText = "+" + system.getProductionRate();
        int prodWidth = g2d.getFontMetrics().stringWidth(prodText);
        g2d.setColor(Color.YELLOW);
        g2d.drawString(prodText, position.x - prodWidth/2, position.y + SYSTEM_RADIUS + 15);
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