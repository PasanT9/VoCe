import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MulticastSocketServer {

    final static String INET_ADDR = "224.0.0.3";
    final static int PORT = 20000;

    public static void main(String[] args) throws UnknownHostException, InterruptedException {

        // Get the address that we are going to connect to.
        InetAddress addr = InetAddress.getByName(INET_ADDR);
        int i=0;

        // Open a new DatagramSocket, which will be used to send the data.
        try (DatagramSocket serverSocket = new DatagramSocket()) {

            //for (int i = 0; i < 500; i++) {
            while(true){
                String msg = ""+ i++;

                // Create a packet that will contain the data
                // (in the form of bytes) and send it.
                DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, addr, PORT);
                serverSocket.send(msgPacket);

                System.out.println("Server sent packet with msg: " + msg);
                Thread.sleep(1000);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
