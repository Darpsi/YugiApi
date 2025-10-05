package org.example.unit3;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.List;

public class YugiGUI {
    private JPanel mainPanel;
    private JPanel JPanelUp;
    private JPanel JPanelDown;
    private JLabel Titulo;
    private JButton RandomUp;
    private JButton RandomDown;
    private JButton usarButton1;
    private JButton usarButton2;
    private JButton usarButton3;
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

    private JLabel ImgUp1;
    private JLabel ImgUp2;
    private JLabel ImgUp3;
    private JLabel ImgDown1;
    private JLabel ImgDown2;
    private JLabel ImgDown3;

    private Cartas[] playerCards = new Cartas[3];
    private Cartas[] npcCards = new Cartas[3];
    private boolean randomUpUsed = false;
    private boolean randomDownUsed = false;
    private Random random = new Random();

    public YugiGUI() {
        ImgUp1 = new JLabel();
        ImgUp2 = new JLabel();
        ImgUp3 = new JLabel();
        ImgDown1 = new JLabel();
        ImgDown2 = new JLabel();
        ImgDown3 = new JLabel();

        inicializarPanelCarta(CardUp1, ImgUp1, Nombreup1);
        inicializarPanelCarta(CardUp2, ImgUp2, Nombreup2);
        inicializarPanelCarta(CardUp3, ImgUp3, Nombreup3);
        inicializarPanelCarta(CardDown1, ImgDown1, NombreDown1);
        inicializarPanelCarta(CardDown2, ImgDown2, NombreDown2);
        inicializarPanelCarta(CardDown3, ImgDown3, NombreDown3);

        RandomDown.addActionListener((ActionEvent e) -> {
            if (!randomDownUsed) {
                mostrarCartas(1);
                randomDownUsed = true;
                RandomDown.setEnabled(false);
            }
        });
        RandomUp.addActionListener((ActionEvent e) -> {
            if (!randomUpUsed) {
                mostrarCartas(2); // NPC
                randomUpUsed = true;
                RandomUp.setEnabled(false);
            }
        });

        usarButton1.addActionListener((ActionEvent e) -> compareCards(1));
        usarButton2.addActionListener((ActionEvent e) -> compareCards(2));
        usarButton3.addActionListener((ActionEvent e) -> compareCards(3));
    }

    private void inicializarPanelCarta(JPanel panel, JLabel imagen, JLabel nombre) {
        panel.setLayout(new BorderLayout());
        imagen.setHorizontalAlignment(SwingConstants.CENTER);
        imagen.setVerticalAlignment(SwingConstants.CENTER);
        nombre.setHorizontalAlignment(SwingConstants.CENTER);
        nombre.setVerticalAlignment(SwingConstants.TOP);
        nombre.setPreferredSize(new Dimension(120, 150));
        panel.add(imagen, BorderLayout.CENTER);
        panel.add(nombre, BorderLayout.SOUTH);
    }

    public Cartas consultar() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://db.ygoprodeck.com/api/v7/randomcard.php"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject root = new JSONObject(response.body());
                JSONArray dataArray = root.getJSONArray("data");
                JSONObject cardData = dataArray.getJSONObject(0);

                String id = String.valueOf(cardData.getInt("id"));
                String name = cardData.optString("name", "Desconocida");
                String type = cardData.optString("type", "N/A");
                String desc = cardData.optString("desc", "Sin descripción");
                String atk = cardData.has("atk") ? String.valueOf(cardData.getInt("atk")) : "0";
                String def = cardData.has("def") ? String.valueOf(cardData.getInt("def")) : "0";
                String level = cardData.has("level") ? String.valueOf(cardData.getInt("level")) : "0";
                String race = cardData.optString("race", "N/A");
                String attribute = cardData.optString("attribute", "N/A");

                JSONArray images = cardData.getJSONArray("card_images");
                String imgURL = images.getJSONObject(0).getString("image_url");

