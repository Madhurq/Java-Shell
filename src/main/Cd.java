package main;

import java.io.File;
import java.io.IOException;

public class Cd {
    public static void change(String command) throws IOException {
        if(command.length() == 1) {}
        else
        {
            File dir = new File(command);
            if(!dir.isAbsolute()) {
                dir = new File(System.getProperty("user.dir"), command);
            }
            if(dir.exists() && dir.isDirectory())
            {
                File crd = dir.getCanonicalFile();
                System.setProperty("user.dir", crd.getAbsolutePath());
                System.out.print("$ ");
            }
            else
            {
                System.out.println("cd : "+command+" : not found");
                System.out.print("$ ");
            }
        }
    }
}
