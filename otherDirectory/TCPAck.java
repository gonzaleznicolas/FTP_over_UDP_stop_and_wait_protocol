import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.Socket;

public class TCPAck
  extends Thread
{
  Socket clientSocket = null;
  DataInputStream clientInputStream = null;
  DataOutputStream clientOutputStream = null;
  FileOutputStream clientFileWriter = null;
  DatagramSocket udpSocket = null;
  
  public TCPAck(Socket paramSocket, DatagramSocket paramDatagramSocket, DataInputStream paramDataInputStream, DataOutputStream paramDataOutputStream, FileOutputStream paramFileOutputStream)
  {
    this.clientSocket = paramSocket;
    this.udpSocket = paramDatagramSocket;
    this.clientInputStream = paramDataInputStream;
    this.clientOutputStream = paramDataOutputStream;
    this.clientFileWriter = paramFileOutputStream;
  }
  
  public void run()
  {
    try
    {
      if (this.clientInputStream.readByte() == 0)
      {
        System.out.println("[Server] file transfer completed.");
        System.out.println("[Server] Terminating connection");
        this.clientSocket.close();
        this.clientFileWriter.close();
        
        System.exit(0);
      }
      else
      {
        System.out.println("[Server] Invalid termination message from client");
        System.out.println("[Server] Terminating connection");
        this.clientSocket.close();
        
        this.clientFileWriter.close();
        System.exit(0);
      }
    }
    catch (Exception localException1)
    {
      System.out.println("[Server] Closing Exception: " + localException1.getMessage());
      try
      {
        if (this.clientSocket != null) {
          this.clientSocket.close();
        }
      }
      catch (Exception localException2)
      {
        System.out.println("[Server] Closing Exception: " + localException2.getMessage());
        
        System.exit(0);
      }
      System.exit(0);
    }
  }
}
