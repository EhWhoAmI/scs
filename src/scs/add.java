package scs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import scstools.Command;
import scstools.scsutils;

/**
 *
 * @author Zyun
 */
public class add implements Command {

    @Override
    public int execute(String[] args) {
        //Check the arguments
        if (args.length == 0) {
            System.out.println("usage: scs add [<files>]");
            return EXIT_SUCCESS;
        } else {
            try {
                //Check whether this is a working copy
                File currentDir = new File(System.getProperty("user.dir"));
                if (!scsutils.isSCSWorkingDir(currentDir)) {
                    System.out.println("The dir " + currentDir.getAbsolutePath() + " is not a scs working copy.");
                    return EXIT_SUCCESS;
                }
                //Get the path of the scs repo.
                File scsWorkingDirFile = scsutils.getSCSWorkingDirFile(new File(System.getProperty("user.dir")));
                String scsWorkingDirString = scsWorkingDirFile.getAbsolutePath();
                ArrayList<String> toAdd = new ArrayList<>();
                ArrayList<String> added = new ArrayList<>();

                //Read all files
                File FILESFile = new File(scsWorkingDirString + "/.scs/FILES");
                Scanner FILESScanner = new Scanner(FILESFile);
                while (FILESScanner.hasNextLine()) {
                    added.add(FILESScanner.nextLine());
                }
                //Add the files
                /**
                 * A file is marked, by the fact it does not have a / at the
                 * end, while a directory is marked by a / at the end.
                 */

                if (args[0].equals(".")) {
                    //Get the other files in the user dir
                    File[] files = currentDir.listFiles();

                    for (File add : files) {
                        if (!add.getName().equals(".scs")) {
                            //Add to to add list.
                            String addPath = add.getAbsolutePath().substring(scsWorkingDirString.length());
                            addPath = addPath.concat((add.isDirectory() == true ? "/" : ""));

                            int addedOrNot = (added.indexOf(addPath.replace('\\', '/')));

                            if (addedOrNot == -1) {
                                if (add.isFile()) {
                                    //Remove the front bit, where the file does not exist
                                    String addingFileString = add.getAbsolutePath().substring(scsWorkingDirString.length());

                                    //Replace all as foward slashes, for the general use.
                                    toAdd.add((addingFileString.replace('\\', '/')));

                                } else if (add.isDirectory()) {
                                    //Add as dir

                                    String addingFileString = add.getAbsolutePath().substring(scsWorkingDirString.length());

                                    //Replace all as foward slashes, for the general use.
                                    toAdd.add((addingFileString.replace('\\', '/')) + "/");
                                }
                            }
                        }
                    }
                } else {
                    //Get the file list to add
                    File[] files = new File[args.length];
                    for (int i = 0; i < args.length; i++) {
                        System.out.println("Parsing file " + args[i]);
                        File f = new File(args[i]);
                        if (f.exists()) {
                            files[i] = new File(args[i]);
                        }
                        else {
                            System.err.println("The file " + f.getAbsolutePath() + " does not exist.");
                        }
                    }
                        for (File add : files) {
                            if (!add.getName().equals(".scs")) {
                                //Add to to add list.
                                String addPath = add.getAbsolutePath().substring(scsWorkingDirString.length());
                                addPath = addPath.concat((add.isDirectory() == true ? "/" : ""));

                                int addedOrNot = (added.indexOf(addPath.replace('\\', '/')));

                                if (addedOrNot == -1) {
                                    if (add.isFile()) {
                                        //Remove the front bit, where the file does not exist
                                        String addingFileString = add.getAbsolutePath().substring(scsWorkingDirString.length());

                                        //Replace all as foward slashes, for the general use.
                                        toAdd.add((addingFileString.replace('\\', '/')));

                                    } else if (add.isDirectory()) {
                                        //Add as dir

                                        String addingFileString = add.getAbsolutePath().substring(scsWorkingDirString.length());

                                        //Replace all as foward slashes, for the general use.
                                        toAdd.add((addingFileString.replace('\\', '/')) + "/");
                                    }
                                }
                            }
                        }
                    }
                    //Write to file
                    FileWriter FILESWriter = new FileWriter(FILESFile, true);

                    PrintWriter FILESPrintWriter = new PrintWriter(FILESWriter);
                    for (Iterator<String> it = toAdd.iterator(); it.hasNext();) {
                        String next = it.next();
                        FILESPrintWriter.println(next);
                    }
                    FILESPrintWriter.close();

                    if (toAdd.size() == 0) {
                        System.out.println("No files to add.");
                        return Command.EXIT_SUCCESS;
                    }
                    System.out.println("Adding files:");
                    for (String list : toAdd) {
                        System.out.println("A\t" + list);
                    }
                    //Then communicate with server to describe the changes.

                    //Get HEAD
                    Scanner HEADFILEScanner = new Scanner(new File(scsWorkingDirString + "/.scs/HEAD"));
                    String HEAD = HEADFILEScanner.nextLine();
                    Socket sock = new Socket(HEAD, 19319);
                    sock.setSoTimeout(20000);
                    BufferedOutputStream outputStream = new BufferedOutputStream(sock.getOutputStream());
                    //Send verify
                    outputStream.write("c6711b33d73157f21d70ef7d1341e016e92f8443cedd7de866".getBytes("UTF-8"));

                    //Write command
                    outputStream.write("psh".getBytes("UTF-8"));
                    outputStream.flush();

                    //Write the type of push: 0
                    outputStream.write(0);

                    //Then write the file
                    long FILESFILElen = FILESFile.length();

                    //To string.
                    String FILESFILELEString = Long.toString(FILESFILElen);
                    //Write the length of that string
                    outputStream.write(FILESFILELEString.length());
                    outputStream.write(FILESFILELEString.getBytes("UTF-8"));

                    //Write the file
                    FileInputStream FILESFileInputStream = new FileInputStream(FILESFile);

                    //Write to server
                    int write;
                    while ((write = FILESFileInputStream.read()) != -1) {
                        outputStream.write(write);
                    }
                    outputStream.close();
                }catch (FileNotFoundException ex) {
                Logger.getLogger(add.class.getName()).log(Level.SEVERE, null, ex);
            }catch (IOException ex) {
                Logger.getLogger(add.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
            return EXIT_SUCCESS;
        }
    }
