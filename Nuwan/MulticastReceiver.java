import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MulticastReceiver{

   final String INET_ADDR;
   final int PORT;
   private InetAddress addr;
   private MulticastSocket clientSocket;

   byte[] buf = new byte[256];

   public MulticastReceiver()  throws UnknownHostException{ //
      INET_ADDR = "224.0.0.3";
      PORT = 20000;

      this.addr = InetAddress.getByName(INET_ADDR);

      try{
         clientSocket = new MulticastSocket(PORT);
         clientSocket.joinGroup(addr);

      } catch (IOException ex) {
         ex.printStackTrace();
      }
   }

   public DatagramPacket receive(){
      try{
         DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
         clientSocket.receive(msgPacket);
         //String msg = new String(buf, 0, buf.length);
         //System.out.println("Socket received msg: " + msg);
         return msgPacket;
      } catch (IOException ex) {
         ex.printStackTrace();
      }
      // Send when an error occurred
      return new DatagramPacket(buf, 0);
   }
}
