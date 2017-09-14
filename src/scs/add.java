package scs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
        System.out.println("Args: " + args.length);
        for (String a : args) {
            System.out.println(a);
        }
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
                System.out.println(added.toString());
                //Add the files
                /**
                 * A file is marked, by the fact it does not have a / at the
                 * end, while a directory is marked by a / at the end.
                 */

                if (args[0].equals(".")) {
                    //Get the other files in the userdir
                    System.out.println("Getting all files...");
                    File[] files = currentDir.listFiles();

                    for (File add : files) {
                        if (!add.getName().equals(".scs")) {
                            //Add to to add list.
                            if (!(added.contains(add.getAbsolutePath().substring(scsWorkingDirString.length())))) {
                                if (add.isFile()) {
                                    //Remove the string
                                    
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
                    System.out.println(toAdd.toString());
                    //Write to file
                    PrintWriter FILESPrintWriter = new PrintWriter(FILESFile);
                    for (Iterator<String> it = toAdd.iterator(); it.hasNext();) {
                        String next = it.next();
                        FILESPrintWriter.println(next);
                    }
                    FILESPrintWriter.close();
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(add.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return EXIT_SUCCESS;
    }

    public static void main(String[] args) {
        Command thisCmd = new add();
        String[] a = {""};
        System.out.println("Running...");
        thisCmd.execute(a);
    }
}
