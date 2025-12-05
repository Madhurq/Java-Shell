package main;

public class Echo {
    public static void say(String msg)
    {
        String[] parts = msg.split(" ");

        for(int i = 1 ; i < parts.length ; i++)
        {
            if(i>1)
            {
                System.out.print(" ");
            }
            System.out.print(parts[i]);
        }
        System.out.print("\n$ ");
    }
}
