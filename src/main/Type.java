package main;

import java.io.File;
import java.util.HashMap;

public class Type implements Command
{
    private HashMap<String, String> builtins;

    public Type(HashMap<String, String> builtins)
    {
        this.builtins = builtins;
    }

    @Override
    public void execute(String[] args)
    {
        if (args.length < 2)
        {
            
            return;
        }

        String alr = args[1];
        if (builtins.containsKey(alr))
        {
            System.out.println(builtins.get(alr));
            
            return;
        }

        //path for external cmds
        String os = System.getProperty("os.name").toLowerCase();
        String ps = os.contains("win") ? ";" : ":";
        String ext = os.contains("win") ? ".exe" : "";
        String ds = os.contains("win") ? "\\" : "/";
        
        String path = System.getenv("PATH");
        if (path != null)
        {
            String[] dir = path.split(ps);
            for (String dir1 : dir)
            {
                String filepath = dir1 + ds + alr + ext;
                File f = new File(filepath);
                if (f.exists())
                {
                    System.out.println(alr + " is " + filepath);
                    
                    return;
                }
            }
        }
        //if nth found
        System.out.println(alr + ": not found ");
        
    }
}
