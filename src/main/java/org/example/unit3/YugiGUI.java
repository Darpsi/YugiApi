package org.example.unit3;

import javax.swing.*;

public class YugiGUI {
    private JPanel mainPanel;
    private JPanel JPanelUp;
    private JPanel JPanelRight;
    private JLabel Titulo;
    private JButton RandomLeft;
    private JButton RandomRight;

    public static void main(String[] args) {
        JFrame frame = new JFrame("YugiGUI");
        frame.setContentPane(new YugiGUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
