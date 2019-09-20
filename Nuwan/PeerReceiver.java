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

import java.net.DatagramPacket;

public class PeerReceiver extends Peer implements Runnable{

   MulticastReceiver mr;

   public void run(){
      // Need to implement, receive thread
   }

   public PeerReceiver(MulticastReceiver multicastReceiver){
      this.mr = multicastReceiver;
      initializeMemBuffer();
      setupCompleted = false;
   }

   public void play(){

      // -----------------------------------------------
      // readCount = dis.read(tempBuffer);
      // This isn't working. Pasan, please check -------
      byteArrayOutputStream = new ByteArrayOutputStream();
      stopCapture = false;
      // ------------------------------------------------
      try {
         int readCount;
         while (!stopCapture) {

            //DatagramPacket msgPacket = mr.receive();
            tempBuffer = mr.receive();
            readCount = tempBuffer.length;

            if (readCount > 0 && (tempBuffer[499] >= 0 && tempBuffer[499] <= 15)) {
               int currentPacket = tempBuffer[499];
               System.out.println("Expected: "+packetCount+" "+"Arrived: "+currentPacket);
               if(currentPacket != packetCount){
                  System.out.println("Not in Sequence");
                  if(memBuffer[packetCount] == null){
                     System.out.println("Not in Buffer");
                     memBuffer[currentPacket] = tempBuffer;
                     int packets=0;
                     do{
                        System.out.println("Wait packets for: " + packetCount);

                        // -----------------------------------------------
                        // readCount = dis.read(tempBuffer);
                        // This isn't working. Pasan, please check -------
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        stopCapture = false;
                        // ------------------------------------------------

                        ++packets;
                        if(readCount > 0 && (tempBuffer[499] >= 0 && tempBuffer[499] <= 15)){
                           currentPacket = tempBuffer[499];
                           memBuffer[currentPacket] = tempBuffer;
                           System.out.println("Into buffer: " + currentPacket);
                        }
                     }while(currentPacket != packetCount && packets < 5);
                     if(packets == 5){
                        System.out.println("Packet Drop");
                        ++packetCount;
                        packetCount %= 16;
                        continue;
                     }
                  }
                  else{
                     System.out.println("Exist in Buffer");
                     tempBuffer = memBuffer[packetCount];
                     memBuffer[packetCount] = null;
                  }
               }
               System.out.println(tempBuffer[499]);
               byteArrayOutputStream.write(tempBuffer, 0, readCount);
               System.out.println("Playing: "+tempBuffer[499]);
               sourceDataLine.write(tempBuffer, 0, 500);   //playing audio available in tempBuffer
               ++packetCount;
               packetCount %= 16;
            }
         }
         byteArrayOutputStream.close();

      } catch (IOException e) {
         System.out.println(e);
         System.exit(0);
      }
   }

}
