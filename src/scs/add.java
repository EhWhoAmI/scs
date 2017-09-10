package scs;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import nu.xom.Element;
import nu.xom.Text;
import scstools.Command;

/**
 *
 * @author Zyun
 */
public class add implements Command{

    @Override
    public int execute(String[] args) {
        //Check the arguments
        if (args.length == 0) {
            System.out.println("usage: scs add [<files>]");
            return EXIT_SUCCESS;
        }
        else {
            //Check whether this is a working copy
            File currentDir = new File(System.getProperty("user.dir"));
            if (!isSCSWorkingDir(currentDir)) {
                System.out.println("The dir " + currentDir.getAbsolutePath() + " is not a scs working copy.");
                return EXIT_SUCCESS;
            }
            //Get the path of the 
            //Add the files
            Element scsRoot = new Element("scs");
            
            if (args[0].equals(".")) {
                //Get the other files in the userdir
                File[] files = currentDir.listFiles();
                for (File add:files) {
                    if (!add.getName().equals(".scs")) {
                        //Add to to add list.
                        if (add.isFile()) {
                            Element FileToAdd = new Element("file");
                            Text text = new Text(add.getName());
                            FileToAdd.appendChild(text);
                        }
                    }
                }
            }
        }
        return EXIT_SUCCESS;
    }
    
    private boolean isSCSWorkingDir(File f) {
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
    
    public static void main(String[] args) {
        Command thisCmd = new add();
        String[] a = {"./example"};
        thisCmd.execute(a);
    }
}
