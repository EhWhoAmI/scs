package scs;

import scstools.Command;
/**
 * Write a description of class Help here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Help implements Command {
    @Override
    public int execute (String[] args) {
        //If the arguments are none, just display basic help.
        if (args.length == 0) {
            System.out.println("usage:  scs <command> [<args>]\n"
                    + "There are the following commands:\n"
                    + "\tcheckout       checkout a scs repo\n"
                    + "\tinfo           display the info of this program\n"
                    + "\thelp           display the help message\n"
                    + "\n\'scs help <command>\' can let you find out more about the command.");
        } else {
            switch (args[0]) {
                case "help":
                    System.out.println("usage: scs help <command>");
                    System.out.println("Displays the help message");
                    break;
                case "info":
                    System.out.println("usage: scs info");
                    System.out.println("Displays the info of this program.");
                    break;
                case "checkout":
                    System.out.println("usage: scs checkout <url>");
                    System.out.println("Checkouts a new scs repo as specified repo.");
                    break;
            }
        }
        return Command.EXIT_SUCCESS;
    }
}
