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
        outputArea.append("Java Shell GUI\n\n");
    }

    private void setupUI()
    {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        Color bg = new Color(30, 30, 30), fg = new Color(200, 200, 200);

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
        promptLabel.setForeground(new Color(100, 180, 100));
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
        });
        // Prevent Tab from moving focus
        inputField.setFocusTraversalKeysEnabled(false);

        inputPanel.add(promptLabel, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(bg);
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        main.add(inputPanel, BorderLayout.SOUTH);
        add(main);
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
