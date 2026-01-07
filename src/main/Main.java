package main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.io.File;


public class Main
{
    public static void main(String[] args) throws Exception
    {
        System.out.print("$ ");
        try (Scanner sc = new Scanner(System.in))
        {
            HashMap<String, String> builtins = new HashMap<>();
            builtins.put("echo", "echo is a shell builtin");
            builtins.put("exit", "exit is a shell builtin");
            builtins.put("type", "type is a shell builtin");
            builtins.put("pwd", "pwd is a shell builtin");
            builtins.put("cd", "cd is a shell builtin");
            builtins.put("ls", "ls is a shell builtin");
            builtins.put("cat", "cat is a shell builtin");
            builtins.put("clear", "clear is a shell builtin");

            while (sc.hasNextLine())
            {
                String command = sc.nextLine();
                //Exit cmds
                if (Exit.handle(command))
                    break;
                //Echo cmds
                String[] words = (Parsec.parse(command));

                //for adding >
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
                        outFile = words[++i]; // next token is filename
                    }
                    else
                    {
                        argsList.add(w);
                    }
                }
                if (argsList.isEmpty())
                {
                    System.out.print("$ ");
                    continue;
                }

                String cmdName = argsList.get(0);
                String[] args1 = argsList.toArray(new String[0]);

                if (words.length > 0 && words[0].equals("echo"))
                {
                    Echo.say(words);
                    continue;
                }

                //Type cmds
                if (words.length > 0 && words[0].equals("type"))
                {
                    Type.show(builtins, command);
                    continue;
                }

                //pwd code
                if (words.length > 0 && words[0].equals("pwd"))
                {
                    Pwd.getdir();
                    continue;
                }

                //cd code
                if (words.length > 0 && words[0].equals("cd"))
                {
                    Cd.change(words[1]);
                    continue;
                }

                //ls code
                if (words.length > 0 && words[0].equals("ls"))
                {
                    Ls.list(words);
                    continue;
                }

                //cat code - for looking inside a file :)
                if (words.length > 0 && words[0].equals("cat"))
                {
                    Cat.show(words);
                    continue;
                }

                //clear code
                if (words.length > 0 && words[0].equals("clear"))
                {
                    Clear.screen();
                    continue;
                }

                //Command exec
                String os = System.getProperty("os.name").toLowerCase();
                String ps = os.contains("win") ? ";" : ":";
                String ds = os.contains("win") ? "\\" : "/";
                String path = System.getenv("PATH");
                String[] dir = path.split(ps);
                boolean found = false;
                for (String dir1 : dir)
                {
                    String fp = dir1 + ds + words[0] + (os.contains("win")?".exe" : "");
                    File f = new File(fp);
                    if (f.exists() && f.canExecute())
                    {
                        List<String> cmd = new ArrayList<>();
                        cmd.add(cmdName);  // Using basename
                        for (int i = 1; i < args1.length; i++)
                        {
                            cmd.add(args1[i]);
                        }
                        ProcessBuilder pb = new ProcessBuilder(cmd);

                        pb.directory(new File(dir1));  // using path differently***

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
                    System.out.print(words[0] + ": not found \n$ ");
                    continue;
                }
                System.out.print("$ ");
            }
        }
    }
}
