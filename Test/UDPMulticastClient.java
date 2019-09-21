import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

public class UDPMulticastClient implements Runnable {

  public static void main(String[] args) {
    Thread t=new Thread(new UDPMulticastClient());
    t.start();
  }

  @Override
  public void run(){

    try {
      MulticastSocket socket=new MulticastSocket(4321);
      InetAddress group = InetAddress.getByName("230.0.0.0");

      Session peer = new Session(socket, group);
      AudioSession audio = new AudioSession(peer);
      socket.joinGroup(group);

      audio.captureAudio();
      audio.play();

      socket.leaveGroup(group);
      socket.close();

    }catch(IOException ex){
      ex.printStackTrace();
    }
  }
}
