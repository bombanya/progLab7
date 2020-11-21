package please.help;

import please.help.commands.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

public class ClientManager{

    private Mode managerMode = Mode.CONSOLE;
    private int currentScriptSkip = 0;
    private String currentScript;
    private final Client client;

    private String login;
    private String password;

    private final ArrayList<Command> listOfCommands = new ArrayList<>();

    {
        listOfCommands.add(new Add());
        listOfCommands.add(new Help());
        listOfCommands.add(new Info());
        listOfCommands.add(new Show());
        listOfCommands.add(new Update());
        listOfCommands.add(new Remove_by_id());
        listOfCommands.add(new Clear());
        listOfCommands.add(new Execute_script());
        listOfCommands.add(new History());
        listOfCommands.add(new Add_if_max());
        listOfCommands.add(new Remove_greater());
        listOfCommands.add(new Average_of_annual_turnover());
        listOfCommands.add(new Count_by_annual_turnover());
        listOfCommands.add(new Print_field_ascending_type());
        listOfCommands.add(new Exit());
        listOfCommands.add(new Change_password());
        listOfCommands.add(new Change_user());
        listOfCommands.add(new Delete_user());
        listOfCommands.add(new Create_new_user());
    }

    private ClientManager(String ip, int port) {
        this.client = new Client(ip, port);
    }

    public static ClientManager createManager(){
        System.out.println("Введите ip и номер порта сервера в одну строку через пробел:");
        Scanner scan = new Scanner(System.in);
        while (true) {
            try {
                String[] input = scan.nextLine().trim().split("\\s+");
                if (input.length == 2) {
                    return new ClientManager(input[0], Integer.parseInt(input[1]));
                }
                System.out.println("Некорректный ввод. Повторите попытку.");
            }
            catch (NumberFormatException e){
                System.out.println("Некорректный ввод. Повторите попытку.");
            }
        }
    }

    public void start(){

        LinkedList<String[]> inputList = new LinkedList<>();
        String[] textForOld = new String[]{"change_user"};
        String[] textForNew = new String[]{"create_new_user"};
        Scanner scan = new Scanner(System.in);

        while (login == null){
            System.out.print("Для начала работы необходимо авторизоваться.\n" +
                    "Для того, чтобы завершить программу на этом этапе, введите 'stopInit'\n" +
                    "Создать новый аккаунт или использовать существующий? (new/old): ");
            String[] input = scan.nextLine().trim().split("\\s+");
            if (input.length == 1) {
                if (input[0].equals("new")) {
                    inputList.add(textForNew);
                    (new Create_new_user()).validateCommand(inputList, this);
                } else if (input[0].equals("old")) {
                    inputList.add(textForOld);
                    (new Change_user()).validateCommand(inputList, this);
                }
                else if (input[0].equals("stopInit")){
                    managerMode = Mode.EXIT;
                    break;
                }
                else System.out.println("Некорректный ввод. Повторите попытку.");
            }
            else System.out.println("Некорректный ввод. Повторите попытку.");
        }
        if (managerMode != Mode.EXIT) System.out.println("Вывести справку по доступным командам: 'help'.\n-----");

        while (managerMode != Mode.EXIT){
            System.out.print("[" + login + "]: ");
            inputList.add(scan.nextLine().trim().split("\\s+"));
            executeCommand(inputList);
        }
    }

    public boolean executeCommand(LinkedList<String[]> inputList){
        for (Command c : listOfCommands) {
            if (inputList.peek()[0].equals(c.getCommandName())) {
                Command newCommand = c.validateCommand(inputList, this);
                if (newCommand != null) {
                    if (newCommand.isValid()) {
                        System.out.println((String) client.sendCommand(newCommand, login, password));
                    }
                    if (!(c.getCommandName().equals("execute_script"))) System.out.println("-----");
                    return true;
                } else {
                    System.out.println("-----");
                    return false;
                }
            }
        }
        System.out.println("Некорректный ввод. Повторите попытку.\n-----");
        inputList.poll();
        return false;

    }

    public void setManagerMode(Mode managerMode) {
        this.managerMode = managerMode;
    }

    public Mode getManagerMode() {
        return managerMode;
    }

    public void setCurrentScriptSkip(int currentScriptSkip) {
        this.currentScriptSkip = currentScriptSkip;
    }

    public int getCurrentScriptSkip() {
        return currentScriptSkip;
    }

    public void incrementScriptSkip(){
        currentScriptSkip++;
    }

    public void setCurrentScript(String currentScript) {
        this.currentScript = currentScript;
    }

    public String getCurrentScript() {
        return currentScript;
    }

    public Client getClient() {
        return client;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
