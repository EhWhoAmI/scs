/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scsserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import scstools.scsutils;

/**
 *
 * @author Lam Zyun
 */
public class ScsSimpleServer {

    public ScsSimpleServer(String repo) throws FileNotFoundException {
        File scsRepo = new File(repo);
        int repoCommitNumber;
        String repoUUID;
        
        if (!scsutils.isSCSRepo(scsRepo)) {
            System.err.println("The folder " + repo + " is not an scs repo.");
            System.exit(1);
        }
        //Read the data from the repo.
        File repoCommitFile = new File(scsRepo.getAbsolutePath() + "/db/current");
        File repoUUIDFile = new File(scsRepo.getAbsolutePath() + "/db/UUID");
        if (repoCommitFile.exists()) {
            Scanner repocireader = new Scanner(repoCommitFile);
            //Get int
            repoCommitNumber = repocireader.nextInt();
        } else {
            //Then kill the application
            throw new FileNotFoundException();
        }

        //Then get the uuid
        if (repoUUIDFile.exists()) {
            Scanner repoUUIDScanner = new Scanner(repoUUIDFile);

            //Get string
            repoUUID = repoUUIDScanner.nextLine();

        } else {
            throw new FileNotFoundException();
        }
        
        ServerMainframe serverMainframe = new ServerMainframe(repoUUID, repoCommitNumber, scsRepo);
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: ScsSimpleServer <repo file path>");
            System.exit(0);
        }
        try {
            System.out.println("Starting server...");
            new ScsSimpleServer(args[0]);
        } catch (FileNotFoundException ex) {
            System.err.println("The folder " + args + " is not an scs repo");
        }
    }
}
