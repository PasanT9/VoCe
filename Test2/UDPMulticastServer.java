import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class UDPMulticastServer implements Runnable{

  String ipAddress;

  public UDPMulticastServer(String ipAddress){
    this.ipAddress = ipAddress;
  }

  public void run() {
    DatagramSocket socket = null;
    try{

      //Create a Datagram socket and connect
      socket = new DatagramSocket();
      InetAddress group = InetAddress.getByName(this.ipAddress);
      int port = 4321;

      //Create a session object to store connection data
      Session peer = new Session(socket, group, port);

      //Pass session data to AudioSession object
      AudioSession audio = new AudioSession(peer);

      //Mixer setting
      audio.captureAudio();

      //Start capturing audio
      audio.capture();
    }
    catch(IOException ex){
    }
    finally{
      socket.close();
    }

  }
}
