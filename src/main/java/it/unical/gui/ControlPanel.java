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
    private JRadioButton percent25Radio;
    private JRadioButton percent50Radio;
    private JRadioButton percent75Radio;
    private JRadioButton percent100Radio;
    private ButtonGroup percentButtonGroup;

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

        // Aggiungi selettore di percentuale
        JPanel percentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        percentPanel.setOpaque(false);
        JLabel percentLabel = new JLabel("Navi da inviare:");
        percentLabel.setForeground(Color.WHITE);
        percentPanel.add(percentLabel);

        percent25Radio = new JRadioButton("25%");
        percent50Radio = new JRadioButton("50%");
        percent75Radio = new JRadioButton("75%");
        percent100Radio = new JRadioButton("100%");

        // Imposta colore testo pulsanti
        percent25Radio.setForeground(Color.WHITE);
        percent50Radio.setForeground(Color.WHITE);
        percent75Radio.setForeground(Color.WHITE);
        percent100Radio.setForeground(Color.WHITE);

        // Imposta sfondo trasparente
        percent25Radio.setOpaque(false);
        percent50Radio.setOpaque(false);
        percent75Radio.setOpaque(false);
        percent100Radio.setOpaque(false);

        // Aggiungi ActionListener per i bottoni
        percent25Radio.addActionListener(this);
        percent50Radio.addActionListener(this);
        percent75Radio.addActionListener(this);
        percent100Radio.addActionListener(this);

        // Raggruppa i bottoni radio
        percentButtonGroup = new ButtonGroup();
        percentButtonGroup.add(percent25Radio);
        percentButtonGroup.add(percent50Radio);
        percentButtonGroup.add(percent75Radio);
        percentButtonGroup.add(percent100Radio);

        // Seleziona 100% come valore predefinito
        percent100Radio.setSelected(true);

        // Aggiungi i bottoni al pannello
        percentPanel.add(percent25Radio);
        percentPanel.add(percent50Radio);
        percentPanel.add(percent75Radio);
        percentPanel.add(percent100Radio);

        actionsPanel.add(percentPanel);
        actionsPanel.add(Box.createRigidArea(new Dimension(20, 0)));

        statusLabel = new JLabel("Seleziona un sistema da cui inviare una flotta");
        statusLabel.setForeground(Color.WHITE);

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

        if (selectedSystem!=null && targetSystem != null && selectedSystem.getShips() > 1) {  // Modifica: deve avere più di 1 nave
            int ships = calculateShipsToSend(selectedSystem);
            gameController.sendFleet(selectedSystem, targetSystem, ships);

            // Reimposta la selezione
            gameController.getGamePanel().setSelectedSystem(null);
            gameController.getGamePanel().repaint();

            // Aggiorna i controlli
            updateControls();
        }
    }

    private int calculateShipsToSend(StarSystem system) {
        int totalShips = system.getShips();
        int shipsToSend = 0;

        // Calcola le navi da inviare in base alla percentuale selezionata
        if (percent25Radio.isSelected()) {
            shipsToSend = (int)(totalShips * 0.25);
        } else if (percent50Radio.isSelected()) {
            shipsToSend = (int)(totalShips * 0.5);
        } else if (percent75Radio.isSelected()) {
            shipsToSend = (int)(totalShips * 0.75);
        } else { // 100% è selezionato
            shipsToSend = totalShips - 1; // Lascia sempre almeno una nave
        }

        // Assicurati di non inviare 0 navi e di lasciare sempre almeno una nave
        return Math.max(1, Math.min(shipsToSend, totalShips - 1));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Aggiorna il valore della percentuale nel GameController
        if (e.getSource() == percent25Radio) {
            gameController.setSendPerc(25);
        } else if (e.getSource() == percent50Radio) {
            gameController.setSendPerc(50);
        } else if (e.getSource() == percent75Radio) {
            gameController.setSendPerc(75);
        } else if (e.getSource() == percent100Radio) {
            gameController.setSendPerc(100);
        }
    }
}
