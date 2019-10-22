import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

public class UDPMulticastClient implements Runnable {

  String ipAddress;

  public UDPMulticastClient(String ipAddress){
    this.ipAddress = ipAddress;
  }

  @Override
  public void run(){
    MulticastSocket socket = null;
    InetAddress group = null;
    try {

      //Create a multicast socket and join multicast group
      socket=new MulticastSocket(4321);
      group = InetAddress.getByName(this.ipAddress);

      //Create a Session object to store connection data
      Session peer = new Session(socket, group);

      //Pass connection data to AudioSession object
      AudioSession audio = new AudioSession(peer);
      socket.joinGroup(group);

      //Mixer settings
      audio.captureAudio();

      //Start playing audio
      audio.play();

    }catch(IOException ex){

      //If an error occured
      System.out.println("Program exited with an error!!!");
      System.out.println("To fix try folllowing:");
      System.out.println("\tCheck IP address (ex.233.0.0.1)");
      System.out.println("\tCheck network connection");
      System.exit(0);
    }
    finally{
      try{
        socket.leaveGroup(group);
      }
      catch(Exception ex){
        ex.printStackTrace();
      }
      socket.close();
    }
  }
}
