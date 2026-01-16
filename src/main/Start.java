package main;

import java.io.IOException;

public class Start implements Command
{
    @Override
    public void execute(String[] args)
    {
        if (args.length < 2)
        {
            System.out.println("Usage: start <program> [arguments...]");
            System.out.println("Examples:");
            System.out.println("  start chrome.exe");
            System.out.println("  start notepad.exe");
            System.out.println("  start chrome.exe https://google.com");
            System.out.println("  start \"C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe\"");
            return;
        }

        try
        {
            // Build the command using Windows "start" for better compatibility
            // "start" command can find executables in PATH and handle associations
            ProcessBuilder pb;
            
            if (args.length == 2)
            {
                // Just the program name - use cmd /c start to leverage Windows PATH lookup
                pb = new ProcessBuilder("cmd", "/c", "start", "", args[1]);
            }
            else
            {
                // Program with arguments
                String[] cmdArray = new String[args.length + 3];
                cmdArray[0] = "cmd";
                cmdArray[1] = "/c";
                cmdArray[2] = "start";
                cmdArray[3] = "";  // Empty title (required for start command with quoted paths)
                for (int i = 1; i < args.length; i++)
                {
                    cmdArray[i + 3] = args[i];
                }
                pb = new ProcessBuilder(cmdArray);
            }

            pb.directory(new java.io.File(System.getProperty("user.dir")));
            pb.start();
            System.out.println("Started: " + args[1]);
        }
        catch (IOException e)
        {
            System.out.println("start: failed to start '" + args[1] + "': " + e.getMessage());
        }
    }
}
