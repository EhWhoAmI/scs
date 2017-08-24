package scstools;

import java.io.*;
import java.util.Scanner;
/**
 * Gets the user id, and creates it if necessary
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class UserID {
    public static String userhash;
    
    private static String create40HexStr() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            int val = ((int) (Math.random() * 256)) % 16;
            builder.append(Integer.toHexString(val));
        }
        return builder.toString();
    }
    
    public static void processUser () {
        //Get user home dir.
        String homedir = System.getProperty("user.home");
        //Get the .scsconf folder.
        File scsconfFile = new File (homedir + "/.scsconf");
        File userhashFile = new File (scsconfFile.getPath() + "/hash");
        if (scsconfFile.exists() && scsconfFile.isDirectory()) {
            //Search for user hash            
            if (!userhashFile.exists()) {
                //Delete dir and try again, recursive
                scsconfFile.delete();
                processUser();
            } else {
                //Get user hash.
                try {
                    Scanner scan = new Scanner (userhashFile);
                    userhash = scan.nextLine();
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace();
                }
            }
        }
        else {
            //Create folder, do stuff...
            if (!scsconfFile.mkdir()) {
                System.out.println("Unable to create folder!");
            }
            //Create file.
            try {
                userhashFile.createNewFile();
                //create hash
                String hash = create40HexStr();
                //Write to file
                FileWriter versionFileWriter = new FileWriter(userhashFile.getPath());
                versionFileWriter.write(hash);
                versionFileWriter.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
