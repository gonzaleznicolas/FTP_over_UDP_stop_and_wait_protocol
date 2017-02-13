/**
 * FastFTP Class
 * FastFtp implements a basic FTP application based on UDP data transmission and 
 * alternating-bit stop-and-wait concept
 * @author      Nicolas Gonzalez
 * @version     1.0, 12 Feb 2017
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

	private Socket socketConnectingToServer;
	private DataInputStream tcpInputStreamFromServer;
	private DataOutputStream tcpOutputStreamToServer;

    /**
     * Constructor to initialize the program 
     * 
     * @param serverName	server name
     * @param server_port	server port
     * @param file_name		name of file to transfer
     * @param timeout		Time out value (in milli-seconds).
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
			// INITIALIZE TCP STREAMS
			socketConnectingToServer = new Socket(serverName, serverPort);
			tcpInputStreamFromServer = new DataInputStream(socketConnectingToServer.getInputStream());
			tcpOutputStreamToServer = new DataOutputStream(socketConnectingToServer.getOutputStream());

			// INITIAL HANDSHAKE OVER TCP
			tcpOutputStreamToServer.writeUTF(fileName);
			byte response = tcpInputStreamFromServer.readByte();
			if (response != (byte) 0)
			{
				System.out.println("An error occured establishing the TCP connection with the server");
				System.exit(0);
			}

			// TRANSFER THE FILE OVER UDP

			// make a byte array dataFromFile. it will be of size MAX_PAYLOAD_SIZE unless there are not
			// enough bytes left to read from the file. in that case, dataFromFile will only be as big as
			// bytes were left in the file
            FileInputStream fin = null;
            boolean bytesLeftToRead = true;

            byte[] dataFromFile = new byte[Segment.MAX_PAYLOAD_SIZE];
            byte[] temp1;
            try
            {
                fin = new FileInputStream(fileName);

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
	                System.out.println(dataFromFile.length);
	                System.out.println(Arrays.toString(dataFromFile));
	            }
            }
            catch (Exception e)
            {
                System.out.println("Exception writing to file: " + e.getMessage());
            }
            finally
            {
                try
                {
                    if (fin != null)
                    {
                        fin.close();
                        //System.out.println("closing file");
                    }
                }
                catch (IOException ex)
                {
                    System.out.println("error closing file.");
                }
            }







		}
		catch (Exception e)
		{
			System.out.println("An exception occured, amigo");
		}	

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
		System.out.println("file transfer completed.");
	}

}
