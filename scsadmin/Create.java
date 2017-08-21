package scsadmin;

import scstools.Command;
import java.io.File;
import java.util.UUID;
import java.util.zip.*;
import java.io.*;
import nu.xom.*;
/**
 * Creates a repo in the argument.
 * 
 * @author Zyun
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
        String basePath = folder.getPath();
        System.out.println("Creating repo in " + basePath);
        try {
        File database = new File (basePath + "/db/");
        database.mkdir();
        //Generate file for repo UUID.
        UUID repoID = UUID.randomUUID();
        //Write it to a file
        File repoIDFile = new File(database.getPath() + "/UUID");
        repoIDFile.createNewFile();
        
        FileWriter repoIDFileWriter = new FileWriter(repoIDFile.getPath());
        
        repoIDFileWriter.write(String.valueOf(repoID));
        repoIDFileWriter.close();
        
        //Create the revision file
        File revisionFile = new File (database.getPath() + "/current");
        revisionFile.createNewFile();
        
        FileWriter currentFileWriter = new FileWriter(revisionFile.getPath());
        currentFileWriter.write("0");
        currentFileWriter.close();
        
        //Create branch folder
        File branches = new File (basePath + "/branches");
        branches.mkdir();
        
        //Create master branch
        File masterBranch = new File (branches.getPath() + "/master");
        masterBranch.mkdir();
        
        //Create branch,,,
        File leaf = new File (masterBranch.getPath() + "/leaf");
        leaf.mkdir();
        File working = new File (masterBranch.getPath() + "/working");
        working.mkdir();
        
        //Now create the diff files in the folders. It will be in XML.
        Element root = new Element("scs");
        
        Attribute scsversion = new Attribute("version", "0.0");
        root.addAttribute(scsversion);
        
        //Then write to a XML
        
        //Create file
        File temp = new File(leaf.getPath() + "/branch.diff");
        temp.createNewFile();
        
        FileWriter tempWriter = new FileWriter(temp.getPath());
        BufferedWriter tempBuff = new BufferedWriter(tempWriter);
        
        Document leafDoc = new Document(root);
        tempBuff.write(leafDoc.toXML());
        tempBuff.close();
        
        //Now write the same to the working branch
        temp = new File(working.getPath() + "/branch.diff");
        temp.createNewFile();
        
        tempWriter = new FileWriter(temp.getPath());
        tempBuff = new BufferedWriter(tempWriter);

        tempBuff.write(leafDoc.toXML());
        tempBuff.close();
        } catch(IOException ioe) {
            System.out.println("Unable to open file: " + ioe.getMessage());
            ioe.printStackTrace();
            //Delete whole dir if failed to create.
            folder.delete();
        } catch (Throwable t) {
            t.printStackTrace();
            //Need to clean up...
            folder.delete();
        }
        return Command.EXIT_SUCCESS;
    } 
}
