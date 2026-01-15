package main;

import java.awt.Color;
import java.io.*;
import java.util.Properties;

/**
 * Singleton config manager for shell customization.
 * Stores settings in ~/.jshell/config.properties
 */
public class ShellConfig
{
    private static ShellConfig instance;
    private Properties props;
    private File configFile;

    // Default colors
    private static final String DEFAULT_BG = "#1e1e1e";
    private static final String DEFAULT_FG = "#c8c8c8";
    private static final String DEFAULT_PROMPT = "#64b464";

    private ShellConfig()
    {
        props = new Properties();
        String home = System.getProperty("user.home");
        File configDir = new File(home, ".jshell");
        if (!configDir.exists())
        {
            configDir.mkdirs();
        }
        configFile = new File(configDir, "config.properties");
        load();
    }

    public static ShellConfig getInstance()
    {
        if (instance == null)
        {
            instance = new ShellConfig();
        }
        return instance;
    }

    public void load()
    {
        if (configFile.exists())
        {
            try (FileInputStream fis = new FileInputStream(configFile))
            {
                props.load(fis);
            }
            catch (IOException e)
            {
                System.err.println("Warning: Could not load config: " + e.getMessage());
            }
        }
    }

    public void save()
    {
        try (FileOutputStream fos = new FileOutputStream(configFile))
        {
            props.store(fos, "Java Shell Configuration");
        }
        catch (IOException e)
        {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }

    // Color getters
    public Color getBackgroundColor()
    {
        return parseColor(props.getProperty("bg.color", DEFAULT_BG));
    }

    public Color getForegroundColor()
    {
        return parseColor(props.getProperty("fg.color", DEFAULT_FG));
    }

    public Color getPromptColor()
    {
        return parseColor(props.getProperty("prompt.color", DEFAULT_PROMPT));
    }

    // Color setters
    public void setBackgroundColor(Color c)
    {
        props.setProperty("bg.color", colorToHex(c));
    }

    public void setForegroundColor(Color c)
    {
        props.setProperty("fg.color", colorToHex(c));
    }

    public void setPromptColor(Color c)
    {
        props.setProperty("prompt.color", colorToHex(c));
    }

    // Reset to defaults
    public void reset()
    {
        props.setProperty("bg.color", DEFAULT_BG);
        props.setProperty("fg.color", DEFAULT_FG);
        props.setProperty("prompt.color", DEFAULT_PROMPT);
    }

    // Utility: hex string to Color
    public static Color parseColor(String hex)
    {
        try
        {
            if (hex.startsWith("#"))
            {
                hex = hex.substring(1);
            }
            return new Color(Integer.parseInt(hex, 16));
        }
        catch (Exception e)
        {
            return Color.GRAY; // fallback
        }
    }

    // Utility: Color to hex string
    public static String colorToHex(Color c)
    {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // Get config summary for display
    public String getSummary()
    {
        return String.format(
            "Background: %s\nForeground: %s\nPrompt:     %s",
            props.getProperty("bg.color", DEFAULT_BG),
            props.getProperty("fg.color", DEFAULT_FG),
            props.getProperty("prompt.color", DEFAULT_PROMPT)
        );
    }
}
