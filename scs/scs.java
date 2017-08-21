package scs;

import java.util.Arrays;
import scstools.Command;
/**
 * Write a description of class scs here.
 * 
 * @author Zyun 
 * @version (a version number or a date)
 */
public class scs
{
    public final static String version = "0.0";
    public final static String[] commandList = {
            "help",
            "checkout",
            "info"
    };
    static Command cmd;
    public static void main (String[] args) {
        if (args.length != 0) {
            String command = loadCommands(args[0]);
            if (command != null) {
                //Load all the commands.
                
                String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
                switch (command) {
                    case "help":
                        cmd = new Help();
                    break;
                    case "checkout":
                        cmd = new checkout();
                    break;
                    case "info":
                        cmd = new Info();
                    break;
                }
                cmd.execute(commandArgs);
            }
            else{
                System.out.println("Use \'scs help\' for help");
            }
        }
        else {
            System.out.println("Use \'scs help\' for help");
        }
    }
    
    private static String loadCommands (String arg) {
        //Array of commands
        arg = arg.toLowerCase();
        //Search commands
        String cmd = null;
        for (String ar : commandList) {
            if (arg.equals(ar)){
                //Found it
                cmd = ar;
                break;
            }
        }
        return cmd;
    }
}

