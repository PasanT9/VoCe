import java.util.*;
import java.io.IOException;

public abstract class Peer implements Runnable{

  public static void main(String[] args){

    //Create Multicast Client and Serv threads
    if(args.length == 1){
      Thread p2 = new Thread(new UDPMulticastServer(args[0]));
      p2.start();
      Thread p1=new Thread(new UDPMulticastClient(args[0]));
      p1.start();
    }
    else{
      System.out.println("Incorrect format(java Peer <IP address>)");
    }
  }
}
