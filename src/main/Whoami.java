package main;

public class Whoami implements Command
{
    @Override
    public void execute(String[] args)
    {
        System.out.println(System.getProperty("user.name"));
    }
}
