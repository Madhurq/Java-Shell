package main;

public class Clear
{
    public static void screen()
    {
        System.out.print("\033[H\033[2J"); //special ansi code to clear screen
        System.out.flush(); //flushes everything
        System.out.print("$ ");
    }
}
