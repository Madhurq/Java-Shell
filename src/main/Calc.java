package main;

import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import java.util.List;

/**
 * Executes dynamic Java code snippets using the JShell API.
 * Justifies the "JShell" entry on the resume!
 */
public class Calc implements Command
{
    private static JShell jshell;

    @Override
    public void execute(String[] args)
    {
        if (args.length < 2)
        {
            System.out.println("calc: missing expression");
            return;
        }

        // Lazy initialization of JShell (it can be heavy to start)
        if (jshell == null)
        {
            jshell = JShell.create();
        }

        // Reconstruct the expression from args (e.g., calc 1 + 1)
        StringBuilder raw = new StringBuilder();
        for (int i = 1; i < args.length; i++)
        {
            raw.append(args[i]).append(" ");
        }
        String expression = raw.toString().trim();

        // Evaluate
        List<SnippetEvent> events = jshell.eval(expression);

        for (SnippetEvent e : events)
        {
            if (e.value() != null)
            {
                System.out.println(e.value());
            }
            if (e.exception() != null)
            {
                System.out.println("Error: " + e.exception().getMessage());
            }
        }
    }
}
