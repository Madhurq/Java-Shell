package main;

import java.io.File;

public class Ls implements Command
{
    @Override
    public void execute(String[] args)
    {
        list(args);
    }

    public static void list(String[] args)
    {
        String dirPath;
        if (args.length > 1)
        {
            dirPath = args[1]; //if user mentions explicitely that they want to list a directory
        }
        else
        {
            dirPath = System.getProperty("user.dir"); //else list the current directory
        }

        File dir = new File(dirPath);
        if (!dir.isAbsolute())
        {
            dir = new File(System.getProperty("user.dir"), dirPath);
        }

        if (!dir.exists())
        {
            System.out.println("ls: cannot access '" + dirPath + "': No such file or directory");
            
            return;
        }

        if (!dir.isDirectory())
        {
            System.out.println(dir.getName());
            
            return;
        }

        File[] files = dir.listFiles(); //array of multiple files
        if (files != null && files.length > 0)
        {
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    System.out.print(file.getName() + "/  "); // directories are shown as / at the end
                }
                else
                {
                    System.out.print(file.getName() + "  "); //files with just a space
                }
            }
            System.out.println();
        }
        
    }
}
