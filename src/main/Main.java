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

            while (sc.hasNextLine()) {
                String command = sc.nextLine();
                if (Exit.handle(command))
                {
                    break;
                }

            }


        }
    }
}

