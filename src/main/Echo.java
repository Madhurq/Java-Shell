package main;

public class Echo {
    public static void say(String[] words) {
        for (int i = 1; i < words.length; i++) {
            if (i > 1) {
                System.out.print(" ");
            }
            System.out.print(words[i]);
        }
        System.out.print("\n$ ");
    }
}
