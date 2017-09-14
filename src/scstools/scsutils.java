/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scstools;

import java.io.File;
import java.util.Arrays;

/**
 *
 * @author Lam Zyun
 */
public class scsutils {
    /**
     * Check if a folder is a scs repo
     *
     * @param fileCheck file to check
     * @return whether of not it is a scs repo
     */
    public static boolean isSCSRepo(File fileCheck) {
        //Open the db folder and check if current and UUID exists. Also check version.
        File dbFile = new File(fileCheck.getPath() + "/db");
        File currentFile = new File(dbFile.getPath() + "/current");
        File UUIDFile = new File(dbFile.getPath() + "/UUID");
        File versionFile = new File(dbFile.getPath() + "/version");

        return (dbFile.exists() & currentFile.exists() & UUIDFile.exists() & versionFile.exists());
    }
    
    public static boolean isSCSWorkingDir(File f) {
        if (Arrays.asList(f.list()).contains(".scs")) {
            return true;
        }
        else if (f.getParentFile() != null) {
            return isSCSWorkingDir(f.getParentFile());
        }
        else {
            return false;
        }
    }
    
    public static File getSCSWorkingDirFile(File f) {
        if (Arrays.asList(f.list()).contains(".scs")) {
            return f;
        }
        else if (f.getParentFile() != null) {
            return (getSCSWorkingDirFile(f.getParentFile()));
        }
        else {
            return null;
        }
    }
}
