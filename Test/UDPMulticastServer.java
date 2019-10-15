import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

import java.io.IOException;
import java.net.UnknownHostException;

public class UDPMulticastServer implements Runnable{
   private Session peer;
   private AudioSession audio;
   private DatagramSocket socket;

   public UDPMulticastServer()  throws UnknownHostException, InterruptedException{
      // throws IOException

      try{
         // Create a Datagram Socket which can push data into the multicast group
         socket = new DatagramSocket();

      } catch (IOException ex) {
         ex.printStackTrace();
      }

      InetAddress group = InetAddress.getByName("230.0.0.0");
      int port = 4321;

      peer = new Session(socket, group, port);
      audio = new AudioSession(peer);

      /*audio.captureAudio();
      audio.capture();
      socket.close();*/
   }

   @Override
   public void run(){

      audio.captureAudio();
      audio.capture();
      socket.close();
   }
}
