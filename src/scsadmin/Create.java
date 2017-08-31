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
            System.out.println("Create a scs repo at the path.");
            return Command.EXIT_SUCCESS;
        } 
        //Open the 'folder' and check.
        /**
         * The base folder of the repo.
         */
        File repoBase = new File (args[0]);
        if (repoBase.exists()) {
            if (repoBase.isFile()) {
                System.out.println("Invalid path: path is file.");
                return Command.EXIT_SUCCESS;
            }
            //Check if folder is empty
            if (repoBase.list().length > 0) {
                System.out.println("Invalid path: folder is not empty.");
                return Command.EXIT_SUCCESS;
            }
        }
        
        //Then, initialiaze the repo.
        // if it doesn't exist, create.
        if (!repoBase.exists()) {
            if (!repoBase.mkdirs()) {
                System.out.println("Unable to create directories. Please make sure you have premissions for that.");
                return Command.EXIT_SUCCESS;
            }
        }
        
        //Initalize repo create folders
        String basePath = repoBase.getPath();
        System.out.println("Creating repo in " + basePath);
        try {
            File databaseFile = new File (basePath + "/db/");
            databaseFile.mkdir();
            //Generate file for repo UUID.
            UUID repoID = UUID.randomUUID();
            //Write it to a file
            File repoIDFile = new File(databaseFile.getPath() + "/UUID");
            repoIDFile.createNewFile();
            
            FileWriter repoIDFileWriter = new FileWriter(repoIDFile.getPath());
            
            repoIDFileWriter.write(String.valueOf(repoID));
            repoIDFileWriter.close();
            
            //Create the revision file
            File revisionFile = new File (databaseFile.getPath() + "/current");
            revisionFile.createNewFile();
            
            FileWriter currentFileWriter = new FileWriter(revisionFile.getPath());
            currentFileWriter.write("0");
            currentFileWriter.close();
            
            //Create the scs version file, to show which version it works with
            File versionFile = new File (databaseFile.getPath() + "/version");
            versionFile.createNewFile();
            
            FileWriter versionFileWriter = new FileWriter(versionFile.getPath());
            versionFileWriter.write(scsadmin.version);
            versionFileWriter.close();
            
            //Create access folder: TODO
            
            //Create branch folder
            File branches = new File (basePath + "/branches");
            branches.mkdir();
            
            //Create branch,,,
            File leaf = new File (branches.getPath() + "/leaf");
            leaf.mkdir();
            File working = new File (branches.getPath() + "/working");
            working.mkdir();
            
            //Now create the diff files in the folders. It will be in XML.
            Element root = new Element("scs");
            
            Attribute scsversion = new Attribute("version", scsadmin.version);
            root.addAttribute(scsversion);
            
            //Create file.
            //0 for commit 0
            File temp = new File(leaf.getPath() + "/0");
            temp.createNewFile();
            
            FileWriter tempWriter = new FileWriter(temp.getPath());
            BufferedWriter tempBuff = new BufferedWriter(tempWriter);
            
            Document leafDoc = new Document(root);
            tempBuff.write(leafDoc.toXML());
            tempBuff.close();
            
            //Now write the same to the working branch
            //0 for push 0
            temp = new File(working.getPath() + "/0");
            temp.createNewFile();
            
            tempWriter = new FileWriter(temp.getPath());
            tempBuff = new BufferedWriter(tempWriter);
    
            tempBuff.write(leafDoc.toXML());
            tempBuff.close();
        } catch(IOException ioe) {
            System.out.println("Unable to open file: " + ioe.getMessage());
            ioe.printStackTrace();
            //Delete whole dir if failed to create.
            repoBase.delete();
        } catch (Throwable t) {
            t.printStackTrace();
            //Need to clean up...
            repoBase.delete();
        }
        return Command.EXIT_SUCCESS;
    } 
}
