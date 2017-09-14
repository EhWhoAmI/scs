package scs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import scstools.Command;
import java.net.Socket;
import java.nio.charset.Charset;
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
public class checkout implements Command {

    //The charset we use for the communication
    Charset utf8 = Charset.forName("UTF-8");

    @Override
    public int execute(String[] args) {
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
                if (args.length == 1) {
                    repoLocalFile = new File(System.getProperty("user.dir") + "/" + checkoutrepoURL.getName());
                } else {
                    repoLocalFile = new File(args[1]);
                }

                if (repoLocalFile.exists()) {
                    //Check if empty.
                    if (repoLocalFile.list().length > 0) {
                        System.err.println("Error: checkout url is full.");
                        return Command.EXIT_SUCCESS;
                    }
                    //Continue
                } else {
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
                File HEADuuid = new File(checkoutrepoURL.getAbsolutePath() + "/db/UUID");
                Scanner HEADuuidScanner = new Scanner(HEADuuid);

                String UUIDCODE = HEADuuidScanner.next();
                //Write to uuid file

                File uuidLocal = new File(scsFile.getAbsolutePath() + "/UUID");
                uuidLocal.createNewFile();

                FileWriter uuidLocalWriter = new FileWriter(uuidLocal);
                uuidLocalWriter.write(UUIDCODE);
                uuidLocalWriter.close();

                //Now, build the repo...
                //Read from latest commit file.
                //Get commit file
                File currentFile = new File(checkoutrepoURL.getPath() + "/db/current");
                Scanner versionFileScanner = new Scanner(currentFile);

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
        } else {
            //Open server socket
            try (Socket digit = new Socket(args[0], 19319);
                    BufferedInputStream in = new BufferedInputStream(digit.getInputStream());) {
                digit.setSoTimeout(20000);
                OutputStream outputStream = digit.getOutputStream();
                BufferedOutputStream out = new BufferedOutputStream(outputStream);
                //Verification string
                byte[] toW = "c6711b33d73157f21d70ef7d1341e016e92f8443cedd7de866".getBytes("UTF-8");
                out.write(toW);
                out.flush();

                //Send command "GET"
                byte[] code = ("get".getBytes("UTF-8"));
                out.write(code);
                out.flush();

                //Check
                if (in.read() == 1) {
                    System.err.println("Error, unable to get repo.");
                    return EXIT_FAILURE;
                }
                int repoCommit = in.read();

                //Get uuid
                byte[] uuid = new byte[36];
                in.read(uuid);

                //Get repo name
                byte[] repoName = new byte[in.read()];
                in.read(repoName);

                //Read the files list
                //Get length of string to tell the length,
                int lengthOfFilesStr = in.read();
                byte[] lengthOfFiles = new byte[lengthOfFilesStr];
                in.read(lengthOfFiles);
                String lengthOfF = new String(lengthOfFiles, "UTF-8");
                byte[] fileList = new byte[Integer.parseInt(lengthOfF)];
                in.read(fileList);

                //Get the repo zip size
                int zipSizeStr = in.read();

                byte[] zipLength = new byte[zipSizeStr];
                in.read(zipLength);
                //Then read from the zip
                String zipLen = new String(zipLength, utf8);

                //Use double & so that java will not execute the next statement
                if (zipSizeStr != 0 && Integer.parseInt(zipLen) != 0) {
                    byte[] zip = new byte[Integer.parseInt(zipLen)];
                    in.read(zip);

                    File zipFile = new File(((args.length == 2) ? args[1] : new String(repoName, utf8)) + "/current.zip");
                    zipFile.createNewFile();

                    FileOutputStream zipFileOutputStream = new FileOutputStream(zipFile);
                    zipFileOutputStream.write(zip);
                }

                File scsFile;
                if (args.length == 2) {
                    scsFile = new File(args[1] + "/.scs");
                } else {
                    scsFile = new File(new String(repoName, utf8) + "/.scs");
                }

                scsFile.mkdirs();
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    Path hiddenFile = scsFile.toPath();
                    Files.setAttribute(hiddenFile, "dos:hidden", true);
                }
                //Then add the files 
                File UUIDFile = new File(scsFile.getAbsolutePath() + "/UUID");
                UUIDFile.createNewFile();

                PrintStream uuidFilePrintStream = new PrintStream(UUIDFile);
                System.out.println(new String(uuid, utf8));
                uuidFilePrintStream.print(new String(uuid, utf8));
                uuidFilePrintStream.close();

                //Commit id
                File commitFile = new File(scsFile.getAbsolutePath() + "/commit");
                commitFile.createNewFile();
                PrintStream commitFilePrintStream = new PrintStream(commitFile);
                commitFilePrintStream.print(repoCommit);
                commitFilePrintStream.close();

                //HEAD
                File HEADFile = new File(scsFile.getAbsolutePath() + "/HEAD");
                HEADFile.createNewFile();
                PrintStream HEADFilePrintStream = new PrintStream(HEADFile);
                HEADFilePrintStream.print(args[0]);
                HEADFilePrintStream.close();

                //FILES file
                File FILESFile = new File(scsFile.getAbsolutePath() + "/FILES");
                FILESFile.createNewFile();
                FileOutputStream FILESFileInputStream = new FileOutputStream(FILESFile);
                FILESFileInputStream.write(fileList);
                FILESFileInputStream.close();

                outputStream.close();
                in.close();
                digit.close();
                System.out.println("Checked out revision " + repoCommit);
            } catch (IOException e) {
                System.out.println("IO Error:" + e.getMessage());
                e.printStackTrace();
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
