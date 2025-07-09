package it.unical.gui;

import it.unical.controller.GameController;
import it.unical.model.Fleet;
import it.unical.model.GameMap;
import it.unical.model.StarSystem;
import it.unical.utils.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.function.Consumer;

public class GamePanel extends JPanel {
    private final GameController gameController;
    private StarSystemView[] systemViews;
    private FleetView[] fleetViews;
    private int starCountRate = 0;

    private double scale = 1.0;
    private final Point viewPosition = new Point(0, 0);

    private StarSystem selectedSystem;
    private StarSystem targetSystem;

    private Point[] starPositions;
    private int[] starSizes;

    private static final Image backgroundImage;
    private static final ImageObserver backgroundStarObserver;

    static {
        ImageIcon backgroundStarIcon = new ImageIcon(ResourceLoader.class.getClassLoader().getResource("images/background.gif"));
        backgroundImage = backgroundStarIcon.getImage();
        backgroundStarObserver = backgroundStarIcon.getImageObserver();
    }

    public GamePanel(GameController gameController) {
        this.gameController = gameController;
        setBackground(Color.BLACK);


        bindKeyStroke("P", "togglePause", e -> gameController.showPauseDialog());



        updateSystemViews();
        initializeStars();
    }

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

    private void drawBackground(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();
        g2d.setTransform(new AffineTransform());
        g2d.drawImage(backgroundImage, 0, 0, backgroundStarObserver);
        g2d.setTransform(oldTransform);
    }


    private void bindKeyStroke(String key, String actionName, Consumer<ActionEvent> handler) {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(key), actionName);
        am.put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { handler.accept(e); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.translate(viewPosition.x, viewPosition.y);
        g2d.scale(scale, scale);


        drawConnections(g2d);

        drawSystems(g2d);

        drawFleets(g2d);

        drawSelection(g2d);
        starCountRate++;
    }

    public void updateSystemViews() {
        GameMap gameMap = gameController.getGameState().getGameMap();

        systemViews = new StarSystemView[gameMap.getSystems().size()];
        for (int i = 0; i < gameMap.getSystems().size(); i++) {
            StarSystem system = gameMap.getSystems().get(i);
            systemViews[i] = new StarSystemView(system);
        }
    }

    private void drawConnections(Graphics2D g2d) {
        GameMap gameMap = gameController.getGameState().getGameMap();

        g2d.setStroke(new BasicStroke(3.0f));
        g2d.setColor(new Color(80, 80, 100));

        for (StarSystem system : gameMap.getSystems()) {
            Point p1 = system.getPosition();

            for (StarSystem connected : system.getConnectedSystems()) {
                Point p2 = connected.getPosition();

                if (system.getId() < connected.getId()) {
                    g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
                }
            }
        }
    }

    private void drawSystems(Graphics2D g2d) {
        for (StarSystemView systemView : systemViews) {
            systemView.draw(g2d);
        }
    }

    private void drawFleets(Graphics2D g2d) {
        GameMap gameMap = gameController.getGameState().getGameMap();

        ArrayList<Fleet> fleetListCopy = new ArrayList<>(gameMap.getFleets());

        for (Fleet fleet : fleetListCopy) {
            FleetView fleetView = new FleetView(fleet);
            fleetView.draw(g2d);
        }
    }

    private void drawSelection(Graphics2D g2d) {
        if (selectedSystem != null) {
            Point p = selectedSystem.getPosition();
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(p.x - 25, p.y - 25, 50, 50);

            g2d.setColor(new Color(150, 150, 255, 100));
            for (StarSystem connected : selectedSystem.getConnectedSystems()) {
                Point p2 = connected.getPosition();
                g2d.fillOval(p2.x - 22, p2.y - 22, 44, 44);
            }
        }

        if (targetSystem != null && selectedSystem != null) {
            Point p1 = selectedSystem.getPosition();
            Point p2 = targetSystem.getPosition();

            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8}, 0));
            g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
        }
    }

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

    public Point screenToMap(Point screenPoint) {
        int mapX = (int) ((screenPoint.x - viewPosition.x) / scale);
        int mapY = (int) ((screenPoint.y - viewPosition.y) / scale);
        return new Point(mapX, mapY);
    }

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
        scale = Math.max(1, Math.min(scale, 50.0));

        Point mapMousePointAfterZoom = screenToMap(mousePosition);
        viewPosition.translate(
                (int) ((mapMousePointAfterZoom.x-mapMousePointBeforeZoom.x) * scale),
                (int) ((mapMousePointAfterZoom.y-mapMousePointBeforeZoom.y) * scale)
        );

        repaint();
    }
}