package main;

public class Pwd implements Command
{
    @Override
    public void execute(String[] args)
    {
        String dir = System.getProperty("user.dir");
        System.out.print(dir);
        System.out.print("\n$ ");
    }
}
