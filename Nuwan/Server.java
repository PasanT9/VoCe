import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

class Server{

   public static void main(String[] args)  throws UnknownHostException, InterruptedException{

      Scanner in = new Scanner(System.in);

      // Objects required to transmit/send audio stream
      MulticastSender ms = new MulticastSender();
      PeerSender ps = new PeerSender(ms);

      // Thread for transmitter
      Thread threadSender = new Thread(ps);
      threadSender.start();


      // Setup Transmitter by cnfiguring transmission stream
      ps.setupAudioStream();

      try{
         while(true){
            System.out.print("\n\nEnter (1=start, 0=stop): ");
            int cmd = in.nextInt();

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

      in.close();
   }
}
