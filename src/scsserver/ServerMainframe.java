/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scsserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Lam Zyun
 */
public class ServerMainframe {
    Logging log = new Logging("/scsserver.log", true, true);

        public ServerMainframe(String repoUUID, int repoCommitNumber, File repoBaseFile) {
            System.out.println("log: " + log.getLogfileName());
            try {
                // Create a non-blocking server socket channel
                ServerSocketChannel sock = ServerSocketChannel.open();
                sock.configureBlocking(false);

                // Set the host and port to monitor
                InetSocketAddress server = new InetSocketAddress(
                        19319);
                ServerSocket socket = sock.socket();
                socket.bind(server);
                
                // Create the selector and register it on the channel
                Selector selector = Selector.open();
                sock.register(selector, SelectionKey.OP_ACCEPT);

                // Loop forever, looking for client connections
                while (true) {
                    // Wait for a connection
                    selector.select();

                    // Get list of selection keys with pending events
                    Set keys = selector.selectedKeys();
                    Iterator it = keys.iterator();

                    // Handle each key
                    while (it.hasNext()) {

                        // Get the key and remove it from the iteration
                        SelectionKey sKey = (SelectionKey) it.next();

                        it.remove();
                        if (sKey.isAcceptable()) {

                            // Create a socket connection with client
                            ServerSocketChannel selChannel
                                    = (ServerSocketChannel) sKey.channel();
                            ServerSocket sSock = selChannel.socket();
                            Socket connection = sSock.accept();
                            
                            log.log("Client IP: " + connection.getRemoteSocketAddress().toString());
                            OutputStream outputStream = connection.getOutputStream();
                            BufferedOutputStream serverOutput = new BufferedOutputStream(outputStream);

                            InputStream inputStream = connection.getInputStream();
                            BufferedInputStream serverInput = new BufferedInputStream(inputStream);

                            //Read the enter code. ("c6711b33d73157f21d70ef7d1341e016e92f8443cedd7de866")
                            byte[] verifyCode = new byte[50];
                            serverInput.read(verifyCode);

                            if (verifyCode.equals("c6711b33d73157f21d70ef7d1341e016e92f8443cedd7de866".getBytes("UTF-8"))) {
                                //Kill
                                log.log("Denied access to someone, got " + verifyCode);

                                //Close everything
                                serverOutput.close();
                                outputStream.close();
                                connection.close();
                                continue;
                            } else {
                                log.log("Accepted someone: Getting command");
                                byte[] inputcommand = new byte[3];
                                serverInput.read(inputcommand);
                                
                                //Parse code
                                
                                if ((new String(inputcommand, "UTF-8")).equals("get")) {
                                    serverOutput.write(2); //2 for ok
                                    
                                    log.log("GET repo.");
                                    
                                    //Send commit number
                                    serverOutput.write(repoCommitNumber);

                                    //Send uuid
                                    log.log("Sending uuid");
                                    serverOutput.write((repoUUID).getBytes("UTF-8"));

                                    //Send repo name
                                    serverOutput.write(repoBaseFile.getName().length());
                                    serverOutput.write(repoBaseFile.getName().getBytes("UTF-8"));
                                    
                                    //Send number of files
                                    {
                                       //First get size of files.
                                       File FILESFile = new File (repoBaseFile.getAbsolutePath() + "/master/working/FILES");
                                       String filesSize = Long.toString(FILESFile.length());
                                       serverOutput.write(filesSize.length());
                                       serverOutput.write(filesSize.getBytes());
                                       //Read the file.
                                       FileInputStream FILESInputStream = new FileInputStream(FILESFile);
                                       BufferedInputStream FilesBufferedInputStream = new BufferedInputStream(FILESInputStream);
                                       int file;
                                       while ((file = FilesBufferedInputStream.read()) != -1) {
                                           serverOutput.write(file);
                                       }
                                    }
                                    //Send repo data, find current.zip
                                    File workingZip = new File(repoBaseFile.getAbsolutePath() + "/master/working/current.zip");
                                    if (workingZip.exists()) {
                                        //Then send the data
                                        log.log("Sending repo size: " + workingZip.length());
                                        //Send size of repo
                                        String len = Long.toString(workingZip.length());
                                        byte[] fileLen = len.getBytes("UTF-8");
                                        serverOutput.write(fileLen.length);
                                        serverOutput.write(fileLen);
                                        
                                        //Then open the file
                                        FileInputStream currentzip = new FileInputStream(workingZip);
                                        byte[] buff = new byte[1000];
                                        while (currentzip.read(buff) != -1) {
                                            serverOutput.write(buff);
                                        }
                                    }
                                    else {
                                        serverOutput.write(0);
                                    }
                                }
                                //Push
                                else if (new String(inputcommand, "UTF-8").equals("psh")){
                                    //Get the amount of files to push
                                    int fileInputLen = serverInput.read();
                                    byte[] fileInputStr = new byte[fileInputLen];
                                    serverInput.read(fileInputStr);
                                    Integer.parseInt(new String(fileInputStr, "UTF-8"));
                                }
                                else {
                                    serverOutput.write(1); //For not ok.
                                    log.log("Failed to recieve command. Got " + new String(inputcommand, "UTF-8"));
                                }
                            }
                            //Close everything
                            serverOutput.close();
                            outputStream.close();
                            connection.close();
                        }
                    }
                }
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }
}
