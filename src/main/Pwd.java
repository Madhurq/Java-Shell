package main;

public class Pwd {
    public static void getdir() {
        String dir = System.getProperty("user.dir");
        System.out.print(dir);
        System.out.print("\n$ ");
    }
}
