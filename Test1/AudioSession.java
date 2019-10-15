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
import java.net.DatagramSocket;

import java.util.*;


public class AudioSession {

  boolean stopCapture = false;
  ByteArrayOutputStream byteArrayOutputStream;
  AudioFormat audioFormat;
  TargetDataLine targetDataLine;
  AudioInputStream audioInputStream;
  SourceDataLine sourceDataLine;
  byte tempBuffer[] = new byte[200];
  Session peer;
  static boolean sFlag = false;


  public AudioSession(Session peer) {
    this.peer = peer;
  }

  private AudioFormat getAudioFormat() {
    float sampleRate = 16000.0F;
    int sampleSizeInBits = 16;
    int channels = 2;
    boolean signed = true;
    boolean bigEndian = true;
    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
  }

  public void captureAudio() {
      System.out.println("On Capture Audio");
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

    } catch (LineUnavailableException e) {
      System.out.println(e);
      System.exit(0);
    }

  }
  public void capture() {
    byteArrayOutputStream = new ByteArrayOutputStream();
    stopCapture = false;
    try {
      int seq = 0;
      //Record non-stop
      while (!stopCapture) {

        //Read from mic and store in temp buffer
        targetDataLine.read(tempBuffer, 0, tempBuffer.length);  //capture sound into tempBuffer
        seq = seq%16;
        tempBuffer[199] = (byte)seq++;
        tempBuffer[198] = (byte)getHashId();
        System.out.println(tempBuffer[199]);

        //Send whats in buffer to the server using sockets
        DatagramPacket packet = new DatagramPacket(tempBuffer, tempBuffer.length, peer.ip, peer.port);
        if(sFlag){
          peer.socket.send(packet);
        }
      }
      byteArrayOutputStream.close();
    } catch (IOException e) {
      System.out.println(e);
      System.exit(0);
    }
  }

  private int getHashId(){
    int id = 1;
    for(int i=0;i<ip.length();++i)
    {
      id *= (ip.charAt(i)+100);
    }
    id *= port;
    if(id<0){
      id *= (-1);
    }
    return id%500;
  }

  public void play() {
    byteArrayOutputStream = new ByteArrayOutputStream();
    stopCapture = false;
    HashMap<Integer, Client> clientList = new HashMap<Integer, Client>();

    try {
      byte[] buffer=new byte[200];
      int seqNum= 0;
      int packetLoss = 0;

      //Play non-stop
      while (!stopCapture) {

        DatagramPacket packet=new DatagramPacket(buffer, buffer.length);

        peer.socket0.receive(packet);
        /*String sourceIp = packet.getAddress().getHostName();
        int sourcePort = packet.getPort();

        int hashId = getHashId(sourceIp,sourcePort);
        Client client;

        if(clientList.containsKey(hashId)){
          client = clientList.get(hashId);
        }
        else{
          client = new Client();
          clientList.put(hashId,client);
        }

        int userId = client.userId;*/
        buffer = packet.getData();

        /*System.out.println(userId+": "+buffer[499]);
        sourceDataLine.write(buffer, 0, 200);   //playing audio available in tempBuffer*/

        //Packet re-arranging algorithm

        //------------------------------------------------------------------------------------------------------
        if (buffer[199] >= 0 && buffer[199] <= 15) {



          int currentPacket = buffer[199];
          System.out.println("Expected: "+seqNum+" "+"Arrived: "+currentPacket);
          if(currentPacket != seqNum) {
            System.out.println("Not in Sequence");
            if(client.memBuffer[seqNum] == null) {
              System.out.println("Not in Buffer");
              client.memBuffer[currentPacket] = Arrays.copyOf(buffer, 200);
              ++client.packetLoss;
              if(client.packetLoss > 3){
                client.packetLoss = 0;
                continue;
              }
              else{
                continue;
              }
            }
            else{
              System.out.println("Exist in Buffer: "+client.seq);
              buffer = Arrays.copyOf(client.memBuffer[client.seq], 200);
              client.memBuffer[client.seq] = null;
              client.memBuffer[currentPacket] = Arrays.copyOf(buffer, 200);
            }
          }
          //------------------------------------------------------------------------------------------------------

          //Play data in temp buffer
          byteArrayOutputStream.write(buffer, 0, 200);
          System.out.println("Playing: "+buffer[199]);
          sourceDataLine.write(buffer, 0, 200);   //playing audio available in tempBuffer

          //--------------------------------------------------------------------------------------------------------
          ++client.seq;
          client.seq %= 16;
          if(client.seq == 0){
            client.memBuffer = client.initializeMemBuffer();
            System.out.println("User: "+userId+" clearing buffer");
          }
        }
      }
      byteArrayOutputStream.close();
    } catch (IOException e) {
      System.out.println(e);
      System.exit(0);
    }
  }
}
