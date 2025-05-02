package it.unical.gui;

import it.unical.controller.GameController;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel {
    private GameController controller;
    private BufferedImage background;
    private int offsetX, offsetY;
    private float zoomLevel;

    public GamePanel(GameController controller) {
        this.controller = controller;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);

        // Inizializza il background e lo zoom
        try {
            background = ImageIO.read(getClass().getResourceAsStream("/images/space_background.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        zoomLevel = 1.0f;

        // Gestione degli eventi del mouse
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });

        // Gestione dello scrolling per la mappa
        addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) {
                zoomLevel *= 1.1f;
            } else {
                zoomLevel *= 0.9f;
            }
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Applica trasformazioni per zoom e panning
        g2d.translate(offsetX, offsetY);
        g2d.scale(zoomLevel, zoomLevel);

        // Disegna lo sfondo
        drawBackground(g2d);

        // Disegna i sistemi e le connessioni
        drawConnections(g2d);
        drawSystems(g2d);

        // Disegna le flotte
        drawFleets(g2d);

        // Disegna elementi UI sovrapposti
        drawOverlayUI(g2d);
    }

    private void drawBackground(Graphics2D g2d) {  }
    private void drawConnections(Graphics2D g2d) {  }
    private void drawSystems(Graphics2D g2d) {  }
    private void drawFleets(Graphics2D g2d) {  }
    private void drawOverlayUI(Graphics2D g2d) {  }

    private void handleClick(int x, int y) {
        // Trasforma le coordinate in base a zoom e offset
        int gameX = (int)((x - offsetX) / zoomLevel);
        int gameY = (int)((y - offsetY) / zoomLevel);

        // Informa il controller del click
        controller.handleMapClick(gameX, gameY);
    }
}
