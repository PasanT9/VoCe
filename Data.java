import java.io.Serializable;

public class Data implements Serializable{
  private static final long serialVersionUID = 1L;
  byte buffer[] = new byte[200];
  int seq;

  public Data(byte buffer[], int seq){
    this.buffer = buffer;
    this.seq = seq;
  }
}
