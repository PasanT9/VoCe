import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Scanner;


/*
PeerToPeer.java is written to facilitate communication between one-to-one
Compile:    $javac PeerToPeer.java
Run: initiate:   $java PeerToPeer peer1
     join:       $java PeerToPeer peer2 <peer2 IP address>
 */

public class PeerToPeer extends Thread {

    enum MODE {PEER_1, PEER_2}
    enum STATE {RECV, REC_SEND, PLAY} //Holds 3 states of thread
    private static MODE mode = MODE.PEER_1;
    private static int packetSize = 64;
    private STATE state;
    private static final int server_port = 12000;
    private static int clientPort = -1;

    private static InetAddress server_address = null, clientAddress = null;
    private static DatagramSocket up_linkSocket = null, down_linkSocket = null;
    private static RecordPlayback recordPlayback = new RecordPlayback();
    private static PacketNumberingAndData packetNumberingAndData = new PacketNumberingAndData();

    private PeerToPeer(STATE state) {
        this.state = state;
    }

    public static void main(String[] args) {
        recordPlayback = new RecordPlayback();
        //Filter invalid user inputs
        String usage = "usage:  $java PeerToPeer peer1\nOR\n$java PeerToPeer peer2 <IP address>";
        if (args.length == 1) {
            if (args[0].equals("peer1")) {
                mode = MODE.PEER_1;
            } else {
                System.out.println("Invalid format\n" + usage);
            }
        } else if (args.length == 2) {
            if (args[0].equals("peer2")) {
                try {
                    server_address = InetAddress.getByName(args[1]);
                    mode = MODE.PEER_2;
                } catch (Exception e) {
                    System.out.println("Invalid IP address\n" + usage);
                }

            }
        }
        Scanner sc = new Scanner(System.in);


        //Peer 1 mode
        if (mode == MODE.PEER_1) {
            try {
                //initiate the socket and wait
                down_linkSocket = new DatagramSocket(server_port);
                DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);

                System.out.println("\nWaiting for peer2...\nPlease share your IP address with peer 2");

                down_linkSocket.receive(packet);
                System.out.println("You have a call!\n Press Enter to answer");

                //Wait until hit enter
                while (true) {
                    String s = sc.nextLine();
                    if (s.isEmpty()) break;
                }
                sc.close();

                //Get the client address from the packet
                clientAddress = packet.getAddress();
                ByteBuffer wrapped = ByteBuffer.wrap(packet.getData());
                clientPort = wrapped.getInt();

                //Send a confirmation to the other side
                byte[] data = "Client has Answered your call...".getBytes();
                up_linkSocket = new DatagramSocket();
                DatagramPacket packet_send = new DatagramPacket(data, data.length, clientAddress, clientPort);
                Thread.sleep(100);
                up_linkSocket.send(packet_send);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (mode == MODE.PEER_2) { //Peer2 mode

            try {

                up_linkSocket = new DatagramSocket();
                down_linkSocket = new DatagramSocket();
                int down_linkPort = down_linkSocket.getLocalPort();

                /*Sending the down_linkPort port to other side for ask that side user to send data to this down_linkPort */
                ByteBuffer b = ByteBuffer.allocate(4);
                b.putInt(down_linkPort);
                byte[] data = b.array();

                DatagramPacket packet = new DatagramPacket(data, data.length, server_address, server_port);
                clientAddress = server_address;
                clientPort = server_port;

                up_linkSocket.send(packet);


                packet.setData(new byte[packetSize]);

                System.out.println("Wait until for the peer to answer...");
                down_linkSocket.receive(packet);
                System.out.println(new String(packet.getData()));


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //Separate threads to handle Receiving, Recording and Sending, Playback simultaneously
        Thread recv = new Thread(new PeerToPeer(STATE.RECV));
        Thread rec_send = new Thread(new PeerToPeer(STATE.REC_SEND));
        Thread play = new Thread(new PeerToPeer(STATE.PLAY));
        //Start all 3 threads
        recv.start();rec_send.start();play.start();


    }

    public void run() {
        if (state == STATE.RECV) {
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);
                    down_linkSocket.receive(packet);
                    packetNumberingAndData.appendPacket(packet.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        } else if (state == STATE.REC_SEND) {
            while (true) {

                byte[] data = recordPlayback.captureAudio();
                byte[] temp_data = PacketNumberingAndData.addNumbers(data);

                try {
                    DatagramPacket packet = new DatagramPacket(temp_data, temp_data.length, clientAddress, clientPort);
                    up_linkSocket.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();

                }


            }
        } else if (state == STATE.PLAY) {

            while (true) {

                byte[] temp = packetNumberingAndData.getPacket();
                recordPlayback.playAudio(temp);

            }
        }
    }


}
