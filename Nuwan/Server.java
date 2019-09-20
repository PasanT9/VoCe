import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

class Server{

   public static void main(String[] args)  throws UnknownHostException, InterruptedException{

      MulticastSender ms = new MulticastSender();
      PeerSender ps = new PeerSender(ms);
      Scanner in = new Scanner(System.in);  // Create a Scanner object

      ps.setupAudioStream();

      try{
         while(true){
            System.out.print("\n\nEnter (1=start, 0=stop): ");

            int cmd = in.nextInt();  // Read user input

            if(cmd==1){
               System.out.println("Send Start");
               ps.sendStart();
            }else{
               System.out.println("Send Stop");
               ps.sendStop();
               Thread.sleep(50);
            }
         }
      }catch (Exception ex) {
         ex.printStackTrace();
      }

   }
}
