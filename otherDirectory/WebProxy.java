/**
 * WebProxy Class
 * 
 * @author      Nicolas Gonzalez
 * @version     1.2 3 Feb 2017
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class WebProxy {

    public ServerSocket serverSocket = null;
    public Socket socket = null;

     /*
     *  Constructor that initalizes the server listenig port
     * @param port      Proxy server listening port
     */

    public WebProxy(int port)
    {
        /* Intialize server listening port */
        try { this.serverSocket = new ServerSocket(port); }
        catch (Exception e){System.out.println("Error1: " + e.getMessage());}

    }



     /**
     * The webproxy logic goes here 
     */
    public void start()
    {
        try
        {
            while (true)
            {
                socket = serverSocket.accept(); // instantiate new socket

                // make streams
                OutputStream os = socket.getOutputStream();
                InputStream is = socket.getInputStream();

                //System.out.println("hi");

                // now that we have a connection, wait for the client to send a message.

                //System.out.println("here");
                //String s = inputStream.nextLine(); // waiting for a message. the server will only get out of this line
                                            // when the client sends a message
                //System.out.println(s); // print that message

                byte[] bytes = new byte[10000];

                boolean bytesLeft = true;
                int index = 0;
                int numberOfConsecutiveNorR = 0;
                int indexAtWhichHeaderLinesEnd = 0;
                int indexAtWhichDataEnds = 0;
                boolean haveReachedEndOfHeaderLines = false;
                while (haveReachedEndOfHeaderLines==false)
                {
                    //if (index >= 180) break;
                    int numRepresentationOf