
public class Peer{
  public static void main(String[] args) {
    Thread listen=new Thread(new Runnable(){
      @Override
      public void run(){
        UDPMulticastServer server = new UDPMulticastServer();
      }
    });
    listen.start();
    Thread play=new Thread(new Runnable(){
      @Override
      public void run(){
        UDPMulticastClient client = new UDPMulticastClient();
      }
    });
    play.start();
  }
}
