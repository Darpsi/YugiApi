package org.example.unit3;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class YugiGUI {
    private JPanel mainPanel;
    private JPanel JPanelUp;
    private JPanel JPanelDown;
    private JLabel Titulo;
    private JButton RandomUp;
    private JButton RandomDown;
    private JPanel CardUp1;
    private JPanel CardUp2;
    private JPanel CardUp3;
    private JPanel CardDown1;
    private JPanel CardDown2;
    private JPanel CardDown3;
    private JLabel Nombreup1;
    private JLabel Nombreup2;
    private JLabel Nombreup3;
    private JLabel NombreDown1;
    private JLabel NombreDown2;
    private JLabel NombreDown3;
    private JButton usarButton2;
    private JButton usarButton1;
    private JButton usarButton3;
    


    public YugiGUI() {
        RandomUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        RandomDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }


    public int generarCarta() {
        Random rand = new Random();
        int numero = rand.nextInt(8000000)+1;
        return numero;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("YugiGUI");
        frame.setContentPane(new YugiGUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
