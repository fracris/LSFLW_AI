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
    private final GameController gameController;
    private final JLabel statusLabel;
    private final JRadioButton percent25Radio;
    private final JRadioButton percent50Radio;
    private final JRadioButton percent75Radio;
    private final JRadioButton percent100Radio;

    public ControlPanel(GameController gameController) {
        this.gameController = gameController;

        setPreferredSize(new Dimension(0, 80));
        setBackground(new Color(40, 40, 50));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout(10, 0));

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setOpaque(false);

        JPanel percentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        percentPanel.setOpaque(false);
        JLabel percentLabel = new JLabel("Navi da inviare:");
        percentLabel.setForeground(Color.WHITE);
        percentPanel.add(percentLabel);

        percent25Radio = new JRadioButton("25%");
        percent50Radio = new JRadioButton("50%");
        percent75Radio = new JRadioButton("75%");
        percent100Radio = new JRadioButton("100%");

        percent25Radio.setForeground(Color.WHITE);
        percent50Radio.setForeground(Color.WHITE);
        percent75Radio.setForeground(Color.WHITE);
        percent100Radio.setForeground(Color.WHITE);

        percent25Radio.setOpaque(false);
        percent50Radio.setOpaque(false);
        percent75Radio.setOpaque(false);
        percent100Radio.setOpaque(false);

        percent25Radio.addActionListener(this);
        percent50Radio.addActionListener(this);
        percent75Radio.addActionListener(this);
        percent100Radio.addActionListener(this);

        ButtonGroup percentButtonGroup = new ButtonGroup();
        percentButtonGroup.add(percent25Radio);
        percentButtonGroup.add(percent50Radio);
        percentButtonGroup.add(percent75Radio);
        percentButtonGroup.add(percent100Radio);

        percent100Radio.setSelected(true);

        percentPanel.add(percent25Radio);
        percentPanel.add(percent50Radio);
        percentPanel.add(percent75Radio);
        percentPanel.add(percent100Radio);

        actionsPanel.add(percentPanel);
        actionsPanel.add(Box.createRigidArea(new Dimension(20, 0)));

        statusLabel = new JLabel("Seleziona un sistema da cui inviare una flotta");
        statusLabel.setForeground(Color.WHITE);

        add(actionsPanel, BorderLayout.WEST);
        add(statusLabel, BorderLayout.EAST);
    }

    public void updateControls() {
        StarSystem selectedSystem = gameController.getGamePanel().getSelectedSystem();
        StarSystem targetSystem   = gameController.getGamePanel().getTargetSystem();
        Player   human            = gameController.getGameState().getHumanPlayer();

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

    public void sendFleet() {
        StarSystem selectedSystem = gameController.getGamePanel().getSelectedSystem();
        StarSystem targetSystem = gameController.getGamePanel().getTargetSystem();

        if (selectedSystem!=null && targetSystem != null && selectedSystem.getShips() > 1) {
            int ships = calculateShipsToSend(selectedSystem);
            gameController.sendFleet(selectedSystem, targetSystem, ships);

            gameController.getGamePanel().setSelectedSystem(null);
            gameController.getGamePanel().repaint();

            updateControls();
        }
    }

    private int calculateShipsToSend(StarSystem system) {
        int totalShips = system.getShips();
        int shipsToSend;

        if (percent25Radio.isSelected()) {
            shipsToSend = (int)(totalShips * 0.25);
        } else if (percent50Radio.isSelected()) {
            shipsToSend = (int)(totalShips * 0.5);
        } else if (percent75Radio.isSelected()) {
            shipsToSend = (int)(totalShips * 0.75);
        } else {
            shipsToSend = totalShips - 1;
        }
        return Math.max(1, Math.min(shipsToSend, totalShips - 1));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
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
