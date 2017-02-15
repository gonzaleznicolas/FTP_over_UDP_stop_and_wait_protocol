/**
 * FastFTP Class
 * FastFtp implements a basic FTP application based on UDP data transmission and 
 * alternating-bit stop-and-wait concept
 * @author      Nicolas Gonzalez
 * @version     1.0, 14 Feb 2017
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;


public class FTPClient {

    private String serverName;
    private int serverPort;
    private String fileName;
    private int timeOut;

    private Socket tcpSocketConnectingToServer;
    private DataInputStream tcpInputStreamFromServer;
    private DataOutputStream tcpOutputStreamToServer;

    private DatagramSocket udpSocketConnectingToServer;
    private DatagramPacket pktToSend;

    private Timer timer;
    private TimeOutHandler timeOutHandler;



    /**
     * Constructor to initialize the program 
     * 
     * @param serverName    server name
     * @param server_port    server port
     * @param file_name        name of file to transfer
     * @param timeout        Time out value (in milli-seconds).
     */
    public FTPClient(String server_name, int server_port, String file_name, int timeout) {
    
    /* Initialize values */
    serverName = server_name;
    serverPort = server_port;
    fileName = file_name;
    timeOut = timeout;
    }
    

    /**
     * Send file content as Segments
     * 
     */
    public void send()
    {
        try
        {
            // INITIALIZE TCP STREAMS AND SOCKET
            tcpSocketConnectingToServer = new Socket(serverName, serverPort);
            tcpInputStreamFromServer = new DataInputStream(tcpSocketConnectingToServer.getInputStream());
            tcpOutputStreamToServer = new DataOutputStream(tcpSocketConnectingToServer.getOutputStream());

            // INITIAL HANDSHAKE OVER TCP
            tcpOutputStreamToServer.writeUTF(fileName);
            byte response = tcpInputStreamFromServer.readByte();
            if (response != (byte) 0)
            {
                System.out.println("An error occured establishing the TCP connection with the server");
                System.exit(0);
            }

            // make an array of arrays of bytes. these arrays will be filled with the bytes from the file.
            // each array will be of size MAX_PAYLOAD_SIZE except for the last one which may be smaller.
            FileInputStream fin = null;
            File f = null;
            boolean bytesLeftToRead = true;
            byte[][] arrayOfChunks = {};
            byte[] dataFromFile = new byte[Segment.MAX_PAYLOAD_SIZE];
            byte[] temp1; // a temporary array
            try
            {
                fin = new FileInputStream(fileName);
                f = new File(fileName);
                long fileSize = f.length();
                int numOfChunks = (int) (fileSize/((int) Segment.MAX_PAYLOAD_SIZE)) + 1;
                arrayOfChunks = new byte[numOfChunks][];
                int ithChunk = 0;
                while(bytesLeftToRead)
                {
                    for(int i = 0; i < dataFromFile.length; i++)
                    {
                        int byteReadAsInt = fin.read();
                        if (byteReadAsInt == -1)
                        {
                            // if we fell in here, the file had fewer than MAX_PAYLOAD_SIZE bytes left to read
                            // so make dataFromFIle byte array just the size needed for the number of bytes we read
                            temp1 = new byte[i];
                            System.arraycopy(dataFromFile, 0, temp1, 0, i);
                            dataFromFile = temp1;
                            bytesLeftToRead = false;
                            break; // break out of for loop
                        }
                        // if here, we read a byte from the file
                        dataFromFile[i] = (byte) byteReadAsInt;
                    }
                    arrayOfChunks[ithChunk] = dataFromFile.clone();
                    ithChunk++;
                }

                //System.out.println(Arrays.deepToString(arrayOfChunks));

            }
            catch (Exception e)
            {
                System.out.println("Exception reading file: " + e.getMessage());
                System.exit(0); // exit
            }
            finally
            {
                try
                {
                    if (fin != null)
                    {
                        fin.close();
                    }
                }
                catch (IOException ex)
                {
                    System.out.println("error closing file.");
                    System.exit(0); // exit
                }
            }
            if (arrayOfChunks[arrayOfChunks.length-1].length == 0)
                arrayOfChunks = Arrays.copyOf(arrayOfChunks, arrayOfChunks.length-1);
            //System.out.println(Arrays.deepToString(arrayOfChunks));


            // at this point, arrayOfChunks is an array of arrays of bytes. all of those bytes together are the file
            // all of the arrays are MAX_PAYLOAD_SIZE in length, except for the last one which may be smaller.

            // TRANSFER CHUNK BY CHUNK OVER UDP USING alternating bit stop-and-wait-protocol

            // establish UDP socket and streams
            InetAddress ipAddress = InetAddress.getByName(serverName); // get ip address of hostname
            udpSocketConnectingToServer = new DatagramSocket();    

            timer = new Timer(); // initialize timer
            int currentChunk = 0;  // currentCunnk is the chunk we are in the process of sending
            int seqNum = 0;   // seqNum will alternate between 0 and 1
            for (int i = 0; i < arrayOfChunks.length; i++)  // this loop iterates once for every chunk of the file
            {
                System.out.println("\nPACKET #"+i);

                // send a chunk
                Segment segToSend = new Segment(seqNum, arrayOfChunks[currentChunk]);
                pktToSend = new DatagramPacket(segToSend.getBytes(), segToSend.getBytes().length, ipAddress, serverPort);
                sendPacket();
                System.out.println(" - Send ACK" + seqNum +" ... now wait to receive ack and start timer.");


                // immediately start the timer
                startTimer();

                // if i receive an ack number which is not of the packet i just sent, ignore it, and keep
                // waiting for the correct ack
                int receivedAckNumber=2;
                do{
                    // receive ack
                    byte[] receiveAck = new byte[4]; // the ack is only 4 bytes so i only need to allocate 4 bytes
                    DatagramPacket ack = new DatagramPacket(receiveAck, receiveAck.length);

                    udpSocketConnectingToServer.receive(ack);

                    Segment ackReceived = new Segment(ack);
                    receivedAckNumber = ackReceived.getSeqNum();
                    System.out.println(" - ACK"+receivedAckNumber+" received.");
                    if (receivedAckNumber == seqNum) // i.e. if we received the ack we were expecting for the packet we just sent
                    {
                        System.out.println(" - Correct ACK arrived. Cancel timer.");
                        timer.cancel(); // cancel the timer since we received the correct ack
                    }
                }while(seqNum != receivedAckNumber); // as long as we do not receive the expected ack, keep listening for acks.




                if (seqNum == 0) {seqNum = 1;}else {seqNum = 0;} // alternate sequence number
                currentChunk++;
            }

            // DONE TRANSFERRING FILE, SEND END OF TRANSMISSION MESSAGE OVER TCP
            System.out.println("\nDisconnecting...");
            tcpOutputStreamToServer.writeByte(0);


            // CLEANUP
            timer.cancel();
            tcpInputStreamFromServer.close();
            tcpOutputStreamToServer.close();
            tcpSocketConnectingToServer.close();
            udpSocketConnectingToServer.close();


        }
        catch (Exception e)
        {
            System.out.println("An fatal error has occured. The program will exit.");
            System.exit(0);
        }

    }

    
    public void sendPacket()
    {
        try{
            udpSocketConnectingToServer.send(pktToSend);
        }
        catch (IOException e)
        {
            System.out.println("There was an excepion sending the packet.");
            System.exit(0);
        }
    }

    public void startTimer()
    {
        timer.cancel(); // cancel previous timer
        timer = new Timer(); // initialize new one
        timeOutHandler = new TimeOutHandler(this);  // initialize the TimeOutHandler and pass it a reference to
                                                    // the FTPClient so that it can access the methods of this class
        timer.schedule(timeOutHandler, timeOut);
    }

        /**
         * A simple test driver
         * 
         */
    public static void main(String[] args) {
        
        String server = "localhost";
        String file_name = "";
        int server_port = 8888;
        int timeout = 50; // milli-seconds (this value should not be changed)

        
        // check for command line arguments
        if (args.length == 3) {
            // either provide 3 parameters
            server = args[0];
            server_port = Integer.parseInt(args[1]);
            file_name = args[2];
        }
        else {
            System.out.println("wrong number of arguments, try again.");
            System.out.println("usage: java FTPClient server port file");
            System.exit(0);
        }

        
        FTPClient ftp = new FTPClient(server, server_port, file_name, timeout);
        
        System.out.printf("sending file \'%s\' to server...\n", file_name);
        ftp.send();
        System.out.println("File transfer completed.");
    }

}
