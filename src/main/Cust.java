package main;

import java.awt.Color;

/**
 * Command for customizing shell appearance.
 * 
 * Usage:
 *   cust show                 - Show current colors
 *   cust color bg #1e1e1e     - Set background color
 *   cust color fg #00ff00     - Set foreground/text color
 *   cust color prompt #ffcc00 - Set prompt color
 *   cust reset                - Reset to default colors
 */
public class Cust implements Command
{
    // Callback to apply colors in GUI (set by ShellGUI)
    public static Runnable applyCallback;

    @Override
    public void execute(String[] args) throws Exception
    {
        if (args.length < 2)
        {
            printUsage();
            return;
        }

        ShellConfig config = ShellConfig.getInstance();
        String subCmd = args[1].toLowerCase();

        switch (subCmd)
        {
            case "show":
                System.out.println("Current configuration:");
                System.out.println(config.getSummary());
                break;

            case "reset":
                config.reset();
                config.save();
                applyColors();
                System.out.println("Colors reset to defaults.");
                break;

            case "color":
                if (args.length < 4)
                {
                    System.out.println("Usage: cust color <bg|fg|prompt> <#hexcolor>");
                    return;
                }
                String target = args[2].toLowerCase();
                String hexColor = args[3];
                
                // Validate hex color
                Color color;
                try
                {
                    color = ShellConfig.parseColor(hexColor);
                }
                catch (Exception e)
                {
                    System.out.println("Invalid color format. Use hex like #ff0000");
                    return;
                }

                switch (target)
                {
                    case "bg":
                    case "background":
                        config.setBackgroundColor(color);
                        break;
                    case "fg":
                    case "foreground":
                    case "text":
                        config.setForegroundColor(color);
                        break;
                    case "prompt":
                        config.setPromptColor(color);
                        break;
                    default:
                        System.out.println("Unknown target: " + target);
                        System.out.println("Use: bg, fg, or prompt");
                        return;
                }
                config.save();
                applyColors();
                System.out.println("Color updated: " + target + " = " + hexColor);
                break;

            default:
                printUsage();
        }
    }

    private void applyColors()
    {
        if (applyCallback != null)
        {
            applyCallback.run();
        }
    }

    private void printUsage()
    {
        System.out.println("Usage:");
        System.out.println("  cust show                 - Show current colors");
        System.out.println("  cust color bg #rrggbb     - Set background color");
        System.out.println("  cust color fg #rrggbb     - Set foreground color");
        System.out.println("  cust color prompt #rrggbb - Set prompt color");
        System.out.println("  cust reset                - Reset to defaults");
    }
}
