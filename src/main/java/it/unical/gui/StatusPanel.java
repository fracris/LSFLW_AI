package it.unical.gui;

import it.unical.controller.GameController;
import it.unical.model.Player;
import it.unical.model.StarSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatusPanel extends JPanel {
    private GameController gameController;
    private JLabel turnLabel;
    private JLabel currentPlayerLabel;
    private JPanel playersStatsPanel;
    private JPanel selectedSystemPanel;
    private JLabel selectedSystemLabel;
    private JLabel selectedSystemShipsLabel;
    private JLabel selectedSystemProdLabel;

    public StatusPanel(GameController gameController) {
        this.gameController = gameController;

        // Configura il pannello
        setPreferredSize(new Dimension(200, 0));
        setBackground(new Color(30, 30, 40));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout(0, 10));

        // Crea il pannello per le informazioni sul turno
        JPanel turnPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        turnPanel.setOpaque(false);
        turnLabel = new JLabel("Turno: 1");
        turnLabel.setForeground(Color.WHITE);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 14));
        currentPlayerLabel = new JLabel("Giocatore: 1");
        currentPlayerLabel.setForeground(Color.WHITE);
        currentPlayerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        turnPanel.add(turnLabel);
        turnPanel.add(currentPlayerLabel);

        // Crea il pannello per le statistiche dei giocatori
        playersStatsPanel = new JPanel();
        playersStatsPanel.setLayout(new BoxLayout(playersStatsPanel, BoxLayout.Y_AXIS));
        playersStatsPanel.setOpaque(false);

        // Crea il pannello per le informazioni sul sistema selezionato
        selectedSystemPanel = new JPanel();
        selectedSystemPanel.setLayout(new BoxLayout(selectedSystemPanel, BoxLayout.Y_AXIS));
        selectedSystemPanel.setOpaque(false);
        selectedSystemPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Sistema Selezionato",
                1,
                0,
                new Font("Arial", Font.BOLD, 12),
                Color.WHITE
        ));

        selectedSystemLabel = new JLabel("Nessun sistema selezionato");
        selectedSystemLabel.setForeground(Color.WHITE);
        selectedSystemLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        selectedSystemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        selectedSystemShipsLabel = new JLabel("Navi: -");
        selectedSystemShipsLabel.setForeground(Color.WHITE);
        selectedSystemShipsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        selectedSystemShipsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        selectedSystemProdLabel = new JLabel("Produzione: -");
        selectedSystemProdLabel.setForeground(Color.YELLOW);
        selectedSystemProdLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        selectedSystemProdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        selectedSystemPanel.add(selectedSystemLabel);
        selectedSystemPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        selectedSystemPanel.add(selectedSystemShipsLabel);
        selectedSystemPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        selectedSystemPanel.add(selectedSystemProdLabel);

        // Aggiunge i pannelli al layout
        add(turnPanel, BorderLayout.NORTH);
        add(playersStatsPanel, BorderLayout.CENTER);
        add(selectedSystemPanel, BorderLayout.SOUTH);

        // Aggiorna lo stato iniziale
        updateStatus();
    }

    // Aggiorna le informazioni di stato
    public void updateStatus() {

        // Aggiorna le statistiche dei giocatori
        updatePlayerStats();

        // Aggiorna le informazioni sul sistema selezionato
        updateSelectedSystemInfo();
    }

    // Aggiorna le statistiche dei giocatori
    private void updatePlayerStats() {
        playersStatsPanel.removeAll();

        // Titolo del pannello
        JLabel statsTitle = new JLabel("Statistiche Giocatori");
        statsTitle.setForeground(Color.WHITE);
        statsTitle.setFont(new Font("Arial", Font.BOLD, 12));
        statsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        playersStatsPanel.add(statsTitle);
        playersStatsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Aggiungi statistiche per ogni giocatore
        for (Player player : gameController.getGameState().getPlayers()) {
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
            playerPanel.setOpaque(false);
            playerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel nameLabel = new JLabel(player.getName());
            nameLabel.setForeground(player.getColor());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel systemsLabel = new JLabel("Sistemi: " + player.getOwnedSystems().size());
            systemsLabel.setForeground(Color.WHITE);
            systemsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            systemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel shipsLabel = new JLabel("Navi: " + player.getTotalShips());
            shipsLabel.setForeground(Color.WHITE);
            shipsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            shipsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            playerPanel.add(nameLabel);
            playerPanel.add(systemsLabel);
            playerPanel.add(shipsLabel);

            playersStatsPanel.add(playerPanel);
            playersStatsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        playersStatsPanel.revalidate();
        playersStatsPanel.repaint();
    }

    // Aggiorna le informazioni sul sistema selezionato
    public void updateSelectedSystemInfo() {
        StarSystem selectedSystem = gameController.getGamePanel().getSelectedSystem();

        if (selectedSystem != null) {
            selectedSystemLabel.setText("Nome: " + selectedSystem.getName());
            selectedSystemShipsLabel.setText("Navi: " + selectedSystem.getShips());
            selectedSystemProdLabel.setText("Produzione: +" + selectedSystem.getProductionRate());

            // Aggiorna il colore in base al proprietario
            if (selectedSystem.getOwner() != null) {
                selectedSystemLabel.setForeground(selectedSystem.getOwner().getColor());
            } else {
                selectedSystemLabel.setForeground(Color.GRAY);
            }
        } else {
            selectedSystemLabel.setText("Nessun sistema selezionato");
            selectedSystemLabel.setForeground(Color.WHITE);
            selectedSystemShipsLabel.setText("Navi: -");
            selectedSystemProdLabel.setText("Produzione: -");
        }
    }
}