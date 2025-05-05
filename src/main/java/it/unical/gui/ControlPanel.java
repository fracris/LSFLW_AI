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
    private JButton sendFleetButton;
    private JSpinner shipsSpinner;
    private JButton endTurnButton;
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

        sendFleetButton = new JButton("Invia Flotta");
        sendFleetButton.setEnabled(false);
        sendFleetButton.addActionListener(this);

        JLabel shipsLabel = new JLabel("Navi:");
        shipsLabel.setForeground(Color.WHITE);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 1, 100, 1);
        shipsSpinner = new JSpinner(spinnerModel);
        shipsSpinner.setPreferredSize(new Dimension(60, 25));

        endTurnButton = new JButton("Fine Turno");
        endTurnButton.addActionListener(this);

        statusLabel = new JLabel("Seleziona un sistema da cui inviare una flotta");
        statusLabel.setForeground(Color.WHITE);

        // Aggiungi i componenti al pannello delle azioni
        actionsPanel.add(sendFleetButton);
        actionsPanel.add(shipsLabel);
        actionsPanel.add(shipsSpinner);
        actionsPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        actionsPanel.add(endTurnButton);

        // Aggiungi i pannelli al layout principale
        add(actionsPanel, BorderLayout.WEST);
        add(statusLabel, BorderLayout.EAST);
    }

//    // Aggiorna lo stato dei controlli
//    public void updateControls() {
//        Player currentPlayer = gameController.getGameState().getCurrentPlayer();
//        boolean isHumanTurn = !currentPlayer.isAI();
//
//        // Disabilita i controlli se non è il turno del giocatore umano
//        endTurnButton.setEnabled(isHumanTurn);
//        shipsSpinner.setEnabled(isHumanTurn);
//
//        StarSystem selectedSystem = gameController.getGamePanel().getSelectedSystem();
//        StarSystem targetSystem = gameController.getGamePanel().getTargetSystem();
//
//        // Abilita il pulsante "Invia Flotta" solo se:
//        // 1. È il turno del giocatore umano
//        // 2. È selezionato un sistema di proprietà del giocatore corrente
//        // 3. È selezionato un sistema target valido
//        boolean canSendFleet = isHumanTurn &&
//                selectedSystem != null &&
//                selectedSystem.getOwner() == currentPlayer &&
//                targetSystem != null &&
//                selectedSystem.getConnectedSystems().contains(targetSystem);
//
//        sendFleetButton.setEnabled(canSendFleet);
//
//        // Limita il numero massimo di navi che possono essere inviate
//        if (selectedSystem != null) {
//            int maxShips = selectedSystem.getShips();
//            SpinnerNumberModel model = (SpinnerNumberModel) shipsSpinner.getModel();
//            model.setMaximum(maxShips);
//
//            // Se il valore attuale è maggiore del massimo, aggiustalo
//            if ((Integer) shipsSpinner.getValue() > maxShips) {
//                shipsSpinner.setValue(maxShips);
//            }
//        }
//
//        // Aggiorna il messaggio di stato
//        updateStatusMessage();
//    }

    public void updateControls() {
        // rimuovi queste righe:
        // Player currentPlayer = gameController.getGameState().getCurrentPlayer();
        // boolean isHumanTurn = !currentPlayer.isAI();
        // endTurnButton.setEnabled(isHumanTurn);
        // shipsSpinner.setEnabled(isHumanTurn);

        // nascondi del tutto il Fine Turno
        endTurnButton.setVisible(false);

        StarSystem selectedSystem = gameController.getGamePanel().getSelectedSystem();
        StarSystem targetSystem   = gameController.getGamePanel().getTargetSystem();
        Player   human            = gameController.getGameState().getHumanPlayer();

        // abilita solo se è un tuo sistema e il target è connesso
        boolean canSendFleet =
                selectedSystem != null &&
                        selectedSystem.getOwner() == human &&
                        targetSystem != null &&
                        selectedSystem.getConnectedSystems().contains(targetSystem);
        sendFleetButton.setEnabled(canSendFleet);

        // aggiorna il massimo delle navi come prima
        if (selectedSystem != null) {
            int maxShips = selectedSystem.getShips();
            SpinnerNumberModel model = (SpinnerNumberModel) shipsSpinner.getModel();
            model.setMaximum(maxShips);
            if ((Integer) shipsSpinner.getValue() > maxShips) {
                shipsSpinner.setValue(maxShips);
            }
        }

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


    // Aggiorna il messaggio di stato
//    private void updateStatusMessage() {
//        Player currentPlayer = gameController.getGameState().getCurrentPlayer();
//
//        if (currentPlayer.isAI()) {
//            statusLabel.setText("L'IA sta pensando...");
//            return;
//        }
//
//        StarSystem selectedSystem = gameController.getGamePanel().getSelectedSystem();
//        StarSystem targetSystem = gameController.getGamePanel().getTargetSystem();
//
//        if (selectedSystem == null) {
//            statusLabel.setText("Seleziona un tuo sistema stellare");
//        } else if (selectedSystem.getOwner() != currentPlayer) {
//            statusLabel.setText("Questo non è un tuo sistema! Selezionane uno tuo");
//        } else if (targetSystem == null) {
//            statusLabel.setText("Seleziona un sistema target connesso");
//        } else {
//            statusLabel.setText("Pronto a inviare una flotta!");
//        }
//    }

    // Gestisce gli eventi dei pulsanti
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendFleetButton) {
            sendFleet();
        } else if (e.getSource() == endTurnButton) {
            endTurn();
        }
    }

    // Invia una flotta
    private void sendFleet() {
        StarSystem selectedSystem = gameController.getGamePanel().getSelectedSystem();
        StarSystem targetSystem = gameController.getGamePanel().getTargetSystem();
        int ships = (Integer) shipsSpinner.getValue();

        if (selectedSystem != null && targetSystem != null && ships > 0) {
            gameController.sendFleet(selectedSystem, targetSystem, ships);

            // Reimposta la selezione
            gameController.getGamePanel().setSelectedSystem(null);
            gameController.getGamePanel().repaint();

            // Aggiorna i controlli
            updateControls();
        }
    }

    // Termina il turno corrente
    private void endTurn() {

        // Reimposta la selezione
        gameController.getGamePanel().setSelectedSystem(null);
        gameController.getGamePanel().repaint();

        // Aggiorna i controlli
        updateControls();
    }
}