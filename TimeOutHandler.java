/*
 * @author      Nicolas Gonzalez
 */

import java.util.TimerTask;
import java.net.*;
import java.util.*;
import java.io.*;


public class TimeOutHandler extends TimerTask
{
    private FTPClient ftpClient;

	public TimeOutHandler(FTPClient c)
	{
		ftpClient = c;
	}

	public void run()
	{
		ftpClient.sendPacket();

	}
}