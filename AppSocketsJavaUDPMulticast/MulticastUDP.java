import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Scanner;

public class MulticastUDP{
    @SuppressWarnings("deprecation")
    public static void main(String [] args){
        try {
           int puerto = 8080;
           InetAddress grupo = InetAddress.getByName("224.0.0.0");
           MulticastSocket socket = new MulticastSocket(puerto); 

           socket.joinGroup(grupo);

           Scanner scan = new Scanner(System.in);
           System.out.println("Envie un mensaje al grupo: ");
           String msj = scan.nextLine();

           byte[] m = msj.getBytes();
           DatagramPacket mensajeSalida = new DatagramPacket(m, m.length, grupo, puerto);
           socket.send(mensajeSalida);

           byte[] bufer = new byte[1024];
           String linea;

           while(true){
            DatagramPacket mensajeEntrada = new DatagramPacket(bufer, bufer.length);
            socket.receive(mensajeEntrada);

            linea = new String(mensajeEntrada.getData(), 0, mensajeEntrada.getLength());
            System.out.println("Recibido: " + linea);

            if(linea.equalsIgnoreCase("Adios")){
                socket.leaveGroup(grupo);
                break;
            }
           }
           scan.close();
           socket.close();
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){
            System.out.println("IO: " + e.getMessage());
        }
    }
}