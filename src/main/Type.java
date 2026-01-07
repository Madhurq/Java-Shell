package main;

import java.io.File;
import java.util.HashMap;

public class Type
{

    public static void show(HashMap<String, String> map, String cmd)
    {
        String[] parts = cmd.split(" ");
        if (parts.length < 2)
        {
            return;
        }

        String alr = parts[1];
        if (map.containsKey(alr))
        {
            System.out.println(map.get(alr));
            System.out.print("$ ");
            return;
        }

        //path for external cmds
        String path = System.getenv("PATH");
        if (path != null)
        {
            String[] dir = path.split(";");
            for (String dir1 : dir)
            {
                String filepath = dir1 + "\\" + alr + ".exe";
                File f = new File(filepath);
                if (f.exists())
                {
                    System.out.println(alr + " is " + filepath);
                    System.out.print("$ ");
                    return;
                }
            }
        }
        //if nth found
        System.out.println(alr + ": not found ");
        System.out.print("$ ");
    }
}
