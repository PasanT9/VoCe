import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

// Server class
public class Peer
{
    public static void main(String[] args) throws IOException
    {
        // server is listening on port 5056
        ServerSocket ss = new ServerSocket(5056);

        while (true)
        {
            Socket s = null;

            try
            {
                s = ss.accept();

                System.out.println("A new client is connected : " + s);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                System.out.println("Assigning new thread for this client");

                // create a new thread object
                Thread listenThread = new ClientHandler(true, s, dis, dos);
                Thread playThread = new ClientHandler(false, s,dis,dos);

                // Invoking the start() method
                listenThread.start();
                playThread.start();

            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }
}

// ClientHandler class
class ClientHandler extends Thread
{

    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    final boolean listen;
    Queue<Byte[]> buffer = new LinkedList<>();


    // Constructor
    public ClientHandler(boolean listen, Socket s, DataInputStream dis, DataOutputStream dos)
    {
        this.listen = listen;
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run()
    {
        if(this.listen==true){
          while(true){
            try{

            }
            catch(Exception e){

            }
          }
        }

        AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
        AudioInputStream audioInputStream;
        SourceDataLine sourceDataLine;
        while (true)
        {
            try {

                // Ask user what he wants
                //dos.writeUTF("What do you want?[Date | Time]..\n"+
                  //          "Type Exit to terminate connection.");

                // receive the answer from client
                ///received = dis.read();
                //byte audioData[] = dis.read
                byte[] audioData=null;
                int length = dis.readInt();                    // read length of incoming message
                if(length>0) {
                    audioData = new byte[length];
                    dis.readFully(audioData, 0, audioData.length); // read the message
                }
                System.out.println("Recieved data");
                System.out.println(audioData);
                try{
                  InputStream byteArrayInputStream = new ByteArrayInputStream(
                          audioData);
                  audioInputStream = new AudioInputStream(byteArrayInputStream,format, audioData.length / format.getFrameSize());
                  DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
                  sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                  sourceDataLine.open(format);
                  sourceDataLine.start();
                  int cnt = 0;
                  byte tempBuffer[] = new byte[500];
                  try {
                      while ((cnt = audioInputStream.read(tempBuffer, 0,tempBuffer.length)) != -1) {
                          if (cnt > 0) {
                              // Write data to the internal buffer of
                              // the data line where it will be
                              // delivered to the speaker.
                              sourceDataLine.write(tempBuffer, 0, cnt);
                          }// end if
                      }
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
                  // Block and wait for internal buffer of the
                  // data line to empty.
                  sourceDataLine.drain();
                  sourceDataLine.close();

                }
                catch(Exception e){

                }

                if(false)
                {
                    System.out.println("Client " + this.s + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.s.close();
                    System.out.println("Connection closed");
                    break;
                }

                // creating Date object
              //  Date date = new Date();

                // write on output stream based on the
                // answer from the client
              /*  switch (received) {

                    case "Date" :
                        toreturn = fordate.format(date);
                        dos.writeUTF(toreturn);
                        break;

                    case "Time" :
                        toreturn = fortime.format(date);
                        dos.writeUTF(toreturn);
                        break;

                    default:
                        dos.writeUTF("Invalid input");
                        break;
                }*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try
        {
            // closing resources
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
