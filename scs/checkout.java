package scs;

import scstools.Command;
import java.io.*;
/**
 * Checks out a scs repo.
 * 
 * @author Zyun 
 */
public class checkout implements Command
{
    @Override
    public int execute (String[] args) {
        if (args.length != 1) {
            System.out.println("usage: scs checkout <url>");
            System.out.println("Checkouts a scs repo at the specified url.");
            return Command.EXIT_SUCCESS;
        }
        
        //If the url begins with "file:///", it is a local file.
        if (args[0].startsWith("file:///")) {
            //Local file.
            String path = args[0].substring(8, args[0].length());

            File repoURL = new File(path);
            if (!repoURL.exists()) {
                System.err.println("Failed to open repo: does not exist");
                return Command.EXIT_SUCCESS;
            }
            
            //Check if it is a repo.
            if (!isSCSRepo(repoURL)) {
                System.err.println("The path " + repoURL.getPath() + " is not a scs repo.");
                return Command.EXIT_SUCCESS;
            }
            //Now, let's check it out!!!
        }
        return Command.EXIT_SUCCESS;
    }
    
    private boolean isSCSRepo(File fileCheck) {
        //Open the db folder and check if current and UUID exists. Also check version.
        File dbFile = new File(fileCheck.getPath() + "/db");
        File currentFile = new File(dbFile.getPath() + "/current");
        File UUIDFile = new File(dbFile.getPath() + "/UUID");
        File versionFile = new File(dbFile.getPath() + "/version");
        
        return (dbFile.exists() & currentFile.exists() & UUIDFile.exists() & versionFile.exists());
    }
}
