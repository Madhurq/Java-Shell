package main;

import java.io.File;

public class Mkdir implements Command
{
    @Override
    public void execute(String[] args)
    {
        if (args.length < 2)
        {
            System.out.println("mkdir: missing operand");
            System.out.print("$ ");
            return;
        }

        for (int i = 1; i < args.length; i++)
        {
            String dirPath = args[i];
            File dir = new File(dirPath);

            if (!dir.isAbsolute())
            {
                dir = new File(System.getProperty("user.dir"), dirPath);
            }

            if (dir.exists())
            {
                System.out.println("mkdir: cannot create directory '" + dirPath + "': File exists");
                continue;
            }

            if (!dir.mkdir())
            {
                System.out.println("mkdir: cannot create directory '" + dirPath + "': No such file or directory");
            }
        }
        System.out.print("$ ");
    }
}
