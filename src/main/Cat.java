package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Cat implements Command
{
    @Override
    public void execute(String[] args)
    {
        show(args);
    }

    public static void show(String[] args)
    {
        if (args.length < 2)
        {
            System.out.println("cat: missing file operand");
            System.out.print("$ ");
            return;
        }

        for (int i = 1; i < args.length; i++)
        {
            String filePath = args[i];
            File file = new File(filePath);

            if (!file.isAbsolute())
            {
                file = new File(System.getProperty("user.dir"), filePath);
            }

            if (!file.exists())
            {
                System.out.println("cat: " + filePath + ": No such file or directory");
                continue;
            }

            if (file.isDirectory())
            {
                System.out.println("cat: " + filePath + ": Is a directory");
                continue;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    System.out.println(line);
                }
            }
            catch (IOException e)
            {
                System.out.println("cat: " + filePath + ": " + e.getMessage());
            }
        }
        System.out.print("$ ");
    }
}
