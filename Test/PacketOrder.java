import java.util.*;

public class PacketOrder{

    static byte memBuffer[][] = initializeMemBuffer();
    static int packetLoss;
    static int outOfOrderPackets;
    static int totalPackets;
    int seqNum;
    int currentPacket;
    byte[] buffer;

    public PacketOrder(int seqNum, int currentPacket, byte[] buffer){
      this.seqNum = seqNum;
      this.currentPacket = currentPacket;
      this.buffer = buffer;
    }
    public byte[] getOrder(){
      if(memBuffer[this.seqNum] == null) {
        //System.out.println("Not in Buffer");
        memBuffer[this.currentPacket] = Arrays.copyOf(this.buffer, 202);
        //System.out.println("Packet Loss: "+packetLoss);
        ++packetLoss;
        ++outOfOrderPackets;
        if(packetLoss > 3){
          //System.out.println("Drop packet");
          ++this.seqNum;
          this.seqNum %= 16;
          packetLoss = 0;
        }
        this.buffer[201] = -1;
        this.buffer[200] = (byte)this.seqNum;
      }
      else{
        memBuffer[this.currentPacket] = Arrays.copyOf(this.buffer, 202);
        this.buffer = Arrays.copyOf(memBuffer[this.seqNum], 202);
        //System.out.println("Exist in Buffer: "+this.buffer[199]);
        memBuffer[this.seqNum] = null;
      }
      return this.buffer;
    }


    public static byte[][] initializeMemBuffer(){
      byte[][] buffer = new byte[16][202];
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
