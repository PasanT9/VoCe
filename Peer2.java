import java.io.*;
import java.net.*;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

// Client class
public class Peer2
{
    public static void main(String[] args) throws IOException
    {
        try
        {
            Scanner scn = new Scanner(System.in);
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
            TargetDataLine microphone;
            byte audioData[] = null;


            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection with server port 5056
            Socket s = new Socket(ip, 5056);

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                        System.out.println("Connection Created");
            // the following loop performs the exchange of
            // information between client and client handler
            while (true)
            {
              try{
                microphone = AudioSystem.getTargetDataLine(format);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int numBytesRead;
                int CHUNK_SIZE = 1024;
                byte[] data = new byte[microphone.getBufferSize() / 5];
                microphone.start();

                int bytesRead = 0;
                System.out.println("Recording !!!");
                try {
                    while (bytesRead < 500) { // Just so I can test if recording
                                                    // my mic works...
                        numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
                        bytesRead = bytesRead + numBytesRead;
                        System.out.println(bytesRead);
                        out.write(data, 0, numBytesRead);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Finished !!!");
                audioData = out.toByteArray();
                microphone.close();


              }catch(Exception e){

              }

                //System.out.println(dis.readUTF());
                //String tosend = scn.nextLine();
                String tosend = new String(audioData);
                System.out.println(tosend.getBytes());
                dos.writeInt(audioData.length);
                dos.write(audioData);
              //  dos.write(tosend);
                System.out.println("Data Sent!!");
                // If client sends exit,close this connection
                // and then break from the while loop
                if(tosend.equals("Exit"))
                {
                    System.out.println("Closing this connection : " + s);
                    s.close();
                    System.out.println("Connection closed");
                    break;
                }

                // printing date or time as requested by client
                //String received = dis.readUTF();
                //System.out.println(received);
                //s.close();
            }

            // closing resources
            s.close();
            scn.close();
            dis.close();
            dos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
