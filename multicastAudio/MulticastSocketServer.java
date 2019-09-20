import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

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


public class MulticastSocketServer {

   final static String INET_ADDR = "224.0.0.3";
   final static int PORT = 20000;

   static byte[][] memBuffer = new byte[16][];
   static int packetCount = 0;
   byte tempBuffer[] = new byte[500];

   boolean stopCapture = false;
   ByteArrayOutputStream byteArrayOutputStream;
   AudioFormat audioFormat;
   TargetDataLine targetDataLine;
   AudioInputStream audioInputStream;
   SourceDataLine sourceDataLine;

   public MulticastSocketServer(){

   }

   public void run(){
      InetAddress addr = InetAddress.getByName(INET_ADDR);
      int i=0;

      try (DatagramSocket serverSocket = new DatagramSocket()) {
         System.out.println("A new client is connected : " + serverSocket);

         //------------
         captureAudio();

         // ---------------------------------------------

      } catch (IOException ex) {
         ex.printStackTrace();
      }
   }

   public static void main(String[] args) throws UnknownHostException, InterruptedException {

      MulticastSocketServer ms = new MulticastSocketServer();

      ms.run();
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

         //captureAndPlay(); //playing the audio

      } catch (LineUnavailableException e) {
         System.out.println(e);
         System.exit(0);
      }

   }

}
