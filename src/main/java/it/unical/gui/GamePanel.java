package it.unical.gui;

import it.unical.controller.GameController;
import it.unical.model.Fleet;
import it.unical.model.GameMap;
import it.unical.model.StarSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class GamePanel extends JPanel {
    private GameController gameController;
    private StarSystemView[] systemViews;
    private FleetView[] fleetViews;
    private int starCountRate = 0;

    // Variabili per gestire lo zoom e il pan
    private double scale = 1.0;
    private Point viewPosition = new Point(0, 0);

    // Variabili per la selezione
    private StarSystem selectedSystem;
    private StarSystem targetSystem;

    // Variabili per gestire le stelle inizializzate una sola volta
    private Point[] starPositions;
    private int[] starSizes;

    public GamePanel(GameController gameController) {
        this.gameController = gameController;
        setBackground(Color.BLACK);

        // Inizializza le viste dei sistemi e delle flotte
        updateSystemViews();
        initializeStars(); // Inizializza le posizioni delle stelle
    }

    // Metodo per inizializzare le stelle una sola volta
    private void initializeStars() {
        int starCount = 200;
        starPositions = new Point[starCount];
        starSizes = new int[starCount];

        GameMap gameMap = gameController.getGameState().getGameMap();
        int mapWidth = (int) gameMap.getMapSize().getWidth();
        int mapHeight = (int) gameMap.getMapSize().getHeight();

        for (int i = 0; i < starCount; i++) {
            int x = (int) (Math.random() * mapWidth);
            int y = (int) (Math.random() * mapHeight);
            int size = (int) (Math.random() * 10) + 1;
            starPositions[i] = new Point(x, y);
            starSizes[i] = size;
        }
    }



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Abilita l'anti-aliasing per una grafica più liscia
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Applica zoom e pan
        g2d.translate(viewPosition.x, viewPosition.y);
        g2d.scale(scale, scale);



        // Disegna le connessioni tra i sistemi
        drawConnections(g2d);

        // Disegna i sistemi stellari
        drawSystems(g2d);

        // Disegna le flotte
        drawFleets(g2d);

        // Disegna la selezione attuale (se presente)
        drawSelection(g2d);
        starCountRate++;
    }

    // Aggiorna le viste dei sistemi stellari
    public void updateSystemViews() {
        GameMap gameMap = gameController.getGameState().getGameMap();

        // Crea le viste per i sistemi
        systemViews = new StarSystemView[gameMap.getSystems().size()];
        for (int i = 0; i < gameMap.getSystems().size(); i++) {
            StarSystem system = gameMap.getSystems().get(i);
            systemViews[i] = new StarSystemView(system);
        }

        // Le viste delle flotte sono dinamiche e si aggiornano nel paint
    }

    // Disegna le connessioni tra i sistemi
    private void drawConnections(Graphics2D g2d) {
        GameMap gameMap = gameController.getGameState().getGameMap();

        g2d.setStroke(new BasicStroke(1.0f));
        g2d.setColor(new Color(80, 80, 100));

        for (StarSystem system : gameMap.getSystems()) {
            Point p1 = system.getPosition();

            for (StarSystem connected : system.getConnectedSystems()) {
                Point p2 = connected.getPosition();

                // Evita di disegnare due volte la stessa connessione
                if (system.getId() < connected.getId()) {
                    g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
                }
            }
        }
    }

    // Disegna i sistemi stellari
    private void drawSystems(Graphics2D g2d) {
        for (StarSystemView systemView : systemViews) {
            systemView.draw(g2d);
        }
    }

    // Disegna le flotte
    private void drawFleets(Graphics2D g2d) {
        GameMap gameMap = gameController.getGameState().getGameMap();

        // Crea una copia della lista di flotte per evitare modifiche concorrenti
        ArrayList<Fleet> fleetListCopy = new ArrayList<>(gameMap.getFleets());

        for (Fleet fleet : fleetListCopy) {
            FleetView fleetView = new FleetView(fleet);
            fleetView.draw(g2d);
        }
    }

    // Disegna la selezione attuale e l'evidenziazione per l'invio delle flotte
    private void drawSelection(Graphics2D g2d) {
        if (selectedSystem != null) {
            // Disegna un cerchio attorno al sistema selezionato
            Point p = selectedSystem.getPosition();
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(p.x - 25, p.y - 25, 50, 50);

            // Evidenzia i sistemi raggiungibili
            g2d.setColor(new Color(150, 150, 255, 100));
            for (StarSystem connected : selectedSystem.getConnectedSystems()) {
                Point p2 = connected.getPosition();
                g2d.fillOval(p2.x - 22, p2.y - 22, 44, 44);
            }
        }

        if (targetSystem != null && selectedSystem != null) {
            // Disegna una linea dal sistema selezionato al sistema target
            Point p1 = selectedSystem.getPosition();
            Point p2 = targetSystem.getPosition();

            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8}, 0));
            g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
        }
    }

    // Metodi per gestire selezione e target
    public void setSelectedSystem(StarSystem system) {
        this.selectedSystem = system;
        this.targetSystem = null;
        repaint();
    }

    public void setTargetSystem(StarSystem system) {
        if (selectedSystem != null && selectedSystem.getConnectedSystems().contains(system)) {
            this.targetSystem = system;
            repaint();
        }
    }

    public StarSystem getSelectedSystem() {
        return selectedSystem;
    }

    public StarSystem getTargetSystem() {
        return targetSystem;
    }

    // Converte le coordinate del mouse alle coordinate della mappa
    public Point screenToMap(Point screenPoint) {
        int mapX = (int) ((screenPoint.x - viewPosition.x) / scale);
        int mapY = (int) ((screenPoint.y - viewPosition.y) / scale);
        return new Point(mapX, mapY);
    }

    // Trova un sistema stellare alle coordinate date
    public StarSystem findSystemAt(Point point) {
        for (StarSystemView systemView : systemViews) {
            if (systemView.contains(point)) {
                return systemView.getSystem();
            }
        }
        return null;
    }

    public void panMap(int deltaX, int deltaY) {
        viewPosition.translate(deltaX, deltaY);
        repaint();
    }

    public void zoomMap(double v, Point mousePosition) {
        Point mapMousePointBeforeZoom = screenToMap(mousePosition);

        scale *= v;
        scale = Math.max(0.01, Math.min(scale, 50.0));

        Point mapMousePointAfterZoom = screenToMap(mousePosition);
        viewPosition.translate(
                (int) ((mapMousePointAfterZoom.x-mapMousePointBeforeZoom.x) * scale),
                (int) ((mapMousePointAfterZoom.y-mapMousePointBeforeZoom.y) * scale)
        );

        repaint();
    }
}