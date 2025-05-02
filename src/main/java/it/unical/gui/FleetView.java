package it.unical.gui;

import it.unical.model.Fleet;

import javax.imageio.ImageIO;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.image.BufferedImage;

public class FleetView {
    private Fleet fleet;
    private BufferedImage image;
    //private Animation moveAnimation;

    public FleetView(Fleet fleet) {
        this.fleet = fleet;

        // Carica le immagini appropriate
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/images/fleet.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Inizializza l'animazione
        initAnimation();
    }

    private void initAnimation() {
        Position start = fleet.getSource().getPosition();
        Position end = fleet.getDestination().getPosition();
        //moveAnimation = new Animation(start, end, fleet.getTurnsToArrival() * 60); // 60 frame per turno
    }

    public void update() {
       // moveAnimation.update();
    }

//    public void draw(Graphics2D g2d) {
//        Position currentPos = moveAnimation.getCurrentPosition();
//        int x = currentPos.getX();
//        int y = currentPos.getY();
//
//        // Disegna l'immagine della flotta
//        g2d.drawImage(image, x - image.getWidth()/2, y - image.getHeight()/2, null);
//
//        // Disegna il numero di navi
//        g2d.setColor(fleet.getOwner().getColor());
//        g2d.setFont(new Font("Arial", Font.BOLD, 10));
//        g2d.drawString(String.valueOf(fleet.getShips()), x, y - 10);
//    }
}