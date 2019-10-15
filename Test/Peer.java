import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Peer{


   public static void main(String[] args) throws UnknownHostException, InterruptedException {
      
      Scanner in = new Scanner(System.in);

      UDPMulticastServer sender = new UDPMulticastServer();
      UDPMulticastClient receiver = new UDPMulticastClient();

      Thread threadSender = new Thread(sender);
      Thread threadReceiver = new Thread(receiver);

      threadSender.start();
      //threadReceiver.start();

      try{
         while(true){
            System.out.print("\n\nEnter (1=start, 0=stop): ");
            int cmd = in.nextInt();

            if(cmd==1){
               System.out.println("Send Start");
               threadSender.start();
            }else{
               System.out.println("Send Stop");
               //ps.sendStop();
               Thread.sleep(50);
            }
         }
      }catch (Exception ex) {
         ex.printStackTrace();
      }

      in.close();
   }
}
