import java.util.*;

public class PacketOrder{

  byte[][] buffer;

  public byte[][] initializeMemBuffer(){
    byte[][] buffer = new byte[16][200];
    for(int i=0;i<16;++i)
    {
      buffer[i] = null;
    }
    return buffer;
  }

  public PacketOrder() {
    buffer = initializeMemBuffer();
  }

  public void addToBuffer(int packetCount, byte[] data){
    buffer[packetCount] = Arrays.copyOf(data, 200);
  }

  public void playBuffer(AudioSession audio){
    int packetLoss=0;
    for(int i=0;i<16;++i)
    {
      byte[] tempBuffer = buffer[i];
      if(tempBuffer != null){
        //Play data in temp buffer
        audio.byteArrayOutputStream.write(tempBuffer, 0, 200);
        System.out.println("Playing: "+tempBuffer[199]);
        audio.sourceDataLine.write(tempBuffer, 0, 200);   //playing audio available in tempBuffer
      }
      else{
        ++packetLoss;
      }
    }
    buffer = initializeMemBuffer();
    System.out.println(packetLoss+"/16 Packets Lost.");

  }

}
