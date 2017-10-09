immutable string[] commandList = ["init", "help"];

interface Command {
    public:
        void execute(string[] args);
}