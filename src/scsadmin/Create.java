package scsadmin;

import scstools.Command;
import java.io.File;
import java.util.UUID;
import java.util.zip.*;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
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
        Instant start = Instant.now();
        
        Instant end = Instant.now();
        createRepo(repoBase);
        Duration timeElapsed = Duration.between(start, end);
        System.out.println("Time elapsed: " + timeElapsed.toNanos()+ " nanoseconds");
        return Command.EXIT_SUCCESS;
    } 
    
    private void createRepo(File repoBasePath) {
        //Get the name of the path
        String repoBaseName = repoBasePath.getAbsolutePath();
        try {
            /*
             * Create file structure.
             * Looks like this:
             * REPO ROOT
             * |--> db <== Stands for database
             *   |--> current <== The current revision number
             *   |--> UUID <== The identification number of this repo
             *   |--> version <== The current version of the 
             * |--> master <== `master` folder, where the code resides
             *   |--> leaf <== The place where the latest revision resides
             *     |--> current.zip <== The zip archive of the current revision
             *     |--> diff <== The folder for the diff
             *       |--> 0 <== Diff for revision 0
             *       |--> ... (etc, etc...)
             *     |--> logs <== The revision logs.
             *   |--> working <== The working branch
             *       |--> current <== Folder where all the code exists. Use this for comparing
             *         |-->xxx.txt <== The files in the repo
             *       |--> diff <== The total diff from the latest revision
             *       |--> pushes <== The diff for each individual push. Scrapped after each revision
             *         |--> 0 <== Diff for push 0
             *         |--> ... (etc, etc...)
            */
            //Create a readme.
            
            //Create db folder
            DBFolderCreate(repoBaseName);
            //Create master folder
            masterFolderCreate(repoBaseName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.err.println("Unable to create file!");
        }
    }
    
    private void DBFolderCreate (String basePath) throws IOException {
        //Create the db folder
        File dbRoot = new File (basePath + "/db");
        
        //Create and check whether it exists.
        if (!dbRoot.mkdir()) {
            throw new IOException("Unable to create db folder " + dbRoot.getAbsolutePath() + ". Check premissions.");
        }
        
        //Using this as a generalized file writer
        FileWriter writer;
        
        //Create version file
        {
            File versionFile = new File (dbRoot.getAbsolutePath() + "/version");
            if (!versionFile.createNewFile()) {
                throw new IOException("Unable to create db/current file! IDK what went wrong.");
            }
            
            //Write version
            writer = new FileWriter(versionFile);
            writer.write(scsadmin.version);
            writer.close();
            
            //Uninitalize it
            writer = null;
        }
        
        //Create UUID
        {
            File UUIDFile = new File (dbRoot.getAbsolutePath() + "/UUID");
            if (!UUIDFile.createNewFile()) {
                throw new IOException("Unable to create db/UUID file! IDK what went wrong.");
            }
            
            //Generate UUID
            UUID id = UUID.randomUUID();
            /*The documentation states, `tatic factory to retrieve a type 4 
             * (pseudo randomly generated) UUID. The UUID is generated using a 
             * cryptographically strong pseudo random number generator.`
             * So it should be unique.
             */
            String idValue = String.valueOf(id);
            
            //Write to file
            writer = new FileWriter(UUIDFile);
            writer.write(idValue);
            writer.close();
            writer = null;
        }
        
        //create current folder
        {
            File currentFile = new File(dbRoot.getAbsolutePath() + "/current");
            if (!currentFile.createNewFile()) {
                throw new IOException("Unable to create db/current file! IDK what went wrong.");
            }
            
            //Write to file
            writer = new FileWriter(currentFile);
            writer.write(Integer.toString(0));
            writer.close();
            writer = null;
        }
        //Done!
    }
    
    private void masterFolderCreate (String basePath) throws IOException {
        File masterRoot = new File (basePath + "/master");
        
        //Create and check whether it exists.
        if (!masterRoot.mkdir()) {
            throw new IOException("Unable to create master folder " + masterRoot.getAbsolutePath() + ". Check premissions.");
        }
        
        //Create folders and subsiquent files
        
        //Leaf file
        {
            File leafFile = new File (masterRoot.getAbsolutePath() + "/leaf");
            
            //Create and check whether it exists.
            if (!leafFile.mkdir()) {
                throw new IOException("Unable to create leaf folder " + leafFile.getAbsolutePath() + ". Check premissions.");
            }
            
            //Create current.zip
            
            //Create diff folder
            {
                File diffFolder = new File(leafFile.getAbsolutePath() + "/diff");
                if (!diffFolder.mkdir()) {
                    throw new IOException("Unable to create leaf folder " + leafFile.getAbsolutePath() + ". Check premissions.");
                }
                //Then create commit 0
                File rev0 = new File (diffFolder.getAbsolutePath() + "/0");
                rev0.createNewFile();
                
                //Create xml parser
                
                //Root element
                Element root = new Element("scs");
                
                //Revision no.
                Element revElement = new Element("rev");
                Attribute revAttribute = new Attribute("value", "0");
                revElement.addAttribute(revAttribute);
                
                //Version element, for the sake of compatability
                Element versionElement = new Element("version");
                Attribute versionAttribute = new Attribute("value", scsadmin.version);
                versionElement.addAttribute(versionAttribute);
                
                Element dateElement = new Element("date");
                Attribute dateAttribute = new Attribute("value", new Date().toString());
                dateElement.addAttribute(dateAttribute);
                //Bunch everything together
                
                root.appendChild(revElement);
                root.appendChild(versionElement);
                root.appendChild(dateElement);
                Document toWrite = new Document(root);
                
                FileWriter xmlFileWriter = new FileWriter(rev0);
                xmlFileWriter.write(toWrite.toXML());
                xmlFileWriter.close();
            }
            
            //Create log file
            {
                File logFile = new File(masterRoot + "/logs");
                logFile.createNewFile();
                
                Element logElement = new Element("logs");
                Document logDocument = new Document(logElement);
                
                FileWriter xmlFileWriter = new FileWriter(logFile);
                xmlFileWriter.write(logDocument.toXML());
                xmlFileWriter.close();
                //Done. Do nothing for now
            }
        }
        
        //Working folder
        {
            File workingFolder = new File (masterRoot + "/working");
            if (!workingFolder.mkdir()) {
                    throw new IOException("Unable to create working folder " + workingFolder.getAbsolutePath() + ". Check premissions.");
            }
            
            //Add a curent folder for all code
            File currentFolder = new File (workingFolder.getAbsolutePath() + "/current");
            if (!currentFolder.mkdir()) {
                    throw new IOException("Unable to create current folder " + workingFolder.getAbsolutePath() + ". Check premissions.");
            }
            
            File diffFile = new File (workingFolder.getAbsolutePath() + "/diff");
            if (!diffFile.createNewFile()) {
                throw new IOException("Unable to create leaf folder " + diffFile.getAbsolutePath() + ". Check premissions.");
            }
            
            //Write to diff file
            Element scs = new Element("scs");
            //That's all. Will parse when commiting
            Document scsDocument = new Document(scs);
            FileWriter xmlFileWriter = new FileWriter(diffFile);
            xmlFileWriter.write(scsDocument.toXML());
            xmlFileWriter.flush();
            
            //Pushes folder for all pushes
            File pushes = new File (workingFolder.getAbsolutePath() + "/push");
            if (!pushes.mkdir()) {
                    throw new IOException("Unable to create push folder " + workingFolder.getAbsolutePath() + ". Check premissions.");
            }
        }
    }
}