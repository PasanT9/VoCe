import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Session{

  DatagramSocket socket;
  MulticastSocket socket0;
  InetAddress ip;
  int port;

  public Session(){
    
  }


  public Session(DatagramSocket socket, InetAddress ip, int port){
    this.socket=socket;
    this.ip = ip;
    this.port = port;

  }

  public Session(MulticastSocket socket, InetAddress ip){
    this.socket0=socket;
    this.ip = ip;
  }
}
