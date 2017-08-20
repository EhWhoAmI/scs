package scsadmin;

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
            System.out.println("usage:  scsadmin <command> [<args>]\n"
                    + "There are the following commands:\n"
                    + "\tcreate          create a scs repo\n"
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
                case "create":
                    System.out.println("usage: scs create <path> <port> <mySQL server username> <mySQL server password>");
                    System.out.println("Create a scs repo and server at the path, port and the mySql server username and password is for the usernames and passwords.");
                    break;
            }
        }
        return Command.EXIT_SUCCESS;
    }
}
