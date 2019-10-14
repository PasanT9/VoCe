import java.util.*;
import java.io.IOException;

public abstract class Peer implements Runnable{

  public static void main(String[] args){
    Thread p1=new Thread(new UDPMulticastClient());
    p1.start();
    Thread p2 = new Thread(new UDPMulticastServer());
    p2.start();
  }
}
