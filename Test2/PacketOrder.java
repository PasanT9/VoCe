import java.util.*;

public class PacketOrder{

  //Buffer to keep outof order packets
  static byte memBuffer[][] = initializeMemBuffer();

  //Data for testing
  //---------------------------------------------------------------------------
  static int packetLoss;
/*  static int outOfOrderPackets;
  static int totalPackets;
  static int bufferHits;*/
  //----------------------------------------------------------------------------

  static final int packetSize = 500;
  int seqNum;
  int currentPacket;
  byte[] buffer;

  public PacketOrder(int seqNum, int currentPacket, byte[] buffer){
    this.seqNum = seqNum;
    this.currentPacket = currentPacket;
    this.buffer = buffer;
  }

  public byte[] getOrder(){
  //  ++outOfOrderPackets;

    //if expected packet is not in buffer
    if(memBuffer[this.seqNum] == null) {
      //System.out.println("Not in Buffer");

      //Copy current packet to buffer
      memBuffer[this.currentPacket] = Arrays.copyOf(this.buffer, packetSize+2);
      //System.out.println("Packet Loss: "+packetLoss);
      ++packetLoss;

      //If expected packet not reacived after 3 packets. Assume packet is dropped
      if(packetLoss > 3){
        //System.out.println("Drop packet");
        ++this.seqNum;
        this.seqNum %= 16;
        packetLoss = 0;
      }
      this.buffer[packetSize+1] = -1;
      this.buffer[packetSize] = (byte)this.seqNum;
    }
    //If packet is in buffer extract the btye and send to play
    else{
      //++bufferHits;

      //Store the current packet in buffer
      memBuffer[this.currentPacket] = Arrays.copyOf(this.buffer, packetSize+2);
      this.buffer = Arrays.copyOf(memBuffer[this.seqNum], packetSize+2);
      //System.out.println("Exist in Buffer: "+this.buffer[packetSize+1]);

      //Remove extracted data from buffer
      memBuffer[this.seqNum] = null;
    }
    return this.buffer;
  }


  //To initialize buffer to null values
  public static byte[][] initializeMemBuffer(){
    byte[][] buffer = new byte[16][packetSize+2];
    for(int i=0;i<16;++i)
    {
      buffer[i] = null;
    }
    return buffer;
  }

  public static void memBufferToNull(){
    memBuffer = initializeMemBuffer();
  }


}
