package org.example.unit3;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    // Etiquetas de imagen
    private JLabel ImgUp1 = new JLabel();
    private JLabel ImgUp2 = new JLabel();
    private JLabel ImgUp3 = new JLabel();
    private JLabel ImgDown1 = new JLabel();
    private JLabel ImgDown2 = new JLabel();
    private JLabel ImgDown3 = new JLabel();

    public YugiGUI() {
        // Se añaden los labels de imagen a los paneles
        CardUp1.add(ImgUp1);
        CardUp2.add(ImgUp2);
        CardUp3.add(ImgUp3);
        CardDown1.add(ImgDown1);
        CardDown2.add(ImgDown2);
        CardDown3.add(ImgDown3);

        // Botón jugador 1
        RandomUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarCartas(1);
            }
        });

        // Botón jugador 2
        RandomDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarCartas(2);
            }
        });
    }

    // Genera ID aleatorio entre 1 y 15000
    public int generarCarta() {
        Random rand = new Random();
        return rand.nextInt(15000) + 1;
    }

    // Consulta carta desde la API
    public Cartas consultar(String idCarta) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://db.ygoprodeck.com/api/v7/cardinfo.php?id=" + idCarta))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                JSONArray data = json.getJSONArray("data");
                JSONObject cardData = data.getJSONObject(0);

                // Se extraen todos los campos principales
                String id = String.valueOf(cardData.getInt("id"));
                String name = cardData.optString("name", "Desconocida");
                String type = cardData.optString("type", "N/A");
                String desc = cardData.optString("desc", "Sin descripción");
                String atk = cardData.has("atk") ? String.valueOf(cardData.getInt("atk")) : "0";
                String def = cardData.has("def") ? String.valueOf(cardData.getInt("def")) : "0";
                String level = cardData.has("level") ? String.valueOf(cardData.getInt("level")) : "0";
                String race = cardData.optString("race", "N/A");
                String attribute = cardData.optString("attribute", "N/A");

                // Imagen
                JSONArray images = cardData.getJSONArray("card_images");
                String imgURL = images.getJSONObject(0).getString("image_url");

                // Crear objeto carta
                Cartas carta = new Cartas(id, name, type, desc, atk, def, level, race, attribute);
                carta.setImage(imgURL);
                return carta;
            }
        } catch (Exception e) {
            System.out.println("Error al consultar carta: " + e.getMessage());
        }
        return null;
    }

    // Muestra tres cartas en pantalla para el jugador indicado
    public void mostrarCartas(int jugador) {
        for (int i = 1; i <= 3; i++) {
            int random = generarCarta();
            Cartas carta = consultar(String.valueOf(random));
            if (carta != null) {
                extraer_datos(carta, jugador, i);
            }
        }
    }

    // Muestra imagen, nombre, ATK y DEF en el GUI
    public void extraer_datos(Cartas carta, int jugador, int posicion) {
        try {
            Image imagen = ImageIO.read(new URL(carta.getImage()));
            ImageIcon icon = new ImageIcon(imagen.getScaledInstance(120, 180, Image.SCALE_SMOOTH));

            String texto = "<html><b>" + carta.getName() + "</b><br>"
                    + "ATK: " + carta.getAtk() + " | DEF: " + carta.getDef()
                    + "<br>Tipo: " + carta.getType()
                    + "<br>Raza: " + carta.getRace()
                    + "<br>Atributo: " + carta.getAttribute() + "</html>";

            if (jugador == 1) {
                switch (posicion) {
                    case 1 -> { ImgUp1.setIcon(icon); Nombreup1.setText(texto); }
                    case 2 -> { ImgUp2.setIcon(icon); Nombreup2.setText(texto); }
                    case 3 -> { ImgUp3.setIcon(icon); Nombreup3.setText(texto); }
                }
            } else {
                switch (posicion) {
                    case 1 -> { ImgDown1.setIcon(icon); NombreDown1.setText(texto); }
                    case 2 -> { ImgDown2.setIcon(icon); NombreDown2.setText(texto); }
                    case 3 -> { ImgDown3.setIcon(icon); NombreDown3.setText(texto); }
                }
            }

        } catch (IOException e) {
            System.out.println("Error cargando imagen: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("YugiGUI");
        frame.setContentPane(new YugiGUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
