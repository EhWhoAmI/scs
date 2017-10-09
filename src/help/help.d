import command;
import std.stdio;
import std.algorithm;

class help : Command
{
    this()
    {

    }

    void execute(string[] args)
    {
        if (args.length == 0)
        {
            writeln("Usage: scs [-h | --help] [-v | --version] <command> [<args>]");
            writeln("\nHere are the common scs commands you would use:");
            writeln("For help");
            writeln("help        Show this message if there are no args. The args are");
            writeln("            whatever command you need help on.");
            writeln("\nFor going to work");
            writeln("init        Start a new scs repo in the dir specified in the args,");
            writeln("            or in the current dir if there are none.");
        }
        else
        {
            if (!commandList.canFind(args[0]))
            {
                writeln("The command ", args[0], " is not part of scs.");
            }
            else
            {
                //Get the messages
                switch (args[0])
                {
                case "help":
                    writeln("usage: scs help <command>");
                    writeln("\nWrites the help message of the command onto screen, and shows the generic help");
                    writeln("message if there are none.");
                    break;
                case "init":
                    writeln("usage: scs init [dir]");
                    writeln("\nInitalizes a scs repo in the directory specified, or the current directory if none");
                    writeln("are specified. Will overwrite if one exists.");
                    break;
                default:
                    break;
                }
            }
        }
    }
}
