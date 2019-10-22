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
import java.net.InetAddress;

import java.util.*;


public class AudioSession implements Runnable{

  //Mixer settings
  boolean stopCapture = false;
  ByteArrayOutputStream byteArrayOutputStream;
  AudioFormat audioFormat;
  TargetDataLine targetDataLine;
  AudioInputStream audioInputStream;
  SourceDataLine sourceDataLine;

  //To change packet size
  static final int packetSize = 500;
  byte tempBuffer[] = new byte[packetSize+2];

  //Connection data
  Session peer;

  //Unique id to each user([-500,500])
  static byte userId;

  //Flags to stop recording
  static boolean sFlag = true;
  static boolean fFlag = true;

  //Flags to mute audio
  static boolean mFlag = false;

  //Extra data for testing
  int totalPackets = 0;
  int corruptedPackets = 0;


  public AudioSession(Session peer) {
    this.peer = peer;
  }


  public AudioSession(){
  }

  //Thread to recieve User inputs
  public void run(){
    Scanner read= new Scanner(System.in);
    System.out.println("Commands:");
    System.out.println("\texit-Exit program");
    System.out.println("\tmute-Stop hearing audio");
    System.out.println("\tunmute-Undo mute operation");
    while(true){
      System.out.println("Wish to Speak(y):");
      String input = read.nextLine();
      if(input.equals("y")){
        fFlag = true;
      }
      else if(input.equals("exit")){
        System.exit(0);
      }
      else if(input.equals("mute")){
        System.out.println("Mute: ON");
        mFlag = true;
      }
      else if(input.equals("unmute")){
        System.out.println("Mute: OFF");
        mFlag = false;
      }
    }

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
      Mixer mixer = null;
      for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
        mixer = AudioSystem.getMixer(mixerInfo[cnt]);

        Line.Info[] lineInfos = mixer.getTargetLineInfo();
        if (lineInfos.length >= 1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
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

  //Capture packets
  public void capture() {

    //Update User Id
    getHashId();
    System.out.println("User ID: "+ userId);
    byteArrayOutputStream = new ByteArrayOutputStream();
    stopCapture = false;
    try {
      int seq = 0;

      //Thread to get user inputs
      Thread i = new Thread(new AudioSession());
      i.start();
      while (!stopCapture) {
        //Read from mic and store in temp buffer
        targetDataLine.read(tempBuffer, 0, tempBuffer.length-2);  //capture sound into tempBuffer

        //Sequence numbers for packet [0,15]
        seq = seq%16;

        //To stop recording
      /*  if(fFlag){
          sFlag = true;
          fFlag = false;
          seq = 0;
        }*/

        //write recorded data to buffer
        tempBuffer[packetSize+1] = (byte)seq++;
        tempBuffer[packetSize] = userId;
        DatagramPacket packet = new DatagramPacket(tempBuffer, tempBuffer.length, peer.ip, peer.port);

        //Send whats in buffer to the server using sockets
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

  //Hash function that updates User ID
  private void getHashId(){
    String ip="";
    try{
      ip = InetAddress.getLocalHost().toString();
    }
    catch(Exception ex){
      ex.printStackTrace();
    }
    int port = (int)(Math.random()*500);
    int id = 1;
    for(int i=0;i<ip.length();++i)
    {
      id *= ((ip.charAt(i)+100)%500);
    }
    id *= port;
    if(id<0){
      id *= (-1);
    }
    userId = (byte)(id%500);
  }


  //Play recieved audio pacekts
  public void play() {
    byteArrayOutputStream = new ByteArrayOutputStream();
    stopCapture = false;

    try {
      //Data to remeber State of packets and user
      int seqNum= 0;
      int prevUserId = 0;

      //To test the program
      //---------------------------------------------------------------------------------------------

      /*PacketOrder.packetLoss = 0;
      PacketOrder.outOfOrderPackets = 0;
      PacketOrder.bufferHits = 0;
      TimerTask task = new TimerTask(){
      public void run(){
      System.out.println("Total Packets: "+ totalPackets);
      System.out.println("Corrupted Packets: "+ corruptedPackets);
      System.out.println("Out Of Order Packets: "+ PacketOrder.outOfOrderPackets);
      System.out.println("Buffer Hits: "+ PacketOrder.bufferHits);
      totalPackets = 0;
      corruptedPackets = 0;
      PacketOrder.outOfOrderPackets = 0;
      PacketOrder.bufferHits = 0;
    }
  };
  Timer timer = new Timer();
  timer.schedule(task, new Date(), 1000*60);*/

  //----------------------------------------------------------------------------------------------------

  //Play non-stop
  while (!stopCapture) {
    byte[] buffer=new byte[packetSize+2];
    DatagramPacket packet=new DatagramPacket(buffer, buffer.length);

    //Get data recieved to socket
    peer.socket0.receive(packet);
    buffer = packet.getData();

    //Drop corrupted packets here
    if (buffer[packetSize+1] >= 0 && buffer[packetSize+1] <= 15) {

      //Get data from recieved bytes array
      int currentPacket = buffer[packetSize+1];
      int speaker = buffer[packetSize];

      //To stop playing your own records
      if(speaker != userId){
        if(prevUserId == 0){
          prevUserId = speaker;
        }
        else if(prevUserId != speaker){
          seqNum = 0;
          prevUserId = speaker;
        }
        sFlag = false;

      }
      else if(speaker == userId){;
        sFlag = true;
        continue;
      }

      ++totalPackets;
      //  System.out.println("Expected: "+seqNum+" "+"Arrived: "+currentPacket);

      //If packets are outof order
      if(currentPacket != seqNum) {
          //System.out.println("Not in Sequence");

        //Create PacketOrder object get correct packet order
        PacketOrder packetData = new PacketOrder(seqNum,currentPacket, buffer);
        buffer = Arrays.copyOf(packetData.getOrder(),packetSize+2);
        if(buffer[packetSize+1]==-1) {
          seqNum = buffer[packetSize];
          continue;
        }
      }
      PacketOrder.packetLoss = 0;

      //If not mute play audio
      if(!mFlag){
        byteArrayOutputStream.write(buffer, 0, packetSize);
        //System.out.println("Playing("+speaker+"): "+buffer[packetSize+1]);
        sourceDataLine.write(buffer, 0, packetSize);   //playing audio available in tempBuffer
      }

      //Updates sequence number
      ++seqNum;
      seqNum %= 16;

      //After 15 packets flush memory buffer
      if(seqNum == 0){
        PacketOrder.memBufferToNull();
      }
    }
    else{
      ++corruptedPackets;
    }
  }
  byteArrayOutputStream.close();
} catch (IOException e) {
  System.out.println(e);
  System.exit(0);
}
}
}
