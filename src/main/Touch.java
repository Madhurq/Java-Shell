package main;

import java.io.File;
import java.io.IOException;

public class Touch implements Command
{
    @Override
    public void execute(String[] args)
    {
        if (args.length < 2)
        {
            System.out.println("touch: missing file operand");
            return;
        }
        for (int i = 1; i < args.length; i++)
        {
            String path = args[i];
            File file = new File(path);
            if (!file.isAbsolute()) // If path is relative, resolve against current directory
            {
                file = new File(System.getProperty("user.dir"), path);
            }

            try
            {
                if (file.exists())
                {
                    if (!file.setLastModified(System.currentTimeMillis())) // Update the timestamp
                    {
                        System.out.println("touch: cannot touch '" + path + "': Permission denied");
                    }
                }
                else
                {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists())
                    {
                        parent.mkdirs();
                    }                    
                    if (!file.createNewFile()) // Create empty file
                    {
                        System.out.println("touch: cannot touch '" + path + "': Unable to create file");
                    }
                }
            }
            catch (IOException e)
            {
                System.out.println("touch: cannot touch '" + path + "': " + e.getMessage());
            }
        }
    }
}
