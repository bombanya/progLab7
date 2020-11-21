package please.help.commands;

import please.help.ClientManager;

import java.util.LinkedList;
import java.util.Scanner;

public class Change_user extends Command{

    private static final long serialVersionUID = 20201026L;
    private String login;
    private String password;

    public Change_user(){
        commandName = "change_user";
    }

    public Change_user(String login, String password){
        commandName = "change_user";
        this.login = login;
        this.password = password;
    }

    @Override
    public Change_user validateCommand(LinkedList<String[]> data, ClientManager manager) {
        if (data.size() == 0 || data.poll().length > 1) {
            System.out.println("Неверно введена комманда.");
            return null;
        }
        System.out.println("Процесс можно прервать на любом этапе, введя 'stopInit'.");
        Scanner scan = new Scanner(System.in);
        System.out.print("Введите логин: ");
        String login = scan.nextLine().trim();
        if (login.equals("stopInit")) return new Change_user();
        System.out.print("Введите пароль: ");
        String password = scan.nextLine().trim();
        if (password.equals("stopInit")) return new Change_user();
        Change_user commandToSend = new Change_user(login, Change_password.getHash(password));

        Object result = manager.getClient().sendCommand(commandToSend, manager.getLogin(), manager.getPassword());
        if (result == null) return new Change_user();
        if (!(result instanceof Boolean) || result.equals(false)){
            System.out.print("Неверно введен логин или пароль.");
        }
        else{
            manager.setLogin(login);
            manager.setPassword(Change_password.getHash(password));
            System.out.println("Вы авторизованы как " + login);
        }
        return new Change_user();
    }
}
