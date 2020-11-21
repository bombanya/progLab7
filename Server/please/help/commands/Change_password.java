package please.help.commands;

import please.help.CollectionManager;
import please.help.UserAuthorizer;

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

    @Override
    public String execute(CollectionManager manager, String login, String password) {
        if (manager.authorizer.changePassword(login, password, newPassword)){
            return "Пароль изменен.";
        }
        else return "Пароль не изменен - что-то пошло не так";
    }

    @Override
    public Change_password validateCommand(LinkedList<String[]> data, CollectionManager manager) {
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
        Change_password change_password = new Change_password(UserAuthorizer.getHash(newPassword));
        System.out.println(change_password.execute(manager, manager.getLogin(), manager.getPassword()));
        manager.setPassword(UserAuthorizer.getHash(newPassword));
        return change_password;
    }
}
