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

// Server class
public class PeerServer{

   final DataInputStream dis;
   final DataOutputStream dos;
   final Socket s;

   static byte[][] memBuffer = new byte[16][];
   static int packetCount = 0;

   boolean stopCapture = false;
   ByteArrayOutputStream byteArrayOutputStream;
   AudioFormat audioFormat;
   TargetDataLine targetDataLine;
   AudioInputStream audioInputStream;
   SourceDataLine sourceDataLine;
   byte tempBuffer[] = new byte[500];

   public PeerServer(Socket s, DataInputStream dis, DataOutputStream dos){
      this.s = s;
      this.dis = dis;
      this.dos = dos;
   }

   private AudioFormat getAudioFormat() {
      float sampleRate = 16000.0F;
      int sampleSizeInBits = 16;
      int channels = 2;
      boolean signed = true;
      boolean bigEndian = true;
      return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
   }

   private void captureAudio() {

      try {
         Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();    //get available mixers
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

         audioFormat = getAudioFormat();     //get the audio format
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

         captureAndPlay(); //playing the audio

      } catch (LineUnavailableException e) {
         System.out.println(e);
         System.exit(0);
      }

   }

   private void captureAndPlay() {

      byteArrayOutputStream = new ByteArrayOutputStream();
      stopCapture = false;
      try {
         int readCount;
         while (!stopCapture) {
            readCount = dis.read(tempBuffer);

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
                        readCount = dis.read(tempBuffer);
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
   private static void initializeMemBuffer(){
      for(int i=0;i<16;++i){
         memBuffer[i] = null;
      }
   }

   public static void main(String[] args) throws IOException{

      ServerSocket ss = new ServerSocket(5056);
      Socket s = null;
      try{
         s = ss.accept();

         System.out.println("A new client is connected : " + s);

         // obtaining input and out streams
         DataInputStream dis = new DataInputStream(s.getInputStream());
         DataOutputStream dos = new DataOutputStream(s.getOutputStream());

         System.out.println("Assigning new thread for this client");

         PeerServer peer = new PeerServer(s,dis,dos);
         initializeMemBuffer();
         peer.captureAudio();

         s.close();
         dis.close();
         dos.close();

      }
      catch (Exception e){
         s.close();
         e.printStackTrace();
      }
   }
}
