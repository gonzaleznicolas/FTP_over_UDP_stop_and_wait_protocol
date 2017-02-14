import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class FTPServer
{
  float serverLoss = 0.0F;
  String serverName = "localhost";
  int serverPort = 8888;
  ServerSocket serverSocket = null;
  Socket socket = null;
  DatagramSocket udpSocket = null;
  String filename = null;
  DataInputStream in = null;
  DataOutputStream out = null;
  FileOutputStream fileWriter = null;
  File file = null;
  Random ran = null;
  int rcvSeq = 0;
  
  public FTPServer(int paramInt, float paramFloat)
  {
    this.serverPort = paramInt;
    this.serverLoss = paramFloat;
    this.rcvSeq = 0;
  }
  
  public void receive()
  {
    try
    {
      for (;;)
      {
        byte[] arrayOfByte = new byte[1024];
        
        DatagramPacket localDatagramPacket = new DatagramPacket(arrayOfByte, arrayOfByte.length);
        this.udpSocket.receive(localDatagramPacket);
        System.out.println("hi");//Arrays.toString(arrayOfByte));
        System.out.println(arrayOfByte[7]);
        if (this.ran.nextFloat() > this.serverLoss) // if we dont lose packet
        {
          InetAddress localInetAddress = localDatagramPacket.getAddress();
          int i = localDatagramPacket.getPort();
          Segment localSegment1 = new Segment(localDatagramPacket);
          
          System.out.println("Received packet with sequence number: " + localSegment1.getSeqNum());
          Segment localSegment2;
          if (localSegment1.getSeqNum() == this.rcvSeq)
          {
            this.fileWriter.write(localSegment1.getPayload());
            localSegment2 = new Segment(localSegment1.getSeqNum());
            localDatagramPacket = new DatagramPacket(localSegment2.getBytes(), localSegment2.getLength(), localInetAddress, i);
            TimeUnit.SECONDS.sleep(1);
            this.udpSocket.send(localDatagramPacket);
            System.out.println("Sent ACK with sequence number: " + localSegment2.getSeqNum());
            if (this.rcvSeq == 0) {
              this.rcvSeq = 1;
            } else {
              this.rcvSeq = 0;
            }
          }
          else
          {
            localSegment2 = null;
            System.out.println("Invalid sequence number or duplicate packet: " + localSegment1.getSeqNum());
            if (this.rcvSeq == 0) {
              localSegment2 = new Segment(this.rcvSeq + 1);
            } else {
              localSegment2 = new Segment(this.rcvSeq - 1);
            }
            localDatagramPacket = new DatagramPacket(localSegment2.getBytes(), localSegment2.getLength(), localInetAddress, i);
            this.udpSocket.send(localDatagramPacket);
            System.out.println("Sent ACK with sequence number: " + localSegment2.getSeqNum());
          }
        }
      }
    }
    catch (Exception localException1)
    {
      System.out.println("[Server] Exception while receiving file content: " + localException1.getMessage());
      try
      {
        if (this.socket != null) {
          this.socket.close();
        }
        if (this.udpSocket != null) {
          this.udpSocket.close();
        }
      }
      catch (Exception localException2)
      {
        System.out.println("[Server] Exception while receiving file content: " + localException2.getMessage());
        
        System.exit(0);
      }
      System.exit(0);
    }
  }
  
  public void waitForClient()
  {
    try
    {
      this.serverSocket = new ServerSocket(this.serverPort);
      this.socket = this.serverSocket.accept();
      this.in = new DataInputStream(this.socket.getInputStream());
      this.out = new DataOutputStream(this.socket.getOutputStream());
      this.filename = this.in.readUTF();
      this.file = new File(this.filename);
      System.out.println("\nthis is the file: "+filename+"\n");
      
      this.fileWriter = new FileOutputStream(this.file);
      this.udpSocket = new DatagramSocket(this.serverPort);
      TCPAck localTCPAck = new TCPAck(this.socket, this.udpSocket, this.in, this.out, this.fileWriter);
      localTCPAck.start();
      this.out.writeByte(0);
      this.ran = new Random();
      System.out.printf("[Server] Ready to receive " + this.filename + " from client\n", new Object[0]);
    }
    catch (Exception localException1)
    {
      System.out.println("[Server] Exception during connection initialization: " + localException1.getMessage());
      try
      {
        this.out.writeByte(1);
        if (this.socket != null) {
          this.socket.close();
        }
        if (this.udpSocket != null) {
          this.udpSocket.close();
        }
      }
      catch (Exception localException2)
      {
        System.out.println("[Server] Exception during connection initialization: " + localException2.getMessage());
        
        System.exit(0);
      }
      System.exit(0);
    }
  }
  
  public static void main(String[] paramArrayOfString)
  {
    float f = 0.0F;
    int i = 8888;
    if (paramArrayOfString.length == 2)
    {
      i = Integer.parseInt(paramArrayOfString[0]);
      f = Float.parseFloat(paramArrayOfString[1]);
    }
    else
    {
      System.out.println("[Server] wrong number of arguments, try again.");
      System.out.println("[Server] usage: java FTPServer serverport loss");
      System.exit(0);
    }
    FTPServer localFTPServer = new FTPServer(i, f);
    System.out.printf("[Server] Ready to receive client connection request\n", new Object[0]);
    localFTPServer.waitForClient();
    System.out.println("im hereee");
    localFTPServer.receive();
  }
}
