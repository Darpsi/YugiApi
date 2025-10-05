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

/**
 * Clase principal de la interfaz gráfica para el juego.
 * Gestiona la UI, la obtención de cartas desde la API YGOProDeck y la lógica de duelos.
 */
public class YugiGUI {
    private JPanel mainPanel;
    private JPanel JPanelUp;
    private JPanel JPanelDown;
    private JLabel Titulo;
    private JButton RandomUp;
    private JButton RandomDown;
    private JButton AtaqueButton1;
    private JButton AtaqueButton2;
    private JButton AtaqueButton3;
    private JButton DefensaButton1;
    private JButton DefensaButton2;
    private JButton DefensaButton3;
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

    private Cartas[] playerCards = new Cartas[3]; // Cartas del jugador humano (abajo, CardDown*)
    private Cartas[] npcCards = new Cartas[3];    // Cartas del NPC (arriba, CardUp*)
    private boolean randomUpUsed = false;         // Indicador para RandomUp (NPC)
    private boolean randomDownUsed = false;       // Indicador para RandomDown (jugador)
    private int playerWins = 0;                   // Contador de victorias del jugador
    private int npcWins = 0;                      // Contador de victorias del NPC
    private Random random = new Random();

    /**
     * Constructor que inicializa la interfaz gráfica y configura los listeners para los botones.
     */
    public YugiGUI() {
        // Inicializar etiquetas de imagen
        ImgUp1 = new JLabel();
        ImgUp2 = new JLabel();
        ImgUp3 = new JLabel();
        ImgDown1 = new JLabel();
        ImgDown2 = new JLabel();
        ImgDown3 = new JLabel();

        // Configurar paneles de cartas
        inicializarPanelCarta(CardUp1, ImgUp1, Nombreup1);
        inicializarPanelCarta(CardUp2, ImgUp2, Nombreup2);
        inicializarPanelCarta(CardUp3, ImgUp3, Nombreup3);
        inicializarPanelCarta(CardDown1, ImgDown1, NombreDown1);
        inicializarPanelCarta(CardDown2, ImgDown2, NombreDown2);
        inicializarPanelCarta(CardDown3, ImgDown3, NombreDown3);

        // Listener para generar cartas del jugador
        RandomDown.addActionListener((ActionEvent e) -> {
            if (!randomDownUsed) {
                mostrarCartas(1); // Jugador humano
                randomDownUsed = true;
                RandomDown.setEnabled(false);
                RandomDown.setVisible(false); // Ocultar botón tras uso
            }
        });

        // Listener para generar cartas del NPC
        RandomUp.addActionListener((ActionEvent e) -> {
            if (!randomUpUsed) {
                mostrarCartas(2); // NPC
                randomUpUsed = true;
                RandomUp.setEnabled(false);
                RandomUp.setVisible(false); // Ocultar botón tras uso
            }
        });

        // Listeners para seleccionar cartas del jugador en modo ataque
        AtaqueButton1.addActionListener((ActionEvent e) -> compareCards(1, false));
        AtaqueButton2.addActionListener((ActionEvent e) -> compareCards(2, false));
        AtaqueButton3.addActionListener((ActionEvent e) -> compareCards(3, false));

        // Listeners para seleccionar cartas del jugador en modo defensa
        DefensaButton1.addActionListener((ActionEvent e) -> compareCards(1, true));
        DefensaButton2.addActionListener((ActionEvent e) -> compareCards(2, true));
        DefensaButton3.addActionListener((ActionEvent e) -> compareCards(3, true));
    }

