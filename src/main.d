import std.stdio;
import std.algorithm;
import std.string;
import core.stdc.stdlib;
import command;
import help;
import init;


immutable string VERSION_STR = "0.0.0-dev";

void main(string[] args) {
	//Parse args

	//List of program options
	string[] options = ["-h", "--help", "-v", "--version"];
	//Parse options
	int i;
	if (args.length > 1) {
		while (i < args.length) {
			if (args[i].dup[0] != '-') {
				break;
			}
			if (options.canFind(args[i])) {
				if (args[i] == "-v" || args[i] == "--version") {
					showVersion();
				}
				else if (args[i] == "-h" || args[i] == "--help") {
					helpMessage();
				}
			}
			i++;
		}
	}
	else {
		helpMessage();
		exit(0);
	}
	//Commands
	Command cmd;
	i ++;
	string[] cmdArgs = args[(i + 1)..args.length];
	switch (args[i]) {
		case "init":
			cmd = new init();
			break;
		default:
			cmd = new help();
	}
	cmd.execute(cmdArgs);
}

void showVersion() {
	writeln("Version: SCS version ", VERSION_STR);
}

void helpMessage() {
	writeln("Usage: scs [-h | --help] [-v | --version] <command> [<args>]");
	writeln("\nHere are the common scs commands you would use:");
	writeln("For help");
	writeln("help        Show this message if there are no args. The args are");
	writeln("            whatever command you need help on.");
	writeln("\nFor going to work");
	writeln("init        Start a new scs repo in the dir specified in the args,");
	writeln("            or in the current dir if there are none.");
}