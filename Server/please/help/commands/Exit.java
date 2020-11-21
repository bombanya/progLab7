package please.help.commands;

import please.help.CollectionManager;
import please.help.Mode;

import java.util.LinkedList;

/**
 * Класс для комманды exit.
 * Формат комманды: exit
 */
public class Exit extends Command{

    private static final long serialVersionUID = 20200916L;

    public Exit(){
        commandName = "exit";
    }

    @Override
    public String execute(CollectionManager manager, String login, String password) {
        manager.setManagerMode(Mode.EXIT);
        return "Работа сервера завершается";
    }

    @Override
    public Exit validateCommand(LinkedList<String[]> data, CollectionManager manager) {
        if (data.size() == 0 || data.poll().length > 1) {
            System.out.println("Неверно введена комманда.");
            return null;
        }
        Exit exit = new Exit();
        exit.makeValid();
        return exit;
    }
}
