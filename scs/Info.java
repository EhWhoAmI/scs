package scs;

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
        System.out.println("scs version 0.0");
        return Command.EXIT_SUCCESS;
    }
}
