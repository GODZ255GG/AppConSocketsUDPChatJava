import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;

public class MulticastUDPChatGUI {
    
    static String nombre;

    static volatile boolean terminado = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                crearInterfazUsuario();
            }
        });
    }

    @SuppressWarnings("deprecation")
    public static void crearInterfazUsuario() {
        // Ventana para ingresar el nombre
        JFrame frame = new JFrame("Ingresar nombre");
        nombre = JOptionPane.showInputDialog(frame, "Ingresa tu nombre:");

        if (nombre == null || nombre.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre de usuario no puede estar vacio.");
            System.exit(1);
        }

        // Ventana principal del chat
        JFrame chatFrame = new JFrame("Chat UDP Multicast");
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.setSize(400, 300);

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        JLabel usuarioLabel = new JLabel("Usuario: " + nombre);

        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("Enviar");

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(usuarioLabel, BorderLayout.SOUTH);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatFrame.getContentPane().setLayout(new BorderLayout());
        chatFrame.getContentPane().add(chatPanel, BorderLayout.CENTER);
        chatFrame.getContentPane().add(inputPanel, BorderLayout.SOUTH);

        try {
            int puerto = 8080;
            InetAddress grupo = InetAddress.getByName("224.0.0.0");
            MulticastSocket socket = new MulticastSocket(puerto); 

            socket.joinGroup(grupo);

            String mensajeInicio = "Se ha unido al chat\n";
            enviarMensaje(socket, grupo, puerto, mensajeInicio, nombre);

            Thread hilo = new Thread(new HiloLectura(socket, grupo, puerto, chatArea));
            hilo.start();

            sendButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String message = messageField.getText();
                    enviarMensaje(socket, grupo, puerto, message, nombre);
                    messageField.setText("");
                }
            });

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }

        chatFrame.setVisible(true);
    }

    @SuppressWarnings("deprecation")
    static void enviarMensaje(MulticastSocket socket, InetAddress grupo, int puerto, String mensaje, String nombre) {
        try {
            if (mensaje.equalsIgnoreCase("Adios")) {
                terminado = true;

                mensaje = nombre + ": Ha terminado la conexion.\n";
                byte[] bufer = mensaje.getBytes();
                DatagramPacket mensajeSalida = new DatagramPacket(bufer, bufer.length, grupo, puerto);
                socket.send(mensajeSalida);

                socket.leaveGroup(grupo);
                socket.close();
                System.exit(0);
            }

            mensaje = nombre + ": " + mensaje + "\n";
            byte[] bufer = mensaje.getBytes();
            DatagramPacket datagram = new DatagramPacket(bufer, bufer.length, grupo, puerto);
            socket.send(datagram);
        } catch (IOException e) {
            System.out.println("Error al enviar el mensaje: " + e.getMessage());
        }
    }
}

class HiloLectura implements Runnable {
    private MulticastSocket socket;
    private InetAddress grupo;
    private int puerto;
    private JTextArea chatArea;

    HiloLectura(MulticastSocket socket, InetAddress grupo, int puerto, JTextArea chatArea) {
        this.socket = socket;
        this.grupo = grupo;
        this.puerto = puerto;
        this.chatArea = chatArea;
    }

    public void run() {
        byte[] bufer = new byte[1024];

        while (!MulticastUDPChatGUI.terminado) {
            try {
                DatagramPacket mensajeEntrada = new DatagramPacket(bufer, bufer.length, grupo, puerto);
                socket.receive(mensajeEntrada);

                final String linea = new String(bufer, 0, mensajeEntrada.getLength());

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        chatArea.append(linea);
                    }
                });
            } catch (IOException e) {
                System.out.println("Error al recibir el mensaje: " + e.getMessage());
            }
        }
    }
}