    /**
     * Inicializa un panel de carta con su imagen y etiqueta de texto.
     * @param panel Panel de la carta
     * @param imagen JLabel para la imagen de la carta
     * @param nombre JLabel para los datos de la carta
     */
    private void inicializarPanelCarta(JPanel panel, JLabel imagen, JLabel nombre) {
        panel.setLayout(new BorderLayout());
        imagen.setHorizontalAlignment(SwingConstants.CENTER);
        imagen.setVerticalAlignment(SwingConstants.CENTER);
        nombre.setHorizontalAlignment(SwingConstants.CENTER);
        nombre.setVerticalAlignment(SwingConstants.TOP);
        nombre.setPreferredSize(new Dimension(120, 150));
        panel.setPreferredSize(new Dimension(150, 250)); // Tamaño fijo para cartas
        panel.add(imagen, BorderLayout.CENTER);
        panel.add(nombre, BorderLayout.SOUTH);
    }

    /**
     * Consulta una carta aleatoria desde la API YGOProDeck.
     * @return Objeto Cartas con los datos de la carta, o null si falla la consulta
     */
    public Cartas consultar() {
        try {
            // Configurar cliente HTTP
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://db.ygoprodeck.com/api/v7/randomcard.php"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Verificar respuesta exitosa
            if (response.statusCode() == 200) {
                // Parsear JSON
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

                // Obtener URL de la imagen
                JSONArray images = cardData.getJSONArray("card_images");
                String imgURL = images.getJSONObject(0).getString("image_url");

                // Crear y devolver objeto Cartas
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

    /**
     * Obtiene y muestra 3 cartas únicas para un jugador (humano o NPC).
     * @param jugador 1 para humano (CardDown*), 2 para NPC (CardUp*)
     */
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
                        if (jugador == 1) { // Jugador humano (abajo)
                            playerCards[i - 1] = carta;
                        } else { // NPC (arriba)
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

    /**
     * Compara las cartas del jugador y NPC, asignando puntos según las reglas de ATK vs. DEF.
     * @param playerPosition Posición de la carta del jugador (1, 2, 3)
     * @param playerInDefense Indica si el jugador está en modo defensa (true) o ataque (false)
     */
    private void compareCards(int playerPosition, boolean playerInDefense) {
        // Verificar si ambos jugadores han generado sus cartas
        if (!randomDownUsed || !randomUpUsed) {
            JOptionPane.showMessageDialog(mainPanel, "¡Ambos jugadores deben generar sus cartas primero!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar si hay cartas disponibles
        List<Integer> playerActive = getActiveCardIndices(playerCards);
        List<Integer> npcActive = getActiveCardIndices(npcCards);

        if (playerActive.isEmpty() || npcActive.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "¡No hay cartas disponibles para la batalla!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar si la carta seleccionada del jugador está activa
        int playerIndex = playerPosition - 1;
        if (!playerActive.contains(playerIndex)) {
            JOptionPane.showMessageDialog(mainPanel, "¡No hay carta en la posición seleccionada!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Seleccionar carta aleatoria del NPC
        int npcIndex = npcActive.get(random.nextInt(npcActive.size()));
        Cartas playerCard = playerCards[playerIndex];
        Cartas npcCard = npcCards[npcIndex];

        // Determinar modos de las cartas
        boolean npcInDefense = random.nextBoolean();
        String playerMode = playerInDefense ? "Defensa" : "Ataque";
        String npcMode = npcInDefense ? "Defensa" : "Ataque";
        int playerScore = playerInDefense ? Integer.parseInt(playerCard.getDef()) : Integer.parseInt(playerCard.getAtk());
        int npcScore = npcInDefense ? Integer.parseInt(npcCard.getDef()) : Integer.parseInt(npcCard.getAtk());

        // Comparar según reglas de Yu-Gi-Oh!
        if (playerInDefense && npcInDefense) {
            // Ambos en defensa: empate
            JOptionPane.showMessageDialog(mainPanel,
                    "¡Empate! La carta del Jugador " + playerPosition + " (" + playerCard.getName() +
                            ") y la carta del NPC " + (npcIndex + 1) + " (" + npcCard.getName() +
                            ") están en Defensa.\n" +
                            "Victorias: Jugador " + playerWins + ", NPC " + npcWins,
                    "Resultado de la batalla", JOptionPane.INFORMATION_MESSAGE);
        } else if (playerInDefense) {
            // Jugador en defensa, NPC en ataque
            if (npcScore > playerScore) {
                npcWins++;
                JOptionPane.showMessageDialog(mainPanel,
                        "¡La carta del NPC " + (npcIndex + 1) + " (" + npcCard.getName() + ", ATK: " + npcScore +
                                ") derrota a la carta del Jugador " + playerPosition + " (" + playerCard.getName() + ", DEF: " + playerScore + ")!\n" +
                                "Victorias: Jugador " + playerWins + ", NPC " + npcWins,
                        "Resultado de la batalla", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainPanel,
                        "¡Empate! La carta del Jugador " + playerPosition + " (" + playerCard.getName() +
                                ", DEF: " + playerScore + ") resiste a la carta del NPC " + (npcIndex + 1) + " (" + npcCard.getName() +
                                ", ATK: " + npcScore + ").\n" +
                                "Victorias: Jugador " + playerWins + ", NPC " + npcWins,
                        "Resultado de la batalla", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (npcInDefense) {
            // Jugador en ataque, NPC en defensa
            if (playerScore > npcScore) {
                playerWins++;
                JOptionPane.showMessageDialog(mainPanel,
                        "¡La carta del Jugador " + playerPosition + " (" + playerCard.getName() + ", ATK: " + playerScore +
                                ") derrota a la carta del NPC " + (npcIndex + 1) + " (" + npcCard.getName() + ", DEF: " + npcScore + ")!\n" +
                                "Victorias: Jugador " + playerWins + ", NPC " + npcWins,
                        "Resultado de la batalla", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainPanel,
                        "¡Empate! La carta del Jugador " + playerPosition + " (" + playerCard.getName() +
                                ", ATK: " + playerScore + ") no supera a la carta del NPC " + (npcIndex + 1) + " (" + npcCard.getName() +
                                ", DEF: " + npcScore + ").\n" +
                                "Victorias: Jugador " + playerWins + ", NPC " + npcWins,
                        "Resultado de la batalla", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            // Ambos en ataque
            if (playerScore > npcScore) {
                playerWins++;
                JOptionPane.showMessageDialog(mainPanel,
                        "¡La carta del Jugador " + playerPosition + " (" + playerCard.getName() + ", ATK: " + playerScore +
                                ") derrota a la carta del NPC " + (npcIndex + 1) + " (" + npcCard.getName() + ", ATK: " + npcScore + ")!\n" +
                                "Victorias: Jugador " + playerWins + ", NPC " + npcWins,
                        "Resultado de la batalla", JOptionPane.INFORMATION_MESSAGE);
            } else if (npcScore > playerScore) {
                npcWins++;
                JOptionPane.showMessageDialog(mainPanel,
                        "¡La carta del NPC " + (npcIndex + 1) + " (" + npcCard.getName() + ", ATK: " + npcScore +
                                ") derrota a la carta del Jugador " + playerPosition + " (" + playerCard.getName() + ", ATK: " + playerScore + ")!\n" +
                                "Victorias: Jugador " + playerWins + ", NPC " + npcWins,
                        "Resultado de la batalla", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainPanel,
                        "¡Empate! La carta del Jugador " + playerPosition + " (" + playerCard.getName() +
                                ") y la carta del NPC " + (npcIndex + 1) + " (" + npcCard.getName() +
                                ") (ATK: " + playerScore + " vs. ATK: " + npcScore + ") tienen la misma fuerza.\n" +
                                "Victorias: Jugador " + playerWins + ", NPC " + npcWins,
                        "Resultado de la batalla", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        // Verificar si el juego ha terminado
        checkGameOver();
    }

    /**
     * Obtiene los índices de las cartas activas (no nulas) en un arreglo.
     * @param cards Arreglo de cartas
     * @return Lista de índices de cartas activas
     */
    private List<Integer> getActiveCardIndices(Cartas[] cards) {
        List<Integer> active = new ArrayList<>();
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null) {
                active.add(i);
            }
        }
        return active;
    }

    /**
     * Limpia la imagen y texto de una carta en la UI (no usado en el sistema actual).
     * @param jugador 1 para humano, 2 para NPC
     * @param posicion Posición de la carta (1, 2, 3)
     */
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

    /**
     * Verifica si el juego ha terminado (2 victorias para un jugador o empate).
     */
    private void checkGameOver() {
        if (playerWins >= 2 && npcWins >= 2) {
            JOptionPane.showMessageDialog(mainPanel, "¡Empate! Ambos jugadores alcanzaron 2 victorias.", "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
            disableGame();
        } else if (playerWins >= 2) {
            JOptionPane.showMessageDialog(mainPanel, "¡El jugador gana! Alcanzó 2 victorias.", "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
            disableGame();
        } else if (npcWins >= 2) {
            JOptionPane.showMessageDialog(mainPanel, "¡NPC gana! Alcanzó 2 victorias.", "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
            disableGame();
        }
    }

    /**
     * Desactiva todos los botones al finalizar el juego.
     */
    private void disableGame() {
        AtaqueButton1.setEnabled(false);
        AtaqueButton2.setEnabled(false);
        AtaqueButton3.setEnabled(false);
        DefensaButton1.setEnabled(false);
        DefensaButton2.setEnabled(false);
        DefensaButton3.setEnabled(false);
        RandomUp.setEnabled(false);
        RandomDown.setEnabled(false);
    }

    /**
     * Obtiene el JLabel de la imagen para una carta.
     * @param jugador 1 para humano (CardDown*), 2 para NPC (CardUp*)
     * @param posicion Posición de la carta (1, 2, 3)
     * @return JLabel de la imagen o null si la posición es inválida
     */
    private JLabel getImgLabel(int jugador, int posicion) {
        if (jugador == 1) { // Jugador humano (abajo)
            return switch (posicion) {
                case 1 -> ImgDown1;
                case 2 -> ImgDown2;
                case 3 -> ImgDown3;
                default -> null;
            };
        } else { // NPC (arriba)
            return switch (posicion) {
                case 1 -> ImgUp1;
                case 2 -> ImgUp2;
                case 3 -> ImgUp3;
                default -> null;
            };
        }
    }

    /**
     * Obtiene el JLabel de los datos para una carta.
     * @param jugador 1 para humano (CardDown*), 2 para NPC (CardUp*)
     * @param posicion Posición de la carta (1, 2, 3)
     * @return JLabel de los datos o null si la posición es inválida
     */
    private JLabel getNombreLabel(int jugador, int posicion) {
        if (jugador == 1) { // Jugador humano (abajo)
            return switch (posicion) {
                case 1 -> NombreDown1;
                case 2 -> NombreDown2;
                case 3 -> NombreDown3;
                default -> null;
            };
        } else { // NPC (arriba)
            return switch (posicion) {
                case 1 -> Nombreup1;
                case 2 -> Nombreup2;
                case 3 -> Nombreup3;
                default -> null;
            };
        }
    }

    /**
     * Muestra los datos e imagen de una carta en la interfaz gráfica.
     * @param carta Objeto Cartas con los datos
     * @param jugador 1 para humano, 2 para NPC
     * @param posicion Posición de la carta (1, 2, 3)
     */
    public void extraer_datos(Cartas carta, int jugador, int posicion) {
        // Construir texto HTML con los datos de la carta
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

        // Cargar y escalar la imagen de la carta
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
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }

    /**
     * Punto de entrada principal para la aplicación.
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        // Configurar y mostrar la ventana principal
        JFrame frame = new JFrame("YugiGUI - Batalla de Cartas");
        frame.setContentPane(new YugiGUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(false);
        frame.setVisible(true);
    }
}