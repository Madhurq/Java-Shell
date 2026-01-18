package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * Swing GUI for the Java Shell.
 */
public class ShellGUI extends JFrame
{
    private JTextArea outputArea;
    private JTextField inputField;
    private JLabel promptLabel;
    private HashMap<String, Command> commands;
    private java.util.List<String> history = new ArrayList<>();
    private int historyIndex = -1;

    public ShellGUI()
    {
        super("Java Shell");
        setupUI();
        commands = Main.getCommands(Main.getBuiltins());
        redirectOutput();
        Clear.clearCallback = () -> outputArea.setText("");
        Cust.applyCallback = () -> applyColors();
        SoundPlayer.init(); // Initialize sound effects
        outputArea.append("Java Shell GUI\n\n");
    }

    private void setupUI()
    {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        // Load colors from config
        ShellConfig config = ShellConfig.getInstance();
        Color bg = config.getBackgroundColor();
        Color fg = config.getForegroundColor();
        Color promptColor = config.getPromptColor();

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(bg);
        outputArea.setForeground(fg);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        outputArea.setLineWrap(true);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBackground(bg);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        promptLabel = new JLabel(getPrompt());
        promptLabel.setForeground(promptColor);
        promptLabel.setFont(new Font("Consolas", Font.BOLD, 14));

        inputField = new JTextField();
        inputField.setBackground(bg);
        inputField.setForeground(fg);
        inputField.setCaretColor(fg);
        inputField.setFont(new Font("Consolas", Font.PLAIN, 14));
        inputField.setBorder(null);
        inputField.addActionListener(e -> runCommand());
        inputField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) navigateHistory(-1);
                else if (e.getKeyCode() == KeyEvent.VK_DOWN) navigateHistory(1);
                else if (e.getKeyCode() == KeyEvent.VK_TAB) { tabComplete(); e.consume(); }
            }
            public void keyTyped(KeyEvent e) {
                // Play key press sound for every typed character
                SoundPlayer.playKeyPress();
            }
        });
        // Prevent Tab from moving focus
        inputField.setFocusTraversalKeysEnabled(false);

        // Setup right-click context menus
        setupContextMenu();

        inputPanel.add(promptLabel, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(bg);
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        main.add(inputPanel, BorderLayout.SOUTH);
        add(main);
    }

    /**
     * Setup right-click context menus for output area and input field.
     */
    private void setupContextMenu()
    {
        // Context menu for output area (read-only: Copy, Select All, Clear)
        JPopupMenu outputMenu = new JPopupMenu();
        
        JMenuItem copyOutput = new JMenuItem("Copy");
        copyOutput.addActionListener(e -> outputArea.copy());
        outputMenu.add(copyOutput);
        
        JMenuItem selectAllOutput = new JMenuItem("Select All");
        selectAllOutput.addActionListener(e -> outputArea.selectAll());
        outputMenu.add(selectAllOutput);
        
        outputMenu.addSeparator();
        
        JMenuItem clearOutput = new JMenuItem("Clear");
        clearOutput.addActionListener(e -> outputArea.setText(""));
        outputMenu.add(clearOutput);
        
        outputArea.setComponentPopupMenu(outputMenu);

        // Context menu for input field (editable: Copy, Paste, Select All, Clear)
        JPopupMenu inputMenu = new JPopupMenu();
        
        JMenuItem copyInput = new JMenuItem("Copy");
        copyInput.addActionListener(e -> inputField.copy());
        inputMenu.add(copyInput);
        
        JMenuItem pasteInput = new JMenuItem("Paste");
        pasteInput.addActionListener(e -> inputField.paste());
        inputMenu.add(pasteInput);
        
        JMenuItem selectAllInput = new JMenuItem("Select All");
        selectAllInput.addActionListener(e -> inputField.selectAll());
        inputMenu.add(selectAllInput);
        
        inputMenu.addSeparator();
        
        JMenuItem clearInput = new JMenuItem("Clear");
        clearInput.addActionListener(e -> inputField.setText(""));
        inputMenu.add(clearInput);
        
        inputField.setComponentPopupMenu(inputMenu);
    }

    /**
     * Apply colors from config to all GUI components.
     * Called on startup and when colors are changed via cust command.
     */
    public void applyColors()
    {
        ShellConfig config = ShellConfig.getInstance();
        Color bg = config.getBackgroundColor();
        Color fg = config.getForegroundColor();
        Color promptColor = config.getPromptColor();

        outputArea.setBackground(bg);
        outputArea.setForeground(fg);
        inputField.setBackground(bg);
        inputField.setForeground(fg);
        inputField.setCaretColor(fg);
        promptLabel.setForeground(promptColor);

        // Update panel backgrounds
        Component comp = getContentPane().getComponent(0);
        if (comp instanceof JPanel)
        {
            JPanel mainPanel = (JPanel) comp;
            mainPanel.setBackground(bg);
            for (Component c : mainPanel.getComponents())
            {
                if (c instanceof JPanel)
                {
                    c.setBackground(bg);
                }
            }
        }
        repaint();
    }

    private void redirectOutput()
    {
        PrintStream ps = new PrintStream(new OutputStream() {
            public void write(int b) {
                SwingUtilities.invokeLater(() -> {
                    outputArea.append(String.valueOf((char) b));
                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                });
            }
        }, true);
        System.setOut(ps);
        System.setErr(ps);
    }

    private String getPrompt() { return System.getProperty("user.dir") + " $ "; }

    private void runCommand()
    {
        String cmd = inputField.getText().trim();
        inputField.setText("");
        if (cmd.isEmpty()) return;

        history.add(cmd);
        historyIndex = history.size();
        outputArea.append(getPrompt() + cmd + "\n");

        if (Exit.handle(cmd)) { dispose(); System.exit(0); return; }

        String[] words = Parsec.parse(cmd);
        if (words.length == 0) { promptLabel.setText(getPrompt()); return; }

        String name = words[0];
        if (commands.containsKey(name))
        {
            try { commands.get(name).execute(words); }
            catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
            promptLabel.setText(getPrompt());
            return;
        }

        // External command execution
        String os = System.getProperty("os.name").toLowerCase();
        String ps = os.contains("win") ? ";" : ":";
        String ds = os.contains("win") ? "\\" : "/";
        String ext = os.contains("win") ? ".exe" : "";
        String[] dirs = System.getenv("PATH").split(ps);
        boolean found = false;

        for (String dir : dirs)
        {
            java.io.File f = new java.io.File(dir + ds + name + ext);
            if (f.exists() && f.canExecute())
            {
                try
                {   //pb for external commands support :)
                    java.util.List<String> cmdList = new java.util.ArrayList<>();
                    for (String w : words) cmdList.add(w);
                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.directory(new java.io.File(System.getProperty("user.dir")));
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) System.out.println(line);
                    process.waitFor();
                }
                catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
                found = true;
                break;
            }
        }

        if (!found) System.out.println(name + ": not found");
        promptLabel.setText(getPrompt());
    }

    private void tabComplete()
    {
        String text = inputField.getText();
        if (text.isEmpty()) return;

        String[] parts = text.split("\\s+");
        String toComplete = parts[parts.length - 1];
        String prefix = parts.length > 1 ? text.substring(0, text.lastIndexOf(toComplete)) : "";

        java.util.List<String> matches = new ArrayList<>();

        // If first word, complete commands
        if (parts.length == 1)
        {
            String[] cmds = {"echo", "exit", "type", "pwd", "cd", "ls", "cat", "clear", "mkdir", "rm", "rmdir", "whoami", "touch"};
            for (String c : cmds) if (c.startsWith(toComplete)) matches.add(c);
        }

        // Complete file/directory names
        java.io.File dir = new java.io.File(System.getProperty("user.dir"));
        String filePrefix = toComplete;
        if (toComplete.contains("/") || toComplete.contains("\\"))
        {
            int lastSep = Math.max(toComplete.lastIndexOf('/'), toComplete.lastIndexOf('\\'));
            String dirPath = toComplete.substring(0, lastSep + 1);
            filePrefix = toComplete.substring(lastSep + 1);
            dir = new java.io.File(dir, dirPath);
        }
        java.io.File[] files = dir.listFiles();
        if (files != null)
        {
            for (java.io.File f : files)
            {
                if (f.getName().startsWith(filePrefix))
                {
                    String name = f.isDirectory() ? f.getName() + "/" : f.getName();
                    if (toComplete.contains("/") || toComplete.contains("\\"))
                    {
                        int lastSep = Math.max(toComplete.lastIndexOf('/'), toComplete.lastIndexOf('\\'));
                        name = toComplete.substring(0, lastSep + 1) + name;
                    }
                    matches.add(name);
                }
            }
        }

        if (matches.size() == 1)
        {
            inputField.setText(prefix + matches.get(0));
        }
        else if (matches.size() > 1)
        {
            // Find common prefix
            String common = matches.get(0);
            for (String m : matches) {
                while (!m.startsWith(common)) common = common.substring(0, common.length() - 1);
            }
            if (common.length() > toComplete.length()) inputField.setText(prefix + common);
            else System.out.println(String.join("  ", matches));
        }
    }

    private void navigateHistory(int dir)
    {
        if (history.isEmpty()) return;
        historyIndex = Math.max(0, Math.min(history.size(), historyIndex + dir));
        inputField.setText(historyIndex < history.size() ? history.get(historyIndex) : "");
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            new ShellGUI().setVisible(true);
        });
    }
}
