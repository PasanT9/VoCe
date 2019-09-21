import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

//extends Peer
public class PeerSender extends Peer implements Runnable{
   // new

   MulticastSender ms;

   // Only broadcast when this flag is marked as true
   boolean enableBroadcast;

   // Runnable thread for the broadcast
   public void run(){
      System.out.println("Broadcast thread is ready now"); // Initialization message

      while(true){
         // This block is looping in a separate thread

         try{
            if(enableBroadcast==true){
               // Run this function to boradcast.
               // The function has it's own while loop, and it execute while(enableBroadcast)
               captureAndTransmit();
            }else{
               // Sleep for 0.5 second if broadcast isn't enabled yet
               Thread.sleep(500);
            }
         }catch(Exception e){

         }
      }
   }

   public PeerSender(MulticastSender multicastSender){
      this.ms = multicastSender;
      initializeMemBuffer();
      setupCompleted = false;
      enableBroadcast = false;
   }

   // Start sending
   public void sendStart(){
      if(setupCompleted==false){
         setupAudioStream();
      }
      System.out.println("Broadcast thread is started now");
      enableBroadcast = true;
   }

   // Stop sending
   public void sendStop(){
      enableBroadcast = false;
   }

   private void captureAndTransmit() {

      try {
         int seq = 0;
         while (enableBroadcast) {
            targetDataLine.read(tempBuffer, 0, tempBuffer.length);  //capture sound into tempBuffer
            seq = seq%16;
            tempBuffer[100] = (byte)seq++;
            System.out.println(tempBuffer[100]);
            ms.write(tempBuffer);

         }
      } catch (Exception e) {
         System.out.println(e);
         System.exit(0);
      }
   }

}
