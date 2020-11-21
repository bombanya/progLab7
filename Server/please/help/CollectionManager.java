package please.help;

import please.help.commands.*;
import please.help.network_part.Listener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class CollectionManager{

    public final CommandsHistory history = new CommandsHistory();
    public final CollectionShell collectionShell;
    public final UserAuthorizer authorizer;

    private String login;
    private String password;

    private Mode managerMode = Mode.CONSOLE;
    private int currentScriptSkip = 0;
    private String currentScript;

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
        listOfCommands.add(new Change_user());
        listOfCommands.add(new Create_new_user());
        listOfCommands.add(new Delete_user());
        listOfCommands.add(new Change_password());
    }

    public CollectionManager(CollectionShell collectionShell, UserAuthorizer authorizer) {
        this.collectionShell = collectionShell;
        this.authorizer = authorizer;
    }

    public void start(){
        LinkedList<String[]> inputList = new LinkedList<>();
        Scanner scan = new Scanner(System.in);
        Thread serverThread;

        System.out.println("Введите номер порта для сервера:");
        while (true) {
            try {
                String[] input = scan.nextLine().trim().split("\\s+");
                if (input.length == 1) {
                    serverThread = new Thread(Listener.createServer(Integer.parseInt(input[0]), this));
                    serverThread.start();
                    System.out.println("Сервер начал работу.");
                    break;
                }
                System.out.println("Некорректный ввод. Повторите попытку.");
            }
            catch (NumberFormatException e){
                System.out.println("Некорректный ввод. Повторите попытку.");
            }
        }

        System.out.println("Вывести справку по доступным командам: 'help'.\n-----");
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
                if (newCommand != null){
                    if (c.getCommandName().equals("change_user")) {
                        System.out.println("-----");
                        return true;
                    }
                    if (authorizer.authorize(login, password)) {
                        if (newCommand.isValid()) {
                            System.out.println((String) newCommand.execute(this, login, password));
                        }
                        if (!(c.getCommandName().equals("execute_script"))) System.out.println("-----");
                        Listener.logger.info("Пользователем login: {} с консоли выполнена команда {}",
                                login, newCommand.getCommandName());
                        history.addCommand(newCommand, login);
                        return true;
                    }
                    else System.out.println("Пользователь не авторизован, команда не выполнена.");
                }
                System.out.println("-----");
                return false;
            }
        }
        System.out.println("Некорректный ввод. Повторите попытку.\n-----");
        inputList.poll();
        return false;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }
}
