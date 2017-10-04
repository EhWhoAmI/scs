package scs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import scstools.Command;

/**
 *
 * @author Zyun
 */
public class push implements Command {

    @Override
    public int execute(String[] args) {
        try {
            //Checkthe repo base path
            File currentDir = new File(System.getProperty("user.dir"));
            if (!scstools.scsutils.isSCSRepo(currentDir)) {
                System.out.println("Not a scs working copy!");
                return EXIT_SUCCESS;
            }
            //Then get the scs file
            File repoBase = scstools.scsutils.getSCSWorkingDirFile(currentDir);
            //Get the files thst have been already added
            File FILEFile = new File(repoBase.getCanonicalPath() + "/.scs/FILES");
            Scanner FILESScanner = new Scanner(FILEFile);

            ArrayList<File> fileList = new ArrayList<>();

            while (FILESScanner.hasNextLine()) {
                String fileN = FILESScanner.nextLine();
                File f = new File(repoBase.getCanonicalPath() + fileN);
                fileList.add(f);
            }

            //Get all those files
            int deletedFiles = 0;
            ArrayList<File> deletedFileList = new ArrayList<>();
            for (File f : fileList) {
                if (f.exists()) {
                    //Then mark as send to server
                } else {
                    //Mark as deleted.
                    deletedFileList.add(f);
                    deletedFiles++;
                }
            }

            //Open server socket.
            File HEADFile = new File(repoBase.getCanonicalPath() + "/.scs/HEAD");
            Scanner HEADFileScanner = new Scanner(HEADFile);
            String HEADPath = HEADFileScanner.nextLine();
            //Check if file or server
            if (HEADPath.startsWith("file:///")) {
                System.out.println("Local repos are not supported yet!");
                System.exit(0);
            } else {
                Socket sock = new Socket(HEADPath, 19319);
                sock.setSoTimeout(20000);
                BufferedOutputStream outputStream = new BufferedOutputStream(sock.getOutputStream());

                //Send verify
                outputStream.write("c6711b33d73157f21d70ef7d1341e016e92f8443cedd7de866".getBytes("UTF-8"));

                //Write command
                outputStream.write("psh".getBytes("UTF-8"));
                outputStream.flush();

                //Write the type of push: 0
                outputStream.write(1);

                //Deleted files
                outputStream.write((Integer.toString(deletedFiles) + "\0").getBytes("UTF-8"));
                if (deletedFiles != 0) {
                    for (File f : deletedFileList) {
                        //Write name
                        //Get file path relative to base
                        String fullPath = f.getCanonicalPath().substring(repoBase.getCanonicalPath().length());
                        outputStream.write((fullPath + "\0").getBytes("UTF-8"));
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(push.class.getName()).log(Level.SEVERE, null, ex);
        }
        return EXIT_SUCCESS;
    }

}
