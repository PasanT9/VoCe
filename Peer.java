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
public class Peer
{

  final DataInputStream dis;
  final DataOutputStream dos;
  final Socket s;

  boolean stopCapture = false;
  ByteArrayOutputStream byteArrayOutputStream;
  AudioFormat audioFormat;
  TargetDataLine targetDataLine;
  AudioInputStream audioInputStream;
  SourceDataLine sourceDataLine;
  byte tempBuffer[] = new byte[500];

  public Peer(Socket s, DataInputStream dis, DataOutputStream dos)
  {
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
              System.out.println("->"+tempBuffer);

              if (readCount > 0) {
                  byteArrayOutputStream.write(tempBuffer, 0, readCount);
                  sourceDataLine.write(tempBuffer, 0, 500);   //playing audio available in tempBuffer
              }
          }
          byteArrayOutputStream.close();
      } catch (IOException e) {
          System.out.println(e);
          System.exit(0);
      }
  }



    public static void main(String[] args) throws IOException
    {
        // server is listening on port 5056
        ServerSocket ss = new ServerSocket(5056);

        //while (true)
        //{
            Socket s = null;

            try
            {
                s = ss.accept();

                System.out.println("A new client is connected : " + s);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                System.out.println("Assigning new thread for this client");

                // create a new thread object
                /*Thread listenThread = new ClientHandler(true, s, dis, dos);
                Thread playThread = new ClientHandler(false, s,dis,dos);

                // Invoking the start() method
                listenThread.start();
                playThread.start();*/
                Peer peer = new Peer(s,dis,dos);
                peer.captureAudio();

                s.close();
                dis.close();
                dos.close();

            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        //}
    }
}


// ClientHandler class
/*class ClientHandler extends Thread
{

    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;

    boolean stopCapture = false;
    ByteArrayOutputStream byteArrayOutputStream;
    AudioFormat audioFormat;
    TargetDataLine targetDataLine;
    AudioInputStream audioInputStream;
    SourceDataLine sourceDataLine;
    byte tempBuffer[] = new byte[500];



    // Constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos)
    {
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
                readCount = targetDataLine.read(tempBuffer, 0, tempBuffer.length);  //capture sound into tempBuffer
                if (readCount > 0) {
                    byteArrayOutputStream.write(tempBuffer, 0, readCount);
                    sourceDataLine.write(tempBuffer, 0, 500);   //playing audio available in tempBuffer
                }
            }
            byteArrayOutputStream.close();
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    public synchronized void addToBuffer(byte[] data){
      System.out.println("Add to Buffer");
      buffer.add(data);
    }
    public synchronized byte[] getFromBuffer(){
      return buffer.poll();
    }

    @Override
    public void run()
    {
        if(this.listen==true){
          while(true){
            try{
              byte[] audioData=null;
              int length = dis.readInt();                    // read length of incoming message
              if(length>0) {
                  audioData = new byte[length];
                dis.readFully(audioData, 0, audioData.length); // read the message
              System.out.println("Recieved data");
              System.out.println(audioData);
              addToBuffer(audioData);
            }
            catch(IOException e){

            }
          }

          /*try {
            this.s.close();
            this.dis.close();
            this.dos.close();
          }
          catch(IOException e){
            e.printStackTrace();
          }

        }
        else {
          AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
          AudioInputStream audioInputStream;
          SourceDataLine sourceDataLine;
          while(true){
            //System.out.println("Playing");
            try{
              byte[] audioData = getFromBuffer();
              if(audioData != null){
                System.out.println("Playing");
                InputStream byteArrayInputStream = new ByteArrayInputStream(
                        audioData);
                audioInputStream = new AudioInputStream(byteArrayInputStream,format, audioData.length / format.getFrameSize());
                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                sourceDataLine.open(format);
                sourceDataLine.start();
                int cnt = 0;
                byte tempBuffer[] = new byte[500];
                try {
                    while ((cnt = audioInputStream.read(tempBuffer, 0,tempBuffer.length)) != -1) {
                        if (cnt > 0) {
                            // Write data to the internal buffer of
                            // the data line where it will be
                            // delivered to the speaker.
                            sourceDataLine.write(tempBuffer, 0, cnt);
                        }// end if
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Block and wait for internal buffer of the
                // data line to empty.
                sourceDataLine.drain();
                sourceDataLine.close();
              }

            }
            catch(Exception e){
              e.printStackTrace();
            }

          }
        }

    }
}*/
