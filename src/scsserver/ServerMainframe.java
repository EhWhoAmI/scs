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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

/**
 *
 * @author Lam Zyun
 */
public class ServerMainframe {

    Logging log = new Logging("/scsserver.log", true, true);
    public static String version = "0.0.0a";

    public ServerMainframe(String repoUUID, int repoCommitNumber, File repoBaseFile) {
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
                                    File FILESFile = new File(repoBaseFile.getAbsolutePath() + "/master/working/FILES");
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
                                } else {
                                    serverOutput.write(0);
                                }
                            } //Push
                            else if (new String(inputcommand, "UTF-8").equals("psh")) {
                                serverOutput.write(2); //2 for ok

                                log.log("Push repo.");
                                //Get the type of push : 
                                /**
                                 * 0, for files, as in filenames, 1, for the
                                 * content of files.
                                 */
                                int typeOfPush = serverInput.read();
                                if (typeOfPush == 0) {
                                    //Read the number of files 
                                    int sizeOfPushStr = serverInput.read();
                                    byte[] sizeOfPush = new byte[sizeOfPushStr];
                                    serverInput.read(sizeOfPush);
                                    //Parse
                                    byte[] pushSizeByteBuff = new byte[Integer.parseInt(new String(sizeOfPush, "UTF-8"))];
                                    serverInput.read(pushSizeByteBuff);

                                    //Then write to a temp file
                                    //Get amount of temps around.
                                    File FILESTemp = new File(repoBaseFile.getAbsolutePath() + "/master/working/TEMP/" + String.valueOf(UUID.randomUUID()));
                                    FILESTemp.getParentFile().mkdirs();
                                    FILESTemp.createNewFile();
                                    //Open the file and write to it.
                                    FileOutputStream FILESTempWriter = new FileOutputStream(FILESTemp);
                                    FILESTempWriter.write(pushSizeByteBuff);
                                    FILESTempWriter.close();

                                    //Done. Time to check diff.
                                    //Read all the elements from the temp file
                                    Scanner TEMPScanner = new Scanner(FILESTemp);
                                    ArrayList<String> toAdd = new ArrayList<>();
                                    while (TEMPScanner.hasNextLine()) {
                                        String next = TEMPScanner.nextLine();
                                        toAdd.add(next);
                                    }

                                    Scanner FILESScanner = new Scanner(new File(repoBaseFile.getAbsolutePath() + "/master/working/FILES"));
                                    ArrayList<String> added = new ArrayList<>();
                                    while (FILESScanner.hasNextLine()) {
                                        String next = FILESScanner.nextLine();
                                        added.add(next);
                                    }
                                    //Now the actual file
                                    //Push xml doc.
                                    Element root = new Element("scs");
                                    Attribute versionAttribute = new Attribute("value", version);
                                    Element versionElement = new Element("version");
                                    versionElement.addAttribute(versionAttribute);
                                    Element timeElement = new Element("date");
                                    Attribute daAttribute = new Attribute("value", new Date().toString());
                                    timeElement.addAttribute(daAttribute);
                                    root.appendChild(timeElement);

                                    if (added.size() == 0) {
                                        //Just copy the whole file FILES
                                        PrintWriter writer = new PrintWriter(repoBaseFile.getAbsolutePath() + "/master/working/FILES");
                                        for (String s : toAdd) {
                                            writer.println(s);
                                            //Create files
                                            File toCreate = new File(repoBaseFile.getAbsolutePath() + "/master/working/current" + s);
                                            Element e;
                                            Element add = new Element("add");
                                            if (s.endsWith("/")) {
                                                toCreate.mkdir();
                                                e = new Element("dir");

                                            } else {
                                                toCreate.getParentFile().mkdirs();
                                                toCreate.createNewFile();
                                                e = new Element("file");
                                            }
                                            e.appendChild(add);
                                            root.appendChild(e);

                                        }
                                        writer.close();
                                    } else {
                                        //Find the point where both files cease to be the same.
                                        //The added file will always be bigger than the 
                                        ArrayList<String> adding = new ArrayList<>(toAdd.subList(added.size(), toAdd.size()));
                                        //Write to the file

                                        FileWriter FILESFileWriter = new FileWriter(repoBaseFile.getAbsolutePath() + "/master/working/FILES", true);
                                        PrintWriter FILESPrintWriter = new PrintWriter(FILESFileWriter);
                                        for (String s : adding) {
                                            FILESPrintWriter.println(s);
                                            //Also recreate file.
                                            //Create files
                                            File toCreate = new File(repoBaseFile.getAbsolutePath() + "/master/working/current" + s);
                                            Element e;
                                            Element add = new Element("add");
                                            if (s.endsWith("/")) {
                                                toCreate.mkdir();
                                                e = new Element("dir");
                                            } else {
                                                toCreate.getParentFile().mkdirs();
                                                toCreate.createNewFile();
                                                e = new Element("file");
                                            }
                                            e.appendChild(add);
                                            Attribute a = new Attribute("name", s);
                                            e.addAttribute(a);
                                            root.appendChild(e);
                                        }
                                        FILESPrintWriter.close();
                                        FILESFileWriter.close();
                                    }

                                    //Write to xml file
                                    Document doc = new Document(root);

                                    //Get latest diff
                                    String diffFileName;
                                    File diffFileFolder = new File(repoBaseFile.getAbsolutePath() + "/master/working/push");
                                    String[] files = diffFileFolder.list();

                                    if (files.length == 0) {
                                        diffFileName = "0";
                                    } else {
                                        int[] diffValues = new int[files.length];
                                        for (int i = 0; i < files.length; i++) {
                                            diffValues[i] = Integer.parseInt(files[i]);
                                        }
                                        diffFileName = Integer.toString(getMaxValue(diffValues) + 1);
                                    }

                                    File diffFile = new File(diffFileFolder.getAbsolutePath() + "/" + diffFileName);
                                    diffFile.createNewFile();
                                    PrintWriter pw4diff = new PrintWriter(diffFile);
                                    pw4diff.print(doc.toXML());
                                    pw4diff.close();
                                }
                            } else {
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
            ioe.printStackTrace();
        }
    }

    public static int getMaxValue(int[] array) {
        int maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
            }
        }
        return maxValue;
    }
}
