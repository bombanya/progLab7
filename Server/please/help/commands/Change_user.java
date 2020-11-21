package please.help.commands;

import please.help.CollectionManager;
import please.help.network_part.Listener;
import please.help.UserAuthorizer;

import java.util.LinkedList;
import java.util.Scanner;

public class Change_user extends Command{

    private static final long serialVersionUID = 20201026L;
    private String login;
    private String password;

    public Change_user(){
        commandName = "change_user";
    }

    @Override
    public Boolean execute(CollectionManager manager, String login, String password) {
        return manager.authorizer.authorize(this.login, this.password);
    }

    @Override
    public Change_user validateCommand(LinkedList<String[]> data, CollectionManager manager) {
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
        if (manager.authorizer.authorize(login, UserAuthorizer.getHash(password))){
            Listener.logger.info("Пользователем login: {} с консоли выполнена команда {}",
                    manager.getLogin(), getCommandName());
            manager.history.addCommand(this, manager.getLogin());
            manager.setLogin(login);
            manager.setPassword(UserAuthorizer.getHash(password));
            System.out.println("Вы авторизованы как " + login);
        }
        else System.out.println("Неверно введен логин или пароль.");
        return new Change_user();
    }
}
