package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;

import org.jline.reader.*;
import org.jline.reader.impl.completer.*;
import org.jline.builtins.Completers;
import org.jline.terminal.*;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        // Build terminal and line reader with completers
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        
        // Command completer for builtins
        StringsCompleter commandCompleter = new StringsCompleter(
            "echo", "exit", "type", "pwd", "cd", "ls", "cat", "clear", "mkdir", "rm", "rmdir"
        );
        
        // File path completer for tab-completing files/directories
        Completers.FileNameCompleter fileCompleter = new Completers.FileNameCompleter();
        
        // Combine both completers
        AggregateCompleter completer = new AggregateCompleter(commandCompleter, fileCompleter);
        
        LineReader reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .completer(completer)
            .build();

        // Builtins map for the 'type' command
        HashMap<String, String> builtins = new HashMap<>();
        builtins.put("echo", "echo is a shell builtin");
        builtins.put("exit", "exit is a shell builtin");
        builtins.put("type", "type is a shell builtin");
        builtins.put("pwd", "pwd is a shell builtin");
        builtins.put("cd", "cd is a shell builtin");
        builtins.put("ls", "ls is a shell builtin");
        builtins.put("cat", "cat is a shell builtin");
        builtins.put("clear", "clear is a shell builtin");
        builtins.put("mkdir", "mkdir is a shell builtin");
        builtins.put("rm", "rm is a shell builtin");
        builtins.put("rmdir", "rmdir is a shell builtin");

        // Command dispatch map - HashMap replaces the if-else chain!
        HashMap<String, Command> commands = new HashMap<>();
        commands.put("echo", new Echo());
        commands.put("pwd", new Pwd());
        commands.put("cd", new Cd());
        commands.put("ls", new Ls());
        commands.put("cat", new Cat());
        commands.put("clear", new Clear());
        commands.put("type", new Type(builtins));
        commands.put("mkdir", new Mkdir());
        commands.put("rm", new Rm());
        commands.put("rmdir", new Rm(true));  // rmdir is just rm with directory mode

        while (true)
        {
            String prompt = System.getProperty("user.dir") + " $ ";
            String command;
            try
            {
                command = reader.readLine(prompt);
            }
            catch (UserInterruptException | EndOfFileException e)
            {
                break; // Ctrl+C or Ctrl+D exits
            }
            
            // Exit command (special case - needs to break the loop)
            if (Exit.handle(command))
                break;

            String[] words = Parsec.parse(command);

            // Handle output redirection (> and 1>)
            String outFile = null;
            List<String> argsList = new ArrayList<>();
            for (int i = 0; i < words.length; i++)
            {
                String w = words[i];
                if (w.equals(">") || w.equals("1>"))
                {
                    if (i + 1 >= words.length)
                    {
                        System.out.println("syntax error: no file after >");
                        outFile = null;
                        argsList.clear();
                        break;
                    }
                    outFile = words[++i];
                }
                else
                {
                    argsList.add(w);
                }
            }

            if (argsList.isEmpty())
            {
                continue;
            }

            String cmdName = argsList.get(0);
            String[] cmdArgs = argsList.toArray(new String[0]);

            // Try builtin command dispatch (HashMap lookup instead of if-else chain!)
            if (commands.containsKey(cmdName))
            {
                commands.get(cmdName).execute(cmdArgs);
                continue;
            }

            // External command execution
            String os = System.getProperty("os.name").toLowerCase();
            String ps = os.contains("win") ? ";" : ":";
            String ds = os.contains("win") ? "\\" : "/";
            String ext = os.contains("win") ? ".exe" : "";
            String path = System.getenv("PATH");
            String[] dirs = path.split(ps);
            boolean found = false;

            for (String dir : dirs)
            {
                String fp = dir + ds + cmdName + ext;
                File f = new File(fp);
                if (f.exists() && f.canExecute())
                {
                    List<String> cmd = new ArrayList<>();
                    cmd.add(cmdName);
                    for (int i = 1; i < cmdArgs.length; i++)
                    {
                        cmd.add(cmdArgs[i]);
                    }
                    ProcessBuilder pb = new ProcessBuilder(cmd);
                    pb.directory(new File(System.getProperty("user.dir")));

                    if (outFile != null)
                    {
                        pb.redirectOutput(new File(outFile));
                        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                    }
                    else
                    {
                        pb.inheritIO();
                    }
                    Process process = pb.start();
                    process.waitFor();
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                System.out.println(cmdName + ": not found");
            }
        }
    }
}
