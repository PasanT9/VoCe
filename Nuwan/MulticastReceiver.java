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

  byte[] buf = new byte[500];

  public MulticastReceiver()  throws UnknownHostException{
    // Address and the port are hardcoded for now
    INET_ADDR = "224.0.0.3";
    PORT = 50000;
    this.addr = InetAddress.getByName(INET_ADDR);

    try{
      //Initialize MulticastSocket on given port and join to it as a client
      clientSocket = new MulticastSocket(PORT);
      clientSocket.joinGroup(addr);

    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  // Receive a byte stream
  public byte[] receive(){
    try{
      DatagramPacket msgPacket = new DatagramPacket(buf, 500);
      clientSocket.receive(msgPacket);
      //String msg = new String(buf, 0, buf.length);
      System.out.println(buf[55]);
      return buf;

    } catch (IOException ex) {
      ex.printStackTrace();
    }

    // Send when an error occurred
    //REM: Pasan, please correct
    return buf;
  }
}
