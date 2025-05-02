package it.unical.gui;

import it.unical.controller.GameController;
import it.unical.model.GameState;
import it.unical.model.Player;

import javax.swing.*;
import java.awt.*;

public class StatusPanel extends JPanel {
    private GameController controller;
    private JLabel turnLabel;
    private JLabel playerLabel;
    private JPanel playersStatsPanel;

    public StatusPanel(GameController controller) {
        this.controller = controller;
        setPreferredSize(new Dimension(800, 60));
        setBackground(new Color(40, 40, 60));
        setLayout(new BorderLayout());

        // Inizializza i componenti
        turnLabel = new JLabel("Turno: 1");
        turnLabel.setForeground(Color.WHITE);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 16));

        playerLabel = new JLabel("Giocatore attuale: Player 1");
        playerLabel.setForeground(Color.WHITE);
        playerLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Pannello per statistiche giocatori
        playersStatsPanel = new JPanel();
        playersStatsPanel.setOpaque(false);
        playersStatsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        // Aggiunge i componenti al pannello
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        leftPanel.add(turnLabel);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setOpaque(false);
        centerPanel.add(playerLabel);

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(playersStatsPanel, BorderLayout.EAST);
    }

    public void update() {
        GameState state = controller.getGameState();
        turnLabel.setText("Turno: " + state.getTurn());
        playerLabel.setText("Giocatore attuale: " + state.getCurrentPlayer().getName());

        // Aggiorna le statistiche dei giocatori
        updatePlayerStats();
    }

    private void updatePlayerStats() {
        playersStatsPanel.removeAll();

        for (Player player : controller.getGameState().getPlayers()) {
            JPanel playerStatPanel = new JPanel();
            playerStatPanel.setOpaque(false);
            playerStatPanel.setLayout(new BoxLayout(playerStatPanel, BoxLayout.Y_AXIS));

            JLabel nameLabel = new JLabel(player.getName());
            nameLabel.setForeground(player.getColor());

            JLabel systemsLabel = new JLabel("Sistemi: " + player.getTotalSystems());
            systemsLabel.setForeground(Color.WHITE);

            JLabel shipsLabel = new JLabel("Navi: " + player.getTotalShips());
            shipsLabel.setForeground(Color.WHITE);

            playerStatPanel.add(nameLabel);
            playerStatPanel.add(systemsLabel);
            playerStatPanel.add(shipsLabel);

            playersStatsPanel.add(playerStatPanel);
        }

        playersStatsPanel.revalidate();
        playersStatsPanel.repaint();
    }
}
