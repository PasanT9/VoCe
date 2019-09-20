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

class Peer{

   static byte[][] memBuffer = new byte[16][];
   static int packetCount = 0;

   boolean setupCompleted; // this will be true if setupAudioStream() is called

   boolean stopCapture = false;
   ByteArrayOutputStream byteArrayOutputStream;
   AudioFormat audioFormat;
   TargetDataLine targetDataLine;
   AudioInputStream audioInputStream;
   SourceDataLine sourceDataLine;
   byte tempBuffer[] = new byte[500];

   public Peer(){
      // Empty constructor
   }

   // Return audio format
   protected AudioFormat getAudioFormat() {
      float sampleRate = 16000.0F;
      int sampleSizeInBits = 16;
      int channels = 2;
      boolean signed = true;
      boolean bigEndian = true;
      return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
   }

   // Initialize the memory buffer with null data
   protected static void initializeMemBuffer(){
      for(int i=0;i<16;++i){
         memBuffer[i] = null;
      }
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


}
