/**
 * @author      Nicolas Gonzalez
 * @version     1.0, 14 Feb 2017
 *
 */


import java.util.TimerTask;
import java.net.*;
import java.util.*;
import java.io.*;


public class TimeOutHandler extends TimerTask
{
    private FTPClient ftpClient; // reference to the FTPClient

	public TimeOutHandler(FTPClient c)
	{
		ftpClient = c;
	}

	public void run()
	{
		ftpClient.sendPacket();
        // immediately start the timer
        ftpClient.startTimer();
        System.out.println("    - The ACK did not arrive. Re-send the packet and re-start the timer.");
	}
}