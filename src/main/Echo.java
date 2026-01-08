package main;

public class Echo implements Command
{
    @Override
    public void execute(String[] args)
    {
        for (int i = 1; i < args.length; i++)
        {
            if (i > 1)
            {
                System.out.print(" ");
            }
            System.out.print(args[i]);
        }
        System.out.print("\n$ ");
    }
}
