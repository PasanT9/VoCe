public class Client{
  int userId;
  int seq;
  byte[][] memBuffer;
  static int userCount = 0;

  public Client(){
    userId = userCount++;
    seq=0;
    memBuffer = initializeMemBuffer();
  }

  private static byte[][] initializeMemBuffer(){
    byte[][] buffer = new byte[16][500];
    for(int i=0;i<16;++i)
    {
      buffer[i] = null;
    }
    return buffer;
  }
}
