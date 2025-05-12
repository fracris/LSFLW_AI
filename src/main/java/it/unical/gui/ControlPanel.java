package it.unical.gui;

import it.unical.controller.GameController;
import it.unical.model.StarSystem;
import it.unical.model.Player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControlPanel extends JPanel implements ActionListener {
    private GameController gameController;
    private JLabel statusLabel;

    public ControlPanel(GameController gameController) {
        this.gameController = gameController;

        // Configura il pannello
        setPreferredSize(new Dimension(0, 80));
        setBackground(new Color(40, 40, 50));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout(10, 0));

        // Crea i componenti
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setOpaque(false);


        statusLabel = new JLabel("Seleziona un sistema da cui inviare una flotta");
        statusLabel.setForeground(Color.WHITE);

        actionsPanel.add(Box.createRigidArea(new Dimension(20, 0)));

        // Aggiungi i pannelli al layout principale
        add(actionsPanel, BorderLayout.WEST);
        add(statusLabel, BorderLayout.EAST);
    }

    public void updateControls() {

        StarSystem selectedSystem = gameController.getGamePanel().getSelectedSystem();
        StarSystem targetSystem   = gameController.getGamePanel().getTargetSystem();
        Player   human            = gameController.getGameState().getHumanPlayer();

        // messaggio di stato
        if (selectedSystem == null) {
            statusLabel.setText("Seleziona un tuo sistema stellare");
        } else if (selectedSystem.getOwner() != human) {
            statusLabel.setText("Questo non è un tuo sistema!");
        } else if (targetSystem == null) {
            statusLabel.setText("Seleziona un sistema target");
        } else {
            statusLabel.setText("Pronto a inviare una flotta");
        }
    }

    // Invia una flotta
    public void sendFleet() {
        StarSystem selectedSystem = gameController.getGamePanel().getSelectedSystem();
        StarSystem targetSystem = gameController.getGamePanel().getTargetSystem();

        if (selectedSystem!=null && targetSystem != null && selectedSystem.getShips()>0) {
            int ships = selectedSystem.getShips()*gameController.getSendPerc()/100;
            gameController.sendFleet(selectedSystem, targetSystem, ships);

            // Reimposta la selezione
            gameController.getGamePanel().setSelectedSystem(null);
            gameController.getGamePanel().repaint();

            // Aggiorna i controlli
            updateControls();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}