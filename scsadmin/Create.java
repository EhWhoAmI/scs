package scsadmin;

import scstools.Command;
import java.io.File;
/**
 * Write a description of class Create here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Create implements Command {
    @Override
    public int execute (String[] args) {
        if (args.length != 1) {
            System.out.println("usage: scs create <path>");
            System.out.println("Create a scs repo and server at the path, port and the mySql server username and password is for the usernames and passwords.");
            return Command.EXIT_SUCCESS;
        } 
        //Open the 'folder' and check.
        File folder = new File (args[0]);
        if (folder.exists()) {
            if (folder.isFile()) {
                System.out.println("Invalid path: path is file.");
                return Command.EXIT_SUCCESS;
            }
            //Check if folder is empty
            if (folder.list().length > 0) {
                System.out.println("Invalid path: folder is not empty.");
                return Command.EXIT_SUCCESS;
            }
        }
        
        //Then, initialiaze the repo.
        // if it doesn't exist, create.
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                System.out.println("Unable to create directories. Please make sure you have premissions for that.");
                return Command.EXIT_SUCCESS;
            }
        }
        
        //Initalize repo create folders
        
        return Command.EXIT_SUCCESS;
    }
}
