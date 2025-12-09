package main;

import java.io.File;
import java.io.IOException;

public class Cd {
    static String home = System.getProperty("user.home");
    public static void change(String command) throws IOException {
        if(command==null || command.isEmpty())
        {
            if(home!=null)
            {
                File f = new File(home);
                if(f.exists())
                {
                    System.setProperty("user.dir", f.getCanonicalPath());
                }
            }
            System.out.print("$ ");
            return;
        }
        if (command.equals("~")) {
            if (home != null) {
                File crd = new File(home);
                if (crd.exists() && crd.isDirectory()) {
                    System.setProperty("user.dir", crd.getCanonicalPath());
                }
            }
            System.out.print("$ ");
            return;
        }
        File dir = new File(command);
        if (!dir.isAbsolute())
        {
            dir = new File(System.getProperty("user.dir"), command);
        }
        if (dir.exists() && dir.isDirectory())
        {
            File crd = dir.getCanonicalFile();
            System.setProperty("user.dir", crd.getAbsolutePath());
            System.out.print("$ ");
        }
        else
        {
            System.out.println("cd : " + command + " : not found");
            System.out.print("$ ");
        }

    }
}
