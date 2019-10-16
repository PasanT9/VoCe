import java.util.*;

public class PacketOrder{

    static byte memBuffer[][] = initializeMemBuffer();
    static int packetLoss = 0;
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
        System.out.println("Not in Buffer");
        memBuffer[this.currentPacket] = Arrays.copyOf(this.buffer, 500);
        ++packetLoss;
        if(packetLoss > 3){
          packetLoss = 0;
        }
        this.buffer = null;
      }
      else{
        System.out.println("Exist in Buffer: "+seqNum);
        this.buffer = Arrays.copyOf(memBuffer[this.seqNum], 500);
        memBuffer[this.seqNum] = null;
        memBuffer[this.currentPacket] = Arrays.copyOf(this.buffer, 500);
      }
      return this.buffer;
    }


    public static byte[][] initializeMemBuffer(){
      byte[][] buffer = new byte[16][500];
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
