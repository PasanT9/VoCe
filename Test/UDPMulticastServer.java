import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class UDPMulticastServer implements Runnable{

public void run() {
  try{
    DatagramSocket socket = new DatagramSocket();
    InetAddress group = InetAddress.getByName("230.0.0.3");
    int port = 4321;

    Session peer = new Session(socket, group, port);
    AudioSession audio = new AudioSession(peer);
    audio.captureAudio();
    audio.capture();

    socket.close();
  }
  catch(IOException ex){
    ex.printStackTrace();
  }

}
}
