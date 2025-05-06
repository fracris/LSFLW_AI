package it.unical.gui;

import it.unical.controller.GameController;
import it.unical.model.Fleet;
import it.unical.model.GameMap;
import it.unical.model.StarSystem;

import javax.swing.*;
import java.awt.*;
import java.util.List; // ✅ GIUSTO
import java.awt.geom.Line2D;

public class GamePanel extends JPanel {
    private GameController gameController;
    private StarSystemView[] systemViews;
    private FleetView[] fleetViews;

    // Variabili per gestire lo zoom e il pan
    private double scale = 1.0;
    private Point viewPosition = new Point(0, 0);

    // Variabili per la selezione
    private StarSystem selectedSystem;
    private StarSystem targetSystem;

    public GamePanel(GameController gameController) {
        this.gameController = gameController;
        setBackground(Color.BLACK);

        // Non chiamiamo updateSystemViews() qui perché la mappa potrebbe non essere ancora inizializzata
        systemViews = new StarSystemView[0]; // Inizializza come array vuoto
    }


    // Aggiorna le viste dei sistemi stellari
    public void updateSystemViews() {
        // Controllo inizializzazione
        if (gameController == null) {
            System.err.println("[ERRORE] GameController non inizializzato.");
            return;
        }

        if (gameController.getGameState() == null) {
            System.err.println("[ERRORE] GameState non inizializzato.");
            return;
        }

        GameMap gameMap = gameController.getGameState().getGameMap();
        if (gameMap == null) {
            System.err.println("[ERRORE] GameMap non inizializzata.");
            return;
        }

        List<StarSystem> systems = gameMap.getSystems();
        if (systems == null || systems.isEmpty()) {
            System.err.println("[ERRORE] Nessun sistema stellare presente nella mappa.");
            return;
        }

        // Inizializza array di viste
        systemViews = new StarSystemView[systems.size()];

        // Crea una vista per ciascun sistema
        for (int i = 0; i < systems.size(); i++) {
            StarSystem system = systems.get(i);
            systemViews[i] = new StarSystemView(system);

            // Log di debug
            System.out.printf("Sistema %d: %s (ID=%d) - Posizione: (%d, %d), Navi: %d, Proprietario: %s%n",
                    i, system.getName(), system.getId(),
                    system.getPosition().x, system.getPosition().y,
                    system.getShips(),
                    system.getOwner() != null ? system.getOwner().getName() : "Nessuno");
        }

        // Forza il repaint per mostrare tutto
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Debugging
        if (systemViews == null || systemViews.length == 0) {
            g2d.setColor(Color.RED);
            g2d.drawString("Nessun sistema da visualizzare!", 50, 50);
            return;
        }

        // Abilita l'anti-aliasing per una grafica più liscia
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Applica zoom e pan
        g2d.translate(viewPosition.x, viewPosition.y);
        g2d.scale(scale, scale);

        // Disegna lo sfondo dello spazio (stelle casuali)
        drawBackground(g2d);

        // Disegna le connessioni tra i sistemi
        drawConnections(g2d);

        // Disegna i sistemi stellari
        drawSystems(g2d);

        // Disegna le flotte
        drawFleets(g2d);

        // Disegna la selezione attuale (se presente)
        drawSelection(g2d);
    }

    // Disegna lo sfondo con stelle casuali
    private void drawBackground(Graphics2D g2d) {
        // Implementazione semplice: stelle casuali
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 200; i++) {
            int x = (int) (Math.random() * getWidth() / scale);
            int y = (int) (Math.random() * getHeight() / scale);
            int size = (int) (Math.random() * 2) + 1;
            g2d.fillOval(x, y, size, size);
        }
    }

    // Disegna le connessioni tra i sistemi
    private void drawConnections(Graphics2D g2d) {
        if (gameController == null || gameController.getGameState() == null ||
                gameController.getGameState().getGameMap() == null) {
            return;
        }

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
        if (systemViews == null) return;

        for (StarSystemView systemView : systemViews) {
            if (systemView != null) {
                systemView.draw(g2d);
            }
        }
    }

    // Disegna le flotte
    private void drawFleets(Graphics2D g2d) {
        if (gameController == null || gameController.getGameState() == null ||
                gameController.getGameState().getGameMap() == null) {
            return;
        }

        GameMap gameMap = gameController.getGameState().getGameMap();

        for (Fleet fleet : gameMap.getFleets()) {
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
}