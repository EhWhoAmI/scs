package scsadmin;

import scstools.Command;
/**
 * Displays version.
 * 
 * @author Zyun
 * @version (a version number or a date)
 */
public class Info implements Command {
    @Override
    public int execute (String[] args) {
        System.out.println("scsadmin version " + scsadmin.version);
        return Command.EXIT_SUCCESS;
    }
}
