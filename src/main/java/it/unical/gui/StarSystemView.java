package it.unical.gui;

import it.unical.model.StarSystem;
import it.unical.utils.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;

public class StarSystemView {
    private final StarSystem system;
    private static final int SYSTEM_RADIUS = 30;
    private static final Font SYSTEM_FONT = new Font("Arial", Font.BOLD, 12);
    private static final Font SHIPS_FONT = new Font("Arial", Font.BOLD, 10);

    private static final Image neutralStarImage;
    private static final ImageObserver neutralStarObserver;
    private static final Image playerStarImage;
    private static final ImageObserver playerStarObserver;
    private static final Image ai1StarImage;
    private static final ImageObserver ai1StarObserver;
    private static final Image ai2StarImage;
    private static final ImageObserver ai2StarObserver;
    private static final Image ai3StarImage;
    private static final ImageObserver ai3StarObserver;



    static {
        ImageIcon neutralStarIcon = new ImageIcon(ResourceLoader.class.getClassLoader().getResource("images/white_star.gif"));
        neutralStarImage = neutralStarIcon.getImage();
        neutralStarObserver = neutralStarIcon.getImageObserver();
        ImageIcon playerStarIcon = new ImageIcon(ResourceLoader.class.getClassLoader().getResource("images/blue_star.gif"));
        playerStarImage = playerStarIcon.getImage();
        playerStarObserver = playerStarIcon.getImageObserver();
        ImageIcon ai1StarIcon = new ImageIcon(ResourceLoader.class.getClassLoader().getResource("images/red_star.gif"));
        ai1StarImage = ai1StarIcon.getImage();
        ai1StarObserver = ai1StarIcon.getImageObserver();
        ImageIcon ai2StarIcon = new ImageIcon(ResourceLoader.class.getClassLoader().getResource("images/green_star.gif"));
        ai2StarImage = ai2StarIcon.getImage();
        ai2StarObserver = ai2StarIcon.getImageObserver();
        ImageIcon ai3StarIcon = new ImageIcon(ResourceLoader.class.getClassLoader().getResource("images/yellow_star.gif"));
        ai3StarImage = ai3StarIcon.getImage();
        ai3StarObserver = ai3StarIcon.getImageObserver();

        FleetView.setPlayerShipImage(ResourceLoader.loadImage("images/blue_ship.png"));
        FleetView.setAi1ShipImage(ResourceLoader.loadImage("images/red_ship.png"));
        FleetView.setAi2ShipImage(ResourceLoader.loadImage("images/green_ship.png"));
        FleetView.setAi3ShipImage(ResourceLoader.loadImage("images/yellow_ship.png"));
    }

    public StarSystemView(StarSystem system) {
        this.system = system;
    }

    public void draw(Graphics2D g2d) {
        Point position = system.getPosition();
        Image starImage;
        ImageObserver starObserver;

        if (system.getOwner() == null) {
            starImage = neutralStarImage;
            starObserver = neutralStarObserver;
        } else if (system.getOwner().isAI()) {
            if(system.getOwner().getColor()==Color.RED){
                starImage = ai1StarImage;
                starObserver = ai1StarObserver;
            }
            else if(system.getOwner().getColor()==Color.GREEN) {
                starImage = ai2StarImage;
                starObserver = ai2StarObserver;
            }
            else {
                starImage = ai3StarImage;
                starObserver = ai3StarObserver;
            }
        } else {
            starImage = playerStarImage;
            starObserver = playerStarObserver;
        }

        g2d.drawImage(starImage, position.x - SYSTEM_RADIUS, position.y - SYSTEM_RADIUS,
                SYSTEM_RADIUS * 2, SYSTEM_RADIUS * 2, starObserver);
        
        if(system.isAutomated()) {
            double dx = system.getAutomatedTo().getPosition().x - system.getPosition().x;
            double dy = system.getAutomatedTo().getPosition().y - system.getPosition().y;
            double angle = Math.atan2(dy, dx);

            AffineTransform oldTransform = g2d.getTransform();
            g2d.translate(position.x, position.y);
            g2d.rotate(angle);
            int tri = 15;
            int[] xP = { SYSTEM_RADIUS + tri - 15, SYSTEM_RADIUS - 15, SYSTEM_RADIUS - 15};
            int[] yP = { 0, -tri/2, tri/2};
            g2d.setColor(Color.BLUE);
            g2d.fillPolygon(xP, yP, 3);

            g2d.setTransform(oldTransform);
        }

        g2d.setFont(SYSTEM_FONT);
        int nameWidth = g2d.getFontMetrics().stringWidth(system.getName());
        g2d.setColor(system.getOwner()==null ? Color.WHITE : system.getOwner().getColor());
        g2d.drawString(system.getName(), position.x - nameWidth / 2, position.y - SYSTEM_RADIUS + 10);

        if (system.getOwner() != null) {
            g2d.setFont(SHIPS_FONT);
            String shipsText = String.valueOf(system.getShips());
            int shipsWidth = g2d.getFontMetrics().stringWidth(shipsText);
            g2d.setColor(Color.WHITE);
            g2d.drawString(shipsText, position.x - shipsWidth / 2, position.y + 5);
        }

        g2d.setFont(SYSTEM_FONT);
        String prodText = "+" + system.getProductionRate();
        int prodWidth = g2d.getFontMetrics().stringWidth(prodText);
        g2d.setColor(Color.YELLOW);
        g2d.drawString(prodText, position.x - prodWidth / 2, position.y + SYSTEM_RADIUS);
    }

    public boolean contains(Point point) {
        Point position = system.getPosition();
        double distance = position.distance(point);
        return distance <= SYSTEM_RADIUS;
    }

    public StarSystem getSystem() {
        return system;
    }
}