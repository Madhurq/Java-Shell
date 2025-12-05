package main;

public class Exit {
    public static boolean handle(String command) {
        return "exit".equals(command.trim());
    }
}
