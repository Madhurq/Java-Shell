package main;

import java.io.File;

public class Rm implements Command
{
    private boolean directoryMode = false;

    public Rm(){}

    public Rm(boolean directoryMode) //added this constructor for directory mode showing
    {
        this.directoryMode = directoryMode;
    }

    @Override
    public void execute(String[] args)
    {
        if (args.length < 2)
        {
            String cmd = directoryMode ? "rmdir" : "rm"; //sends true for rmdir command and false for rm
            System.out.println(cmd + ": missing operand");
            
            return;
        }

        for (int i = 1; i < args.length; i++)
        {
            String path = args[i];
            File file = new File(path);

            if (!file.isAbsolute())
            {
                file = new File(System.getProperty("user.dir"), path);
            }

            if (!file.exists())
            {
                String cmd = directoryMode ? "rmdir" : "rm";
                System.out.println(cmd + ": cannot remove '" + path + "': No such file or directory");
                continue;
            }

            if (file.isDirectory())
            {
                String[] contents = file.list();
                if (contents != null && contents.length > 0)
                {
                    String cmd = directoryMode ? "rmdir" : "rm";
                    System.out.println(cmd + ": failed to remove '" + path + "': Directory not empty");
                }
                else if (!file.delete())
                {
                    String cmd = directoryMode ? "rmdir" : "rm";
                    System.out.println(cmd + ": cannot remove '" + path + "': Permission denied");
                }
            }
            else
            {
                if (directoryMode)
                {
                    System.out.println("rmdir: failed to remove '" + path + "': Not a directory");
                }
                else if (!file.delete())
                {
                    System.out.println("rm: cannot remove '" + path + "': Permission denied");
                }
            }
        }
        
    }
}
