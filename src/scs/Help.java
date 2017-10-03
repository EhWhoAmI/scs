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
                    + "\tadd            add the files in the command arguments\n"
                    + "\tpush           push file to server or repo."
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
                    System.out.println("usage: scs checkout <url> [<checkout path>]");
                    System.out.println("Checkouts a new scs repo as specified repo.");
                    break;
                case "add":
                    System.out.println("usage: scs add [<files>]");
                    System.out.println("Adds the following files in the arguments. If it is \'scs add .\', you add all the files in the working directory.");
                    break;
                case "push":
                    System.out.println("usage: scs push");
                    System.out.println("Pushes the files to server or repo.");
            }
        }
        return Command.EXIT_SUCCESS;
    }
}
