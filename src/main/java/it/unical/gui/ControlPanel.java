package it.unical.gui;

import it.unical.controller.GameController;
import it.unical.model.StarSystem;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    private GameController controller;
    private JButton endTurnButton;
    private JSlider shipPercentageSlider;
    private JLabel selectedSystemLabel;
    private JLabel shipsToSendLabel;

    public ControlPanel(GameController controller) {
        this.controller = controller;
        setPreferredSize(new Dimension(800, 80));
        setBackground(new Color(40, 40, 60));
        setLayout(new BorderLayout());

        // Inizializza i componenti
        endTurnButton = new JButton("Fine Turno");
        endTurnButton.addActionListener(e -> controller.endTurn());

        shipPercentageSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        shipPercentageSlider.setMajorTickSpacing(25);
        shipPercentageSlider.setMinorTickSpacing(5);
        shipPercentageSlider.setPaintTicks(true);
        shipPercentageSlider.setPaintLabels(true);
        shipPercentageSlider.setForeground(Color.WHITE);
        shipPercentageSlider.setOpaque(false);
        shipPercentageSlider.addChangeListener(e -> updateShipsToSend());

        selectedSystemLabel = new JLabel("Nessun sistema selezionato");
        selectedSystemLabel.setForeground(Color.WHITE);

        shipsToSendLabel = new JLabel("Navi da inviare: 0");
        shipsToSendLabel.setForeground(Color.WHITE);

        // Aggiunge i componenti al pannello
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new GridLayout(2, 1));

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setOpaque(false);
        sliderPanel.add(new JLabel("Percentuale:"), BorderLayout.WEST);
        sliderPanel.add(shipPercentageSlider, BorderLayout.CENTER);
        sliderPanel.add(shipsToSendLabel, BorderLayout.EAST);

        centerPanel.add(selectedSystemLabel);
        centerPanel.add(sliderPanel);

        add(centerPanel, BorderLayout.CENTER);
        add(endTurnButton, BorderLayout.EAST);
    }

    public void update() {
        StarSystem selectedSystem = controller.getSelectedSystem();
        if (selectedSystem != null) {
            selectedSystemLabel.setText("Sistema selezionato: " + selectedSystem.getName() +
                    " (" + selectedSystem.getShips() + " navi)");
            updateShipsToSend();
        } else {
            selectedSystemLabel.setText("Nessun sistema selezionato");
            shipsToSendLabel.setText("Navi da inviare: 0");
        }
    }

    private void updateShipsToSend() {
        StarSystem selectedSystem = controller.getSelectedSystem();
        if (selectedSystem != null) {
            int percentage = shipPercentageSlider.getValue();
            int shipsToSend = selectedSystem.getShips() * percentage / 100;
            shipsToSendLabel.setText("Navi da inviare: " + shipsToSend);
        }
    }

    public int getShipsPercentage() {
        return shipPercentageSlider.getValue();
    }
}
