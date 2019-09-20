import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

class Client{

   public static void main(String[] args)  throws UnknownHostException, InterruptedException{

      Scanner in = new Scanner(System.in);  // Create a Scanner object

      MulticastReceiver mr = new MulticastReceiver();

      PeerReceiver pr = new PeerReceiver(mr);
      pr.setupAudioStream();

      try{
         while(true){

            pr.play();
         }
      }catch (Exception ex) {
         ex.printStackTrace();
      }

   }
}
