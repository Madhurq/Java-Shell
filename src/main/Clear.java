package main;

public class Clear implements Command
{
    // Static callback for GUI mode - if set, this is invoked instead of ANSI codes
    public static Runnable clearCallback = null;

    @Override
    public void execute(String[] args)
    {
        if (clearCallback != null)
        {
            clearCallback.run();
        }
        else
        {
            System.out.print("\033[H\033[2J"); //special ansi code to clear screen
            System.out.flush(); //flushes everything
        }
    }
}
