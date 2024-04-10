import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Scanner;

public class MulticastUDPChat {
    
    static String nombre;

    static volatile boolean terminado = false;

    @SuppressWarnings("deprecation")
    public static void main(String [] args){
    
        try {
           int puerto = 8080;
           InetAddress grupo = InetAddress.getByName("224.0.0.0");
           MulticastSocket socket = new MulticastSocket(puerto); 

           Scanner scanner = new Scanner(System.in);
           System.out.print("Ingresa tu nombre: ");
           nombre = scanner.nextLine();

           socket.joinGroup(grupo);

           Thread hilo = new Thread(new HiloLectura(socket, grupo, puerto));
           hilo.start();

           System.out.println("Puede comenzar a escribir mensajes al grupo...\n");

           byte[] bufer = new byte[1024];
           String linea;

           while (true) {
            linea = scanner.nextLine();
            if(linea.equalsIgnoreCase("Adios")){
                terminado = true;

                linea = nombre + ": Ha terminado la conexion.\n";
                bufer = linea.getBytes();
                DatagramPacket mensajeSalida = new DatagramPacket(bufer, bufer.length, grupo, puerto);
                socket.send(mensajeSalida);

                socket.leaveGroup(grupo);
                socket.close();
                break;
            }

            linea = nombre + ": " + linea + "\n";
            bufer = linea.getBytes();
            DatagramPacket datagram = new DatagramPacket(bufer, bufer.length, grupo, puerto);
            socket.send(datagram);
           }
           scanner.close();
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){
            System.out.println("IO: " + e.getMessage());
        }
    }
}

class HiloLectura implements Runnable{
    private MulticastSocket socket;
    private InetAddress grupo;
    private int port;

    HiloLectura(MulticastSocket socket, InetAddress grupo, int port){
        this.socket = socket;
        this.grupo = grupo;
        this.port = port;
    }

    public void run(){
        byte[] bufer = new byte[1024];
        String linea;

        while(!MulticastUDPChat.terminado){
            try {
                DatagramPacket mensajeEntrada = new DatagramPacket(bufer, bufer.length, grupo, port);
                socket.receive(mensajeEntrada);

                linea = new String(bufer, 0, mensajeEntrada.getLength());

                if(!linea.startsWith(MulticastUDPChat.nombre))
                    System.out.print(linea);
            } catch (Exception e) {
                System.out.println("Comunicacion y socket cerrados.");
            }
        }
    }
}