                Cartas carta = new Cartas(id, name, type, desc, atk, def, level, race, attribute);
                carta.setImage(imgURL);
                return carta;
            } else {
                System.out.println("Error: respuesta HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("Error al consultar carta: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void mostrarCartas(int jugador) {
        Set<String> usedCardIds = new HashSet<>();
        int maxRetries = 5;

        for (int i = 1; i <= 3; i++) {
            int retries = 0;
            Cartas carta = null;
            while (retries < maxRetries) {
                carta = consultar();
                if (carta != null) {
                    if (!usedCardIds.contains(carta.getId())) {
                        usedCardIds.add(carta.getId());
                        if (jugador == 1) {
                            playerCards[i - 1] = carta;
                        } else {
                            npcCards[i - 1] = carta;
                        }
                        extraer_datos(carta, jugador, i);
                        break;
                    } else {
                        System.out.println("Carta duplicada encontrada, reintentando... (Intento " + (retries + 1) + "/" + maxRetries + ")");
                    }
                } else {
                    System.out.println("Carta null obtenida, reintentando... (Intento " + (retries + 1) + "/" + maxRetries + ")");
                }
                retries++;
                try {
                    Thread.sleep(random.nextInt(501) + 500);
                } catch (InterruptedException e) {
                    System.out.println("Error en espera: " + e.getMessage());
                }
            }
            if (retries >= maxRetries) {
                System.out.println("No se pudo obtener carta única en la posición " + i + " después de " + maxRetries + " intentos. Usando placeholder.");
                JLabel imgLabel = getImgLabel(jugador, i);
                JLabel nombreLabel = getNombreLabel(jugador, i);
                imgLabel.setIcon(null);
                nombreLabel.setText("<html><b>Carta no disponible</b><br>Intenta de nuevo.</html>");
                if (jugador == 1) {
                    playerCards[i - 1] = null;
                } else {
                    npcCards[i - 1] = null;
                }
            }
        }
    }

    private void compareCards(int playerPosition) {
        if (!randomDownUsed || !randomUpUsed) {
            JOptionPane.showMessageDialog(mainPanel,
                    "¡Ambos jugadores deben generar sus cartas primero!",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Integer> playerActive = getActiveCardIndices(playerCards);
        List<Integer> npcActive = getActiveCardIndices(npcCards);

        if (playerActive.isEmpty() || npcActive.isEmpty()) {
            checkGameOver();
            return;
        }

        int playerIndex = playerPosition - 1;
        if (!playerActive.contains(playerIndex)) {
            JOptionPane.showMessageDialog(mainPanel,
                    "¡No hay carta en la posición seleccionada!",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int npcIndex = npcActive.get(random.nextInt(npcActive.size()));
        Cartas playerCard = playerCards[playerIndex];
        Cartas npcCard = npcCards[npcIndex];

        int playerScore = Integer.parseInt(playerCard.getAtk()) + Integer.parseInt(playerCard.getDef());
        int npcScore = Integer.parseInt(npcCard.getAtk()) + Integer.parseInt(npcCard.getDef());

        if (playerScore > npcScore) {
            removeCard(2, npcIndex + 1);
            npcCards[npcIndex] = null;
            JOptionPane.showMessageDialog(mainPanel,
                    "¡La carta del Jugador " + playerPosition + " (" + playerCard.getName() + ", ATK+DEF: " + playerScore +
                            ") derrota a la carta del NPC " + (npcIndex + 1) + " (" + npcCard.getName() + ", ATK+DEF: " + npcScore + ")!",
                    "Resultado de la batalla", JOptionPane.INFORMATION_MESSAGE);
        } else if (npcScore > playerScore) {
            removeCard(1, playerIndex + 1);
            playerCards[playerIndex] = null;
            JOptionPane.showMessageDialog(mainPanel,
                    "¡La carta del NPC " + (npcIndex + 1) + " (" + npcCard.getName() + ", ATK+DEF: " + npcScore +
                            ") derrota a la carta del Jugador " + playerPosition + " (" + playerCard.getName() + ", ATK+DEF: " + playerScore + ")!",
                    "Resultado de la batalla", JOptionPane.INFORMATION_MESSAGE);
        } else {
            removeCard(1, playerIndex + 1);
            removeCard(2, npcIndex + 1);
            playerCards[playerIndex] = null;
            npcCards[npcIndex] = null;
            JOptionPane.showMessageDialog(mainPanel,
                    "¡Empate! La carta del Jugador " + playerPosition + " (" + playerCard.getName() +
                            ") y la carta del NPC " + (npcIndex + 1) + " (" + npcCard.getName() +
                            ") (ATK+DEF: " + playerScore + ") son eliminadas.",
                    "Resultado de la batalla", JOptionPane.INFORMATION_MESSAGE);
        }

        checkGameOver();
    }


    private List<Integer> getActiveCardIndices(Cartas[] cards) {
        List<Integer> active = new ArrayList<>();
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null) {
                active.add(i);
            }
        }
        return active;
    }

    private void removeCard(int jugador, int posicion) {
        JLabel imgLabel = getImgLabel(jugador, posicion);
        JLabel nombreLabel = getNombreLabel(jugador, posicion);
        JPanel panel = (jugador == 1) ? switch (posicion) {
            case 1 -> CardDown1;
            case 2 -> CardDown2;
            case 3 -> CardDown3;
            default -> null;
        } : switch (posicion) {
            case 1 -> CardUp1;
            case 2 -> CardUp2;
            case 3 -> CardUp3;
            default -> null;
        };
        imgLabel.setIcon(null);
        nombreLabel.setText("");
        if (panel != null) {
            panel.revalidate();
            panel.repaint();
        }
    }

    private void checkGameOver() {
        int playerCount = getActiveCardIndices(playerCards).size();
        int npcCount = getActiveCardIndices(npcCards).size();

        if (playerCount == 0 && npcCount == 0) {
            JOptionPane.showMessageDialog(mainPanel, "¡Empate! Ambos jugadores se quedaron sin cartas.", "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
            disableGame();
        } else if (playerCount == 0) {
            JOptionPane.showMessageDialog(mainPanel, "¡NPC gana! El jugador se quedó sin cartas.", "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
            disableGame();
        } else if (npcCount == 0) {
            JOptionPane.showMessageDialog(mainPanel, "¡El jugador gana! El NPC se quedó sin cartas.", "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
            disableGame();
        }
    }

    private void disableGame() {
        usarButton1.setEnabled(false);
        usarButton2.setEnabled(false);
        usarButton3.setEnabled(false);
        RandomUp.setEnabled(false);
        RandomDown.setEnabled(false);
    }

    private JLabel getImgLabel(int jugador, int posicion) {
        if (jugador == 1) { // Human player (bottom)
            return switch (posicion) {
                case 1 -> ImgDown1;
                case 2 -> ImgDown2;
                case 3 -> ImgDown3;
                default -> null;
            };
        } else { // NPC (top)
            return switch (posicion) {
                case 1 -> ImgUp1;
                case 2 -> ImgUp2;
                case 3 -> ImgUp3;
                default -> null;
            };
        }
    }

    private JLabel getNombreLabel(int jugador, int posicion) {
        if (jugador == 1) {
            return switch (posicion) {
                case 1 -> NombreDown1;
                case 2 -> NombreDown2;
                case 3 -> NombreDown3;
                default -> null;
            };
        } else { // NPC (top)
            return switch (posicion) {
                case 1 -> Nombreup1;
                case 2 -> Nombreup2;
                case 3 -> Nombreup3;
                default -> null;
            };
        }
    }

    public void extraer_datos(Cartas carta, int jugador, int posicion) {
        String texto = "<html>" +
                "<table style='width:100%;'>" +
                "<tr>" +
                "<td style='width:50%; vertical-align:top;'>" +
                "<b>ID:</b> " + carta.getId() + "<br>" +
                "<b>Nombre:</b> " + carta.getName() + "<br>" +
                "<b>Nivel:</b> " + carta.getLevel() + "<br>" +
                "<b>Raza:</b> " + carta.getRace() + "</td>" +
                "<td style='width:50%; vertical-align:top;'>" +
                "<b>ATK:</b> " + carta.getAtk() + "<br>" +
                "<b>DEF:</b> " + carta.getDef() + "<br>" +
                "<b>Tipo:</b> " + carta.getType() + "</td>" +
                "</tr>" +
                "</table>" +
                "<b>Atributo:</b> " + carta.getAttribute() +
                "</html>";

        JLabel imgLabel = getImgLabel(jugador, posicion);
        JLabel nombreLabel = getNombreLabel(jugador, posicion);

        try {
            Image imagen = ImageIO.read(new URL(carta.getImage()));
            ImageIcon icon = new ImageIcon(imagen.getScaledInstance(120, 180, Image.SCALE_SMOOTH));
            imgLabel.setIcon(icon);
        } catch (IOException e) {
            System.out.println("Error cargando imagen: " + e.getMessage());
            imgLabel.setIcon(null);
        }

        nombreLabel.setText(texto);
        JPanel panel = (jugador == 1) ? switch (posicion) {
            case 1 -> CardDown1;
            case 2 -> CardDown2;
            case 3 -> CardDown3;
            default -> null;
        } : switch (posicion) {
            case 1 -> CardUp1;
            case 2 -> CardUp2;
            case 3 -> CardUp3;
            default -> null;
        };
        if (panel != null) {
            panel.revalidate();
            panel.repaint();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("YugiGUI - Batalla de Cartas");
        frame.setContentPane(new YugiGUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(900, 700));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}