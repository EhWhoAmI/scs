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
            //Now, let's check the path out!!!
            
            String repoName = repoURL.getName();
            File repoLocalFile = new File (System.getProperty("user.dir") + "/" + repoName);
            if (repoLocalFile.exists()) {
                //Check if empty.
                if (repoLocalFile.list().length > 0) {
                    System.err.println("Error: checkout url is full.");
                    return Command.EXIT_SUCCESS;
                }
                //Continue
            }
            else {
                if (!repoLocalFile.mkdir()) {
                    //Inform.
                    System.err.println("Error: Unable to checkout url: " + repoLocalFile.getPath() + " Maybe access is denied?");
                    return Command.EXIT_SUCCESS;
                }
            }
            //Create repo
            //Create .scs hidden file
            File scsFile = new File(repoLocalFile.getPath() + "/.scs");
            scsFile.mkdir();
            
            //Add HEAD file
            File HEADFile = new File(scsFile.getPath() + "/HEAD");
            HEADFile.createNewFile();
            
            //Write to file
            
        }
        else {
            System.out.println("The format you chose is wrong. Please get a file protocol, eg. http:// or https://, or \"file:///\" for local files");
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
