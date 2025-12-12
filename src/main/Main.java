package main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.io.File;


public class Main {
    public static void main(String[] args) throws Exception {
        System.out.print("$ ");
        try (Scanner sc = new Scanner(System.in)) {
            HashMap<String, String> builtins = new HashMap<>();
            builtins.put("echo", "echo is a shell builtin");
            builtins.put("exit", "exit is a shell builtin");
            builtins.put("type", "type is a shell builtin");
            builtins.put("pwd", "pwd is a shell builtin");
            builtins.put("cd", "cd is a shell builtin");

            while (sc.hasNextLine()) {
                String command = sc.nextLine();
                //Exit cmds
                if (Exit.handle(command))
                    break;
                //Echo cmds
                String[] words = (Parsec.parse(command));

                if (words.length > 0 && words[0].equals("echo")) {
                    Echo.say(command);
                    continue;
                }

                //Type cmds
                if (words.length > 0 && words[0].equals("type")) {
                    Type.show(builtins, command);
                    continue;
                }

                //pwd code
                if (words.length > 0 && words[0].equals("pwd")) {
                    Pwd.getdir();
                    continue;
                }

                //cd code
                if (words.length > 0 && words[0].equals("cd")) {
                    Cd.change(words[1]);
                    continue;
                }

                //Command exec
                String os = System.getProperty("os.name").toLowerCase();
                String ps = os.contains("win") ? ";" : ":";
                String ds = os.contains("win") ? "\\" : "/";
                String path = System.getenv("PATH");
                String[] dir = path.split(ps);
                boolean found = false;
                for (String dir1 : dir) {
                    String fp = dir1 + ds + words[0] + (os.contains("win")?".exe" : "");
                    File f = new File(fp);
                    if (f.exists() && f.canExecute()) {
                        List<String> cmd = new ArrayList<>();
                        cmd.add(fp);
                        for (int i = 1; i < words.length; i++) {
                            cmd.add(words[i]);
                        }
                        ProcessBuilder pb = new ProcessBuilder(cmd);
                        pb.inheritIO();
                        Process p = pb.start();
                        p.waitFor();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.print(words[0] + ": not found \n$ ");
                    continue;
                }
                System.out.print("$ ");
            }
        }
    }
}

