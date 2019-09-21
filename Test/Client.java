public class Client{
  int userId;
  int seq;
  int packetLoss;
  byte[][] memBuffer;
  static int userCount = 0;

  public Client(){
    userId = userCount++;
    seq=0;
    packetLoss=0;
    memBuffer = initializeMemBuffer();
  }

  public byte[][] initializeMemBuffer(){
    byte[][] buffer = new byte[16][500];
    for(int i=0;i<16;++i)
    {
      buffer[i] = null;
    }
    return buffer;
  }
}
