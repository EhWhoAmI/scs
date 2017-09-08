package scs;

import scstools.Command;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
/**
 * Checks out a scs repo.
 * 
 * @author Zyun 
 */
public class checkout implements Command
{
    @Override
    public int execute (String[] args) {
        if (args.length < 1 | args.length > 2) {
            System.out.println("usage: scs checkout <url> [<checkout path>]");
            System.out.println("Checkouts a scs repo at the specified url.");
            return Command.EXIT_SUCCESS;
        }
        
        //If the url begins with "file:///", it is a local file.
        if (args[0].startsWith("file:///")) {
            try {
                //Local file.
                String path = args[0].substring(8, args[0].length());
                
                File checkoutrepoURL = new File(path);
                if (!checkoutrepoURL.exists()) {
                    System.err.println("Failed to open repo: does not exist");
                    return Command.EXIT_SUCCESS;
                }
                //Check if it is a repo.
                if (!isSCSRepo(checkoutrepoURL)) {
                    System.err.println("The path " + checkoutrepoURL.getPath() + " is not a scs repo.");
                    return Command.EXIT_SUCCESS;
                }
                //Now, let's check the path out!!!
                File repoLocalFile;
                if (args.length == 1) 
                    repoLocalFile = new File (System.getProperty("user.dir") + "/" + checkoutrepoURL.getName());
                else 
                    repoLocalFile = new File (args[1]);
                
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
                
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    Path hiddenFile = scsFile.toPath();
                    Files.setAttribute(hiddenFile, "dos:hidden", true);
                }
                
                //Add HEAD file
                File HEADFile = new File(scsFile.getPath() + "/HEAD");
                HEADFile.createNewFile();
                
                //Write to file
                FileWriter HEADFileWriter = new FileWriter(HEADFile.getPath());
                HEADFileWriter.write("file:///" + checkoutrepoURL.getAbsolutePath());
                HEADFileWriter.close();
                
                //Get the ID of the repo
                File HEADuuid = new File (checkoutrepoURL.getAbsolutePath() + "/db/UUID");
                Scanner HEADuuidScanner = new Scanner(HEADuuid);
                
                
                String UUIDCODE = HEADuuidScanner.next();
                //Write to uuid file

                File uuidLocal = new File (scsFile.getAbsolutePath() + "/UUID");
                uuidLocal.createNewFile();

                FileWriter uuidLocalWriter = new FileWriter(uuidLocal);
                uuidLocalWriter.write(UUIDCODE);
                uuidLocalWriter.close();
                
                
                //Now, build the repo...
                //Read from latest commit file.
                
                //Get commit file
                File currentFile = new File(checkoutrepoURL.getPath() + "/db/current");
                Scanner versionFileScanner = new Scanner (currentFile);
                
                int commitNo = versionFileScanner.nextInt();
                
                //Open master folder
                File masterFile = new File(checkoutrepoURL.getPath() + "/branches/working/" + Integer.toHexString(commitNo));
                
                //Parse it.
                Builder builder = new Builder();
                Document build = builder.build(masterFile);
                Element scsElement = build.getRootElement();
                Attribute clonedVersion = scsElement.getAttribute("version");
                if (clonedVersion.getValue().charAt(0) != scs.version.charAt(0)) {
                    System.err.println("The version is incorrect!");
                    return Command.EXIT_SUCCESS;
                }
                
                //Parse...
                System.out.println("Checked out commit " + commitNo);
            } catch (IOException ex) {
                Logger.getLogger(checkout.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParsingException ex) {
                Logger.getLogger(checkout.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
            //Open server socket
            try (Socket digit = new Socket(args[0], 19319);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(digit.getInputStream()));
            ) {

            digit.setSoTimeout(20000);
            PrintStream out = new PrintStream(
                digit.getOutputStream());
            //Verification string
            out.print("c6711b33d73157f21d70ef7d1341e016e92f8443cedd7de866");
            //Then give the command
            out.print("GET");
            //Read the repo commit
            int repoCommit = in.read();
            System.out.println("Repo commit: " + repoCommit);
               
            out.close();
            in.close();
            digit.close();
        } catch (IOException e) {
            System.out.println("IO Error:" + e.getMessage());
        }
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
