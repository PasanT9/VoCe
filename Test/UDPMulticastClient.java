import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

import java.net.UnknownHostException;

public class UDPMulticastClient implements Runnable {

   private MulticastSocket socket;
   private Session peer;
   AudioSession audio;
   InetAddress group;

   public UDPMulticastClient()  throws UnknownHostException, InterruptedException{

      try{
         socket=new MulticastSocket(4321);
         group = InetAddress.getByName("230.0.0.0");
         peer = new Session(socket, group);
         audio = new AudioSession(peer);

      } catch (IOException ex) {
         ex.printStackTrace();
      }

   }

   @Override
   public void run(){
      try {
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
