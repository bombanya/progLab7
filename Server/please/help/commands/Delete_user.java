package please.help.commands;

import please.help.CollectionManager;

import java.util.LinkedList;
import java.util.Scanner;

public class Delete_user extends Command{

    private static final long serialVersionUID = 20201026L;
    private String loginToDelete;

    public Delete_user(){
        commandName = "delete_user";
    }

    private Delete_user(String loginToDelete){
        this.loginToDelete = loginToDelete;
        commandName = "delete_user";
    }

    @Override
    public String execute(CollectionManager manager, String login, String password) {
        if (loginToDelete.equals("admin")) return "Пользователь admin не может быть удален.";
        else if (!loginToDelete.equals(login) && !login.equals("admin"))
            return "Пользователя может удалить только он сам или admin.";
        else{
            if (manager.authorizer.deleteUser(login, password, loginToDelete)){
                (new Clear()).execute(manager, loginToDelete, null);
                return "Пользователь удален.";
            }
            else return "Пользователь не удален - что-то пошло не так.";
        }
    }

    @Override
    public Delete_user validateCommand(LinkedList<String[]> data, CollectionManager manager) {
        if (data.size() == 0 || data.peek().length > 2) {
            System.out.println("Неверно введена комманда.");
            data.poll();
            return null;
        }
        loginToDelete = data.peek().length == 1 ? manager.getLogin() : data.peek()[1];
        data.poll();
        System.out.println("Внимание! Будет удален пользователь " + loginToDelete + " и все элементы коллекции" +
                ", принадлежащие ему.\n" +
                "Не существует способа их восстановить.\n" +
                "Для подтверждения операции введите логин удаляемого пользователя.");
        Scanner scan = new Scanner(System.in);
        String input = scan.nextLine();
        if (!input.equals(loginToDelete)){
            System.out.println("Пользователь не будет удален.");
            return new Delete_user();
        }
        else{
            Delete_user delete_user = new Delete_user(loginToDelete);
            delete_user.makeValid();
            return delete_user;
        }
    }
}

