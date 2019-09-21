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


public class AudioSession {

  boolean stopCapture = false;
  ByteArrayOutputStream byteArrayOutputStream;
  AudioFormat audioFormat;
  TargetDataLine targetDataLine;
  AudioInputStream audioInputStream;
  SourceDataLine sourceDataLine;
  byte tempBuffer[] = new byte[500];
  Session peer;

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
        tempBuffer[499] = (byte)seq++;
        System.out.println(tempBuffer[499]);

        //Send whats in buffer to the server using sockets
        DatagramPacket packet = new DatagramPacket(tempBuffer, tempBuffer.length, peer.ip, peer.port);
        peer.socket.send(packet);
      }
      byteArrayOutputStream.close();
    } catch (IOException e) {
      System.out.println(e);
      System.exit(0);
    }
  }

  public void play() {
    byteArrayOutputStream = new ByteArrayOutputStream();
    stopCapture = false;
    try {
      byte[] buffer=new byte[500];

      //Play non-stop
      while (!stopCapture) {

        DatagramPacket packet=new DatagramPacket(buffer, buffer.length);
        peer.socket0.receive(packet);
        buffer = packet.getData();

        System.out.println("Playing: "+buffer[499]);
        sourceDataLine.write(buffer, 0, 500);   //playing audio available in tempBuffer

        //Packet re-arranging algorithm

        //------------------------------------------------------------------------------------------------------
        /*  if (readCount > 0 && (tempBuffer[499] >= 0 && tempBuffer[499] <= 15)) {
        int currentPacket = tempBuffer[499];
        System.out.println("Expected: "+packetCount+" "+"Arrived: "+currentPacket);
        if(currentPacket != packetCount){
        System.out.println("Not in Sequence");
        if(memBuffer[packetCount] == null){
        System.out.println("Not in Buffer");
        memBuffer[currentPacket] = Arrays.copyOf(tempBuffer, 500);
        int packets=0;
        do{
        System.out.println("Wait packets for: " + packetCount);
        readCount = dis.read(tempBuffer);
        ++packets;
        if(readCount > 0 && (tempBuffer[499] >= 0 && tempBuffer[499] <= 15)){
        currentPacket = tempBuffer[499];
        memBuffer[currentPacket] = Arrays.copyOf(tempBuffer, 500);
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
System.out.println("Exist in Buffer: "+packetCount);
tempBuffer = Arrays.copyOf(memBuffer[packetCount], 500);
memBuffer[packetCount] = null;
}
}
//------------------------------------------------------------------------------------------------------

//Play data in temp buffer
byteArrayOutputStream.write(tempBuffer, 0, 500);
System.out.println("Playing: "+tempBuffer[499]);
sourceDataLine.write(tempBuffer, 0, 500);   //playing audio available in tempBuffer

//--------------------------------------------------------------------------------------------------------
++packetCount;
packetCount %= 16;
if(packetCount == 0){
initializeMemBuffer();
}
}*/
}
byteArrayOutputStream.close();
} catch (IOException e) {
  System.out.println(e);
  System.exit(0);
}
}
}
