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

public class PeerSender extends Thread{
   // new
   MulticastSender ms;
   //Thread broadcastThread;
   boolean setupCompleted; // this will be true if setupAudioStream() is called
   boolean enableBroadcast;

   Thread broadcastThread = new Thread("Broadcast Thread") {
      public void run(){
         System.out.println("Broadcast thread is ready now");
         while(true){
            if(enableBroadcast==true){
               captureAndTransmit();
            }else{
               try{
                  broadcastThread.sleep(500);
               }catch(Exception e){

               }
            }
            //Thread.sleep(100);
            //System.out.println(".");
         }
      }
   };

   // Old
   static byte[][] memBuffer = new byte[16][];
   static int packetCount = 0;

   boolean stopCapture = false;
   ByteArrayOutputStream byteArrayOutputStream;
   AudioFormat audioFormat;
   TargetDataLine targetDataLine;
   AudioInputStream audioInputStream;
   SourceDataLine sourceDataLine;
   byte tempBuffer[] = new byte[500];

   public PeerSender(MulticastSender multicastSender){
      this.ms = multicastSender;
      initializeMemBuffer();
      setupCompleted = false;
      enableBroadcast = false;
      broadcastThread.start();
   }
   public void setupAudioStream() {
      try {
         // Get available mixers and print them to STDOUT
         Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
         System.out.println("Available mixers:");
         Mixer mixer = null;
         for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
            System.out.println(cnt + " " + mixerInfo[cnt].getName());
            mixer = AudioSystem.getMixer(mixerInfo[cnt]);

            Line.Info[] lineInfos = mixer.getTargetLineInfo();
            if (lineInfos.length >= 1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
               System.out.println(cnt + " Mic is supported!");
               break;
            }
         }

         // Get the audio format
         audioFormat = getAudioFormat();
         DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

         targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
         targetDataLine.open(audioFormat);
         targetDataLine.start();

         DataLine.Info dataLineInfo1 = new DataLine.Info(SourceDataLine.class, audioFormat);
         sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo1);
         sourceDataLine.open(audioFormat);
         sourceDataLine.start();

         //Setting the maximum volume
         FloatControl control = (FloatControl)sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
         control.setValue(control.getMaximum());

         setupCompleted = true;

      } catch (LineUnavailableException e) {
         System.out.println(e);
         System.exit(0);
      }

   }

   public void sendStart(){
      if(setupCompleted==false){
         setupAudioStream();
      }
      System.out.println("Broadcast thread is started now");
      enableBroadcast = true;
   }
   public void sendStop(){
      enableBroadcast = false;
      //broadcastThread.stop();
   }

   private AudioFormat getAudioFormat() {
      float sampleRate = 16000.0F;
      int sampleSizeInBits = 16;
      int channels = 2;
      boolean signed = true;
      boolean bigEndian = true;
      return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
   }
   private static void initializeMemBuffer(){
      for(int i=0;i<16;++i){
         memBuffer[i] = null;
      }
   }

   private void captureAndTransmit() {

      //byteArrayOutputStream = new ByteArrayOutputStream();
      //stopCapture = false;
      try {
         int seq = 0;
         while (enableBroadcast) {
            targetDataLine.read(tempBuffer, 0, tempBuffer.length);  //capture sound into tempBuffer
            seq = seq%16;
            tempBuffer[499] = (byte)seq++;
            //System.out.println(tempBuffer[499]);
            ms.write(tempBuffer);
         }
         //byteArrayOutputStream.close();
      } catch (Exception e) {
         System.out.println(e);
         System.exit(0);
      }
   }

}
