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

   public MulticastSender()  throws UnknownHostException{ //
      INET_ADDR = "224.0.0.3";
      PORT = 20000;

      this.addr = InetAddress.getByName(INET_ADDR);
      //serverSocket = new DatagramSocket();

      try{
         serverSocket = new DatagramSocket();

      } catch (IOException ex) {
         ex.printStackTrace();
      }
   }

   public void send(String msg){
      try{
         DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, addr, PORT);
         serverSocket.send(msgPacket);
         System.out.println("Server sent packet with msg: " + msg);
      }catch (IOException ex) {
         ex.printStackTrace();
      }
   }


   public void write(byte[] buffer){
      String msg = new String(buffer);

      try{
         DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, addr, PORT);
         serverSocket.send(msgPacket);
         //System.out.println("Server sent packet with msg: " + msg);
      }catch (IOException ex) {
         ex.printStackTrace();
      }
   }
}
