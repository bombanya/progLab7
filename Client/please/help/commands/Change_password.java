package please.help.commands;

import please.help.ClientManager;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Scanner;

public class Change_password extends Command{

    private static final long serialVersionUID = 20201026L;
    private String newPassword;

    public Change_password(){
        commandName = "change_password";
    }

    private Change_password(String newPassword){
        commandName = "change_password";
        this.newPassword = newPassword;
    }

    public static String getHash(String str) {
        try {
            StringBuilder hex = new StringBuilder(new BigInteger(1, MessageDigest.getInstance("SHA-224")
                    .digest(str.getBytes())).toString(16));
            while (hex.length() < 56) hex.insert(0, '0');
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e){
            return null;
        }
    }

    @Override
    public Change_password validateCommand(LinkedList<String[]> data, ClientManager manager) {
        if (data.size() == 0 || data.poll().length > 1) {
            System.out.println("Неверно введена комманда.");
            return null;
        }
        System.out.println("Процесс можно прервать на любом этапе, введя 'stopInit'.");
        Scanner scan = new Scanner(System.in);
        String[] input;
        while (true) {
            if (newPassword == null) {
                System.out.print("Введите новый пароль (Не может быть пробелов внутри пароля, минимум 5 символов): ");
                input = scan.nextLine().trim().split("\\s+");
                if (input.length == 1) {
                    if (input[0].equals("stopInit")) return new Change_password();
                    else if (input[0].length() < 5) System.out.println("Пароль должен состоять " +
                            "как минимум из 5 символов");
                    else newPassword = input[0];
                } else {
                    System.out.println("В пароле не должно быть пробелов.");
                }
            }
            else{
                System.out.print("Введите пароль повторно: ");
                input = scan.nextLine().trim().split("\\s+");
                if (input.length == 1){
                    if (input[0].equals("stopInit")) return new Change_password();
                    else if (input[0].equals(newPassword)) break;
                }
                System.out.println("Пароли различаются");
                newPassword = null;
            }
        }
        Change_password change_password = new Change_password(getHash(newPassword));
        Object result = manager.getClient().sendCommand(change_password,manager.getLogin(), manager.getPassword());
        if (result instanceof String) System.out.println(result);
        if (result.equals("Пароль изменен.")) manager.setPassword(getHash(newPassword));
        else System.out.print("Пароль не изменен.");
        return new Change_password();
    }
}
