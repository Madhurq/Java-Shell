package main;

/**
 * Command interface for shell commands.
 * All builtin commands should implement this interface.
 */
public interface Command
{
    /**
     * Execute the command with the given arguments.
     * @param args The command arguments (args[0] is the command name)
     * @throws Exception if command execution fails
     */
    void execute(String[] args) throws Exception;
}
