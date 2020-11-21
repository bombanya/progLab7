package please.help.commands;

import please.help.CollectionManager;
import please.help.UserAuthorizer;

import java.util.LinkedList;
import java.util.Scanner;

public class Create_new_user extends Command{

    private static final long serialVersionUID = 20201026L;
    private String login;
    private String password;

    public Create_new_user(){
        commandName = "create_new_user";
    }

    private Create_new_user(String login, String password){
        commandName = "create_new_user";
        this.login = login;
        this.password = password;
    }

    @Override
    public String execute(CollectionManager manager, String login, String password) {
        if (manager.authorizer.loginAlreadyExists(this.login)){
            return "Новый аккаунт не создан - аккаунт с таким логином уже существует";
        }
        if (manager.authorizer.createNewUser(this.login, this.password)){
            return "Новый пользователь создан.";
        }
        else return "Новый пользователь не создан - что-то пошло не так.";
    }

    @Override
    public Create_new_user validateCommand(LinkedList<String[]> data, CollectionManager manager) {
        if (data.size() == 0 || data.poll().length > 1) {
            System.out.println("Неверно введена комманда.");
            return null;
        }
        System.out.println("Процесс можно прервать на любом этапе, введя 'stopInit'.");
        Scanner scan = new Scanner(System.in);
        String[] input;
        while (true) {
            System.out.print("Введите логин (Не должно быть пробелов внутри логина, не может быть пустой строкой): ");
            input = scan.nextLine().trim().split("\\s+");
            if (input.length == 1){
                if (input[0].equals("stopInit")) return new Create_new_user();
                else if (input[0].equals("")) System.out.println("Логин не может быть пустым полем.");
                else if (manager.authorizer.loginAlreadyExists(input[0])) {
                    System.out.println("Аккаунт с таким логином уже существует.");
                }
                else{
                    login = input[0];
                    break;
                }
            }
            else{
                System.out.println("В логине не должно быть пробелов.");
            }
        }
        while (true) {
            if (password == null) {
                System.out.print("Введите пароль (Не может быть пробелов внутри пароля, минимум 5 символов): ");
                input = scan.nextLine().trim().split("\\s+");
                if (input.length == 1) {
                    if (input[0].equals("stopInit")) return new Create_new_user();
                    else if (input[0].length() < 5) System.out.println("Пароль должен состоять " +
                            "как минимум из 5 символов");
                    else password = input[0];
                } else {
                    System.out.println("В пароле не должно быть пробелов.");
                }
            }
            else{
                System.out.print("Введите пароль повторно: ");
                input = scan.nextLine().trim().split("\\s+");
                if (input.length == 1){
                    if (input[0].equals("stopInit")) return new Create_new_user();
                    else if (input[0].equals(password)) break;
                }
                System.out.println("Пароли различаются");
                password = null;
            }
        }
        Create_new_user create_new_user = new Create_new_user(login, UserAuthorizer.getHash(password));
        create_new_user.makeValid();
        return create_new_user;
    }
}
