import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MulticastSender{

   final String INET_ADDR;
   final int PORT;
   private InetAddress addr;
   private DatagramSocket serverSocket;

   public MulticastSender()  throws UnknownHostException{
      // Address and the port are hardcoded for now
      INET_ADDR = "224.0.0.3";
      PORT = 20000;
      this.addr = InetAddress.getByName(INET_ADDR);

      try{
         // Create a Datagram Socket which can push data into the multicast group
         serverSocket = new DatagramSocket();

      } catch (IOException ex) {
         ex.printStackTrace();
      }
   }

   // Push a message into multicast group
   public void send(String msg){
      try{
         DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, addr, PORT);
         serverSocket.send(msgPacket);
         System.out.println("Server sent packet with msg: " + msg);
      }catch (IOException ex) {
         ex.printStackTrace();
      }
   }

   // Push the data buffer into multicast group
   public void write(byte[] buffer){
      String msg = new String(buffer);

      try{
         DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, addr, PORT);
         serverSocket.send(msgPacket);
      }catch (IOException ex) {
         ex.printStackTrace();
      }
   }
}
