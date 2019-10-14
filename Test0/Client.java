public class Client{
  int userId;
  int packetLoss;
  int packetCount;
  static int userCount = 0;
  PacketOrder pBuffer;

  public Client(){
    userId = userCount++;
    packetLoss=0;
    packetCount=0;
    pBuffer = new PacketOrder();
  }
}
