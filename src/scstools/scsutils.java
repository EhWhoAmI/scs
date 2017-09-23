/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scstools;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.EmptyFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

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
        } else if (f.getParentFile() != null) {
            return isSCSWorkingDir(f.getParentFile());
        } else {
            return false;
        }
    }

    public static File getSCSWorkingDirFile(File f) {
        if (Arrays.asList(f.list()).contains(".scs")) {
            return f;
        } else if (f.getParentFile() != null) {
            return (getSCSWorkingDirFile(f.getParentFile()));
        } else {
            return null;
        }
    }
}
